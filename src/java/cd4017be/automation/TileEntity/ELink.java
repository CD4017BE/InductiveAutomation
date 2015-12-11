/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cd4017be.automation.TileEntity;

import java.io.DataInputStream;
import java.io.IOException;

import cd4017be.api.automation.IEnergy;
import cd4017be.api.automation.PipeEnergy;
import cd4017be.api.energy.EnergyAPI;
import cd4017be.api.energy.EnergyThermalExpansion;
import cd4017be.api.energy.IEnergyAccess;
import cd4017be.automation.Config;
import cd4017be.lib.BlockItemRegistry;
import cd4017be.lib.TileContainer;
import cd4017be.lib.TileEntityData;
import cd4017be.lib.templates.AutomatedTile;
import cd4017be.lib.util.Utils;
import cofh.api.energy.IEnergyHandler;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

/**
 *
 * @author CD4017BE
 */
public class ELink extends AutomatedTile implements IEnergy, IEnergyHandler
{

    @Override
    public int receiveEnergy(ForgeDirection dir, int e, boolean sim) 
    {
        if (energy == null) return 0;
    	if (energyDemand <= 0) return 0;
        double x = energy.getEnergy(energy.Umax, 1);
        if (energyDemand + x > 0) energyDemand = x;
        if ((double)e * EnergyThermalExpansion.E_Factor > energyDemand) e = (int)Math.floor(energyDemand / EnergyThermalExpansion.E_Factor);
        if (!sim) {
        	x = (double)e * EnergyThermalExpansion.E_Factor;
        	energy.addEnergy(x);
        	netData.floats[3] -= x;
        }
        return e;
    }

    @Override
    public int extractEnergy(ForgeDirection dir, int e, boolean sim) 
    {
    	if (energy == null) return 0;
        if (energyDemand >= 0) return 0;
        double x = energy.getEnergy(0, 1);
        if (energyDemand + x < 0) energyDemand = -x;
        if ((double)e * EnergyThermalExpansion.E_Factor > -energyDemand) e = (int)Math.floor(-energyDemand / EnergyThermalExpansion.E_Factor);
        if (!sim) {
        	x = (double)e * EnergyThermalExpansion.E_Factor;
        	energy.addEnergy(-x);
        	netData.floats[3] += x;
        }
        return e;
    }

    @Override
    public int getEnergyStored(ForgeDirection dir) 
    {
    	if (energy == null) return 0;
        return (int)(energy.getEnergy(0, 1) / EnergyThermalExpansion.E_Factor);
    }

    @Override
    public int getMaxEnergyStored(ForgeDirection dir) 
    {
    	if (energy == null) return 0;
        return (int)((float)energy.Umax * (float)energy.Umax / EnergyThermalExpansion.E_Factor);
    }
    
    public ELink()
    {
        //energy = new PipeEnergy(Config.Umax[1], Config.Rcond[1]);
        netData = new TileEntityData(0, 3, 4, 0);
    }
    
    public int type = 0;
    public double energyDemand = 0;
    
    protected void setWireType()
    {
        Block id = worldObj.getBlock(xCoord, yCoord, zCoord);
        type = id == BlockItemRegistry.blockId("tile.link") ? 1 : 2;
    }

    @Override
    public void updateEntity() 
    {
    	if (energy == null) 
        {
            this.setWireType();
            energy = new PipeEnergy(Config.Umax[type], Config.Rcond[type]);
        }
    	super.updateEntity();
        if (this.worldObj.isRemote) return;
        boolean active = (isRedstoneEnabled() ? worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord) : false) ^ (netData.ints[2] & 1) != 0;
        int s = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
        IEnergyAccess storage = EnergyAPI.getAccess(Utils.getTileOnSide(this, (byte)s));
        netData.floats[0] = (float)energy.Ucap;
        netData.floats[1] = storage.getStorage(s^1);
        netData.floats[2] = storage.getCapacity(s^1);
        double Uref = netData.floats[2] > 0 ? netData.ints[0] + (netData.ints[1] - netData.ints[0]) * (netData.floats[1] / netData.floats[2]) : (netData.ints[0] + netData.ints[1]) / 2F;
        if (active) {
        	float e = (float)energy.getEnergy(Uref, 1);
        	energyDemand = -e;
            e = storage.addEnergy(e, s^1);
        	if (e != 0) {
        		energy.addEnergy(-e);
        		energyDemand += e;
        	}
        	netData.floats[3] = e;
        } else {
        	energyDemand = 0;
        	netData.floats[3] = 0;
        }
    }
    
    public boolean isRedstoneEnabled()
    {
        return (netData.ints[2] & 2) != 0;
    }

    public int getStorageScaled(int s)
    {
    	if (netData.floats[1] <= 0 || netData.floats[2] <= 0) return 0;
    	else if (netData.floats[1] >= netData.floats[2]) return s;
    	return (int)(netData.floats[1] / netData.floats[2] * s);
    }
    
    public int getVoltageScaled(int s)
    {
    	float f = 0;
    	if (netData.ints[0] < netData.ints[1]) {
    		f = (netData.floats[0] - netData.ints[0]) / (netData.ints[1] - netData.ints[0]);
    	} else if (netData.ints[0] > netData.ints[1]) {
    		f = 1F - (netData.floats[0] - netData.ints[1]) / (netData.ints[0] - netData.ints[1]);
    	}
    	if (f > 1) f = 1F;
    	if (f < 0) f = 0F;
    	return (int)(s * f);
    }
    
    @Override
	protected void customPlayerCommand(byte cmd, DataInputStream dis, EntityPlayerMP player) throws IOException 
    {
    	if (cmd >= 0 && cmd < 3) netData.ints[cmd] = dis.readInt();
	}

	@Override
    public void writeToNBT(NBTTagCompound nbt) 
    {
        super.writeToNBT(nbt);
        nbt.setByte("type", (byte)type);
        nbt.setByte("mode", (byte)netData.ints[2]);
        nbt.setInteger("Umin", netData.ints[0]);
        nbt.setInteger("Umax", netData.ints[1]);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) 
    {
    	type = nbt.getByte("type");
        energy = new PipeEnergy(Config.Umax[type], Config.Rcond[type]);
        super.readFromNBT(nbt);
        netData.ints[2] = nbt.getByte("mode");
        netData.ints[0] = nbt.getInteger("Umin");
        netData.ints[1] = nbt.getInteger("Umax");
    }
    
    @Override
    public void initContainer(TileContainer container)
    {
        container.addPlayerInventory(8, 86);
    }

    @Override
    public PipeEnergy getEnergy(byte side) 
    {
        if (side == worldObj.getBlockMetadata(xCoord, yCoord, zCoord)) return PipeEnergy.empty;
        else return this.energy;
    }

	@Override
	public boolean canConnectEnergy(ForgeDirection dir) {
		return dir.ordinal() == worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
	}
    
}
