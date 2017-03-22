package cd4017be.automation.TileEntity;

import cd4017be.api.automation.IItemPipeCon;
import cd4017be.automation.Objects;
import cd4017be.automation.Item.PipeUpgradeItem;
import cd4017be.lib.Gui.DataContainer;
import cd4017be.lib.Gui.DataContainer.IGuiData;
import cd4017be.lib.Gui.SlotItemType;
import cd4017be.lib.Gui.TileContainer;
import cd4017be.lib.templates.AutomatedTile;
import cd4017be.lib.templates.Inventory;
import cd4017be.lib.templates.Inventory.IAccessHandler;
import cd4017be.lib.util.Utils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.SlotItemHandler;

/**
 *
 * @author CD4017BE
 */
public class ItemSorter extends AutomatedTile implements IGuiData, IAccessHandler, IItemPipeCon {

	public static final byte F_Invert = 0x01;
	public static final byte F_Meta = 0x02;
	public static final byte F_NBT = 0x04;
	public static final byte F_Ore = 0x08;
	public static final byte F_Force = 0x10;

	private final PipeUpgradeItem[] filter = new PipeUpgradeItem[4];

	public ItemSorter() {
		inventory = new Inventory(9, 5, this).group(0, 0, 1, Utils.OUT).group(1, 1, 2, Utils.OUT).group(2, 2, 3, Utils.OUT).group(3, 3, 4, Utils.OUT).group(4, 4, 5, Utils.OUT);
	}

	@Override
	public void update() {
		if (worldObj.isRemote) return;
		boolean rs = worldObj.isBlockPowered(pos);
		ICapabilityProvider te;
		IItemHandler acc;
		PipeUpgradeItem filt;
		for (int i = 0; i < 5; i++) {
			ItemStack item = inventory.items[i];
			if (item == null || (i < 4 && (filt = filter[i]) != null && !filt.active(rs))) continue;
			for (int s = 0; s < 6; s++)
				if ((inventory.sideCfg >> (s * 5 + i) * 2 & 3) == 2 &&
					(te = getTileOnSide(EnumFacing.VALUES[s])) != null &&
					(acc = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.VALUES[s^1])) != null &&
					(inventory.items[i] = ItemHandlerHelper.insertItemStacked(acc, item, false)) == null) break;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getCapability(Capability<T> cap, EnumFacing s) {
		if (cap != CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) return null;
		if (s != null && getItemConnectType(s.getIndex()) == 2) return (T)new Inserter(s);
		return (T)inventory.new Access(s);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		for (int i = 0; i < inventory.items.length; i++) this.setSlot(0, i, inventory.items[i]);
	}

	@Override
	public void setSlot(int g, int s, ItemStack item) {
		inventory.items[s] = item;
		if (s >= 5){
			s -= 5;
			if (item == null || item.getItem() != Objects.itemUpgrade || item.getTagCompound() == null) filter[s] = null;
			else filter[s] = PipeUpgradeItem.load(item.getTagCompound());
		}
	}

	@Override
	public void initContainer(DataContainer cont) {
		TileContainer container = (TileContainer)cont;
		for (int i = 0; i < 5; i++)
			container.addItemSlot(new SlotItemHandler(inventory, i, 8 + i * 36, 16));
		for (int i = 5; i < 9; i++)
			container.addItemSlot(new SlotItemType(inventory, i, -154 + i * 36, 16, new ItemStack(Objects.itemUpgrade)));
		container.addPlayerInventory(8, 50);
	}

	@Override
	public byte getItemConnectType(int s) {
		int c = (int)(inventory.sideCfg >> s * 10);
		return (c & 0x3ff) == 0 ? (byte)0 : (c & 0x2aa) == 0 ? (byte)2 : (c & 0x155) == 0 ? (byte)1 : (byte)0;
	}

	@Override
	public ItemStack insert(ItemStack item, EnumFacing side) {
		return new Inserter(side).insertItem(0, item, false);
	}

	private class Inserter implements IItemHandler {
		final int cfg;
		Inserter(EnumFacing side) {cfg = (int)(inventory.sideCfg >> side.getIndex() * 10);}
		@Override
		public int getSlots() {return 1;}
		@Override
		public ItemStack getStackInSlot(int slot) {return null;}
		@Override
		public ItemStack extractItem(int slot, int amount, boolean sim) {return null;}
		@Override
		public ItemStack insertItem(int slot, ItemStack item, boolean sim) {
			item = item.copy();
			int c = cfg;
			PipeUpgradeItem filt;
			for (int i = 0; i < 4; i++, c >>= 2)
				if ((c & 3) == 1 && (filt = filter[i]) != null) {
					int p = filt.getFilter().getMatch(item), n;
					boolean match = p >= 0 ^ (filt.mode & 1) != 0;
					if ((filt.mode & 32) != 0 && p >= 0) n = Math.min(item.getMaxStackSize(), filt.list[p].stackSize);
					else if (match) n = item.getMaxStackSize();
					else continue;
					ItemStack stack = inventory.items[i];
					if (stack != null) {
						n -= stack.stackSize;
						if (n <= 0 || !ItemHandlerHelper.canItemStacksStack(item, stack))
							if (match && (filt.mode & 2) != 0) return item;
							else continue;
					}
					if (item.stackSize <= n) {
						if (!sim)
							if (stack == null) inventory.items[i] = item;
							else stack.stackSize += item.stackSize;
						return null;
					} else if (!sim)
						if (stack == null) inventory.items[i] = item.splitStack(n);
						else {stack.stackSize += n; item.stackSize -= n;}
					else item.stackSize -= n;
					if (match && (filt.mode & 2) != 0) return item;
				}
			if ((c & 3) == 1) {
				ItemStack stack = inventory.items[4];
				if (stack == null) {
					if (!sim) inventory.items[4] = item;
					return null;
				} else if (ItemHandlerHelper.canItemStacksStack(item, stack)) {
					int m = stack.getMaxStackSize() - stack.stackSize;
					if (item.stackSize <= m) {
						if (!sim) stack.stackSize += item.stackSize;
						return null;
					}
					item.stackSize -= m;
					if (!sim) stack.stackSize += m;
				}
			}
			return item;
		}
	}

}
