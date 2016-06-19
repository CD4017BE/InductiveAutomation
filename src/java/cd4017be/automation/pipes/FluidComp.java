package cd4017be.automation.pipes;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import cd4017be.automation.Objects;
import cd4017be.automation.Item.PipeUpgradeFluid;
import cd4017be.automation.pipes.WarpPipePhysics.IObjLink;
import cd4017be.lib.util.Utils;

public class FluidComp extends ConComp implements IObjLink
{

	public final BasicWarpPipe pipe;
	public IFluidHandler inv;
	public PipeUpgradeFluid filter;
	
	public FluidComp(BasicWarpPipe pipe, byte side) {
		super(side);
		this.pipe = pipe;
	}

	@Override
	public void load(NBTTagCompound nbt) {
		if (nbt.hasKey("mode")) filter = PipeUpgradeFluid.load(nbt);
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
		if (te != null && te instanceof IFluidHandler) inv = (IFluidHandler)te;
		else inv = null;
	}
	
	@Override
	public boolean onClicked(EntityPlayer player, long uid) {
		ItemStack item = player == null ? null : player.getCurrentEquippedItem();
		if (item == null && filter != null) {
			item = new ItemStack(Objects.fluidUpgrade);
			item.setTagCompound(PipeUpgradeFluid.save(filter));
			filter = null;
			pipe.pipe.dropStack(item);
			pipe.network.reorder(this);
			return true;
		} else if (filter == null && item != null && item.getItem() == Objects.fluidUpgrade && item.getTagCompound() != null) {
			filter = PipeUpgradeFluid.load(item.getTagCompound());
			item.stackSize--;
			if (item.stackSize <= 0) item = null;
			player.setCurrentItemOrArmor(0, item);
			pipe.network.reorder(this);
			return true;
		}
		return false;
	}
	
	/**
	 * Check if that fluid stack is allowed for low priority destinations
	 * @param fluid
	 * @return true if not
	 */
	public boolean blockFluid(FluidStack fluid)
	{
		return filter != null && !filter.transferFluid(fluid);
	}
	
	/**
	 * Insert a fluid stack into this destination tank
	 * @param fluid
	 * @return the result if not possible
	 */
	public FluidStack insertFluid(FluidStack fluid)
	{
		if (fluid == null) return null;
		EnumFacing dir = EnumFacing.VALUES[side^1];
		FluidTankInfo[] infos = inv.getTankInfo(dir);
        if (!PipeUpgradeFluid.isNullEq(filter) && infos != null)
            for (FluidTankInfo inf : infos) {
                int n = filter.getMaxFillAmount(fluid, inf, pipe.redstone);
                if (n > 0) {
                    fluid.amount -= inv.fill(dir, new FluidStack(fluid, n), true);
                    break;
                }
            }
        else fluid.amount -= inv.fill(dir, fluid, true);
        return fluid.amount <= 0 ? null : fluid;
	}
	
	public byte getPriority() {
		return filter == null ? 0 : filter.priority;
	}

}
