package cd4017be.automation.jeiPlugin;

import javax.annotation.Nonnull;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IDrawableAnimated;
import mezz.jei.api.gui.IDrawableStatic;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.BlankRecipeCategory;
import mezz.jei.util.Translator;

public class GravCondenserRecipeCategory extends BlankRecipeCategory<GravCondenserRecipeWrapper> {

private static final int SlotIn = 0, SlotOut = 1;
	
	private final ResourceLocation backgroundLocation;
	private final IDrawableStatic power;
	private final IDrawableStatic trash;
	private final IDrawableAnimated arrow;
	private final IDrawable background;
	private final String localizedName;
	
	public GravCondenserRecipeCategory(IGuiHelper guiHelper) {
		backgroundLocation = new ResourceLocation("automation", "textures/gui/recipesJEI/graviCondenser.png");
		power = guiHelper.createDrawable(backgroundLocation, 176, 0, 16, 52);
		trash = guiHelper.createDrawable(backgroundLocation, 192, 0, 16, 52);
		IDrawableStatic arrowDrawable = guiHelper.createDrawable(backgroundLocation, 208, 0, 18, 10);
		this.arrow = guiHelper.createAnimatedDrawable(arrowDrawable, 40, IDrawableAnimated.StartDirection.LEFT, false);
		background = guiHelper.createDrawable(backgroundLocation, 25, 15, 135, 54);
		localizedName = Translator.translateToLocal("gui.cd4017be.gravCond.name");
	}

	@Override
	@Nonnull
	public IDrawable getBackground() {
		return background;
	}

	@Override
	public void drawAnimations(Minecraft minecraft) {
		arrow.draw(minecraft, 90, 22);
	}

	@Nonnull
	@Override
	public String getTitle() {
		return localizedName;
	}

	@Nonnull
	@Override
	public String getUid() {
		return "automation.gravCond";
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, GravCondenserRecipeWrapper recipe) {
		IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();
		
		guiItemStacks.init(SlotIn, true, 72, 18);
		guiItemStacks.init(SlotOut, false, 108, 18);

		recipe.setPowerDraw(power, 1, 1);
		recipe.setTrashDraw(trash, 28, 1);
		guiItemStacks.set(SlotIn, recipe.getInputs());
		guiItemStacks.set(SlotOut, recipe.getOutputs());
	}

}
