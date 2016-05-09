package cd4017be.automation.pipes;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import cd4017be.automation.Objects;
import cd4017be.automation.Block.BlockItemPipe;
import cd4017be.automation.pipes.WarpPipePhysics.IItemDest;

public class ItemDestination extends ItemComp implements IItemDest {

	public ItemDestination(BasicWarpPipe pipe, byte side) {
		super(pipe, side);
	}
	
	@Override
	public boolean onClicked(EntityPlayer player, EnumHand hand, ItemStack item, long uid) {
		if (player == null) pipe.pipe.dropStack(new ItemStack(Objects.itemPipe, 1, BlockItemPipe.ID_Injection));
		if (super.onClicked(player, hand, item, uid) || player == null) return true;
		if (player.getHeldItemMainhand() == null) {
			pipe.pipe.dropStack(new ItemStack(Objects.itemPipe, 1, BlockItemPipe.ID_Injection));
			pipe.con[side] = 0;
			pipe.network.remConnector(pipe, side);
			pipe.updateCon = true;
			pipe.pipe.markUpdate();
			return true;
		}
		return false;
	}

}
