/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cd4017be.automation.TileEntity;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import cd4017be.lib.TileEntityData;
import cd4017be.lib.Gui.TileContainer;
import cd4017be.lib.templates.AutomatedTile;
import cd4017be.lib.templates.IAutomatedInv;
import cd4017be.lib.templates.Inventory;
import cd4017be.lib.templates.Inventory.Group;
import cd4017be.lib.util.Utils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;

/**
 *
 * @author CD4017BE
 */
public class MassstorageChest extends AutomatedTile implements IAutomatedInv
{
	private boolean invChange = false;
	
	public MassstorageChest()
	{
		inventory = new Inventory(this, 65, new Group(0, 1, -1), new Group(1, 65, 1));
		netData = new TileEntityData(1, 0, 0, 0);
	}
	
	@Override
	public void update()
	{
		super.update();
		if (!worldObj.isRemote && invChange)
		{
			this.addStack();
		}
	}
	
	private void addStack()
	{
		invChange = false;
		if (inventory.items[0] == null) return;
		int es = -1;
		for (int i = 1; i < inventory.items.length; i++)
		{
			if (inventory.items[i] == null && es == -1) es = i;
			if (inventory.items[i] != null && Utils.itemsEqual(inventory.items[i], inventory.items[0]))
			{
				inventory.items[i].stackSize += inventory.items[0].stackSize;
				if (inventory.items[i].stackSize > this.getInventoryStackLimit())
				{
					inventory.items[0].stackSize = inventory.items[i].stackSize - this.getInventoryStackLimit();
					inventory.items[i].stackSize = this.getInventoryStackLimit();
				} else {
					inventory.items[0] = null;
					return;
				}
			}
		}
		if (es != -1)
		{
			inventory.items[es] = inventory.items[0];
			inventory.items[0] = null;
		}
	}

	@Override
	public int getInventoryStackLimit() 
	{
		return 4096;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) 
	{
		super.readFromNBT(nbt);
		inventory.items = new ItemStack[inventory.items.length];
		NBTTagList list = nbt.getTagList("Items", 10);
		for (int id = 0; id < list.tagCount(); ++id)
		{
			NBTTagCompound tag = list.getCompoundTagAt(id);
			byte s = tag.getByte("Slot");
			if (s >= 0 && s < this.getSizeInventory())
			{
				ItemStack stack = ItemStack.loadItemStackFromNBT(tag);
				stack.stackSize = tag.getShort("Num");
				this.setInventorySlotContents(s, stack);
			}
		}
		invChange = true;
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) 
	{
		NBTTagList list = new NBTTagList();
		for (int s = 0; s < this.getSizeInventory(); s++)
		{
			ItemStack stack = this.getStackInSlot(s);
			if (stack != null) {
				NBTTagCompound tag = new NBTTagCompound();
				tag.setByte("Slot", (byte)s);
				stack.writeToNBT(tag);
				tag.removeTag("Count");
				tag.setShort("Num", (short)stack.stackSize);
				list.appendTag(tag);
			}
		}
		nbt.setTag("Items", list);
		return super.writeToNBT(nbt);
	}
	
	@Override
	protected void customPlayerCommand(byte cmd, PacketBuffer dis, EntityPlayerMP player) throws IOException {
		if (cmd == 0) {
			int n = 0;
			ItemStack[] arr = new ItemStack[64];
			ItemStack stack;
			for (int i = 1; i < 65; i++) {
				stack = inventory.items[i];
				for (int j = 0; j < n && stack != null; j++)
					if (Utils.itemsEqual(arr[j], stack)) {
						arr[j].stackSize += stack.stackSize;
						stack = null;
					}
				if (stack != null) arr[n++] = stack;
			}
			Arrays.sort(arr, 0, n, new Comparator<ItemStack>(){
				@Override
				public int compare(ItemStack arg0, ItemStack arg1) {
					return arg0.stackSize - arg1.stackSize;
				}
			});
			int max = this.getInventoryStackLimit();
			for (int i = 0, m = 1; m < 65; m++) {
				if (i >= n) inventory.items[m] = null;
				else if (arr[i].stackSize <= max) inventory.items[m] = arr[i++];
				else inventory.items[m] = arr[i].splitStack(max);
			}
		}
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack) {
		if (worldObj == null || !worldObj.isRemote || clientInvEdit) super.setInventorySlotContents(i, itemstack);
	}

