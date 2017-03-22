package cd4017be.automation.TileEntity;

import java.util.ArrayList;

import cd4017be.api.automation.IFluidPipeCon;
import cd4017be.automation.Config;
import cd4017be.automation.Objects;
import cd4017be.automation.Block.BlockItemPipe;
import cd4017be.automation.Block.BlockLiquidPipe;
import cd4017be.automation.Item.ItemFluidUpgrade;
import cd4017be.automation.Item.PipeUpgradeFluid;
import cd4017be.lib.ModTileEntity;
import cd4017be.lib.templates.IPipe;
import cd4017be.lib.templates.LinkedTank;
import cd4017be.lib.util.TileAccess;
import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

/**
 *
 * @author CD4017BE
 */
public class LiquidPipe extends ModTileEntity implements ITickable, IPipe {

	public static byte ticks = 1;
	private final LinkedTank tankcap;
	public FluidStack tank, last;
	private PipeUpgradeFluid filter = null;
	private Cover cover = null;
	private ArrayList<LiquidPipe> targets = new ArrayList<LiquidPipe>(5);
	private ArrayList<TileAccess> invs = null;
	/** bits[0-13 (6+1)*2]: (side + total) * dir{0:none, 1:in, 2:out, 3:lock/both} */
	private short flow;
	private boolean updateCon = true;
	private byte timer = 0;

	public LiquidPipe() {
		tankcap = new LinkedTank(Config.tankCap[0], () -> tank, (fluid) -> tank = fluid);
	}

	@Override
	public void update() {
		if (worldObj.isRemote) return;
		int type = this.getBlockMetadata();
		if (updateCon) this.updateConnections(type);
		if ((flow & 0x3000) == 0x3000)
			switch(type) {
			case BlockLiquidPipe.ID_Injection:
				if (tank != null && (filter == null || filter.active(worldObj.isBlockPowered(pos)))) transferIn();
				if (tank != null && !targets.isEmpty() && (filter == null || filter.transfer(tank))) transfer();
				break;
			case BlockLiquidPipe.ID_Extraction:
				timer++;
				if ((filter == null || filter.active(worldObj.isBlockPowered(pos))) && (timer & 0xff) >= ticks) transferEx();
			default:
				if (tank != null) transfer();
			}
		if (last != tank) {
			last = tank;
			markUpdate();
		}
	}

