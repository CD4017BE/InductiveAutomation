package cd4017be.automation.TileEntity;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import cd4017be.api.automation.IItemPipeCon;
import cd4017be.automation.Block.BlockItemPipe;
import cd4017be.automation.Item.ItemItemPipe;
import cd4017be.automation.Item.ItemItemUpgrade;
import cd4017be.automation.Item.PipeUpgradeItem;
import cd4017be.lib.BlockItemRegistry;
import cd4017be.lib.templates.AutomatedTile;
import cd4017be.lib.templates.IPipe;
import cd4017be.lib.util.Utils;

public class ItemWarpPipe extends AutomatedTile implements IPipe, IItemPipeCon
{
	
	//[1<<0 .. 1<<5]: dir[0..5] -> 1: out;
    //[1<<8 .. 1<<13]: dir[0..5] -> 1: in;
    //0x0040: has_out; 0x4000: has_in
    private short flow;
	private boolean updateCon = true;
	public boolean redstone = false;
    public PipeUpgradeItem[] filter = new PipeUpgradeItem[6];
    ArrayList<ItemDestination> dest = new ArrayList<ItemDestination>();
    ArrayList<ItemSource> src = new ArrayList<ItemSource>();
    private Cover cover = null;
    public PipeNetwork network;
    
	public ItemWarpPipe() 
	{
		
	}
	
	@Override
	public void update() 
	{
		if (worldObj.isRemote) return;
		if (network == null) {
			TileEntity te;
			for (int i = 0; i < 6; i++) 
				if (!this.getFlowBit(i) && !this.getFlowBit(i | 8) && (te = Utils.getTileOnSide(this, (byte)i)) != null && te instanceof ItemWarpPipe) {
					ItemWarpPipe p = (ItemWarpPipe)te;
					if (p.getFlowBit(i^1) || p.getFlowBit((i^1) | 8) || p.network == null) continue;
					if (network == null) {
						network = p.network;
						network.addPipe(this);
					} else {
						network.combine(p.network);
					}
				}
			if (network == null) network = new PipeNetwork(this);
		}
		if (updateCon) {
			TileEntity te;
			for (int i = 0; i < 6; i++) {
				boolean b0 = this.getFlowBit(i), b1 = this.getFlowBit(i | 8);
				if (b1 && b0) continue;
				te = Utils.getTileOnSide(this, (byte)i);
				if (b0 && te != null && te instanceof IInventory && !(te instanceof ItemWarpPipe)) {
					boolean add = true;
					for (ItemDestination d : dest) {
						if (d.side == i) {
							add = false;
							break;
						}
					}
					if (add) {
						ItemDestination d = new ItemDestination(this, (byte)i, (IInventory)te);
						dest.add(d);
						network.addDestination(d);
					}
				} else if (b1 && te != null && te instanceof IInventory && !(te instanceof ItemWarpPipe)) {
					boolean add = true;
					for (ItemSource d : src) {
						if (d.side == i) {
							add = false;
							break;
						}
					}
					if (add) {
						src.add(new ItemSource(this, (byte)i, (IInventory)te));
					}
				} else if (!b0 && !b1 && te instanceof ItemWarpPipe) {
					ItemWarpPipe p = (ItemWarpPipe)te;
					if (p.getFlowBit(i^1) || p.getFlowBit((i^1) | 8)) {
						this.setFlowBit(i, true);
						this.setFlowBit(i | 8, true);
					} else if (p.network != null && p.network != this.network) this.network.combine(p.network);
				}
			}
			int[] rem = new int[6];
			int n = 0;
			for (int i = 0; i < dest.size(); i++) 
				if (!dest.get(i).isValid()) rem[n++] = i;
			for (int i = n - 1; i >= 0; i--) {
				dest.remove(rem[i]);
				network.updateOutputs = true;
			}
			n = 0;
			for (int i = 0; i < src.size(); i++) 
				if (!src.get(i).isValid()) rem[n++] = i;
			for (int i = n - 1; i >= 0; i--)
				src.remove(rem[i]);
			updateCon = false;
		}
		redstone = worldObj.isBlockPowered(getPos());
		for (ItemSource s : src) s.extract();
		if (network.core == this || network.core.tileEntityInvalid) network.update11();
	}
	
