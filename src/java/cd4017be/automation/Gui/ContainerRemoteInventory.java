package cd4017be.automation.Gui;

import java.io.DataInputStream;
import java.io.IOException;

import cd4017be.automation.Gui.ContainerFilteredSubInventory.FilterInventory;
import cd4017be.automation.Item.ItemItemUpgrade;
import cd4017be.automation.Item.ItemRemoteInv;
import cd4017be.lib.BlockItemRegistry;
import cd4017be.lib.ItemContainer;
import cd4017be.lib.templates.SlotItemType;
import cd4017be.lib.templates.SlotRemote;
import cd4017be.lib.util.Utils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class ContainerRemoteInventory extends ItemContainer 
{
	public static class ClientRemoteInventory implements IInventory {
		private ItemStack[] items;
		public ClientRemoteInventory(int s) {
			items = new ItemStack[s];
		}
		
		@Override
		public int getSizeInventory() {
			return items.length;
		}

		@Override
		public ItemStack getStackInSlot(int s) {
			return items[s];
		}

		@Override
		public ItemStack decrStackSize(int s, int n) {
			ItemStack stack = items[s];
			if (stack == null) return null;
			else if (n < stack.stackSize) return stack.splitStack(n);
			else {
				items[s] = null;
				return stack;
			}
		}

		@Override
		public ItemStack getStackInSlotOnClosing(int s) {
			return null;
		}

		@Override
		public void setInventorySlotContents(int s, ItemStack stack) {
			items[s] = stack;
		}

		@Override
		public String getInventoryName() {
			return "Client Inventory";
		}

		@Override
		public boolean hasCustomInventoryName() {
			return false;
		}

		@Override
		public int getInventoryStackLimit() {
			return 64;
		}

		@Override
		public void markDirty() {}

		@Override
		public boolean isUseableByPlayer(EntityPlayer player) {
			return true;
		}

		@Override
		public void openInventory() {}

		@Override
		public void closeInventory() {}

		@Override
		public boolean isItemValidForSlot(int s, ItemStack stack) {
			return true;
		}
		
	}
	
	public IInventory linkedInv;
	public FilterInventory filters;
	public int ofsY;
	public int size;
	
	public ContainerRemoteInventory(EntityPlayer player) 
	{
		super(player);
		if (player.worldObj.isRemote) {
			int s;
			if (type != null && type.stackTagCompound != null && (s = type.stackTagCompound.getInteger("size")) > 0) linkedInv = new ClientRemoteInventory(s);
		} else {
			linkedInv = ItemRemoteInv.getLink(type);
		}
		filters = new FilterInventory();
		if (linkedInv == null) linkedInv = new ClientRemoteInventory(0); 
		if (linkedInv.getSizeInventory() > 0) {
			byte side = this.type.stackTagCompound.getByte("s");
			int[] acs = Utils.accessibleSlots(linkedInv, side);
			size = acs.length;
			ofsY = (size - 1) / 12 * 18 - 18;
			int h = size / 12;
			int w = size % 12;
			for (int j = 0; j < h; j++)
				for (int i = 0; i < 12; i++)
					this.addSlotToContainer(new SlotRemote(linkedInv, acs[i + 12 * j], side, 8 + i * 18, 16 + j * 18));
			for (int i = 0; i < w; i++)
				this.addSlotToContainer(new SlotRemote(linkedInv, acs[i + 12 * h], side, 8 + i * 18, 16 + h * 18));
		} else ofsY = -18;
		this.addSlotToContainer(new SlotItemType(this.filters, 0, 8, 86 + ofsY, BlockItemRegistry.stack("item.itemUpgrade", 1)));
		this.addSlotToContainer(new SlotItemType(this.filters, 1, 26, 86 + ofsY, BlockItemRegistry.stack("item.itemUpgrade", 1)));
		this.addPlayerInventory(62, 68 + ofsY);
		filters.load(type);
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) 
	{
		if (!super.canInteractWith(player)) return false;
		if (!(linkedInv instanceof TileEntity)) return true;
		else return !((TileEntity)linkedInv).isInvalid();
	}

	@Override
	protected int[] stackTransferTarget(ItemStack item, int id) 
	{
		if (id < size + 2) return new int[]{size + 2, size + 38};
		else if (item != null && item.getItem() instanceof ItemItemUpgrade) return new int[]{size, size + 2};
		else return new int[]{0, size};
	}
	
	public boolean isFilterOn(int s)
	{
		ItemStack stack = filters.getStackInSlot(s);
		if (stack != null && stack.getItem() == BlockItemRegistry.getItem("item.itemUpgrade") && stack.stackTagCompound != null) {
			byte m = stack.stackTagCompound.getByte("mode");
			return (m & 128) != 0;
		} else return false;
	}
	
	public String inventoryName()
	{
		return this.type.getDisplayName();
	}

	@Override
	public void onContainerClosed(EntityPlayer p_75134_1_) 
	{
		ItemStack item = player.getCurrentEquippedItem();
		if (item != null) filters.save(item);
		super.onContainerClosed(p_75134_1_);
	}
	
	public void onPlayerCommand(World world, EntityPlayer player, DataInputStream dis) throws IOException
	{
		byte cmd = dis.readByte();
		if (cmd >= 0 && cmd < 2) {
			ItemStack stack = filters.getStackInSlot(cmd);
			if (stack != null && stack.getItem() == BlockItemRegistry.getItem("item.itemUpgrade") && stack.stackTagCompound != null) {
				byte m = stack.stackTagCompound.getByte("mode");
				m |= 64;
				m ^= 128;
				stack.stackTagCompound.setByte("mode", m);
			}
		}
	}

	@Override
	public ItemStack slotClick(int s, int b, int m, EntityPlayer player) 
	{
		ItemStack ret = super.slotClick(s, b, m, player);
		if (!player.worldObj.isRemote) {
			if (s >= 0 && s < size) {
				ItemStack item0 = this.getSlot(s).getStack();
				ItemStack item1 = item0 == null ? null : item0.copy();
		        this.inventoryItemStacks.set(s, item1);
		        for (int j = 0; j < this.crafters.size(); ++j)
		        {
		            ((ICrafting)this.crafters.get(j)).sendSlotContents(this, s, item1);
		        }
			}
	        this.detectAndSendChanges();
		}
		return ret;
	}
	

}
