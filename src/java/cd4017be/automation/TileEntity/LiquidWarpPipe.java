package cd4017be.automation.TileEntity;


import java.util.ArrayList;
import java.util.HashMap;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import cd4017be.api.automation.IFluidPipeCon;
import cd4017be.automation.Objects;
import cd4017be.automation.Block.BlockLiquidPipe;
import cd4017be.automation.Item.ItemFluidUpgrade;
import cd4017be.automation.Item.ItemLiquidPipe;
import cd4017be.automation.Item.PipeUpgradeFluid;
import cd4017be.lib.BlockItemRegistry;
import cd4017be.lib.templates.AutomatedTile;
import cd4017be.lib.templates.IPipe;
import cd4017be.lib.util.Utils;

public class LiquidWarpPipe extends AutomatedTile implements IPipe, IFluidPipeCon {

	//[1<<0 .. 1<<5]: dir[0..5] -> 1: out;
    //[1<<8 .. 1<<13]: dir[0..5] -> 1: in;
    //0x0040: has_out; 0x4000: has_in
    private short flow;
	private boolean updateCon = true;
	public boolean redstone = false;
    public PipeUpgradeFluid[] filter = new PipeUpgradeFluid[6];
    ArrayList<FluidDestination> dest = new ArrayList<FluidDestination>();
    ArrayList<FluidSource> src = new ArrayList<FluidSource>();
    private Cover cover = null;
    public PipeNetwork network;

	public LiquidWarpPipe() 
	{
		
	}
	
