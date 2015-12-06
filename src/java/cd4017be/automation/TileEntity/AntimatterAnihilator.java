/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cd4017be.automation.TileEntity;

import java.io.DataInputStream;
import java.io.IOException;

import cd4017be.api.automation.IEnergy;
import cd4017be.api.automation.IEnergyStorage;
import cd4017be.api.automation.PipeEnergy;
import cd4017be.automation.Automation;
import cd4017be.automation.Config;
import cd4017be.lib.TileContainer;
import cd4017be.lib.TileEntityData;
import cd4017be.lib.templates.AutomatedTile;
import cd4017be.lib.templates.Inventory;
import cd4017be.lib.templates.SlotTank;
import cd4017be.lib.templates.TankContainer;
import cd4017be.lib.templates.TankContainer.Tank;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidHandler;

/**
 *
 * @author CD4017BE
 */
public class AntimatterAnihilator extends AutomatedTile implements ISidedInventory, IFluidHandler, IEnergy, IEnergyStorage
{
    public static final int AMEnergy = 90000;
    private static final int MaxPower = 160;
    private static final int AMHeat = 1;
    public static final int MaxTemp = 800;
    private static final int CoolRes = 5;
    
    public AntimatterAnihilator()
    {
        netData = new TileEntityData(1, 4, 1, 3);
        energy = new PipeEnergy(Config.Umax[2], Config.Rcond[2]);
        tanks = new TankContainer(this, new Tank(Config.tankCap[1], -1, Automation.L_heliumL).setIn(0), new Tank(Config.tankCap[1], 1, Automation.L_heliumG).setOut(1), new Tank(Config.tankCap[1], -1, Automation.L_antimatter).setIn(2));
        inventory = new Inventory(this, 3).setInvName("Antimatter Anihilator");
        /**
         * long: tanks
         * int: voltage, power, drop, heat
         * float: storage
         * fluid: Red, Ox
         */
        netData.ints[3] = MaxTemp;
    }
    
    @Override
    public void updateEntity() 
    {
		super.updateEntity();
		if (worldObj.isRemote) return;
		//energy
	        double e = energy.getEnergy(netData.ints[0], 1);
	        double e1 = this.addEnergy(e);
	        if (e != e1) energy.addEnergy(-e1);
	        else energy.Ucap = netData.ints[0];
	        //cooling
	        if (netData.ints[3] > 0) {
	            int c = (netData.ints[3] - 1) / CoolRes + 1;	
	            if (netData.ints[2] < c && tanks.getAmount(0) > 0) {
	                netData.ints[2] += 800;
	                tanks.drain(0, 1, true);
	            }
	            if (c > netData.ints[2]) c = netData.ints[2];
	            netData.ints[3] -= c;
	            netData.ints[2] -= c;
	            tanks.fill(1, new FluidStack(Automation.L_heliumG, c), true);
	        }
		//antimatter
		int p = Math.min((MaxTemp - netData.ints[3]) / AMHeat, (int)Math.ceil((1F - netData.floats[0] / this.getCapacity() * 2F) * MaxPower));
		if (p > tanks.getAmount(2)) p = tanks.getAmount(2);
		if (p > 0) {
	            tanks.drain(2, p, true);
	            netData.ints[3] += p * AMHeat;
	            netData.floats[0] += p * AMEnergy;
		} else p = 0;
		netData.ints[1] = p;
    }
	
    public int getStorageScaled(int s)
    {
        return (int)(netData.floats[0] * (double)s / this.getCapacity());
    }
	
    public int getHeatScaled(int s)
    {
        return netData.ints[3] * s / MaxTemp;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) 
    {
        super.readFromNBT(nbt);
        netData.floats[0] = nbt.getFloat("storage");
        netData.ints[0] = nbt.getInteger("voltage");
        netData.ints[3] = nbt.getInteger("heat");
        netData.ints[2] = nbt.getInteger("drop");
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) 
    {
        super.writeToNBT(nbt);
        nbt.setFloat("storage", netData.floats[0]);
        nbt.setInteger("voltage", netData.ints[0]);
        nbt.setInteger("heat", netData.ints[3]);
        nbt.setInteger("drop", netData.ints[2]);
    }

    @Override
    protected void customPlayerCommand(byte cmd, DataInputStream dis, EntityPlayerMP player) throws IOException 
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
        container.addEntitySlot(new SlotTank(this, 2, 69, 34));
        
        container.addPlayerInventory(8, 86);
    }

    @Override
    public double getEnergy() 
    {
        return netData.floats[0];
    }

    @Override
    public double getCapacity() {
        return Config.Ecap[1] * 1000D;
    }

    @Override
    public double addEnergy(double e) 
    {
        netData.floats[0] += e;
        if (netData.floats[0] < 0) {
            e -= netData.floats[0];
            netData.floats[0] = 0;
        } else if (netData.floats[0] > this.getCapacity()) {
            e -= netData.floats[0] - this.getCapacity();
            netData.floats[0] = (float)this.getCapacity();
        }
        return e;
    }
    
}
