package cd4017be.automation.jeiPlugin;

import javax.annotation.Nonnull;

import net.minecraft.client.Minecraft;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IDrawableAnimated;
import mezz.jei.api.gui.IDrawableStatic;
import mezz.jei.api.gui.IGuiFluidStackGroup;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.BlankRecipeCategory;
import mezz.jei.util.Translator;

public class BioFuelRecipeCategory extends BlankRecipeCategory<BioFuelRecipeWrapper> {

	private final ResourceLocation backgroundLocation;
	private final IDrawableStatic nutrient;
	private final IDrawableStatic algae;
	private final IDrawableAnimated arrow;
	private final IDrawable tankOverlay;
	private final IDrawable background;
	private final String localizedName;
	
	public BioFuelRecipeCategory(IGuiHelper guiHelper) {
		backgroundLocation = new ResourceLocation("automation", "textures/gui/recipesJEI/algaePool.png");
		nutrient = guiHelper.createDrawable(backgroundLocation, 176, 0, 8, 52);
		algae = guiHelper.createDrawable(backgroundLocation, 184, 0, 8, 52);
		IDrawableStatic arrowDrawable = guiHelper.createDrawable(backgroundLocation, 208, 0, 32, 10);
		this.arrow = guiHelper.createAnimatedDrawable(arrowDrawable, 40, IDrawableAnimated.StartDirection.LEFT, false);
		tankOverlay = guiHelper.createDrawable(backgroundLocation, 192, 0, 16, 52);
		background = guiHelper.createDrawable(backgroundLocation, 25, 15, 126, 54);
		localizedName = Translator.translateToLocal("gui.cd4017be.algaePool.name");
	}

	@Override
	@Nonnull
	public IDrawable getBackground() {
		return background;
	}

	@Override
	public void drawAnimations(Minecraft minecraft) {
		arrow.draw(minecraft, 74, 22);
	}

	@Nonnull
	@Override
	public String getTitle() {
		return localizedName;
	}

	@Nonnull
	@Override
	public String getUid() {
		return "automation.bioFuel";
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, BioFuelRecipeWrapper recipe) {
		IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();
		IGuiFluidStackGroup guiFluidStacks = recipeLayout.getFluidStacks();

		guiItemStacks.init(0, true, 27, 18);
		guiItemStacks.init(1, false, 81, 0);
		guiItemStacks.init(2, true, 81, 36);
		guiFluidStacks.init(0, true, 1, 1, 16, 52, 8000, false, tankOverlay);
		guiFluidStacks.init(1, false, 109, 1, 16, 52, 8000, false, tankOverlay);

		recipe.setNutrientDraw(nutrient, 55, 1);
		recipe.setAlgaeDraw(algae, 63, 1);
		guiItemStacks.set(0, recipe.getInputs());
		guiItemStacks.set(1, recipe.getOutputs());
		guiItemStacks.set(2, new ItemStack(Items.GLASS_BOTTLE));
		guiFluidStacks.set(0, recipe.getFluidInputs());
		guiFluidStacks.set(1, recipe.getFluidOutputs());
	}

}
