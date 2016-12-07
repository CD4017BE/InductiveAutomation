package cd4017be.automation.TileEntity;

import net.minecraft.network.PacketBuffer;

import java.util.ArrayList;
import java.util.UUID;

import net.minecraftforge.fml.common.Optional;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.Environment;
import li.cil.oc.api.network.Message;
import li.cil.oc.api.network.Node;

import com.mojang.authlib.GameProfile;

import cd4017be.api.automation.IOperatingArea;
import cd4017be.api.automation.PipeEnergy;
import cd4017be.api.circuits.IQuickRedstoneHandler;
import cd4017be.api.computers.ComputerAPI;
import cd4017be.automation.Config;
import cd4017be.automation.Objects;
import cd4017be.automation.Gui.InventoryPlacement;
import cd4017be.automation.Item.ItemBuilderTexture;
import cd4017be.automation.Item.ItemFluidDummy;
import cd4017be.automation.Item.ItemMachineSynchronizer;
import cd4017be.automation.Item.ItemPlacement;
import cd4017be.lib.Gui.DataContainer;
import cd4017be.lib.Gui.DataContainer.IGuiData;
import cd4017be.lib.Gui.TileContainer;
import cd4017be.lib.Gui.TileContainer.ISlotClickHandler;
import cd4017be.lib.templates.AutomatedTile;
import cd4017be.lib.templates.Inventory;
import cd4017be.lib.templates.Inventory.IAccessHandler;
import cd4017be.lib.util.CachedChunkProtection;
import cd4017be.lib.util.ItemFluidUtil;
import cd4017be.lib.util.ItemFluidUtil.StackedFluidAccess;
import cd4017be.lib.util.Utils;
import cd4017be.lib.util.Vec2;
import cd4017be.lib.util.Vec3;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.SlotItemHandler;

/**
 *
 * @author CD4017BE
 */
@Optional.Interface(iface = "li.cil.oc.api.network.Environment", modid = "OpenComputers")
public class Builder extends AutomatedTile implements IOperatingArea, Environment, IGuiData, IAccessHandler, ISlotClickHandler, IQuickRedstoneHandler {

	public static class VectorConstruction 
	{
		
		public Polygon[] polygons;
		
		public void reset(Builder builder)
		{
			builder.px = 0;
			builder.py = 0;
			builder.pz = 0;
			this.setTexture(builder);
		}
		
		public void setTexture(Builder builder)
		{
			if (builder.py >= polygons.length) builder.tmpData = builder.new TextureData(null);
			else builder.tmpData = builder.new TextureData(builder.inventory.items[(polygons[builder.py].texId & 0x7) + 9]);
		}
		
		public boolean processBuilding(Builder builder)
		{
			int[] area = builder.getOperatingArea();
			if (builder.py >= polygons.length) {
				builder.nextStep();
				return false;
			}
			Polygon pol = polygons[builder.py];
			int[] size = pol.getSize(area);
			int[] block = size[0] * size[1] == 0 ? null : pol.getBlock(builder.px % size[0], builder.px / size[0], builder.pz);
			if (block == null) {
				builder.pz = pol.thick;
			} else if (block[0] >= 0 && block[1] >= 0 && block[2] >= 0 && (block[0] += area[0]) < area[3] && (block[1] += area[1]) < area[4] && (block[2] += area[2]) < area[5] && 
					!builder.setBlock(new BlockPos(block[0], block[1], block[2]), builder.tmpData.get(block[3], block[4]))) return false;
			
			if (++builder.pz >= pol.thick) {
				builder.pz = 0;
				if (++builder.px >= size[0] * size[1]) {
					builder.px = 0;
					builder.py++;
					this.setTexture(builder);
				}
			}
			return true;
		}
		
		public static VectorConstruction read(NBTTagCompound nbt)
		{
			VectorConstruction constr = new VectorConstruction();
			NBTTagList list = nbt.getTagList("pol", 10);
			constr.polygons = new Polygon[list.tagCount()];
			for (int i = 0; i < constr.polygons.length; i++)
				constr.polygons[i] = new Polygon(list.getCompoundTagAt(i));
			return constr;
		}
		
