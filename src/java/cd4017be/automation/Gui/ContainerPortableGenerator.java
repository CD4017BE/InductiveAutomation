package cd4017be.automation.Gui;

import cd4017be.automation.Objects;
import cd4017be.automation.Item.ItemItemUpgrade;
import cd4017be.lib.templates.SlotItemType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerPortableGenerator extends ContainerFilteredSubInventory 
{

	public ContainerPortableGenerator(EntityPlayer player) 
	{
		super(player);
		this.addPlayerInventory(8, 50);
		this.addSlotToContainer(new Slot(this.inventory, 0, 80, 16));
		this.addSlotToContainer(new SlotItemType(this.filters, 0, 44, 16, new ItemStack(Objects.itemUpgrade)));
	}
	
	@Override
	protected int[] stackTransferTarget(ItemStack item, int id) 
	{
		if (id >= 36) return new int[]{0, 36};
		else if (item != null && item.getItem() instanceof ItemItemUpgrade) return new int[]{37, 38};
		else return new int[]{36, 37};
	}

}
