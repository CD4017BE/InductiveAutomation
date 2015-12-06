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
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

import org.lwjgl.input.Keyboard;

import cd4017be.automation.Block.BlockLiquidPipe;
import cd4017be.lib.BlockItemRegistry;
import cd4017be.lib.TooltipInfo;

    
/**
 *
 * @author CD4017BE
 */
public class ItemLiquidPipe extends ItemBlock
{
    
    public ItemLiquidPipe(Block id)
    {
        super(id);
        this.setHasSubtypes(true);
        BlockItemRegistry.registerItemStack(new ItemStack(this, 1, BlockLiquidPipe.ID_Transport), "liquidPipeT");
        BlockItemRegistry.registerItemStack(new ItemStack(this, 1, BlockLiquidPipe.ID_Extraction), "liquidPipeE");
        BlockItemRegistry.registerItemStack(new ItemStack(this, 1, BlockLiquidPipe.ID_Injection), "liquidPipeI");
    }

    @Override
    public String getItemStackDisplayName(ItemStack item) 
    {
        if (item.getItemDamage() == BlockLiquidPipe.ID_Transport) return "Liquid Transport Pipe";
        else if (item.getItemDamage() == BlockLiquidPipe.ID_Extraction) return "Liquid Extraction Pipe";
        else if (item.getItemDamage() == BlockLiquidPipe.ID_Injection) return "Liquid Injection Pipe";
        else return "Invalid Item";
    }
    
    @Override
    public void addInformation(ItemStack item, EntityPlayer player, List list, boolean b) 
    {
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            String s = TooltipInfo.getInfo(this.getUnlocalizedName(item) + ":" + item.getItemDamage());
            if (s != null) list.addAll(Arrays.asList(s.split("\n")));
        } else list.add("<SHIFT for info>");
        super.addInformation(item, player, list, b);
    }

    @Override
    public int getMetadata(int dmg) 
    {
        return dmg;
    }
    
    @Override
    public IIcon getIconFromDamage(int m) 
    {
        return this.field_150939_a.getIcon(0, m);
    }

}