		public static class Polygon {
			
			public Polygon(NBTTagCompound nbt)
			{
				this(	Vec3.Def(nbt.getFloat("v00"), nbt.getFloat("v01"), nbt.getFloat("v02")),
							Vec2.Def(nbt.getFloat("v03"), nbt.getFloat("v04")),
						Vec3.Def(nbt.getFloat("v10"), nbt.getFloat("v11"), nbt.getFloat("v12")),
							Vec2.Def(nbt.getFloat("v13"), nbt.getFloat("v14")),
						Vec3.Def(nbt.getFloat("v20"), nbt.getFloat("v21"), nbt.getFloat("v22")),
							Vec2.Def(nbt.getFloat("v23"), nbt.getFloat("v24")),
						nbt.getByte("dir"), nbt.getShort("th"), nbt.getByte("tex"));
			}
			
			public NBTTagCompound save()
			{
				NBTTagCompound tag = new NBTTagCompound();
				tag.setByte("vn", (byte)3);
				tag.setFloat("v00", (float)vert[0].x);
				tag.setFloat("v01", (float)vert[0].y);
				tag.setFloat("v02", (float)vert[0].z);
				tag.setFloat("v10", (float)(vert[1].x + vert[0].x));
				tag.setFloat("v11", (float)(vert[1].y + vert[0].y));
				tag.setFloat("v12", (float)(vert[1].z + vert[0].z));
				tag.setFloat("v20", (float)(vert[2].x + vert[0].x));
				tag.setFloat("v21", (float)(vert[2].y + vert[0].y));
				tag.setFloat("v22", (float)(vert[2].z + vert[0].z));
				tag.setFloat("v03", (float)texP[0].x);
				tag.setFloat("v04", (float)texP[0].z);
				tag.setFloat("v13", (float)(texP[1].x + texP[0].x));
				tag.setFloat("v14", (float)(texP[1].z + texP[0].z));
				tag.setFloat("v23", (float)(texP[2].x + texP[0].x));
				tag.setFloat("v24", (float)(texP[2].z + texP[0].z));
				tag.setByte("dir", dir);
				tag.setShort("th", thick);
				tag.setShort("tex", texId);
				return tag;
			}
			
			public Polygon(Vec3 p0, Vec2 t0, Vec3 p1, Vec2 t1, Vec3 p2, Vec2 t2, byte dir, int thick, int texId) 
			{
				this.vert = new Vec3[]{p0, p1.diff(p0), p2.diff(p0)};
				this.texP = new Vec2[]{t0, t1.diff(t0), t2.diff(t0)};
				boolean inv = thick < 0;
				this.dir = inv ? (byte)(dir^1) : dir;
				this.thick = (short)(inv ? -thick : thick);
				this.texId = (byte)texId;
			}
			
			public final Vec3[] vert;
			public final Vec2[] texP;
			public short thick;
			public byte texId;
			public byte dir;
			
			public int[] getSize(int[] area)
			{
				if (dir == 0 || dir == 1) return new int[]{area[3] - area[0], area[5] - area[2]};
				if (dir == 2 || dir == 3) return new int[]{area[3] - area[0], area[4] - area[1]};
				return new int[]{area[5] - area[2], area[4] - area[1]};
			}
			
