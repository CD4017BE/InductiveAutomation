/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cd4017be.automation.Item;

import cd4017be.automation.Config;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

/**
 *
 * @author CD4017BE
 */
public class ItemHugeTank extends ItemTank
{
    
    public ItemHugeTank(Block id)
    {
        super(id);
    }

    @Override
    public int getCapacity(ItemStack item) 
    {
        return Config.tankCap[3];
    }
    
}
