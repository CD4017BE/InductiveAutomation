/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cd4017be.automation.Block;

import java.util.List;

import cd4017be.automation.Automation;
import cd4017be.automation.Item.ItemOre;
import cd4017be.lib.DefaultBlock;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

/**
 *
 * @author CD4017BE
 */
public class BlockOre extends DefaultBlock
{
    public static final byte ID_Silver = 0;
    public static final byte ID_Copper = 1;
    
    public BlockOre(String id)
    {
        super(id, Material.rock, ItemOre.class, "ore/silver", "ore/copper");
        this.setCreativeTab(Automation.tabAutomation);
    }

    @Override
    public IIcon getIcon(int s, int m) 
    {
        return this.getIconN(m);
    }

    @Override
    public void getSubBlocks(Item par1, CreativeTabs par2CreativeTabs, List list) 
    {
        list.add(new ItemStack(this, 1, ID_Silver));
        list.add(new ItemStack(this, 1, ID_Copper));
    }

    @Override
    public int damageDropped(int m) 
    {
        return m;
    }
    
}
