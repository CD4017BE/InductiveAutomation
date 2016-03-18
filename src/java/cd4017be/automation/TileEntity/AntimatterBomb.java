/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cd4017be.automation.TileEntity;

import net.minecraft.util.ITickable;

import java.util.ArrayList;

import cd4017be.api.automation.AreaProtect;
import cd4017be.api.automation.IAreaConfig;
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
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;

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
        int x = pos.getX() >> 4;
        int z = pos.getZ() >> 4;
        int dx, dz;
        ArrayList<IAreaConfig> list = AreaProtect.instance.loadedSS.get(this.worldObj.provider.getDimensionId());
        if (list == null) return false;
        for (IAreaConfig area : list) {
            for (int[] e : area.getProtectedChunks("#antimatterBomb")) {
            	dx = e[0] - x; dz = e[1] - z;
                if (dx * dx + dz * dz < 70000) return true;
            }
        }
        return false;
    }
    
    @Override
    public void onNeighborBlockChange(Block b) 
    {
        if (timer < 0 && worldObj.isBlockPowered(pos)) timer = 200;
    }

    @Override
	public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
    	timer = pkt.getNbtCompound().getShort("timer");
	}

    @Override
    public Packet getDescriptionPacket() 
    {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setShort("timer", (short)timer);
        return new S35PacketUpdateTileEntity(getPos(), -1, nbt);
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
