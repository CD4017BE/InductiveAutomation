package cd4017be.automation.neiPlugin;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import cd4017be.api.automation.AutomationRecipes;
import cd4017be.api.automation.AutomationRecipes.CoolRecipe;
import cd4017be.lib.templates.GuiMachine;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import codechicken.lib.gui.GuiDraw;
import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.TemplateRecipeHandler;

public class DecompCoolerRecipeHandler extends TemplateRecipeHandler 
{
    private static final int ofsX = -5;
    private static final int ofsY = -11;
	
    @Override
    public String getGuiTexture() 
    {
        return new ResourceLocation("automation", "textures/gui/recipesNEI/decompCooler.png").toString();
    }

    @Override
    public String getRecipeName() 
    {
        return "Decompression Cooler";
    }
    
    public class Recipe extends CachedRecipe
    {
    	final PositionedStack reqOutput;
    	final List<PositionedStack> output;
    	final List<PositionedStack> input;
    	final int energy;
    	public Recipe(CoolRecipe recipe)
    	{
    		this.energy = (int)recipe.energy;
    		this.output = new ArrayList();
    		this.input = new ArrayList();
    		ItemStack item;
    		if ((item = Utils.getItem(recipe.in0, true)) != null) input.add(new PositionedStack(item, 53 + ofsX, 52 + ofsY));
    		if ((item = Utils.getItem(recipe.in1, true)) != null) input.add(new PositionedStack(item, 116 + ofsX, 52 + ofsY));
    		if ((item = Utils.getItem(recipe.out0, true)) != null) output.add(new PositionedStack(item, 89 + ofsX, 52 + ofsY));
    		if ((item = Utils.getItem(recipe.out1, true)) != null) output.add(new PositionedStack(item, 152 + ofsX, 52 + ofsY));
    		this.reqOutput = output.remove(0);
    	}
    	
		@Override
		public PositionedStack getResult() 
		{
			return this.reqOutput;
		}
		
		@Override
		public List<PositionedStack> getOtherStacks() 
		{
			return this.output;
		}

		@Override
		public List<PositionedStack> getIngredients() 
		{
			return this.input;
		}
    	
    }

    @Override
    public void loadTransferRects()
    {
        transferRects.add(new RecipeTransferRect(new Rectangle(70 + ofsX, 33 + ofsY, 18, 18), "decompCooler"));
        transferRects.add(new RecipeTransferRect(new Rectangle(133 + ofsX, 33 + ofsY, 18, 18), "decompCooler"));
    }
    
    @Override
    public Class<? extends GuiContainer> getGuiClass()
    {
        return GuiMachine.class;
    }

    @Override
    public void loadCraftingRecipes(String outputId, Object... results)
    {
        if(outputId.equals("decompCooler") && getClass() == DecompCoolerRecipeHandler.class)//don't want subclasses getting a hold of this
        {
            List<CoolRecipe> recipes = AutomationRecipes.getCoolerRecipes();
            for (CoolRecipe rcp : recipes) {
            	arecipes.add(new Recipe(rcp));
            }
        } else {
            super.loadCraftingRecipes(outputId, results);
        }
    }
    
    @Override
    public void loadCraftingRecipes(ItemStack result)
    {
    	List<CoolRecipe> recipes = AutomationRecipes.getCoolerRecipes();
        for (CoolRecipe rcp : recipes) {
        	if (Utils.isOrContains(result, rcp.out0) || Utils.isOrContains(result, rcp.out1))
        		arecipes.add(new Recipe(rcp));
        }
    }
    
    @Override
    public void loadUsageRecipes(ItemStack ingredient)
    {
    	List<CoolRecipe> recipes = AutomationRecipes.getCoolerRecipes();
        for (CoolRecipe rcp : recipes) {
        	if (Utils.isOrContains(ingredient, rcp.in0) || Utils.isOrContains(ingredient, rcp.in1))
        		arecipes.add(new Recipe(rcp));
        }
    }

    @Override
    public void drawExtras(int recipe)
    {
    	GuiDraw.drawTexturedModalRect(53 + ofsX, 16 + ofsY, 184, 0, 16, 52);
    	GuiDraw.drawTexturedModalRect(89 + ofsX, 16 + ofsY, 184, 0, 16, 52);
    	GuiDraw.drawTexturedModalRect(116 + ofsX, 16 + ofsY, 184, 0, 16, 52);
    	GuiDraw.drawTexturedModalRect(152 + ofsX, 16 + ofsY, 184, 0, 16, 52);
        drawProgressBar(8 + ofsX, 16 + ofsY, 176, 0, 8, 52, 100, 7);
        drawProgressBar(70 + ofsX, 37 + ofsY, 200, 0, 18, 10, 20, 0);
        drawProgressBar(133 + ofsX, 37 + ofsY, 200, 10, 18, 10, 20, 0);
        GuiDraw.drawStringC(((Recipe)arecipes.get(recipe)).energy + "kJ", 34 + ofsX, 38 + ofsY, 0x404040, false);
    }
        
    @Override
    public String getOverlayIdentifier()
    {
        return "decompCooler";
    }

}
