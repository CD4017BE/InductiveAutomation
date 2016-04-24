package cd4017be.automation.TileEntity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import cd4017be.automation.shaft.ShaftComponent;
import cd4017be.automation.shaft.ShaftPhysics.IShaft;
import cd4017be.lib.ModTileEntity;
import cd4017be.lib.templates.IPipe;
import cd4017be.lib.templates.SharedNetwork;
import cd4017be.lib.util.Utils;

public class Shaft extends ModTileEntity implements IPipe, IShaft, ITickable {

	private Cover cover;
	public ShaftComponent shaft;
	
	public Shaft() {
		this.shaft = new ShaftComponent(this, 1);
	}
	
	@Override
	public void update() 
	{
		if (shaft.updateCon) shaft.network.updateLink(shaft);
		shaft.network.updateTick(shaft);
	}

	@Override
	public boolean onActivated(EntityPlayer player, EnumFacing dir, float X, float Y, float Z) 
	{
		if (worldObj.isRemote) return true;
		ItemStack item = player.getCurrentEquippedItem();
		if (cover != null) {
			if (player.isSneaking() && item == null) {
				this.dropStack(cover.item);
				cover = null;
				this.markUpdate();
				return true;
			}
			return false;
		} 
		if (shaft.onClicked(player, item)) return true;
		else if (item != null && !player.isSneaking() && (cover = Cover.create(item)) != null) {
			if (--item.stackSize <= 0) item = null;
			player.setCurrentItemOrArmor(0, item);
			this.markUpdate();
			return true;
		} else return false;
	}

	@Override
	public void onNeighborTileChange(BlockPos pos) 
	{
		shaft.updateCon = true;
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) 
	{
		super.writeToNBT(nbt);
		shaft.writeToNBT(nbt);
		if (cover != null) cover.write(nbt, "cover");
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) 
	{
		super.readFromNBT(nbt);
		cover = Cover.read(nbt, "cover");
		shaft = ShaftComponent.readFromNBT(this, nbt);
	}

	@Override
	public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) 
	{
		NBTTagCompound nbt = pkt.getNbtCompound();
		if (nbt.hasKey("RotVel")) {
			shaft.network.v = nbt.getFloat("RotVel");
			shaft.network.s = nbt.getFloat("RotPos");
		}
		if (nbt.hasKey("con")) {
			cover = Cover.read(nbt, "cover");
			shaft.con = nbt.getByte("con");
			shaft.type = nbt.getByte("type");
			this.markUpdate();
		}
	}

	@Override
	public Packet getDescriptionPacket() 
	{
		NBTTagCompound nbt = new NBTTagCompound();
		if (cover != null) cover.write(nbt, "cover");
		nbt.setByte("type", shaft.type);
		nbt.setByte("con", shaft.con);
		return new S35PacketUpdateTileEntity(getPos(), -1, nbt);
	}
	
	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return shaft.network.boundingBox(this);
	}

	@Override
	public void breakBlock() 
	{
		super.breakBlock();
		if (cover != null) this.dropStack(cover.item);
	}

	@Override
	public int textureForSide(byte s) {
		if (s < 0 || s / 2 != this.getBlockMetadata()) return -1;
		TileEntity te = Utils.getTileOnSide(this, s);
		return te != null && te instanceof IShaft ? -2 : 0;
	}

	@Override
	public Cover getCover() {
		return cover;
	}

	@Override
	public ShaftComponent getShaft() {
		return shaft;
	}
	
	@Override
	public void validate() {
		super.validate();
		shaft.initUID(SharedNetwork.ExtPosUID(pos, dimensionId));
	}

	@Override
	public void invalidate() {
		super.invalidate();
		shaft.remove();
	}

	@Override
	public void onChunkUnload() {
		super.onChunkUnload();
		shaft.remove();
	}

}
