/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cd4017be.automation.TileEntity;

import java.io.DataInputStream;
import java.io.IOException;

import cd4017be.automation.Automation;
import cd4017be.automation.Config;
import cd4017be.lib.TileContainer;
import cd4017be.lib.TileEntityData;
import cd4017be.lib.templates.AutomatedTile;
import cd4017be.lib.templates.Inventory;
import cd4017be.lib.templates.Inventory.Component;
import cd4017be.lib.templates.SlotTank;
import cd4017be.lib.templates.TankContainer;
import cd4017be.lib.templates.TankContainer.Tank;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraftforge.fluids.IFluidHandler;

/**
 *
 * @author CD4017BE
 */
public class SteamBoiler extends AutomatedTile implements ISidedInventory, IFluidHandler
{
    public byte waterType;
    
    public SteamBoiler()
    {
        netData = new TileEntityData(2, 5, 0, 2);
        inventory = new Inventory(this, 5, new Component(0, 3, -1)).setInvName("Steam Boiler");
        tanks = new TankContainer(this, new Tank(Config.tankCap[1], -1, Automation.L_water, Automation.L_biomass).setIn(3), new Tank(Config.tankCap[1], 1, Automation.L_steam).setOut(4)).setNetLong(1);
        
        netData.ints[4] = 1;
    }

    @Override
    public void updateEntity() 
    {
        super.updateEntity();
        if (this.worldObj.isRemote) return;
        int steamOut = 0;
        if (netData.ints[1] > 0)
        {
        	int d = Math.min(netData.ints[4], netData.ints[1]);
            netData.ints[1] -= d;
            netData.ints[2] += d;
            steamOut += Config.Lsteam_Kfuel_burning * netData.ints[4];
            if (netData.ints[2] > Config.maxK_steamBoiler) netData.ints[2] = Config.maxK_steamBoiler;
        }
        if (netData.ints[1] <= 0 && netData.ints[2] < Config.maxK_steamBoiler)
        {
            burnItem();
        }
        int nw = tanks.getFluid(0) == null || tanks.getFluid(0).getFluid().equals(Automation.L_water) ? Config.get_LWater_Cooking() : Config.get_LBiomass_Cooking();
        if (netData.ints[2] >= Config.K_Cooking_steamBoiler && tanks.getAmount(0) >= nw)
        {
            netData.ints[3]++;
            if (netData.ints[3] >= getCookTime())
            {
                if (tanks.getFluid(0).getFluid().equals(Automation.L_water))
                {
                    netData.ints[2] -= Config.K_Cooking_steamBoiler;
                } else
                {
                    steamOut += Config.steam_biomass_burning;
                }
                tanks.drain(0, nw, true);
                steamOut += Config.get_LSteam_Cooking();
                netData.ints[3] = 0;
            }
        }
        tanks.fill(1, Automation.getLiquid(Automation.L_steam, steamOut), true);
    }
    
    private void burnItem()
    {
        if (worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord)) return;
        for (int i = 0; i < inventory.items.length; i++)
        {
            if (inventory.items[i] == null) continue;
            int n = TileEntityFurnace.getItemBurnTime(inventory.items[i]);
            if (n != 0)
            {
                netData.ints[0] = n;
                netData.ints[1] += netData.ints[0];
                this.decrStackSize(i, 1);
                return;
            }
        }
    }
    
    public int getBurnScaled(int s)
    {
        if (netData.ints[0] == 0) return 0;
        return netData.ints[1] * s / netData.ints[0];
    }
    
    public int getCookScaled(int s)
    {
        int i = netData.ints[3] * s / getCookTime();
        if (i > s) i = s;
        return i;
    }
    
    private int getCookTime()
    {
        if (netData.ints[2] == 0) return Integer.MAX_VALUE;
        return 40000 / netData.ints[2];
    }
    
    public int getTempScaled(int s)
    {
        return netData.ints[2] * s / Config.maxK_steamBoiler;
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) 
    {
        super.writeToNBT(nbt);
        nbt.setInteger("burn", netData.ints[1]);
        nbt.setInteger("cook", netData.ints[3]);
        nbt.setInteger("temp", netData.ints[2]);
        nbt.setInteger("blow", netData.ints[4]);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) 
    {
        super.readFromNBT(nbt);
        netData.ints[1] = nbt.getInteger("burn");
        netData.ints[3] = nbt.getInteger("cook");
        netData.ints[2] = nbt.getInteger("temp");
        netData.ints[4] = nbt.getInteger("blow");
        netData.ints[0] = netData.ints[1];
    }
    
    @Override
	protected void customPlayerCommand(byte cmd, DataInputStream dis, EntityPlayerMP player) throws IOException 
    {
    	if (cmd == 0) {
    		netData.ints[4]--;
    		if (netData.ints[4] < 1) netData.ints[4] = 1;
    	} else if (cmd == 1) {
    		netData.ints[4]++;
    		if (netData.ints[4] > 8) netData.ints[4] = 8;
    	}
	}

	@Override
    public void initContainer(TileContainer container)
    {
        for (int i = 0; i < 3; i++)
        {
            container.addEntitySlot(new Slot(this, i, 8, 16 + 18 * i));
        }
        container.addEntitySlot(new SlotTank(this, 3, 71, 34));
        container.addEntitySlot(new SlotTank(this, 4, 143, 34));
        container.addPlayerInventory(8, 86);
    }
    
    @Override
    public int[] stackTransferTarget(ItemStack item, int s, TileContainer container) 
    {
        int[] pi = container.getPlayerInv();
        if (s < pi[0]) return pi;
        else return new int[]{0, 3};
    }
    
}
