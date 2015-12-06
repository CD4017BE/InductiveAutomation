/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cd4017be.automation.Item;

import java.util.List;

import cd4017be.api.automation.EnergyItemHandler;
import cd4017be.api.automation.EnergyItemHandler.IEnergyItem;
import cd4017be.automation.Config;
import cd4017be.automation.TileEntity.ESU;
import cd4017be.lib.BlockItemRegistry;
import cd4017be.lib.DefaultItemBlock;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 *
 * @author CD4017BE
 */
public class ItemESU extends DefaultItemBlock implements IEnergyItem
{
    
    public ItemESU(Block id)
    {
        super(id);
    }

    @Override
    public void addInformation(ItemStack item, EntityPlayer player, List list, boolean f) 
    {
        EnergyItemHandler.addInformation(item, list);
        super.addInformation(item, player, list, f);
    }

    @Override
	public void getSubItems(Item item, CreativeTabs tab, List list) 
    {
		super.getSubItems(item, tab, list);
		ItemStack stack = new ItemStack(item);
		EnergyItemHandler.addEnergy(stack, getEnergyCap(stack), false);
		list.add(stack);
	}

	@Override
    public int getEnergyCap(ItemStack item) 
    {
        return item.getItem() == Item.getItemFromBlock(BlockItemRegistry.blockId("tile.SCSU")) ? Config.Ecap[0] : item.getItem() == Item.getItemFromBlock(BlockItemRegistry.blockId("tile.OCSU")) ? Config.Ecap[1] : Config.Ecap[2];
    }

    @Override
    public int getChargeSpeed(ItemStack item) 
    {
        return item.getItem() == Item.getItemFromBlock(BlockItemRegistry.blockId("tile.SCSU")) ? 160 : item.getItem() == Item.getItemFromBlock(BlockItemRegistry.blockId("tile.OCSU")) ? 400 : 1000;
    }

    @Override
    public String getEnergyTag(ItemStack item) 
    {
        return "energy";
    }
    
}
