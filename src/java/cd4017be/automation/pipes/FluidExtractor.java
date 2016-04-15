package cd4017be.automation.pipes;

import cd4017be.automation.Objects;
import cd4017be.automation.Block.BlockLiquidPipe;
import cd4017be.automation.Item.PipeUpgradeFluid;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;

public class FluidExtractor extends FluidComp implements ITickable {

	private int tankIdx;
	
	public FluidExtractor(BasicWarpPipe pipe, byte side) {
		super(pipe, side);
		tankIdx = 0;
	}

	@Override
	public void update() {
		if (!this.isValid()) return;
		boolean nullFilter = PipeUpgradeFluid.isNullEq(filter);
		FluidStack fluid = null;
		EnumFacing dir = EnumFacing.VALUES[side^1];
		FluidTankInfo[] infos = inv.getTankInfo(dir);
        if (infos != null && infos.length > 0) {
            for (int i = 0; i < infos.length; i++) {
            	FluidTankInfo inf = infos[(i + tankIdx) % infos.length];
                if (inf.fluid == null) continue;
            	int n = nullFilter ? inf.fluid.amount : filter.getMaxDrainAmount(inf.fluid, inf, pipe.redstone);
                if (n > 0) {
                    fluid = inv.drain(dir, new FluidStack(inf.fluid, n), false);
                    tankIdx = (tankIdx + i + 1) % infos.length;
                    break;
                }
            }
        } else fluid = inv.drain(dir, Integer.MAX_VALUE, false);
        if (fluid == null) return;
        FluidStack result = pipe.network.insertFluid(fluid.copy());
        if (result != null) fluid.amount -= result.amount;
        if (fluid.amount > 0) inv.drain(dir, fluid, true);
	}
	
	@Override
	public boolean onClicked(EntityPlayer player, long uid) {
		if (player == null) pipe.pipe.dropStack(new ItemStack(Objects.liquidPipe, 1, BlockLiquidPipe.ID_Extraction));
		if (super.onClicked(player, uid) || player == null) return true;
		if (player.isSneaking() && player.getCurrentEquippedItem() == null) {
			pipe.pipe.dropStack(new ItemStack(Objects.liquidPipe, 1, BlockLiquidPipe.ID_Extraction));
			pipe.con[side] = 0;
			pipe.network.remConnector(pipe, side);
			pipe.updateCon = true;
			pipe.pipe.markUpdate();
			return true;
		}
		return false;
	}

}
