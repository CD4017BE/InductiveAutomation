package cd4017be.automation.TileEntity;

import cd4017be.api.Capabilities;
import cd4017be.api.automation.PipeEnergy;
import cd4017be.automation.Config;
import cd4017be.automation.Objects;
import cd4017be.lib.templates.AutomatedTile;
import cd4017be.lib.templates.IPipe;
import cd4017be.lib.util.Utils;
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
import net.minecraftforge.common.capabilities.ICapabilityProvider;

/**
 *
 * @author CD4017BE
 */
public class Wire extends AutomatedTile implements IPipe {

	public int type;
	private Cover cover = null;

	public Wire() {
		energy = new PipeEnergy(0, 0);
	}

	private void setWireType() {
		Block id = this.getBlockType();
		type = id == Objects.wireC ? 0 : id == Objects.wireA ? 1 : 2;
	}

	@Override
	public void update() {
		if (energy.Umax == 0) {
			this.setWireType();
			byte con = energy.sideCfg;
			energy = new PipeEnergy(Config.Umax[type], Config.Rcond[type]);
			energy.sideCfg = con;
		}
		super.update();
	}

	@Override
	public boolean onActivated(EntityPlayer player, EnumHand hand, ItemStack item, EnumFacing dir, float X, float Y, float Z) {
		if (player.isSneaking() && item == null && cover != null) {
			if (worldObj.isRemote) return true;
			player.setHeldItem(hand, cover.item);
			cover = null;
			this.markUpdate();
			return true;
		} else if (player.isSneaking() && item == null) {
			if (worldObj.isRemote) return true;
			dir = getClickedSide(X, Y, Z);
			int s = dir.getIndex();
			boolean con = energy.isConnected(s);
			energy.sideCfg ^= 1 << s;
			this.markUpdate();
			ICapabilityProvider te = getTileOnSide(dir);
			if (te != null && te instanceof Wire) {
				PipeEnergy pipe = ((Wire)te).energy;
				if (pipe != null) {
					if (con) pipe.sideCfg &= ~(1 << (s^1));
					else pipe.sideCfg |= 1 << (s^1);
					((Wire)te).markUpdate();
				}
			}
			return true;
		} else if (!player.isSneaking() && cover == null && item != null && (cover = Cover.create(item)) != null) {
			if (worldObj.isRemote) return true;
			item.stackSize--;
			if (item.stackSize <= 0) item = null;
			player.setHeldItem(hand, item);
			this.markUpdate();
			return true;
		} else return false;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) 
	{
		type = nbt.getByte("type");
		energy = new PipeEnergy(Config.Umax[type], Config.Rcond[type]);
		cover = Cover.read(nbt, "cover");
		super.readFromNBT(nbt);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) 
	{
		nbt.setByte("type", (byte)type);
		if (cover != null) cover.write(nbt, "cover");
		return super.writeToNBT(nbt);
	}

	@Override
	public SPacketUpdateTileEntity getUpdatePacket() 
	{
		NBTTagCompound nbt = new NBTTagCompound();
		if (cover != null) cover.write(nbt, "cover");
		nbt.setByte("con", energy.sideCfg);
		return new SPacketUpdateTileEntity(pos, -1, nbt);
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) 
	{
		cover = Cover.read(pkt.getNbtCompound(), "cover");
		energy.sideCfg = pkt.getNbtCompound().getByte("con");
		this.markUpdate();
	}

	@Override
	public void breakBlock() 
	{
		super.breakBlock();
		if (cover != null) {
			EntityItem entity = new EntityItem(worldObj, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, cover.item);
			cover = null;
			worldObj.spawnEntityInWorld(entity);
		}
	}

	@Override
	public int textureForSide(byte s) 
	{
		if (s < 0) return 0;
		TileEntity p = Utils.getTileOnSide(this, s);
		return energy.isConnected(s) && p != null && p.hasCapability(Capabilities.ELECTRIC_CAPABILITY, EnumFacing.VALUES[s^1]) ? 0 : -1;
	}
	
	@Override
	public Cover getCover() 
	{
		return cover;
	}
	
}
