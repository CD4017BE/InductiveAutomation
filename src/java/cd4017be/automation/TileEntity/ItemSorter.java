/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cd4017be.automation.TileEntity;


import cd4017be.api.automation.IItemPipeCon;
import cd4017be.automation.Objects;
import cd4017be.automation.Item.PipeUpgradeItem;
import cd4017be.lib.TileEntityData;
import cd4017be.lib.Gui.SlotItemType;
import cd4017be.lib.Gui.TileContainer;
import cd4017be.lib.templates.AutomatedTile;
import cd4017be.lib.templates.IAutomatedInv;
import cd4017be.lib.templates.Inventory;
import cd4017be.lib.templates.Inventory.Group;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

/**
 *
 * @author CD4017BE
 */
public class ItemSorter extends AutomatedTile implements IAutomatedInv, IItemPipeCon
{
    public static final byte F_Invert = 0x01;
    public static final byte F_Meta = 0x02;
    public static final byte F_NBT = 0x04;
    public static final byte F_Ore = 0x08;
    public static final byte F_Force = 0x10;
    
    private final PipeUpgradeItem[] filter = new PipeUpgradeItem[4];
    
    public ItemSorter()
    {
        inventory = new Inventory(this, 9, new Group(0, 1, 1), new Group(2, 3, 1), new Group(4, 5, 1), new Group(6, 7, 1), new Group(8, 9, 1));
        /**
         * long: cfg
         * int: flags
         * float:
         * fluid:
         */
        netData = new TileEntityData(1, 0, 0, 0);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) 
    {
        super.readFromNBT(nbt);
        for (int i = 0; i < inventory.items.length; i++) this.slotChange(null, inventory.items[i], i);
    }
    
    @Override
    public boolean canInsert(ItemStack item, int cmp, int i) 
    {
        if (cmp < 4) {
            if (filter[cmp] == null) return false;
            if (filter[cmp].getFilter().matches(item) ^ (filter[cmp].mode&1)== 0) return false;
        }
        for (int j = 0; j < cmp; j++) {
            if (filter[j] != null && !filter[j].transferItem(item)) return false;
        }
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
        if (i % 2 == 1){
            int j = (i - 1) / 2;
            if (newItem == null || newItem.getItem() != Objects.itemUpgrade || newItem.getTagCompound() == null) filter[j] = null;
            else filter[j] = PipeUpgradeItem.load(newItem.getTagCompound());
        }
    }

    @Override
    public void initContainer(TileContainer container) 
    {
        for (int i = 1; i < 9; i+=2)
            container.addItemSlot(new SlotItemType(this, i, 8 + i * 18, 16, new ItemStack(Objects.itemUpgrade)));
        for (int i = 0; i < 9; i+=2)
            container.addItemSlot(new Slot(this, i, 8 + i * 18, 16));
        
        container.addPlayerInventory(8, 50);
    }

	@Override
	public byte getItemConnectType(int s) 
	{
		boolean in = false, out = false;
		byte b;
		for (int i = 0; i < inventory.componets.length; i++) {
			b = inventory.getConfig(netData.longs[0], s, i);
			out |= b == 3;
			in |= b == 2;
		}
		return (byte)((out ? 1 : 0) | (in ? 2 : 0));
	}
    
}
