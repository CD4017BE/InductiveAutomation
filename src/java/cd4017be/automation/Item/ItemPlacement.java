package cd4017be.automation.Item;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.List;

import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
import cd4017be.automation.Automation;
import cd4017be.automation.Block.GhostBlock;
import cd4017be.automation.Gui.ContainerPlacement;
import cd4017be.automation.Gui.GuiPlacement;
import cd4017be.automation.Gui.InventoryPlacement;
import cd4017be.lib.BlockGuiHandler;
import cd4017be.lib.DefaultItem;
import cd4017be.lib.IGuiItem;

public class ItemPlacement extends DefaultItem implements IGuiItem 
{

	public ItemPlacement(String id, String tex) 
	{
		super(id);
		this.setTextureName(tex);
        this.setCreativeTab(Automation.tabAutomation);
	}

	private static final String[] dirs = {"B", "T", "N", "S", "W", "E", " ", " "};
	
	@Override
	public void addInformation(ItemStack item, EntityPlayer player, List list, boolean b) 
	{
		InventoryPlacement inv = new InventoryPlacement(item);
		for (int i = 0; i < inv.inventory.length && inv.inventory[i] != null; i++) 
			list.add(dirs[inv.getDir(i)] + ": " + inv.inventory[i].getDisplayName());
		super.addInformation(item, player, list, b);
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
        if (world.isRemote) return true;
    	InventoryPlacement inv = new InventoryPlacement(item);
        int p;
        ItemStack stack;
        ForgeDirection dir = ForgeDirection.getOrientation(s);
        x += dir.offsetX; y += dir.offsetY; z += dir.offsetZ;
        for (int i = 0; i < inv.inventory.length && inv.inventory[i] != null; i++) {
        	for (p = 0; true; p++) {
        		if (p == player.inventory.currentItem) continue;
        		if (p >= player.inventory.mainInventory.length) return true;
        		stack = player.inventory.mainInventory[p];
        		if (stack != null && stack.getItem() == inv.inventory[i].getItem() && (!inv.useDamage(i) || stack.getItemDamage() == inv.inventory[i].getItemDamage())) {
        			stack = player.inventory.decrStackSize(p, 1);
        			break;
        		}
        	}
        	stack = doPlacement(world, player, stack, x, y, z, inv.getDir(i), inv.Vxy[i], inv.Vxy[i + 8], inv.sneak(i), inv.useBlock);
        	if (stack != null && !player.inventory.addItemStackToInventory(stack)) {
        		EntityItem e = new EntityItem(world, player.posX, player.posY, player.posZ, stack);
        		world.spawnEntityInWorld(e);
        	}
        }
        return true;
    }
    
    public static ItemStack doPlacement(World world, EntityPlayer player, ItemStack stack, int x, int y, int z, byte s, float A, float B, boolean sneak, boolean useBlock)
    {
    	ItemStack lItem = player.getCurrentEquippedItem();
    	float lYaw = player.rotationYaw, lPitch = player.rotationPitch;
    	double lPx = player.posX, lPy = player.posY, lPz = player.posZ;
    	boolean lSneak = player.isSneaking();
    	
    	player.setCurrentItemOrArmor(0, stack);
    	player.setSneaking(sneak);
    	ForgeDirection dir = ForgeDirection.getOrientation(s);
    	float X = 0, Y = 0, Z = 0;
    	if (s < 2) {
    		player.rotationYaw = (float)Math.toDegrees(Math.atan2(A - 0.5D, B - 0.5D));
    		player.posX = (double)x + (X = A);
    		player.posZ = (double)z + (Z = B);
    		if (s == 0) {player.rotationPitch = -90; player.posY = (double)y + (Y = 1) + (double)player.ySize - (double)player.yOffset;}
    		else {player.rotationPitch = 90; player.posY = (double)y + (Y = 0) - (double)player.yOffset;}
    	} else {
    		player.rotationPitch = 0;
    		player.posY = (double)y + (Y = B);
    		if (s < 4) player.posX = (double)x + (X = A);
    		else player.posZ = (double)z + (Z = A);
    		if (s == 2) {player.rotationYaw = 0; player.posZ = (double)z + (Z = 0) - 0.5D;}
    		else if (s == 3) {player.rotationYaw = 180; player.posZ = (double)z + (Z = 1) + 0.5D;}
    		else if (s == 4) {player.rotationYaw = 90; player.posX = (double)x + (X = 0) - 0.5D;}
    		else {player.rotationYaw = 270; player.posX = (double)x + (X = 1) + 0.5D;}
    	}
    	Block id = world.getBlock(x, y, z);
    	Vec3 v0 = Vec3.createVectorHelper((double)(x - dir.offsetX) + X, (double)(y - dir.offsetY) + Y, (double)(z - dir.offsetZ) + Z);
    	Vec3 v1 = Vec3.createVectorHelper((double)x + X, (double)y + Y, (double)z + Z);
    	MovingObjectPosition obj = id.isAir(world, x, y, z) ? null : id.collisionRayTrace(world, x, y, z, v0, v1);
    	if (obj == null && useBlock) obj = world.rayTraceBlocks(v0, v1);
    	boolean flag = false;
    	if (obj != null) {
    		X = (float)obj.hitVec.xCoord; Y = (float)obj.hitVec.yCoord; Z = (float)obj.hitVec.zCoord;
    		x = obj.blockX; y = obj.blockY; z = obj.blockZ;
    		id = world.getBlock(x, y, z);
    		PlayerInteractEvent event = ForgeEventFactory.onPlayerInteract(player, Action.RIGHT_CLICK_BLOCK, x, y, z, obj.sideHit, world);
    		flag = (event.useBlock != Event.Result.DENY && id.onBlockActivated(world, x, y, z, player, obj.sideHit, X, Y, Z)) || event.useItem == Event.Result.DENY;
    	} else if (useBlock) {
    		x += dir.offsetX;
    		y += dir.offsetY;
    		z += dir.offsetZ;
    		if (world.isAirBlock(x, y, z)) world.setBlock(x, y, z, GhostBlock.ID, 0, 4);
    	}
    	if (!flag) {
    		PlayerInteractEvent event = ForgeEventFactory.onPlayerInteract(player, Action.RIGHT_CLICK_AIR, x, y, z, s, world);
    		if (event.useBlock != Event.Result.DENY && event.useItem != Event.Result.DENY) 
    			stack.getItem().onItemUse(player.getCurrentEquippedItem(), player, world, x, y, z, s^1, X, Y, Z);
    	}
    	stack = player.getCurrentEquippedItem();
    	if (stack != null && stack.stackSize <= 0) stack = null;
    	if (useBlock && world.getBlock(x, y, z) == GhostBlock.ID) world.setBlock(x, y, z, Blocks.air, 0, 4);
    	
    	player.setCurrentItemOrArmor(0, lItem);
    	player.rotationYaw = lYaw; player.rotationPitch = lPitch;
    	player.posX = lPx; player.posY = lPy; player.posZ = lPz;
    	player.setSneaking(lSneak);
    	return stack;
    }
    
    @Override
    public Container getContainer(World world, EntityPlayer player, int x, int y, int z) 
    {
        return new ContainerPlacement(player);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public GuiContainer getGui(World world, EntityPlayer player, int x, int y, int z) 
    {
        return new GuiPlacement(new ContainerPlacement(player));
    }

    @Override
    public void onPlayerCommand(World world, EntityPlayer player, DataInputStream dis) throws IOException
    {
    	if (player.openContainer != null && player.openContainer instanceof ContainerPlacement) {
            ((ContainerPlacement)player.openContainer).inventory.onCommand(dis);
        }
    }

}
