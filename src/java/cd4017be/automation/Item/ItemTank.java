package cd4017be.automation.Item;

import java.util.List;

import cd4017be.automation.Config;
import cd4017be.lib.DefaultItemBlock;
import cd4017be.lib.TooltipInfo;
import cd4017be.lib.util.Utils;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;

/**
 *
 * @author CD4017BE
 */
public class ItemTank extends DefaultItemBlock implements IFluidContainerItem {

	public ItemTank(Block id) {
		super(id);
	}

	@Override
	public void addInformation(ItemStack item, EntityPlayer player, List<String> list, boolean par4) {
		FluidStack fluid = this.getFluid(item);
		if (fluid != null) {
			list.add(String.format("%s/%s %s %s", 
					Utils.formatNumber((float)fluid.amount / 1000F, 3), 
					Utils.formatNumber((float)this.getCapacity(item) / 1000F, 3),
					TooltipInfo.getFluidUnit(),
					fluid.getLocalizedName()));
		}
		super.addInformation(item, player, list, par4);
	}

	@Override
	public FluidStack getFluid(ItemStack item) {
		if (item.getTagCompound() == null) return null;
		else return FluidStack.loadFluidStackFromNBT(item.getTagCompound());
	}

	@Override
	public int getCapacity(ItemStack item) {
		return Config.tankCap[2];
	}

	@Override
	public int fill(ItemStack item, FluidStack resource, boolean doFill) {
		if (resource == null) return 0;
		int n;
		FluidStack fluid = this.getFluid(item);
		if (fluid != null) {
			if (!fluid.isFluidEqual(resource)) return 0;
			n = Math.min(this.getCapacity(item) - fluid.amount, resource.amount);
			if (doFill){
				fluid.amount += n;
				fluid.writeToNBT(item.getTagCompound());
			}
		} else {
			n = Math.min(this.getCapacity(item), resource.amount);
			if (doFill) {
				item.setTagCompound(new NBTTagCompound());
				fluid = resource.copy();
				fluid.amount = n;
				fluid.writeToNBT(item.getTagCompound());
			}
		}
		return n;
	}

	@Override
	public FluidStack drain(ItemStack item, int maxDrain, boolean doDrain) {
		FluidStack fluid = this.getFluid(item);
		if (fluid == null || maxDrain == 0) return null;
		FluidStack ret = fluid.copy();
		ret.amount = Math.min(fluid.amount, maxDrain);
		if (doDrain) {
			fluid.amount -= ret.amount;
			if (fluid.amount <= 0) item.setTagCompound(null);
			else fluid.writeToNBT(item.getTagCompound());
		}
		return ret;
	}

}
