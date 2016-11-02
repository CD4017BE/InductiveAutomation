package cd4017be.automation.TileEntity;

import cd4017be.lib.templates.AutomatedTile;
import cd4017be.lib.templates.Inventory;
import cd4017be.lib.util.Utils;

/**
 *
 * @author CD4017BE
 */
public class MatterInterface extends AutomatedTile {

	public MatterInterface() {
		inventory = new Inventory(5, 3, null).group(0, 1, 2, Utils.IN).group(1, 2, 3, Utils.OUT).group(2, 0, 1, Utils.ACC);
	}
/*
	@Override
	public void update() 
	{
		super.update();
		if (MatterOrbItemHandler.isMatterOrb(inventory.items[0])) {
			if (inventory.items[1] != null) {
				ItemStack[] remain = MatterOrbItemHandler.addItemStacks(inventory.items[0], inventory.items[1]);
				inventory.items[1] = remain.length == 0 ? null : remain[0];
			}
			boolean rs = worldObj.isBlockPowered(getPos());
			if (rs ^ (netI0 & 1) != 0) {
				netI0 ^= 1;
				if (rs && (netI0 & 2) != 0) {
					ItemStack item = MatterOrbItemHandler.decrStackSize(inventory.items[0], 0, Integer.MAX_VALUE);
					if (item != null) MatterOrbItemHandler.addItemStacks(inventory.items[0], item);
				}
			}
			this.updateOutput();
		} else {
			inventory.items[2] = null;
		}
	}

	private void updateOutput()
	{
		inventory.items[2] = MatterOrbItemHandler.getItem(inventory.items[0], 0);
		if (inventory.items[2] != null && inventory.items[2].stackSize > 64) inventory.items[2].stackSize = 64;
		inventory.items[4] = inventory.items[2] != null ? inventory.items[2].copy() : null;
	}
	
	@Override
	protected void customPlayerCommand(byte cmd, PacketBuffer dis, EntityPlayerMP player) throws IOException 
	{
		if (cmd == 0) { //flip Stack
			ItemStack item = MatterOrbItemHandler.decrStackSize(inventory.items[0], 0, Integer.MAX_VALUE);
			if (item != null) {
				MatterOrbItemHandler.addItemStacks(inventory.items[0], item);
				this.updateOutput();
			}
		} else if (cmd == 1) {
			int n = dis.readShort();
			if (n > 4096) n = 4096;
			ItemStack item = MatterOrbItemHandler.getItem(inventory.items[0], 0);
			if (item == null) return;
			if (n > 0 && item.stackSize > n) item.stackSize = n;
			else n = item.stackSize;
			ItemStack[] remain = MatterOrbItemHandler.addItemStacks(inventory.items[3], item);
			if (remain.length == 0) {
				MatterOrbItemHandler.decrStackSize(inventory.items[0], 0, n);
				this.updateOutput();
			}
		} else if (cmd == 2) {
			netI0 = dis.readByte(); 
		}
	}

	@Override
	public int[] stackTransferTarget(ItemStack item, int s, TileContainer container) 
	{
		int[] pi = container.getPlayerInv();
		if (s < pi[0] || s >= pi[1]) return pi;
		else if (MatterOrbItemHandler.isMatterOrb(item)) return new int[]{0, 1};
		else return new int[]{1, 2};
	}

	@Override
	public void initContainer(DataContainer cont) {
		TileContainer container = (TileContainer)cont;
		container.addItemSlot(new SlotItemHandler(inventory, 0, 44, 34));
		container.addItemSlot(new SlotItemHandler(inventory, 1, 26, 16));
		container.addItemSlot(new SlotItemType(inventory, 2, 26, 52));
		container.addItemSlot(new SlotItemHandler(inventory, 3, 8, 34));
		
		container.addPlayerInventory(8, 86);
	}

	@Override
	public ItemStack decrStackSize(int i, int n) 
	{
		if (i == 2) {
			ItemStack item = MatterOrbItemHandler.decrStackSize(inventory.items[0], 0, n);
			this.updateOutput();
			return item;
		} else return super.decrStackSize(i, n);
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack item) 
	{
		if (i == 2) {
			inventory.items[2] = item;
			int n = (inventory.items[4] != null ? inventory.items[4].stackSize : 0) - (inventory.items[2] != null ? inventory.items[2].stackSize : 0);
			inventory.items[4] = inventory.items[2] != null ? inventory.items[2].copy() : null;
			if (n > 0) MatterOrbItemHandler.decrStackSize(inventory.items[0], 0, n);
		} super.setInventorySlotContents(i, item);
	}

	@Override
	public ItemStack removeStackFromSlot(int i) 
	{
		if (i == 2 || i == 4) return null;
		else return super.removeStackFromSlot(i);
	}
	
	@Override
	public boolean canInsert(ItemStack item, int cmp, int i) 
	{
		return i != 2;
	}

	@Override
	public boolean canExtract(ItemStack item, int cmp, int i) 
	{
		return true;
	}

	@Override
	public boolean isValid(ItemStack item, int cmp, int i) 
	{
		return i != 2;
	}

	@Override
	public void slotChange(ItemStack oldItem, ItemStack newItem, int i) 
	{
		if (i == 0) {
			this.updateOutput();
		} else if (i == 2) {
			int n = (oldItem == null ? 0 : oldItem.stackSize) - (newItem == null ? 0 : newItem.stackSize);
			if (n > 0) MatterOrbItemHandler.decrStackSize(inventory.items[0], 0, n);
			this.updateOutput();
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) 
	{
		super.readFromNBT(nbt);
		netI0 = nbt.getByte("mode");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) 
	{
		nbt.setByte("mode", (byte)netI0);
		return super.writeToNBT(nbt);
	}
	*/
}