			public int[] getBlock(int i, int j, int k)
			{
				int[] u = new int[5];
				double p = (double)i + 0.5D, q = (double)j + 0.5D, r = ((double)k + 0.5D);
				double a, b, c, d;
				if ((dir & 1) == 0) r = -r;
				if (dir == 0 || dir == 1) {
					p -= vert[0].x; q -= vert[0].z; r += vert[0].y;
					c = vert[1].x * vert[2].z - vert[1].z * vert[2].x;
					a = (vert[2].z * p - vert[2].x * q) / c;
					b = (vert[1].x * q - vert[1].z * p) / c;
					d = a * vert[1].y + b * vert[2].y + r;
					u[0] = i; u[2] = j; u[1] = (int)Math.floor(d);
				} else if (dir == 2 || dir == 3) {
					p -= vert[0].x; q -= vert[0].y; r += vert[0].z;
					c = vert[1].x * vert[2].y - vert[1].y * vert[2].x;
					a = (vert[2].y * p - vert[2].x * q) / c;
					b = (vert[1].x * q - vert[1].y * p) / c;
					d = a * vert[1].z + b * vert[2].z + r;
					u[0] = i; u[1] = j; u[2] = (int)Math.floor(d);
				} else {
					p -= vert[0].z; q -= vert[0].y; r += vert[0].x;
					c = vert[1].z * vert[2].y - vert[1].y * vert[2].z;
					a = (vert[2].y * p - vert[2].z * q) / c;
					b = (vert[1].z * q - vert[1].y * p) / c;
					d = a * vert[1].x + b * vert[2].x + r;
					u[2] = i; u[1] = j; u[0] = (int)Math.floor(d);
				}
				if (a < 0D || b < 0D || a + b > 1D) return null;
				Vec2 t = texP[0].add(texP[1].scale(a)).add(texP[2].scale(b));
				u[3] = (int)Math.floor(t.x); u[4] = (int)Math.floor(t.z);
				return u;
			}
			
		}

	}
	
	private class TextureData {
		final byte[] data;
		final byte width, height;
		final byte ofsX, ofsY;
		final ItemStack[] palette;
		final byte expMode;
		
		public TextureData(ItemStack item) {
			if (item != null && (item.getItem() instanceof ItemBlock || item.getItem() instanceof ItemPlacement || item.getItem() instanceof ItemFluidDummy || item.getItem() == Objects.remBlockType)) {
				data = new byte[1];
				width = 1; height = 1;
				ofsX = 0; ofsY = 0;
				palette = new ItemStack[]{item.copy()};
				data[0] = 1;
				expMode = 1;
			} else if (item != null && item.getItem() instanceof ItemBuilderTexture && item.getTagCompound() != null) {
				data = item.getTagCompound().getByteArray("data");
				width = item.getTagCompound().getByte("width");
				height = (byte)(width == 0 ? 0 : data.length / width);
				ofsX = item.getTagCompound().getByte("ofsX");
				ofsY = item.getTagCompound().getByte("ofsY");
				ItemFluidUtil.loadItems(item.getTagCompound().getTagList("def", 10), palette = new ItemStack[16]);
				expMode = item.getTagCompound().getByte("mode");
			} else {
				data = new byte[0];
				width = 0; height = 0;
				ofsX = 0; ofsY = 0;
				palette = new ItemStack[0];
				expMode = 0;
			}
		}
		
		public boolean skip()
		{
			return data.length == 0 || palette.length == 0;
		}
		
		public ItemStack get(int x, int y) {
			if (this.skip()) return null;
			else if (palette.length < 16) return palette[0];
			x -= ofsX; y -= ofsY;
			if (x < 0 || y < 0 || x >= width || y >= height) {
				if (expMode == 1) {
					if (x < 0) x = 0;
					else if (x >= width) x = width - 1;
					if (y < 0) y = 0;
					else if (y >= height) y = height - 1;
				} else if (expMode == 2) {
					x = Utils.mod(x, width);
					y = Utils.mod(y, height);
				} else if (expMode == 3) {
					if ((Utils.div(x, width) & 1) != 0) x = -1 - x;
					if ((Utils.div(y, height) & 1) != 0) y = -1 - y;
					x = Utils.mod(x, width);
					y = Utils.mod(y, height);
				} else return null;
			}
			int idx = data[x + (height - y - 1) * width] & 0xf;
			if (idx == 0) return null;
			else return palette[idx];
		}
	}
	
