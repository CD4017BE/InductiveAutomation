/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cd4017be.automation.TileEntity;

import net.minecraft.network.PacketBuffer;

import java.io.IOException;

import cd4017be.api.automation.AreaProtect;
import cd4017be.automation.Config;
import cd4017be.lib.TileContainer;
import cd4017be.lib.TileEntityData;
import cd4017be.lib.templates.AutomatedTile;
import cd4017be.lib.templates.Inventory;
import cd4017be.lib.templates.SlotTank;
import cd4017be.lib.templates.TankContainer;
import cd4017be.lib.templates.TankContainer.Tank;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.IFluidHandler;

/**
 *
 * @author CD4017BE
 */
public class FluidVent extends AutomatedTile implements IFluidHandler
{
    private String lastUser = "";
    private int[] blocks = new int[0];
    private int dist = -1;
    private boolean flowDir;
    private static final EnumFacing[] SearchArray = {EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.EAST, EnumFacing.WEST};
    private final EnumFacing[] lastDir = new EnumFacing[2];
    private Block blockId;
    public boolean blockNotify;
    
    public FluidVent()
    {
        netData = new TileEntityData(1, 2, 0, 1);
        inventory = new Inventory(this, 1);
        tanks = new TankContainer(this, new Tank(Config.tankCap[1], -1).setIn(0));
    }

    @Override
    public void update() 
    {
    	super.update();
        if (worldObj.isRemote) return;
        if (tanks.getAmount(0) < 1000) return;
        Fluid fluid = tanks.getFluid(0).getFluid();
        if (!fluid.canBePlacedInWorld()) return;
        flowDir = fluid.getDensity() <= 0;
        blockId = fluid.getBlock();
        if (blocks.length == 0) return;
        EnumFacing dir;
        if (dist < 0) {
            dir = EnumFacing.VALUES[this.getOrientation()];
            if (this.canFill(pos.offset(dir))) {
                dist = 0;
                blocks[dist] = (dir.getFrontOffsetX() & 0xff) | (dir.getFrontOffsetY() & 0xff) << 8 | (dir.getFrontOffsetZ() & 0xff) << 16;
                lastDir[0] = lastDir[1] = null;
            } else return;
        }
        int target = blocks[dist];
        netData.ints[1] = target;
        byte dx = (byte)target, dy = (byte)(target >> 8), dz = (byte)(target >> 16);
        if (!AreaProtect.instance.isOperationAllowed(lastUser, worldObj, (pos.getX() + dx) >> 4, (pos.getZ() + dz) >> 4)) return;
        if (dist >= blocks.length - 1) {
            this.moveBack(pos.add(dx, dy, dz));
            return;
        }
        dir = this.findNextDir(pos.add(dx, dy, dz));
        if (dir == null) this.moveBack(pos.add(dx, dy, dz));
        else {
            if (dir == EnumFacing.UP || dir == EnumFacing.DOWN) lastDir[0] = lastDir[1] = null;
            else if (dir != lastDir[0]) {
                lastDir[1] = lastDir[0];
                lastDir[0] = dir;
            }
            blocks[dist] = (blocks[dist] & 0x00ffffff) | (dir.ordinal() << 24);
            blocks[++dist] = (dx + dir.getFrontOffsetX() & 0xff) | (dy + dir.getFrontOffsetY() & 0xff) << 8 | (dz + dir.getFrontOffsetZ() & 0xff) << 16;
        }
    }
    
