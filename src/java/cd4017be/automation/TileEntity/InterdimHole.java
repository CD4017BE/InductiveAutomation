/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cd4017be.automation.TileEntity;

import net.minecraft.util.ITickable;

import java.util.ArrayList;

import cd4017be.api.automation.IEnergy;
import cd4017be.api.automation.PipeEnergy;
import cd4017be.lib.BlockItemRegistry;
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
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

/**
 *
 * @author CD4017BE
 */
public class InterdimHole extends ModTileEntity implements ISidedInventory, IFluidHandler, IEnergy, ITickable //, IEnergyHandler //TODO reimplement
{
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
            drop.stackTagCompound = new NBTTagCompound();
            drop.stackTagCompound.setInteger("lx", pos.getX());
            drop.stackTagCompound.setInteger("ly", pos.getY());
            drop.stackTagCompound.setInteger("lz", pos.getZ());
            drop.stackTagCompound.setInteger("ld", worldObj.provider.getDimensionId());
            EntityItem eitem = new EntityItem(worldObj, entity.posX, entity.posY, entity.posZ, drop);
            worldObj.spawnEntityInWorld(eitem);
            if (entity instanceof EntityPlayer) ((EntityPlayer)entity).addChatMessage(new ChatComponentText("The droped Wormhole will link to this"));
        } else if (item.stackTagCompound != null) {
            linkX = item.stackTagCompound.getInteger("lx");
            linkY = item.stackTagCompound.getInteger("ly");
            linkZ = item.stackTagCompound.getInteger("lz");
            linkD = item.stackTagCompound.getInteger("ld");
            link(true);
            if (linkTile != null && entity instanceof EntityPlayer) ((EntityPlayer)entity).addChatMessage(new ChatComponentText(String.format("Link found in dimension %d at position %d , %d , %d", linkD, linkX, linkY, linkZ)));
            else if (entity instanceof EntityPlayer) ((EntityPlayer)entity).addChatMessage(new ChatComponentText("Error: Link not Found!"));
        }
        this.init();
    }

    @Override
    public ArrayList<ItemStack> dropItem(IBlockState state, int fortune) 
    {
        ArrayList<ItemStack> list = new ArrayList<ItemStack>();
        ItemStack drop = new ItemStack(this.getBlockType(), 1, 1);
        drop.stackTagCompound = new NBTTagCompound();
        drop.stackTagCompound.setInteger("lx", linkX);
        drop.stackTagCompound.setInteger("ly", linkY);
        drop.stackTagCompound.setInteger("lz", linkZ);
        drop.stackTagCompound.setInteger("ld", linkD);
        list.add(drop);
        return list;
    }

    @Override
    public boolean onActivated(EntityPlayer player, EnumFacing s, float X, float Y, float Z) 
    {
        if (worldObj.isRemote) return true;
        if (player.isSneaking() && player.getCurrentEquippedItem() == null && linkTile != null && !linkTile.isInvalid() && linkTile.linkTile == this) {
            ItemStack item = new ItemStack(this.getBlockType(), 1, 0);
            linkTile.worldObj.setBlockToAir(linkTile.pos);
            worldObj.setBlockToAir(getPos());
            EntityItem eitem = new EntityItem(worldObj, player.posX, player.posY, player.posZ, item);
            worldObj.spawnEntityInWorld(eitem);
            player.addChatMessage(new ChatComponentText("Both linked Wormholes removed"));
            return true;
        } else if (player.getCurrentEquippedItem() != null && player.getCurrentEquippedItem().getItem() == Items.paper) {
            ItemStack item = BlockItemRegistry.stack("item.teleporterCoords", player.getCurrentEquippedItem().stackSize);
            item.stackTagCompound = new NBTTagCompound();
            item.stackTagCompound.setInteger("x", pos.getX());
            item.stackTagCompound.setInteger("y", pos.getY());
            item.stackTagCompound.setInteger("z", pos.getZ());
            player.setCurrentItemOrArmor(0, item);
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
    public void update() 
    {
        if (updateCon && !worldObj.isRemote) {
            init();
            link(false);
            updateCon = false;
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) 
    {
        super.writeToNBT(nbt);
        nbt.setInteger("lx", linkX);
        nbt.setInteger("ly", linkY);
        nbt.setInteger("lz", linkZ);
        nbt.setInteger("ld", linkD);
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
    public int[] getSlotsForFace(EnumFacing s) 
    {
        if (linkTile == null) return new int[0];
        TileEntity te = linkTile.neighbors[s.getIndex()];
        if (te != null && !te.isInvalid() && te instanceof IInventory) {
            return linkTile.invIdx[s.getIndex()];
        } else return new int[0];
    }

    @Override
    public boolean canInsertItem(int i, ItemStack itemstack, EnumFacing s) 
    {
        if (linkTile == null) return false;
        TileEntity te = linkTile.neighbors[s.getIndex()];
        if (te != null && !te.isInvalid() && te instanceof IInventory) {
            if (te instanceof ISidedInventory) return linkTile.linkIdx[i] >> 28 == s.getIndex() && ((ISidedInventory)te).canInsertItem(linkTile.linkIdx[i]&0x0fffffff, itemstack, s);
            else return true;
        } else return false;
    }

    @Override
    public boolean canExtractItem(int i, ItemStack itemstack, EnumFacing s) 
    {
        if (linkTile == null) return false;
        TileEntity te = linkTile.neighbors[s.getIndex()];
        if (te != null && !te.isInvalid() && te instanceof IInventory) {
            if (te instanceof ISidedInventory) return linkTile.linkIdx[i] >> 28 == s.getIndex() && ((ISidedInventory)te).canExtractItem(linkTile.linkIdx[i]&0x0fffffff, itemstack, s);
            else return true;
        } else return false;
    }

    @Override
    public int getSizeInventory() 
    {
        return linkTile == null ? 0 : linkTile.linkIdx.length;
    }

    @Override
    public ItemStack getStackInSlot(int i) 
    {
        if (linkTile == null) return null;
        int s = linkTile.linkIdx[i] >> 28;
        TileEntity te = linkTile.neighbors[s];
        if (te != null && !te.isInvalid() && te instanceof IInventory)
            return ((IInventory)te).getStackInSlot(linkTile.linkIdx[i]&0x0fffffff);
        else return null;
    }

    @Override
    public ItemStack decrStackSize(int i, int n) 
    {
        if (linkTile == null) return null;
        int s = linkTile.linkIdx[i] >> 28;
        TileEntity te = linkTile.neighbors[s];
        if (te != null && !te.isInvalid() && te instanceof IInventory)
            return ((IInventory)te).decrStackSize(linkTile.linkIdx[i]&0x0fffffff, n);
        else return null;
    }

    @Override
    public ItemStack removeStackFromSlot(int i) 
    {
        return null;
    }

    @Override
    public void setInventorySlotContents(int i, ItemStack itemstack) 
    {
        if (linkTile == null) return;
        int s = linkTile.linkIdx[i] >> 28;
        TileEntity te = linkTile.neighbors[s];
        if (te != null && !te.isInvalid() && te instanceof IInventory)
            ((IInventory)te).setInventorySlotContents(linkTile.linkIdx[i]&0x0fffffff, itemstack);
    }

    @Override
    public String getName() 
    {
        return "Interdimensional Wormhole";
    }

    @Override
    public int getInventoryStackLimit() 
    {
        return 64;
    }

    @Override
    public boolean isItemValidForSlot(int i, ItemStack itemstack) 
    {
        if (linkTile == null) return false;
        int s = linkTile.linkIdx[i] >> 28;
        TileEntity te = linkTile.neighbors[s];
        if (te != null && !te.isInvalid() && te instanceof IInventory) {
            if (te instanceof ISidedInventory) return ((ISidedInventory)te).isItemValidForSlot(linkTile.linkIdx[i]&0x0fffffff, itemstack);
            else return true;
        } else return false;
    }

    @Override
    public int fill(EnumFacing from, FluidStack resource, boolean doFill) 
    {
        if (linkTile == null) return 0;
        TileEntity te = linkTile.neighbors[from.ordinal()];
        if (te != null && !te.isInvalid() && te instanceof IFluidHandler) {
            return ((IFluidHandler)te).fill(from, resource, doFill);
        } else return 0;
    }

    @Override
    public FluidStack drain(EnumFacing from, FluidStack resource, boolean doDrain) 
    {
        if (linkTile == null) return null;
        TileEntity te = linkTile.neighbors[from.ordinal()];
        if (te != null && !te.isInvalid() && te instanceof IFluidHandler) {
            return ((IFluidHandler)te).drain(from, resource, doDrain);
        } else return null;
    }

    @Override
    public FluidStack drain(EnumFacing from, int maxDrain, boolean doDrain) 
    {
        if (linkTile == null) return null;
        TileEntity te = linkTile.neighbors[from.ordinal()];
        if (te != null && !te.isInvalid() && te instanceof IFluidHandler) {
            return ((IFluidHandler)te).drain(from, maxDrain, doDrain);
        } else return null;
    }

    @Override
    public boolean canFill(EnumFacing from, Fluid fluid) 
    {
        if (linkTile == null) return false;
        TileEntity te = linkTile.neighbors[from.ordinal()];
        if (te != null && !te.isInvalid() && te instanceof IFluidHandler) {
            return ((IFluidHandler)te).canFill(from, fluid);
        } else return false;
    }

    @Override
    public boolean canDrain(EnumFacing from, Fluid fluid) 
    {
        if (linkTile == null) return false;
        TileEntity te = linkTile.neighbors[from.ordinal()];
        if (te != null && !te.isInvalid() && te instanceof IFluidHandler) {
            return ((IFluidHandler)te).canDrain(from, fluid);
        } else return false;
    }

    @Override
    public FluidTankInfo[] getTankInfo(EnumFacing from) 
    {
        if (linkTile == null) return null;
        TileEntity te = linkTile.neighbors[from.ordinal()];
        if (te != null && !te.isInvalid() && te instanceof IFluidHandler) {
            return ((IFluidHandler)te).getTankInfo(from);
        } else return null;
    }

    @Override
    public PipeEnergy getEnergy(byte side) 
    {
        if (worldObj.isRemote) return PipeEnergy.empty;
        if (linkTile == null) return null;
        TileEntity te = linkTile.neighbors[side];
        if (te != null && !te.isInvalid() && te instanceof IEnergy) {
            return ((IEnergy)te).getEnergy(side);
        } else return null;
    }

    @Override
    public void onEntityCollided(Entity entity) 
    {
        if (worldObj.isRemote) return;
        link(true);
        if (linkTile == null) return;
        Vec3 pos = Vec3.Def(entity.posX, entity.posY, entity.posZ);
        AxisAlignedBB block = this.getBlockType().getCollisionBoundingBox(worldObj, getPos(), worldObj.getBlockState(this.pos));
        AxisAlignedBB box = entity.getEntityBoundingBox();
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
        MovedBlock.moveEntity(entity, linkTile.worldObj.provider.getDimensionId(), pos.x, pos.y, pos.z);
    }
    
    protected boolean isAreaClear(AxisAlignedBB area)
    {
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
                        block.getBlock().addCollisionBoxesToList(worldObj, pos, block, area, list, (Entity)null);
                        if (!list.isEmpty()) return false;
                    }
        return true;
    }

    @Override
    public void onNeighborTileChange(BlockPos pos) 
    {
        this.init();
    }

    public void init()
    {
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
            for (int j = 0; j < invIdx[i].length; j++)
            {
                linkIdx[n++] = i << 28 | invIdx[i][j];
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
    public void invalidate() 
    {
        super.invalidate();
        if (linkTile != null) {
            if (linkTile.linkTile == this) linkTile.linkTile = null;
            linkTile = null;
        }
        neighbors = null;
    }
    
    public boolean hasLink()
    {
        return linkTile != null;
    }
    
    private void link(boolean force)
    {
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
                linkTile.linkD = worldObj.provider.getDimensionId();
            }
        }
    }

	@Override
	public boolean hasCustomName() {
		return true;
	}

	@Override
	public void openInventory(EntityPlayer player) {}

	@Override
	public void closeInventory(EntityPlayer player) {}

	@Override
	public IChatComponent getDisplayName() {
		return new ChatComponentText(this.getName());
	}

	@Override
	public int getField(int id) {
		return 0;
	}

	@Override
	public void setField(int id, int value) {}

	@Override
	public int getFieldCount() {
		return 0;
	}

	@Override
	public void clear() {}
	
	/* TODO reimplement
    @Override
    public int receiveEnergy(EnumFacing dir, int e, boolean d) 
    {
        if (linkTile == null) return 0;
        TileEntity te = linkTile.neighbors[dir.ordinal()];
        if (te != null && !te.isInvalid() && te instanceof IEnergyHandler) 
        {
            return ((IEnergyHandler)te).receiveEnergy(dir, e, d);
        } else return 0;
    }

    @Override
    public int extractEnergy(EnumFacing dir, int e, boolean d) 
    {
        if (linkTile == null) return 0;
        TileEntity te = linkTile.neighbors[dir.ordinal()];
        if (te != null && !te.isInvalid() && te instanceof IEnergyHandler) 
        {
            return ((IEnergyHandler)te).extractEnergy(dir, e, d);
        } else return 0;
    }

    @Override
    public int getEnergyStored(EnumFacing dir) 
    {
        if (linkTile == null) return 0;
        TileEntity te = linkTile.neighbors[dir.ordinal()];
        if (te != null && !te.isInvalid() && te instanceof IEnergyHandler) 
        {
            return ((IEnergyHandler)te).getEnergyStored(dir);
        } else return 0;
    }

    @Override
    public int getMaxEnergyStored(EnumFacing dir) 
    {
        if (linkTile == null) return 0;
        TileEntity te = linkTile.neighbors[dir.ordinal()];
        if (te != null && !te.isInvalid() && te instanceof IEnergyHandler) 
        {
            return ((IEnergyHandler)te).getMaxEnergyStored(dir);
        } else return 0;
    }
	
	@Override
	public boolean canConnectEnergy(EnumFacing dir) {
		if (linkTile == null) return false;
        TileEntity te = linkTile.neighbors[dir.ordinal()];
        if (te != null && !te.isInvalid() && te instanceof IEnergyHandler) 
        {
            return ((IEnergyHandler)te).canConnectEnergy(dir);
        } else return false;
	}
    */
}
