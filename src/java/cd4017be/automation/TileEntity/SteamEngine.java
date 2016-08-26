/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cd4017be.automation.TileEntity;


import cd4017be.api.automation.IEnergy;
import cd4017be.api.automation.PipeEnergy;
import cd4017be.automation.Config;
import cd4017be.automation.Objects;
import cd4017be.lib.Gui.SlotTank;
import cd4017be.lib.Gui.TileContainer;
import cd4017be.lib.Gui.TileContainer.TankSlot;
import cd4017be.lib.TileEntityData;
import cd4017be.lib.templates.AutomatedTile;
import cd4017be.lib.templates.Inventory;
import cd4017be.lib.templates.TankContainer;
import cd4017be.lib.templates.TankContainer.Tank;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidHandler;

/**
 *
 * @author CD4017BE
 */
public class SteamEngine extends AutomatedTile implements ISidedInventory, IEnergy, IFluidHandler
{
    
    protected int getTier() {return 0;}
    public float getPower() {return Config.PsteamGen[0];}
    protected float getDissipation() {return 0.1F;}
    
    public SteamEngine()
    {
        netData = new TileEntityData(2, 0, 1, 2);
        energy = new PipeEnergy(Config.Umax[this.getTier()], Config.Rcond[this.getTier()]);
        inventory = new Inventory(this, 2);
        tanks = new TankContainer(this, new Tank(Config.tankCap[1], -1, Objects.L_steam).setIn(1), new Tank(Config.tankCap[1], 1, Objects.L_waterG).setOut(0)).setNetLong(1);
        
    }
    
    @Override
    public void update() 
    {
    	super.update();
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
            tanks.drain(0, p, true);
            tanks.fill(1, new FluidStack(Objects.L_waterG, p * 8), true);
            netData.floats[0] += (float)p * (float)Config.E_Steam;
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
    public void initContainer(TileContainer container)
    {
        container.addItemSlot(new SlotTank(this, 0, 89, 34));
        container.addItemSlot(new SlotTank(this, 1, 17, 34));
        
        container.addPlayerInventory(8, 86);
        
        container.addTankSlot(new TankSlot(tanks, 0, 8, 16, true));
        container.addTankSlot(new TankSlot(tanks, 1, 80, 16, true));
    }
    
    @Override
    public int[] stackTransferTarget(ItemStack item, int s, TileContainer container) 
    {
        int[] pi = container.getPlayerInv();
        if (s < pi[0]) return pi;
        else return new int[]{0, 1};
    }
    
}
