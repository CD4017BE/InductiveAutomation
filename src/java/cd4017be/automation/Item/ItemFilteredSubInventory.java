package cd4017be.automation.Item;

import java.io.IOException;
import java.util.List;

import cd4017be.api.automation.InventoryItemHandler;
import cd4017be.api.automation.InventoryItemHandler.IItemStorage;
import cd4017be.automation.Automation;
import cd4017be.automation.Gui.ContainerFilteredSubInventory;
import cd4017be.lib.DefaultItem;
import cd4017be.lib.IGuiItem;
import cd4017be.lib.util.Utils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;

public abstract class ItemFilteredSubInventory extends DefaultItem implements IItemStorage, IGuiItem {

	public ItemFilteredSubInventory(String id)
	{
		super(id);
        this.setCreativeTab(Automation.tabAutomation);
        this.setMaxStackSize(1);
	}

	@Override
	public String getInventoryTag() {
		return "items";
	}

	@Override
	public void addInformation(ItemStack item, EntityPlayer player, List list, boolean b)
	{
		InventoryItemHandler.addInformation(item, list);
		super.addInformation(item, player, list, b);
	}

	protected int tickTime()
	{
		return 20;
	}
	
	@Override
	public void onUpdate(ItemStack item, World world, Entity entity, int s, boolean b) 
	{
		if (world.isRemote) return;
		if (entity instanceof EntityPlayer) {
			if (item.stackTagCompound == null) item.stackTagCompound = new NBTTagCompound();
			int t = item.stackTagCompound.getByte("t") + 1;
			if (t >= this.tickTime()) {
				t = 0;
				EntityPlayer player = (EntityPlayer)entity;
				ContainerFilteredSubInventory container = null;
				if (b && player.openContainer != null && player.openContainer instanceof ContainerFilteredSubInventory) container = (ContainerFilteredSubInventory)player.openContainer;
				if (container != null) container.save(item);
				InventoryPlayer inv = player.inventory;
				PipeUpgradeItem in = item.stackTagCompound.hasKey("fin") ? PipeUpgradeItem.load(item.stackTagCompound.getCompoundTag("fin")) : null;
				PipeUpgradeItem out = item.stackTagCompound.hasKey("fout") ? PipeUpgradeItem.load(item.stackTagCompound.getCompoundTag("fout")) : null;
				this.updateItem(item, player, inv, s, in, out);
				if (container != null) container.update(item);
			}
			item.stackTagCompound.setByte("t", (byte)t);
		}
	}
	
	protected void updateItem(ItemStack item, EntityPlayer player, InventoryPlayer inv, int s, PipeUpgradeItem in, PipeUpgradeItem out)
	{
		
		int[] slots = new int[s == inv.currentItem ? 35 : 34];
		int ofs = 0;
		for (int i = 0; i < slots.length; i++)
			if (i + ofs == s || i + ofs == inv.currentItem) {ofs++; i--;}
			else slots[i] = i + ofs;
		//for (int i = 0; i < s; i++) slots[i] = i;
		//for (int i = s; i < slots.length; i++) slots[i] = i + 1;
		if (in != null) this.autoInput(item, inv, slots, in);
		if (out != null) this.autoOutput(item, inv, slots, out);
	}
	
	protected void autoInput(ItemStack item, InventoryPlayer inv, int[] slots, PipeUpgradeItem in)
	{
		int n, p, sp;
		ItemStack stack;
		in.mode |= 64;
		if ((in.mode & 128) == 0) return;
		for (ItemStack search : InventoryItemHandler.getItemList(item)) {
			search = in.getExtractItem(search, inv, slots, false);
			if (search == null) continue;
			n = search.stackSize;
			p = 0;
			while((p = Utils.findStack(search, inv, slots, p)) >= 0 && n > 0) {
				sp = slots[p++];
				stack = inv.decrStackSize(sp, n);
				n -= stack.stackSize;
				stack = InventoryItemHandler.insertItemStack(item, stack);
				if (stack != null) {
					if (inv.getStackInSlot(sp) != null) stack.stackSize += inv.getStackInSlot(sp).stackSize;
					inv.setInventorySlotContents(sp, stack);
					return;
				}
			}
		}
		while(InventoryItemHandler.hasEmptySlot(item)) {
			ItemStack search = in.getExtractItem(null, inv, slots, false);
			if (search == null) break;
			n = search.stackSize;
			p = 0;
			while((p = Utils.findStack(search, inv, slots, p)) >= 0 && n > 0) {
				sp = slots[p++];
				stack = inv.decrStackSize(sp, n);
				n -= stack.stackSize;
				stack = InventoryItemHandler.insertItemStack(item, stack);
				if (stack != null) {
					if (inv.getStackInSlot(sp) != null) stack.stackSize += inv.getStackInSlot(sp).stackSize;
					inv.setInventorySlotContents(sp, stack);
					return;
				}
			}
		}
	}
	
	protected void autoOutput(ItemStack item, InventoryPlayer inv, int[] slots, PipeUpgradeItem out)
	{
		int n;
		out.mode |= 64;
		if ((out.mode & 128) == 0) return;
		for (ItemStack search : InventoryItemHandler.getItemList(item)) {
			n = out.getInsertAmount(search, inv, slots, false);
			if (n <= 0) continue;
			if (n < search.stackSize) search.stackSize = n;
			ItemStack[] rem = Utils.fill(inv, 0, slots, search);
			if (rem.length == 1) n -= rem[0].stackSize;
			search = search.copy();
			search.stackSize = n;
			InventoryItemHandler.extractItemStack(item, search);
		}
	}

	@Override
	public void onPlayerCommand(World world, EntityPlayer player, PacketBuffer dis) throws IOException 
	{
		if (player.openContainer != null && player.openContainer instanceof ContainerFilteredSubInventory) ((ContainerFilteredSubInventory)player.openContainer).onPlayerCommand(world, player, dis);
	}

}
