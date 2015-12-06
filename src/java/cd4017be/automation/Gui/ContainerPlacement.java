package cd4017be.automation.Gui;

import net.minecraft.entity.player.EntityPlayer;
import cd4017be.lib.ItemContainer;
import cd4017be.lib.templates.SlotHolo;

public class ContainerPlacement extends ItemContainer 
{

	public InventoryPlacement inventory;
	
	public ContainerPlacement(EntityPlayer player) 
	{
		super(player);
        this.addPlayerInventory(8, 68);
        inventory = new InventoryPlacement(player.getCurrentEquippedItem());
        for (int i = 0; i < 8; i++) {
            this.addSlotToContainer(new SlotHolo(inventory, i, 8 + i * 18, 16, false, false));
        }
	}
	
	@Override
    public void onContainerClosed(EntityPlayer player) 
    {
        this.inventory.onClose(player.getCurrentEquippedItem());
        super.onContainerClosed(player);
    }

}
