package cd4017be.automation.TileEntity;

import net.minecraft.network.PacketBuffer;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.mojang.authlib.GameProfile;

import net.minecraftforge.fml.common.Optional;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.Environment;
import li.cil.oc.api.network.Message;
import li.cil.oc.api.network.Node;
import cd4017be.api.automation.IOperatingArea;
import cd4017be.api.automation.PipeEnergy;
import cd4017be.api.computers.ComputerAPI;
import cd4017be.automation.Config;
import cd4017be.automation.Item.ItemMachineSynchronizer;
import cd4017be.automation.Item.ItemMinerDrill;
import cd4017be.lib.Gui.DataContainer;
import cd4017be.lib.Gui.DataContainer.IGuiData;
import cd4017be.lib.Gui.TileContainer;
import cd4017be.lib.templates.AutomatedTile;
import cd4017be.lib.templates.Inventory;
import cd4017be.lib.templates.Inventory.IAccessHandler;
import cd4017be.lib.util.CachedChunkProtection;
import cd4017be.lib.util.Utils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.SlotItemHandler;

/**
 *
 * @author CD4017BE
 */
@Optional.Interface(iface = "li.cil.oc.api.network.Environment", modid = "OpenComputers")
public class Miner extends AutomatedTile implements IOperatingArea, IAccessHandler, Environment, IGuiData {

	public static int Umax = 1200;
	public static float Energy = 40000F;
	public static float resistor = 25F;
	public static float eScale = (float)Math.sqrt(1D - 1D / resistor);
	public static byte maxSpeed = 16;
	private int[] area = new int[6];
	private int px;
	private int py;
	private int pz;
	private float storage;
	private float neededE;
	private IOperatingArea slave = null;
	public boolean active;

	private static final GameProfile defaultUser = new GameProfile(new UUID(0, 0), "#Miner");
	private GameProfile lastUser = defaultUser;
	private CachedChunkProtection prot;

	public Miner() {
		inventory = new Inventory(29, 2, this).group(0, 0, 6, Utils.IN).group(1, 6, 24, Utils.OUT);
		energy = new PipeEnergy(Umax, Config.Rcond[1]);
	}

	@Override
	public void onPlaced(EntityLivingBase entity, ItemStack item) {
		if (entity instanceof EntityPlayer)lastUser = ((EntityPlayer)entity).getGameProfile();
		prot = null;
	}

	@Override
	public boolean onActivated(EntityPlayer player, EnumHand hand, ItemStack item, EnumFacing s, float X, float Y, float Z) {
		lastUser = player.getGameProfile();
		prot = null;
		return super.onActivated(player, hand, item, s, X, Y, Z);
	}

	@Override
	public void update() 
	{
		super.update();
		if (worldObj.isRemote) return;
		if (slave == null || ((TileEntity)slave).isInvalid()) slave = ItemMachineSynchronizer.getLink(inventory.items[28], this);
		double e = energy.getEnergy(0, resistor);
		storage += e;
		storage -= ComputerAPI.update(this, node, storage * 0.5D);
		if (active)
			for (int i = 0; i < maxSpeed; i++) {
				if (breakBlock(new BlockPos(px, py, pz)))
				{
					py--;
					if (py < area[1] || py >= area[4]) {
						py = area[4] - 1;
						px++;
					}
					if (px >= area[3] || px < area[0]) {
						px = area[0];
						pz++;
					}
					if (pz >= area[5] || pz < area[2]) {
						pz = area[2];
						px = area[0];
						py = area[1];
						active = false;
					}
				} else break;
			}
		else if (worldObj.isBlockIndirectlyGettingPowered(getPos()) >= 8) active = true;
		else {
			synchronized(sheduledTasks) {
				while (!sheduledTasks.isEmpty()) {
					if (sheduledTasks.getFirst().execute(this)) sheduledTasks.removeFirst();
					else break;
				}
			}
		}
		if (storage < e + neededE) energy.Ucap *= eScale;
		else storage -= e;
		neededE = 0;
	}
	
	@Override
	public int redstoneLevel(int s, boolean str) {
		return active ? 0 : 7;
	}