	public static float Energy = 25000F;
	public static final float resistor = 25F;
	private static final GameProfile defaultUser = new GameProfile(new UUID(0, 0), "#Builder");
	private static final float eScale = (float)Math.sqrt(1D - 1D / resistor);
	private static final short S_Offline = 0;
	private static final short S_FrameY = 1;
	private static final short S_FrameZ = 2;
	private static final short S_FrameX = 3;
	private static final short S_WallB = 4;
	private static final short S_WallT = 5;
	private static final short S_WallN = 6;
	private static final short S_WallS = 7;
	private static final short S_WallW = 8;
	private static final short S_WallE = 9;
	private static final short S_Stack = 10;
	private static final byte D_XZ = 0;
	private static final byte D_XY = 1;
	private static final byte D_ZY = 2;
	private static final byte D_Y = 3;
	private static final byte D_Z = 4;
	private static final byte D_X = 5;
	private int px;
	private int py;
	private int pz;
	private int stackIterSum;
	private int[] area = new int[6];
	private float storage;
	private IOperatingArea slave = null;
	private TextureData tmpData;
	private VectorConstruction Vconst;
	private CachedChunkProtection prot;
	public int[] thick = new int[10];

	private GameProfile lastUser = defaultUser;

	public Builder()
	{
		inventory = new Inventory(50, 1, this).group(0, 17, 44, Utils.ACC);
		energy = new PipeEnergy(Config.Umax[1], Config.Rcond[1]);
	}
	
	@Override
	public void onPlaced(EntityLivingBase entity, ItemStack item)  
	{
		lastUser = entity instanceof EntityPlayer ? ((EntityPlayer)entity).getGameProfile() : new GameProfile(new UUID(0, 0), entity.getName());
		prot = null;
	}
	
	@Override
	public boolean onActivated(EntityPlayer player, EnumHand hand, ItemStack item, EnumFacing s, float X, float Y, float Z) 
	{
		lastUser = player.getGameProfile();
		prot = null;
		return super.onActivated(player, hand, item, s, X, Y, Z);
	}
	
	@Override
	public void update() 
	{
		super.update();
		if (worldObj.isRemote) return;
		if (slave == null || ((TileEntity)slave).isInvalid()) slave = ItemMachineSynchronizer.getLink(inventory.items[48], this);
		if (storage < Energy) {
			storage += energy.getEnergy(0, resistor);
			energy.Ucap *= eScale;
		}
		storage -= ComputerAPI.update(this, node, storage * 0.5D);
		for (int i = 0; i < Config.taskQueueSize; i++) 
			if (!build()) break;
	}
	
	private boolean build()
	{
		switch (thick[8]) {
		case S_Offline:
			synchronized(this.sheduledTasks) {
				if (!this.sheduledTasks.isEmpty() && this.sheduledTasks.get(0).execute(this)) {
					this.sheduledTasks.remove(0);
					return true;
				} else return false;
			}
		case S_FrameY:
			if (placeBlock(D_Y)) {
				py++;
				if (py >= area[4]) {
					py = area[1];
					if (px <= area[0]) px = area[3] - 1;
					else if (pz <= area[2]) {pz = area[5] - 1; px = area[0];}
					else nextStep();
				}
				return true;
			} else return false;
		case S_FrameZ:
			if (placeBlock(D_Z)) {
				pz++;
				if (pz >= area[5]) {
					pz = area[2];
					if (px <= area[0]) px = area[3] - 1;
					else if (py <= area[1]) {py = area[4] - 1; px = area[0];}
					else nextStep();
				}
				return true;
			} else return false;
		case S_FrameX:
			if (placeBlock(D_X)) {
				px++;
				if (px >= area[3]) {
					px = area[0];
					if (py <= area[1]) py = area[4] - 1;
					else if (pz <= area[2]) {pz = area[5] - 1; py = area[1];}
					else nextStep();
				}
				return true;
			} else return false;
		case S_WallB:
		case S_WallT:
			if (placeBlock(D_XZ)) {
				px++;
				if (px >= area[3]) {
					px = area[0];
					pz++;
				}
				if (pz >= area[5]) nextStep();
				return true;
			} else return false;
		case S_WallN:
		case S_WallS:
			if (placeBlock(D_XY)) {
				px++;
				if (px >= area[3]) {
					px = area[0];
					py++;
				}
				if (py >= area[4]) nextStep();
				return true;
			} else return false;
		case S_WallW:
		case S_WallE:
			if (placeBlock(D_ZY)) {
				pz++;
				if (pz >= area[5]) {
					pz = area[2];
					py++;
				}
				if (py >= area[4]) nextStep();
				return true;
			} else return false;
		case S_Stack:
			if (Vconst != null) return Vconst.processBuilding(this);
			if (px >= area[3] || py >= area[4] || pz >= area[5]) {
				nextStep();
				return true;
			}
			if (tmpData == null) this.setupStackData();
			if (tmpData == null) {
				nextStep();
				return true;
			}
			if (tmpData.skip()) {
				if (thick[9] == D_XY) pz++;
				else if (thick[9] == D_XZ) py++;
				else if (thick[9] == D_ZY) px++;
				tmpData = null;
				return true;
			}
			if (placeBlock(thick[9])) {
				boolean e = true;
				if (thick[9] != D_ZY) { 
					if (++px >= area[3]) {
						px = area[0];
						e = false;
					}
				} else if (++pz >= area[5]) {
						pz = area[2];
						e = false;
					}
				if (e) return true;
				e = true;
				if (thick[9] == D_XZ) {
					if (++pz >= area[5]) {
						pz = area[2];
						e = false;
					}
				} else if (++py >= area[4]) {
						py = area[1];
						e = false;
					}
				if (e) return true;
				if (thick[9] == D_XY) {
					pz++;
				} else if (thick[9] == D_XZ) {
					py++;
				} else {
					px++;
				}
				tmpData = null;
				return true;
			} else return false;
		}
		return false;
	}
	
