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
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;

/**
 *
 * @author CD4017BE
 */
public class ItemTank extends DefaultItemBlock {

	public ItemTank(Block id) {
		super(id);
	}

	@Override
	public void addInformation(ItemStack item, EntityPlayer player, List<String> list, boolean par4) {
		NBTTagCompound nbt = item.getTagCompound();
		FluidStack fluid = nbt != null && nbt.hasKey(FluidHandlerItemStack.FLUID_NBT_KEY) ? FluidStack.loadFluidStackFromNBT(nbt.getCompoundTag(FluidHandlerItemStack.FLUID_NBT_KEY)) : null;
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
	public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound nbt) {
		return new FluidHandlerItemStack(stack, getCapacity(stack));
	}

	public int getCapacity(ItemStack item) {
		return Config.tankCap[2];
	}

}
