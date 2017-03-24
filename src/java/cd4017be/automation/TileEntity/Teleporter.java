package cd4017be.automation.TileEntity;

import net.minecraft.network.PacketBuffer;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import com.mojang.authlib.GameProfile;

import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.Environment;
import li.cil.oc.api.network.Message;
import li.cil.oc.api.network.Node;
import cd4017be.api.automation.AreaProtect;
import cd4017be.api.automation.IOperatingArea;
import cd4017be.api.automation.PipeEnergy;
import cd4017be.api.computers.ComputerAPI;
import cd4017be.automation.Config;
import cd4017be.automation.Objects;
import cd4017be.automation.Item.ItemTeleporterCoords;
import cd4017be.lib.MovedBlock;
import cd4017be.lib.Gui.DataContainer;
import cd4017be.lib.Gui.DataContainer.IGuiData;
import cd4017be.lib.Gui.TileContainer;
import cd4017be.lib.Gui.TileContainer.ISlotClickHandler;
import cd4017be.lib.templates.AutomatedTile;
import cd4017be.lib.templates.Inventory;
import cd4017be.lib.templates.Inventory.IAccessHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

/**
 *
 * @author CD4017BE
 */
@Optional.Interface(iface = "li.cil.oc.api.network.Environment", modid = "OpenComputers")
public class Teleporter extends AutomatedTile implements IOperatingArea, Environment, IGuiData, IAccessHandler, ISlotClickHandler {

	public static int Umax = 8000;
	public static float resistor = 50F;
	public static float eScale = (float)Math.sqrt(1D - 1D / resistor);
	public static float Energy = 2000F;
	private int[] area = new int[6];
	private int[] target = new int[4];
	
	private static final GameProfile defaultUser = new GameProfile(new UUID(0, 0), "#Teleporter");
	private GameProfile lastUser = defaultUser;
	public int tgX, tgY, tgZ, mode;
	public float Estor, Eneed;
	
	public Teleporter() {
		inventory = new Inventory(16, 1, null).group(0, 1, 2, 0);
		energy = new PipeEnergy(Umax, Config.Rcond[2]);
		//mode[int 3]: 0x1 = use rst, 0x2 = rel coord, 0x4 = last rst, 0x8 = teleport, 0x10 = copy
	}
	
	@Override
	public void onPlaced(EntityLivingBase entity, ItemStack item)  
	{
		if (entity instanceof EntityPlayer) lastUser = ((EntityPlayer)entity).getGameProfile();
	}
	
	@Override
	public boolean onActivated(EntityPlayer player, EnumHand hand, ItemStack item, EnumFacing s, float X, float Y, float Z) 
	{
		return super.onActivated(player, hand, item, s, X, Y, Z);
	}
	
	@Override
	public void update() 
	{
		super.update();
		if (worldObj.isRemote) return;
		if ((mode & 1) == 1) {
			boolean r = worldObj.isBlockIndirectlyGettingPowered(pos) > 0;
			if (r && (mode & 4) == 0 && isInWorldBounds()) {
				mode ^= 4;
				initTeleportation();
			} else if (!r && (mode & 4) == 4) {
				mode ^= 4;
				mode &= ~8;
			}
		}
		if ((mode & 8) != 0) {
			if (Estor < Eneed) {
				Estor += energy.getEnergy(0, resistor);
				energy.Ucap *= eScale;
			} else {
				Estor -= Eneed;
				Eneed = 0;
				teleport();
			}
		} else if (node != null && Estor < 1000F) {
			Estor += energy.getEnergy(0, resistor);
			energy.Ucap *= eScale;
		}
		Estor -= ComputerAPI.update(this, node, (mode & 8) == 0 ? Estor : 0);
	}
	
	public boolean isInWorldBounds()
	{
		int yn = area[1] + tgY;
		int yp = area[4] + tgY;
		if ((mode & 2) == 0) {yn -= pos.getY(); yp -= pos.getY();}
		return area[1] >= 0 && area[4] < 256 && yn >= 0 && yp < 256;
	}
	
