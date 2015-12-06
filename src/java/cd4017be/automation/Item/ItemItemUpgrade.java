/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cd4017be.automation.Item;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import java.io.DataInputStream;
import java.io.IOException;

import cd4017be.automation.Automation;
import cd4017be.automation.Gui.ContainerItemUpgrade;
import cd4017be.automation.Gui.GuiItemUpgrade;
import cd4017be.lib.BlockGuiHandler;
import cd4017be.lib.DefaultItem;
import cd4017be.lib.IGuiItem;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/**
 *
 * @author CD4017BE
 */
public class ItemItemUpgrade extends DefaultItem implements IGuiItem
{
    
    public ItemItemUpgrade(String id, String tex)
    {
        super(id);
        this.setTextureName(tex);
        this.setCreativeTab(Automation.tabAutomation);
    }
    
    @Override
    public ItemStack onItemRightClick(ItemStack item, World world, EntityPlayer player) 
    {
        BlockGuiHandler.openItemGui(player, world, 0, -1, 0);
        return item;
    }

    @Override
    public boolean onItemUse(ItemStack item, EntityPlayer player, World world, int x, int y, int z, int s, float X, float Y, float Z) 
    {
        return false;
    }
    
    @Override
    public Container getContainer(World world, EntityPlayer player, int x, int y, int z) 
    {
        return new ContainerItemUpgrade(player);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public GuiContainer getGui(World world, EntityPlayer player, int x, int y, int z) 
    {
        return new GuiItemUpgrade(new ContainerItemUpgrade(player));
    }

    @Override
    public void onPlayerCommand(World world, EntityPlayer player, DataInputStream dis) throws IOException
    {
        if (player.openContainer != null && player.openContainer instanceof ContainerItemUpgrade) {
            ((ContainerItemUpgrade)player.openContainer).inventory.onCommand(dis);
        }
    }
    
}