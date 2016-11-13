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
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import cd4017be.api.energy.EnergyAutomation.EnergyItem;

/**
 *
 * @author CD4017BE
 */
public class ItemJetpackFuel extends DefaultItem {

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
	public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound nbt) {
		return new FluidProvider(stack, Config.tankCap[2]);
	}

	class FluidProvider implements IFluidHandler, ICapabilityProvider {

		final ItemStack item;
		final int cap;

		FluidProvider(ItemStack item, int cap) {
			this.item = item;
			this.cap = cap;
		}

		@Override
		public boolean hasCapability(Capability<?> cap, EnumFacing facing) {
			return cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> T getCapability(Capability<T> cap, EnumFacing facing) {
			return cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY ? (T)this : null;
		}

		@Override
		public IFluidTankProperties[] getTankProperties() {
			NBTTagCompound nbt = item.getTagCompound();
			return new IFluidTankProperties[]{
				new FluidTankProperties(new FluidStack(Objects.L_hydrogenG, nbt != null ? (int)nbt.getFloat("H2") : 0), cap * 2, true, true),
				new FluidTankProperties(new FluidStack(Objects.L_oxygenG, nbt != null ? (int)nbt.getFloat("O2") : 0), cap, true, true)
			};
		}

		@Override
		public int fill(FluidStack fluid, boolean doFill) {
			NBTTagCompound nbt = item.getTagCompound();
			if (nbt == null) item.setTagCompound(nbt = new NBTTagCompound());
			float max = Config.tankCap[2];
			boolean O = false;
			if (fluid.getFluid() == Objects.L_oxygenG) O = true;
			else if (fluid.getFluid() == Objects.L_hydrogenG) max *= 2F;
			else return 0;
			float n = nbt.getFloat(O ? "O2" : "H2");
			int m = Math.min(fluid.amount, (int)Math.floor(max - n));
			if (doFill) nbt.setFloat(O ? "O2" : "H2", n + (float)m);
			return m;
		}

		@Override
		public FluidStack drain(FluidStack type, boolean doDrain) {
			NBTTagCompound nbt = item.getTagCompound();
			if (nbt == null) return null;
			boolean O = false;
			if (type.getFluid() == Objects.L_oxygenG) O = true;
			else if (type.getFluid() != Objects.L_hydrogenG) return null;
			float n = nbt.getFloat(O ? "O2" : "H2");
			int m = Math.min(type.amount, (int)Math.floor(n));
			if (doDrain) nbt.setFloat(O ? "O2" : "H2", n - (float)m);
			return m > 0 ? new FluidStack(type, m) : null;
		}

		@Override
		public FluidStack drain(int maxDrain, boolean doDrain) {
			NBTTagCompound nbt = item.getTagCompound();
			if (nbt == null) return null;
			return this.drain(new FluidStack(nbt.getFloat("O2") > nbt.getFloat("H2") ? Objects.L_oxygenG : Objects.L_hydrogenG, maxDrain), doDrain);
		}
	}

}
