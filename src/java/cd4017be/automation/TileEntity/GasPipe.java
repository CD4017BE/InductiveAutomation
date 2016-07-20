package cd4017be.automation.TileEntity;

import java.io.IOException;
import java.util.List;

import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import cd4017be.automation.gas.GasState;
import cd4017be.automation.pipes.WarpPipePhysics;
import cd4017be.automation.shaft.GasContainer;
import cd4017be.automation.shaft.GasPhysics.IGasCon;
import cd4017be.automation.shaft.GasPhysics.IGasStorage;
import cd4017be.automation.shaft.HeatReservoir;
import cd4017be.automation.shaft.IHeatReservoir;
import cd4017be.lib.ModTileEntity;
import cd4017be.lib.TileContainer;
import cd4017be.lib.TileEntityData;
import cd4017be.lib.templates.IPipe;
import cd4017be.lib.templates.SharedNetwork;
import cd4017be.lib.util.Utils;

public class GasPipe extends ModTileEntity implements IPipe, IGasStorage, ITickable {
	public static final float size = 0.25F;
	private Cover cover = null;
	public GasContainer pipe;
	
	public GasPipe() {
		pipe = new GasContainer(this, size);
	}
	
	@Override
	public void update() 
	{
		if (worldObj.isRemote) return;
		if (pipe.updateCon) pipe.network.updateLink(pipe);
		pipe.network.updateTick(pipe);
	}
	
	@Override
	public boolean onActivated(EntityPlayer player, EnumHand hand, ItemStack item, EnumFacing dir, float X, float Y, float Z) 
	{
		if (worldObj.isRemote) return true;
		if (!player.isSneaking() && item == null) return super.onActivated(player, hand, item, dir, X, Y, Z);
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
		boolean t = (pipe.con >> s & 1) != 0;
		if (player.isSneaking() && item == null) {
			pipe.con ^= 1 << s;
			pipe.updateCon = true;
			if (t) pipe.network.onDisconnect(pipe, (byte)(s^1), WarpPipePhysics.ExtPosUID(pos.offset(dir), 0));
			this.markUpdate();
			TileEntity te = Utils.getTileOnSide(this, (byte)s);
			if (te != null && te instanceof GasPipe) {
				GasContainer pipe = ((GasPipe)te).pipe;
				if ((pipe.con >> (s^1) & 1) == (pipe.con >> s & 1)) {
					pipe.con ^= 1 << (s^1);
					pipe.updateCon = true;
					pipe.tile.markUpdate();
				}
			}
			return true;
		} 
		if (player.isSneaking()) return false;
		if (item != null && (cover = Cover.create(item)) != null) {
			if (--item.stackSize <= 0) item = null;
			player.setHeldItem(hand, item);
			this.markUpdate();
			return true;
		} else return false;
	}

	@Override
	public void onNeighborTileChange(BlockPos pos) {
		pipe.updateCon = true;
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		pipe.writeToNBT(nbt, "gas");
		if (cover != null) cover.write(nbt, "cover");
        return super.writeToNBT(nbt);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) 
	{
		super.readFromNBT(nbt);
		cover = Cover.read(nbt, "cover");
		pipe = GasContainer.readFromNBT(this, nbt, "gas", size);
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) 
	{
		cover = Cover.read(pkt.getNbtCompound(), "cover");
		pipe.con = pkt.getNbtCompound().getByte("con");
		this.markUpdate();
	}

	@Override
	public SPacketUpdateTileEntity getUpdatePacket() 
	{
		NBTTagCompound nbt = new NBTTagCompound();
		if (cover != null) cover.write(nbt, "cover");
		nbt.setByte("con", pipe.con);
		return new SPacketUpdateTileEntity(getPos(), -1, nbt);
	}

	@Override
	public int textureForSide(byte s) 
	{
		if (s < 0 || s > 5) return 0;
		if ((pipe.con >> s & 1) == 0) return -1;
		TileEntity te = Utils.getTileOnSide(this, s);
		return te != null && te instanceof IGasCon && ((IGasCon)te).conGas((byte)(s^1)) ? 0 : -1;
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
		if (cover != null) this.dropStack(cover.item);
	}

	@Override
	public void validate() {
		super.validate();
		if (!worldObj.isRemote) pipe.initUID(SharedNetwork.ExtPosUID(pos, 0));
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

	@Override
	public GasContainer getGas() {
		return pipe;
	}

	@Override
	public boolean conGas(byte side) {
		return pipe.canConnect(side);
	}

	@Override
	public void initContainer(TileContainer container) {
		container.refData = new TileEntityData(0, 0, 4, 0);
	}

	@Override
	public void updateNetData(PacketBuffer dis, TileContainer container) throws IOException {
		for (int i = 0; i < container.refData.floats.length; i++) 
			container.refData.floats[i] = dis.readFloat();
	}

	@Override
	public boolean detectAndSendChanges(TileContainer container, List<IContainerListener> crafters, PacketBuffer dos) throws IOException {
		
		if (pipe.network == null) return false;
		GasState gas = pipe.network.gas;
		dos.writeFloat(gas.V);
		dos.writeFloat(gas.nR);
		dos.writeFloat(gas.T);
		dos.writeFloat(gas.P());
		return true;
	}

	@Override
	public IHeatReservoir getHeat(byte side) {
		return pipe;
	}

	@Override
	public float getHeatRes(byte side) {
		Float r = HeatReservoir.blocks.get(cover == null ? Material.AIR : cover.block.getMaterial());
		return r == null ? HeatReservoir.def_block : r.floatValue();
	}

}
