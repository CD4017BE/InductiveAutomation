/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cd4017be.automation.Gui;

import cd4017be.lib.Gui.ItemContainer;
import cd4017be.lib.Gui.SlotHolo;
import net.minecraft.entity.player.EntityPlayer;

/**
 *
 * @author CD4017BE
 */
public class ContainerItemUpgrade  extends ItemContainer
{
    public InventoryItemUpgrade inventory;
    
    public ContainerItemUpgrade(EntityPlayer player)
    {
        super(player);
        this.addPlayerInventory(8, 68);
        inventory = new InventoryItemUpgrade(player.getHeldItemMainhand());
        for (int i = 0; i < 9; i++) {
            this.addSlotToContainer(new SlotHolo(inventory, i, 8 + i * 18, 34, false, true));
        }
    }

    @Override
    public void onContainerClosed(EntityPlayer player) 
    {
        this.inventory.onClose(player.getHeldItemMainhand());
        super.onContainerClosed(player);
    }

}
