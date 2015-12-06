/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cd4017be.automation.TileEntity;

import java.util.ArrayList;

import cd4017be.api.automation.IFluidPipeCon;
import cd4017be.automation.Automation;
import cd4017be.automation.Config;
import cd4017be.automation.Block.BlockLiquidPipe;
import cd4017be.automation.Item.ItemFluidUpgrade;
import cd4017be.automation.Item.PipeUpgradeFluid;
import cd4017be.lib.BlockItemRegistry;
import cd4017be.lib.TileEntityData;
import cd4017be.lib.templates.AutomatedTile;
import cd4017be.lib.templates.IPipe;
import cd4017be.lib.templates.TankContainer;
import cd4017be.lib.templates.TankContainer.Tank;
import cd4017be.lib.util.Utils;
import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

/**
 *
 * @author CD4017BE
 */
public class LiquidPipe extends AutomatedTile implements IFluidHandler, IPipe
{
    //[1<<0 .. 1<<5]: dir[0..5] -> 1: out;
    //[1<<8 .. 1<<13]: dir[0..5] -> 1: in;
    //0x0040: has_out; 0x4000: has_in
    private short flow;
    private boolean updateCon = true;
    private PipeUpgradeFluid filter = null;
    private Cover cover = null;
    
    public LiquidPipe()
    {
        netData = new TileEntityData(1, 0, 0, 1);
        tanks = new TankContainer(this, new Tank(Config.tankCap[0], 0));
        
        netData.longs[0] = 0x555;
    }

    @Override
    public void updateEntity() 
    {
        if (worldObj.isRemote) return;
        int type = this.worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
        if (updateCon) this.updateConnections(type);
        if (getFlowBit(6) && getFlowBit(14)) this.transferFluid(type);
    }
    
    private void updateConnections(int type) 
    {
        ForgeDirection dir;
        TileEntity te;
        boolean lHasOut = getFlowBit(6);
        boolean lHasIn = getFlowBit(14);
        boolean nHasOut = false;
        boolean nHasIn = false;
        boolean lDirOut;
        boolean lDirIn;
        ArrayList<LiquidPipe> updateList = new ArrayList<LiquidPipe>();
        short lFlow = flow;
        for (int i = 0; i < 6; i++) {
            lDirOut = this.getFlowBit(i);
            lDirIn = this.getFlowBit(i | 8);
            if (lDirOut && lDirIn) continue;
            dir = ForgeDirection.getOrientation(i);
            te = worldObj.getTileEntity(xCoord + dir.offsetX, yCoord + dir.offsetY, zCoord + dir.offsetZ);
            if (te == null) {
                setFlowBit(i, false);
                setFlowBit(i | 8, false);
            } else if (te instanceof LiquidPipe) {
                LiquidPipe pipe = (LiquidPipe)te;
                boolean pHasOut = pipe.getFlowBit(6);
                boolean pHasIn = pipe.getFlowBit(14);
                boolean pDirOut = pipe.getFlowBit(i ^ 1);
                boolean pDirIn = pipe.getFlowBit((i ^ 1) | 8);
                boolean nDirOut = pHasOut && !pDirOut;
                boolean nDirIn = pHasIn && !pDirIn;
                if (pDirOut && pDirIn) {
                    nDirOut = true;
                    nDirIn = true;
                } else if (nDirOut && nDirIn) {
                    boolean s = lHasIn ^ lHasOut;
                    nDirOut = s && lHasIn && !lDirIn;
                    nDirIn = s && lHasOut && !lDirOut;
                }
                this.setFlowBit(i, nDirOut);
                this.setFlowBit(i | 8, nDirIn);
                nHasIn |= nDirIn && !nDirOut;
                nHasOut |= nDirOut && !nDirIn;
                updateList.add(pipe);
            } else if (te instanceof IFluidPipeCon) {
            	byte c = ((IFluidPipeCon)te).getFluidConnectType(i^1);
            	if (c == 1) {
            		setFlowBit(i, false);
                    setFlowBit(i | 8, true);
                    nHasIn = true;
            	} else if (c == 2) {
            		setFlowBit(i, true);
                    setFlowBit(i | 8, false);
                    nHasOut = true;
            	} else {
            		setFlowBit(i, false);
                    setFlowBit(i | 8, false);
            	}
            } else if (te instanceof IFluidHandler) {
                if (type == BlockLiquidPipe.ID_Transport) {
                    setFlowBit(i, false);
                    setFlowBit(i | 8, false);
                } else if (type == BlockLiquidPipe.ID_Extraction) {
                    setFlowBit(i, false);
                    setFlowBit(i | 8, true);
                    nHasIn = true;
                } else if (type == BlockLiquidPipe.ID_Injection) {
                    setFlowBit(i, true);
                    setFlowBit(i | 8, false);
                    nHasOut = true;
                }
            } else {
                setFlowBit(i, false);
                setFlowBit(i | 8, false);
            }
        }
        setFlowBit(6, nHasOut);
        setFlowBit(14, nHasIn);
        if (flow != lFlow) {
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
            for (LiquidPipe pipe : updateList) {
                pipe.onNeighborBlockChange(Blocks.air);
            }
        }
        updateCon = false;
    }
    
