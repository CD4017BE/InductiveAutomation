/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cd4017be.automation.Item;

import cd4017be.automation.Automation;
import cd4017be.lib.DefaultItem;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import cd4017be.api.energy.EnergyAutomation.EnergyItem;

/**
 *
 * @author CD4017BE
 */
public class ItemJetpackFuel extends DefaultItem
{
    
    public static float maxFuel = 2400;
    public static float electricEmult = 0.000005F;
    
    public ItemJetpackFuel(String id)
    {
        super(id);
        this.setCreativeTab(Automation.tabAutomation);
        this.setMaxDamage(16);
        this.setMaxStackSize(1);
    }

    @Override
    public String getItemStackDisplayName(ItemStack item) 
    {
        if (item.getItemDamage() == 0) {
            return super.getItemStackDisplayName(item);
        } else {
            return super.getItemStackDisplayName(item) + String.format(" %.1f", getFuel(item) / maxFuel * 100F) + "% filled";
        }
    }
    
    private static void checkNBT(ItemStack item)
    {
        if (item.stackTagCompound == null) {
            item.stackTagCompound = new NBTTagCompound();
            item.stackTagCompound.setFloat("fuel", maxFuel);
        }
    }
    
    public static float getFuel(ItemStack item)
    {
        if (item == null || item.getItem() == null) return 0;
        if (item.getItem() instanceof ItemJetpackFuel) {
	        checkNBT(item);
	        return item.stackTagCompound.getFloat("fuel");
        } else if (item.getItem() instanceof ItemInvEnergy) {
        	EnergyItem energy = new EnergyItem(item, (ItemInvEnergy)item.getItem());
        	energy.fractal = item.getTagCompound().getFloat("buff");
        	return (float)energy.getStorage(-2) * electricEmult;
        } else return 0;
    }
    
    public static float useFuel(float n, InventoryPlayer inv, int slot)
    {
        ItemStack item = inv.mainInventory[slot];
    	float e = getFuel(item);
        if (e > 0) {
            if (item.getItem() instanceof ItemInvEnergy) {
            	EnergyItem energy = new EnergyItem(item, (ItemInvEnergy)item.getItem());
            	energy.fractal = item.getTagCompound().getFloat("buff");
            	e = electricEmult * -(float)energy.addEnergy(-n / electricEmult, -2);
            	item.getTagCompound().setFloat("buff", (float)energy.fractal);
            	return e;
            }
        	if (e - n > 0) {
            	item.stackTagCompound.setFloat("fuel", e - n);
                depleteItem(inv, slot);
                return n;
            } else {
            	item.stackTagCompound.setFloat("fuel", 0);
                depleteItem(inv, slot);
                return e;
            }
        } else return e;
    }
    
    private static void depleteItem(InventoryPlayer inv, int slot)
    {
        ItemStack item = inv.mainInventory[slot];
    	float fuel = item.stackTagCompound.getFloat("fuel");
        int d = (int)Math.ceil(fuel / maxFuel * (float)item.getMaxDamage());
        if (d > 0) {
            d = item.getMaxDamage() - d;
            item.setItemDamage(d == 0 && fuel < maxFuel ? 1 : d);
        } 
        if (fuel < 0.05F) {
            inv.mainInventory[slot] = new ItemStack(Items.glass_bottle, 3);
        }
    }
    
}
