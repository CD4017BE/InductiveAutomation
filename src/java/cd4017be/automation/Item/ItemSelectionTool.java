/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cd4017be.automation.Item;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import cd4017be.api.automation.IOperatingArea;
import cd4017be.automation.Automation;
import cd4017be.automation.Gui.ContainerAreaUpgrade;
import cd4017be.automation.Gui.GuiAreaUpgrade;
import cd4017be.lib.BlockGuiHandler;
import cd4017be.lib.DefaultItem;
import cd4017be.lib.IGuiItem;
import cd4017be.lib.util.Utils;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

/**
 *
 * @author CD4017BE
 */
public class ItemSelectionTool extends DefaultItem implements IGuiItem
{
    private static final String[] modes = {"Set Point", "Add Point", "Expand", "Contract", "Move", "Configure"};
    
    public ItemSelectionTool(String id, String tex)
    {
        super(id);
        this.setTextureName(tex);
        this.setCreativeTab(Automation.tabAutomation);
        this.setMaxStackSize(1);
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int s, float X, float Y, float Z) {
        if (!world.isRemote) onClick(stack, player, true, x, y, z);
        return true;
    }


    @Override
    public ItemStack onItemRightClick(ItemStack item, World world, EntityPlayer player) 
    {
        if (!world.isRemote) onClick(item, player, false, (int)player.posX, (int)player.posY, (int)player.posZ);
        return item;
    }
    
    private void onClick(ItemStack item, EntityPlayer player, boolean b, int x, int y, int z)
    {
        if (item.stackTagCompound == null) return;
        byte mode = item.stackTagCompound.getByte("mode");
        if (player.isSneaking())
        {
            if (b)
            {
                TileEntity te = player.worldObj.getTileEntity(x, y, z);
                if (mode == 5 && te != null && te instanceof IOperatingArea)
                {
                    BlockGuiHandler.openItemGui(player, player.worldObj, x, y, z);
                    return;
                } else
                if (item.stackTagCompound.getInteger("mx") == x && item.stackTagCompound.getInteger("my") == y
                    && item.stackTagCompound.getInteger("mz") == z && te != null && te instanceof IOperatingArea)
                {
                    item.stackTagCompound.setInteger("mx", 0);
                    item.stackTagCompound.setInteger("my", -1);
                    item.stackTagCompound.setInteger("mz", 0);
                    player.addChatMessage(new ChatComponentText("Unlinked Machine"));
                    return;
                } else
                if (te != null && te instanceof IOperatingArea)
                {
                    player.addChatMessage(new ChatComponentText("Linked Machine"));
                    int[] oa = ((IOperatingArea)te).getOperatingArea();
                    item.stackTagCompound.setInteger("mx", x);
                    item.stackTagCompound.setInteger("my", y);
                    item.stackTagCompound.setInteger("mz", z);
                    this.storeArea(oa, item);
                    return;
                }
            }
            mode++;
            if (mode >= modes.length) mode = 0;
            item.stackTagCompound.setByte("mode", mode);
            return;
        }
        String msg = null;
        int[] area = new int[6];
        IOperatingArea mach = this.loadArea(area, item, player.worldObj);
        if (mode == 0)
        {
            area[0] = x; area[3] = x + 1;
            area[1] = y; area[4] = y + 1;
            area[2] = z; area[5] = z + 1;
            msg = "Area set to: X=" + x + " Y=" + y + " Z=" + z;
        } else
        if (mode == 1)
        {
            if (x < area[0]) area[0] = x;
            if (y < area[1]) area[1] = y;
            if (z < area[2]) area[2] = z;
            if (x >= area[3]) area[3] = x + 1;
            if (y >= area[4]) area[4] = y + 1;
            if (z >= area[5]) area[5] = z + 1;
            msg = "Point added: X=" + x + " Y=" + y + " Z=" + z;
        } else
        if (mode < 5)
        {
            byte look = Utils.getLookDir(player);
            if (mode == 4) {
                int d = look >> 1;
                int o = (look & 1) == 1 ? -1 : 1;
                if (d == 0){area[1] += o; area[4] += o;}
                else if (d == 1){area[2] += o; area[5] += o;}
                else if (d == 2){area[0] += o; area[3] += o;}
            } else if (mode == 2) {
                if (look == 1) area[1]--;
                else if (look == 0) area[4]++;
                else if (look == 3) area[2]--;
                else if (look == 2) area[5]++;
                else if (look == 5) area[0]--;
                else if (look == 4) area[3]++;
            } else if (mode == 3) {
                if (look == 1 && area[1] < area[4]) area[1]++;
                else if (look == 0 && area[1] < area[4]) area[4]--;
                else if (look == 3 && area[2] < area[5]) area[2]++;
                else if (look == 2 && area[2] < area[5]) area[5]--;
                else if (look == 5 && area[0] < area[3]) area[0]++;
                else if (look == 4 && area[0] < area[3]) area[3]--;
            }
        } else if (mach != null) {
            BlockGuiHandler.openItemGui(player, player.worldObj, 0, -1, 0);
            return;
        }
        if (mach != null) {
        	if (IOperatingArea.Handler.setCorrectArea(mach, area, false)) {
        		this.storeArea(area, item);
        	} else {
        		player.addChatMessage(new ChatComponentText("Error: selection out of bounds!"));
        		return;
        	}
        } else this.storeArea(area, item);
        if (msg != null) player.addChatMessage(new ChatComponentText(msg));
    }
    
