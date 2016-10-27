package cd4017be.automation.TileEntity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import cd4017be.lib.Gui.DataContainer;
import cd4017be.lib.Gui.DataContainer.IGuiData;
import cd4017be.lib.Gui.GlitchSaveSlot;
import cd4017be.lib.Gui.TileContainer;
import cd4017be.lib.templates.AutomatedTile;
import cd4017be.lib.templates.Inventory;
import cd4017be.lib.templates.Inventory.IAccessHandler;
import cd4017be.lib.util.Utils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.SlotItemHandler;

/**
 *
 * @author CD4017BE
 */
public class MassstorageChest extends AutomatedTile implements IGuiData, IAccessHandler {

	public static final int StackLimit = 4096;

	public MassstorageChest() {
		inventory = new Inventory(65, 2, null).group(0, 0, 1, Utils.IN).group(1, 1, 65, Utils.OUT);
	}

	@Override
	public void update() {
		super.update();
		if (!worldObj.isRemote && inventory.items[0] != null) {
			this.addStack();
		}
	}

	private void addStack() {
		int es = -1;
		for (int i = 1; i < inventory.items.length; i++) {
			if (inventory.items[i] == null && es == -1) es = i;
			if (inventory.items[i] != null && Utils.itemsEqual(inventory.items[i], inventory.items[0])) {
				inventory.items[i].stackSize += inventory.items[0].stackSize;
				if (inventory.items[i].stackSize > StackLimit) {
					inventory.items[0].stackSize = inventory.items[i].stackSize - StackLimit;
					inventory.items[i].stackSize = StackLimit;
				} else {
					inventory.items[0] = null;
					return;
				}
			}
		}
		if (es != -1) {
			inventory.items[es] = inventory.items[0];
			inventory.items[0] = null;
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		Arrays.fill(inventory.items, null);
		NBTTagList list = nbt.getTagList("Items", 10);
		for (int id = 0; id < list.tagCount(); ++id) {
			NBTTagCompound tag = list.getCompoundTagAt(id);
			byte s = tag.getByte("Slot");
			if (s >= 0 && s < inventory.items.length) {
				ItemStack stack = ItemStack.loadItemStackFromNBT(tag);
				stack.stackSize = tag.getShort("Num");
				inventory.items[s] = stack;
			}
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		NBTTagList list = new NBTTagList();
		for (int s = 0; s < inventory.items.length; s++){
			ItemStack stack = inventory.items[s];
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
			for (int i = 0, m = 1; m < 65; m++) {
				if (i >= n) inventory.items[m] = null;
				else if (arr[i].stackSize <= StackLimit) inventory.items[m] = arr[i++];
				else inventory.items[m] = arr[i].splitStack(StackLimit);
			}
		}
	}

	@Override
	public void initContainer(DataContainer cont) {
		TileContainer container = (TileContainer)cont;
		for (int i = 0; i < 8; i++)
			for (int j = 0; j < 8; j++)
				container.addItemSlot(new GlitchSaveSlot(inventory, 1 + j + i * 8, 14 + j * 24, 16 + i * 18));
		container.addItemSlot(new SlotItemHandler(inventory, 0, 182, 174));
		container.addPlayerInventory(8, 174);
	}

	@Override
	public boolean transferStack(ItemStack item, int s, TileContainer container) {
		if (s < container.invPlayerS) container.mergeItemStack(item, container.invPlayerS, container.invPlayerE, false);
		else container.mergeItemStack(item, 64, 65, false);
		return true;
	}

	@Override
	public ArrayList<ItemStack> dropItem(IBlockState state, int fortune) {
		ArrayList<ItemStack> list = new ArrayList<ItemStack>();
		ItemStack item = new ItemStack(this.getBlockType());
		NBTTagList nbt = new NBTTagList();
		for (int s = 0; s < 64; s++) {
			ItemStack stack = inventory.items[s + 1];
			inventory.items[s + 1] = null;
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
	public void onPlaced(EntityLivingBase entity, ItemStack item) {
		if (item.getTagCompound() != null) {
			NBTTagList list = item.getTagCompound().getTagList("Items", 10);
			for (int id = 0; id < list.tagCount(); ++id) {
				NBTTagCompound tag = list.getCompoundTagAt(id);
				byte s = tag.getByte("Slot");
				if (s >= 0 && s < 64) {
					ItemStack stack = ItemStack.loadItemStackFromNBT(tag);
					stack.stackSize = tag.getShort("Num");
					inventory.items[s + 1] = stack;
				}
			}
		}
	}

	@Override
	public void setSlot(int g, int s, ItemStack item) {
		inventory.items[s] = item;
		if (s == 0 && item != null) this.addStack();
	}

	@Override
	public int insertAm(int g, int s, ItemStack item, ItemStack insert) {
		int m = Math.min((s != 0 ? StackLimit : insert.getMaxStackSize()) - (item != null ? item.stackSize : 0), insert.stackSize); 
		return item == null || ItemHandlerHelper.canItemStacksStack(item, insert) ? m : 0;
	}

}
