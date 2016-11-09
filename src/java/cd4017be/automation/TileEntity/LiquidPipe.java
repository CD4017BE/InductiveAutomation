package cd4017be.automation.TileEntity;

import java.util.ArrayList;

import cd4017be.api.automation.IFluidPipeCon;
import cd4017be.automation.Config;
import cd4017be.automation.Objects;
import cd4017be.automation.Block.BlockLiquidPipe;
import cd4017be.automation.Item.ItemFluidUpgrade;
import cd4017be.automation.Item.PipeUpgradeFluid;
import cd4017be.lib.templates.AutomatedTile;
import cd4017be.lib.templates.IPipe;
import cd4017be.lib.templates.TankContainer;
import cd4017be.lib.util.Utils;
import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

/**
 *
 * @author CD4017BE
 */
public class LiquidPipe extends AutomatedTile implements IPipe {
	/** bits[0-13 (6+1)*2]: (side + total) * dir{0:none, 1:in, 2:out, 3:lock/both} */
	private short flow;
	private boolean updateCon = true;
	private PipeUpgradeFluid filter = null;
	private Cover cover = null;

	public LiquidPipe() {
		tanks = new TankContainer(1, 1).tank(0, Config.tankCap[0], Utils.ACC, -1, -1);
		tanks.sideCfg = 0x030303030303L;
	}

	@Override
	public void update() {
		if (worldObj.isRemote) return;
		int type = this.getBlockMetadata();
		if (updateCon) this.updateConnections(type);
		if ((flow & 0x3000) == 0x3000) this.transferFluid(type);
	}

