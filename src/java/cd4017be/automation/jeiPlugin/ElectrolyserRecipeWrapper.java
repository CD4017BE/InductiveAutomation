package cd4017be.automation.jeiPlugin;

import java.util.Arrays;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import cd4017be.api.recipes.AutomationRecipes.ElRecipe;

public class ElectrolyserRecipeWrapper extends EnergyRecipeWrapper {
	private static float MAXPOWER = 0F;
	private final ElRecipe recipe;
	public final FluidStack[] inputF;
	public final FluidStack[] outputF;
	public final ItemStack[] inputI;
	public final ItemStack[] outputI;
	

	public ElectrolyserRecipeWrapper(ElRecipe recipe) {
		this.recipe = recipe;
		inputF = new FluidStack[1];
		outputF = new FluidStack[2];
		inputI = new ItemStack[1];
		outputI = new ItemStack[2];
		if (recipe.in instanceof ItemStack) inputI[0] = (ItemStack)recipe.in;
		else if (recipe.in instanceof FluidStack) inputF[0] = (FluidStack)recipe.in;
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
