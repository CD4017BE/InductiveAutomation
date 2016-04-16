package cd4017be.automation.pipes;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import cd4017be.automation.Objects;
import cd4017be.automation.Item.PipeUpgradeItem;
import cd4017be.automation.pipes.WarpPipePhysics.IObjLink;
import cd4017be.lib.util.Utils;

public class ItemComp extends ConComp implements IObjLink{

	public final BasicWarpPipe pipe;
	public IInventory inv;
	public PipeUpgradeItem filter;
	
	public ItemComp(BasicWarpPipe pipe, byte side)
	{
		super(side);
		this.pipe = pipe;
	}
	
	@Override
	public void load(NBTTagCompound nbt) {
		if (nbt.hasKey("mode")) filter = PipeUpgradeItem.load(nbt);
	}

	@Override
	public void save(NBTTagCompound nbt) {
		if (filter != null) filter.save(nbt);
	}

	/**
	 * Check if this destination still exists
	 * @return
	 */
	@Override
	public boolean isValid()
	{
		if (inv == null) return false;
		if (((TileEntity)inv).isInvalid()) this.updateLink();
		return inv != null;
	}
	
	@Override
	public void updateLink() {
		TileEntity te = Utils.getTileOnSide(pipe.pipe, side);
		if (te != null && te instanceof IInventory) inv = (IInventory)te;
		else inv = null;
	}
	
	@Override
	public boolean onClicked(EntityPlayer player, long uid) {
		ItemStack item = player == null ? null : player.getHeldItemMainhand();
		if (item == null && filter != null) {
			item = new ItemStack(Objects.itemUpgrade);
			item.setTagCompound(PipeUpgradeItem.save(filter));
			filter = null;
			pipe.pipe.dropStack(item);
			pipe.network.reorder(this);
			return true;
		} else if (filter == null && item != null && item.getItem() == Objects.itemUpgrade && item.getTagCompound() != null) {
			filter = PipeUpgradeItem.load(item.getTagCompound());
			item.stackSize--;
			if (item.stackSize <= 0) item = null;
			player.setCurrentItemOrArmor(0, item);
			pipe.network.reorder(this);
			return true;
		}
		return false;
	}

	/**
	 * Check if that item stack is allowed for low priority destinations
	 * @param item
	 * @return true if not
	 */
	public boolean blockItem(ItemStack item)
	{
		return filter != null && !filter.transferItem(item);
	}
	
	/**
	 * Insert an item stack into this destination inventory
	 * @param item
	 * @return the result if not possible
	 */
	public ItemStack insertItem(ItemStack item)
	{
		int[] slots = Utils.accessibleSlots(inv, side^1);
		int n = item.stackSize;
		if (PipeUpgradeItem.isNullEq(filter)) return Utils.fillStack(inv, side^1, slots, item);
		n = filter.getInsertAmount(item, inv, slots, pipe.redstone);
		if (n == 0) return item;
		if (n > item.stackSize) n = item.stackSize;
		ItemStack item1 = Utils.fillStack(inv, side^1, slots, item.splitStack(n));
		if (item1 != null) item.stackSize += item1.stackSize;
		return item.stackSize > 0 ? item : null;
	}
	
	public int getPriority() {
		return filter == null ? 0 : filter.priority;
	}

}
