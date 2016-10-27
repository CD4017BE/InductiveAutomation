package cd4017be.automation.TileEntity;

import net.minecraft.network.PacketBuffer;

import java.io.IOException;

import cd4017be.lib.Gui.DataContainer;
import cd4017be.lib.Gui.DataContainer.IGuiData;
import cd4017be.lib.Gui.SlotItemType;
import cd4017be.lib.Gui.TileContainer;
import cd4017be.lib.templates.AutomatedTile;
import cd4017be.lib.templates.Inventory;
import cd4017be.lib.templates.Inventory.IAccessHandler;
import cd4017be.lib.util.Utils;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.SlotItemHandler;

/**
 *
 * @author CD4017BE
 */
public class AutoCrafting extends AutomatedTile implements IGuiData, IAccessHandler
{
	private ItemStack craftOut = null;
	private boolean craftChange;
	
	public long cfg;
	
	public AutoCrafting()
	{
		inventory = new Inventory(11, 5, this).group(0, 0, 1, Utils.IN).group(1, 1, 2, Utils.IN).group(2, 2, 3, Utils.IN).group(3, 3, 9, Utils.IN).group(4, 9, 11, Utils.OUT);
	}
	
	@Override
	public void update() 
	{
		super.update();
		if (worldObj.isRemote) return;
		boolean rst = worldObj.isBlockPowered(pos);
		if (craftChange) craftOut = getCraftingOutput();
		boolean craft = false;
		int mode = this.getRef(9);
		if (mode == 0) craft = inventory.items[9] == null;
		else if (mode == 1 && rst) craft = inventory.items[9] == null;
		else if (mode == 2 && rst && this.getRef(10) == 0) craft = true;
		this.setRef(10, rst ? (byte)1 : (byte)0);
		if (craft && craftOut != null) {
			if (inventory.items[9] == null) {
				inventory.items[9] = craftOut;
				craftItem();
				craftChange = true;
			} else if (Utils.itemsEqual(inventory.items[9], craftOut) && inventory.items[9].stackSize <= craftOut.getMaxStackSize() - craftOut.stackSize) {
				inventory.items[9].stackSize += craftOut.stackSize;
				craftItem();
				craftChange = true;
			}
		}
	}
	
	private ItemStack getCraftingOutput()
	{
		int[] am = new int[9];
		for (int i = 0; i < 9; i++) {
			int s = this.getRef(i);
			if (s < 9) am[s]++;
		}
		for (int i = 0; i < am.length; i++)
		if (inventory.items[i] != null && inventory.items[i].stackSize < am[i]) return null;
		InventoryCrafting icr = new InventoryCrafting(new Container() {
			@Override
			public boolean canInteractWith(EntityPlayer var1) {
				return true;
			}
			@Override
			public void onCraftMatrixChanged(IInventory par1IInventory) {}
		}, 3, 3);
		for (int i = 0; i < 9; i++) {
			int s = this.getRef(i);
			icr.setInventorySlotContents(i, s < 9 ? inventory.items[s] : null);
		}
		craftChange = false;
		return CraftingManager.getInstance().findMatchingRecipe(icr, worldObj);
	}
	
	private void craftItem()
	{
		craftOut = null;
		for (int i = 0; i < 9; i++) {
			int s = this.getRef(i);
			ItemStack item = s < 9 ? inventory.items[s] : null;
			if (item != null) {
				inventory.extractItem(s, 1, false);
				if (item.getItem().hasContainerItem(item)) {
					ItemStack iout = item.getItem().getContainerItem(item);
					if (iout.isItemStackDamageable() && iout.getItemDamage() > iout.getMaxDamage()) iout = null;
					if (iout != null) {
						if (inventory.items[s] == null) inventory.setStackInSlot(s, iout);
						else this.putToUnusedSlotOrDrop(iout);
					}
				}
			}
		}
	}
	
	private void putToUnusedSlotOrDrop(ItemStack item)
	{
		boolean[] used = new boolean[9];
		for (int i = 0; i < 9; i++) {
			int s = this.getRef(i);
			if (s < 9) used[s] = true;
		}
		for (int i = 0; i < used.length; i++)
		if (!used[i]) {
			if (inventory.items[i] == null) {
				inventory.items[i] = item;
				return;
			} else if (Utils.itemsEqual(inventory.items[i], item)) {
				if (item.stackSize <= inventory.items[i].getMaxStackSize() - inventory.items[i].stackSize) {
					inventory.items[i].stackSize += item.stackSize;
					return;
				} else
				{
					item.stackSize -= inventory.items[i].getMaxStackSize() - inventory.items[i].stackSize;
					inventory.items[i].stackSize = inventory.items[i].getMaxStackSize();
				}
			}
		}
		EntityItem entity = new EntityItem(worldObj, pos.getX() + 0.5D, pos.getY() + 1.5D, pos.getZ() + 0.5D, item);
		worldObj.spawnEntityInWorld(entity);
	}
	
	public int getRef(int i)
	{
		return (int)(cfg >> (i * 4) & 0xf);
	}
	
	public void setRef(int i, byte v)
	{
		cfg &= ~((long)0xf << (i * 4));
		cfg |= (long)(v & 0xf) << (i * 4);
	}
	
	@Override
	protected void customPlayerCommand(byte cmd, PacketBuffer dis, EntityPlayerMP player) throws IOException 
	{
		if (cmd == 0) {
			cfg = dis.readLong();
			craftChange = true;
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) 
	{
		super.readFromNBT(nbt);
		cfg = nbt.getLong("data");
		craftOut = null;
		craftChange = true;
	}


	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) 
	{
		nbt.setLong("data", cfg);
		return super.writeToNBT(nbt);
	}
	
	@Override
	public void initContainer(DataContainer cont) {
		TileContainer container = (TileContainer)cont;
		cont.refInts = new int[2];
		for (int j = 0; j < 3; j++)
		for (int i = 0; i < 3; i++)
		container.addItemSlot(new SlotItemHandler(inventory, i + j * 3, 8 + i * 18, 16 + j * 18));
		container.addItemSlot(new SlotItemType(inventory, 9, 152, 34));
		container.addItemSlot(new SlotItemType(inventory, 10, 152, 52));
		
		container.addPlayerInventory(8, 86);
	}

	@Override
	public int insertAm(int g, int s, ItemStack item, ItemStack insert) {
		int m = Math.min(insert.getMaxStackSize() - (item == null ? 0 : item.stackSize), insert.stackSize); 
		return item == null || ItemHandlerHelper.canItemStacksStack(item, insert) ? m : 0;
	}

	@Override
	public int extractAm(int g, int s, ItemStack item, int extract) {
		return item == null ? 0 : item.stackSize < extract ? item.stackSize : extract;
	}

	@Override
	public void setSlot(int g, int s, ItemStack item) {
		if (s < 9) this.craftChange = true;
		inventory.items[s] = item;
	}

	@Override
	public boolean transferStack(ItemStack item, int s, TileContainer container) {
		if (s < container.invPlayerS) container.mergeItemStack(item, container.invPlayerS, container.invPlayerE, false);
		else container.mergeItemStack(item, 0, 9, false);
		return true;
	}

	@Override
	public int[] getSyncVariables() {
		return new int[]{(int)cfg, (int)(cfg >> 32)};
	}

	@Override
	public void setSyncVariable(int i, int v) {
		switch(i) {
		case 0: cfg &= 0xffffffff00000000L; cfg |= (long)v & 0xffffffffL; break;
		case 1: cfg &= 0x00000000ffffffffL; cfg |= (long)v << 32; break;
		}
	}

}
