/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cd4017be.automation.TileEntity;

import net.minecraft.network.PacketBuffer;

import java.io.IOException;
import java.util.ArrayList;

import cd4017be.api.automation.IEnergy;
import cd4017be.api.automation.PipeEnergy;
import cd4017be.api.energy.EnergyAPI;
import cd4017be.api.energy.EnergyAPI.IEnergyAccess;
import cd4017be.automation.Config;
import cd4017be.automation.Objects;
import cd4017be.lib.TileContainer;
import cd4017be.lib.TileEntityData;
import cd4017be.lib.templates.AutomatedTile;
import cd4017be.lib.templates.IAutomatedInv;
import cd4017be.lib.templates.Inventory;
import cd4017be.lib.templates.Inventory.Component;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

/**
 *
 * @author CD4017BE
 */
public class ESU extends AutomatedTile implements IEnergy, IAutomatedInv, IEnergyAccess
{
    public int type = 0;
    
    public ESU()
    {
        energy = new PipeEnergy(0, 0);
    	inventory = new Inventory(this, 2, new Component(0, 1, 0), new Component(1, 2, 0));
        netData = new TileEntityData(1, 1, 2, 0);
    }
    
    protected void setWireType()
    {
        Block id = this.getBlockType();
        type = id == Objects.SCSU ? 0 : id == Objects.OCSU ? 1 : 2;
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
        if (worldObj.isRemote) return;
        netData.floats[0] -= EnergyAPI.get(inventory.items[0]).addEnergy(getStorage(0) - getCapacity(0), 0);
        netData.floats[0] -= EnergyAPI.get(inventory.items[1]).addEnergy(getStorage(0), 0);
        double d = energy.getEnergy(netData.ints[0], 1);
        double d1 = addEnergy(d, 0);
        if (d1 == d) energy.Ucap = netData.ints[0];
        else if (d1 != 0) energy.addEnergy(-d1);
        netData.floats[1] = (float)d1;
    }
    
    public int getMaxStorage()
    {
        return Config.Ecap[type];
    }
    
    @Override
    public double addEnergy(double e, int s)
    {
        if (energy.Umax == 0) return 0;
    	if (netData.floats[0] + e < 0) {
    		e = -netData.floats[0];
    		netData.floats[0] = 0;
    	} else if (netData.floats[0] + e > getCapacity(s)) {
    		e = getCapacity(s) - netData.floats[0];
    		netData.floats[0] = (float)getCapacity(s);
    	} else {
    		netData.floats[0] += e;
    	}
    	return e;
    }

    @Override
    public void onPlaced(EntityLivingBase entity, ItemStack item) 
    {
    	netData.floats[0] = (float)EnergyAPI.get(item).getStorage(-1);
    }

    @Override
    public ArrayList<ItemStack> dropItem(IBlockState state, int fortune) 
    {
        ItemStack item = new ItemStack(this.getBlockType());
        netData.floats[0] -= EnergyAPI.get(item).addEnergy(netData.floats[0], -1);
        ArrayList<ItemStack> list = new ArrayList<ItemStack>();
        list.add(item);
        return list;
    }

    @Override
    protected void customPlayerCommand(byte cmd, PacketBuffer dis, EntityPlayerMP player) throws IOException 
    {
        if (cmd == 0) {
            netData.ints[0] = dis.readInt();
            if (netData.ints[0] < 0) netData.ints[0] = 0;
            if (netData.ints[0] > Config.Umax[type]) netData.ints[0] = Config.Umax[type];
        }
    }
    
    public int getStorageScaled(int s)
    {
        return (int)(netData.floats[0] / (float)getCapacity(0) * (float)s);
    }
    
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) 
    {
        nbt.setByte("type", (byte)type);
        nbt.setFloat("storage", netData.floats[0]);
        nbt.setInteger("voltage", netData.ints[0]);
        return super.writeToNBT(nbt);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) 
    {
        type = nbt.getByte("type");
        energy = new PipeEnergy(Config.Umax[type], Config.Rcond[type]);
        super.readFromNBT(nbt);
        netData.floats[0] = nbt.getFloat("storage");
        netData.ints[0] = nbt.getInteger("voltage");
    }
    
    public int getDiff(int max)
    {
        int d = (int)(netData.floats[1] / (float)this.getCapacity(0) * (float)max * 400F);
        if (d < -max) d = -max;
        if (d > max) d = max;
        return d;
    }

    @Override
    public boolean canInsert(ItemStack item, int cmp, int i) 
    {
        return this.isValid(item, cmp, i);
    }

    @Override
    public boolean canExtract(ItemStack item, int cmp, int i) 
    {
        return true;
    }

    @Override
    public boolean isValid(ItemStack item, int cmp, int i) 
    {
        return EnergyAPI.get(item) != EnergyAPI.NULL;
    }

    @Override
    public int getInventoryStackLimit() 
    {
        return 1;
    }
    
    @Override
    public void initContainer(TileContainer container)
    {
        container.addEntitySlot(new Slot(this, 0, 98, 16));
        container.addEntitySlot(new Slot(this, 1, 134, 16));
        
        container.addPlayerInventory(8, 86);
    }

    @Override
    public void slotChange(ItemStack oldItem, ItemStack newItem, int i) {}

    @Override
    public double getStorage(int s) 
    {
        return netData.floats[0];
    }

    @Override
    public double getCapacity(int s) 
    {
        return this.getMaxStorage() * 1000D;
    }
}