	private boolean placeBlock(int dir)
	{
		if (tmpData == null || tmpData.skip()) {
			nextStep();
			return false;
		}
		if (!worldObj.isBlockLoaded(new BlockPos(px, py, pz))) return false;
		int i = 0, j = 0;
		if (dir == D_Y) i = py - area[1];
		else if (dir == D_Z) i = pz - area[2];
		else if (dir == D_X) i = px - area[0];
		else if (dir == D_XZ) {i = px - area[0]; j = pz - area[2];}
		else if (dir == D_XY) {i = px - area[0]; j = py - area[1];}
		else if (dir == D_ZY) {i = pz - area[2]; j = py - area[1];}
		return this.setBlock(new BlockPos(px, py, pz), tmpData.get(i, j));
	}
	
	private boolean setBlock(BlockPos pos, ItemStack item)
	{
		if (item == null) return true;
		if (prot == null || !prot.equalPos(pos)) prot = CachedChunkProtection.get(lastUser, worldObj, pos);
		if (!prot.allow) return true;
		if (slave != null && comp == 0 && !((TileEntity)slave).isInvalid() && (slave instanceof Miner || slave instanceof Pump) && 
				(slave.getSlave() == null || !(slave.getSlave() instanceof Builder || slave.getSlave().getSlave() != null))) {
			if (!slave.remoteOperation(pos)) return false;
		}
		Block id = null;
		int m = 0;
		if (item.getItem() == Objects.remBlockType) return true;
		if (item.getItem() instanceof ItemBlock) {
			ItemBlock block = (ItemBlock)item.getItem();
			id = block.block;
			m = block.getMetadata(item.getItemDamage());
		} else if (item.getItem() instanceof ItemFluidDummy) {
			FluidStack fluid = ItemFluidDummy.getFluid(item);
			if (fluid != null) id = fluid.getFluid().getBlock();
		} else if (item.getItem() instanceof ItemPlacement) {
			if (comp == 0) {
				Block b = worldObj.getBlockState(pos).getBlock();
				if (b != null && !b.isReplaceable(worldObj, pos)) return true;
			}
			InventoryPlacement inv = new InventoryPlacement(item);
			ItemStack stack;
			if (comp < 0 || comp >= inv.inventory.length || inv.inventory[comp] == null) {
				comp = 0;
				return true;
			}
			if (storage >= Energy && (stack = remove(inv.inventory[comp], inv.useDamage(comp))) != null) {
				EntityPlayer player = FakePlayerFactory.get((WorldServer)worldObj, lastUser);
				stack = inv.doPlacement(worldObj, player, pos, comp, stack);
				storage -= Energy;
				if (stack != null)
					this.dropStack(ItemHandlerHelper.insertItemStacked(inventory.new Access(0), stack, false));
				if (++comp >= inv.inventory.length || inv.inventory[comp] == null){
					comp = 0;
					return true;
				}
			}
			return false;
		}
		comp = 0;
		if (id == null) return true;
		Block b = worldObj.getBlockState(pos).getBlock();
		if (!b.isReplaceable(worldObj, pos)) return true;
		if (storage >= Energy && remove(item, true) != null)
		{
			worldObj.setBlockState(pos, id.getStateFromMeta(m), 0x3);
			storage -= Energy;
			return true;
		} else
		return false;
	}
	
