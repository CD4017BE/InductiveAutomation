/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cd4017be.automation.Item;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

import cd4017be.automation.Block.BlockItemPipe;
import cd4017be.lib.BlockItemRegistry;
import cd4017be.lib.DefaultItemBlock;

/**
 *
 * @author CD4017BE
 */
public class ItemItemPipe extends DefaultItemBlock 
{
    
    public ItemItemPipe(Block id)
    {
        super(id);
        this.setHasSubtypes(true);
        BlockItemRegistry.registerItemStack(new ItemStack(this, 1, BlockItemPipe.ID_Transport), "itemPipeT");
        BlockItemRegistry.registerItemStack(new ItemStack(this, 1, BlockItemPipe.ID_Extraction), "itemPipeE");
        BlockItemRegistry.registerItemStack(new ItemStack(this, 1, BlockItemPipe.ID_Injection), "itemPipeI");
    }

    @Override
    public int getMetadata(int dmg) 
    {
        return dmg;
    }
    
    @Override
    public IIcon getIconFromDamage(int m) 
    {
        return this.field_150939_a.getIcon(0, m);
    }

}
