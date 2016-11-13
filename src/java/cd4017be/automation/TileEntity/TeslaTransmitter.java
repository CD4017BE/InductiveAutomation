package cd4017be.automation.TileEntity;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;

import java.io.IOException;
import java.util.ArrayList;

import cd4017be.api.Capabilities;
import cd4017be.api.automation.ITeslaTransmitter;
import cd4017be.api.automation.PipeEnergy;
import cd4017be.api.automation.TeslaNetwork;
import cd4017be.automation.Config;
import cd4017be.lib.BlockItemRegistry;
import cd4017be.lib.ModTileEntity;
import cd4017be.lib.Gui.DataContainer;
import cd4017be.lib.Gui.DataContainer.IGuiData;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.capabilities.Capability;

/**
 *
 * @author CD4017BE
 */
public class TeslaTransmitter extends ModTileEntity implements ITeslaTransmitter, ITickable, IGuiData
{
	public boolean interdim = false;
	public PipeEnergy energy = new PipeEnergy(this.getMaxVoltage(), 0);
	private float addEnergy = 0;
	public int freq;
	public float Estor;
	
	protected int getMaxVoltage() {
		return Config.Umax[4];
	}
	
	@Override
	public void update() 
	{
		if (worldObj.isRemote) return;
		if (addEnergy != 0) energy.addEnergy(addEnergy);
		addEnergy = 0;
		Estor = (float)(energy.Ucap * energy.Ucap);
		TeslaNetwork.instance.transmittEnergy(this);
	}
	
	private void changeFrequency(short nf)
	{
		if (nf == freq) return;
		TeslaNetwork.instance.remove(this);
		freq = nf;
		TeslaNetwork.instance.register(this);
	}
	
	public double addEnergy(double e)
	{
		double s = addEnergy + energy.Ucap * energy.Ucap;
		if (s + e < 0) e = -s;
		else if (s + e > energy.Umax * energy.Umax) e = energy.Umax * energy.Umax - s;
		addEnergy += e;
		return e;
	}
	
	public double getPower(double dst, double Uref)
	{
		if (dst < 8) dst = 8;
		double e = energy.getEnergy((float)Uref, (float)dst);
		double s = addEnergy + energy.Ucap * energy.Ucap;
		if (s - e < 0) e = s;
		else if (s - e > energy.Umax * energy.Umax) e = s - energy.Umax * energy.Umax;
		return e;
	}
	
	public boolean checkAlive()
	{
		return !this.isInvalid() && worldObj.isBlockLoaded(pos) && worldObj.getTileEntity(pos) == this;
	}
	
	public double getSqDistance(ITeslaTransmitter te)
	{
		int[] p = te.getLocation();
		double d;
		if (p[3] == worldObj.provider.getDimension())
		{
			int dx = pos.getX() - p[0];
			int dy = pos.getY() - p[1];
			int dz = pos.getZ() - p[2];
			d = (double)(dx*dx + dy*dy + dz*dz);
		}
		else d = Double.POSITIVE_INFINITY;
		if (interdim && d > Config.m_interdimDist) d = (double)Config.m_interdimDist;
		return d;
	}

	@Override
	public boolean onActivated(EntityPlayer player, EnumHand hand, ItemStack item, EnumFacing s, float X, float Y, float Z) 
	{
		if (!interdim && item != null)
		{
			if (item.isItemEqual(BlockItemRegistry.stack("m.EMatrix", 1)))
			{
				interdim = true;
			} else return false;
			item.stackSize--;
			if (item.stackSize <= 0) item = null;
			player.setHeldItem(hand, item);
			return true;
		} else
		if (interdim && item == null && player.isSneaking())
		{
			player.inventory.addItemStackToInventory(BlockItemRegistry.stack("m.EMatrix", 1));
			interdim = false;
			return true;
		} else
		return super.onActivated(player, hand, item, s, X, Y, Z);
	}
	
	@Override
	public void onPlaced(EntityLivingBase entity, ItemStack item) 
	{
		if (item.getTagCompound() != null) energy.Ucap = (float)item.getTagCompound().getDouble("voltage");
	}

	@Override
	public ArrayList<ItemStack> dropItem(IBlockState state, int fortune) 
	{
		ArrayList<ItemStack> list = new ArrayList<ItemStack>();
		ItemStack item = new ItemStack(this.getBlockType(), 1);
		if (energy.Ucap >= 1.0D) {
			item.setTagCompound(new NBTTagCompound());
			item.getTagCompound().setDouble("voltage", energy.Ucap);
		}
		list.add(item);
		return list;
	}

	@Override
	public void onPlayerCommand(PacketBuffer dis, EntityPlayerMP player) throws IOException 
	{
		changeFrequency(dis.readShort());
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) 
	{
		if (addEnergy != 0) energy.addEnergy(addEnergy);
		addEnergy = 0;
		energy.writeToNBT(nbt, "wire");
		nbt.setShort("frequency", (short)freq);
		nbt.setBoolean("interdim", interdim);
		return super.writeToNBT(nbt);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) 
	{
		super.readFromNBT(nbt);
		energy.readFromNBT(nbt, "wire");
		freq = nbt.getShort("frequency");
		interdim = nbt.getBoolean("interdim");
		TeslaNetwork.instance.register(this);
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		if (capability == Capabilities.ELECTRIC_CAPABILITY) return facing == EnumFacing.DOWN;
		return super.hasCapability(capability, facing);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		if (capability == Capabilities.ELECTRIC_CAPABILITY && facing == EnumFacing.DOWN) {
			if (addEnergy != 0) energy.addEnergy(addEnergy);
			addEnergy = 0;
			return (T)energy;
		}
		return super.getCapability(capability, facing);
	}

	public short getFrequency()
	{
		return (short)freq;
	}

	@Override
	public double getVoltage() 
	{
		return energy.Ucap;
	}

	@Override
	public int[] getLocation() 
	{
		return new int[]{pos.getX(), pos.getY(), pos.getZ(), worldObj.provider.getDimension()};
	}

	@Override
	public void initContainer(DataContainer container) {
	}

	@Override
	public int[] getSyncVariables() {
		return new int[]{freq, Float.floatToIntBits(Estor)};
	}

	@Override
	public void setSyncVariable(int i, int v) {
		switch(i) {
		case 0: freq = v; break;
		case 1: Estor = Float.intBitsToFloat(v); break;
		}
	}

}