    private EnumFacing findNextDir(BlockPos pos)
    {
        if (this.isValidPos(pos.up(flowDir ? 1 : -1))) return flowDir ? EnumFacing.UP : EnumFacing.DOWN;
        else if (lastDir[0] != null && this.isValidPos(pos.add(lastDir[0].getFrontOffsetX(), 0, lastDir[0].getFrontOffsetZ()))) return lastDir[0];
        else if (lastDir[0] == null || lastDir[1] == null) {
            for (EnumFacing dir : SearchArray) {
                if (this.isValidPos(pos.add(dir.getFrontOffsetX(), 0, dir.getFrontOffsetZ()))) return dir;
            }
            return null;
        } else if (this.isValidPos(pos.add(lastDir[1].getFrontOffsetX(), 0, lastDir[1].getFrontOffsetZ()))) return lastDir[1];
        else if (this.isValidPos(pos.add(-lastDir[1].getFrontOffsetX(), 0, -lastDir[1].getFrontOffsetZ()))) {
            BlockPos pos1 = pos.add(-lastDir[1].getFrontOffsetX(), 0, -lastDir[1].getFrontOffsetZ()); int d1 = dist - 1;
            while (--d1 > 0) {
                pos1.add(-lastDir[0].getFrontOffsetX(), 0, -lastDir[0].getFrontOffsetZ());
                if ((byte)blocks[d1] + this.pos.getX() == pos1.getX() && (byte)(blocks[d1] >> 16) + this.pos.getZ() == pos1.getZ()) return null;
                else if (!this.isValidPos(pos1)) return lastDir[1].getOpposite();
            }
            return null;
        } else return null;
    }
    
    private boolean isValidPos(BlockPos pos)
    {
    	BlockPos pos1 = pos.subtract(this.pos);
    	int l = blocks.length / 3;
        if (pos.getY() < 0 || pos.getY() >= 256 || Math.abs(pos1.getY()) > l || Math.abs(pos1.getX()) > l || Math.abs(pos1.getZ()) > l) return false;
        if (!canFill(pos)) return false;
        int p = (pos1.getX() & 0xff) | (pos1.getY() & 0xff) << 8 | (pos1.getZ() & 0xff) << 16;
        for (int i = dist - 1; i >= 0; i -= 2)
            if ((blocks[i] & 0xffffff) == p) return false;
        return true;
    }
    
    private boolean canFill(BlockPos pos)
    {
        if (worldObj.isAirBlock(pos)) return true;
        IBlockState state = worldObj.getBlockState(pos);
        if (state.getBlock() == blockId) return state != state.getBlock().getDefaultState();
        Material material = state.getBlock().getMaterial();
        return material.isReplaceable() && !material.isLiquid();
    }
    
    private void moveBack(BlockPos pos)
    {
        if (this.canFill(pos)) {
            IBlockState state = worldObj.getBlockState(pos);
        	Block block = state.getBlock();
            if (block != null) block.dropBlockAsItem(worldObj, pos, state, 0);
            if (worldObj.setBlockState(pos, blockId.getDefaultState(), this.blockNotify ? 3 : 2)) tanks.drain(0, 1000, true);
        }
        dist--;
        if (dist <= 0) {
            lastDir[0] = null;
            return;
        }
        byte d0 = (byte)(blocks[dist - 1] >> 24), d1;
        if (d0 <= 1) lastDir[0] = null;
        else if (EnumFacing.VALUES[d0] != lastDir[0]) {
            lastDir[0] = EnumFacing.VALUES[d0];
            for (int i = dist - 2; i >= 0; i--) {
                d1 = (byte)(blocks[i] >> 24);
                if (d1 <= 1) break;
                else if (d1 != d0) {
                    lastDir[1] = EnumFacing.VALUES[d1];
                    return;
                }
            }
            lastDir[1] = null;
        }
    }

    @Override
    protected void customPlayerCommand(byte cmd, PacketBuffer dis, EntityPlayerMP player) throws IOException 
    {
        lastUser = player.getName();
        if (cmd == 0) {
            blockNotify = !blockNotify;
            netData.ints[0] = (netData.ints[0] & 0xff) | (blockNotify ? 0x100 : 0);
        } else if (cmd == 1) {
            byte l = dis.readByte();
            if (l < 0) l = 0;
            else if (l > 127) l = 127;
            blocks = new int[l * 3];
            dist = -1;
            netData.ints[0] = (netData.ints[0] & 0xf00) | (l & 0xff);
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) 
    {
        super.writeToNBT(nbt);
        nbt.setInteger("mode", netData.ints[0]);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) 
    {
        super.readFromNBT(nbt);
        netData.ints[0] = nbt.getInteger("mode");
        blocks = new int[(netData.ints[0] & 0x7f) * 3];
        blockNotify = (netData.ints[0] & 0x100) != 0;
        dist = -1;
    }

    @Override
    public void initContainer(TileContainer container) 
    {
        container.addEntitySlot(new SlotTank(this, 0, 202, 34));
        
        container.addPlayerInventory(8, 16);
    }
    
}
