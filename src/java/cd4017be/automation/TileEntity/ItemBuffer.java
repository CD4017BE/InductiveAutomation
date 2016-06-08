package cd4017be.automation.TileEntity;

import net.minecraft.network.PacketBuffer;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import cd4017be.lib.TileContainer;
import cd4017be.lib.TileEntityData;
import cd4017be.lib.templates.AutomatedTile;
import cd4017be.lib.templates.Inventory;
import cd4017be.lib.templates.Inventory.Component;
import cd4017be.lib.util.Obj2;
import cd4017be.lib.util.Utils;

public class ItemBuffer extends AutomatedTile implements ISidedInventory
{
	
	public ItemBuffer()
	{
		inventory = new Inventory(this, 21, new Component(0, 18, -1), new Component(18, 19, 1), new Component(19, 20, 1), new Component(20, 21, 1));
		netData = new TileEntityData(1, 3, 0, 0);
	}

	@Override
	public void update() 
	{
		super.update();
		if (worldObj.isRemote) return;
		boolean bigStack = (netData.ints[0] & 0x100) != 0;
		int ovflSlots = netData.ints[0] & 0xff;
		int min = netData.ints[1] + netData.ints[2];
		if (min > 0 && inventory.items[18] == null && inventory.items[19] == null) {
			Obj2<ItemStack, Integer> out = this.getItem(min, bigStack);
			if (out.objA != null) {
				int n = out.objA.stackSize / min;
				inventory.items[18] = netData.ints[1] == 0 ? null : out.objA.splitStack(n * netData.ints[1]);
				inventory.items[19] = netData.ints[2] == 0 ? null : out.objA.splitStack(n * netData.ints[2]);
				if (out.objA.stackSize > 0) inventory.items[out.objB] = out.objA;
			}
		}
		if (ovflSlots > 0 && inventory.items[20] == null) {
			for (int i = 0; i < 18 && ovflSlots > 0; i++)
				if (inventory.items[i] != null) ovflSlots--;
			if (ovflSlots <= 0) inventory.items[20] = this.getItem(1, bigStack).objA;
		}
	}
	
	private Obj2<ItemStack, Integer> getItem(int minAm, boolean biggest)
	{
		ItemStack item = null;
		int s = -1;
		for (int i = 0; i < 18; i++) {
			if (inventory.items[i] != null && inventory.items[i].getMaxStackSize() >= minAm) {
				if (item == null) {
					item = inventory.items[i];
					inventory.items[i] = null;
					s = i;
				} else if (item.stackSize >= item.getMaxStackSize()) return new Obj2<ItemStack, Integer>(item, s);
				else if (Utils.itemsEqual(item, inventory.items[i])) {
					int n = Math.min(item.getMaxStackSize() - item.stackSize, inventory.items[i].stackSize);
					this.decrStackSize(i, n);
					item.stackSize += n;
				} else if (biggest && inventory.items[i].stackSize > item.stackSize) {
						inventory.items[s] = item;
						item = inventory.items[i];
						inventory.items[i] = null;
						s = i;
				}
			}
		}
		if (item != null && item.stackSize >= minAm) return new Obj2<ItemStack, Integer>(item, s);
		if (item != null) inventory.items[s] = item;
		return new Obj2<ItemStack, Integer>();
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) 
	{
		super.readFromNBT(nbt);
		netData.ints[0] = nbt.getInteger("mode");
		netData.ints[1] = nbt.getInteger("n1");
		netData.ints[2] = nbt.getInteger("n2");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) 
	{
		nbt.setInteger("mode", netData.ints[0]);
		nbt.setInteger("n1", netData.ints[1]);
		nbt.setInteger("n2", netData.ints[2]);
		return super.writeToNBT(nbt);
	}

	@Override
	protected void customPlayerCommand(byte cmd, PacketBuffer dis, EntityPlayerMP player) throws IOException 
	{
		if (cmd >= 0 && cmd < 3) {
			netData.ints[cmd] = dis.readInt();
			if (netData.ints[cmd] < 0) netData.ints[cmd] = 0;
		}
	}

	@Override
	public void initContainer(TileContainer container) 
	{
		container.addPlayerInventory(8, 86);
		for (int j = 0; j < 2; j++)
			for (int i = 0; i < 9; i++)
				container.addEntitySlot(new Slot(this, i + 9 * j, 8 + 18 * i, 16 + 18 * j));
		container.addEntitySlot(new Slot(this, 18, 107, 52));
		container.addEntitySlot(new Slot(this, 19, 152, 52));
		container.addEntitySlot(new Slot(this, 20, 62, 52));
	}

	@Override
	public int[] stackTransferTarget(ItemStack item, int s, TileContainer container) 
	{
		int[] pi = container.getPlayerInv();
        if (s < pi[0] || s >= pi[1]) return pi;
        else return new int[]{36, 54};
	}
	
}
