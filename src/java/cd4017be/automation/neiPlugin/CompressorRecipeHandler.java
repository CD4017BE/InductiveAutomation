/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cd4017be.automation.neiPlugin;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import cd4017be.api.automation.AutomationRecipes;
import cd4017be.api.automation.AutomationRecipes.CmpRecipe;
import cd4017be.lib.templates.GuiMachine;
import cd4017be.lib.util.Obj2;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.oredict.OreDictionary;
import codechicken.nei.NEIServerUtils;
import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.TemplateRecipeHandler;

/**
 *
 * @author CD4017BE
 */
public class CompressorRecipeHandler extends TemplateRecipeHandler
{
    private static final int ofsX = -5;
    private static final int ofsY = -11;
	
    @Override
    public String getGuiTexture() 
    {
        return new ResourceLocation("automation", "textures/gui/recipesNEI/compressor.png").toString();
    }

    @Override
    public String getRecipeName() 
    {
        return "Compression Assembler";
    }
    
    public class Recipe extends CachedRecipe
    {
    	final PositionedStack output;
    	final List<PositionedStack> input;
    	public Recipe(CmpRecipe recipe)
    	{
    		this.output = new PositionedStack(recipe.output, 134 + ofsX, 34 + ofsY);
    		this.input = new ArrayList<PositionedStack>();
    		ItemStack[] items;
    		for (int i = 0; i < recipe.input.length; i++){
    			if (recipe.input[i] == null)  continue;
    			items = Utils.getItems(recipe.input[i]);
    			if (items != null && items.length > 0) this.input.add(new PositionedStack(items, 62 + (i % 2) * 18 + ofsX, 25 + (i / 2) * 18 + ofsY, false));
    		}
    	}
    	
		@Override
		public PositionedStack getResult() 
		{
			return this.output;
		}

		@Override
		public List<PositionedStack> getIngredients() 
		{
			return this.getCycledIngredients(CompressorRecipeHandler.this.cycleticks / 20, this.input);
		}
    	
    }

    @Override
    public void loadTransferRects()
    {
        transferRects.add(new RecipeTransferRect(new Rectangle(97 + ofsX, 33 + ofsY, 36, 18), "compAssemble"));
    }
    
    @Override
    public Class<? extends GuiContainer> getGuiClass()
    {
        return GuiMachine.class;
    }

    @Override
    public void loadCraftingRecipes(String outputId, Object... results)
    {
        if(outputId.equals("compAssemble") && getClass() == CompressorRecipeHandler.class)//don't want subclasses getting a hold of this
        {
            List<CmpRecipe> recipes = AutomationRecipes.getCompressorRecipes();
            for (CmpRecipe rcp : recipes) {
            	arecipes.add(new Recipe(rcp));
            }
        } else {
            super.loadCraftingRecipes(outputId, results);
        }
    }
    
    @Override
    public void loadCraftingRecipes(ItemStack result)
    {
    	List<CmpRecipe> recipes = AutomationRecipes.getCompressorRecipes();
        for (CmpRecipe rcp : recipes) {
        	if (NEIServerUtils.areStacksSameTypeCrafting(rcp.output, result))
        		arecipes.add(new Recipe(rcp));
        }
    }
    
    @Override
    public void loadUsageRecipes(ItemStack ingredient)
    {
    	List<CmpRecipe> recipes = AutomationRecipes.getCompressorRecipes();
        for (CmpRecipe rcp : recipes) {
        	for (Object obj : rcp.input) {
        		if (obj == null) continue;
        		else if (obj instanceof ItemStack && !NEIServerUtils.areStacksSameTypeCrafting((ItemStack)obj, ingredient)) continue;
        		else if (obj instanceof Obj2) {
        			Obj2<Short, Integer> stack = (Obj2<Short, Integer>)obj;
        			boolean match = false;
        			for (int ore : OreDictionary.getOreIDs(ingredient)) 
                		if (ore == stack.objB) {
                			match = true;
                			break;
                		}
                	if (!match) continue;
        		}
        		Recipe r = new Recipe(rcp);
        		r.setIngredientPermutation(r.input, ingredient);
        		arecipes.add(r);
        		break;
        	}
        }
        
    }

    @Override
    public void drawExtras(int recipe)
    {
        drawProgressBar(26 + ofsX, 16 + ofsY, 176, 0, 8, 52, 100, 7);
        drawProgressBar(99 + ofsX, 37 + ofsY, 184, 0, 32, 10, 20, 0);
    }
        
    @Override
    public String getOverlayIdentifier()
    {
        return "compAssemble";
    }
    
}