	@Override
    public boolean onActivated(EntityPlayer player, EnumFacing dir, float X, float Y, float Z) 
    {
		int s = dir.getIndex();
        ItemStack item = player.getCurrentEquippedItem();
        X -= 0.5F;
        Y -= 0.5F;
        Z -= 0.5F;
        float dx = Math.abs(X);
        float dy = Math.abs(Y);
        float dz = Math.abs(Z);
        if (dy > dz && dy > dx) s = Y < 0 ? 0 : 1;
        else if (dz > dx) s = Z < 0 ? 2 : 3;
        else s = X < 0 ? 4 : 5;
        boolean bo = this.getFlowBit(s), bi = this.getFlowBit(s | 8);
        if (player.isSneaking() && item == null) {
            if (worldObj.isRemote) return true;
            if (cover != null) {
                player.setCurrentItemOrArmor(0, cover.item);
                cover = null;
                worldObj.markBlockForUpdate(getPos());
                return true;
            }
            if (bo ^ bi) {
            	if (bo) {
            		player.setCurrentItemOrArmor(0, BlockItemRegistry.stack("itemPipeI", 1));
            		this.setFlowBit(s, false);
            		this.updateCon = true;
            	} else {
            		player.setCurrentItemOrArmor(0, BlockItemRegistry.stack("itemPipeE", 1));
            		this.setFlowBit(s | 8, false);
            		this.updateCon = true;
            	}
            	worldObj.markBlockForUpdate(getPos());
            } else {
            	boolean lock = !(bo && bi);
                this.setFlowBit(s, lock);
                this.setFlowBit(s | 8, lock);
                this.updateCon = true;
                worldObj.markBlockForUpdate(getPos());
                TileEntity te = Utils.getTileOnSide(this, (byte)s);
                if (te != null && te instanceof ItemWarpPipe) {
                	ItemWarpPipe pipe = (ItemWarpPipe)te;
                    pipe.setFlowBit(s^1, lock);
                    pipe.setFlowBit(s^1 | 8, lock);
                    pipe.updateCon = true;
                    if (lock && pipe.network == this.network) this.network.updateStructure = true; 
                    worldObj.markBlockForUpdate(pipe.pos);
                }
            }
            return true;
        } else if (!player.isSneaking() && item == null && cover == null && filter[s] != null) {
            if (worldObj.isRemote) return true;
            item = BlockItemRegistry.stack("item.itemUpgrade", 1);
            item.stackTagCompound = PipeUpgradeItem.save(filter[s]);
            filter[s] = null;
            player.setCurrentItemOrArmor(0, item);
            if (this.network != null) this.network.reorder();
            return true;
        } else if (!player.isSneaking() && cover == null && item != null && (cover = Cover.create(item)) != null) {
            if (worldObj.isRemote) return true;
            item.stackSize--;
            if (item.stackSize <= 0) item = null;
            player.setCurrentItemOrArmor(0, item);
            worldObj.markBlockForUpdate(getPos());
            return true;
        } else if (filter[s] == null && (bo ^ bi) && item != null && item.getItem() instanceof ItemItemUpgrade && item.stackTagCompound != null) {
            if (worldObj.isRemote) return true;
            filter[s] = PipeUpgradeItem.load(item.stackTagCompound);
            item.stackSize--;
            if (item.stackSize <= 0) item = null;
            player.setCurrentItemOrArmor(0, item);
            if (this.network != null) this.network.reorder();
            return true;
        } else if (!(bo ^ bi) && item != null && item.getItem() instanceof ItemItemPipe) {
        	if (worldObj.isRemote) return true;
        	if (item.getItemDamage() == BlockItemPipe.ID_Injection) {
        		item.stackSize--;
        		this.setFlowBit(s, true);
        		this.setFlowBit(s | 8, false);
        	} else if (item.getItemDamage() == BlockItemPipe.ID_Extraction) {
        		item.stackSize--;
        		this.setFlowBit(s, false);
        		this.setFlowBit(s | 8, true);
        	} else return true;
        	if (item.stackSize <= 0) item = null;
            player.setCurrentItemOrArmor(0, item);
            worldObj.markBlockForUpdate(getPos());
            updateCon = true;
            TileEntity te = Utils.getTileOnSide(this, (byte)s);
            if (te != null && te instanceof ItemWarpPipe) {
            	ItemWarpPipe pipe = (ItemWarpPipe)te;
                pipe.setFlowBit(s^1, true);
                pipe.setFlowBit(s^1 | 8, true);
                pipe.updateCon = true;
                if (pipe.network == this.network) this.network.updateStructure = true; 
                worldObj.markBlockForUpdate(pipe.pos);
            }
            return true;
        } else return false;
    }

