package cd4017be.automation.jeiPlugin;

import net.minecraft.item.ItemStack;
import cd4017be.automation.CommonProxy.BioEntry;
import cd4017be.lib.util.OreDictStack;
import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;

public class BioFuelRecipeHandler implements IRecipeHandler<BioEntry> {

	@Override
	public Class<BioEntry> getRecipeClass() {
		return BioEntry.class;
	}

	@Override
	public String getRecipeCategoryUid() {
		return "automation.bioFuel";
	}

	@Override
	public IRecipeWrapper getRecipeWrapper(BioEntry recipe) {
		return new BioFuelRecipeWrapper(recipe);
	}

	@Override
	public boolean isRecipeValid(BioEntry recipe) {
		return recipe.item instanceof ItemStack || recipe.item instanceof OreDictStack || recipe.item instanceof Class;
	}
	
}
