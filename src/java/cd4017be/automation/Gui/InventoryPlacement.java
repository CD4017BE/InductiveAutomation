package cd4017be.automation.Gui;

import cd4017be.lib.util.ItemFluidUtil;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class InventoryPlacement {

	public final ItemStack[] inventory;
	public final int[] placement;

	public InventoryPlacement(ItemStack item) {
		if (item != null && item.hasTagCompound()) {
			inventory = ItemFluidUtil.loadItems(item.getTagCompound().getTagList(ItemFluidUtil.Tag_ItemList, 10));
			placement = item.getTagCompound().getIntArray("mode");
		} else {
			inventory = new ItemStack[0];
			placement = new int[0];
		}
	}

	public boolean useDamage(int i) {
		return (placement[i] & 0x2000) != 0;
	}

	public boolean sneak(int i) {
		return (placement[i] & 0x1000) != 0;
	}

	public EnumFacing getDir(int i) {
		return EnumFacing.VALUES[(placement[i] >> 8 & 0xf) % 6];
	}

	public boolean rayTrace(int i) {
		return (placement[i] & 0x4000) != 0;
	}

	public ItemStack doPlacement(World world, EntityPlayer player, BlockPos pos, int i, ItemStack stack) {
		int ci = player.inventory.currentItem;
		ItemStack lItem = player.inventory.mainInventory[ci];
		float lYaw = player.rotationYaw, lPitch = player.rotationPitch;
		double lPx = player.posX, lPy = player.posY, lPz = player.posZ;
		boolean lSneak = player.isSneaking();

		player.setHeldItem(EnumHand.MAIN_HAND, stack);
		player.setSneaking(this.sneak(i));
		EnumFacing dir = this.getDir(i);
		float X = 0, Y = 0, Z = 0, A = (float)(placement[i] & 0xf) * 0.0625F + 0.03125F, B = (float)(placement[i] >> 4 & 0xf) * 0.0625F + 0.03125F;
		if (dir == EnumFacing.DOWN || dir == EnumFacing.UP) {
			player.rotationYaw = (float)Math.toDegrees(Math.atan2(A - 0.5D, B - 0.5D));
			player.posX = (double)pos.getX() + (X = A);
			player.posZ = (double)pos.getZ() + (Z = B);
			if (dir == EnumFacing.DOWN) {player.rotationPitch = -90; player.posY = (double)pos.getY() + (Y = 1) + (double)player.getEyeHeight();}
			else {player.rotationPitch = 90; player.posY = (double)pos.getY() + (Y = 0) - (double)player.getYOffset();}
		} else {
			player.rotationPitch = 0;
			player.posY = (double)pos.getY() + (Y = B);
			if (dir.ordinal() < 4) player.posX = (double)pos.getX() + (X = A);
			else player.posZ = (double)pos.getZ() + (Z = A);
			if (dir == EnumFacing.NORTH) {player.rotationYaw = 0; player.posZ = (double)pos.getZ() + (Z = 0) - 0.5D;}
			else if (dir == EnumFacing.SOUTH) {player.rotationYaw = 180; player.posZ = (double)pos.getZ() + (Z = 1) + 0.5D;}
			else if (dir == EnumFacing.WEST) {player.rotationYaw = 90; player.posX = (double)pos.getX() + (X = 0) - 0.5D;}
			else {player.rotationYaw = 270; player.posX = (double)pos.getX() + (X = 1) + 0.5D;}
		}
		IBlockState state = world.getBlockState(pos);
		BlockPos pos1 = pos.offset(dir, -1);
		Vec3d v0 = new Vec3d((double)pos1.getX() + X, (double)pos1.getY() + Y, (double)pos1.getZ() + Z);
		Vec3d v1 = new Vec3d((double)pos.getX() + X, (double)pos.getX() + Y, (double)pos.getZ() + Z);
		boolean flag = false;
		RayTraceResult obj = null;
		if (this.rayTrace(i)) obj = world.rayTraceBlocks(v0, v1);//TODO correct vectors
		if (obj != null) {
			X = (float)obj.hitVec.xCoord; Y = (float)obj.hitVec.yCoord; Z = (float)obj.hitVec.zCoord;
			pos = obj.getBlockPos();
			state = world.getBlockState(pos);
			//PlayerInteractEvent event = ForgeEventFactory.onPlayerInteract(player, Action.RIGHT_CLICK_BLOCK, world, pos, obj.sideHit);
			flag = /*(event.useBlock != Event.Result.DENY && */state.getBlock().onBlockActivated(world, pos, state, player, EnumHand.MAIN_HAND, lItem, obj.sideHit, X, Y, Z) /*) || event.useItem == Event.Result.DENY */;
		}
		if (!flag) {
			//PlayerInteractEvent event = ForgeEventFactory.onPlayerInteract(player, Action.RIGHT_CLICK_AIR, world, pos, dir);
			//if (event.useBlock != Event.Result.DENY && event.useItem != Event.Result.DENY) 
				stack.getItem().onItemUse(player.getHeldItemMainhand(), player, world, pos, EnumHand.MAIN_HAND, dir.getOpposite(), X, Y, Z);
		}
		stack = player.getHeldItemMainhand();
		if (stack != null && stack.stackSize <= 0) stack = null;

		player.setHeldItem(EnumHand.MAIN_HAND, lItem);
		player.rotationYaw = lYaw; player.rotationPitch = lPitch;
		player.posX = lPx; player.posY = lPy; player.posZ = lPz;
		player.setSneaking(lSneak);
		return stack;
	}

}
