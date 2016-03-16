/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cd4017be.automation.TileEntity;


import cd4017be.api.automation.IEnergy;
import cd4017be.api.automation.PipeEnergy;
import cd4017be.automation.Config;
import cd4017be.automation.Objects;
import cd4017be.lib.templates.AutomatedTile;
import cd4017be.lib.templates.IPipe;
import cd4017be.lib.util.Utils;
import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

/**
 *
 * @author CD4017BE
 */
public class Wire extends AutomatedTile implements IEnergy, IPipe
{
    public int type;
    private Cover cover = null;
    
    public Wire()
    {
    	energy = new PipeEnergy(0, 0);
    }
    
    private void setWireType()
    {
        Block id = this.getBlockType();
        type = id == Objects.wireC ? 0 : id == Objects.wireA ? 1 : 2;
    }
    
    @Override
    public void update() 
    {
        if (energy.Umax == 0) {
            this.setWireType();
            byte con = energy.con;
            energy = new PipeEnergy(Config.Umax[type], Config.Rcond[type]);
            energy.con = con;
        }
        super.update();
    }

	@Override
    public boolean onActivated(EntityPlayer player, EnumFacing dir, float X, float Y, float Z) 
    {
		int s = dir.getIndex();
        ItemStack item = player.getCurrentEquippedItem();
        if (player.isSneaking() && item == null && cover != null) {
            if (worldObj.isRemote) return true;
            player.setCurrentItemOrArmor(0, cover.item);
            cover = null;
            worldObj.markBlockForUpdate(pos);
            return true;
        } else if (player.isSneaking() && item == null) {
        	if (worldObj.isRemote) return true;
        	X -= 0.5F;
            Y -= 0.5F;
            Z -= 0.5F;
            float dx = Math.abs(X);
            float dy = Math.abs(Y);
            float dz = Math.abs(Z);
            if (dy > dz && dy > dx) s = Y < 0 ? 0 : 1;
            else if (dz > dx) s = Z < 0 ? 2 : 3;
            else s = X < 0 ? 4 : 5;
            boolean con = energy.isConnected(s);
            energy.con ^= 1 << s;
            worldObj.markBlockForUpdate(pos);
            TileEntity te = Utils.getTileOnSide(this, (byte)s);
            if (te != null && te instanceof Wire) {
                PipeEnergy pipe = ((IEnergy)te).getEnergy((byte)(s^1));
                if (pipe != null) {
                	if (con) pipe.con |= 1 << (s^1);
                	else pipe.con &= ~(1 << (s^1));
                	worldObj.markBlockForUpdate(te.getPos());
                }
            }
            return true;
        } else if (!player.isSneaking() && cover == null && item != null && (cover = Cover.create(item)) != null) {
            if (worldObj.isRemote) return true;
            item.stackSize--;
            if (item.stackSize <= 0) item = null;
            player.setCurrentItemOrArmor(0, item);
            worldObj.markBlockForUpdate(pos);
            return true;
        } else return false;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) 
    {
        type = nbt.getByte("type");
        energy = new PipeEnergy(Config.Umax[type], Config.Rcond[type]);
        cover = Cover.read(nbt, "cover");
        super.readFromNBT(nbt);
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) 
    {
        super.writeToNBT(nbt);
        nbt.setByte("type", (byte)type);
        if (cover != null) cover.write(nbt, "cover");
    }

    @Override
    public Packet getDescriptionPacket() 
    {
        NBTTagCompound nbt = new NBTTagCompound();
        if (cover != null) cover.write(nbt, "cover");
        nbt.setByte("con", energy.con);
        return new S35PacketUpdateTileEntity(pos, -1, nbt);
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) 
    {
        cover = Cover.read(pkt.getNbtCompound(), "cover");
        energy.con = pkt.getNbtCompound().getByte("con");
        worldObj.markBlockForUpdate(pos);
    }

    @Override
    public void breakBlock() 
    {
        super.breakBlock();
        if (cover != null) {
            EntityItem entity = new EntityItem(worldObj, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, cover.item);
            cover = null;
            worldObj.spawnEntityInWorld(entity);
        }
    }

    @Override
    public int textureForSide(byte s) 
    {
        if (s < 0) return type;
    	TileEntity p = Utils.getTileOnSide(this, s);
        PipeEnergy e;
        return (energy.isConnected(s) && p instanceof IEnergy && (e = ((IEnergy)p).getEnergy(s)) != null && e.isConnected(s^1)) ? type : -1;
    }
    
    @Override
    public Cover getCover() 
    {
        return cover;
    }
    
}
