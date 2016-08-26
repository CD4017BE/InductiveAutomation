package cd4017be.automation.Gui;

import cd4017be.automation.Objects;
import cd4017be.automation.Item.ItemItemUpgrade;
import cd4017be.lib.Gui.SlotItemType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerPortableInventory extends ContainerFilteredSubInventory {

	public ContainerPortableInventory(EntityPlayer player) 
	{
		super(player);
		this.addPlayerInventory(8, 86);
		for (int j = 0; j < 3; j++) 
			for (int i = 0; i < 8; i++) 
				this.addSlotToContainer(new Slot(this.inventory, i + 8 * j, 26 + i * 18, 16 + j * 18));
		this.addSlotToContainer(new SlotItemType(this.filters, 0, 8, 16, new ItemStack(Objects.itemUpgrade)));
		this.addSlotToContainer(new SlotItemType(this.filters, 1, 8, 52, new ItemStack(Objects.itemUpgrade)));
	}

	@Override
	protected int[] stackTransferTarget(ItemStack item, int id) 
	{
		if (id >= 36) return new int[]{0, 36};
		else if (item != null && item.getItem() instanceof ItemItemUpgrade) return new int[]{60, 62};
		else return new int[]{36, 60};
	}
}
