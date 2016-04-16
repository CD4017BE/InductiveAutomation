package cd4017be.automation.Gui;

import cd4017be.lib.ItemContainer;
import cd4017be.lib.templates.SlotHolo;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class ContainerPortableCrafting extends ItemContainer 
{
	
	public InventoryCrafting craftingIn;
	public InventoryCraftResult craftingOut;
	
	public ContainerPortableCrafting(EntityPlayer player) 
	{
		super(player);
		this.addPlayerInventory(8, 86);
		craftingIn = new InventoryCrafting(this, 3, 3);
		craftingOut = new InventoryCraftResult();
		for (int j = 0; j < 3; j++)
			for (int i = 0; i < 3; i++) 
				this.addSlotToContainer(new SlotHolo(craftingIn, i + 3 * j, 17 + i * 18, 16 + j * 18, false, false));
		this.addSlotToContainer(new SlotHolo(craftingOut, 0, 89, 34, true, true));
		
		this.load(type);
	}
	
	public void load(ItemStack item)
	{
		if (item.getTagCompound() == null) return;
		NBTTagList data = item.getTagCompound().getTagList("grid", 10);
		NBTTagCompound tag;
		craftingIn.clear();
		for (int i = 0; i < data.tagCount(); i++) {
			tag = data.getCompoundTagAt(i);
			int s = tag.getByte("slot");
			if (s >= 0 && s < 9) craftingIn.setInventorySlotContents(s, ItemStack.loadItemStackFromNBT(tag));
		}
		this.onCraftMatrixChanged(craftingIn);
	}
	
	private void save(ItemStack item)
	{
		if (item.getTagCompound() == null) item.setTagCompound(new NBTTagCompound());
		NBTTagList data = new NBTTagList();
		NBTTagCompound tag;
		for (int i = 0; i < 9; i++) {
			ItemStack stack = craftingIn.getStackInSlot(i);
			if (stack != null) {
				tag = new NBTTagCompound();
				stack.writeToNBT(tag);
				tag.setByte("slot", (byte)i);
				data.appendTag(tag);
			}
		}
		item.getTagCompound().setTag("grid", data);
	}

	@Override
	public void onCraftMatrixChanged(IInventory inv) 
	{
		this.craftingOut.setInventorySlotContents(0, CraftingManager.getInstance().findMatchingRecipe(this.craftingIn, player.worldObj));
		this.save(player.getHeldItemMainhand());
		super.onCraftMatrixChanged(inv);
	}
	
	@Override
	public void onContainerClosed(EntityPlayer player) 
	{
		ItemStack item = player.getHeldItemMainhand();
		if (item != null) this.save(item);
		super.onContainerClosed(player);
	}

	public String inventoryName()
	{
		return this.type.getDisplayName();
	}
	
	public boolean isActive()
	{
		ItemStack item = this.player.getHeldItemMainhand();
		return item != null && item.getTagCompound() != null && item.getTagCompound().getBoolean("active");
	}
	
	public boolean isAuto()
	{
		ItemStack item = this.player.getHeldItemMainhand();
		return item != null && item.getTagCompound() != null && item.getTagCompound().getBoolean("auto");
	}
	
	public int getAmount()
	{
		ItemStack item = this.player.getHeldItemMainhand();
		if (item != null && item.getTagCompound() != null) return item.getTagCompound().getByte("amount");
		else return 0;
	}

}
