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
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

/**
 *
 * @author CD4017BE
 */
public class ItemTeleporterCoords extends DefaultItem
{
    
    public ItemTeleporterCoords(String id)
    {
        super(id);
        this.setCreativeTab(Automation.tabAutomation);
    }

    @Override
    public String getItemStackDisplayName(ItemStack item) 
    {
        if (item.getTagCompound() == null) return super.getItemStackDisplayName(item);
        else return super.getItemStackDisplayName(item) + " " + item.getTagCompound().getString("name");
    }
    
    @Override
    public void addInformation(ItemStack item, EntityPlayer player, List list, boolean b) 
    {
        if (item.getTagCompound() != null)
        {
            list.add("x = " + item.getTagCompound().getInteger("x"));
            list.add("y = " + item.getTagCompound().getInteger("y"));
            list.add("z = " + item.getTagCompound().getInteger("z"));
        }
        super.addInformation(item, player, list, b);
    }

    @Override
    public ItemStack onItemRightClick(ItemStack item, World world, EntityPlayer player) 
    {
        this.onItemUse(item, player, world, new BlockPos(player.posX, player.posY, player.posZ), EnumFacing.DOWN, 0, 0, 0);
        return item;
    }

    @Override
    public boolean onItemUse(ItemStack item, EntityPlayer player, World world, BlockPos pos, EnumFacing s, float X, float Y, float Z) 
    {
        if (world.isRemote) return true;
        if (item.getTagCompound() == null) item.setTagCompound(new NBTTagCompound());
        if (!player.isSneaking()) {
            item.getTagCompound().setInteger("pos.getX()", pos.getX());
            item.getTagCompound().setInteger("pos.getY()", pos.getY());
            item.getTagCompound().setInteger("pos.getZ()", pos.getZ());
            player.addChatMessage(new TextComponentString("Target set to: pX=" + pos.getX() + " pY=" + pos.getY() + " pZ=" + pos.getZ()));
        } else {
            item.getTagCompound().setInteger("pos.getX()", pos.getX() - item.getTagCompound().getInteger("x"));
            item.getTagCompound().setInteger("pos.getY()", pos.getY() - item.getTagCompound().getInteger("y"));
            item.getTagCompound().setInteger("pos.getZ()", pos.getZ() - item.getTagCompound().getInteger("z"));
            player.addChatMessage(new TextComponentString("Translation set to: dX=" + pos.getX() + " dY=" + pos.getY() + " dZ=" + pos.getZ()));
        }
        return true;
    }
    
}