	private void initTeleportation()
	{
		int sx = area[3] - area[0];
		int sy = area[4] - area[1];
		int sz = area[5] - area[2];
		int dx = tgX;
		int dy = tgY;
		int dz = tgZ;
		if ((mode & 2) == 0)
		{
			dx -= pos.getX();
			dy -= pos.getY();
			dz -= pos.getZ();
		}
		World world = worldObj;
		int[] maxS = Handler.maxSize(this);
		if ((dx != 0 || dy != 0 || dz != 0) && (sx <= maxS[0] && sy <= maxS[1] && sz <= maxS[2]))
		{
			mode |= 8;
			Eneed = (float)sx * (float)sy * (float)sz * (float)Math.sqrt((double)dx * (double)dx + (double)dy * (double)dy + (double)dz * (double)dz) * Energy;
			TileEntity te = worldObj.getTileEntity(pos.add(dx, dy, dz));
			if (te != null && te instanceof InterdimHole) {
				InterdimHole idwh = (InterdimHole)te;
				Eneed += (float)(sx * sy * sz) * 256D * Energy;
				dx = idwh.linkX - idwh.getPos().getX();
				dy = idwh.linkY - idwh.getPos().getY();
				dz = idwh.linkZ - idwh.getPos().getZ();
				world = DimensionManager.getWorld(idwh.linkD);
				if (world == null){
					mode &= ~8;
					return;
				}
			}
		} else return;
		if (!AreaProtect.operationAllowed(lastUser, worldObj, area[0] >> 4, (area[3] + 15) >> 4, area[2] >> 4, (area[5] + 15) >> 4) ||
			!AreaProtect.operationAllowed(lastUser, world, (area[0] + dx) >> 4, (area[3] + dx + 15) >> 4, (area[2]+ dz) >> 4, (area[5] + dz + 15) >> 4)) 
			mode &= ~8;
		target = new int[]{area[0] + dx, area[1] + dy, area[2] + dz, world.provider.getDimension()};
	}
	
