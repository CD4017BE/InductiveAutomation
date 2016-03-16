/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cd4017be.automation.Item;

import java.util.List;

import cd4017be.lib.DefaultItemBlock;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;

/**
 *
 * @author CD4017BE
 */
public class ItemInterdimHole extends DefaultItemBlock
{
    
    public ItemInterdimHole(Block id)
    {
        super(id);
    }
    
    @Override
	public EnumRarity getRarity(ItemStack item) 
    {
		return EnumRarity.RARE;
	}

    @Override
    public String getItemStackDisplayName(ItemStack item) 
    {
        return (item.getItemDamage() != 0 ? "Linked" : "" ) + super.getItemStackDisplayName(item);
    }

    @Override
    public void addInformation(ItemStack item, EntityPlayer player, List list, boolean f) 
    {
        if (item.getItemDamage() != 0 && item.stackTagCompound != null) {
            list.add(String.format("Linked: x= %d ,y= %d ,z= %d in dim %d", item.stackTagCompound.getInteger("lx"), item.stackTagCompound.getInteger("ly"), item.stackTagCompound.getInteger("lz"), item.stackTagCompound.getInteger("ld")));
        }
        super.addInformation(item, player, list, f);
    }
    
}