	private byte comp;
	
	private void nextStep()
	{
		if (++thick[8] > 10) thick[8] = 0;
		this.setupData();
		if (thick[8] == S_Stack && Vconst != null) {
			Vconst.reset(this);
			return;
		}
		px = area[0];
		py = area[1];
		pz = area[2];
		switch(thick[8]) {
		case S_WallT:
			py = area[4] - 1;
			break;
		case S_WallS:
			pz = area[5] - 1;
			break;
		case S_WallE:
			px = area[3] - 1;
			break;
		}
	}
	
	private void setupData()
	{
		if (thick[8] == S_Stack && inventory.items[49] != null && inventory.items[49].getTagCompound() != null) {
			Vconst = VectorConstruction.read(inventory.items[49].getTagCompound());
			Vconst.setTexture(this);
		} else if (thick[8] == S_Offline || thick[8] == S_Stack) {
			tmpData = null;
			Vconst = null;
		} else tmpData = new TextureData(inventory.items[thick[8] - 1]);
	}
	
	private void setupStackData()
	{
		if (stackIterSum <= 0) return;
		int n = thick[9] == D_XZ ? py - area[1] : thick[9] == D_XY ? pz - area[2] : px - area[0];
		n %= stackIterSum;
		int m = -1;
		while (n >= 0 && ++m < thick.length) n -= thick[m];
		if (m >= thick.length) m = 0;
		tmpData = new TextureData(inventory.items[9 + m]);
	}
	
	private ItemStack remove(ItemStack item, boolean meta)
	{
		if (item.getItem() instanceof ItemFluidDummy) {
			FluidStack fluid = ItemFluidDummy.getFluid(item);
			for (int i = 17; i < 44; i++) {
				StackedFluidAccess acc = new StackedFluidAccess(inventory.items[i]);
				if (acc.valid() && fluid.isFluidStackIdentical(acc.drain(fluid, false))) {
					acc.drain(fluid, true);
					inventory.items[i] = acc.result();
					return item.copy();
				}
			}
			return null;
		} else {
			for (int i = 17; i < 44; i++) {
				if (inventory.items[i] != null && inventory.items[i].getItem() == item.getItem() && (!meta || inventory.items[i].getItemDamage() == item.getItemDamage()))
					return inventory.extractItem(i, 1, false);
			}
			return null;
		}
	}

