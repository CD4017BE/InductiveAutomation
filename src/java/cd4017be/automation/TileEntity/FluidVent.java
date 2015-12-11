/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cd4017be.automation.TileEntity;

import java.io.DataInputStream;
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
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
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
    private static final ForgeDirection[] SearchArray = {ForgeDirection.NORTH, ForgeDirection.SOUTH, ForgeDirection.EAST, ForgeDirection.WEST};
    private final ForgeDirection[] lastDir = new ForgeDirection[2];
    private Block blockId;
    public boolean blockNotify;
    
    public FluidVent()
    {
        netData = new TileEntityData(1, 2, 0, 1);
        inventory = new Inventory(this, 1);
        tanks = new TankContainer(this, new Tank(Config.tankCap[1], -1).setIn(0));
    }

    @Override
    public void updateEntity() 
    {
        super.updateEntity();
        if (worldObj.isRemote) return;
        if (tanks.getAmount(0) < 1000) return;
        Fluid fluid = tanks.getFluid(0).getFluid();
        if (!fluid.canBePlacedInWorld()) return;
        flowDir = fluid.getDensity() <= 0;
        blockId = fluid.getBlock();
        if (blocks.length == 0) return;
        ForgeDirection dir;
        if (dist < 0) {
            dir = ForgeDirection.getOrientation(this.getOrientation());
            if (this.canFill(xCoord + dir.offsetX, yCoord + dir.offsetY, zCoord + dir.offsetZ)) {
                dist = 0;
                blocks[dist] = (dir.offsetX & 0xff) | (dir.offsetY & 0xff) << 8 | (dir.offsetZ & 0xff) << 16;
                lastDir[0] = lastDir[1] = ForgeDirection.UNKNOWN;
            } else return;
        }
        int target = blocks[dist];
        netData.ints[1] = target;
        byte dx = (byte)target, dy = (byte)(target >> 8), dz = (byte)(target >> 16);
        if (!AreaProtect.instance.isOperationAllowed(lastUser, worldObj, (xCoord + dx) >> 4, (zCoord + dz) >> 4)) return;
        if (dist >= blocks.length - 1) {
            this.moveBack(xCoord + dx, yCoord + dy, zCoord + dz);
            return;
        }
        dir = this.findNextDir(xCoord + dx, yCoord + dy, zCoord + dz);
        if (dir == null) this.moveBack(xCoord + dx, yCoord + dy, zCoord + dz);
        else {
            if (dir == ForgeDirection.UP || dir == ForgeDirection.DOWN) lastDir[0] = lastDir[1] = ForgeDirection.UNKNOWN;
            else if (dir != lastDir[0]) {
                lastDir[1] = lastDir[0];
                lastDir[0] = dir;
            }
            blocks[dist] = (blocks[dist] & 0x00ffffff) | (dir.ordinal() << 24);
            blocks[++dist] = (dx + dir.offsetX & 0xff) | (dy + dir.offsetY & 0xff) << 8 | (dz + dir.offsetZ & 0xff) << 16;
        }
    }
    
    private ForgeDirection findNextDir(int x, int y, int z)
    {
        if (this.isValidPos(x, y + (flowDir ? 1 : -1), z)) return flowDir ? ForgeDirection.UP : ForgeDirection.DOWN;
        else if (lastDir[0] != ForgeDirection.UNKNOWN && this.isValidPos(x + lastDir[0].offsetX, y, z + lastDir[0].offsetZ)) return lastDir[0];
        else if (lastDir[0] == ForgeDirection.UNKNOWN || lastDir[1] == ForgeDirection.UNKNOWN) {
            for (ForgeDirection dir : SearchArray) {
                if (this.isValidPos(x + dir.offsetX, y, z + dir.offsetZ)) return dir;
            }
            return null;
        } else if (this.isValidPos(x + lastDir[1].offsetX, y, z + lastDir[1].offsetZ)) return lastDir[1];
        else if (this.isValidPos(x - lastDir[1].offsetX, y, z - lastDir[1].offsetZ)) {
            int x1 = x - lastDir[1].offsetX, z1 = z - lastDir[1].offsetZ, d1 = dist - 1;
            while (--d1 > 0) {
                x1 -= lastDir[0].offsetX; z1 -= lastDir[0].offsetZ;
                if ((byte)blocks[d1] + xCoord == x1 && (byte)(blocks[d1] >> 16) + zCoord == z1) return null;
                else if (!this.isValidPos(x1, y, z1)) return lastDir[1].getOpposite();
            }
            return null;
        } else return null;
    }
    
    private boolean isValidPos(int x, int y, int z)
    {
        if (y < 0 || y >= 256 || Math.max(Math.abs(x - xCoord), Math.abs(z - zCoord)) > blocks.length / 3) return false;
        if (!canFill(x, y, z)) return false;
        int p = (x - xCoord & 0xff) | (y - yCoord & 0xff) << 8 | (z - zCoord & 0xff) << 16;
        for (int i = dist - 1; i >= 0; i -= 2) {
            if ((blocks[i] & 0xffffff) == p) return false;
        }
        return true;
    }
    
    private boolean canFill(int x, int y, int z)
    {
        if (worldObj.isAirBlock(x, y, z)) return true;
        Block bId = worldObj.getBlock(x, y, z);
        if (bId == blockId) return worldObj.getBlockMetadata(x, y, z) != 0;
        Material material = bId.getMaterial();
        if (material.blocksMovement() || material == Material.portal) return false;
        return !material.isLiquid();
    }
    
    private void moveBack(int x, int y, int z)
    {
        if (this.canFill(x, y, z)) {
            Block block = worldObj.getBlock(x, y, z);
            if (block != null) block.dropBlockAsItem(worldObj, x, y, z, worldObj.getBlockMetadata(x, y, z), 0);
            if (worldObj.setBlock(x, y, z, blockId, 0, this.blockNotify ? 3 : 2)) tanks.drain(0, 1000, true);
        }
        dist--;
        if (dist <= 0) {
            lastDir[0] = ForgeDirection.UNKNOWN;
            return;
        }
        byte d0 = (byte)(blocks[dist - 1] >> 24), d1;
        if (d0 <= 1) lastDir[0] = ForgeDirection.UNKNOWN;
        else if (d0 != lastDir[0].ordinal()) {
            lastDir[0] = ForgeDirection.getOrientation(d0);
            for (int i = dist - 2; i >= 0; i--) {
                d1 = (byte)(blocks[i] >> 24);
                if (d1 <= 1) break;
                else if (d1 != d0) {
                    lastDir[1] = ForgeDirection.getOrientation(d1);
                    return;
                }
            }
            lastDir[1] = ForgeDirection.UNKNOWN;
        }
    }

    @Override
    protected void customPlayerCommand(byte cmd, DataInputStream dis, EntityPlayerMP player) throws IOException 
    {
        lastUser = player.getCommandSenderName();
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
