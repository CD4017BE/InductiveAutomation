package cd4017be.automation.neiPlugin;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import cd4017be.api.automation.AutomationRecipes;
import cd4017be.api.automation.AutomationRecipes.LFRecipe;
import cd4017be.lib.templates.GuiMachine;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import codechicken.lib.gui.GuiDraw;
import codechicken.nei.NEIServerUtils;
import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.TemplateRecipeHandler;

public class AdvFurnaceRecipeHandler extends TemplateRecipeHandler 
{
	private static final int ofsX = -5;
    private static final int ofsY = -11;
	
    @Override
    public String getGuiTexture() 
    {
        return new ResourceLocation("automation", "textures/gui/recipesNEI/advFurnace.png").toString();
    }

    @Override
    public String getRecipeName() 
    {
        return "Advanced Furnace";
    }
    
    public class Recipe extends CachedRecipe
    {
    	final PositionedStack reqOutput;
    	final List<PositionedStack> output;
    	final List<PositionedStack> input;
    	final int energy;
    	public Recipe(LFRecipe recipe)
    	{
    		this.energy = (int)recipe.energy;
    		this.output = new ArrayList<PositionedStack>();
    		this.input = new ArrayList<PositionedStack>();
    		if (recipe.Iinput != null) {
    			ItemStack[] items;
    			for (int i = 0; i < recipe.Iinput.length; i++) {
    				items = Utils.getItems(recipe.Iinput[i]);
    				if (items != null && items.length > 0) input.add(new PositionedStack(items, 71 + ofsX, 16 + i * 18 + ofsY));
                }
    		}
    		if (recipe.Ioutput != null)
                    for (int i = 0; i < recipe.Ioutput.length; i++) {
                            output.add(new PositionedStack(recipe.Ioutput[i], 125 + ofsX, 16 + i * 18 + ofsY));
                    }
    		if (recipe.Linput != null) {
    			input.add(new PositionedStack(Utils.getItem(recipe.Linput, true), 53 + ofsX, 52 + ofsY));
    		}
    		if (recipe.Loutput != null) {
    			output.add(new PositionedStack(Utils.getItem(recipe.Loutput, true), 143 + ofsX, 52 + ofsY));
    		}
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
			return this.getCycledIngredients(AdvFurnaceRecipeHandler.this.cycleticks / 20, this.input);
		}
    	
    }

    @Override
    public void loadTransferRects()
    {
        transferRects.add(new RecipeTransferRect(new Rectangle(88 + ofsX, 33 + ofsY, 36, 18), "advFurnace"));
    }
    
    @Override
    public Class<? extends GuiContainer> getGuiClass()
    {
        return GuiMachine.class;
    }

    @Override
    public void loadCraftingRecipes(String outputId, Object... results)
    {
        if(outputId.equals("advFurnace") && getClass() == AdvFurnaceRecipeHandler.class)//don't want subclasses getting a hold of this
        {
            List<LFRecipe> recipes = AutomationRecipes.getAdvancedFurnaceRecipes();
            for (LFRecipe rcp : recipes) {
            	arecipes.add(new Recipe(rcp));
            }
        } else {
            super.loadCraftingRecipes(outputId, results);
        }
    }
    
    @Override
    public void loadCraftingRecipes(ItemStack result)
    {
    	List<LFRecipe> recipes = AutomationRecipes.getAdvancedFurnaceRecipes();
        for (LFRecipe rcp : recipes) {
        	boolean add = false;
        	if (rcp.Ioutput != null) for (ItemStack item : rcp.Ioutput) {
        		if (NEIServerUtils.areStacksSameTypeCrafting(result, item)) {
            		add = true;
            		break;
            	}
        	}
        	if (add || Utils.isOrContains(result, rcp.Loutput)) {
        		arecipes.add(new Recipe(rcp));
        	}
        }
    }
    
    @Override
    public void loadUsageRecipes(ItemStack ingredient)
    {
    	List<LFRecipe> recipes = AutomationRecipes.getAdvancedFurnaceRecipes();
        for (LFRecipe rcp : recipes) {
        	boolean add = false;
        	if (rcp.Iinput != null) for (Object item : rcp.Iinput) {
        		if (AutomationRecipes.isItemEqual(ingredient, item)) {
        			add = true;
        			break;
        		}
        	}
        	if (add || Utils.isOrContains(ingredient, rcp.Linput)) {
        		arecipes.add(new Recipe(rcp));
        	}
        }
    }

    @Override
    public void drawExtras(int recipe)
    {
    	GuiDraw.drawTexturedModalRect(53 + ofsX, 16 + ofsY, 184, 0, 16, 52);
    	GuiDraw.drawTexturedModalRect(143 + ofsX, 16 + ofsY, 184, 0, 16, 52);
        drawProgressBar(26 + ofsX, 16 + ofsY, 176, 0, 8, 52, 100, 7);
        drawProgressBar(90 + ofsX, 37 + ofsY, 200, 0, 32, 10, 20, 0);
        GuiDraw.drawStringC(((Recipe)arecipes.get(recipe)).energy + "kJ", 106 + ofsX, 51 + ofsY, 0x404040, false);
    }
        
    @Override
    public String getOverlayIdentifier()
    {
        return "advFurnace";
    }
    
}
