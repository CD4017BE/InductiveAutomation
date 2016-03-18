package cd4017be.automation.jeiPlugin;

import javax.annotation.Nonnull;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.ICraftingGridHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IDrawableAnimated;
import mezz.jei.api.gui.IDrawableStatic;
import mezz.jei.api.gui.IGuiFluidStackGroup;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.BlankRecipeCategory;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.util.Translator;

public class AdvFurnaceRecipeCategory extends BlankRecipeCategory {

	private static final int SlotIn = 0, SlotOut = 3;
	
	private final ResourceLocation backgroundLocation;
	private final IDrawableAnimated power;
	private final IDrawableAnimated arrow;
	private final IDrawable background;
	private final IDrawable tankOverlay;
	private final String localizedName;
	private final ICraftingGridHelper craftingGridHelper;
	private final ICraftingGridHelper craftingGridHelperOut;
	
	public AdvFurnaceRecipeCategory(IGuiHelper guiHelper) {
		backgroundLocation = new ResourceLocation("automation", "textures/gui/recipesJEI/advFurnace.png");
		IDrawableStatic powerDrawable = guiHelper.createDrawable(backgroundLocation, 176, 0, 8, 52);
		power = guiHelper.createAnimatedDrawable(powerDrawable, 200, IDrawableAnimated.StartDirection.TOP, true);
		IDrawableStatic arrowDrawable = guiHelper.createDrawable(backgroundLocation, 200, 0, 32, 10);
		this.arrow = guiHelper.createAnimatedDrawable(arrowDrawable, 40, IDrawableAnimated.StartDirection.LEFT, false);
		tankOverlay = guiHelper.createDrawable(backgroundLocation, 184, 0, 16, 52);
		background = guiHelper.createDrawable(backgroundLocation, 25, 15, 135, 54);
		localizedName = Translator.translateToLocal("gui.cd4017be.advancedFurnace.name");
		craftingGridHelper = guiHelper.createCraftingGridHelper(SlotIn, SlotOut);
		craftingGridHelperOut = guiHelper.createCraftingGridHelper(SlotOut, 6);
	}

	@Override
	@Nonnull
	public IDrawable getBackground() {
		return background;
	}

	@Override
	public void drawAnimations(Minecraft minecraft) {
		power.draw(minecraft, 1, 1);
		arrow.draw(minecraft, 65, 22);
	}

	@Nonnull
	@Override
	public String getTitle() {
		return localizedName;
	}

	@Nonnull
	@Override
	public String getUid() {
		return "automation.advFurnace";
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, IRecipeWrapper recipeWrapper) {
		IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();
		IGuiFluidStackGroup guiFluidStacks = recipeLayout.getFluidStacks();
		
		guiItemStacks.init(0, true, 45, 0);
		guiItemStacks.init(1, true, 45, 18);
		guiItemStacks.init(2, true, 45, 36);
		guiItemStacks.init(3, false, 99, 0);
		guiItemStacks.init(4, false, 99, 18);
		guiItemStacks.init(5, false, 99, 36);
		guiFluidStacks.init(0, true, 28, 1, 16, 52, 8000, false, tankOverlay);
		guiFluidStacks.init(1, false, 118, 1, 16, 52, 8000, false, tankOverlay);

		if (recipeWrapper instanceof AdvFurnaceRecipeWrapper) {
			craftingGridHelper.setInput(guiItemStacks, recipeWrapper.getInputs(), 3, 3);
			craftingGridHelperOut.setInput(guiItemStacks, ((AdvFurnaceRecipeWrapper)recipeWrapper).getOutputs(), 3, 3);
			guiFluidStacks.set(0, recipeWrapper.getFluidInputs());
			guiFluidStacks.set(1, recipeWrapper.getFluidOutputs());
		}
	}

}