	private void teleport()
	{
		if (context != null) {
			ComputerAPI.sendEvent(context, "teleport_done");
			context = null;
		}
		mode &= ~8;
		int[] pos = {area[0], area[1], area[2], area[3] - area[0], area[4] - area[1], area[5] - area[2]};
		World world = DimensionManager.getWorld(target[3]);
		if (world == null) return;
		if (this.pos.getX() < area[0] || this.pos.getX() >= area[3] || this.pos.getY() < area[1] || this.pos.getY() >= area[4] || this.pos.getZ() < area[2] || this.pos.getZ() >= area[5]) {
			area[0] = target[0];
			area[1] = target[1];
			area[2] = target[2];
			area[3] = area[0] + pos[3];
			area[4] = area[1] + pos[4];
			area[5] = area[2] + pos[5];
		}
		this.markUpdate();
		BlockPos size = new BlockPos(pos[3], pos[4], pos[5]);
		BlockPos pos0 = new BlockPos(pos[0], pos[1], pos[2]);
		BlockPos pos1 = new BlockPos(target[0], target[1], target[2]);
		if (!worldObj.isAreaLoaded(pos0, pos0.add(size)))
			for (int cx = pos[0] >> 4; cx <= (pos[0] + pos[3]) >> 4; cx++)
				for (int cz = pos[2] >> 4; cz <= (pos[2] + pos[5]) >> 4; cz++)
					worldObj.getChunkProvider().provideChunk(cx, cz);
		if (!world.isAreaLoaded(pos1, pos1.add(size)))
			for (int cx = target[0] >> 4; cx <= (target[0] + pos[3]) >> 4; cx++)
				for (int cz = target[2] >> 4; cz <= (target[2] + pos[5]) >> 4; cz++)
					world.getChunkProvider().provideChunk(cx, cz);
		List<Entity> e0 = worldObj.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos[0], pos[1], pos[2], pos[0] + pos[3], pos[1] + pos[4], pos[2] + pos[5]));
		List<Entity> e1 = world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(target[0], target[1], target[2], target[0] + pos[3], target[1] + pos[4], target[2] + pos[5]));
		int dx = target[0] - pos[0];
		int dy = target[1] - pos[1];
		int dz = target[2] - pos[2];
		for (Entity e : e0) MovedBlock.moveEntity(e, world.provider.getDimension(), e.posX + dx, e.posY + dy, e.posZ + dz);
		for (Entity e : e1) MovedBlock.moveEntity(e, worldObj.provider.getDimension(), e.posX - dx, e.posY - dy, e.posZ - dz);
		boolean ox = dx < 0, oy = dy < 0, oz = dz < 0;
		for (int x = ox ? 0 : pos[3] - 1; ox ? x < pos[3] : x >= 0; x += ox ? 1 : -1)
			for (int y = oy ? 0 : pos[4] - 1; oy ? y < pos[4] : y >= 0; y += oy ? 1 : -1)
				for (int z = oz ? 0 : pos[5] - 1; oz ? z < pos[5] : z >= 0; z += oz ? 1 : -1) {
					pos0 = new BlockPos(pos[0] + x, pos[1] + y, pos[2] + z);
					pos1 = new BlockPos(target[0] + x, target[1] + y, target[2] + z);
					MovedBlock b0 = MovedBlock.get(worldObj, pos0);
					MovedBlock b1 = MovedBlock.get(world, pos1);
					if ((mode & 16) != 0) {
						b0.set(world, pos1);
					} else {
						b0.set(world, pos1);
						b1.set(worldObj, pos0);
					}
				}
	}
	
	public float getStorage() {
		return Estor / Eneed;
	}

	@Override
	protected void customPlayerCommand(byte cmd, PacketBuffer dis, EntityPlayerMP player) throws IOException 
	{
		lastUser = player.getGameProfile();
		if (cmd == 0)
		{
			byte b = dis.readByte();
			if (b == 0)
			{
				mode ^= 4;
				if ((mode & 4) == 4)
				{
					initTeleportation();
				} else
				{
					mode &= ~8;
				}
				return;
			} else
			if (b == 1)
			{
				mode ^= 1;
			} else
			if (b == 2)
			{
				mode ^= 2;
				if ((mode & 2) == 0) {
					tgX += pos.getX();
					tgY += pos.getY();
					tgZ += pos.getZ();
				} else {
					tgX -= pos.getX();
					tgY -= pos.getY();
					tgZ -= pos.getZ();
				}
			} else
			if (b == 3)
			{
				if (inventory.items[0] == null && inventory.items[1] == null)
				{
					if ((mode & 2) == 0){
						tgX = pos.getX();
						tgY = pos.getY();
						tgZ = pos.getZ();
					} else {
						tgX = 0;
						tgY = 0;
						tgZ = 0;
					}
				}
				if (inventory.items[0] != null && (inventory.items[0].getItem() instanceof ItemTeleporterCoords || inventory.items[0].getItem() == Items.PAPER))
				{
					String name = inventory.items[0].getTagCompound() == null ? "" : inventory.items[0].getTagCompound().getString("name");
					inventory.items[0] = new ItemStack(Objects.teleporterCoords, inventory.items[0].stackSize);
					inventory.items[0].setTagCompound(new NBTTagCompound());
					inventory.items[0].getTagCompound().setString("name", name);
					inventory.items[0].getTagCompound().setInteger("x", tgX);
					inventory.items[0].getTagCompound().setInteger("y", tgY);
					inventory.items[0].getTagCompound().setInteger("z", tgZ);
				}
			} else
			if (b == 4)
			{
				if (player.capabilities.isCreativeMode) mode ^= 16;
				else mode &= ~16;
			}
		} else
		if (cmd == 1)
		{
			tgX = dis.readInt();
		} else
		if (cmd == 2)
		{
			tgY = dis.readInt();
		} else
		if (cmd == 3)
		{
			tgZ = dis.readInt();
		} else
		if (cmd == 4 && inventory.items[0] != null && inventory.items[0].getItem() instanceof ItemTeleporterCoords && inventory.items[0].getTagCompound() != null)
		{
			inventory.items[0].getTagCompound().setString("name", dis.readStringFromBuffer(64));
		}
		mode &= ~8;
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) 
	{
		if (node != null) ComputerAPI.saveNode(node, nbt);
		nbt.setByte("mode", (byte)mode);
		nbt.setInteger("px", tgX);
		nbt.setInteger("py", tgY);
		nbt.setInteger("pz", tgZ);
		nbt.setIntArray("area", area);
		nbt.setFloat("storage", Estor);
		nbt.setString("lastUser", lastUser.getName());
		nbt.setLong("lastUserID0", lastUser.getId().getMostSignificantBits());
		nbt.setLong("lastUserID1", lastUser.getId().getLeastSignificantBits());	
		return super.writeToNBT(nbt);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) 
	{
		super.readFromNBT(nbt);
		if (node != null) ComputerAPI.readNode(node, nbt);
		mode = nbt.getByte("mode");
		tgX = nbt.getInteger("px");
		tgY = nbt.getInteger("py");
		tgZ = nbt.getInteger("pz");
		area = nbt.getIntArray("area");
		if (area == null || area.length != 6) area = new int[6];
		Estor = nbt.getFloat("storage");
		mode &= ~8;
		try {lastUser = new GameProfile(new UUID(nbt.getLong("lastUserID0"), nbt.getLong("lastUserID1")), nbt.getString("lastUser"));
		} catch (Exception e) {lastUser = defaultUser;}
		this.setSlot(0, 14, inventory.items[14]);
	}

	@Override
	public int[] getOperatingArea() 
	{
		return area;
	}

	@Override
	public void updateArea(int[] area) 
	{
		boolean b = false;
		for (int i = 0; i < this.area.length && !b;i++){b = area[i] != this.area[i];}
		if (b) {
			this.area = area;
			mode &= ~8;
		}
		this.markUpdate();
	}

	@Override
	public int insertAm(int g, int s, ItemStack item, ItemStack insert) {
		return s != 1 || item == null || item.getItem() instanceof ItemTeleporterCoords ? super.insertAm(g, s, item, insert) : 0;
	}

	@Override
	public void initContainer(DataContainer cont) {
		TileContainer container = (TileContainer)cont;
		container.clickHandler = this;
		container.addItemSlot(new SlotItemHandler(inventory, 0, 116, 52));
		container.addItemSlot(new SlotItemHandler(inventory, 1, 152, 52));
		for (int i = 0; i < 9; i++)
		{
			container.addItemSlot(new SlotItemHandler(inventory, 2 + i, 8 + i * 18, 16));
		}
		
		container.addPlayerInventory(8, 86);
	}

	@Override
	public boolean transferStack(ItemStack item, int s, TileContainer container) {
		if (s < 2) container.mergeItemStack(item, 2, 11, false);
		else if (s >= 2 && s < 11) container.mergeItemStack(item, 1, 2, false);
		else container.mergeItemStack(item, 2, 11, false);
		return true;
	}

	@Override
	public IItemHandler getUpgradeSlots() {
		return inventory.new SlotAccess(11, 5);
	}

	@Override
	public int[] getBaseDimensions() 
	{
		return new int[]{12, 12, 12, Integer.MAX_VALUE, Config.Umax[2]};
	}

	@Override
	public void setSlot(int g, int s, ItemStack item) {
		inventory.items[s] = item;
		if (s == 11) this.markUpdate();
		else if (s == 14) {
			float u = energy.Ucap;
			energy = new PipeEnergy(Handler.Umax(this), Config.Rcond[2]);
			energy.Ucap = u;
		} else if (s == 12 || s == 13){
			Handler.setCorrectArea(this, area, true);
		} else if (s >= inventory.groups[0].s && s < inventory.groups[0].e && item != null && item.getItem() instanceof ItemTeleporterCoords && item.getTagCompound() != null) {
			tgX = item.getTagCompound().getInteger("x");
			tgY = item.getTagCompound().getInteger("y");
			tgZ = item.getTagCompound().getInteger("z");
		}
	}
	
	@Override
	public boolean remoteOperation(BlockPos pos) 
	{
		return true;
	}

	@Override
	public IOperatingArea getSlave() 
	{
		return null;
	}

	@Override
	public int[] getSyncVariables() {
		return new int[]{tgX, tgY, tgZ, mode, Float.floatToIntBits(Estor), Float.floatToIntBits(Eneed)};
	}

	@Override
	public void setSyncVariable(int i, int v) {
		switch(i) {
		case 0: tgX = v; break;
		case 1: tgY = v; break;
		case 2: tgZ = v; break;
		case 3: mode = v; break;
		case 4: Estor = Float.intBitsToFloat(v); break;
		case 5: Eneed = Float.intBitsToFloat(v); break;
		}
	}

	//OpenComputers:
	
	private Object node = ComputerAPI.newOCnode(this, "Automation-Teleporter", true);
	private Object context = null;
	
	@Optional.Method(modid = "OpenComputers")
	@Override
	public Node node() 
	{
		return (Node)node;
	}

	@Override
	public void invalidate() 
	{
		super.invalidate();
		ComputerAPI.removeOCnode(node);
	}

	@Override
	public void onChunkUnload() 
	{
		super.onChunkUnload();
		ComputerAPI.removeOCnode(node);
	}

	@Optional.Method(modid = "OpenComputers")
	@Override
	public void onConnect(Node node) {}

	@Optional.Method(modid = "OpenComputers")
	@Override
	public void onDisconnect(Node node) {}

	@Optional.Method(modid = "OpenComputers")
	@Override
	public void onMessage(Message message) {}
	
	@Optional.Method(modid = "OpenComputers")
	@Callback(doc = "function():int{sizeX, sizeY, sizeZ, baseX, baseY, baseZ} --returns the operating area size and position", direct = true)
	public Object[] getArea(Context cont, Arguments args) 
	{
		return new Object[]{area[3] - area[0], area[4] - area[1], area[5] - area[2], area[0], area[1], area[2]};
	}
	
	@Optional.Method(modid = "OpenComputers")
	@Callback(doc = "function():int{pos} --returns the position of the teleporter block", direct = true)
	public Object[] getPosition(Context cont, Arguments args)
	{
		return new Object[]{pos.getX(), pos.getY(), pos.getZ()};
	}
	
	@Optional.Method(modid = "OpenComputers")
	@Callback(doc = "function(x:int, y:int, z:int, rel:bool) --sets the teleporter target position in relative (rel=true) or absolute (rel=false) coordinates", direct = true)
	public Object[] setCoords(Context cont, Arguments args)
	{
		mode &= ~8;
		tgX = args.checkInteger(0);
		tgY = args.checkInteger(1);
		tgZ = args.checkInteger(2);
		if (args.checkBoolean(3)) mode |= 2;
		else mode &= ~2;
		return null;
	}
	
	@Optional.Method(modid = "OpenComputers")
	@Callback(doc = "function():bool --starts teleportation energy accumulation and returns true if succeed", direct = true)
	public Object[] teleport(Context cont, Arguments args) throws Exception
	{
		if (!isInWorldBounds()) throw new Exception("Area out of world bounds! For safety reasons this teleport was denied!");
		if ((mode & 8) == 0) {
			initTeleportation();
			if ((mode & 8) != 0) {
				context = cont;
				return new Object[]{true};
			}
		}
		return new Object[]{false};
	}

}
