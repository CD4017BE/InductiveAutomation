/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cd4017be.automation.Gui;

import cd4017be.lib.ItemContainer;
import cd4017be.lib.templates.SlotHolo;
import net.minecraft.entity.player.EntityPlayer;

/**
 *
 * @author CD4017BE
 */
public class ContainerFluidUpgrade extends ItemContainer
{
    public InventoryFluidUpgrade inventory;
    
    public ContainerFluidUpgrade(EntityPlayer player)
    {
    	super(player);
        this.addPlayerInventory(8, 50);
        inventory = new InventoryFluidUpgrade(player.getCurrentEquippedItem());
        for (int i = 0; i < 4; i++) {
            this.addSlotToContainer(new SlotHolo(inventory, i, 26 + i * 18, 16, false, false));
        }
    }

    @Override
    public void onContainerClosed(EntityPlayer player) 
    {
        this.inventory.onClose(player.getCurrentEquippedItem());
        super.onContainerClosed(player);
    }

}
