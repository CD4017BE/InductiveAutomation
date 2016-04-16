package cd4017be.automation.pipes;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import cd4017be.automation.Objects;
import cd4017be.automation.Block.BlockLiquidPipe;
import cd4017be.automation.pipes.WarpPipePhysics.IFluidDest;

public class FluidDestination extends FluidComp implements IFluidDest {

	public FluidDestination(BasicWarpPipe pipe, byte side) {
		super(pipe, side);
	}
	
	@Override
	public boolean onClicked(EntityPlayer player, long uid) {
		if (player == null) pipe.pipe.dropStack(new ItemStack(Objects.liquidPipe, 1, BlockLiquidPipe.ID_Injection));
		if (super.onClicked(player, uid) || player == null) return true;
		if (player.getHeldItemMainhand() == null && player.isSneaking()) {
			pipe.pipe.dropStack(new ItemStack(Objects.liquidPipe, 1, BlockLiquidPipe.ID_Injection));
			pipe.con[side] = 0;
			pipe.network.remConnector(pipe, side);
			pipe.updateCon = true;
			pipe.pipe.markUpdate();
			return true;
		}
		return false;
	}

}