	@Override
	public void update() 
	{
		if (worldObj.isRemote) return;
		if (network == null) {
			TileEntity te;
			for (int i = 0; i < 6; i++) 
				if (!this.getFlowBit(i) && !this.getFlowBit(i | 8) && (te = Utils.getTileOnSide(this, (byte)i)) != null && te instanceof LiquidWarpPipe) {
					LiquidWarpPipe p = (LiquidWarpPipe)te;
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
				if (b0 && te != null && te instanceof IFluidHandler && !(te instanceof LiquidWarpPipe)) {
					boolean add = true;
					for (FluidDestination d : dest) {
						if (d.side == i) {
							add = false;
							break;
						}
					}
					if (add) {
						FluidDestination d = new FluidDestination(this, (byte)i, (IFluidHandler)te);
						dest.add(d);
						network.addDestination(d);
					}
				} else if (b1 && te != null && te instanceof IFluidHandler && !(te instanceof LiquidWarpPipe)) {
					boolean add = true;
					for (FluidSource d : src) {
						if (d.side == i) {
							add = false;
							break;
						}
					}
					if (add) {
						src.add(new FluidSource(this, (byte)i, (IFluidHandler)te));
					}
				} else if (!b0 && !b1 && te instanceof LiquidWarpPipe) {
					LiquidWarpPipe p = (LiquidWarpPipe)te;
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
		for (FluidSource s : src) s.extract();
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
            		player.setCurrentItemOrArmor(0, BlockItemRegistry.stack("liquidPipeI", 1));
            		this.setFlowBit(s, false);
            		this.updateCon = true;
            	} else {
            		player.setCurrentItemOrArmor(0, BlockItemRegistry.stack("liquidPipeE", 1));
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
                if (te != null && te instanceof LiquidWarpPipe) {
                	LiquidWarpPipe pipe = (LiquidWarpPipe)te;
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
            item = new ItemStack(Objects.fluidUpgrade);
            item.setTagCompound(PipeUpgradeFluid.save(filter[s]));
            if ((filter[s].mode & 2) != 0) this.filterChange(s, false);
            filter[s] = null;
            player.setCurrentItemOrArmor(0, item);
            return true;
        } else if (!player.isSneaking() && cover == null && item != null && (cover = Cover.create(item)) != null) {
            if (worldObj.isRemote) return true;
            item.stackSize--;
            if (item.stackSize <= 0) item = null;
            player.setCurrentItemOrArmor(0, item);
            worldObj.markBlockForUpdate(getPos());
            return true;
        } else if (filter[s] == null && (bo ^ bi) && item != null && item.getItem() instanceof ItemFluidUpgrade && item.getTagCompound() != null) {
            if (worldObj.isRemote) return true;
            filter[s] = PipeUpgradeFluid.load(item.getTagCompound());
            if ((filter[s].mode & 2) != 0) this.filterChange(s, true);
            item.stackSize--;
            if (item.stackSize <= 0) item = null;
            player.setCurrentItemOrArmor(0, item);
            return true;
        } else if (!(bo ^ bi) && item != null && item.getItem() instanceof ItemLiquidPipe) {
        	if (worldObj.isRemote) return true;
        	if (item.getItemDamage() == BlockLiquidPipe.ID_Injection) {
        		item.stackSize--;
        		this.setFlowBit(s, true);
        		this.setFlowBit(s | 8, false);
        	} else if (item.getItemDamage() == BlockLiquidPipe.ID_Extraction) {
        		item.stackSize--;
        		this.setFlowBit(s, false);
        		this.setFlowBit(s | 8, true);
        	} else return true;
        	if (item.stackSize <= 0) item = null;
            player.setCurrentItemOrArmor(0, item);
            worldObj.markBlockForUpdate(getPos());
            updateCon = true;
            TileEntity te = Utils.getTileOnSide(this, (byte)s);
            if (te != null && te instanceof LiquidWarpPipe) {
            	LiquidWarpPipe pipe = (LiquidWarpPipe)te;
                pipe.setFlowBit(s^1, true);
                pipe.setFlowBit(s^1 | 8, true);
                pipe.updateCon = true;
                if (pipe.network == this.network) this.network.updateStructure = true; 
                worldObj.markBlockForUpdate(pipe.pos);
            }
            return true;
        } else return false;
    }
	
	private void filterChange(int s, boolean add)
	{
		FluidDestination d = null;
		for (FluidDestination d1 : dest)
			if (d1.side == s) {
				d = d1;
				break;
			}
		if (d == null || this.network == null) return;
		if (add) {
			network.dest2.remove(d);
			network.dest1.add(d);
		} else {
			network.dest1.remove(d);
			network.dest2.add(d);
		}
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
        	if (filter[i] != null) nbt.setTag("filter" + i, PipeUpgradeFluid.save(filter[i]));
        }
        if (cover != null) cover.write(nbt, "cover");
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) 
    {
        super.readFromNBT(nbt);
        flow = nbt.getShort("flow");
        for (int i = 0; i < filter.length; i++) {
        	if (nbt.hasKey("filter" + i)) filter[i] = PipeUpgradeFluid.load(nbt.getCompoundTag("filter" + i));
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
        if (!(b0 && b1) && p != null && p instanceof LiquidWarpPipe) return 0;
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
		public LiquidWarpPipe core;
		/**
		 * All pipes connected in this network
		 */
		public HashMap<Long, LiquidWarpPipe> networkList = new HashMap<Long, LiquidWarpPipe>();
		/**
		 * High priority destinations
		 */
		public ArrayList<FluidDestination> dest1 = new ArrayList<FluidDestination>();
		/**
		 * Low priority destinations
		 */
		public ArrayList<FluidDestination> dest2 = new ArrayList<FluidDestination>();
		/**
		 * Some pipes were unloaded / broken
		 */
		public boolean updateStructure = false;
		/**
		 * Some pipes changed their destination status
		 */
		public boolean updateOutputs = false;
		
		public PipeNetwork(LiquidWarpPipe pipe)
		{
			core = pipe;
			this.addPipe(pipe);
		}
		
		/**
		 * Insert an fluid stack into valid destinations
		 * @param fluid
		 * @return the result if not possible
		 */
		public FluidStack insertFluid(FluidStack fluid)
		{
			if (fluid == null) return null;
			boolean block = false;
			for (FluidDestination dest : dest1) {
				if (dest.isValid()) {
					fluid = dest.insertFluid(fluid);
					if (fluid == null) return null;
					block |= dest.blockFluid(fluid);
				} else updateOutputs = true;
			}
			if (block) return fluid;
			for (FluidDestination dest : dest2) {
				if (dest.isValid()) {
					fluid = dest.insertFluid(fluid);
					if (fluid == null) return null;
				} else updateOutputs = true;
			}
			return fluid;
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
			result.dest1.addAll(network.dest1);
			result.dest2.addAll(network.dest2);
			for (LiquidWarpPipe pipe : network.networkList.values()) pipe.network = result;
			result.updateOutputs |= network.updateOutputs;
			result.updateStructure |= network.updateStructure;
			return result;
		}
		
		public void update11()
		{
			if (updateOutputs) {
				int[] ids = new int[dest1.size()];
				int n = 0;
				for (int i = 0; i < ids.length; i++) {
					FluidDestination dest = dest1.get(i);
					if (dest.isValid()) continue;
					ids[n++] = i;
					if (dest.pipe.tileEntityInvalid) updateStructure = true;
					else dest.pipe.dest.remove(dest);
				}
				for (int i = n - 1; i >= 0; i--) dest1.remove(ids[i]);
				ids = new int[dest2.size()];
				n = 0;
				for (int i = 0; i < ids.length; i++) {
					FluidDestination dest = dest2.get(i);
					if (dest.isValid()) continue;
					ids[n++] = i;
					if (dest.pipe.tileEntityInvalid) updateStructure = true;
					else dest.pipe.dest.remove(dest);
				}
				for (int i = n - 1; i >= 0; i--) dest2.remove(ids[i]);
				updateOutputs = false;
			}
			if (core.tileEntityInvalid) {
				for (LiquidWarpPipe pipe : networkList.values())
					if (!pipe.tileEntityInvalid) {
						core = pipe;
						break;
					}
				updateStructure = true;
			}
			if (updateStructure) {
				long[] ids = new long[networkList.size()];
				int n = 0;
				for (LiquidWarpPipe pipe : networkList.values()) {
					if (pipe.tileEntityInvalid) ids[n++] = pipe.getPositionCode();
					pipe.network = null;
				}
				for (int i = 0; i < n; i++) networkList.remove(Long.valueOf(ids[i]));
				if (core.tileEntityInvalid) return;
				HashMap<Long, LiquidWarpPipe> result = new HashMap<Long, LiquidWarpPipe>();
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
				int[] rem = new int[dest1.size()];
				n = 0;
				for (int i = 0; i < rem.length; i++) {
					FluidDestination dest = dest1.get(i);
					if (dest.pipe.network == this) continue;
					rem[n++] = i;
				}
				for (int i = n - 1; i >= 0; i--) dest1.remove(rem[i]);
				rem = new int[dest2.size()];
				n = 0;
				for (int i = 0; i < rem.length; i++) {
					FluidDestination dest = dest2.get(i);
					if (dest.pipe.network == this) continue;
					rem[n++] = i;
				}
				for (int i = n - 1; i >= 0; i--) dest2.remove(rem[i]);
				updateStructure = false;
			}
		}
		
		private boolean add(long l, int s, HashMap<Long, LiquidWarpPipe> list)
		{
			LiquidWarpPipe pipe = networkList.get(Long.valueOf(l));
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
		public void addPipe(LiquidWarpPipe pipe)
		{
			networkList.put(Long.valueOf(pipe.getPositionCode()), pipe);
			for (FluidDestination dest : pipe.dest) this.addDestination(dest);
		}
		
		/**
		 * Add a destination to this network
		 * @param dest
		 */
		public void addDestination(FluidDestination dest)
		{
			if (dest.pipe.filter[dest.side] != null && (dest.pipe.filter[dest.side].mode & 0x2) != 0) dest1.add(dest);
			else dest2.add(dest);
		}
		
	}
	
	public static class FluidDestination
	{
		public final LiquidWarpPipe pipe;
		public IFluidHandler inv;
		public final byte side;
		
		public FluidDestination(LiquidWarpPipe pipe, byte side, IFluidHandler inv)
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
				if (te != null && te instanceof IFluidHandler) {
					inv = (IFluidHandler)te;
				} else {
					inv = null;
					return false;
				}
			}
			return true;
		}
		
		/**
		 * Check if that fluid stack is allowed for low priority destinations
		 * @param fluid
		 * @return true if not
		 */
		public boolean blockFluid(FluidStack fluid)
		{
			return pipe.filter[side] != null && !pipe.filter[side].transferFluid(fluid);
		}
		
		/**
		 * Insert an fluid stack into this destination inventory
		 * @param fluid
		 * @return the result if not possible
		 */
		public FluidStack insertFluid(FluidStack fluid)
		{
			if (fluid == null) return null;
			EnumFacing dir = EnumFacing.VALUES[side^1];
			FluidTankInfo[] infos = inv.getTankInfo(dir);
            if (pipe.filter[side] != null && infos != null)
                for (FluidTankInfo inf : infos) {
                    int n = pipe.filter[side].getMaxFillAmount(fluid, inf, pipe.redstone);
                    if (n > 0) {
                        fluid.amount -= inv.fill(dir, new FluidStack(fluid, n), true);
                        break;
                    }
                }
            else fluid.amount -= inv.fill(dir, fluid, true);
            return fluid.amount <= 0 ? null : fluid;
		}
		
	}
	
	public class FluidSource 
	{
		public final LiquidWarpPipe pipe;
		public IFluidHandler inv;
		public final byte side;
		private int tankIdx;
		
		public FluidSource(LiquidWarpPipe pipe, byte side, IFluidHandler inv)
		{
			this.pipe = pipe;
			this.side = side;
			this.inv = inv;
			tankIdx = 0;
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
				if (te != null && te instanceof IFluidHandler) {
					inv = (IFluidHandler)te;
				} else {
					inv = null;
					return false;
				}
			}
			return true;
		}
		
		/**
		 * Will try to extract fluid from this source and insert it into the pipe network
		 */
		public void extract()
		{
			if (!this.isValid()) {
				pipe.updateCon = true;
				return;
			}
			FluidStack fluid = null;
			EnumFacing dir = EnumFacing.VALUES[side^1];
			FluidTankInfo[] infos = inv.getTankInfo(dir);
            if (infos != null && infos.length > 0) {
                for (int i = 0; i < infos.length; i++) {
                	FluidTankInfo inf = infos[(i + tankIdx) % infos.length];
                    if (inf.fluid == null) continue;
                	int n = pipe.filter[side] != null ? pipe.filter[side].getMaxDrainAmount(inf.fluid, inf, pipe.redstone) : inf.fluid.amount;
                    if (n > 0) {
                        fluid = inv.drain(dir, new FluidStack(inf.fluid, n), false);
                        tankIdx = (tankIdx + i + 1) % infos.length;
                        break;
                    }
                }
            } else fluid = inv.drain(dir, Integer.MAX_VALUE, false);
            if (fluid == null) return;
            FluidStack result = pipe.network.insertFluid(fluid.copy());
            if (result != null) fluid.amount -= result.amount;
            if (fluid.amount > 0) inv.drain(dir, fluid, true);
		}
		
	}
	
	@Override
    public void breakBlock() 
    {
        super.breakBlock();
        for (int i = 0; i < filter.length; i++) {
        	if (filter[i] != null) {
                ItemStack item = new ItemStack(Objects.fluidUpgrade);
                item.setTagCompound(PipeUpgradeFluid.save(filter[i]));
                filter[i] = null;
                EntityItem entity = new EntityItem(worldObj, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, item);
                worldObj.spawnEntityInWorld(entity);
        	}
        	boolean bo = this.getFlowBit(i), bi = this.getFlowBit(i | 8);
        	if (bo^bi) {
                EntityItem entity = new EntityItem(worldObj, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, BlockItemRegistry.stack(bo ? "liquidPipeI" : "liquidPipeE", 1));
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
	public byte getFluidConnectType(int s) 
	{
		return (byte)((this.getFlowBit(s) ? 1 : 0) | (this.getFlowBit(s | 8) ? 2 : 0));
	}

}
