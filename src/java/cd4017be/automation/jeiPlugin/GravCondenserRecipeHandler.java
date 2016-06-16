package cd4017be.automation.jeiPlugin;

import cd4017be.api.recipes.AutomationRecipes.GCRecipe;
import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;

public class GravCondenserRecipeHandler implements IRecipeHandler<GCRecipe> {

	@Override
	public Class<GCRecipe> getRecipeClass() {
		return GCRecipe.class;
	}

	@Override
	public String getRecipeCategoryUid() {
		return "automation.gravCond";
	}

	@Override
	public IRecipeWrapper getRecipeWrapper(GCRecipe recipe) {
		return new GravCondenserRecipeWrapper(recipe);
	}

	@Override
	public boolean isRecipeValid(GCRecipe recipe) {
		return recipe.input != null && recipe.output != null;
	}

	@Override
	public String getRecipeCategoryUid(GCRecipe arg0) {
		return this.getRecipeCategoryUid();
	}

}
