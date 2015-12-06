/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cd4017be.automation.Item;

import cd4017be.automation.Block.BlockOre;
import cd4017be.lib.BlockItemRegistry;
import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

/**
 *
 * @author CD4017BE
 */
public class ItemOre extends ItemBlock
{
    public ItemOre(Block id)
    {
        super(id);
        this.setHasSubtypes(true);
        BlockItemRegistry.registerItemStack(new ItemStack(this, 1, BlockOre.ID_Silver), "oreSilver");
        BlockItemRegistry.registerItemStack(new ItemStack(this, 1, BlockOre.ID_Copper), "oreCopper");
    }

    @Override
    public String getItemStackDisplayName(ItemStack item) 
    {
        if (item.getItemDamage() == BlockOre.ID_Copper) return "Copper Ore";
        else if (item.getItemDamage() == BlockOre.ID_Silver) return "Silver Ore";
        else return "Invalid Item";
    }

    @Override
    public int getMetadata(int dmg) 
    {
        return dmg;
    }
    
}
