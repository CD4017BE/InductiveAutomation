/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cd4017be.automation.neiPlugin;

import cd4017be.api.automation.AutomationRecipes;
import cd4017be.lib.templates.GuiMachine;
import codechicken.lib.gui.GuiDraw;
import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.TemplateRecipeHandler;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

/**
 *
 * @author CD4017BE
 */
public class ElectrolyserRecipeHandler extends TemplateRecipeHandler
{
    
    private static final int ofsX = -5;
    private static final int ofsY = -11;
	
    @Override
    public String getGuiTexture() 
    {
        return new ResourceLocation("automation", "textures/gui/recipesNEI/electrolyser.png").toString();
    }

    @Override
    public String getRecipeName() 
    {
        return "Electrolyser";
    }
    
    public class Recipe extends CachedRecipe
    {
    	final PositionedStack reqOutput;
    	final List<PositionedStack> output;
    	final List<PositionedStack> input;
    	final int energy;
    	public Recipe(AutomationRecipes.ElRecipe recipe)
    	{
            this.energy = (int)recipe.energy;
            output = new ArrayList();
            input = new ArrayList();
            ItemStack item;
            if ((item = Utils.getItem(recipe.in, true)) != null) input.add(new PositionedStack(item, 107 + ofsX, 52 + ofsY));
            if ((item = Utils.getItem(recipe.out0, true)) != null) output.add(new PositionedStack(item, 62 + ofsX, 52 + ofsY));
            if ((item = Utils.getItem(recipe.out1, true)) != null) output.add(new PositionedStack(item, 152 + ofsX, 52 + ofsY));
            reqOutput = output.remove(0);
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
        transferRects.add(new RecipeTransferRect(new Rectangle(81 + ofsX, 55 + ofsY, 24, 10), "electrolyser"));
        transferRects.add(new RecipeTransferRect(new Rectangle(125 + ofsX, 55 + ofsY, 24, 10), "electrolyser"));
    }
    
    @Override
    public Class<? extends GuiContainer> getGuiClass()
    {
        return GuiMachine.class;
    }

    @Override
    public void loadCraftingRecipes(String outputId, Object... results)
    {
        if(outputId.equals("electrolyser") && getClass() == ElectrolyserRecipeHandler.class)//don't want subclasses getting a hold of this
        {
            List<AutomationRecipes.ElRecipe> recipes = AutomationRecipes.getElectrolyserRecipes();
            for (AutomationRecipes.ElRecipe rcp : recipes) {
            	arecipes.add(new Recipe(rcp));
            }
        } else {
            super.loadCraftingRecipes(outputId, results);
        }
    }
    
    @Override
    public void loadCraftingRecipes(ItemStack result)
    {
    	List<AutomationRecipes.ElRecipe> recipes = AutomationRecipes.getElectrolyserRecipes();
        for (AutomationRecipes.ElRecipe rcp : recipes) {
            if (Utils.isOrContains(result, rcp.out0) || Utils.isOrContains(result, rcp.out1))
                arecipes.add(new Recipe(rcp));
        }
    }
    
    @Override
    public void loadUsageRecipes(ItemStack ingredient)
    {
    	List<AutomationRecipes.ElRecipe> recipes = AutomationRecipes.getElectrolyserRecipes();
        for (AutomationRecipes.ElRecipe rcp : recipes) {
            if (Utils.isOrContains(ingredient, rcp.in))
                arecipes.add(new Recipe(rcp));
        }
    }

    @Override
    public void drawExtras(int recipe)
    {
    	GuiDraw.drawTexturedModalRect(62 + ofsX, 16 + ofsY, 184, 0, 16, 52);
    	GuiDraw.drawTexturedModalRect(107 + ofsX, 16 + ofsY, 184, 0, 16, 52);
    	GuiDraw.drawTexturedModalRect(152 + ofsX, 16 + ofsY, 184, 0, 16, 52);
        drawProgressBar(8 + ofsX, 16 + ofsY, 176, 0, 8, 52, 100, 7);
        drawProgressBar(81 + ofsX, 55 + ofsY, 200, 0, 24, 10, 20, 2);
        drawProgressBar(125 + ofsX, 55 + ofsY, 200, 10, 24, 10, 20, 0);
        GuiDraw.drawStringC(((Recipe)arecipes.get(recipe)).energy + "kJ", 34 + ofsX, 38 + ofsY, 0x404040, false);
    }
        
    @Override
    public String getOverlayIdentifier()
    {
        return "electrolyser";
    }
    
}
