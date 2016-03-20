package cd4017be.automation.jeiPlugin;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import cd4017be.api.automation.AutomationRecipes.LFRecipe;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import mezz.jei.api.recipe.IRecipeWrapper;

public class AdvFurnaceRecipeWrapper extends EnergyRecipeWrapper implements IRecipeWrapper {
	private static float MAXPOWER = 0F;
	private final LFRecipe recipe;
	private final Object[] input;
	
	public AdvFurnaceRecipeWrapper(LFRecipe recipe) {
		this.recipe = recipe;
		this.input = new Object[3];
		int n = 0;
		for (Object obj : recipe.Iinput) {
			this.input[n++] = Utils.getItems(obj);
		}
		if (recipe.energy > MAXPOWER) MAXPOWER = recipe.energy;
	}
	
	@Override
	public List getInputs() {
		return Arrays.asList(input);
	}

	@Override
	public List<ItemStack> getOutputs() {
		return Arrays.asList(recipe.Ioutput);
	}

	@Override
	public List<FluidStack> getFluidInputs() {
		return Collections.singletonList(recipe.Linput);
	}

	@Override
	public List<FluidStack> getFluidOutputs() {
		return Collections.singletonList(recipe.Loutput);
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
