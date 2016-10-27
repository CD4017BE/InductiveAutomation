package cd4017be.automation.pipes;

import cd4017be.automation.Objects;
import cd4017be.automation.Block.BlockItemPipe;
import cd4017be.lib.ModTileEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class ItemExtractor extends ItemComp implements ITickable {

	private int slotIdx;
	public ItemExtractor(BasicWarpPipe pipe, byte side) {
		super(pipe, side);
		slotIdx = 0;
	}

	@Override
	public void update() {
		if (!this.isValid()) return;
		IItemHandler acc = link.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.VALUES[this.side^1]);
		if (acc == null) return;
		int s, target = -1, m = acc.getSlots();
		ItemStack stack = null;
		for (int i = slotIdx; i < slotIdx + m; i++) {
			stack = acc.extractItem(s = i % m, 65536, true);
			if (stack != null && (filter == null || (stack = filter.getExtract(stack, acc)) != null)) {
				target = s;
				slotIdx = (i + 1) % m;
				break;
			}
		}
		if (target < 0 || stack == null) return;
		m = stack.stackSize;
		stack = pipe.network.insertItem(stack, filter == null || (filter.mode & 2) == 0 ? Byte.MAX_VALUE : filter.priority);
		if (stack != null) m -= stack.stackSize;
		acc.extractItem(target, m, false);
	}

	@Override
	public boolean onClicked(EntityPlayer player, EnumHand hand, ItemStack item, long uid) {
		if (player == null) ((ModTileEntity)pipe.tile).dropStack(new ItemStack(Objects.itemPipe, 1, BlockItemPipe.ID_Extraction));
		if (super.onClicked(player, hand, item, uid) || player == null) return true;
		if (player.isSneaking() && player.getHeldItemMainhand() == null) {
			((ModTileEntity)pipe.tile).dropStack(new ItemStack(Objects.itemPipe, 1, BlockItemPipe.ID_Extraction));
			pipe.con[side] = 0;
			pipe.network.remConnector(pipe, side);
			pipe.updateCon = true;
			((ModTileEntity)pipe.tile).markUpdate();
			return true;
		}
		return false;
	}

}
