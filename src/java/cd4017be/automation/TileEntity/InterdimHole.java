package cd4017be.automation.TileEntity;

import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;

import java.util.ArrayList;

import cd4017be.automation.Objects;
import cd4017be.lib.ModTileEntity;
import cd4017be.lib.MovedBlock;
import cd4017be.lib.util.Utils;
import cd4017be.lib.util.Vec3;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.capabilities.Capability;

/**
 *
 * @author CD4017BE
 */
public class InterdimHole extends ModTileEntity implements ITickable {

	public int linkX = 0;
	public int linkY = -1;
	public int linkZ = 0;
	public int linkD = 0;
	private InterdimHole linkTile;
	public TileEntity[] neighbors = new TileEntity[6];

	private int[] linkIdx = new int[0];
	private int[][] invIdx = new int[6][];
	private boolean updateCon;

	@Override
	public void onPlaced(EntityLivingBase entity, ItemStack item) 
	{
		if (worldObj.isRemote) return;
		if (item.getItemDamage() == 0) {
			ItemStack drop = new ItemStack(item.getItem(), 1, 1);
			drop.setTagCompound(new NBTTagCompound());
			drop.getTagCompound().setInteger("lx", pos.getX());
			drop.getTagCompound().setInteger("ly", pos.getY());
			drop.getTagCompound().setInteger("lz", pos.getZ());
			drop.getTagCompound().setInteger("ld", worldObj.provider.getDimension());
			EntityItem eitem = new EntityItem(worldObj, entity.posX, entity.posY, entity.posZ, drop);
			worldObj.spawnEntityInWorld(eitem);
			if (entity instanceof EntityPlayer) ((EntityPlayer)entity).addChatMessage(new TextComponentString("The droped Wormhole will link to this"));
		} else if (item.getTagCompound() != null) {
			linkX = item.getTagCompound().getInteger("lx");
			linkY = item.getTagCompound().getInteger("ly");
			linkZ = item.getTagCompound().getInteger("lz");
			linkD = item.getTagCompound().getInteger("ld");
			link(true);
			if (linkTile != null && entity instanceof EntityPlayer) ((EntityPlayer)entity).addChatMessage(new TextComponentString(String.format("Link found in dimension %d at position %d , %d , %d", linkD, linkX, linkY, linkZ)));
			else if (entity instanceof EntityPlayer) ((EntityPlayer)entity).addChatMessage(new TextComponentString("Error: Link not Found!"));
		}
		this.init();
	}

	@Override
	public ArrayList<ItemStack> dropItem(IBlockState state, int fortune) 
	{
		ArrayList<ItemStack> list = new ArrayList<ItemStack>();
		ItemStack drop = new ItemStack(this.getBlockType(), 1, 1);
		drop.setTagCompound(new NBTTagCompound());
		drop.getTagCompound().setInteger("lx", linkX);
		drop.getTagCompound().setInteger("ly", linkY);
		drop.getTagCompound().setInteger("lz", linkZ);
		drop.getTagCompound().setInteger("ld", linkD);
		list.add(drop);
		return list;
	}

	@Override
	public boolean onActivated(EntityPlayer player, EnumHand hand, ItemStack stack, EnumFacing s, float X, float Y, float Z) 
	{
		if (worldObj.isRemote) return true;
		if (player.isSneaking() && stack == null && linkTile != null && !linkTile.isInvalid() && linkTile.linkTile == this) {
			ItemStack item = new ItemStack(this.getBlockType(), 1, 0);
			linkTile.worldObj.setBlockToAir(linkTile.pos);
			worldObj.setBlockToAir(getPos());
			EntityItem eitem = new EntityItem(worldObj, player.posX, player.posY, player.posZ, item);
			worldObj.spawnEntityInWorld(eitem);
			player.addChatMessage(new TextComponentString("Both linked Wormholes removed"));
			return true;
		} else if (stack != null && stack.getItem() == Items.PAPER) {
			ItemStack item = new ItemStack(Objects.teleporterCoords, player.getHeldItemMainhand().stackSize);
			item.setTagCompound(new NBTTagCompound());
			item.getTagCompound().setInteger("x", pos.getX());
			item.getTagCompound().setInteger("y", pos.getY());
			item.getTagCompound().setInteger("z", pos.getZ());
			player.setHeldItem(hand, item);
		}
		return false;
	}

	@Override
	public int redstoneLevel(int s, boolean str) 
	{
		if (linkTile == null) return 0;
		EnumFacing dir = EnumFacing.VALUES[s];
		if (str) return 0;
		return linkTile.worldObj.getRedstonePower(new BlockPos(linkX - dir.getFrontOffsetX(), linkY - dir.getFrontOffsetY(), linkZ - dir.getFrontOffsetZ()), EnumFacing.VALUES[s]);
	}