    @Override
    public void onUpdate(ItemStack item, World world, Entity par3Entity, int par4, boolean par5) 
    {
        if (item.stackTagCompound == null) item.stackTagCompound = new NBTTagCompound();
    }

    @Override
    public String getItemStackDisplayName(ItemStack item) 
    {
        if (item.stackTagCompound == null) return super.getItemStackDisplayName(item);
        return super.getItemStackDisplayName(item) + " (" + modes[item.stackTagCompound.getByte("mode")] + ")";
    }
    
    @Override
    public void addInformation(ItemStack item, EntityPlayer par2EntityPlayer, List list, boolean par4) 
    {
        if (item.stackTagCompound != null) {
            list.add("P1 = (" + item.stackTagCompound.getInteger("px0")
                    + "|" + item.stackTagCompound.getInteger("py0")
                    + "|" + item.stackTagCompound.getInteger("pz0") + ")");
            list.add("P2 = (" + item.stackTagCompound.getInteger("px1")
                    + "|" + item.stackTagCompound.getInteger("py1")
                    + "|" + item.stackTagCompound.getInteger("pz1") + ")");
            list.add("Link (" + item.stackTagCompound.getInteger("mx")
                    + "|" + item.stackTagCompound.getInteger("my")
                    + "|" + item.stackTagCompound.getInteger("mz") + ")");
        }
        super.addInformation(item, par2EntityPlayer, list, par4);
    }

	@Override
	public Container getContainer(World world, EntityPlayer player, int x, int y, int z) 
	{
		ItemStack item = player.getCurrentEquippedItem();
		if (item == null || item.stackTagCompound == null) return null;
		TileEntity te = world.getTileEntity(x, y, z);
		if (te == null || !(te instanceof IOperatingArea)) {
			te = world.getTileEntity(item.stackTagCompound.getInteger("mx"), item.stackTagCompound.getInteger("my"), item.stackTagCompound.getInteger("mz"));
			if (te == null || !(te instanceof IOperatingArea)) return null;
		}
		return new ContainerAreaUpgrade((IOperatingArea)te, new int[]{te.xCoord, te.yCoord, te.zCoord}, player);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public GuiContainer getGui(World world, EntityPlayer player, int x, int y, int z) 
	{
		Container c = this.getContainer(world, player, x, y, z);
		if (c == null || !(c instanceof ContainerAreaUpgrade)) return null;
		else return new GuiAreaUpgrade((ContainerAreaUpgrade)c);
	}

	@Override
	public void onPlayerCommand(World world, EntityPlayer player, DataInputStream dis) throws IOException 
	{
		if (player.openContainer != null && player.openContainer instanceof ContainerAreaUpgrade) {
			ContainerAreaUpgrade cont = (ContainerAreaUpgrade)player.openContainer;
			byte cmd = dis.readByte();
			if (cmd >= 0 && cmd < 6) {
				int[] area = cont.machine.getOperatingArea();
				area[cmd] = dis.readInt();
				IOperatingArea.Handler.setCorrectArea(cont.machine, area, true);
			} else if (cmd == 6) {
				ItemStack item = player.getCurrentEquippedItem();
				if (item != null && item.stackTagCompound != null) {
					int[] area = new int[6];
					this.loadArea(area, item, player.worldObj);
					if (!IOperatingArea.Handler.setCorrectArea(cont.machine, area, false)) {
						player.addChatMessage(new ChatComponentText("Error: selection out of max bounds!"));
					}
				}
			}
		}
		
	}
	
	private void storeArea(int[] area, ItemStack item)
	{
		item.stackTagCompound.setInteger("px0", area[0]);
        item.stackTagCompound.setInteger("py0", area[1]);
        item.stackTagCompound.setInteger("pz0", area[2]);
        item.stackTagCompound.setInteger("px1", area[3]);
        item.stackTagCompound.setInteger("py1", area[4]);
        item.stackTagCompound.setInteger("pz1", area[5]);
	}
	
	private IOperatingArea loadArea(int[] area, ItemStack item, World world)
	{
		TileEntity te = world.getTileEntity(item.stackTagCompound.getInteger("mx"), item.stackTagCompound.getInteger("my"), item.stackTagCompound.getInteger("mz"));
        if (te != null && te instanceof IOperatingArea) {
        	IOperatingArea mach = (IOperatingArea)te;
        	System.arraycopy(mach.getOperatingArea(), 0, area, 0, 6);
        	return mach;
        } else {
			area[0] = item.stackTagCompound.getInteger("px0");
			area[1] = item.stackTagCompound.getInteger("py0");
			area[2] = item.stackTagCompound.getInteger("pz0");
			area[3] = item.stackTagCompound.getInteger("px1");
			area[4] = item.stackTagCompound.getInteger("py1");
			area[5] = item.stackTagCompound.getInteger("pz1");
			return null;
        }
	}
    
}