	@Override
	public void onNeighborTileChange(BlockPos pos) 
	{
		updateCon = true;
	}
	
	public boolean getFlowBit(int b)
    {
        return (flow & (1 << b)) != 0;
    }
    
    protected void setFlowBit(int b, boolean v)
    {
        if (v) flow |= 1 << b;
        else flow &= ~(1 << b);
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) 
    {
        super.writeToNBT(nbt);
        nbt.setShort("flow", flow);
        for (int i = 0; i < filter.length; i++) {
        	if (filter[i] != null) nbt.setTag("filter" + i, PipeUpgradeItem.save(filter[i]));
        }
        if (cover != null) cover.write(nbt, "cover");
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) 
    {
        super.readFromNBT(nbt);
        flow = nbt.getShort("flow");
        for (int i = 0; i < filter.length; i++) {
        	if (nbt.hasKey("filter" + i)) filter[i] = PipeUpgradeItem.load(nbt.getCompoundTag("filter" + i));
        	else filter[i] = null;
        }
        cover = Cover.read(nbt, "cover");
        network = null;
        updateCon = true;
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) 
    {
        flow = pkt.getNbtCompound().getShort("flow");
        cover = Cover.read(pkt.getNbtCompound(), "cover");
        worldObj.markBlockForUpdate(getPos());
    }

    @Override
    public Packet getDescriptionPacket() 
    {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setShort("flow", flow);
        if (cover != null) cover.write(nbt, "cover");
        return new S35PacketUpdateTileEntity(getPos(), -1, nbt);
    }

    @Override
    public int textureForSide(byte s) 
    {
        if (s == -1) return 0;
        TileEntity p = Utils.getTileOnSide(this, s);
        boolean b0 = getFlowBit(s);
        boolean b1 = getFlowBit(s | 8);
        if (b0 ^ b1) return b0 ? 1 : 2;
        if (!(b0 && b1) && p != null && p instanceof ItemWarpPipe) return 0;
        return -1;
    }

	@Override
    public Cover getCover() 
    {
        return cover;
    }
	
	public long getPositionCode()
	{
		return (long)(pos.getX() & 0xffffff) | (long)(pos.getY() & 0xff) << 24 | (long)(pos.getZ() & 0xffffff) << 32 | (long)(worldObj.provider.getDimensionId() & 0xff) << 56;
	}
	
	public static class PipeNetwork
	{
		/**
		 * The main pipe updating the network
		 */
		public ItemWarpPipe core;
		/**
		 * All pipes connected in this network
		 */
		public HashMap<Long, ItemWarpPipe> networkList = new HashMap<Long, ItemWarpPipe>();
		/**
		 * High priority destinations
		 */
		public ArrayList<ItemDestination> dests = new ArrayList<ItemDestination>();
		/**
		 * Some pipes were unloaded / broken
		 */
		public boolean updateStructure = false;
		/**
		 * Some pipes changed their destination status
		 */
		public boolean updateOutputs = false;
		
