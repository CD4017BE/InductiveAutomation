package cd4017be.automation.Item;

import java.util.List;

import cd4017be.automation.Automation;
import cd4017be.automation.Config;
import cd4017be.automation.Objects;
import cd4017be.lib.DefaultItem;
import cd4017be.lib.TooltipInfo;
import cd4017be.lib.util.Utils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;
import cd4017be.api.energy.EnergyAutomation.EnergyItem;

/**
 *
 * @author CD4017BE
 */
public class ItemJetpackFuel extends DefaultItem implements IFluidContainerItem {

	public static float electricEmult = 0.000005F;
	public static float H2Mult = 0.005F, O2Mult = 0.01F;

	public ItemJetpackFuel(String id) {
		super(id);
		this.setCreativeTab(Automation.tabAutomation);
		this.setMaxStackSize(1);
	}

	@Override
	public void addInformation(ItemStack item, EntityPlayer player, List<String> list, boolean b) {
		if (item.hasTagCompound()) {
			list.add(String.format("H2= %s / %d %s", Utils.formatNumber(item.getTagCompound().getFloat("H2") / 1000F, 3), Config.tankCap[2] / 500, TooltipInfo.getFluidUnit()));
			list.add(String.format("O2= %s / %d %s", Utils.formatNumber(item.getTagCompound().getFloat("O2") / 1000F, 3), Config.tankCap[2] / 1000, TooltipInfo.getFluidUnit()));
		}
		super.addInformation(item, player, list, b);
	}

	@Override
	public double getDurabilityForDisplay(ItemStack item) {
		return item.hasTagCompound() ? 1D - (double)Math.min(item.getTagCompound().getFloat("H2") / 2F, item.getTagCompound().getFloat("O2")) / (double)Config.tankCap[2] : 1D;
	}

	@Override
	public boolean showDurabilityBar(ItemStack item) {
		return item.hasTagCompound();
	}

	public static float getFuel(ItemStack item) {
		if (item == null || item.getItem() == null || !item.hasTagCompound()) return 0;
		if (item.getItem() instanceof ItemJetpackFuel) {
			return Math.min(item.getTagCompound().getFloat("O2") * O2Mult, item.getTagCompound().getFloat("H2") * H2Mult);
		} else if (item.getItem() instanceof ItemInvEnergy) {
			EnergyItem energy = new EnergyItem(item, (ItemInvEnergy)item.getItem(), -2);
			energy.fractal = item.getTagCompound().getFloat("buff");
			return (float)energy.getStorage() * electricEmult;
		} else return 0;
	}

	public static float useFuel(float n, InventoryPlayer inv, int slot) {
		ItemStack item = inv.mainInventory[slot];
		if (item == null || item.getItem() == null || !item.hasTagCompound()) return 0;
		if (item.getItem() instanceof ItemInvEnergy) {
			EnergyItem energy = new EnergyItem(item, (ItemInvEnergy)item.getItem(), -2);
			energy.fractal = item.getTagCompound().getFloat("buff");
			float e = electricEmult * -(float)energy.addEnergy(-n / electricEmult);
			item.getTagCompound().setFloat("buff", (float)energy.fractal);
			return e;
		} else if (item.getItem() instanceof ItemJetpackFuel) {
			float o = item.getTagCompound().getFloat("O2"), h = item.getTagCompound().getFloat("H2");
			if (n > o * O2Mult) n = o * O2Mult;
			if (n > h * H2Mult) n = h * H2Mult;
			o -= n / O2Mult;
			h -= n / H2Mult;
			if (o < 0.001F && h < 0.001F) {
				item.setTagCompound(null);
			} else {
				item.getTagCompound().setFloat("O2", o);
				item.getTagCompound().setFloat("H2", h);
			}
			return n;
		} else return 0;
	}

	@Override
	public FluidStack getFluid(ItemStack item) {
		return null;
	}

	@Override
	public int getCapacity(ItemStack item) {
		return Config.tankCap[2];
	}

	@Override
	public int fill(ItemStack item, FluidStack fluid, boolean doFill) {
		if (!item.hasTagCompound()) item.setTagCompound(new NBTTagCompound());
		float max = Config.tankCap[2];
		boolean O = false;
		if (fluid.getFluid() == Objects.L_oxygenG) O = true;
		else if (fluid.getFluid() == Objects.L_hydrogenG) max *= 2F;
		else return 0;
		float n = item.getTagCompound().getFloat(O ? "O2" : "H2");
		int m = Math.min(fluid.amount, (int)Math.floor(max - n));
		if (doFill) item.getTagCompound().setFloat(O ? "O2" : "H2", n + (float)m);
		return m;
	}

	@Override
	public FluidStack drain(ItemStack item, int maxDrain, boolean doDrain) {
		return null;
	}

}
