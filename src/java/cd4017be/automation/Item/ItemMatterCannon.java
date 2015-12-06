/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cd4017be.automation.Item;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import cd4017be.api.automation.AreaProtect;
import cd4017be.api.automation.EnergyItemHandler;
import cd4017be.api.automation.MatterOrbItemHandler;
import cd4017be.api.automation.MatterOrbItemHandler.IMatterOrb;
import cd4017be.automation.Config;
import cd4017be.lib.BlockGuiHandler;
import cd4017be.lib.ClientInputHandler.IScrollHandlerItem;
import cd4017be.lib.IGuiItem;
import cd4017be.lib.util.Utils;
import net.minecraft.block.Block;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

/**
 *
 * @author CD4017BE
 */
public class ItemMatterCannon extends ItemEnergyCell implements IMatterOrb, IGuiItem, IScrollHandlerItem
{
    public static int EnergyUsage = 16;
    
    public ItemMatterCannon(String id, String tex)
    {
        super(id, tex, Config.Ecap[1]);
        this.setMaxStackSize(1);
    }
    
    @Override
	public EnumRarity getRarity(ItemStack item) 
    {
		return EnumRarity.epic;
	}
    
    @Override
    public int getChargeSpeed(ItemStack item) 
    {
        return 1000;
    }
    
    private static final String[] modes = {" basic", " fill", " extend", " copy"};
    
    @Override
    public String getItemStackDisplayName(ItemStack item) 
    {
    	int sel = item.stackTagCompound == null ? 0 : item.stackTagCompound.getShort("sel");
    	int mode = item.stackTagCompound == null ? 0 : item.stackTagCompound.getByte("mode");
    	if (mode < 0 || mode >= modes.length) mode = 0;
    	ItemStack stack = MatterOrbItemHandler.getItem(item, sel);
        return super.getItemStackDisplayName(item) + modes[mode] + " (" + (stack != null ? stack.stackSize + "x " + stack.getDisplayName() + ")" : "Empty)");
    }
    
    @Override
    public void addInformation(ItemStack item, EntityPlayer player, List list, boolean f) 
    {
        MatterOrbItemHandler.addInformation(item, list);
        super.addInformation(item, player, list, f);
    }

    @Override
    public int getMaxTypes(ItemStack item) 
    {
        return ItemMatterOrb.MaxTypes;
    }

    @Override
    public String getMatterTag(ItemStack item) 
    {
        return "matter";
    }

    @Override
    public ItemStack onItemRightClick(ItemStack item, World world, EntityPlayer player) 
    {
        Vec3 pos = Vec3.createVectorHelper(player.posX, player.posY + 1.62D - player.yOffset, player.posZ);
        Vec3 dir = player.getLookVec();
        MovingObjectPosition obj = world.rayTraceBlocks(pos, pos.addVector(dir.xCoord * 128, dir.yCoord * 128, dir.zCoord * 128), false);
        if (obj != null) this.onItemUse(item, player, world, obj.blockX, obj.blockY, obj.blockZ, obj.sideHit, (float)obj.hitVec.xCoord, (float)obj.hitVec.yCoord, (float)obj.hitVec.zCoord);
        return item;
    }
    
    private class Position {
    	public final int x, y, z;
    	public final byte s;
    	
    	public Position(int x, int y, int z, int s) {
    		this.x = x; this.y = y; this.z = z;
    		this.s = (byte)s;
    	}
    	
    	public boolean place(ItemStack item, EntityPlayer player, World world, float X, float Y, float Z, int ax, ArrayList<Position> list) {
    		ForgeDirection dir = ForgeDirection.getOrientation(s);
    		int nx = x + dir.offsetX, ny = y + dir.offsetY, nz = z + dir.offsetZ;
    		if (!world.getBlock(nx, ny, nz).isReplaceable(world, nx, ny, nz)) return true;
    		if (!ItemMatterCannon.this.placeItem(item, player, world, x, y, z, s, X, Y, Z)) return false;
    		if (!world.getBlock(nx, ny, nz).isReplaceable(world, nx, ny, nz))
    			for (int i = 0; i < 6; i++)
    				if (i != ax && i != (ax^1) && i != (s^1))
    					list.add(new Position(nx, ny, nz, i));
    		return true;
    	} 
    }
    

