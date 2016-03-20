package cd4017be.automation.jeiPlugin;

import java.util.Arrays;
import java.util.List;

import net.minecraft.client.Minecraft;
import cd4017be.lib.TooltipInfo;
import mezz.jei.api.gui.IDrawableStatic;
import mezz.jei.api.recipe.BlankRecipeWrapper;

public abstract class EnergyRecipeWrapper extends BlankRecipeWrapper {

	protected IDrawableStatic powerDraw;
	protected int drawX, drawY;
	
	public void setPowerDraw(IDrawableStatic power, int x, int y) {
		this.powerDraw = power;
		this.drawX = x;
		this.drawY = y;
	}
	
	@Override
	public void drawAnimations(Minecraft minecraft, int recipeWidth, int recipeHeight) {
		int e = (int)Math.ceil(this.getEnergy() / this.getMaxEnergy() * (float)powerDraw.getHeight());
		powerDraw.draw(minecraft, drawX, drawY, powerDraw.getHeight() - e, 0, 0, 0);
	}

	@Override
	public List<String> getTooltipStrings(int x, int y) {
		if (x >= drawX && y >= drawY && x < drawX + powerDraw.getWidth() && y < drawY + powerDraw.getHeight()) 
			return Arrays.asList(TooltipInfo.format("gui.cd4017be.recipe.euse", this.getEnergy()).split("\\n"));
		else return null;
	}
	
	public abstract float getEnergy();
	public abstract float getMaxEnergy();
	
}
