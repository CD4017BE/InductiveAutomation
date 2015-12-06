/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cd4017be.automation.neiPlugin;

import cd4017be.api.automation.AntimatterItemHandler;
import cd4017be.automation.Entity.EntityAntimatterExplosion1;
import cd4017be.lib.BlockItemRegistry;
import cd4017be.lib.templates.GuiMachine;
import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.TemplateRecipeHandler;
import net.minecraft.block.Block;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

/**
 *
 * @author CD4017BE
 */
public class BedrockRecipeHandler extends TemplateRecipeHandler
{
    private static final int ofsX = -5;
    private static final int ofsY = -11;
    private static final float neededAm;
    static {
        float i = (2 * EntityAntimatterExplosion1.Density + 1) / EntityAntimatterExplosion1.Density;
        neededAm = Blocks.bedrock.getExplosionResistance(null) / EntityAntimatterExplosion1.PowerFactor / EntityAntimatterExplosion1.explMult * i * i * 9.375F;
    }
    
    @Override
    public String getGuiTexture() 
    {
        return new ResourceLocation("automation", "textures/gui/recipesNEI/antimatterBomb.png").toString();
    }

    @Override
    public String getRecipeName() 
    {
        return "Antimatter Bomb";
    }
    
    public class Recipe extends CachedRecipe
    {
    	private final PositionedStack out;
        private final PositionedStack in;
        
        public Recipe()
        {
            ItemStack item = BlockItemRegistry.stack("tile.antimatterBombF", 1);
            AntimatterItemHandler.addAntimatter(item, (int)neededAm);
            this.out = new PositionedStack(item, ofsX + 88, ofsY + 36);
            this.in = new PositionedStack(new ItemStack(Blocks.bedrock), 72 + ofsX, 36 + ofsY);
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
        
    }
    
    @Override
    public Class<? extends GuiContainer> getGuiClass()
    {
        return GuiMachine.class;
    }

    @Override
    public void loadCraftingRecipes(String outputId, Object... results)
    {
        if(outputId.equals("bedrock") && getClass() == BedrockRecipeHandler.class)//don't want subclasses getting a hold of this
        {
            this.loadCraftingRecipes(new ItemStack(Blocks.bedrock));
        } else {
            super.loadCraftingRecipes(outputId, results);
        }
    }
    
    @Override
    public void loadCraftingRecipes(ItemStack result)
    {
    	if (result != null && result.getItem() == Item.getItemFromBlock(Blocks.bedrock)) {
            arecipes.add(new Recipe());
        }
    }
    
    @Override
    public void loadUsageRecipes(ItemStack ingredient)
    {
    	if (ingredient != null && ingredient.getItem() == Item.getItemFromBlock(BlockItemRegistry.blockId("tile.antimatterBombF"))) {
            arecipes.add(new Recipe());
        }
    }

    @Override
    public void drawExtras(int recipe)
    {
        
    }
        
    @Override
    public String getOverlayIdentifier()
    {
        return "bedrock";
    }
}