	@Override
	protected void customPlayerCommand(byte cmd, PacketBuffer dis, EntityPlayerMP player) {
		if (cmd >= thick.length) return;
		if (cmd == 8) {
			if (thick[8] != 0) {
				thick[8] = 0;
				this.setupData();
			} else {
				thick[8] = 0;
				nextStep();
			}
		} else thick[cmd] = dis.readShort();
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
			thick[8] = 0;
		}
		this.markUpdate();
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) 
	{
		super.readFromNBT(nbt);
		if (node != null) ComputerAPI.readNode(node, nbt);
		area = nbt.getIntArray("area");
		if (area.length != 6) area = new int[6];
		px = nbt.getInteger("px");
		py = nbt.getInteger("py");
		pz = nbt.getInteger("pz");
		comp = nbt.getByte("part");
		storage = nbt.getFloat("storage");
		thick[9] = nbt.getByte("thick[9]");
		byte[] buff = nbt.getByteArray("thick");
		stackIterSum = 0;
		if (buff.length == 8) 
			for (int i = 0; i < buff.length; i++) 
				stackIterSum += thick[i] = buff[i] & 0xff;
		thick[8] = nbt.getShort("step");
		setupData();
		try {lastUser = new GameProfile(new UUID(nbt.getLong("lastUserID0"), nbt.getLong("lastUserID1")), nbt.getString("lastUser"));
		} catch (Exception e) {lastUser = defaultUser;}
		prot = null;
		this.setSlot(-1, 47, inventory.items[47]);
		lastRedstone = nbt.getInteger("lRS");
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
		nbt.setByte("thick[9]", (byte)thick[9]);
		byte[] buff = new byte[8];
		for (int i = 0; i < buff.length; i++) buff[i] = (byte)thick[i];
		nbt.setByteArray("thick", buff);
		nbt.setShort("step", (short)thick[8]);
		nbt.setString("lastUser", lastUser.getName());
		nbt.setLong("lastUserID0", lastUser.getId().getMostSignificantBits());
		nbt.setLong("lastUserID1", lastUser.getId().getLeastSignificantBits());
		nbt.setInteger("lRS", lastRedstone);
		return super.writeToNBT(nbt);
	}
	
	@Override
	public void initContainer(DataContainer cont) {
		TileContainer container = (TileContainer)cont;
		container.clickHandler = this;
		for (int i = 0; i < 9; i++)
			container.addItemSlot(new SlotItemHandler(inventory, i, 8 + i * 18, 34));
		for (int i = 0; i < 8; i++)
			container.addItemSlot(new SlotItemHandler(inventory, 9 + i, 26 + i * 18, 52));
		for (int i = 0; i < 3; i++)
			for (int j = 0; j < 9; j++)
				container.addItemSlot(new SlotItemHandler(inventory, 17 + i * 9 + j, 8 + j * 18, 88 + i * 18));
		//container.addItemSlot(new SlotItemType(inventory, 49, 152, 52, new ItemStack(Items.PAPER)));
		container.addPlayerInventory(8, 158);
	}

	@Override
	public boolean transferStack(ItemStack item, int s, TileContainer container) {
		if (s < container.invPlayerS) container.mergeItemStack(item, container.invPlayerS, container.invPlayerE, false);
		else container.mergeItemStack(item, 17, 44, false);
		return true;
	}

	@Override
	public IItemHandler getUpgradeSlots() {
		return inventory.new SlotAccess(44, 5);
	}

	@Override
	public int[] getBaseDimensions() 
	{
		return new int[]{16, 16, 16, 8, Config.Umax[1]};
	}

	@Override
	public void setSlot(int g, int s, ItemStack item) {
		inventory.items[s] = item;
		if (s == 44) this.markUpdate();
		else if (s == 47) {
			float u = energy.Ucap;
			energy = new PipeEnergy(Handler.Umax(this), Config.Rcond[1]);
			energy.Ucap = u;
		} else if (s == 48) {
			slave = ItemMachineSynchronizer.getLink(item, this);
		} else if (s == 45 || s == 46){
			Handler.setCorrectArea(this, area, true);
		}
	}

	@Override
	public boolean remoteOperation(BlockPos pos) 
	{
		if (pos.getX() < area[0] || pos.getY() < area[1] || pos.getZ() < area[2] || pos.getX() >= area[3] || pos.getY() >= area[4] || pos.getZ() >= area[5]) return true;
		return this.setBlock(pos, inventory.items[0]);
	}

	@Override
	public IOperatingArea getSlave() 
	{
		return slave;
	}

	@Override
	public int[] getSyncVariables() {
		return thick;
	}

	@Override
	public void setSyncVariable(int i, int v) {
		thick[i] = v;
	}

	//---------------- Computer APIs --------------------

	private static class BuildTask {
		public final int x, y, z, s;
		public final Object src;
		public final boolean evType;
		public BuildTask(Object computer, int x, int y, int z, int s, boolean event) {
			this.x = x;
			this.y = y;
			this.z = z;
			this.s = s;
			this.src = computer;
			this.evType = event;
		}
		
		public boolean execute(Builder builder)
		{
			BlockPos pos = new BlockPos(x, y, z);
			if (!builder.worldObj.isBlockLoaded(pos)) return false;
			Block block = builder.worldObj.getBlockState(pos).getBlock();
			boolean can = block == null || block.isReplaceable(builder.worldObj, pos);
			if (builder.setBlock(pos, builder.inventory.items[s])) {
				if (evType) ComputerAPI.sendEvent(src, "set_block", x - builder.area[0], y - builder.area[1], z - builder.area[2], can);
				else if (builder.sheduledTasks.size() == 1) ComputerAPI.sendEvent(src, "set_done");
				return true;
			} else return false;
		}
	}
	
	ArrayList<BuildTask> sheduledTasks = new ArrayList<BuildTask>();

	//Redstone commands: 0xTTZZYYXX

	private int lastRedstone = 0;

	@Override
	public void onRedstoneStateChange(EnumFacing side, int value, TileEntity src) {
		if (value != lastRedstone && thick[8] == S_Offline) {
			lastRedstone = value;
			int x = area[0] + (value & 0xff);
			int y = area[1] + (value >> 8 & 0xff);
			int z = area[2] + (value >> 16 & 0xff);
			int t = (value >> 26 & 0x1f) - 1;
			if (sheduledTasks.size() < Config.taskQueueSize * 4 && x < area[3] && y < area[4] && z < area[5] && t >= 0 && t < 16)
				sheduledTasks.add(new BuildTask(null, x, y, z, t, false));
		}
	}

	@Override
	public void onNeighborBlockChange(Block b) {
		for (EnumFacing s : EnumFacing.VALUES) {
			int rs = worldObj.getRedstonePower(pos.offset(s), s);
			if ((rs & 0x7c000000) != 0) {
				this.onRedstoneStateChange(s, rs, null);
				break;
			}
		}
	}

	//OpenComputers:
	
	private Object node = ComputerAPI.newOCnode(this, "Automation-Builder", true);
	
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
	@Callback(doc = "function():bool --returns true if a user initiated building process is running, in this case computer controlled building is not allowed", direct = true)
	public Object[] isBuilding(Context cont, Arguments args)
	{
		return new Object[]{thick[8] != S_Offline};
	}
	
	@Optional.Method(modid = "OpenComputers")
	@Callback(doc = "function():int --returns the amount of sheduled unfinished block placement tasks and cancels them all", direct = true)
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
		BlockPos pos = new BlockPos(args.checkInteger(0), args.checkInteger(1), args.checkInteger(2)).add(area[0], area[1], area[2]);
		Block block = worldObj.getBlockState(pos).getBlock();
		if (block != null && !block.isReplaceable(worldObj, pos)) return new Object[]{Boolean.TRUE};
		else return new Object[]{Boolean.FALSE};
	}
	
	@Optional.Method(modid = "OpenComputers")
	@Callback(doc = "function(x:int, y:int, z:int, material:int[, notify:bool]):bool --will add the placement of the material in given slot at given position to the processing queue or returns false if queue is full. if notify is given, the caller will receive a \"set_block\"{x:int, y:int, z:int, success:bool} event when executed on true or a \"set_done\" event when processing queue finished on false", direct = true)
	public Object[] setBlock(Context cont, Arguments args) throws Exception
	{
		if (thick[8] != S_Offline) throw new Exception("Manual building operation in progress, must be stopped first!");
		int x = args.checkInteger(0) + area[0];
		int y = args.checkInteger(1) + area[1];
		int z = args.checkInteger(2) + area[2];
		int b = args.checkInteger(3);
		if (b < 0 || b > 16) throw new Exception("Slot index out of range, allowed: 0-2 = frame slots, 3-8 = wall slots, 9-16 = fill slots");
		if (x < area[0] || x >= area[3] || y < area[1] || y >= area[4] || z < area[2] || z >= area[5]) throw new Exception("Position outside of operating area!");
		if (this.sheduledTasks.size() < Config.taskQueueSize) {
			synchronized (this.sheduledTasks) {
				this.sheduledTasks.add(new BuildTask(args.isBoolean(4) ? cont : null, x, y, z, b, args.checkBoolean(4)));
			}
			return new Object[]{Boolean.TRUE};
		} else return new Object[]{Boolean.FALSE};
	}

}