	private boolean slaveOp(BlockPos pos)
	{
		if (slave != null && !((TileEntity)slave).isInvalid() && (slave instanceof Pump || slave instanceof Builder) && (slave.getSlave() == null || !(slave.getSlave() instanceof Miner || slave.getSlave().getSlave() != null))) {
			return slave.remoteOperation(pos);
		} else return true;
	}
	
	private boolean breakBlock(BlockPos pos)
	{
		if (!worldObj.isBlockLoaded(pos)) return false;
		if (prot == null || !prot.equalPos(pos)) prot = CachedChunkProtection.get(lastUser, worldObj, pos);
		if (!prot.allow) return true;
		IBlockState state = worldObj.getBlockState(pos);
		Block block = state.getBlock();
		int m = block.getMetaFromState(state);
		if (!this.isHarvestable(state, pos)) return this.slaveOp(pos);
		float hardness = state.getBlockHardness(worldObj, pos);
		float eff = 0;
		int tool = -1;
		if (state.getMaterial().isToolNotRequired()) eff = 1F;
		else for (int i = 0; i < 6; i++)
		{
			if (inventory.items[i] == null) continue;
			Item item = inventory.items[i].getItem();
			if (item instanceof ItemMinerDrill && ((ItemMinerDrill)item).canHarvestBlock(state, m)) {
				if (eff == 0 || ((ItemMinerDrill)item).efficiency < eff)  {
					eff = ((ItemMinerDrill)item).efficiency;
					tool = i;
				}
			}
		}
		if (eff == 0) return true;
		Map<Enchantment, Integer> enchant = tool < 0 ? Collections.<Enchantment, Integer>emptyMap() : EnchantmentHelper.getEnchantments(inventory.items[tool]);
		Integer n = enchant.get(Enchantments.EFFICIENCY);
		if (n != null) eff *= 1F + 0.3F * (float)n;
		float e = hardness * Energy / eff;
		boolean silk;
		try {silk = enchant.containsKey(Enchantments.SILK_TOUCH) && block.canSilkHarvest(worldObj, pos, state, null);} catch (NullPointerException ex) {silk = false;}
		if (silk) e *= 2.5F;
		else {
			n = enchant.get(Enchantments.FORTUNE);
			if (n != null) e *= 1F + (float)n * 0.6F;
		}
		if (storage < e) {
			neededE = e;
			return false;
		}
		List<ItemStack> list;
		if (silk) {
			list = new ArrayList<ItemStack>();
			list.add(new ItemStack(block, 1, block.damageDropped(state)));
		} else {
			list = block.getDrops(worldObj, pos, state, n == null ? 0 : n);
		}
		if (invFull(list.size())) return false;
		storage -= e;
		if (tool >= 0) {
			n = enchant.get(Enchantments.UNBREAKING);
			if (n == null || randomDamage(n, pos)) {
				int d = inventory.items[tool].getItemDamage() + 1;
				if (d >= inventory.items[tool].getMaxDamage()) inventory.items[tool] = null;
				else inventory.items[tool].setItemDamage(d);
			}
		}
		for (ItemStack item : list) 
			this.dropStack(ItemHandlerHelper.insertItemStacked(inventory.new SlotAccess(6, 18), item, false));
		worldObj.setBlockState(pos, Blocks.AIR.getDefaultState(), 2);
		return this.slaveOp(pos);
	}

	private boolean randomDamage(int unbr, BlockPos pos)
	{
		if (area[3] == area[0] || area[4] == area[1] || area[5] == area[2]) return true;
		int i = pos.getZ() % (area[5] - area[2]);
		i *= 31; 
		i += pos.getX() % (area[3] - area[0]);
		i *= 31; 
		i += pos.getY() % (area[4] - area[1]);
		i *= 31;
		return i % (unbr + 3) < 3;
	}

	private boolean isHarvestable(IBlockState state, BlockPos pos)
	{
		if (state == null || state.getBlockHardness(worldObj, pos) < 0 || state.getBlock() instanceof BlockLiquid || state.getBlock() instanceof IFluidBlock) return false;
		else return state.getMaterial().isToolNotRequired() || state.getMaterial() == Material.ROCK || state.getMaterial() == Material.IRON || state.getMaterial() == Material.ANVIL;
	}

