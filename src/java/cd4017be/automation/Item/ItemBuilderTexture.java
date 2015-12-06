/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cd4017be.automation.Item;

import java.util.List;

import cd4017be.automation.Automation;
import cd4017be.lib.DefaultItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

/**
 *
 * @author CD4017BE
 */
public class ItemBuilderTexture extends DefaultItem
{
    
    public ItemBuilderTexture(String id, String tex)
    {
        super(id);
        this.setTextureName(tex);
        this.setCreativeTab(Automation.tabAutomation);
    }

    @Override
    public void addInformation(ItemStack item, EntityPlayer player, List list, boolean b) 
    {
        if (item.stackTagCompound != null) list.add(item.stackTagCompound.getString("name"));
        super.addInformation(item, player, list, b);
    }
    
}
