package cd4017be.automation.neiPlugin;

import java.awt.Rectangle;
import java.util.List;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import cd4017be.api.automation.AntimatterItemHandler;
import cd4017be.api.automation.AutomationRecipes;
import cd4017be.api.automation.AutomationRecipes.GCRecipe;
import cd4017be.api.automation.AutomationRecipes.LFRecipe;
import cd4017be.automation.Entity.EntityAntimatterExplosion1;
import cd4017be.automation.TileEntity.GraviCond;
import cd4017be.automation.neiPlugin.BedrockRecipeHandler.Recipe;
import cd4017be.lib.BlockItemRegistry;
import cd4017be.lib.templates.GuiMachine;
import cd4017be.lib.util.Utils;
import codechicken.lib.gui.GuiDraw;
import codechicken.nei.NEIServerUtils;
import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.TemplateRecipeHandler;
import codechicken.nei.recipe.TemplateRecipeHandler.CachedRecipe;
import codechicken.nei.recipe.TemplateRecipeHandler.RecipeTransferRect;

public class GraviCondenserRecipeHandler extends TemplateRecipeHandler 
{

	private static final int ofsX = -5;
    private static final int ofsY = -11;
    
    @Override
    public String getGuiTexture() 
    {
        return new ResourceLocation("automation", "textures/gui/recipesNEI/graviCondenser.png").toString();
    }

    @Override
    public String getRecipeName() 
    {
        return "Gravitational Condenser";
    }
    
    public class Recipe extends CachedRecipe
    {
    	private final PositionedStack out;
        private final PositionedStack in;
        public final int mass;
        public final int force;
        public final int energy;
        
        public Recipe(GCRecipe rcp)
        {
            this.out = new PositionedStack(rcp.input, ofsX + 107, ofsY + 34);
            this.in = new PositionedStack(rcp.output, ofsX + 143, ofsY + 34);
            this.mass = rcp.matter;
            this.force = (int)Math.floor((float)this.mass * GraviCond.forceEnergy);
            this.energy = (int)Math.floor((float)this.mass * GraviCond.energyCost / 1000F);
        }
        
        @Override
        public PositionedStack getResult() 
        {
        	return in;
        }

        @Override
        public PositionedStack getIngredient() 
        {
            return out;
        }
    	
    }

    @Override
    public void loadTransferRects()
    {
    	transferRects.add(new RecipeTransferRect(new Rectangle(124 + ofsX, 33 + ofsY, 18, 18), "graviCond"));
    }
    
    @Override
    public Class<? extends GuiContainer> getGuiClass()
    {
        return GuiMachine.class;
    }

    @Override
    public void loadCraftingRecipes(String outputId, Object... results)
    {
        if(outputId.equals("graviCond") && getClass() == GraviCondenserRecipeHandler.class)//don't want subclasses getting a hold of this
        {
            List<GCRecipe> recipes = AutomationRecipes.getGraviCondRecipes();
            for (GCRecipe rcp : recipes) {
            	arecipes.add(new Recipe(rcp));
            }
        } else {
            super.loadCraftingRecipes(outputId, results);
        }
    }
    
    @Override
    public void loadCraftingRecipes(ItemStack result)
    {
    	List<GCRecipe> recipes = AutomationRecipes.getGraviCondRecipes();
        for (GCRecipe rcp : recipes) {
        	if (NEIServerUtils.areStacksSameTypeCrafting(result, rcp.output)) {
            	arecipes.add(new Recipe(rcp));
        	}
        }
    }
    
    @Override
    public void loadUsageRecipes(ItemStack ingredient)
    {
    	List<GCRecipe> recipes = AutomationRecipes.getGraviCondRecipes();
        for (GCRecipe rcp : recipes) {
        	if (NEIServerUtils.areStacksSameTypeCrafting(rcp.input, ingredient)) {
        		arecipes.add(new Recipe(rcp));
        	}
        }
    }

    @Override
    public void drawExtras(int recipe)
    {
    	int i = ((Recipe)arecipes.get(recipe)).force;
    	int n = (int)((long)i * 70L / (long)(GraviCond.maxVoltage * GraviCond.maxVoltage));
    	GuiDraw.drawTexturedModalRect(17 + ofsX, 16 + ofsY, 176, 0, n, 12);
    	drawProgressBar(17 + ofsX, 30 + ofsY, 176, 12, 70, 12, 100, 0);
        drawProgressBar(124 + ofsX, 37 + ofsY, 176, 24, 18, 10, 100, 0);
    	GuiDraw.drawStringC((i / 1000) + " kJ", 52 + ofsX, 18 + ofsY, 0x404040, false);
        GuiDraw.drawStringC("Consuming:", 52 + ofsX, 44 + ofsY, 0x404040, false);
        GuiDraw.drawStringC(Utils.formatNumber((double)((Recipe)arecipes.get(recipe)).mass * 1000D, 4, 0) + "g", 52 + ofsX, 52 + ofsY, 0x404040, false);
        GuiDraw.drawStringC("+ " + ((Recipe)arecipes.get(recipe)).energy + "kJ", 52 + ofsX, 60 + ofsY, 0x404040, false);
    }
        
    @Override
    public String getOverlayIdentifier()
    {
        return "graviCond";
    }

}
