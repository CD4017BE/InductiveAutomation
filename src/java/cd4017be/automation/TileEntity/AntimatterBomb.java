/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cd4017be.automation.TileEntity;

import net.minecraft.util.ITickable;

import java.util.ArrayList;
import java.util.UUID;

import com.mojang.authlib.GameProfile;

import cd4017be.api.automation.AreaProtect;
import cd4017be.automation.Entity.EntityAntimatterExplosion1;
import cd4017be.lib.ModTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;

/**
 *
 * @author CD4017BE
 */
public class AntimatterBomb extends ModTileEntity implements ITickable
{
    
    public int antimatter;
    public int timer = -1;

    @Override
    public void update() 
    {
        if (worldObj.isRemote) return;
        if (timer > 0) {
            worldObj.markBlockForUpdate(pos);
            timer--;
        }
        else if (timer == 0) {
            if (this.isProtected()) {
                timer = -1;
                return;
            }
            EntityAntimatterExplosion1 entity = new EntityAntimatterExplosion1(worldObj, pos.getX(), pos.getY(), pos.getZ(), antimatter);
            antimatter = -1;
            IBlockState state = worldObj.getBlockState(pos);
            int r = state.getBlock().getMetaFromState(state) - 4;
            entity.rotationYaw = r == 2 ? 90F : r == 5 ? 180F : r == 3 ? 270F : 0F;
            worldObj.spawnEntityInWorld(entity);
            worldObj.setBlockToAir(pos);
        }
    }
    
    private boolean isProtected()
    {
        int x1 = pos.getX() + maxSize + 1;
        int z1 = pos.getZ() + maxSize + 1;
        int x0 = pos.getX() - maxSize;
        int z0 = pos.getZ() - maxSize;
        return !AreaProtect.operationAllowed(new GameProfile(new UUID(0, 0), "#antimatterBomb"), worldObj, x0, x1, z0, z1);
    }
    
    @Override
    public void onNeighborBlockChange(Block b) 
    {
        if (timer < 0 && worldObj.isBlockPowered(pos)) timer = 200;
    }

    @Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
    	timer = pkt.getNbtCompound().getShort("timer");
	}

    @Override
    public Packet getDescriptionPacket() 
    {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setShort("timer", (short)timer);
        return new SPacketUpdateTileEntity(getPos(), -1, nbt);
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) 
    {
        super.writeToNBT(nbt);
        nbt.setShort("timer", (short)timer);
        nbt.setInteger("antimatter", antimatter);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) 
    {
        super.readFromNBT(nbt);
        timer = nbt.getShort("timer");
        antimatter = nbt.getInteger("antimatter");
    }
    
    @Override
    public void onPlaced(EntityLivingBase entity, ItemStack item) 
    {
        if (item != null && item.getItem() == Item.getItemFromBlock(this.getBlockType()))
        {
            this.antimatter = 0;
            if (item.getTagCompound() != null) antimatter = item.getTagCompound().getInteger("antimatter");
        }
    }

    @Override
    public ArrayList<ItemStack> dropItem(IBlockState state, int fortune) 
    {
        ArrayList<ItemStack> list = new ArrayList<ItemStack>();
        ItemStack item = new ItemStack(this.getBlockType(), 1, 0);
        if (antimatter >= 0) {
            item.setTagCompound(new NBTTagCompound());
            item.getTagCompound().setInteger("antimatter", antimatter);
        }
        list.add(item);
        return list;
    }
    
}
