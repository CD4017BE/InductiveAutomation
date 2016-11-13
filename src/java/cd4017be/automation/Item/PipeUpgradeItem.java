package cd4017be.automation.Item;

import cd4017be.lib.util.IFilter;
import cd4017be.lib.util.ItemFluidUtil;
import cd4017be.lib.util.Utils.ItemType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.items.IItemHandler;

/**
 *
 * @author CD4017BE
 */
public class PipeUpgradeItem implements IFilter<ItemStack, IItemHandler> {

	public ItemStack[] list = new ItemStack[0];
	public byte mode;//1=invert; 2=force; 4=meta; 8=nbt; 16=ore; 32=count; 64=redstone; 128=invertRS
	public byte priority;

	public ItemType getFilter() {
		return new ItemType((mode&4)!=0, (mode&8)!=0, (mode&16)!=0, list);
	}

	public boolean active(boolean rs) {
		return (mode & 64) == 0 || (rs ^ (mode & 128) != 0);
	}

	public static PipeUpgradeItem load(NBTTagCompound nbt) {
		PipeUpgradeItem upgrade = new PipeUpgradeItem();
		upgrade.mode = nbt.getByte("mode");
		upgrade.priority = nbt.getByte("prior");
		if (nbt.hasKey(ItemFluidUtil.Tag_ItemList)) {
			NBTTagList list = nbt.getTagList(ItemFluidUtil.Tag_ItemList, 10);
			upgrade.list = new ItemStack[list.tagCount()];
			for (int i = 0; i < list.tagCount(); i++) {
				upgrade.list[i] = ItemStack.loadItemStackFromNBT(list.getCompoundTagAt(i));
			}
		}
		return upgrade;
	}

	public void save(NBTTagCompound nbt) {
		nbt.setByte("mode", mode);
		nbt.setByte("prior", priority);
		if (list.length > 0) {
			NBTTagList tlist = new NBTTagList();
			for (ItemStack item : list) {
				NBTTagCompound tag = new NBTTagCompound();
				item.writeToNBT(tag);
				tlist.appendTag(tag);
			}
			nbt.setTag(ItemFluidUtil.Tag_ItemList, tlist);
		}
	}

	public static NBTTagCompound save(PipeUpgradeItem upgrade) {
		NBTTagCompound nbt = new NBTTagCompound();
		upgrade.save(nbt);
		return nbt;
	}

	public boolean isEmpty() {
		return list.length == 0 && (mode & 1) == 0;
	}

	public static boolean isNullEq(PipeUpgradeItem filter) {
		return filter == null || (filter.list.length == 0 && (filter.mode & 1) != 0);
	}

	@Override
	public int insertAmount(ItemStack obj, IItemHandler inv) {
		if (obj == null) return 0;
		int i = this.getFilter().getMatch(obj);
		if ((mode & 32) != 0 && i >= 0) {
			int n = list[i].stackSize;
			ItemType filter = new ItemType((mode&4)!=0, (mode&8)!=0, (mode&16)!=0, list[i]);
			for (int s = 0; s < inv.getSlots(); s++) {
				ItemStack item = inv.getStackInSlot(s);
				if (filter.matches(item)) n -= item.stackSize;
				if (n <= 0) return 0;
			}
			return n;
		} else return i >= 0 ^ (mode & 1) != 0 ? obj.stackSize : 0;
	}

	@Override
	public ItemStack getExtract(ItemStack obj, IItemHandler inv) {
		if (obj != null) {
			ItemType filter = this.getFilter();
			int i = filter.getMatch(obj);
			if ((mode & 32) != 0 && i >= 0) {
				int n = -list[i].stackSize;
				filter = new ItemType((mode&4)!=0, (mode&8)!=0, (mode&16)!=0, list[i]);
				for (int s = 0; s < inv.getSlots(); s++) {
					ItemStack item = inv.getStackInSlot(s);
					if (filter.matches(item)) n += item.stackSize;
					if (n >= obj.stackSize) {
						n = obj.stackSize;
						break;
					}
				}
				if (n > 0) {
					ItemStack item = obj.copy();
					item.stackSize = n;
					return item;
				} else return null;
			} else if (i >= 0 ^ (mode & 1) != 0) {
				ItemStack item = obj.copy();
				return item;
			} else return null;
		} else {
			for (int i = 0; i < inv.getSlots(); i++) {
				ItemStack item = inv.getStackInSlot(i);
				if (item != null) {
					item = this.getExtract(item, inv);
					if (item != null) return item;
				}
			}
			return null;
		}
	}

	@Override
	public boolean transfer(ItemStack stack) {
		if ((mode & 2) == 0) return true;
		else if (stack == null) return false;
		return getFilter().matches(stack) ^ (mode & 1) == 0;
	}

}
