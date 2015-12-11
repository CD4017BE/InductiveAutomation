/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cd4017be.automation.TileEntity;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;

import cd4017be.api.automation.EnergyItemHandler;
import cd4017be.api.automation.EnergyItemHandler.IEnergyItem;
import cd4017be.api.automation.IEnergy;
import cd4017be.api.automation.IEnergyStorage;
import cd4017be.api.automation.PipeEnergy;
import cd4017be.api.energy.EnergyThermalExpansion;
import cd4017be.automation.Config;
import cd4017be.lib.BlockItemRegistry;
import cd4017be.lib.TileContainer;
import cd4017be.lib.TileEntityData;
import cd4017be.lib.templates.AutomatedTile;
import cd4017be.lib.templates.IAutomatedInv;
import cd4017be.lib.templates.Inventory;
import cd4017be.lib.templates.Inventory.Component;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

/**
 *
 * @author CD4017BE
 */
public class ESU extends AutomatedTile implements IEnergy, IAutomatedInv, IEnergyStorage
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
        Block id = worldObj.getBlock(xCoord, yCoord, zCoord);
        type = id == BlockItemRegistry.blockId("tile.SCSU") ? 0 : id == BlockItemRegistry.blockId("tile.OCSU") ? 1 : 2;
    }

    @Override
    public void updateEntity() 
    {
        if (energy.Umax == 0) {
            this.setWireType();
            byte con = energy.con;
            energy = new PipeEnergy(Config.Umax[type], Config.Rcond[type]);
            energy.con = con;
        }
        super.updateEntity();
        if (worldObj.isRemote) return;
        netData.floats[0] -= (float)EnergyItemHandler.addEnergy(inventory.items[0], (int)Math.ceil((netData.floats[0] - getCapacity()) / 1000F), true) * 1000F;
        netData.floats[0] -= EnergyThermalExpansion.addEnergy(inventory.items[0], (float)(getEnergy() - getCapacity()));
        netData.floats[0] -= (float)EnergyItemHandler.addEnergy(inventory.items[1], (int)Math.floor(netData.floats[0] / 1000F), true) * 1000F;
        netData.floats[0] -= EnergyThermalExpansion.addEnergy(inventory.items[1], (float)getEnergy());
        double d = energy.getEnergy(netData.ints[0], 1);
        if ((netData.floats[0] > 0 || d > 0) && (d < 0 || netData.floats[0] < getCapacity())) {
            netData.floats[1] = (float)d;
            d -= addEnergy(d);
            energy.Ucap = netData.ints[0];
            if (d != 0F) {
                energy.addEnergy(d);
                netData.floats[1] -= (float)d;
            }
        } else {
            netData.floats[1] = 0;
        }
    }
    
    public int getMaxStorage()
    {
        return Config.Ecap[type];
    }
    
    @Override
    public double addEnergy(double e)
    {
        if (energy.Umax == 0) return 0;
    	if (netData.floats[0] + e < 0) {
    		e = -netData.floats[0];
    		netData.floats[0] = 0;
    	} else if (netData.floats[0] + e > getCapacity()) {
    		e = getCapacity() - netData.floats[0];
    		netData.floats[0] = (float)getCapacity();
    	} else {
    		netData.floats[0] += e;
    	}
    	return e;
    }

    @Override
    public void onPlaced(EntityLivingBase entity, ItemStack item) 
    {
    	netData.floats[0] = EnergyItemHandler.getEnergy(item);
    }

    @Override
    public ArrayList<ItemStack> dropItem(int m, int fortune) 
    {
        ItemStack item = new ItemStack(this.getBlockType());
        netData.floats[0] -= EnergyItemHandler.addEnergy(item, (int)Math.floor(netData.floats[0]), false);
        ArrayList<ItemStack> list = new ArrayList<ItemStack>();
        list.add(item);
        return list;
    }

    @Override
    protected void customPlayerCommand(byte cmd, DataInputStream dis, EntityPlayerMP player) throws IOException 
    {
        if (cmd == 0) {
            netData.ints[0] = dis.readInt();
            if (netData.ints[0] < 0) netData.ints[0] = 0;
            if (netData.ints[0] > Config.Umax[type]) netData.ints[0] = Config.Umax[type];
        }
    }
    
    public int getStorageScaled(int s)
    {
        return (int)(netData.floats[0] / (float)getCapacity() * (float)s);
    }
    
    @Override
    public void writeToNBT(NBTTagCompound nbt) 
    {
        super.writeToNBT(nbt);
        nbt.setByte("type", (byte)type);
        nbt.setFloat("storage", netData.floats[0]);
        nbt.setInteger("voltage", netData.ints[0]);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) 
    {
        type = nbt.getByte("type");
        energy = new PipeEnergy(Config.Umax[type], Config.Rcond[type]);
        super.readFromNBT(nbt);
        if (nbt.hasKey("storage", 3)) netData.floats[0] = (float)nbt.getInteger("storage") * 1000F; //compatibility to previous version TODO remove
        else netData.floats[0] = nbt.getFloat("storage");
        netData.ints[0] = nbt.getInteger("voltage");
    }
    
    public int getDiff(int max)
    {
        int d = (int)(netData.floats[1] / (float)this.getCapacity() * (float)max * 400F);
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
        return item == null || item.getItem() instanceof IEnergyItem;
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
    public void slotChange(ItemStack oldItem, ItemStack newItem, int i) 
    {
    }

    @Override
    public double getEnergy() 
    {
        return netData.floats[0];
    }

    @Override
    public double getCapacity() 
    {
        return this.getMaxStorage() * 1000D;
    }
    
}
