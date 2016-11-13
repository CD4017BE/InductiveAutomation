package cd4017be.automation.Item;

import java.util.List;

import cd4017be.automation.Config;
import cd4017be.lib.TooltipInfo;
import cd4017be.lib.util.ItemFluidUtil.StackedFluidAccess;
import cd4017be.lib.util.Utils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;
import cd4017be.api.energy.EnergyAutomation.EnergyItem;

public class ItemPortablePump extends ItemEnergyCell {

	public static int energyUse = 8;
	public static float reachDist = 16F;
	public static int FluidCap = 16000;

	public ItemPortablePump(String id) {
		super(id, Config.Ecap[0]);
		this.setMaxStackSize(1);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void addInformation(ItemStack item, EntityPlayer player, List list, boolean par4) {
		NBTTagCompound nbt = item.getTagCompound();
		FluidStack fluid = nbt != null && nbt.hasKey(FluidHandlerItemStack.FLUID_NBT_KEY) ? FluidStack.loadFluidStackFromNBT(nbt.getCompoundTag(FluidHandlerItemStack.FLUID_NBT_KEY)) : null;
		if (fluid != null) {
			list.add(String.format("%s/%s %s %s", 
					Utils.formatNumber((float)fluid.amount / 1000F, 3), 
					Utils.formatNumber((float)FluidCap / 1000F, 3),
					TooltipInfo.getFluidUnit(),
					fluid.getLocalizedName()));
		}
		super.addInformation(item, player, list, par4);
	}

	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound nbt) {
		return new FluidHandlerItemStack(stack, FluidCap);
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(ItemStack item, World world, EntityPlayer player, EnumHand hand) {
		if (world.isRemote) return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, item);
		EnergyItem energy = new EnergyItem(item, this, -1);
		if (energy.getStorageI() < energyUse) return new ActionResult<ItemStack>(EnumActionResult.FAIL, item);
		Vec3d pos = new Vec3d(player.posX, player.posY + player.getEyeHeight(), player.posZ);
		Vec3d dir = player.getLookVec();
		boolean sneak = player.isSneaking();
		RayTraceResult obj = world.rayTraceBlocks(pos, pos.addVector(dir.xCoord * reachDist, dir.yCoord * reachDist, dir.zCoord * reachDist), true, false, sneak);
		if (obj == null) {
			return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, this.fillFluid(item, player.inventory));
		}
		FluidStack fluid = Utils.getFluid(world, obj.getBlockPos(), !sneak);
		IFluidHandler acc = item.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null);
		if (acc != null && fluid != null && acc.fill(fluid, false) == fluid.amount) {
			acc.fill(fluid, true);
			energy.addEnergyI(fluid.amount == 0 ? energyUse / -4 : -energyUse);
			if (sneak) world.setBlockState(obj.getBlockPos(), Blocks.AIR.getDefaultState(), 2);
			else world.setBlockToAir(obj.getBlockPos());
		}
		return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, this.fillFluid(item, player.inventory));
	}

	private ItemStack fillFluid(ItemStack item, InventoryPlayer inv) {
		item = item.copy();
		NBTTagCompound nbt = item.getTagCompound();
		FluidStack type = FluidStack.loadFluidStackFromNBT(nbt.getCompoundTag(FluidHandlerItemStack.FLUID_NBT_KEY));
		if (type == null) {
			nbt.removeTag(FluidHandlerItemStack.FLUID_NBT_KEY);
			return item;
		}
		for (int i = 0; i < inv.mainInventory.length && type.amount > 0; i++) {
			ItemStack stack = inv.mainInventory[i];
			StackedFluidAccess acc = new StackedFluidAccess(stack);
			if (acc.valid() && !(stack.getItem() instanceof ItemPortablePump)) {
				type.amount -= acc.fill(type, true);
				inv.mainInventory[i] = acc.result();
			}
		}
		if (type.amount <= 0) {
			item.getTagCompound().removeTag(FluidHandlerItemStack.FLUID_NBT_KEY);
		} else {
			NBTTagCompound tag = new NBTTagCompound();
			type.writeToNBT(tag);
			nbt.setTag(FluidHandlerItemStack.FLUID_NBT_KEY, tag);
		}
		return item;
	}

}