	@Override
	public boolean hasCapability(Capability<?> cap, EnumFacing facing) {
		return cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getCapability(Capability<T> cap, EnumFacing facing) {
		return cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY ? (T)tankcap : null;
	}

	private void updateConnections(int type) {
		targets.clear();
		if (type != BlockItemPipe.ID_Transport) {
			if (invs == null) invs = new ArrayList<TileAccess>(5);
			else invs.clear();
		}
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
				if (nDirIO == 1) targets.add(pipe);
				updateList.add(pipe);
			} else if (te != null && te instanceof IFluidPipeCon) {
				byte d = ((IFluidPipeCon)te).getFluidConnectType(i^1);
				d = d == 1 ? 2 : d == 2 ? 1 : (byte)0;
				setFlowBit(i, d);
				nHasIO |= d;
			} else if (type != BlockLiquidPipe.ID_Transport && te != null && te.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, dir.getOpposite())) {
				setFlowBit(i, type);
				nHasIO |= type;
				invs.add(new TileAccess(te, dir.getOpposite()));
			} else setFlowBit(i, 0);
		}
		setFlowBit(6, nHasIO);
		if (flow != lFlow) {
			this.markUpdate();
			for (LiquidPipe pipe : updateList) pipe.updateCon = true;
		}
		updateCon = false;
	}

	private void transfer() {
		for (LiquidPipe pipe : targets)
			if (pipe.tileEntityInvalid) updateCon = true;
			else if (pipe.tank == null) {
				pipe.tank = tank;
				tank = null;
				break;
			} else if (tank.isFluidEqual(pipe.tank)) {
				int n = tankcap.cap - pipe.tank.amount;
				if (n >= tank.amount) {
					pipe.tank.amount += tank.amount;
					tank = null;
					break;
				} else {
					pipe.tank.amount += n;
					tank.amount -= n;
				}
			}
		
	}

	private void transferIn() {
		IFluidHandler acc;
		for (TileAccess inv : invs)
			if (inv.te.isInvalid() || (acc = inv.te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, inv.side)) == null) updateCon = true;
			else if (PipeUpgradeFluid.isNullEq(filter)) {
				if ((tank.amount -= acc.fill(tank, true)) <= 0) {
					tank = null;
					break;
				}
			} else {
				int m = filter.insertAmount(tank, acc);
				if (m > 0 && (tank.amount -= acc.fill(new FluidStack(tank, m), true)) <= 0) {
					tank = null;
					break;
				}
			}
	}

	private void transferEx() {
		timer = 0;
		IFluidHandler acc;
		for (TileAccess inv : invs)
			if (inv.te.isInvalid() || (acc = inv.te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, inv.side)) == null) updateCon = true;
			else if (PipeUpgradeFluid.isNullEq(filter)) {
				if (tank == null) tank = acc.drain(tankcap.cap, true);
				else {
					int m = tankcap.cap - tank.amount;
					if (m <= 0) break;
					FluidStack fluid = acc.drain(new FluidStack(tank, m), true);
					if (fluid != null) tank.amount += fluid.amount;
				}
			} else {
				int n;
				if (tank == null) n = 0;
				else if ((n = tank.amount) >= tankcap.cap) break;
				FluidStack fluid = filter.getExtract(tank, acc);
				if (fluid == null || fluid.amount <= 0) continue;
				int m = tankcap.cap - n;
				if (m < fluid.amount) fluid.amount = m;
				fluid.amount = acc.drain(fluid, true).amount + n;
				tank = fluid;
			}
	}

	@Override
	public void onNeighborTileChange(BlockPos pos) {
		updateCon = true;
	}

	@Override
	public void onNeighborBlockChange(Block b) {
		updateCon = true;
	}

	@Override
	public boolean onActivated(EntityPlayer player, EnumHand hand, ItemStack item, EnumFacing dir, float X, float Y, float Z) {
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
			dir = getClickedSide(X, Y, Z);
			int s = dir.getIndex();
			int lock = this.getFlowBit(s) == 3 ? 0 : 3;
			this.setFlowBit(s, lock);
			updateCon = true;
			this.markUpdate();
			ICapabilityProvider te = getTileOnSide(dir);
			if (te != null && te instanceof LiquidPipe) {
				LiquidPipe pipe = (LiquidPipe)te;
				pipe.setFlowBit(s^1, lock);
				pipe.updateCon = true;
				pipe.markUpdate();
			}
			return true;
		} else if (!player.isSneaking() && item == null && filter != null) {
			if (worldObj.isRemote) return true;
			item = new ItemStack(Objects.fluidUpgrade);
			item.setTagCompound(PipeUpgradeFluid.save(filter));
			filter = null;
			player.setHeldItem(hand, item);
			markUpdate();
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
			markUpdate();
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
		if (tank != null) nbt.setTag("fluid", tank.writeToNBT(new NBTTagCompound()));
		return super.writeToNBT(nbt);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		flow = nbt.getShort("flow");
		if (nbt.hasKey("filter")) filter = PipeUpgradeFluid.load(nbt.getCompoundTag("filter"));
		if (nbt.hasKey("fluid")) tank = FluidStack.loadFluidStackFromNBT(nbt.getCompoundTag("fluid"));
		cover = Cover.read(nbt, "cover");
		updateCon = true;
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		NBTTagCompound nbt = pkt.getNbtCompound();
		short nf = nbt.getShort("flow");
		boolean f = nbt.getBoolean("filt");
		Cover nc = Cover.read(nbt, "cover");
		if (nbt.hasKey("fl", 10)) tank = FluidStack.loadFluidStackFromNBT(nbt.getCompoundTag("fl"));
		else tank = null;
		if (nf != flow || (f ^ filter != null) || (cover == null ^ nc == null) || (cover != null && nc.item.isItemEqual(cover.item))) {
			if (f) filter = new PipeUpgradeFluid();
			else filter = null;
			flow = nf;
			cover = nc;
			this.markUpdate();
		}
	}

	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setShort("flow", flow);
		nbt.setBoolean("filt", filter != null);
		if (last != null) nbt.setTag("fl", last.writeToNBT(new NBTTagCompound()));
		if (cover != null) cover.write(nbt, "cover");
		return new SPacketUpdateTileEntity(getPos(), -1, nbt);
	}

	@Override
	public int textureForSide(byte s) {
		if (s == -1) return this.getBlockMetadata();
		int b = getFlowBit(s);
		EnumFacing f = EnumFacing.VALUES[s];
		ICapabilityProvider p = getTileOnSide(f);
		if (b == 3 || p == null || !p.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, f.getOpposite())) return -1;
		if (filter != null && b != 0 && !(b == 2 && p instanceof LiquidPipe)) b += 2;
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
