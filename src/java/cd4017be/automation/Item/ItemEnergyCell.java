/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cd4017be.automation.Item;

import java.util.List;

import cd4017be.api.automation.EnergyItemHandler;
import cd4017be.api.automation.EnergyItemHandler.IEnergyItem;
import cd4017be.automation.Automation;
import cd4017be.lib.DefaultItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

/**
 *
 * @author CD4017BE
 */
public class ItemEnergyCell extends DefaultItem implements IEnergyItem
{
    private final int storage;
    
    public ItemEnergyCell(String id, String tex, int es)
    {
        super(id);
        this.setTextureName(tex);
        this.setCreativeTab(Automation.tabAutomation);
        this.storage = es;
        this.setMaxDamage(16);
    }
    
    @Override
    public void addInformation(ItemStack item, EntityPlayer player, List list, boolean f) 
    {
        EnergyItemHandler.addInformation(item, list);
        super.addInformation(item, player, list, f);
    }

    @Override
    public int getEnergyCap(ItemStack item) 
    {
        return storage;
    }

    @Override
    public int getChargeSpeed(ItemStack item) 
    {
        return 160;
    }

    @Override
    public String getEnergyTag(ItemStack item) 
    {
        return "energy";
    }
    
}
