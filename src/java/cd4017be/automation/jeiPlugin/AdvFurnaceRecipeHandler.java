package cd4017be.automation.jeiPlugin;

import cd4017be.api.recipes.AutomationRecipes.LFRecipe;
import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;

public class AdvFurnaceRecipeHandler implements IRecipeHandler<LFRecipe>
{

	@Override
	public Class<LFRecipe> getRecipeClass() {
		return LFRecipe.class;
	}

	@Override
	public String getRecipeCategoryUid() {
		return "automation.advFurnace";
	}

	@Override
	public IRecipeWrapper getRecipeWrapper(LFRecipe recipe) {
		return new AdvFurnaceRecipeWrapper(recipe);
	}

	@Override
	public boolean isRecipeValid(LFRecipe recipe) {
		if (recipe.Iinput == null) return true;
		for (Object o : recipe.Iinput)
			if (!Utils.valid(o)) return false;
		return true;
	}
    
}
