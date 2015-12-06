/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cd4017be.automation.Block;

import java.util.List;

import cd4017be.automation.Item.ItemItemPipe;
import cd4017be.lib.templates.BlockPipe;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 *
 * @author CD4017BE
 */
public class BlockItemPipe extends BlockPipe 
{
    
    public static final byte ID_Transport = 0;
    public static final byte ID_Extraction = 2;
    public static final byte ID_Injection = 1;
    
    public BlockItemPipe(String id, Material m, int type)
    {
        super(id, m, ItemItemPipe.class, type, "pipes/itemTr", "pipes/itemIn", "pipes/itemEx");
    }
    
    @Override
    public void getSubBlocks(Item par1, CreativeTabs par2CreativeTabs, List list) 
    {
        list.add(new ItemStack(this, 1, ID_Transport));
        list.add(new ItemStack(this, 1, ID_Extraction));
        list.add(new ItemStack(this, 1, ID_Injection));
    }

    @Override
    public int damageDropped(int m) 
    {
        return m;
    }
}
