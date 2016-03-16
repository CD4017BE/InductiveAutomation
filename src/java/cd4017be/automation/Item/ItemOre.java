/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cd4017be.automation.Item;

import cd4017be.automation.Block.BlockOre.Ore;
import cd4017be.lib.BlockItemRegistry;
import cd4017be.lib.DefaultItemBlock;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

/**
 *
 * @author CD4017BE
 */
public class ItemOre extends DefaultItemBlock
{
    public ItemOre(Block id)
    {
        super(id);
        this.setHasSubtypes(true);
        BlockItemRegistry.registerItemStack(new ItemStack(this, 1, Ore.Silver.ordinal()), "oreSilver");
        BlockItemRegistry.registerItemStack(new ItemStack(this, 1, Ore.Copper.ordinal()), "oreCopper");
    }

    @Override
    public int getMetadata(int dmg) 
    {
        return dmg;
    }
    
}
