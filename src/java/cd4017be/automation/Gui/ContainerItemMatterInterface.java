/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cd4017be.automation.Gui;

import cd4017be.api.automation.MatterOrbItemHandler;
import cd4017be.lib.ItemContainer;
import cd4017be.lib.templates.SlotOutput;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

/**
 *
 * @author CD4017BE
 */
public class ContainerItemMatterInterface extends ItemContainer
{
    public InventoryItemMatterInterface inventory = new InventoryItemMatterInterface();
    
    public ContainerItemMatterInterface(EntityPlayer player)
    {
        super(player);
        this.addPlayerInventory(8, 86);
        this.addSlotToContainer(new Slot(inventory, 1, 26, 16));
        this.addSlotToContainer(new Slot(inventory, 0, 44, 34));
        this.addSlotToContainer(new SlotOutput(inventory, 2, 26, 52));
        this.addSlotToContainer(new Slot(inventory, 3, 8, 34));
    }
    
    @Override
    public void detectAndSendChanges() 
    {
        inventory.update();
        super.detectAndSendChanges();
    }

    @Override
    public void onContainerClosed(EntityPlayer player) 
    {
        super.onContainerClosed(player);
        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            ItemStack item = inventory.removeStackFromSlot(i);
            if (item != null && !player.inventory.addItemStackToInventory(item)) {
                EntityItem entity = new EntityItem(player.worldObj, player.posX, player.posY, player.posZ, item);
                player.worldObj.spawnEntityInWorld(entity);
            }
        }
    }
    

    protected int[] stackTransferTarget(ItemStack item, int s) 
    {
        if (s >= 36) return new int[]{0, 36};
        else if (MatterOrbItemHandler.isMatterOrb(item)) return new int[]{37, 38};
        else return new int[]{36, 37};
    }
    
}
