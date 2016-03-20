package cd4017be.automation.jeiPlugin;

import javax.annotation.Nonnull;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IDrawableAnimated;
import mezz.jei.api.gui.IDrawableStatic;
import mezz.jei.api.gui.IGuiFluidStackGroup;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.BlankRecipeCategory;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.util.Translator;

public class DecompCoolerRecipeCategory extends BlankRecipeCategory {
	
	private final ResourceLocation backgroundLocation;
	private final IDrawableStatic power;
	private final IDrawableAnimated arrow;
	private final IDrawableAnimated arrow1;
	private final IDrawable background;
	private final IDrawable tankOverlay;
	private final String localizedName;
	
	public DecompCoolerRecipeCategory(IGuiHelper guiHelper) {
		backgroundLocation = new ResourceLocation("automation", "textures/gui/recipesJEI/decompCooler.png");
		power = guiHelper.createDrawable(backgroundLocation, 176, 0, 8, 52);
		IDrawableStatic arrowDrawable = guiHelper.createDrawable(backgroundLocation, 200, 0, 18, 10);
		this.arrow = guiHelper.createAnimatedDrawable(arrowDrawable, 40, IDrawableAnimated.StartDirection.LEFT, false);
		arrowDrawable = guiHelper.createDrawable(backgroundLocation, 200, 10, 18, 10);
		this.arrow1 = guiHelper.createAnimatedDrawable(arrowDrawable, 40, IDrawableAnimated.StartDirection.LEFT, false);
		tankOverlay = guiHelper.createDrawable(backgroundLocation, 184, 0, 16, 52);
		background = guiHelper.createDrawable(backgroundLocation, 16, 15, 144, 54);
		localizedName = Translator.translateToLocal("gui.cd4017be.decompCooler.name");
	}

	@Override
	@Nonnull
	public IDrawable getBackground() {
		return background;
	}

	@Override
	public void drawAnimations(Minecraft minecraft) {
		arrow.draw(minecraft, 45, 22);
		arrow1.draw(minecraft, 108, 22);
	}

	@Nonnull
	@Override
	public String getTitle() {
		return localizedName;
	}

	@Nonnull
	@Override
	public String getUid() {
		return "automation.decompCool";
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, IRecipeWrapper recipeWrapper) {
		IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();
		IGuiFluidStackGroup guiFluidStacks = recipeLayout.getFluidStacks();
		
		guiItemStacks.init(0, true, 27, 18);
		guiItemStacks.init(1, true, 63, 18);
		guiItemStacks.init(2, false, 90, 18);
		guiItemStacks.init(3, false, 126, 18);
		guiFluidStacks.init(0, true, 28, 1, 16, 52, 8000, false, tankOverlay);
		guiFluidStacks.init(1, true, 91, 1, 16, 52, 8000, false, tankOverlay);
		guiFluidStacks.init(2, false, 64, 1, 16, 52, 8000, false, tankOverlay);
		guiFluidStacks.init(3, false, 127, 1, 16, 52, 8000, false, tankOverlay);

		if (recipeWrapper instanceof DecompCoolerRecipeWrapper) {
			DecompCoolerRecipeWrapper recipe = (DecompCoolerRecipeWrapper)recipeWrapper;
			recipe.setPowerDraw(power, 1, 1);
			if (recipe.inputI[0] != null) guiItemStacks.set(0, recipe.inputI[0]);
			if (recipe.inputI[1] != null) guiItemStacks.set(1, recipe.inputI[1]);
			if (recipe.outputI[0] != null) guiItemStacks.set(2, recipe.outputI[0]);
			if (recipe.outputI[1] != null) guiItemStacks.set(3, recipe.outputI[1]);
			if (recipe.inputF[0] != null) guiFluidStacks.set(0, recipe.inputF[0]);
			if (recipe.inputF[1] != null) guiFluidStacks.set(1, recipe.inputF[1]);
			if (recipe.outputF[0] != null) guiFluidStacks.set(2, recipe.outputF[0]);
			if (recipe.outputF[1] != null) guiFluidStacks.set(3, recipe.outputF[1]);
		}
	}

}