    @Override
    public boolean onItemUse(ItemStack item, EntityPlayer player, World world, int x, int y, int z, int s, float X, float Y, float Z) 
    {
        if (world.isRemote) return true;
        int mode = item.stackTagCompound == null ? 0 : item.stackTagCompound.getByte("mode");
    	if (mode < 0) return true;
    	if (mode == 0) {
    		if (!AreaProtect.instance.isOperationAllowed(player.getCommandSenderName(), world, x >> 4, z >> 4)) {
                player.addChatMessage(new ChatComponentText("Block is Protected"));
                return true;
            }
    		this.placeItem(item, player, world, x, y, z, s, X, Y, Z);
    	} else if (mode == 1) {
    		if (!AreaProtect.instance.isOperationAllowed(player.getCommandSenderName(), world, x - 15, x + 16, z - 15, z + 16)) {
                player.addChatMessage(new ChatComponentText("Block is Protected"));
                return true;
            }
    		ArrayList<Position> curList = new ArrayList<Position>();
    		curList.add(new Position(x, y, z, s));
    		s = Utils.getLookDir(player);
    		ArrayList<Position> newList;
    		int n = 0;
    		while (!curList.isEmpty() && n < 256) {
    			newList = new ArrayList<Position>();
    			for (Position p : curList)
    				if (p.place(item, player, world, X, Y, Z, s, newList)) n++;
    				else return true;
    			curList = newList;
    		}
    	} else if (mode == 2) {
    		if (!AreaProtect.instance.isOperationAllowed(player.getCommandSenderName(), world, x - 7, x + 8, z - 7, z + 8)) {
                player.addChatMessage(new ChatComponentText("Block is Protected"));
                return true;
            }
    		byte ax = (byte)(s / 2);
    		int px, py, pz;
        	for (int i = -7; i <= 7; i++)
        		for (int j = -7; j <= 7; j++) {
        			px = x + (ax==2?0:i); py = y + (ax==0?0:j); pz = z + (ax==1?0: ax==2?i:j);
        			if (!world.getBlock(px, py, pz).isReplaceable(world, px, py, pz) && 
        					!this.placeItem(item, player, world, px, py, pz, s, X, Y, Z))
        				return true;
        		}
    	} else if (mode == 3) {
    		if (!AreaProtect.instance.isOperationAllowed(player.getCommandSenderName(), world, x - 7, x + 8, z - 7, z + 8)) {
                player.addChatMessage(new ChatComponentText("Block is Protected"));
                return true;
            }
    		Block id = world.getBlock(x, y, z);
    		int m = world.getBlockMetadata(x, y, z);
    		byte ax = (byte)(s / 2);
    		int px, py, pz;
        	for (int i = -7; i <= 7; i++)
        		for (int j = -7; j <= 7; j++) {
        			px = x + (ax==2?0:i); py = y + (ax==0?0:j); pz = z + (ax==1?0: ax==2?i:j);
        			if (world.getBlock(px, py, pz) == id && world.getBlockMetadata(px, py, pz) == m && 
        					!this.placeItem(item, player, world, px, py, pz, s, X, Y, Z))
        				return true;
        		}
    	}
        return true;
    }
    
    private boolean placeItem(ItemStack item, EntityPlayer player, World world, int x, int y, int z, int s, float X, float Y, float Z)
    {
    	if (EnergyItemHandler.getEnergy(item) < EnergyUsage){
            player.addChatMessage(new ChatComponentText("Out of Energy"));
            return false;
        }
        Block id = world.getBlock(x, y, z);
        int m = world.getBlockMetadata(x, y, z);
        Block block = id;
        if (block == null) return true;
        int sel = item.stackTagCompound.getShort("sel");
        int tps = MatterOrbItemHandler.getUsedTypes(item);
        if (sel >= tps && tps > 0) sel %= tps;
        ItemStack stack = MatterOrbItemHandler.decrStackSize(item, sel, 1);
        ItemStack refStack = MatterOrbItemHandler.getItem(item, sel);
        boolean empty = refStack == null || !refStack.isItemEqual(stack);
        player.setCurrentItemOrArmor(0, stack);
        if (block.onBlockActivated(world, x, y, z, player, s, X, Y, Z) || (stack != null && stack.getItem() != null && stack.getItem().onItemUse(stack, player, world, x, y, z, s, X, Y, Z))) {
            EnergyItemHandler.addEnergy(item, -EnergyUsage, false);
        }
        stack = player.getCurrentEquippedItem();
        if (stack != null) {
            ItemStack[] remain = MatterOrbItemHandler.addItemStacks(item, stack);
            if (remain != null && remain.length == 1) {
                stack = remain[0];
                if (!player.inventory.addItemStackToInventory(stack)) {
                    EntityItem entity = new EntityItem(world, player.posX, player.posY, player.posZ, stack);
                    world.spawnEntityInWorld(entity);
                }
            }
        }
        player.setCurrentItemOrArmor(0, item);
        return !empty;
    }

	@Override
	public Container getContainer(World world, EntityPlayer player, int x, int y, int z) 
	{
		return null;
	}

	@Override
	public GuiContainer getGui(World world, EntityPlayer player, int x, int y, int z) 
	{
		return null;
	}

	@Override
	public void onPlayerCommand(World world, EntityPlayer player, DataInputStream dis) throws IOException 
	{
		byte cmd = dis.readByte();
		ItemStack item = player.getCurrentEquippedItem();
        int n = MatterOrbItemHandler.getUsedTypes(item);
		if (cmd == 1 && n > 0) {
			item.stackTagCompound.setShort("sel", (short)((item.stackTagCompound.getShort("sel") + 1) % n));
		} else if (cmd == 0 && n > 0) {
			item.stackTagCompound.setShort("sel", (short)((item.stackTagCompound.getShort("sel") + n - 1) % n));
		} else if (cmd == 2) {
			item.stackTagCompound.setByte("mode", (byte)((item.stackTagCompound.getByte("mode") + 1) % modes.length));
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void onSneakScroll(ItemStack item, EntityPlayer player, int scroll) 
	{
		byte cmd = 2;
		if (scroll < 0) cmd = 0;
		else if (scroll > 0) cmd = 1;
		try {
			ByteArrayOutputStream bos = BlockGuiHandler.getPacketTargetData(0, -1, 0);
			bos.write(cmd);
			BlockGuiHandler.sendPacketToServer(bos);
		} catch (IOException e) {}
	}
    
}
