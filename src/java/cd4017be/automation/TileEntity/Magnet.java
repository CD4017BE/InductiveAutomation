/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cd4017be.automation.TileEntity;

import net.minecraft.util.ITickable;

import java.util.Iterator;

import cd4017be.api.automation.IEnergy;
import cd4017be.api.automation.PipeEnergy;
import cd4017be.automation.Config;
import cd4017be.lib.ModTileEntity;
import cd4017be.lib.util.Utils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.EnumFacing;

/**
 *
 * @author CD4017BE
 */
public class Magnet extends ModTileEntity implements IEnergy, ITickable
{
    public static float Energy = 0.1F;
    public static final float rad = 8;
    public static final float accleration = 1;
    private PipeEnergy energy = new PipeEnergy(Config.Umax[2], Config.Rcond[1]);
    
    @Override
    public void update() 
    {
        if (worldObj.isRemote) return;
        energy.update(this);
        if (worldObj.isBlockPowered(getPos())) return;
        double e = energy.Ucap * energy.Ucap;
        float rad = (float)energy.Ucap / 64F;
        Iterator<Entity> list = worldObj.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos.getX() + 0.5D - rad, pos.getY() + 0.5D - rad, pos.getZ() + 0.5D - rad, pos.getX() + 0.5D + rad, pos.getY() + 0.5D + rad, pos.getZ() + 0.5D + rad)).iterator();
        Vec3d vec0 = new Vec3d(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
        while (list.hasNext())
        {
            Entity entity = list.next();
            if (entity instanceof EntityPlayer) continue;
            Vec3d vec1 = vec0.addVector(-entity.posX, -entity.posY, -entity.posZ);
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
        	EnumFacing dir = EnumFacing.VALUES[i];
            TileEntity te = getLoadedTile(pos.offset(dir));
            if (te != null && te instanceof IInventory)
            {
                int[] s = Utils.accessibleSlots((IInventory)te, i^1);
                item = this.putItemStack(item, (IInventory)te, i^1, s);
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
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) 
    {
        energy.writeToNBT(nbt, "wire");
        return super.writeToNBT(nbt);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) 
    {
        super.readFromNBT(nbt);
        energy.readFromNBT(nbt, "wire");
    }
    
}