	/** Dirty workaround to prevent minecraft from wrongly synchronizing the inventory. if true, changing the inventory on client side is allowed.
	 */
	private static boolean clientInvEdit = false;
	
	@Override
	public void initContainer(TileContainer container) 
	{
		for (int i = 0; i < 8; i++)
		for (int j = 0; j < 8; j++)
		{
			container.addItemSlot(new Slot(this, 1 + j + i * 8, 14 + j * 24, 16 + i * 18));
		}
		container.addItemSlot(new Slot(this, 0, 182, 174));
		
		container.addPlayerInventory(8, 174);
	}
	
	@Override
	public int[] stackTransferTarget(ItemStack item, int s, TileContainer container) 
	{
		int[] pi = container.getPlayerInv();
		if (s < pi[0]) return pi;
		else return new int[]{64, 65};
	}

	/*
	shift
	leftclick = b == 0 && m == 0
	rightclick = b == 1 && m == 0
	shift+leftclick = b == 0 && m == 1
	shift+rightclick = b == 1 && m == 1
	*/
	
	/**
	 * 
	 * @param container
	 * @param s
	 * @param mb 0 = leftClick, 1 = rightClick
	 * @param fk 0 = none, 1 = shiftDown 
	 * @param player
	 * @return 
	 */
	@Override
	public ItemStack slotClick(TileContainer container, int s, int mb, ClickType fk, EntityPlayer player) 
	{
		try {
			clientInvEdit = true;
			if (s >= 0 && s < 64) {
				Slot slot = container.getSlot(s);
				ItemStack item0 = slot.getStack();
				ItemStack item1 = player.inventory.getItemStack();
				if (fk == ClickType.PICKUP) {
					if (item1 == null && item0 == null) return null;
					else if (item0 == null) {
						int n = mb == 0 ? item1.stackSize : 1;
						slot.putStack(item1.splitStack(n));
						if (item1.stackSize <= 0) player.inventory.setItemStack(null);
						else player.inventory.setItemStack(item1);
					} else if (Utils.itemsEqual(item0, item1)){
						int n = Math.min(getInventoryStackLimit() - item0.stackSize, mb == 0 ? item1.stackSize : 1);
						item0.stackSize += n;
						item1.stackSize -= n;
						slot.putStack(item0);
						if (item1.stackSize <= 0) player.inventory.setItemStack(null);
						else player.inventory.setItemStack(item1);
					} else if (item1 == null){
						int[] t = this.stackTransferTarget(item0, s, container);
						if (t == null) return null;
						ItemStack item = slot.decrStackSize(Math.min(mb == 0 ? 1 : 8, item0.getMaxStackSize()));	
						container.mergeItemStack(item, t[0], t[1], true);
						if (item.stackSize > 0){
							item0 = slot.getStack();
							if (item0 == null) item0 = item;
							else item0.stackSize += item.stackSize;
							slot.putStack(item0);
						}
					} 
				} else if (fk == ClickType.QUICK_MOVE && item0 != null) {
					int[] t = this.stackTransferTarget(item0, s, container);
					if (t == null) return null;
					else if (mb == 0) {
						ItemStack item = slot.decrStackSize(item0.getMaxStackSize());
						container.mergeItemStack(item, t[0], t[1], true);
						if (item.stackSize > 0){
							item0 = slot.getStack();
							if (item0 == null) item0 = item;
							else item0.stackSize += item.stackSize;
							slot.putStack(item0);
						}
					} else if (mb == 1) {
						boolean run = true;
						while(run && item0.stackSize > 0) {
							int n = Math.min(item0.stackSize, item0.getMaxStackSize());
							ItemStack item = item0.splitStack(n);
							run = container.mergeItemStack(item, t[0], t[1], true);
							item0.stackSize += item.stackSize;
						}
						slot.putStack(item0.stackSize <= 0 ? null : item0);
					}
				}
				return null;
			} else
			return container.standartSlotClick(s, mb, fk, player);
		} finally {
			clientInvEdit = false;
		}
	}
	
	@Override
	public boolean canInsert(ItemStack item, int cmp, int i) 
	{
		return true;
	}

