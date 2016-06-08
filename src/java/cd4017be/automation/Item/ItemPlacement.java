package cd4017be.automation.Item;

import java.io.IOException;
import java.util.List;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
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

	public ItemPlacement(String id) 
	{
		super(id);
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
    public ActionResult<ItemStack> onItemRightClick(ItemStack item, World world, EntityPlayer player, EnumHand hand) 
    {
        BlockGuiHandler.openItemGui(player, world, 0, -1, 0);
        return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, item);
    }

    @Override
    public EnumActionResult onItemUse(ItemStack item, EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing s, float X, float Y, float Z) 
    {
        if (world.isRemote) return EnumActionResult.SUCCESS;
    	InventoryPlacement inv = new InventoryPlacement(item);
        int p;
        ItemStack stack;
        pos = pos.offset(s);
        for (int i = 0; i < inv.inventory.length && inv.inventory[i] != null; i++) {
        	for (p = 0; true; p++) {
        		if (p == player.inventory.currentItem) continue;
        		if (p >= player.inventory.mainInventory.length) return EnumActionResult.SUCCESS;
        		stack = player.inventory.mainInventory[p];
        		if (stack != null && stack.getItem() == inv.inventory[i].getItem() && (!inv.useDamage(i) || stack.getItemDamage() == inv.inventory[i].getItemDamage())) {
        			stack = player.inventory.decrStackSize(p, 1);
        			break;
        		}
        	}
        	stack = doPlacement(world, player, stack, pos, hand, inv.getDir(i), inv.Vxy[i], inv.Vxy[i + 8], inv.sneak(i), inv.useBlock);
        	if (stack != null && !player.inventory.addItemStackToInventory(stack)) {
        		EntityItem e = new EntityItem(world, player.posX, player.posY, player.posZ, stack);
        		world.spawnEntityInWorld(e);
        	}
        }
        return EnumActionResult.SUCCESS;
    }
    
    public static ItemStack doPlacement(World world, EntityPlayer player, ItemStack stack, BlockPos pos, EnumHand hand, byte s, float A, float B, boolean sneak, boolean useBlock)
    {
    	ItemStack lItem = player.getHeldItemMainhand();
    	float lYaw = player.rotationYaw, lPitch = player.rotationPitch;
    	double lPx = player.posX, lPy = player.posY, lPz = player.posZ;
    	boolean lSneak = player.isSneaking();
    	
    	player.setHeldItem(hand, stack);
    	player.setSneaking(sneak);
    	EnumFacing dir = EnumFacing.VALUES[s];
    	float X = 0, Y = 0, Z = 0;
    	if (s < 2) {
    		player.rotationYaw = (float)Math.toDegrees(Math.atan2(A - 0.5D, B - 0.5D));
    		player.posX = (double)pos.getX() + (X = A);
    		player.posZ = (double)pos.getZ() + (Z = B);
    		if (s == 0) {player.rotationPitch = -90; player.posY = (double)pos.getY() + (Y = 1) + (double)player.getEyeHeight();}
    		else {player.rotationPitch = 90; player.posY = (double)pos.getY() + (Y = 0) - (double)player.getYOffset();}
    	} else {
    		player.rotationPitch = 0;
    		player.posY = (double)pos.getY() + (Y = B);
    		if (s < 4) player.posX = (double)pos.getX() + (X = A);
    		else player.posZ = (double)pos.getZ() + (Z = A);
    		if (s == 2) {player.rotationYaw = 0; player.posZ = (double)pos.getZ() + (Z = 0) - 0.5D;}
    		else if (s == 3) {player.rotationYaw = 180; player.posZ = (double)pos.getZ() + (Z = 1) + 0.5D;}
    		else if (s == 4) {player.rotationYaw = 90; player.posX = (double)pos.getX() + (X = 0) - 0.5D;}
    		else {player.rotationYaw = 270; player.posX = (double)pos.getX() + (X = 1) + 0.5D;}
    	}
    	IBlockState state = world.getBlockState(pos);
    	BlockPos pos1 = pos.offset(dir, -1);
    	Vec3d v0 = new Vec3d((double)pos1.getX() + X, (double)pos1.getY() + Y, (double)pos1.getZ() + Z);
    	Vec3d v1 = new Vec3d((double)pos.getX() + X, (double)pos.getX() + Y, (double)pos.getZ() + Z);
    	RayTraceResult obj = state.getBlock().isAir(state, world, pos) ? null : state.getBlock().collisionRayTrace(state, world, pos, v0, v1);
    	if (obj == null && useBlock) obj = world.rayTraceBlocks(v0, v1);
    	boolean flag = false;
    	if (obj != null) {
    		X = (float)obj.hitVec.xCoord; Y = (float)obj.hitVec.yCoord; Z = (float)obj.hitVec.zCoord;
    		pos = obj.getBlockPos();
    		state = world.getBlockState(pos);
    		//PlayerInteractEvent event = ForgeEventFactory.onPlayerInteract(player, Action.RIGHT_CLICK_BLOCK, world, pos, obj.sideHit);
    		flag = /*(event.useBlock != Event.Result.DENY && */state.getBlock().onBlockActivated(world, pos, state, player, hand, lItem, obj.sideHit, X, Y, Z) /*) || event.useItem == Event.Result.DENY */;
    	} else if (useBlock) {
    		pos = pos.offset(dir);
    		if (world.isAirBlock(pos)) world.setBlockState(pos, GhostBlock.ID.getDefaultState(), 4);
    	}
    	if (!flag) {
    		//PlayerInteractEvent event = ForgeEventFactory.onPlayerInteract(player, Action.RIGHT_CLICK_AIR, world, pos, dir);
    		//if (event.useBlock != Event.Result.DENY && event.useItem != Event.Result.DENY) 
    			stack.getItem().onItemUse(player.getHeldItemMainhand(), player, world, pos, hand, EnumFacing.VALUES[s^1], X, Y, Z);
    	}
    	stack = player.getHeldItemMainhand();
    	if (stack != null && stack.stackSize <= 0) stack = null;
    	if (useBlock && world.getBlockState(pos) == GhostBlock.ID.getDefaultState()) world.setBlockState(pos, Blocks.AIR.getDefaultState(), 4);
    	
    	player.setHeldItem(hand, lItem);
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
    public void onPlayerCommand(World world, EntityPlayer player, PacketBuffer dis) throws IOException
    {
    	if (player.openContainer != null && player.openContainer instanceof ContainerPlacement) {
            ((ContainerPlacement)player.openContainer).inventory.onCommand(dis);
        }
    }

}
