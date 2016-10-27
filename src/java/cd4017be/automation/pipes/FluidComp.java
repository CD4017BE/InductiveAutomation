package cd4017be.automation.pipes;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import cd4017be.automation.Objects;
import cd4017be.automation.Item.PipeUpgradeFluid;
import cd4017be.automation.pipes.WarpPipePhysics.IObjLink;
import cd4017be.lib.ModTileEntity;

public class FluidComp extends ConComp implements IObjLink {

	public final BasicWarpPipe pipe;
	public ICapabilityProvider link;
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
	public boolean isValid() {
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
			item = new ItemStack(Objects.fluidUpgrade);
			item.setTagCompound(PipeUpgradeFluid.save(filter));
			filter = null;
			((ModTileEntity)pipe.tile).dropStack(item);
			pipe.network.reorder(this);
			return true;
		} else if (filter == null && item != null && item.getItem() == Objects.fluidUpgrade && item.getTagCompound() != null) {
			filter = PipeUpgradeFluid.load(item.getTagCompound());
			item.stackSize--;
			if (item.stackSize <= 0) item = null;
			player.setHeldItem(hand, item);
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
		return filter != null && !filter.transfer(fluid);
	}

	/**
	 * Insert a fluid stack into this destination tank
	 * @param fluid
	 * @return the result if not possible
	 */
	public FluidStack insertFluid(FluidStack fluid) {
		IFluidHandler acc = link.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, EnumFacing.VALUES[side^1]);
		if (acc == null || (filter != null && !filter.active(pipe.redstone))) return fluid;
		if (PipeUpgradeFluid.isNullEq(filter)) fluid.amount -= acc.fill(fluid, true);
		else {
			int n = filter.insertAmount(fluid, acc);
			if (n > 0) fluid.amount -= acc.fill(new FluidStack(fluid, n), true);
		}
		return fluid.amount <= 0 ? null : fluid;
	}

	public byte getPriority() {
		return filter == null ? 0 : filter.priority;
	}

}
