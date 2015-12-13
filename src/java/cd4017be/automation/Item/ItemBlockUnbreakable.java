/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cd4017be.automation.Item;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

import cd4017be.lib.BlockItemRegistry;
import cd4017be.lib.DefaultItemBlock;

/**
 *
 * @author CD4017BE
 */
public class ItemBlockUnbreakable extends DefaultItemBlock
{
    
    public ItemBlockUnbreakable(Block id)
    {
    	super(id);
    	this.setHasSubtypes(true);
        for (int i = 0; i < 16; i++)
            BlockItemRegistry.registerItemStack(new ItemStack(this, 1, i), "unbrStone" + Integer.toHexString(i));
    }
    
    @Override
    public int getMetadata(int dmg) 
    {
        return dmg;
    }

}