	@Override
	public boolean canExtract(ItemStack item, int cmp, int i) 
	{
		return true;
	}

	@Override
	public boolean isValid(ItemStack item, int cmp, int i) 
	{
		return true;
	}

	@Override
	public void slotChange(ItemStack oldItem, ItemStack newItem, int i) 
	{
		if (i == 0) {
			this.addStack();
		}
		if (oldItem != null && (newItem == null || Utils.itemsEqual(newItem, inventory.items[0]))) invChange = true;
	}

	@Override
	public ArrayList<ItemStack> dropItem(IBlockState state, int fortune) 
	{
		ArrayList<ItemStack> list = new ArrayList<ItemStack>();
		ItemStack item = new ItemStack(this.getBlockType());
		NBTTagList nbt = new NBTTagList();
		for (int s = 0; s < 64; s++)
		{
			ItemStack stack = this.removeStackFromSlot(s + 1);
			if (stack != null) {
				NBTTagCompound tag = new NBTTagCompound();
				tag.setByte("Slot", (byte)s);
				stack.writeToNBT(tag);
				tag.removeTag("Count");
				tag.setShort("Num", (short)stack.stackSize);
				nbt.appendTag(tag);
			}
		}
		if (nbt.tagCount() > 0) {
			item.setTagCompound(new NBTTagCompound());
			item.getTagCompound().setTag("Items", nbt);
		}
		list.add(item);
		return list;
	}

	@Override
	public void onPlaced(EntityLivingBase entity, ItemStack item) 
	{
		if (item.getTagCompound() != null) {
			NBTTagList list = item.getTagCompound().getTagList("Items", 10);
			for (int id = 0; id < list.tagCount(); ++id)
			{
				NBTTagCompound tag = list.getCompoundTagAt(id);
				byte s = tag.getByte("Slot");
				if (s >= 0 && s < 64)
				{
					ItemStack stack = ItemStack.loadItemStackFromNBT(tag);
					stack.stackSize = tag.getShort("Num");
					this.setInventorySlotContents(s + 1, stack);
				}
			}
		}
	}

	@Override
	public boolean detectAndSendChanges(TileContainer container, List<IContainerListener> crafters, PacketBuffer dos) throws IOException 
	{
		long sc = 0;
		for (int i = 0; i < 64; i++) {
			ItemStack itemstack = ((Slot)container.inventorySlots.get(i)).getStack();
			ItemStack itemstack1 = (ItemStack)container.inventoryItemStacks.get(i);
			if (!ItemStack.areItemStacksEqual(itemstack1, itemstack))
			{
				itemstack1 = itemstack == null ? null : itemstack.copy();
				container.inventoryItemStacks.set(i, itemstack1);
				sc |= 1L << i;
			}
		}
		dos.writeLong(sc);
		if (sc != 0) {
			for (int i = 0; i < 64; i++) {
				if ((sc >> i & 1) == 1) {
					ItemStack item = (ItemStack)container.inventoryItemStacks.get(i);
					if (item != null) {
						dos.writeShort((short)Item.getIdFromItem(item.getItem()));
						dos.writeShort((short)item.stackSize);
						dos.writeShort((short)item.getItemDamage());
						if (item.getTagCompound() != null) {
							dos.writeByte(1);
							dos.writeNBTTagCompoundToBuffer(item.getTagCompound());
						} else dos.writeByte(0);
					} else dos.writeShort(0);
				}
			}
			return true;
		} else return false;
	}

	@Override
	public void updateNetData(PacketBuffer dis, TileContainer container) throws IOException 
	{
		try {
			clientInvEdit = true;
			super.updateNetData(dis, container);
			long sc = dis.readLong();
			if (sc != 0) {
				for (int i = 0; i < 64; i++) {
					if ((sc >> i & 1) == 1) {
						short id = dis.readShort();
						ItemStack item;
						if (id == 0) item = null;
						else {
							item = new ItemStack(Item.getItemById(id), dis.readShort(), dis.readShort());
							id = dis.readByte();
							if (id != 0) item.setTagCompound(dis.readNBTTagCompoundFromBuffer());
						}
						((Slot)container.inventorySlots.get(i)).putStack(item);
					}
				}
			}
		} finally {
			clientInvEdit = false;
		}
	}
	
}
