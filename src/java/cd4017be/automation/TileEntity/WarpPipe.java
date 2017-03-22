package cd4017be.automation.TileEntity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import cd4017be.api.automation.IFluidPipeCon;
import cd4017be.api.automation.IItemPipeCon;
import cd4017be.automation.Objects;
import cd4017be.automation.pipes.BasicWarpPipe;
import cd4017be.automation.pipes.ConComp;
import cd4017be.automation.pipes.WarpPipePhysics;
import cd4017be.lib.templates.IPipe;
import cd4017be.lib.templates.MultiblockTile;
import cd4017be.lib.util.Utils;

public class WarpPipe extends MultiblockTile<BasicWarpPipe, WarpPipePhysics> implements IPipe, IItemPipeCon, IFluidPipeCon {

	private Cover cover = null;

	public WarpPipe() {
		comp = new BasicWarpPipe(this);
	}

	@Override
	public void update() {
		if (worldObj.isRemote) return;
		super.update();
		comp.redstone = worldObj.isBlockPowered(pos);
	}

	@Override
	public boolean onActivated(EntityPlayer player, EnumHand hand, ItemStack item, EnumFacing dir, float X, float Y, float Z) {
		if (worldObj.isRemote) return true;
		if (cover != null) {
			if (player.isSneaking() && item == null) {
				this.dropStack(cover.item);
				cover = null;
				this.markUpdate();
				return true;
			}
			return false;
		}
		dir = this.getClickedSide(X, Y, Z);
		byte s = (byte)dir.getIndex();
		byte t = comp.con[s];
		if (t >= 2) {
			long uid = WarpPipePhysics.SidedPosUID(comp.getUID(), s);
			ConComp con = comp.network.connectors.get(uid);
			if (con != null && con.onClicked(player, hand, item, uid)) {
				markUpdate();
				return true;
			}
		} else if (player.isSneaking() && item == null) {
			comp.setConnect(s, t != 0);
			this.markUpdate();
			BasicWarpPipe pipe = comp.getNeighbor(s);
			if (pipe != null && pipe.tile instanceof WarpPipe) {
				pipe.setConnect((byte)(s^1), t != 0);
				((WarpPipe)pipe.tile).markUpdate();
			}
			return true;
		} 
		if (player.isSneaking()) return false;
		else if (t < 2 && ConComp.createFromItem(item, comp, (byte)s)) {
			if (--item.stackSize <= 0) item = null;
			player.setHeldItem(hand, item);
			this.markUpdate();
			return true;
		} else if (item != null && (cover = Cover.create(item)) != null) {
			if (--item.stackSize <= 0) item = null;
			player.setHeldItem(hand, item);
			this.markUpdate();
			return true;
		} else return false;
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		comp.writeToNBT(nbt);
		if (cover != null) cover.write(nbt, "cover");
		return super.writeToNBT(nbt);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		cover = Cover.read(nbt, "cover");
		comp = BasicWarpPipe.readFromNBT(this, nbt);
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		NBTTagCompound nbt = pkt.getNbtCompound();
		cover = Cover.read(nbt, "cover");
		byte[] data = nbt.getByteArray("con");
		if (data.length == 6) System.arraycopy(data, 0, comp.con, 0, 6);
		comp.hasFilters = nbt.getByte("filt");
		this.markUpdate();
	}

	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		NBTTagCompound nbt = new NBTTagCompound();
		if (cover != null) cover.write(nbt, "cover");
		byte[] data = new byte[comp.con.length];
		System.arraycopy(comp.con, 0, data, 0, data.length);
		nbt.setByteArray("con", data);
		nbt.setByte("filt", comp.hasFilters);
		return new SPacketUpdateTileEntity(getPos(), -1, nbt);
	}

	@Override
	public int textureForSide(byte s) {
		if (s < 0 || s > 5) return 0;
		byte t = comp.con[s];
		if (t == 1) return -1;
		else if (t > 1) return t - 1 + (comp.hasFilters >> s & 1) * 4;
		TileEntity te = Utils.getTileOnSide(this, s);
		return te != null && te.hasCapability(Objects.WARP_PIPE_CAP, EnumFacing.VALUES[s^1]) ? 0 : -1;
	}

	@Override
	public Cover getCover() {
		return cover;
	}

	@Override
	public void breakBlock() {
		super.breakBlock();
		for (int i = 0; i < 6; i++) {
			ConComp con = comp.network.remConnector(comp, (byte)i);
			if (con != null) con.onClicked(null, null, null, 0);
		}
		if (cover != null) this.dropStack(cover.item);
	}

	@Override
	public byte getItemConnectType(int s) {
		byte t = comp.con[s];
		return (byte)(t == 2 ? 1 : t == 3 ? 2 : 0);
	}

	@Override
	public byte getFluidConnectType(int s) {
		byte t = comp.con[s];
		return (byte)(t == 4 ? 1 : t == 5 ? 2 : 0);
	}

	@Override
	public ItemStack insert(ItemStack item, EnumFacing side) {return item;}

}
