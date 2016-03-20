package cd4017be.automation.jeiPlugin;

import java.util.Arrays;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import cd4017be.api.automation.AutomationRecipes.CoolRecipe;

public class DecompCoolerRecipeWrapper extends EnergyRecipeWrapper {
	private static float MAXPOWER = 0F;
	private final CoolRecipe recipe;
	public final FluidStack[] inputF;
	public final FluidStack[] outputF;
	public final ItemStack[] inputI;
	public final ItemStack[] outputI;
	

	public DecompCoolerRecipeWrapper(CoolRecipe recipe) {
		this.recipe = recipe;
		inputF = new FluidStack[2];
		outputF = new FluidStack[2];
		inputI = new ItemStack[2];
		outputI = new ItemStack[2];
		if (recipe.in0 instanceof ItemStack) inputI[0] = (ItemStack)recipe.in0;
		else if (recipe.in0 instanceof FluidStack) inputF[0] = (FluidStack)recipe.in0;
		if (recipe.in1 instanceof ItemStack) inputI[1] = (ItemStack)recipe.in1;
		else if (recipe.in1 instanceof FluidStack) inputF[1] = (FluidStack)recipe.in1;
		if (recipe.out0 instanceof ItemStack) outputI[0] = (ItemStack)recipe.out0;
		else if (recipe.out0 instanceof FluidStack) outputF[0] = (FluidStack)recipe.out0;
		if (recipe.out1 instanceof ItemStack) outputI[1] = (ItemStack)recipe.out1;
		else if (recipe.out1 instanceof FluidStack) outputF[1] = (FluidStack)recipe.out1;
		if (recipe.energy > MAXPOWER) MAXPOWER = recipe.energy;
	}

	@Override
	public List<ItemStack> getInputs() {
		return Arrays.asList(inputI);
	}

	@Override
	public List<ItemStack> getOutputs() {
		return Arrays.asList(outputI);
	}

	@Override
	public List<FluidStack> getFluidInputs() {
		return Arrays.asList(inputF);
	}

	@Override
	public List<FluidStack> getFluidOutputs() {
		return Arrays.asList(outputF);
	}
	
	@Override
	public float getEnergy() {
		return recipe.energy;
	}

	@Override
	public float getMaxEnergy() {
		return MAXPOWER;
	}
	
}
