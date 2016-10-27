package cd4017be.automation.pipes;

import cd4017be.automation.Objects;
import cd4017be.automation.Block.BlockLiquidPipe;
import cd4017be.automation.Item.PipeUpgradeFluid;
import cd4017be.lib.ModTileEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class FluidExtractor extends FluidComp implements ITickable {

	public FluidExtractor(BasicWarpPipe pipe, byte side) {
		super(pipe, side);
	}

	@Override
	public void update() {
		if (!this.isValid()) return;
		IFluidHandler acc = link.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, EnumFacing.VALUES[side^1]);
		if (acc == null || (filter != null && !filter.active(pipe.redstone))) return;
		FluidStack stack = PipeUpgradeFluid.isNullEq(filter) ? acc.drain(Integer.MAX_VALUE, false) : filter.getExtract(null, acc);
		if (stack == null) return;
		int n = stack.amount;
		FluidStack result = pipe.network.insertFluid(stack.copy(), filter == null || (filter.mode & 2) == 0 ? Byte.MAX_VALUE : filter.priority);
		if (result != null) stack.amount -= result.amount;
		if (n > 0) acc.drain(stack, true);
	}
	
	@Override
	public boolean onClicked(EntityPlayer player, EnumHand hand, ItemStack item, long uid) {
		if (player == null) ((ModTileEntity)pipe.tile).dropStack(new ItemStack(Objects.liquidPipe, 1, BlockLiquidPipe.ID_Extraction));
		if (super.onClicked(player, hand, item, uid) || player == null) return true;
		if (player.isSneaking() && player.getHeldItemMainhand() == null) {
			((ModTileEntity)pipe.tile).dropStack(new ItemStack(Objects.liquidPipe, 1, BlockLiquidPipe.ID_Extraction));
			pipe.con[side] = 0;
			pipe.network.remConnector(pipe, side);
			pipe.updateCon = true;
			((ModTileEntity)pipe.tile).markUpdate();
			return true;
		}
		return false;
	}

}
