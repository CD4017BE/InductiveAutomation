/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cd4017be.automation.Item;

import cofh.api.energy.IEnergyContainerItem;
import cd4017be.api.automation.EnergyItemHandler;
import cd4017be.api.energy.EnergyThermalExpansion;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;

/**
 *
 * @author CD4017BE
 */
public class ItemInvEnergy extends ItemEnergyCell
{
    
    public ItemInvEnergy(String id, String tex, int store)
    {
        super(id, tex, store);
        this.setMaxStackSize(1);
    }

    @Override
    public int getChargeSpeed(ItemStack item) 
    {
        return 1000;
    }

    @Override
    public void onUpdate(ItemStack item, World world, Entity entity, int i, boolean b) 
    {
        if (!world.isRemote && item.stackTagCompound != null && item.stackTagCompound.getBoolean("ON") && entity instanceof EntityPlayer)
        {
            float remain = item.stackTagCompound.getFloat("buff");
        	InventoryPlayer inv = ((EntityPlayer)entity).inventory;
            for (int j = 0; j < inv.mainInventory.length; j++)
            {
                ItemStack it = inv.mainInventory[j];
                if (EnergyItemHandler.isEnergyItem(it) && !(it.getItem() instanceof ItemInvEnergy)) {
                	EnergyItemHandler.addEnergy(item, -EnergyItemHandler.addEnergy(it, EnergyItemHandler.getEnergy(item), true), false);
                } else if (it != null && it.getItem() instanceof IEnergyContainerItem) {
                	IEnergyContainerItem cont = (IEnergyContainerItem)it.getItem();
                	remain -= (float)cont.receiveEnergy(it, (int)Math.floor(((float)EnergyItemHandler.getEnergy(item) * 1000F + remain) / EnergyThermalExpansion.E_Factor), false) * EnergyThermalExpansion.E_Factor;
                	remain -= (float)EnergyItemHandler.addEnergy(item, (int)Math.floor(remain / 1000F), false) * 1000F;
                }
            }
            for (int j = 0; j < inv.armorInventory.length; j++) {
            	ItemStack it = inv.armorInventory[j];
                if (EnergyItemHandler.isEnergyItem(it)) {
                	EnergyItemHandler.addEnergy(item, -EnergyItemHandler.addEnergy(it, EnergyItemHandler.getEnergy(item), true), false);
                } else if (it != null && it.getItem() instanceof IEnergyContainerItem) {
                	IEnergyContainerItem cont = (IEnergyContainerItem)it.getItem();
                	remain -= (float)cont.receiveEnergy(it, (int)Math.floor(((float)EnergyItemHandler.getEnergy(item) * 1000F + remain) / EnergyThermalExpansion.E_Factor), false) * EnergyThermalExpansion.E_Factor;
                	remain -= (float)EnergyItemHandler.addEnergy(item, (int)Math.floor(remain / 1000F), false) * 1000F;
                }
            }
            item.stackTagCompound.setFloat("buff", remain);
        }
    }

    @Override
    public ItemStack onItemRightClick(ItemStack item, World world, EntityPlayer player) 
    {
        if (world.isRemote) return item;
        if (item.stackTagCompound == null || !item.stackTagCompound.getBoolean("ON"))
        {
            item.stackTagCompound.setBoolean("ON", true);
            player.addChatMessage(new ChatComponentText("Inventory power supply enabled"));
        } else
        if (item.stackTagCompound.getBoolean("ON"))
        {
            item.stackTagCompound.setBoolean("ON", false);
            player.addChatMessage(new ChatComponentText("Inventory power supply disabled"));
        }
        return item;
    }

    @Override
    public String getItemStackDisplayName(ItemStack item) 
    {
        if (item.stackTagCompound == null || !item.stackTagCompound.getBoolean("ON")) return super.getItemStackDisplayName(item);
        else return super.getItemStackDisplayName(item) + " (On)";
    }
    
}
