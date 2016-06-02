/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cd4017be.automation.TileEntity;

import net.minecraft.network.PacketBuffer;

import java.io.IOException;

import cofh.api.energy.IEnergyProvider;
import cofh.api.energy.IEnergyReceiver;
import cd4017be.api.automation.IEnergy;
import cd4017be.api.automation.PipeEnergy;
import cd4017be.api.energy.EnergyAPI;
import cd4017be.api.energy.EnergyAPI.IEnergyAccess;
import cd4017be.automation.Config;
import cd4017be.automation.Objects;
import cd4017be.lib.TileContainer;
import cd4017be.lib.TileEntityData;
import cd4017be.lib.templates.AutomatedTile;
import cd4017be.lib.util.Utils;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import static cd4017be.api.energy.EnergyAPI.RF_value;

/**
 *
 * @author CD4017BE
 */
public class ELink extends AutomatedTile implements IEnergy, IEnergyReceiver, IEnergyProvider
{
    
    public ELink() {
        netData = new TileEntityData(0, 3, 4, 0);
    }
    
    public int type = 0;
    public double energyDemand = 0, energyMoved = 0;
    
    protected void setWireType()
    {
        Block id = this.getBlockType();
        type = id == Objects.link ? 1 : 2;
    }

    @Override
    public void update() 
    {
    	if (energy == null) 
        {
            this.setWireType();
            energy = new PipeEnergy(Config.Umax[type], Config.Rcond[type]);
        }
    	super.update();
        if (this.worldObj.isRemote) return;
        boolean active = (isRedstoneEnabled() ? worldObj.isBlockPowered(getPos()) : false) ^ (netData.ints[2] & 1) != 0;
        int s = this.getOrientation();
        IEnergyAccess storage = EnergyAPI.get(Utils.getTileOnSide(this, (byte)s));
        netData.floats[0] = (float)energy.Ucap;
        netData.floats[1] = (float)storage.getStorage(s^1);
        netData.floats[2] = (float)storage.getCapacity(s^1);
        double Uref = netData.floats[2] > 0 ? netData.ints[0] + (netData.ints[1] - netData.ints[0]) * (netData.floats[1] / netData.floats[2]) : (netData.ints[0] + netData.ints[1]) / 2F;
        if (active) {
        	double e = energy.getEnergy(Uref, 1);
        	energyMoved = storage.addEnergy(e, s^1);
        	energyDemand = energyMoved - e;
        	if (energyDemand == 0) energy.Ucap = Uref;
        	else if (energyMoved != 0) energy.addEnergy(-energyMoved);
        } else {
        	energyDemand = energyMoved = 0;
        }
        netData.floats[3] = (float)energyMoved; energyMoved = 0;
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
	protected void customPlayerCommand(byte cmd, PacketBuffer dis, EntityPlayerMP player) throws IOException 
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
        if (side == this.getOrientation()) return PipeEnergy.empty;
        else return this.energy;
    }

    //RedstoneFlux
    
    @Override
    public int receiveEnergy(EnumFacing dir, int e, boolean sim) {
        if (energy == null || energyDemand <= 0) return 0;
        double x = energy.getEnergy(energy.Umax, 1);
        if (energyDemand + x > 0) energyDemand = -x;
        if ((x = (double)e * RF_value) > energyDemand) e = (int)Math.floor((x = energyDemand) / RF_value);
        if (!sim) {
        	energy.addEnergy(x);
        	energyDemand -= x;
        	energyMoved += x;
        }
        return e;
    }

    @Override
    public int extractEnergy(EnumFacing dir, int e, boolean sim) {
    	if (energy == null || energyDemand >= 0) return 0;
        double x = energy.getEnergy(0, 1);
        if (energyDemand + x < 0) energyDemand = -x;
        if ((x = -(double)e * RF_value) < energyDemand) e = (int)Math.floor(-(x = energyDemand) / RF_value);
        if (!sim) {
        	energy.addEnergy(x);
        	energyDemand -= x;
        	energyMoved += x;
        }
        return e;
    }

    @Override
    public int getEnergyStored(EnumFacing dir) {
        return energy == null ? 0 : (int)(energy.getEnergy(0, 1) / RF_value);
    }

    @Override
    public int getMaxEnergyStored(EnumFacing dir) {
        return energy == null ? 0 : (int)Math.floor(energy.Umax * energy.Umax / RF_value);
    }
    
	@Override
	public boolean canConnectEnergy(EnumFacing dir) {
		return dir.getIndex() == this.getOrientation();
	}
}