    private void transferFluid(int type)
    {
        boolean rs = worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord);
        ArrayList<TankContainer> flowList = new ArrayList<TankContainer>();
        ForgeDirection dir;
        boolean dirOut;
        boolean dirIn;
        if (type == BlockLiquidPipe.ID_Extraction) {
            for (int i = 0; i < 6; i++) {
                dirOut = this.getFlowBit(i);
                dirIn = this.getFlowBit(i | 8);
                if (dirIn ^ dirOut) {
                    dir = ForgeDirection.getOrientation(i);
                    TileEntity te = worldObj.getTileEntity(xCoord + dir.offsetX, yCoord + dir.offsetY, zCoord + dir.offsetZ);
                    if (te == null) updateCon = true;
                    else if (te instanceof LiquidPipe) {
                        if (dirOut) flowList.add(((LiquidPipe)te).tanks);
                    } else if (te instanceof IFluidHandler) {
                        if (dirOut) updateCon = true;
                        IFluidHandler container = (IFluidHandler)te;
                        FluidTankInfo[] infos = container.getTankInfo(dir.getOpposite());
                        if (filter != null && infos != null) {
                            for (FluidTankInfo inf : infos) {
                                int n = Math.min(tanks.getSpace(0), filter.getMaxDrainAmount(tanks.getFluid(0), inf, rs));
                                if (n > 0) {
                                    tanks.fill(0, container.drain(dir.getOpposite(), new FluidStack(inf.fluid, n), true), true);
                                    break;
                                }
                            }
                        } else container.drain(dir.getOpposite(), tanks.fill(0, container.drain(dir.getOpposite(), 1000, false), true), true);
                    } else updateCon = true;
                }
            }
        } else if (type == BlockLiquidPipe.ID_Injection && tanks.getAmount(0) > 0) {
            for (int i = 0; i < 6; i++) {
                dirOut = this.getFlowBit(i);
                dirIn = this.getFlowBit(i | 8);
                if (dirOut && !dirIn) {
                    dir = ForgeDirection.getOrientation(i);
                    TileEntity te = worldObj.getTileEntity(xCoord + dir.offsetX, yCoord + dir.offsetY, zCoord + dir.offsetZ);
                    if (te == null) updateCon = true;
                    else if (te instanceof LiquidPipe) {
                        flowList.add(((LiquidPipe)te).tanks);
                    } else if (te instanceof IFluidHandler) {
                        IFluidHandler container = (IFluidHandler)te;
                        FluidTankInfo[] infos = container.getTankInfo(dir.getOpposite());
                        if (filter != null && infos != null) {
                            for (FluidTankInfo inf : infos) {
                                int n = Math.min(tanks.getAmount(0), filter.getMaxFillAmount(tanks.getFluid(0), inf, rs));
                                if (n > 0) {
                                    tanks.drain(0, container.fill(dir.getOpposite(), new FluidStack(tanks.getFluid(0), n), true), true);
                                    break;
                                }
                            }
                        } else if (tanks.getFluid(0) != null) tanks.drain(0, container.fill(dir.getOpposite(), tanks.getFluid(0), true), true);
                    } else updateCon = true;
                }
            }
        } else if (type == BlockLiquidPipe.ID_Transport && tanks.getAmount(0) > 0) {
            for (int i = 0; i < 6; i++) {
                dirOut = this.getFlowBit(i);
                dirIn = this.getFlowBit(i | 8);
                if (dirOut && !dirIn) {
                    dir = ForgeDirection.getOrientation(i);
                    TileEntity te = worldObj.getTileEntity(xCoord + dir.offsetX, yCoord + dir.offsetY, zCoord + dir.offsetZ);
                    if (te == null) updateCon = true;
                    else if (te instanceof LiquidPipe) {
                        flowList.add(((LiquidPipe)te).tanks);
                    } else updateCon = true;
                }
            }
        }
        if (!flowList.isEmpty() && (type != BlockLiquidPipe.ID_Injection || filter == null || filter.transferFluid(tanks.getFluid(0)))) {
            int n = tanks.getAmount(0) / flowList.size();
            int r = tanks.getAmount(0) % flowList.size();
            for (int i = 0; i < flowList.size() && tanks.getFluid(0) != null; i++)
            {
                TankContainer dst = flowList.get(i);
                tanks.drain(0, dst.fill(0, Automation.getLiquid(tanks.getFluid(0).getFluid(), n + (i<r?1:0)), true), true);
            }
        }
    }

    @Override
    public void onNeighborBlockChange(Block b) 
    {
        updateCon = true;
    }

    @Override
    public boolean onActivated(EntityPlayer player, int s, float X, float Y, float Z) 
    {
        ItemStack item = player.getCurrentEquippedItem();
        int type = this.worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
        boolean canF = type == BlockLiquidPipe.ID_Extraction || type == BlockLiquidPipe.ID_Injection;
        if (player.isSneaking() && item == null) {
            if (worldObj.isRemote) return true;
            if (cover != null) {
                player.setCurrentItemOrArmor(0, cover.item);
                cover = null;
                worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
                return true;
            }
            X -= 0.5F;
            Y -= 0.5F;
            Z -= 0.5F;
            float dx = Math.abs(X);
            float dy = Math.abs(Y);
            float dz = Math.abs(Z);
            if (dy > dz && dy > dx) s = Y < 0 ? 0 : 1;
            else if (dz > dx) s = Z < 0 ? 2 : 3;
            else s = X < 0 ? 4 : 5;
            boolean lock = !(this.getFlowBit(s) && this.getFlowBit(s | 8));
            this.setFlowBit(s, lock);
            this.setFlowBit(s | 8, lock);
            this.onNeighborBlockChange(Blocks.air);
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
            TileEntity te = Utils.getTileOnSide(this, (byte)s);
            if (te != null && te instanceof LiquidPipe) {
                LiquidPipe pipe = (LiquidPipe)te;
                pipe.setFlowBit(s^1, lock);
                pipe.setFlowBit(s^1 | 8, lock);
                pipe.onNeighborBlockChange(Blocks.air);
                worldObj.markBlockForUpdate(pipe.xCoord, pipe.yCoord, pipe.zCoord);
            }
            return true;
        } else if (!player.isSneaking() && item == null && filter != null) {
            if (worldObj.isRemote) return true;
            item = BlockItemRegistry.stack("item.fluidUpgrade", 1);
            item.stackTagCompound = PipeUpgradeFluid.save(filter);
            filter = null;
            player.setCurrentItemOrArmor(0, item);
            return true;
        } else if (!player.isSneaking() && cover == null && item != null && (cover = Cover.create(item)) != null) {
            if (worldObj.isRemote) return true;
            item.stackSize--;
            if (item.stackSize <= 0) item = null;
            player.setCurrentItemOrArmor(0, item);
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
            return true;
        } else if (filter == null && canF && item != null && item.getItem() instanceof ItemFluidUpgrade && item.stackTagCompound != null) {
            if (worldObj.isRemote) return true;
            filter = PipeUpgradeFluid.load(item.stackTagCompound);
            item.stackSize--;
            if (item.stackSize <= 0) item = null;
            player.setCurrentItemOrArmor(0, item);
            return true;
        } else return false;
    }
    
    public boolean getFlowBit(int b)
    {
        return (flow & (1 << b)) != 0;
    }
    
    private void setFlowBit(int b, boolean v)
    {
        if (v) flow |= 1 << b;
        else flow &= ~(1 << b);
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) 
    {
        super.writeToNBT(nbt);
        nbt.setShort("flow", flow);
        if (filter != null) nbt.setTag("filter", PipeUpgradeFluid.save(filter));
        if (cover != null) cover.write(nbt, "cover");
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) 
    {
        super.readFromNBT(nbt);
        flow = nbt.getShort("flow");
        if (nbt.hasKey("filter")) filter = PipeUpgradeFluid.load(nbt.getCompoundTag("filter"));
        cover = Cover.read(nbt, "cover");
        updateCon = true;
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) 
    {
        flow = pkt.func_148857_g().getShort("flow");
        cover = Cover.read(pkt.func_148857_g(), "cover");
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    @Override
    public Packet getDescriptionPacket() 
    {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setShort("flow", flow);
        if (cover != null) cover.write(nbt, "cover");
        return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, -1, nbt);
    }
    
    @Override
    public int textureForSide(byte s) 
    {
        TileEntity p = Utils.getTileOnSide(this, s);
        boolean b0 = getFlowBit(s);
        boolean b1 = getFlowBit(s | 8);
        if (!(b0 && b1) && p != null && (p instanceof IFluidHandler || p instanceof IFluidPipeCon))
        {
            return (b0?1:0)|(b1?2:0);
        } else return -1;
    }

    @Override
    public void breakBlock() 
    {
        super.breakBlock();
        if (filter != null) {
            ItemStack item = BlockItemRegistry.stack("item.fluidUpgrade", 1);
            item.stackTagCompound = PipeUpgradeFluid.save(filter);
            filter = null;
            EntityItem entity = new EntityItem(worldObj, xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D, item);
            worldObj.spawnEntityInWorld(entity);
        }
        if (cover != null) {
            EntityItem entity = new EntityItem(worldObj, xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D, cover.item);
            cover = null;
            worldObj.spawnEntityInWorld(entity);
        }
    }
    
    @Override
    public Cover getCover() 
    {
        return cover;
    }
    
}
