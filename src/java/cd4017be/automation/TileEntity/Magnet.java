/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cd4017be.automation.TileEntity;

import java.util.Iterator;

import cd4017be.api.automation.IEnergy;
import cd4017be.api.automation.PipeEnergy;
import cd4017be.automation.Config;
import cd4017be.lib.ModTileEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.util.ForgeDirection;

/**
 *
 * @author CD4017BE
 */
public class Magnet extends ModTileEntity implements IEnergy
{
    public static float Energy = 0.1F;
    public static final float rad = 8;
    public static final float accleration = 1;
    private PipeEnergy energy = new PipeEnergy(Config.Umax[2], Config.Rcond[1]);
    
    @Override
    public void updateEntity() 
    {
        if (worldObj.isRemote) return;
        energy.update(this);
        if (worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord)) return;
        double e = energy.Ucap * energy.Ucap;
        float rad = (float)energy.Ucap / 64F;
        Iterator list = worldObj.getEntitiesWithinAABB(Entity.class, AxisAlignedBB.getBoundingBox(xCoord + 0.5D - rad, yCoord + 0.5D - rad, zCoord + 0.5D - rad, xCoord + 0.5D + rad, yCoord + 0.5D + rad, zCoord + 0.5D + rad)).iterator();
        Vec3 vec0 = Vec3.createVectorHelper(xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D);
        while (list.hasNext())
        {
            Entity entity = (Entity)list.next();
            if (entity instanceof EntityPlayer) continue;
            Vec3 vec1 = vec0.addVector(-entity.posX, -entity.posY, -entity.posZ);
            double d = vec1.xCoord * vec1.xCoord + vec1.yCoord * vec1.yCoord + vec1.zCoord * vec1.zCoord;
            if (e < Energy) continue;
            e -= Energy;
            if (entity instanceof EntityItem && entity.isEntityAlive())
            {
                ItemStack item = putItem(((EntityItem)entity).getEntityItem());
                if (item == null) worldObj.removeEntity(entity);
                else ((EntityItem)entity).setEntityItemStack(item);
            } else
            {
                d = accleration / d;
                entity.addVelocity(vec1.xCoord * d, vec1.yCoord * d, vec1.zCoord * d);
            }
        }
        energy.Ucap = 0;
        energy.addEnergy(e);
    }
    
    private ItemStack putItem(ItemStack item)
    {
        for (int i = 0; i < 6; i++)
        {
            ForgeDirection dir = ForgeDirection.getOrientation(i);
            TileEntity te = getLoadedTile(xCoord + dir.offsetX, yCoord + dir.offsetY, zCoord + dir.offsetZ);
            if (te != null && te instanceof IInventory)
            {
                int[] s;
                if (te instanceof ISidedInventory)
                {
                    s = ((ISidedInventory)te).getAccessibleSlotsFromSide(i);
                } else {
                    s = new int[((IInventory)te).getSizeInventory()];
                    for (int j = 0; j < s.length; j++) s[j] = j;
                }
                item = this.putItemStack(item, (IInventory)te, i, s);
            }
        }
        return item;
    }

    @Override
    public PipeEnergy getEnergy(byte side) 
    {
        return energy;
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) 
    {
        super.writeToNBT(nbt);
        energy.writeToNBT(nbt, "wire");
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) 
    {
        super.readFromNBT(nbt);
        energy.readFromNBT(nbt, "wire");
    }
    
}
