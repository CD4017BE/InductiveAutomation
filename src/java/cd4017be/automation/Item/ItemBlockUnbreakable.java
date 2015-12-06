/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cd4017be.automation.Item;

import java.util.Arrays;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;

import org.lwjgl.input.Keyboard;

import cd4017be.lib.BlockItemRegistry;
import cd4017be.lib.TooltipInfo;

/**
 *
 * @author CD4017BE
 */
public class ItemBlockUnbreakable extends ItemBlock
{
    
    public ItemBlockUnbreakable(Block id)
    {
        super(id);
        this.setHasSubtypes(true);
        for (int i = 0; i < 16; i++)
            BlockItemRegistry.registerItemStack(new ItemStack(this, 1, i), "unbrStone" + Integer.toHexString(i));
    }

    @Override
    public String getItemStackDisplayName(ItemStack item) 
    {
        int m = ~item.getItemDamage() & 15;
        return ItemDye.field_150923_a[m] + " unbreakable Stone";
    }

    @Override
    public void addInformation(ItemStack item, EntityPlayer player, List list, boolean b) 
    {
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            String s = TooltipInfo.getInfo(this.getUnlocalizedName(item));
            if (s != null) list.addAll(Arrays.asList(s.split("\n")));
        } else list.add("<SHIFT for info>");
        super.addInformation(item, player, list, b);
    }
    
    @Override
    public int getMetadata(int dmg) 
    {
        return dmg;
    }

}
