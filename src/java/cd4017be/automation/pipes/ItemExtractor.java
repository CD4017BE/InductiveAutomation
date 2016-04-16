package cd4017be.automation.pipes;

import cd4017be.automation.Objects;
import cd4017be.automation.Block.BlockItemPipe;
import cd4017be.lib.util.Utils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;

public class ItemExtractor extends ItemComp implements ITickable {

	private int slotIdx;
	public ItemExtractor(BasicWarpPipe pipe, byte side) {
		super(pipe, side);
		slotIdx = 0;
	}

	@Override
	public void update() {
		if (!this.isValid()) return;
		int[] slots = Utils.accessibleSlots(inv, side^1);
		ISidedInventory invS = inv instanceof ISidedInventory ? (ISidedInventory)inv : null;
		if (slots.length == 0) return;
		int s, target = -1;
		ItemStack stack = null;
		for (int i = slotIdx; i < slotIdx + slots.length; i++) {
			s = slots[i % slots.length];
			stack = inv.getStackInSlot(s);
			if (stack != null && (invS == null || invS.canExtractItem(s, stack, EnumFacing.VALUES[side^1]))) {
				if (filter != null && (stack = filter.getExtractItem(stack, inv, slots, pipe.redstone)) == null) continue;
				target = s;
				slotIdx = (i + 1) % slots.length;
				break;
			}
		}
		if (target < 0 || stack == null) return;
		stack = pipe.network.insertItem(inv.decrStackSize(target, stack.stackSize));
		if (stack != null) {
			ItemStack item = inv.getStackInSlot(target);
			if (item == null) inv.setInventorySlotContents(target, stack);
			else if (item.isItemEqual(stack)) {
				stack.stackSize += item.stackSize;
				inv.setInventorySlotContents(target, stack);
			} else {
				stack = Utils.fillStack(inv, side^1, slots, stack);
				if (stack != null) pipe.pipe.dropStack(stack);
			}
		}
	}
	
	@Override
	public boolean onClicked(EntityPlayer player, long uid) {
		if (player == null) pipe.pipe.dropStack(new ItemStack(Objects.itemPipe, 1, BlockItemPipe.ID_Extraction));
		if (super.onClicked(player, uid) || player == null) return true;
		if (player.isSneaking() && player.getHeldItemMainhand() == null) {
			pipe.pipe.dropStack(new ItemStack(Objects.itemPipe, 1, BlockItemPipe.ID_Extraction));
			pipe.con[side] = 0;
			pipe.network.remConnector(pipe, side);
			pipe.updateCon = true;
			pipe.pipe.markUpdate();
			return true;
		}
		return false;
	}

}
