/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cd4017be.automation.Item;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;

import cd4017be.automation.Automation;
import cd4017be.automation.Gui.ContainerItemMatterInterface;
import cd4017be.automation.Gui.GuiItemMatterInterface;
import cd4017be.lib.BlockGuiHandler;
import cd4017be.lib.DefaultItem;
import cd4017be.lib.IGuiItem;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

/**
 *
 * @author CD4017BE
 */
public class ItemMatterInterface extends DefaultItem implements IGuiItem
{
    
    public ItemMatterInterface(String id)
    {
        super(id);
        this.setCreativeTab(Automation.tabAutomation);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(ItemStack item, World world, EntityPlayer player, EnumHand hand) 
    {
        BlockGuiHandler.openItemGui(player, world, 0, -1, 0);
        return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, item);
    }

    @Override
    public EnumActionResult onItemUse(ItemStack item, EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float X, float Y, float Z) 
    {
        BlockGuiHandler.openItemGui(player, world, pos.getX(), pos.getY(), pos.getZ());
        return EnumActionResult.SUCCESS;
    }
    
    @Override
    public Container getContainer(World world, EntityPlayer player, int x, int y, int z) 
    {
        return new ContainerItemMatterInterface(player);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public GuiContainer getGui(World world, EntityPlayer player, int x, int y, int z) 
    {
        return new GuiItemMatterInterface(new ContainerItemMatterInterface(player));
    }

    @Override
    public void onPlayerCommand(World world, EntityPlayer player, PacketBuffer dis) throws IOException
    {
        if (player.openContainer != null && player.openContainer instanceof ContainerItemMatterInterface) {
            ((ContainerItemMatterInterface)player.openContainer).inventory.onCommand(dis);
        }
    }
    
}
