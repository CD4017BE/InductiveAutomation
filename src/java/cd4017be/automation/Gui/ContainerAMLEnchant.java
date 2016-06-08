package cd4017be.automation.Gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import cd4017be.lib.ItemContainer;
import cd4017be.lib.templates.SlotItemType;

public class ContainerAMLEnchant extends ItemContainer 
{	
	
	InventoryAMLEnchant inv;
	
	public ContainerAMLEnchant(EntityPlayer player) 
	{
		super(player);
		inv = new InventoryAMLEnchant(player.getHeldItemMainhand(), this);
		this.addPlayerInventory(8, 50);
		for (int i = 0; i < inv.items.length; i++)
			this.addSlotToContainer(new SlotItemType(inv, i, 8 + i * 18, 16, new ItemStack(Items.ENCHANTED_BOOK)));
	}

	@Override
	public void onContainerClosed(EntityPlayer player) 
	{
		inv.save(player.getHeldItemMainhand());
		super.onContainerClosed(player);
	}
	
}
