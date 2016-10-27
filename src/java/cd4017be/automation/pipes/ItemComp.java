package cd4017be.automation.pipes;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import cd4017be.automation.Objects;
import cd4017be.automation.Item.PipeUpgradeItem;
import cd4017be.automation.pipes.WarpPipePhysics.IObjLink;
import cd4017be.lib.ModTileEntity;

public class ItemComp extends ConComp implements IObjLink {

	public final BasicWarpPipe pipe;
	public ICapabilityProvider link;
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
		if (link == null) return false;
		if (((TileEntity)link).isInvalid()) this.updateLink();
		return link != null;
	}

	@Override
	public void updateLink() {
		link = pipe.tile.getTileOnSide(EnumFacing.VALUES[side]);
	}

	@Override
	public boolean onClicked(EntityPlayer player, EnumHand hand, ItemStack item, long uid) {
		if (item == null && filter != null) {
			item = new ItemStack(Objects.itemUpgrade);
			item.setTagCompound(PipeUpgradeItem.save(filter));
			filter = null;
			((ModTileEntity)pipe.tile).dropStack(item);
			pipe.network.reorder(this);
			return true;
		} else if (filter == null && item != null && item.getItem() == Objects.itemUpgrade && item.getTagCompound() != null) {
			filter = PipeUpgradeItem.load(item.getTagCompound());
			item.stackSize--;
			if (item.stackSize <= 0) item = null;
			player.setHeldItem(hand, item);
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
	public boolean blockItem(ItemStack item) {
		return filter != null && !filter.transfer(item);
	}

	/**
	 * Insert an item stack into this destination inventory
	 * @param item
	 * @return the result if not possible
	 */
	public ItemStack insertItem(ItemStack item) {
		IItemHandler acc = link.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.VALUES[side^1]);
		if (acc == null || (filter != null && !filter.active(pipe.redstone))) return item;
		int n = item.stackSize;
		if (PipeUpgradeItem.isNullEq(filter)) return ItemHandlerHelper.insertItemStacked(acc, item, false);
		n = filter.insertAmount(item, acc);
		if (n == 0) return item;
		if (n > item.stackSize) n = item.stackSize;
		ItemStack item1 = ItemHandlerHelper.insertItemStacked(acc, item.splitStack(n), false);
		if (item1 != null) item.stackSize += item1.stackSize;
		return item.stackSize > 0 ? item : null;
	}

	public byte getPriority() {
		return filter == null ? 0 : filter.priority;
	}

}