		public PipeNetwork(ItemWarpPipe pipe)
		{
			core = pipe;
			this.addPipe(pipe);
		}
		
		/**
		 * Insert an item stack into valid destinations
		 * @param item
		 * @return the result if not possible
		 */
		public ItemStack insertItem(ItemStack item)
		{
			if (item == null) return null;
			for (ItemDestination dest : dests) {
				if (dest.isValid()) {
					item = dest.insertItem(item);
					if (item == null) return null;
					if (dest.blockItem(item)) return item;
				} else updateOutputs = true;
			}
			return item;
		}
		
		/**
		 * Combine this network with another one
		 * @param network
		 * @return the resulting network
		 */
		public PipeNetwork combine(PipeNetwork network)
		{
			if (network == this) return this;
			PipeNetwork result;
			if (networkList.size() >= network.networkList.size()) {
				result = this;
			} else {
				result = network;
				network = this;
			}
			result.networkList.putAll(network.networkList);
			result.dests.addAll(network.dests);
			for (ItemWarpPipe pipe : network.networkList.values()) pipe.network = result;
			result.updateOutputs |= network.updateOutputs;
			result.updateStructure |= network.updateStructure;
			result.reorder();
			return result;
		}
		
		public void update11()
		{
			if (updateOutputs) {
				int[] ids = new int[dests.size()];
				int n = 0;
				for (int i = 0; i < ids.length; i++) {
					ItemDestination dest = dests.get(i);
					if (dest.isValid()) continue;
					ids[n++] = i;
					if (dest.pipe.tileEntityInvalid) updateStructure = true;
					else dest.pipe.dest.remove(dest);
				}
				for (int i = n - 1; i >= 0; i--) dests.remove(ids[i]);
				updateOutputs = false;
			}
			if (core.tileEntityInvalid) {
				for (ItemWarpPipe pipe : networkList.values())
					if (!pipe.tileEntityInvalid) {
						core = pipe;
						break;
					}
				updateStructure = true;
			}
			if (updateStructure) {
				long[] ids = new long[networkList.size()];
				int n = 0;
				for (ItemWarpPipe pipe : networkList.values()) {
					if (pipe.tileEntityInvalid) ids[n++] = pipe.getPositionCode();
					pipe.network = null;
				}
				for (int i = 0; i < n; i++) networkList.remove(Long.valueOf(ids[i]));
				if (core.tileEntityInvalid) return;
				HashMap<Long, ItemWarpPipe> result = new HashMap<Long, ItemWarpPipe>();
				result.put(core.getPositionCode(), core);
				core.network = this;
				ids = new long[]{core.getPositionCode()};
				n = 1;
				long[] buff;
				long l;
				int m;
				while (n > 0) {
					buff = new long[6 * n];
					m = 0;
					for (int i = 0; i < n; i++) {
						l = ids[i] & 0xffffffffff000000L | ((ids[i] + 0x1L) & 0xffffffL);
						if (this.add(l, 4, result)) buff[m++] = l;
						l = ids[i] & 0xffffffffff000000L | ((ids[i] - 0x1L) & 0xffffffL);
						if (this.add(l, 5, result)) buff[m++] = l;
						l = ids[i] & 0xffffffff00ffffffL | ((ids[i] + 0x1000000L) & 0xff000000L);
						if (this.add(l, 0, result)) buff[m++] = l;
						l = ids[i] & 0xffffffff00ffffffL | ((ids[i] - 0x1000000L) & 0xff000000L);
						if (this.add(l, 1, result)) buff[m++] = l;
						l = ids[i] & 0xff000000ffffffffL | ((ids[i] + 0x100000000L) & 0xffffff00000000L);
						if (this.add(l, 2, result)) buff[m++] = l;
						l = ids[i] & 0xff000000ffffffffL | ((ids[i] - 0x100000000L) & 0xffffff00000000L);
						if (this.add(l, 3, result)) buff[m++] = l;
					}
					ids = buff;
					n = m;
				}
				networkList = result;
				int[] rem = new int[dests.size()];
				n = 0;
				for (int i = 0; i < rem.length; i++) {
					ItemDestination dest = dests.get(i);
					if (dest.pipe.network == this) continue;
					rem[n++] = i;
				}
				for (int i = n - 1; i >= 0; i--) dests.remove(rem[i]);
				updateStructure = false;
			}
		}
		
