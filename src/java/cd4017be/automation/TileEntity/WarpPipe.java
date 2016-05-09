package cd4017be.automation.TileEntity;


import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import cd4017be.api.automation.IFluidPipeCon;
import cd4017be.api.automation.IItemPipeCon;
import cd4017be.automation.pipes.BasicWarpPipe;
import cd4017be.automation.pipes.ConComp;
import cd4017be.automation.pipes.IWarpPipe;
import cd4017be.automation.pipes.WarpPipePhysics;
import cd4017be.lib.templates.AutomatedTile;
import cd4017be.lib.templates.IPipe;
import cd4017be.lib.templates.SharedNetwork;
import cd4017be.lib.util.Utils;

public class WarpPipe extends AutomatedTile implements IPipe, IItemPipeCon, IFluidPipeCon, IWarpPipe
{
	private Cover cover = null;
	public BasicWarpPipe pipe;
	
	public WarpPipe() {
		pipe = new BasicWarpPipe(this);
	}
	
	@Override
	public void update() 
	{
		if (worldObj.isRemote) return;
		pipe.redstone = worldObj.isBlockPowered(pos);
		if (pipe.updateCon) pipe.network.updateLink(pipe);
		pipe.network.updateTick(pipe);
	}
	
	@Override
	public boolean onActivated(EntityPlayer player, EnumHand hand, ItemStack item, EnumFacing dir, float X, float Y, float Z) 
	{
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
		int s = dir.getIndex();
		byte t = pipe.con[s];
		if (t >= 2) {
			long uid = WarpPipePhysics.SidedPosUID(pipe.getUID(), s);
			ConComp con = pipe.network.connectors.get(uid);
			if (con != null && con.onClicked(player, hand, item, uid)) return true;
		} else if (player.isSneaking() && item == null) {
			pipe.con[s] = (byte)(1 - t);
			pipe.updateCon = true;
			if (t == 0) pipe.network.onDisconnect(pipe, (byte)(s^1), WarpPipePhysics.ExtPosUID(pos.offset(dir), dimensionId));
			this.markUpdate();
			TileEntity te = Utils.getTileOnSide(this, (byte)s);
			if (te != null && te instanceof WarpPipe) {
				BasicWarpPipe pipe = ((WarpPipe)te).pipe;
				if (pipe.con[s^1] < 2) {
					pipe.con[s^1] = (byte)(1 - t);
					pipe.updateCon = true;
					pipe.pipe.markUpdate();
				}
			}
			return true;
		} 
		if (player.isSneaking()) return false;
		else if (t < 2 && ConComp.createFromItem(item, pipe, (byte)s)) {
			pipe.updateCon = true;
			if (t == 0) pipe.network.onDisconnect(pipe, (byte)(s^1), WarpPipePhysics.ExtPosUID(pos.offset(dir), dimensionId));
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
	public void onNeighborTileChange(BlockPos pos) 
	{
		pipe.updateCon = true;
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) 
	{
		super.writeToNBT(nbt);
		pipe.writeToNBT(nbt);
		if (cover != null) cover.write(nbt, "cover");
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) 
	{
		super.readFromNBT(nbt);
		cover = Cover.read(nbt, "cover");
		pipe = BasicWarpPipe.readFromNBT(this, nbt);
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) 
	{
		cover = Cover.read(pkt.getNbtCompound(), "cover");
		byte[] data = pkt.getNbtCompound().getByteArray("con");
		if (data.length == 6) System.arraycopy(data, 0, pipe.con, 0, 6);
		this.markUpdate();
	}

	@Override
	public Packet getDescriptionPacket() 
	{
		NBTTagCompound nbt = new NBTTagCompound();
		if (cover != null) cover.write(nbt, "cover");
		byte[] data = new byte[pipe.con.length];
		System.arraycopy(pipe.con, 0, data, 0, data.length);
		nbt.setByteArray("con", data);
		return new SPacketUpdateTileEntity(getPos(), -1, nbt);
	}

	@Override
	public int textureForSide(byte s) 
	{
		if (s < 0 || s > 5) return 0;
		byte t = pipe.con[s];
		if (t > 0) return t == 1 ? -1 : t - 1;
		TileEntity te = Utils.getTileOnSide(this, s);
		return te != null && te instanceof IWarpPipe ? 0 : -1;
	}

	@Override
	public Cover getCover() 
	{
		return cover;
	}
	
	@Override
	public void breakBlock() 
	{
		super.breakBlock();
		for (int i = 0; i < 6; i++) {
			ConComp con = pipe.network.remConnector(pipe, (byte)i);
			if (con != null) con.onClicked(null, null, null, 0);
		}
		if (cover != null) this.dropStack(cover.item);
	}

	@Override
	public byte getItemConnectType(int s) 
	{
		byte t = pipe.con[s];
		return (byte)(t == 2 ? 1 : t == 3 ? 2 : 0);
	}
	
	@Override
	public byte getFluidConnectType(int s) {
		byte t = pipe.con[s];
		return (byte)(t == 4 ? 1 : t == 5 ? 2 : 0);
	}

	@Override
	public BasicWarpPipe getWarpPipe() {
		return pipe;
	}

	@Override
	public void validate() {
		super.validate();
		if (!worldObj.isRemote) pipe.initUID(SharedNetwork.ExtPosUID(pos, dimensionId));
	}

	@Override
	public void invalidate() {
		super.invalidate();
		pipe.remove();
	}

	@Override
	public void onChunkUnload() {
		super.onChunkUnload();
		pipe.remove();
	}
	
}
