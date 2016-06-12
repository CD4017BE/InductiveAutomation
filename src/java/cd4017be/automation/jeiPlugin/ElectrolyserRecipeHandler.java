package cd4017be.automation.jeiPlugin;

import cd4017be.api.recipes.AutomationRecipes.ElRecipe;
import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;

public class ElectrolyserRecipeHandler implements IRecipeHandler<ElRecipe> {

	@Override
	public Class<ElRecipe> getRecipeClass() {
		return ElRecipe.class;
	}

	@Override
	public String getRecipeCategoryUid() {
		return "automation.electrolyser";
	}

	@Override
	public IRecipeWrapper getRecipeWrapper(ElRecipe recipe) {
		return new ElectrolyserRecipeWrapper(recipe);
	}

	@Override
	public boolean isRecipeValid(ElRecipe recipe) {
		return Utils.valid(recipe.in) && Utils.valid(recipe.out0) && Utils.valid(recipe.out1);
	}
	
}