		private boolean add(long l, int s, HashMap<Long, ItemWarpPipe> list)
		{
			ItemWarpPipe pipe = networkList.get(Long.valueOf(l));
			if (pipe != null && pipe.network == null && !pipe.getFlowBit(s) && !pipe.getFlowBit(s | 8)) {
				pipe.network = this;
				list.put(Long.valueOf(l), pipe);
				return true;
			}
			return false;
		}
		
		/**
		 * Add a pipe to this network
		 * @param pipe
		 */
		public void addPipe(ItemWarpPipe pipe)
		{
			networkList.put(Long.valueOf(pipe.getPositionCode()), pipe);
			for (ItemDestination dest : pipe.dest) this.addDestination(dest);
		}
		
		/**
		 * Add a destination to this network
		 * @param dest
		 */
		public void addDestination(ItemDestination dest)
		{
			dests.add(dest);
			Collections.sort(dests);
		}
		
		public void reorder()
		{
			Collections.sort(dests);
		}
		
	}
	
	public static class ItemDestination implements Comparable<ItemDestination>
	{
		public final ItemWarpPipe pipe;
		public IInventory inv;
		public final byte side;
		
		public ItemDestination(ItemWarpPipe pipe, byte side, IInventory inv)
		{
			this.pipe = pipe;
			this.side = side;
			this.inv = inv;
		}
		
		/**
		 * Check if this destination still exists
		 * @return
		 */
		public boolean isValid()
		{
			if (pipe.tileEntityInvalid || inv == null || !pipe.getFlowBit(side) || pipe.getFlowBit(side | 8)) return false;
			if (((TileEntity)inv).isInvalid()) {
				TileEntity te = Utils.getTileOnSide(pipe, side);
				if (te != null && te instanceof IInventory) {
					inv = (IInventory)te;
				} else {
					inv = null;
					return false;
				}
			}
			return true;
		}
		
		/**
		 * Check if that item stack is allowed for low priority destinations
		 * @param item
		 * @return true if not
		 */
		public boolean blockItem(ItemStack item)
		{
			return pipe.filter[side] != null && !pipe.filter[side].transferItem(item);
		}
		
		/**
		 * Insert an item stack into this destination inventory
		 * @param item
		 * @return the result if not possible
		 */
		public ItemStack insertItem(ItemStack item)
		{
			int[] slots = Utils.accessibleSlots(inv, side^1);
			int n = item.stackSize;
			if (pipe.filter[side] == null) return Utils.fillStack(inv, side^1, slots, item);
			n = pipe.filter[side].getInsertAmount(item, inv, slots, pipe.redstone);
			if (n == 0) return item;
			if (n > item.stackSize) n = item.stackSize;
			ItemStack item1 = Utils.fillStack(inv, side^1, slots, item.splitStack(n));
			if (item1 != null) item.stackSize += item1.stackSize;
			return item.stackSize > 0 ? item : null;
		}

		private int getPriority() {
			return this.pipe.filter[side] == null ? 0 : (int)this.pipe.filter[side].priority;
		}
		
		@Override
		public int compareTo(ItemDestination o) 
		{
			return o.getPriority() - this.getPriority();
		}
		
	}
	
	public class ItemSource 
	{
		public final ItemWarpPipe pipe;
		public IInventory inv;
		public final byte side;
		private int slotIdx;
		