	private void updateConnections(int type) {
		EnumFacing dir;
		TileEntity te;
		ArrayList<LiquidPipe> updateList = new ArrayList<LiquidPipe>();
		int lHasIO = getFlowBit(6), nHasIO = 0, lDirIO, nDirIO;
		short lFlow = flow;
		for (int i = 0; i < 6; i++) {
			lDirIO = getFlowBit(i);
			if (lDirIO == 3) continue;
			dir = EnumFacing.VALUES[i];
			te = worldObj.getTileEntity(pos.offset(dir));
			if (te != null && te instanceof LiquidPipe) {
				LiquidPipe pipe = (LiquidPipe)te;
				int pHasIO = pipe.getFlowBit(6);
				int pDirIO = pipe.getFlowBit(i ^ 1);
				if (pDirIO == 3) nDirIO = 3;
				else if ((nDirIO = pHasIO & ~pDirIO) == 3) 
					nDirIO = lHasIO == 1 && (lDirIO & 1) == 0 ? 2 : lHasIO == 2 && (lDirIO & 2) == 0 ? 1 : 0;
				setFlowBit(i, nDirIO);
				if (nDirIO != 3) nHasIO |= nDirIO;
				updateList.add(pipe);
			} else if (te != null && te instanceof IFluidPipeCon) {
				byte d = ((IFluidPipeCon)te).getFluidConnectType(i^1);
				d = d == 1 ? 2 : d == 2 ? 1 : (byte)0;
				setFlowBit(i, d);
				nHasIO |= d;
			} else if (type != BlockLiquidPipe.ID_Transport && te != null && te.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, dir.getOpposite())) {
				setFlowBit(i, type);
				nHasIO |= type;
			} else setFlowBit(i, 0);
		}
		setFlowBit(6, nHasIO);
		if (flow != lFlow) {
			this.markUpdate();
			for (LiquidPipe pipe : updateList) {
				pipe.onNeighborBlockChange(this.getBlockType());
			}
		}
		updateCon = false;
	}

	private void transferFluid(int type) {
		boolean rs = worldObj.isBlockPowered(getPos());
		int d;
		EnumFacing dir;
		if (type == BlockLiquidPipe.ID_Transport || (filter != null && !filter.active(rs)));
		else if (type == BlockLiquidPipe.ID_Extraction) {
			for (int i = 0; i < 6; i++) {
				d = this.getFlowBit(i);
				if (d != 2) continue;
				dir = EnumFacing.VALUES[i];
				ICapabilityProvider te = this.getTileOnSide(dir);
				if (te == null || te instanceof LiquidPipe) continue;
				IFluidHandler acc = te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, dir.getOpposite());
				if (acc != null) this.extract(acc, tanks.fluids[0]);
				else updateCon = true;
			}
		} else if (tanks.fluids[0] != null) {
			for (int i = 0; i < 6; i++) {
				d = this.getFlowBit(i);
				if (d != 1) continue;
				dir = EnumFacing.VALUES[i];
				ICapabilityProvider te = this.getTileOnSide(dir);
				if (te == null || te instanceof LiquidPipe) continue;
				IFluidHandler acc = te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, dir.getOpposite());
				if (acc != null) {
					this.insert(acc, tanks.fluids[0]);
					if (tanks.fluids[0] == null) break;
				} else updateCon = true;
			}
		}
		FluidStack fluid = tanks.fluids[0];
		if (fluid == null || (type == BlockLiquidPipe.ID_Injection && filter != null && !filter.transfer(fluid))) return;
		ArrayList<TankContainer> flowList = new ArrayList<TankContainer>(5);
		int n = tanks.getAmount(0), max = 0;
		for (int i = 0; i < 6; i++)
			if ((d = this.getFlowBit(i)) == 1) {
				dir = EnumFacing.VALUES[i];
				TileEntity te = worldObj.getTileEntity(pos.offset(dir));
				if (te != null && te instanceof LiquidPipe) {
					TankContainer dst = ((LiquidPipe)te).tanks;
					int f = dst.getSpace(0);
					if (f > 0 && (dst.fluids[0] == null || fluid.isFluidEqual(dst.fluids[0]))) {
						flowList.add(dst);
						max += f;
					}
				}
			}
		if (flowList.isEmpty()) return;
		int ofs = 0;
		for (TankContainer dst : flowList) {
			ofs %= max; ofs += n * dst.getSpace(0);
			tanks.drain(0, dst.fill(0, new FluidStack(tanks.fluids[0].getFluid(), ofs / max), true), true);
		}
	}

	private void extract(IFluidHandler inv, FluidStack fluid) {
		if (PipeUpgradeFluid.isNullEq(filter)) {
			if (fluid == null) tanks.fluids[0] = inv.drain(1000, true);
			else tanks.fill(0, inv.drain(new FluidStack(fluid, tanks.getSpace(0)), true), true);
		} else {
			fluid = filter.getExtract(fluid, inv);
			if (fluid != null) inv.drain(new FluidStack(fluid, tanks.fill(0, fluid, true)), true);
		}
	}

	private void insert(IFluidHandler inv, FluidStack fluid) {
		if (PipeUpgradeFluid.isNullEq(filter)) tanks.drain(0, inv.fill(fluid, true), true);
		else {
			int n = filter.insertAmount(fluid, inv);
			if (n > 0) tanks.drain(0, inv.fill(new FluidStack(fluid, n), true), true);
		}
	}

	@Override
	public void onNeighborBlockChange(Block b) {
		updateCon = true;
	}

	@Override
	public boolean onActivated(EntityPlayer player, EnumHand hand, ItemStack item, EnumFacing dir, float X, float Y, float Z) {
		int s = dir.getIndex();
		int type = this.getBlockMetadata();
		boolean canF = type == BlockLiquidPipe.ID_Extraction || type == BlockLiquidPipe.ID_Injection;
		if (player.isSneaking() && item == null) {
			if (worldObj.isRemote) return true;
			if (cover != null) {
				player.setHeldItem(hand, cover.item);
				cover = null;
				this.markUpdate();
				return true;
			}
			X -= 0.5F;
			Y -= 0.5F;
			Z -= 0.5F;
			float dx = Math.abs(X);
			float dy = Math.abs(Y);
			float dz = Math.abs(Z);
			if (dy > dz && dy > dx) s = Y < 0 ? 0 : 1;
			else if (dz > dx) s = Z < 0 ? 2 : 3;
			else s = X < 0 ? 4 : 5;
			int lock = this.getFlowBit(s) == 3 ? 0 : 3;
			this.setFlowBit(s, lock);
			this.onNeighborBlockChange(Blocks.AIR);
			this.markUpdate();
			TileEntity te = Utils.getTileOnSide(this, (byte)s);
			if (te != null && te instanceof LiquidPipe) {
				LiquidPipe pipe = (LiquidPipe)te;
				pipe.setFlowBit(s^1, lock);
				pipe.onNeighborBlockChange(Blocks.AIR);
				pipe.markUpdate();
			}
			return true;
		} else if (!player.isSneaking() && item == null && filter != null) {
			if (worldObj.isRemote) return true;
			item = new ItemStack(Objects.fluidUpgrade);
			item.setTagCompound(PipeUpgradeFluid.save(filter));
			filter = null;
			player.setHeldItem(hand, item);
			return true;
		} else if (!player.isSneaking() && cover == null && item != null && (cover = Cover.create(item)) != null) {
			if (worldObj.isRemote) return true;
			item.stackSize--;
			if (item.stackSize <= 0) item = null;
			player.setHeldItem(hand, item);
			this.markUpdate();
			return true;
		} else if (filter == null && canF && item != null && item.getItem() instanceof ItemFluidUpgrade && item.getTagCompound() != null) {
			if (worldObj.isRemote) return true;
			filter = PipeUpgradeFluid.load(item.getTagCompound());
			item.stackSize--;
			if (item.stackSize <= 0) item = null;
			player.setHeldItem(hand, item);
			return true;
		} else return false;
	}

	private int getFlowBit(int b) {
		return flow >> (b * 2) & 3;
	}

	private void setFlowBit(int b, int v) {
		b *= 2;
		flow = (short)(flow & ~(3 << b) | (v & 3) << b);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		nbt.setShort("flow", flow);
		if (filter != null) nbt.setTag("filter", PipeUpgradeFluid.save(filter));
		if (cover != null) cover.write(nbt, "cover");
		return super.writeToNBT(nbt);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		flow = nbt.getShort("flow");
		if (nbt.hasKey("filter")) filter = PipeUpgradeFluid.load(nbt.getCompoundTag("filter"));
		cover = Cover.read(nbt, "cover");
		updateCon = true;
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		flow = pkt.getNbtCompound().getShort("flow");
		cover = Cover.read(pkt.getNbtCompound(), "cover");
		this.markUpdate();
	}

	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setShort("flow", flow);
		if (cover != null) cover.write(nbt, "cover");
		return new SPacketUpdateTileEntity(getPos(), -1, nbt);
	}

	@Override
	public int textureForSide(byte s) {
		if (s == -1) return this.getBlockMetadata();
		TileEntity p = Utils.getTileOnSide(this, s);
		int b = getFlowBit(s);
		if (b == 3 || p == null || !p.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, EnumFacing.VALUES[s^1])) return -1;
		return b;
	}

	@Override
	public void breakBlock() {
		super.breakBlock();
		if (filter != null) {
			ItemStack item = new ItemStack(Objects.fluidUpgrade);
			item.setTagCompound(PipeUpgradeFluid.save(filter));
			filter = null;
			EntityItem entity = new EntityItem(worldObj, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, item);
			worldObj.spawnEntityInWorld(entity);
		}
		if (cover != null) {
			EntityItem entity = new EntityItem(worldObj, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, cover.item);
			cover = null;
			worldObj.spawnEntityInWorld(entity);
		}
	}

	@Override
	public Cover getCover() {
		return cover;
	}

}