	private boolean invFull(int n)
	{
		if (n <= 0) return false;
		for (int i = 6; i < 24; i++)
			if (inventory.items[i] == null && --n <= 0) return false;
		return true;
	}

	private void reset()
	{
		px = area[0];
		py = area[4] - 1;
		pz = area[2];
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
			reset();
		}
		this.markUpdate();
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		if (node != null) ComputerAPI.readNode(node, nbt);
		area = nbt.getIntArray("area");
		if (area.length != 6) area = new int[6];
		px = nbt.getInteger("px");
		py = nbt.getInteger("py");
		pz = nbt.getInteger("pz");
		storage = nbt.getFloat("storage");
		try {lastUser = new GameProfile(new UUID(nbt.getLong("lastUserID0"), nbt.getLong("lastUserID1")), nbt.getString("lastUser"));
		} catch (Exception e) {lastUser = defaultUser;}
		prot = null;
		active = nbt.getBoolean("run");
		this.setSlot(0, 27, inventory.items[27]);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) 
	{
		if (node != null) ComputerAPI.saveNode(node, nbt);
		nbt.setIntArray("area", area);
		nbt.setInteger("px", px);
		nbt.setInteger("py", py);
		nbt.setInteger("pz", pz);
		nbt.setFloat("storage", storage);
		nbt.setString("lastUser", lastUser.getName());
		nbt.setLong("lastUserID0", lastUser.getId().getMostSignificantBits());
		nbt.setLong("lastUserID1", lastUser.getId().getLeastSignificantBits());
		nbt.setBoolean("run", active);
		return super.writeToNBT(nbt);
	}
		
	@Override
	public void initContainer(DataContainer cont) {
		TileContainer container = (TileContainer)cont;
		for (int i = 0; i < 2; i++)
		{
			for (int j = 0; j < 3; j++)
			{
				container.addItemSlot(new SlotItemHandler(inventory, i + j * 2, 134 + i * 18, 16 + j * 18));
			}
		}
		for (int i = 0; i < 6; i++)
		{
			for (int j = 0; j < 3; j++)
			{
				container.addItemSlot(new SlotItemHandler(inventory, 6 + i + j * 6, 8 + i * 18, 16 + j * 18));
			}
		}
		
		container.addPlayerInventory(8, 86);
	}

	@Override
	public int[] getSyncVariables() {
		return new int[]{active ? -1 : 0};
	}

	@Override
	public void setSyncVariable(int i, int v) {
		active = v != 0;
	}

	@Override
	public boolean transferStack(ItemStack item, int s, TileContainer container) {
		if (s < container.invPlayerS) container.mergeItemStack(item, container.invPlayerS, container.invPlayerE, false);
		else if (item.getItem() instanceof ItemMinerDrill) container.mergeItemStack(item, 0, 6, false);
		else container.mergeItemStack(item, 6, 24, false);
		return true;
	}

	@Override
	protected void customPlayerCommand(byte cmd, PacketBuffer dis, EntityPlayerMP player) throws IOException 
	{
		if (cmd == 0) active = !active;
		else if (cmd == 1) reset();
	}

	private static class MineTask {
		public final int x, y, z;
		public final Object src;
		public final boolean evType;
		public MineTask(Object computer, int x, int y, int z, boolean event) {
			this.x = x;
			this.y = y;
			this.z = z;
			this.src = computer;
			this.evType = event;
		}
		
		public boolean execute(Miner miner)
		{
			BlockPos pos = new BlockPos(x, y, z);
			if (!miner.worldObj.isBlockLoaded(pos)) return false;
			Block block = miner.worldObj.getBlockState(pos).getBlock();
			boolean imp = block == null || block.isReplaceable(miner.worldObj, pos);
			if (imp || miner.breakBlock(pos)) {
				if (evType) ComputerAPI.sendEvent(src, "mine_block", x - miner.px, y - miner.py, z - miner.pz, !imp);
				else if (miner.sheduledTasks.size() == 1) ComputerAPI.sendEvent(src, "mine_done");
				return true;
			} else return false;
		}
	}

	@Override
	public IItemHandler getUpgradeSlots() {
		return inventory.new SlotAccess(24, 5);
	}

	@Override
	public int[] getBaseDimensions() 
	{
		return new int[]{16, 64, 16, 8, Config.Umax[1]};
	}

	@Override
	public void setSlot(int g, int s, ItemStack item) {
		inventory.items[s] = item;
		if (s == 24) this.markUpdate();
		else if (s == 27) {
			float u = energy.Ucap;
			energy = new PipeEnergy(Handler.Umax(this), Config.Rcond[1]);
			energy.Ucap = u;
		} else if (s == 28) {
			slave = ItemMachineSynchronizer.getLink(inventory.items[28], this);
		} else if (s == 25 || s == 26) {
			Handler.setCorrectArea(this, area, true);
		}
	}

	@Override
	public boolean remoteOperation(BlockPos pos) 
	{
		if (pos.getX() < area[0] || pos.getY() < area[1] || pos.getZ() < area[2] || pos.getX() >= area[3] || pos.getY() >= area[4] || pos.getZ() >= area[5]) return true;
		return this.breakBlock(pos);
	}

	@Override
	public IOperatingArea getSlave() 
	{
		return slave;
	}
	
	private ArrayDeque<MineTask> sheduledTasks = new ArrayDeque<MineTask>(Config.taskQueueSize);

	//OpenComputers:
	
	private Object node = ComputerAPI.newOCnode(this, "Automation-Miner", true);
	
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
	@Callback(doc = "function():bool --returns true if a user initiated mining process is running, in this case computer controlled mining is not allowed", direct = true)
	public Object[] isMining(Context cont, Arguments args)
	{
		return new Object[]{active};
	}
	
	@Optional.Method(modid = "OpenComputers")
	@Callback(doc = "function():int --returns the amount of sheduled unfinished block break tasks and cancels them all", direct = true)
	public Object[] clearQueue(Context cont, Arguments args)
	{
		int n;
		synchronized(this.sheduledTasks) {
			n = this.sheduledTasks.size();
			this.sheduledTasks.clear();
		}
		return new Object[]{n};
	}
	
	@Optional.Method(modid = "OpenComputers")
	@Callback(doc = "function(x:int, y:int, z:int):bool --returns true if there is a not replaceable block at given operating-area-relative position", direct = true)
	public Object[] isBlockAt(Context cont, Arguments args)
	{
		BlockPos pos = new BlockPos(args.checkInteger(0) + area[0], args.checkInteger(1) + area[1], args.checkInteger(2) + area[2]);
		Block block = worldObj.getBlockState(pos).getBlock();
		if (block != null && !block.isReplaceable(worldObj, pos)) return new Object[]{Boolean.TRUE};
		else return new Object[]{Boolean.FALSE};
	}
	
	@Optional.Method(modid = "OpenComputers")
	@Callback(doc = "function(x:int, y:int, z:int[, notify:bool]):bool --will add the block at given position to the processing queue or returns false if queue is full. if notify is given, the caller will receive a \"mine_block\"{x:int, y:int, z:int, success:bool} event when executed with true or a \"mine_done\" event when processing queue finished with false", direct = true)
	public Object[] mineBlock(Context cont, Arguments args) throws Exception
	{
		if (active) throw new Exception("Manual mining operation in progress, must be stopped first!");
		int x = args.checkInteger(0) + area[0];
		int y = args.checkInteger(1) + area[1];
		int z = args.checkInteger(2) + area[2];
		if (x < area[0] || x >= area[3] || y < area[1] || y >= area[4] || z < area[2] || z >= area[5]) throw new Exception("Position outside of operating area!");
		if (this.sheduledTasks.size() < Config.taskQueueSize) {
			synchronized (this.sheduledTasks) {
				this.sheduledTasks.add(new MineTask(args.isBoolean(3) ? cont : null, x, y, z, args.checkBoolean(3)));
			}
			return new Object[]{Boolean.TRUE};
		} else return new Object[]{Boolean.FALSE};
	}

}
