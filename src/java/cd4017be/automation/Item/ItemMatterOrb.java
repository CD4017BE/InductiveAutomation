/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cd4017be.automation.Item;

import java.util.List;

import cd4017be.api.automation.MatterOrbItemHandler;
import cd4017be.api.automation.MatterOrbItemHandler.IMatterOrb;
import cd4017be.lib.DefaultItemBlock;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;

/**
 *
 * @author CD4017BE
 */
public class ItemMatterOrb extends DefaultItemBlock implements IMatterOrb
{
    public static final int MaxTypes = 256;
    
    public ItemMatterOrb(Block id)
    {
        super(id);
        this.setMaxStackSize(1);
    }

    @Override
	public EnumRarity getRarity(ItemStack item) 
    {
		return EnumRarity.UNCOMMON;
	}
    
    @Override
    public void addInformation(ItemStack item, EntityPlayer player, List list, boolean b) 
    {
        MatterOrbItemHandler.addInformation(item, list);
        super.addInformation(item, player, list, b);
    }
    
    @Override
    public int getMaxTypes(ItemStack item) 
    {
        return MaxTypes;
    }

    @Override
    public String getMatterTag(ItemStack item) 
    {
        return "matter";
    }
    
}