	@Override
	public void update() {
		if (worldObj.isRemote) return;
		if (updateCon) {
			init();
			link(false);
			updateCon = false;
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) 
	{
		nbt.setInteger("lx", linkX);
		nbt.setInteger("ly", linkY);
		nbt.setInteger("lz", linkZ);
		nbt.setInteger("ld", linkD);
		return super.writeToNBT(nbt);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) 
	{
		super.readFromNBT(nbt);
		linkX = nbt.getInteger("lx");
		linkY = nbt.getInteger("ly");
		linkZ = nbt.getInteger("lz");
		linkD = nbt.getInteger("ld");
		updateCon = true;
	}

	@Override
	public void onEntityCollided(Entity entity) {
		if (worldObj.isRemote) return;
		link(true);
		if (linkTile == null) return;
		Vec3 pos = Vec3.Def(entity.posX, entity.posY, entity.posZ);
		IBlockState state = worldObj.getBlockState(this.pos);
		AxisAlignedBB block = state.getCollisionBoundingBox(worldObj, this.pos);
		AxisAlignedBB box = entity.getEntityBoundingBox();
		block = block.offset(this.pos);
		pos = pos.add(-0.5D - (double)this.pos.getX(), -0.5D - (double)this.pos.getY(), -0.5D - (double)this.pos.getZ());
		if (box.minY >= block.maxY || box.maxY <= block.minY) return;
		if (box.minX < block.maxX && box.maxX > block.minX) {
			if (pos.z < 0) pos.z += block.maxZ - box.minZ + 0.125D;
			else pos.z += block.minZ - box.maxZ - 0.125D;
		} else if (box.minZ < block.maxZ && box.maxZ > block.minZ) {
			if (pos.x < 0) pos.x += block.maxX - box.minX + 0.125D;
			else pos.x += block.minX - box.maxX - 0.125D;
		} else return;
		pos = pos.add((double)linkTile.pos.getX() + 0.5D, (double)linkTile.pos.getY() + 0.5D, (double)linkTile.pos.getZ() + 0.5D);
		box = box.offset(pos.x - entity.posX, pos.y - entity.posY, pos.z - entity.posZ);
		if (!linkTile.isAreaClear(box)) return;
		MovedBlock.moveEntity(entity, linkTile.worldObj.provider.getDimension(), pos.x, pos.y, pos.z);
	}

	protected boolean isAreaClear(AxisAlignedBB area) {
		int x0 = MathHelper.floor_double(area.minX);
		int x1 = MathHelper.floor_double(area.maxX + 1.0D);
		int y0 = MathHelper.floor_double(area.minY);
		int y1 = MathHelper.floor_double(area.maxY + 1.0D);
		int z0 = MathHelper.floor_double(area.minZ);
		int z1 = MathHelper.floor_double(area.maxZ + 1.0D);
		BlockPos pos;
		ArrayList<AxisAlignedBB> list = new ArrayList<AxisAlignedBB>();
		for (int x = x0; x < x1; ++x)
			for (int z = z0; z < z1; ++z)
				if (worldObj.isBlockLoaded(new BlockPos(x, 64, z)))
					for (int y = y0 - 1; y < y1; ++y) {
						pos = new BlockPos(x, y, z);
						IBlockState block = worldObj.getBlockState(pos);
						block.addCollisionBoxToList(worldObj, pos, area, list, (Entity)null);
						if (!list.isEmpty()) return false;
					}
		return true;
	}

	@Override
	public void onNeighborTileChange(BlockPos pos) {
		if (!worldObj.isRemote) this.init();
	}

	public void init() {
		neighbors = new TileEntity[6];
		invIdx = new int[6][];
		int n = 0;
		EnumFacing dir;
		for (int i = 0; i < 6; i++)
		{
			dir = EnumFacing.VALUES[i^1];
			neighbors[i] = worldObj.getTileEntity(pos.offset(dir));
			if (neighbors[i] != null && neighbors[i] instanceof IInventory) {
				invIdx[i] = Utils.accessibleSlots((IInventory)neighbors[i], i);
			}
			if (invIdx[i] == null) invIdx[i] = new int[0];
			n += invIdx[i].length;
		}
		linkIdx = new int[n];
		n = 0;
		for (int i = 0; i < 6; i++) {
			for (int j = 0; j < invIdx[i].length; j++) {
				linkIdx[n] = i << 28 | invIdx[i][j];
				invIdx[i][j] = n++;
			}
		}
	}

	@Override
	public void validate() 
	{
		super.validate();
		updateCon = true;
	}

	@Override
	public void invalidate() {
		super.invalidate();
		if (linkTile != null) {
			if (linkTile.linkTile == this) linkTile.linkTile = null;
			linkTile = null;
		}
		neighbors = null;
	}

	public boolean hasLink() {
		return linkTile != null;
	}

	private void link(boolean force) {
		World world = DimensionManager.getWorld(linkD);
		if (world == null && force) {
			DimensionManager.initDimension(linkD);
			world = DimensionManager.getWorld(linkD);
		}
		linkTile = null;
		if (world != null) {
			TileEntity te = world.getTileEntity(new BlockPos(linkX, linkY, linkZ));
			if (te != null && te instanceof InterdimHole) {
				linkTile = (InterdimHole)te;
				linkTile.linkTile = this;
				linkTile.linkX = pos.getX();
				linkTile.linkY = pos.getY();
				linkTile.linkZ = pos.getZ();
				linkTile.linkD = worldObj.provider.getDimension();
			}
		}
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		return linkTile != null && linkTile.neighbors[facing.ordinal()] != null ? linkTile.neighbors[facing.ordinal()].hasCapability(capability, facing) : false;
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		return linkTile != null && linkTile.neighbors[facing.ordinal()] != null ? linkTile.neighbors[facing.ordinal()].getCapability(capability, facing) : null;
	}

}
