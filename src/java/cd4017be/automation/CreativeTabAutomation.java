/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cd4017be.automation;

import cd4017be.lib.BlockItemRegistry;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 *
 * @author CD4017BE
 */
public class CreativeTabAutomation extends CreativeTabs
{
    
    public CreativeTabAutomation(String name)
    {
        super(CreativeTabs.getNextID(), name);
    }

    @Override
    public ItemStack getIconItemStack() 
    {
        return BlockItemRegistry.stack("tile.miner", 1);
    }

    @Override
    public String getTranslatedTabLabel() 
    {
        return "Inductive Automation";
    }

	@Override
	public Item getTabIconItem() 
	{
		return BlockItemRegistry.itemId("tile.miner");
	}
    
}