		public ItemSource(ItemWarpPipe pipe, byte side, IInventory inv)
		{
			this.pipe = pipe;
			this.side = side;
			this.inv = inv;
			slotIdx = 0;
		}
		
		/**
		 * Check if this source still exists
		 * @return
		 */
		public boolean isValid()
		{
			if (inv == null || pipe.getFlowBit(side) || !pipe.getFlowBit(side | 8)) return false;
			if (((TileEntity)inv).isInvalid()) {
				TileEntity te = Utils.getTileOnSide(pipe, side);
				if (te != null && te instanceof IInventory) {
					inv = (IInventory)te;
				} else {
					inv = null;
					return false;
				}
			}
			return true;
		}
		
		/**
		 * Will try to extract one item stack from this source and insert it into the pipe network
		 */
		public void extract()
		{
			if (!this.isValid()) {
				pipe.updateCon = true;
				return;
			}
			int[] slots = Utils.accessibleSlots(inv, side^1);
			ISidedInventory invS = inv instanceof ISidedInventory ? (ISidedInventory)inv : null;
			if (slots.length == 0) return;
			int s, target = -1;
			ItemStack stack = null;
			for (int i = slotIdx; i < slotIdx + slots.length; i++) {
				s = slots[i % slots.length];
				stack = inv.getStackInSlot(s);
				if (stack != null && (invS == null || invS.canExtractItem(s, stack, EnumFacing.VALUES[side^1]))) {
					if (pipe.filter[side] != null && (stack = pipe.filter[side].getExtractItem(stack, inv, slots, pipe.redstone)) == null) continue;
					target = s;
					slotIdx = (i + 1) % slots.length;
					break;
				}
			}
			if (target < 0 || stack == null) return;
			stack = pipe.network.insertItem(inv.decrStackSize(target, stack.stackSize));
			if (stack != null) {
				ItemStack item = inv.getStackInSlot(target);
				if (item == null) inv.setInventorySlotContents(target, stack);
				else if (item.isItemEqual(stack)) {
					stack.stackSize += item.stackSize;
					inv.setInventorySlotContents(target, stack);
				} else {
					stack = Utils.fillStack(inv, side^1, slots, stack);
					if (stack != null) {
						EntityItem ei = new EntityItem(pipe.worldObj, pipe.pos.getX() + 0.5D, pipe.pos.getY() + 0.5D, pipe.pos.getZ() + 0.5D, stack);
						pipe.worldObj.spawnEntityInWorld(ei);
					}
				}
			}
		}
		
	}
	
	@Override
    public void breakBlock() 
    {
        super.breakBlock();
        for (int i = 0; i < filter.length; i++) {
        	if (filter[i] != null) {
                ItemStack item = BlockItemRegistry.stack("item.itemUpgrade", 1);
                item.stackTagCompound = PipeUpgradeItem.save(filter[i]);
                filter[i] = null;
                EntityItem entity = new EntityItem(worldObj, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, item);
                worldObj.spawnEntityInWorld(entity);
        	}
        	boolean bo = this.getFlowBit(i), bi = this.getFlowBit(i | 8);
        	if (bo^bi) {
                EntityItem entity = new EntityItem(worldObj, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, BlockItemRegistry.stack(bo ? "itemPipeI" : "itemPipeE", 1));
                this.setFlowBit(i, true);
                this.setFlowBit(i | 8, true);
                worldObj.spawnEntityInWorld(entity);
        	}
        }
        if (cover != null) {
            EntityItem entity = new EntityItem(worldObj, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, cover.item);
            cover = null;
            worldObj.spawnEntityInWorld(entity);
        }
        
    }

	@Override
	public byte getItemConnectType(int s) 
	{
		return (byte)((this.getFlowBit(s) ? 1 : 0) | (this.getFlowBit(s | 8) ? 2 : 0));
	}
	
}
