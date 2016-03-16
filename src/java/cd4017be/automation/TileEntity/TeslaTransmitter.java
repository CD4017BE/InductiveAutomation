/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cd4017be.automation.TileEntity;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;

import java.io.IOException;
import java.util.ArrayList;

import cd4017be.api.automation.IEnergy;
import cd4017be.api.automation.ITeslaTransmitter;
import cd4017be.api.automation.PipeEnergy;
import cd4017be.api.automation.TeslaNetwork;
import cd4017be.automation.Config;
import cd4017be.lib.BlockItemRegistry;
import cd4017be.lib.ModTileEntity;
import cd4017be.lib.TileEntityData;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;

/**
 *
 * @author CD4017BE
 */
public class TeslaTransmitter extends ModTileEntity implements IEnergy, ITeslaTransmitter, ITickable
{
    public boolean interdim = false;
    public PipeEnergy energy = new PipeEnergy(this.getMaxVoltage(), 0);
    private double addEnergy = 0;
    
    public TeslaTransmitter()
    {
    	netData = new TileEntityData(0, 1, 1, 0);
    }
    
    protected int getMaxVoltage() {
    	return Config.Umax[4];
    }
    
    @Override
    public void update() 
    {
        if (worldObj.isRemote) return;
        if (addEnergy != 0) energy.addEnergy(addEnergy);
        addEnergy = 0;
        netData.floats[0] = (float)(energy.Ucap * energy.Ucap);
        TeslaNetwork.instance.transmittEnergy(this);
    }
    
    private void changeFrequency(short nf)
    {
        if (nf == netData.ints[0]) return;
        TeslaNetwork.instance.remove(this);
        netData.ints[0] = nf;
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
        double e = energy.getEnergy(Uref, dst);
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
        if (p[3] == worldObj.provider.getDimensionId())
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
    public boolean onActivated(EntityPlayer player, EnumFacing s, float X, float Y, float Z) 
    {
        ItemStack item = player.getCurrentEquippedItem();
        if (!interdim && item != null)
        {
            if (item.isItemEqual(BlockItemRegistry.stack("EMatrix", 1)))
            {
                interdim = true;
            } else return false;
            item.stackSize--;
            if (item.stackSize <= 0) item = null;
            player.setCurrentItemOrArmor(0, item);
            return true;
        } else
        if (interdim && item == null && player.isSneaking())
        {
            player.inventory.addItemStackToInventory(BlockItemRegistry.stack("EMatrix", 1));
            interdim = false;
            return true;
        } else
        return super.onActivated(player, s, X, Y, Z);
    }
    
    @Override
	public void onPlaced(EntityLivingBase entity, ItemStack item) 
    {
		if (item.stackTagCompound != null) energy.Ucap = item.stackTagCompound.getDouble("voltage");
	}

	@Override
	public ArrayList<ItemStack> dropItem(IBlockState state, int fortune) 
	{
		ArrayList<ItemStack> list = new ArrayList<ItemStack>();
		ItemStack item = new ItemStack(this.getBlockType(), 1);
		if (energy.Ucap >= 1.0D) {
			item.stackTagCompound = new NBTTagCompound();
			item.stackTagCompound.setDouble("voltage", energy.Ucap);
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
    public void writeToNBT(NBTTagCompound nbt) 
    {
        super.writeToNBT(nbt);
        if (addEnergy != 0) energy.addEnergy(addEnergy);
        addEnergy = 0;
        energy.writeToNBT(nbt, "wire");
        nbt.setShort("frequency", (short)netData.ints[0]);
        nbt.setBoolean("interdim", interdim);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) 
    {
        super.readFromNBT(nbt);
        energy.readFromNBT(nbt, "wire");
        netData.ints[0] = nbt.getShort("frequency");
        interdim = nbt.getBoolean("interdim");
        TeslaNetwork.instance.register(this);
    }

    @Override
    public PipeEnergy getEnergy(byte side) 
    {
        if (side == 0){
            if (addEnergy != 0) energy.addEnergy(addEnergy);
            addEnergy = 0;
            return energy;
        }
        else return null;
    }
    
    public short getFrequency()
    {
    	return (short)netData.ints[0];
    }

	@Override
	public double getVoltage() 
	{
		return energy.Ucap;
	}

	@Override
	public int[] getLocation() 
	{
		return new int[]{pos.getX(), pos.getY(), pos.getZ(), worldObj.provider.getDimensionId()};
	}
    
}
