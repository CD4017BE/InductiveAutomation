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
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
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
        if (item.stackTagCompound == null) return super.getItemStackDisplayName(item);
        else return super.getItemStackDisplayName(item) + " " + item.stackTagCompound.getString("name");
    }
    
    @Override
    public void addInformation(ItemStack item, EntityPlayer player, List list, boolean b) 
    {
        if (item.stackTagCompound != null)
        {
            list.add("x = " + item.stackTagCompound.getInteger("x"));
            list.add("y = " + item.stackTagCompound.getInteger("y"));
            list.add("z = " + item.stackTagCompound.getInteger("z"));
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
        if (item.stackTagCompound == null) item.stackTagCompound = new NBTTagCompound();
        if (!player.isSneaking()) {
            item.stackTagCompound.setInteger("pos.getX()", pos.getX());
            item.stackTagCompound.setInteger("pos.getY()", pos.getY());
            item.stackTagCompound.setInteger("pos.getZ()", pos.getZ());
            player.addChatMessage(new ChatComponentText("Target set to: pX=" + pos.getX() + " pY=" + pos.getY() + " pZ=" + pos.getZ()));
        } else {
            item.stackTagCompound.setInteger("pos.getX()", pos.getX() - item.stackTagCompound.getInteger("x"));
            item.stackTagCompound.setInteger("pos.getY()", pos.getY() - item.stackTagCompound.getInteger("y"));
            item.stackTagCompound.setInteger("pos.getZ()", pos.getZ() - item.stackTagCompound.getInteger("z"));
            player.addChatMessage(new ChatComponentText("Translation set to: dX=" + pos.getX() + " dY=" + pos.getY() + " dZ=" + pos.getZ()));
        }
        return true;
    }
    
}
