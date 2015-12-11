/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cd4017be.automation.TileEntity;

import cd4017be.api.automation.IEnergy;
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
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.IFluidHandler;

/**
 *
 * @author CD4017BE
 */
public class SteamEngine extends AutomatedTile implements ISidedInventory, IEnergy, IFluidHandler
{
    protected short aWater;
    
    protected int getTier() {return 0;}
    public float getPower() {return Config.PsteamGen[0];}
    protected float getDissipation() {return 0.1F;}
    
    public SteamEngine()
    {
        netData = new TileEntityData(2, 0, 1, 2);
        energy = new PipeEnergy(Config.Umax[this.getTier()], Config.Rcond[this.getTier()]);
        inventory = new Inventory(this, 2);
        tanks = new TankContainer(this, new Tank(Config.tankCap[1], -1, Automation.L_steam).setIn(1), new Tank(Config.tankCap[1], 1, Automation.L_water).setOut(0)).setNetLong(1);
        
    }
    
    @Override
    public void updateEntity() 
    {
        super.updateEntity();
        if (this.worldObj.isRemote) return;
        float e = this.getEnergyOut();
        if (e > 0) {
            energy.addEnergy(e * 1000F);
            netData.floats[0] -= e;
        }
        int p = (int)Math.ceil(Math.min((float)tanks.getAmount(0) * 2F / (float)tanks.tanks[0].cap, 1F) * this.getPower() / Config.E_Steam);
        if (p > tanks.getAmount(0)) p = tanks.getAmount(0);
        if (p > 0)
        {
            aWater += p * 1000 / Config.steam_waterOut;
            tanks.drain(0, p, true);
            netData.floats[0] += (float)p * (float)Config.E_Steam;
            if (aWater >= 1000)
            {
                tanks.fill(1, Automation.getLiquid(Automation.L_water, aWater / 1000), true);
                aWater %= 1000;
            }
        }
    }
    
    public int getPowerScaled(int s)
    {
    	return (int)(this.getEnergyOut() * s / this.getPower());
    }
    
    public float getEnergyOut()
    {
    	return netData.floats[0] > 1F ? netData.floats[0] * this.getDissipation() : netData.floats[0];
    }
    
    public int getEnergy()
    {
        return netData.ints[0];
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) 
    {
        super.writeToNBT(nbt);
        nbt.setByte("drop", (byte)aWater);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) 
    {
        super.readFromNBT(nbt);
        aWater = nbt.getByte("drop");
    }
    
    @Override
    public void initContainer(TileContainer container)
    {
        container.addEntitySlot(new SlotTank(this, 0, 89, 34));
        container.addEntitySlot(new SlotTank(this, 1, 17, 34));
        
        container.addPlayerInventory(8, 86);
    }
    
    @Override
    public int[] stackTransferTarget(ItemStack item, int s, TileContainer container) 
    {
        int[] pi = container.getPlayerInv();
        if (s < pi[0]) return pi;
        else return new int[]{0, 1};
    }
    
}