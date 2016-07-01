/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cd4017be.automation.Item;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;
import java.util.List;

import cd4017be.automation.Automation;
import cd4017be.automation.Gui.ContainerItemUpgrade;
import cd4017be.automation.Gui.GuiItemUpgrade;
import cd4017be.lib.BlockGuiHandler;
import cd4017be.lib.DefaultItem;
import cd4017be.lib.IGuiItem;
import cd4017be.lib.TooltipInfo;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

/**
 *
 * @author CD4017BE
 */
public class ItemItemUpgrade extends DefaultItem implements IGuiItem
{
    
    public ItemItemUpgrade(String id)
    {
        super(id);
        this.setCreativeTab(Automation.tabAutomation);
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void addInformation(ItemStack item, EntityPlayer player, List list, boolean b) {
		if (item.hasTagCompound()) {
			String[] states = I18n.translateToLocal("gui.cd4017be.filter.state").split(",");
			PipeUpgradeItem filter = PipeUpgradeItem.load(item.getTagCompound());
			String s;
			if (states.length >= 8) {
				s = states[(filter.mode & 1) == 0 ? 0 : 1];
				if ((filter.mode & 4) != 0) s += states[2];
				if ((filter.mode & 8) != 0) s += states[3];
				if ((filter.mode & 16) != 0) s += states[4];
				if ((filter.mode & 2) != 0) s += states[5];
				if ((filter.mode & 64) != 0) s += states[(filter.mode & 128) != 0 ? 7 : 6];
			} else s = "<invalid lang entry!>";
			list.add(s);
			boolean num = (filter.mode & 32) != 0;
			for (ItemStack stack : filter.list) list.add((num ? stack.stackSize + "x " : "> ") + stack.getDisplayName());
			if (filter.priority != 0) list.add(TooltipInfo.format("gui.cd4017be.priority", filter.priority));
		}
		super.addInformation(item, player, list, b);
	}

	@Override
    public ActionResult<ItemStack> onItemRightClick(ItemStack item, World world, EntityPlayer player, EnumHand hand) 
    {
        BlockGuiHandler.openItemGui(player, world, 0, -1, 0);
        return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, item);
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
    public void onPlayerCommand(World world, EntityPlayer player, PacketBuffer dis) throws IOException
    {
        if (player.openContainer != null && player.openContainer instanceof ContainerItemUpgrade) {
            ((ContainerItemUpgrade)player.openContainer).inventory.onCommand(dis);
        }
    }
    
}
