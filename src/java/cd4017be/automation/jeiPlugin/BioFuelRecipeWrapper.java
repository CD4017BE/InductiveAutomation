package cd4017be.automation.jeiPlugin;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import cd4017be.api.recipes.AutomationRecipes.BioEntry;
import cd4017be.automation.Objects;
import cd4017be.lib.BlockItemRegistry;
import cd4017be.lib.TooltipInfo;
import cd4017be.lib.util.OreDictStack;
import mezz.jei.api.gui.IDrawableStatic;
import mezz.jei.api.recipe.BlankRecipeWrapper;

public class BioFuelRecipeWrapper extends BlankRecipeWrapper {
	
	private static int MaxAlgae;
	private final BioEntry recipe;
	private IDrawableStatic nutrientDraw;
	private IDrawableStatic algaeDraw;
	private int nutrientX, nutrientY, algaeX, algaeY;
	private final List<ItemStack> inputs;
	private final ItemStack fullContainer;
	
	public BioFuelRecipeWrapper(BioEntry recipe) {
		this.recipe = recipe;
		if (recipe.item instanceof ItemStack) this.inputs = Collections.singletonList((ItemStack)recipe.item);
		else if (recipe.item instanceof OreDictStack) this.inputs = Arrays.asList(((OreDictStack)recipe.item).getItems());
		else this.inputs = Collections.emptyList();
		this.fullContainer = BlockItemRegistry.stack("m.LCAlgae", 1);
		if (recipe.algae > MaxAlgae) MaxAlgae = recipe.algae;
		if (recipe.nutrients > MaxAlgae) MaxAlgae = recipe.nutrients;
	}
	
	@Override
	public List<ItemStack> getInputs() {
		return inputs;
	}

	@Override
	public List<ItemStack> getOutputs() {
		return Collections.singletonList(fullContainer);
	}

	@Override
	public List<FluidStack> getFluidInputs() {
		return Collections.singletonList(new FluidStack(Objects.L_water, recipe.nutrients));
	}

	@Override
	public List<FluidStack> getFluidOutputs() {
		return Collections.singletonList(new FluidStack(Objects.L_biomass, recipe.nutrients + recipe.algae));
	}

	public void setNutrientDraw(IDrawableStatic draw, int x, int y) {
		this.nutrientDraw = draw;
		this.nutrientX = x;
		this.nutrientY = y;
	}
	
	public void setAlgaeDraw(IDrawableStatic draw, int x, int y) {
		this.algaeDraw = draw;
		this.algaeX = x;
		this.algaeY = y;
	}

	@Override
	public void drawAnimations(Minecraft minecraft, int width, int height) {
		int e = (int)Math.ceil((float)nutrientDraw.getHeight() * (float)recipe.nutrients / (float)MaxAlgae); 
		nutrientDraw.draw(minecraft, nutrientX, nutrientY, nutrientDraw.getHeight() - e, 0, 0, 0);
		e = (int)Math.ceil((float)algaeDraw.getHeight() * (float)recipe.algae / (float)MaxAlgae); 
		algaeDraw.draw(minecraft, algaeX, algaeY, algaeDraw.getHeight() - e, 0, 0, 0);
	}

	@Override
	public List<String> getTooltipStrings(int x, int y) {
		if (x >= nutrientX && y >= nutrientY && x < nutrientX + nutrientDraw.getWidth() && y < nutrientY + nutrientDraw.getHeight()) 
			return Arrays.asList(TooltipInfo.format("gui.cd4017be.recipe.nutrient", recipe.nutrients).split("\\n"));
		else if (x >= algaeX && y >= algaeY && x < algaeX + algaeDraw.getWidth() && y < algaeY + algaeDraw.getHeight()) 
			return Arrays.asList(TooltipInfo.format("gui.cd4017be.recipe.algae", recipe.algae).split("\\n"));
		else return super.getTooltipStrings(x, y);
	}
}
