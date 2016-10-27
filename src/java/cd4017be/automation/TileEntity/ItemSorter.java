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
	
	public ItemSorter()
	{
		inventory = new Inventory(9, 5, this).group(0, 0, 1, Utils.OUT).group(1, 2, 3, Utils.OUT).group(2, 4, 5, Utils.OUT).group(3, 6, 7, Utils.OUT).group(4, 8, 9, Utils.OUT);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		for (int i = 0; i < inventory.items.length; i++) this.setSlot(0, i, inventory.items[i]);
	}

	@Override
	public void setSlot(int g, int s, ItemStack item) {
		inventory.items[s] = item;
		if (s % 2 == 1){
			int j = (s - 1) / 2;
			if (item == null || item.getItem() != Objects.itemUpgrade || item.getTagCompound() == null) filter[j] = null;
			else filter[j] = PipeUpgradeItem.load(item.getTagCompound());
		}
	}

	@Override
	public int insertAm(int g, int s, ItemStack item, ItemStack insert) {
		if (g < 4) {
			if (filter[g] == null) return 0;
			if (filter[g].getFilter().matches(item) ^ (filter[g].mode&1)== 0) return 0;
		}
		for (int j = 0; j < g; j++)
			if (filter[j] != null && !filter[j].transfer(item)) return 0;
		int m = Math.min(insert.getMaxStackSize() - (item == null ? 0 : item.stackSize), insert.stackSize); //TODO use stock keep function
		return item == null || ItemHandlerHelper.canItemStacksStack(item, insert) ? m : 0;
	}

	@Override
	public void initContainer(DataContainer cont) {
		TileContainer container = (TileContainer)cont;
		for (int i = 1; i < 9; i+=2)
			container.addItemSlot(new SlotItemType(inventory, i, 8 + i * 18, 16, new ItemStack(Objects.itemUpgrade)));
		for (int i = 0; i < 9; i+=2)
			container.addItemSlot(new SlotItemHandler(inventory, i, 8 + i * 18, 16));
		
		container.addPlayerInventory(8, 50);
	}

	@Override
	public byte getItemConnectType(int s) 
	{
		boolean in = false, out = false;
		byte b;
		for (int i = 0; i < inventory.groups.length; i++) {
			b = inventory.getConfig(s, i);
			out |= b == 3;
			in |= b == 2;
		}
		return (byte)((out ? 1 : 0) | (in ? 2 : 0));
	}
	
}
