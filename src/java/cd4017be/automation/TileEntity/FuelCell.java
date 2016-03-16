package cd4017be.automation.TileEntity;

import net.minecraft.network.PacketBuffer;

import java.io.IOException;

import cd4017be.api.automation.IEnergy;
import cd4017be.api.automation.PipeEnergy;
import cd4017be.api.energy.EnergyAPI.IEnergyAccess;
import cd4017be.automation.Config;
import cd4017be.automation.Objects;
import cd4017be.lib.TileContainer;
import cd4017be.lib.TileEntityData;
import cd4017be.lib.templates.AutomatedTile;
import cd4017be.lib.templates.Inventory;
import cd4017be.lib.templates.SlotTank;
import cd4017be.lib.templates.TankContainer;
import cd4017be.lib.templates.TankContainer.Tank;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidHandler;

public class FuelCell extends AutomatedTile implements IFluidHandler, IEnergy, IEnergyAccess
{

    public FuelCell()
    {
        netData = new TileEntityData(1, 2, 1, 3);
        energy = new PipeEnergy(Config.Umax[2], Config.Rcond[2]);
		inventory = new Inventory(this, 3);
        tanks = new TankContainer(this, new Tank(Config.tankCap[1], -1, Objects.L_hydrogenG).setIn(0), new Tank(Config.tankCap[1], -1, Objects.L_oxygenG).setIn(1), new Tank(Config.tankCap[1], 1, Objects.L_steam).setOut(2));
		/**
		 * long: tanks
		 * int: voltage, power, mlHydrogen, mlOxygen
		 * float: storage
		 * fluid: Red, Ox
		 */	
    }
	
    @Override
    public void update() 
    {
    	super.update();
    	if (worldObj.isRemote) return;
    	double e = energy.getEnergy(netData.ints[0], 1);
    	double e1 = this.addEnergy(e, 0);
    	if (e1 != e) energy.addEnergy(-e1);
        else energy.Ucap = netData.ints[0];
    	int p = (int)Math.ceil((1F - netData.floats[0] / (Config.Ecap[0] * 1000F) * 2F) * Config.PfuelCell * 0.5F);
    	if (p * 2 > tanks.getAmount(0)) p = tanks.getAmount(0) / 2;
    	if (p > tanks.getAmount(1)) p = tanks.getAmount(1);
    	if (p > 0) {
            tanks.drain(0, p * 2, true);
            tanks.drain(1, p, true);
            tanks.fill(2, new FluidStack(Objects.L_steam, p), true);
            netData.floats[0] += p * 1800F;
    	} else p = 0;
    	netData.ints[1] = p * 2;
    }
	
    public int getStorageScaled(int s)
    {
        return (int)(netData.floats[0] * s / (Config.Ecap[0] * 1000));
    }
	
    public int getPowerScaled(int s)
    {
        return netData.ints[1] * s / Config.PfuelCell;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) 
    {
        super.readFromNBT(nbt);
	netData.floats[0] = nbt.getFloat("storage");
	netData.ints[0] = nbt.getInteger("voltage");
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) 
    {
        super.writeToNBT(nbt);
        nbt.setFloat("storage", netData.floats[0]);
        nbt.setInteger("voltage", netData.ints[0]);
    }

    @Override
    protected void customPlayerCommand(byte cmd, PacketBuffer dis, EntityPlayerMP player) throws IOException 
    {
        if (cmd == 0) {
            netData.ints[0] = dis.readShort();
        }
    }

    @Override
    public void initContainer(TileContainer container) 
    {
    	container.addEntitySlot(new SlotTank(this, 0, 8, 34));
    	container.addEntitySlot(new SlotTank(this, 1, 26, 34));
    	container.addEntitySlot(new SlotTank(this, 2, 62, 34));
    	
        container.addPlayerInventory(8, 86);
    }

    @Override
    public double getStorage(int s) 
    {
        return netData.floats[0];
    }

    @Override
    public double getCapacity(int s) {
        return Config.Ecap[0] * 1000D;
    }

    @Override
    public double addEnergy(double e, int s) 
    {
        netData.floats[0] += e;
        if (netData.floats[0] < 0) {
            e -= netData.floats[0];
            netData.floats[0] = 0;
        } else if (netData.floats[0] > Config.Ecap[0] * 1000) {
            e -= netData.floats[0] - Config.Ecap[0] * 1000;
            netData.floats[0] = Config.Ecap[0] * 1000;
        }
        return e;
    }
    
}
