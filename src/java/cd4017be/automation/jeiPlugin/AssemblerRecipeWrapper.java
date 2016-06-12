package cd4017be.automation.jeiPlugin;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import cd4017be.api.recipes.AutomationRecipes.CmpRecipe;
import cd4017be.automation.TileEntity.ElectricCompressor;
import net.minecraft.item.ItemStack;

public class AssemblerRecipeWrapper extends EnergyRecipeWrapper {
	
	private final CmpRecipe recipe;
	private final Object[] input;

	public AssemblerRecipeWrapper(CmpRecipe recipe) {
		this.recipe = recipe;
		this.input = new Object[4];
		int n = 0;
		for (Object obj : recipe.input) {
			this.input[n++] = Utils.getItems(obj);
		}
	}

	@Override
	public List getInputs() {
		return Arrays.asList(input);
	}

	@Override
	public List<ItemStack> getOutputs() {
		return Collections.singletonList(recipe.output);
	}

	@Override
	public float getEnergy() {
		return ElectricCompressor.Energy;
	}

	@Override
	public float getMaxEnergy() {
		return ElectricCompressor.Energy;
	}

}
