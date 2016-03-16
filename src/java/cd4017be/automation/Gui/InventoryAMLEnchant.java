package cd4017be.automation.Gui;

import cd4017be.lib.templates.BasicInventory;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class InventoryAMLEnchant extends BasicInventory 
{

	private final ContainerAMLEnchant cont;
	
	public InventoryAMLEnchant(ItemStack item, ContainerAMLEnchant cont) 
	{
		super(2);
		this.cont = cont;
		if (!item.isItemEnchanted()) return;
        NBTTagList list = item.getEnchantmentTagList();
        for (int i = 0; i < list.tagCount() && i < items.length; i++) {
            NBTTagCompound nbt = list.getCompoundTagAt(i);
            int id = nbt.getShort("id");
            int lvl = nbt.getShort("lvl");
            items[i] = Items.enchanted_book.getEnchantedItemStack(new EnchantmentData(Enchantment.getEnchantmentById(id), lvl));
        }
	}
	
	public void save(ItemStack item)
	{
		if (item == null) return;
		if (item.stackTagCompound == null) item.stackTagCompound = new NBTTagCompound();
		item.stackTagCompound.removeTag("ench");
		NBTTagList list = new NBTTagList();
		boolean ench = false;
		for (int i = 0; i < items.length; i++) {
			if (items[i] != null && items[i].getItem() == Items.enchanted_book) {
				NBTTagList l = Items.enchanted_book.getEnchantments(items[i]);
				if (l.tagCount() > 0) {
					list.appendTag(l.getCompoundTagAt(0));
					ench = true;
				}
			}
		}
		if (ench) item.stackTagCompound.setTag("ench", list);
	}

	@Override
	public boolean isItemValidForSlot(int s, ItemStack item) 
	{
		return item == null || item.getItem() == Items.enchanted_book;
	}

	@Override
	public ItemStack decrStackSize(int s, int n) 
	{
		ItemStack item = super.decrStackSize(s, n);
		this.save(cont.player.getCurrentEquippedItem());
		return item;
	}

	@Override
	public void setInventorySlotContents(int s, ItemStack item) 
	{
		super.setInventorySlotContents(s, item);
		this.save(cont.player.getCurrentEquippedItem());
	}
	
}
