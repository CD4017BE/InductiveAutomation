package cd4017be.automation.jeiPlugin;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import mezz.jei.api.gui.IDrawableStatic;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import cd4017be.api.recipes.AutomationRecipes.GCRecipe;
import cd4017be.automation.TileEntity.GraviCond;
import cd4017be.lib.TooltipInfo;

public class GravCondenserRecipeWrapper extends EnergyRecipeWrapper {
	private static int MAXTRASH;
	private final GCRecipe recipe;
	private IDrawableStatic trashDraw;
	private int trashX, trashY;
	
	public GravCondenserRecipeWrapper(GCRecipe recipe) {
		this.recipe = recipe;
		if (recipe.matter > MAXTRASH) MAXTRASH = recipe.matter;
	}
	
	@Override
	public List<ItemStack> getInputs() {
		return Collections.singletonList(recipe.input);
	}

	@Override
	public List<ItemStack> getOutputs() {
		return Collections.singletonList(recipe.output);
	}

	public void setTrashDraw(IDrawableStatic trash, int x, int y) {
		this.trashDraw = trash;
		this.trashX = x;
		this.trashY = y;
	}
	
	@Override
	public float getEnergy() {
		return GraviCond.energyCost * (float)recipe.matter / 1000F;
	}

	@Override
	public float getMaxEnergy() {
		return GraviCond.energyCost * (float)MAXTRASH / 1000F;
	}

	@Override
	public void drawAnimations(Minecraft minecraft, int width, int height) {
		super.drawAnimations(minecraft, width, height);
		int e = (int)Math.ceil((float)trashDraw.getHeight() * (float)recipe.matter / (float)MAXTRASH); 
		trashDraw.draw(minecraft, trashX, trashY, trashDraw.getHeight() - e, 0, 0, 0);
	}

	@Override
	public List<String> getTooltipStrings(int x, int y) {
		if (x >= trashX && y >= trashY && x < trashX + trashDraw.getWidth() && y < trashY + trashDraw.getHeight()) 
			return Arrays.asList(TooltipInfo.format("gui.cd4017be.grav.need", cd4017be.lib.util.Utils.formatNumber((double)recipe.matter * 1000D, 4, 0)).split("\\n"));
		else return super.getTooltipStrings(x, y);
	}
	

}
