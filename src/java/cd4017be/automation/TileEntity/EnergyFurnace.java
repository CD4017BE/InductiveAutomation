/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cd4017be.automation.TileEntity;

import java.io.DataInputStream;
import java.io.IOException;

import cd4017be.api.automation.IEnergy;
import cd4017be.api.automation.PipeEnergy;
import cd4017be.automation.Config;
import cd4017be.lib.TileContainer;
import cd4017be.lib.TileEntityData;
import cd4017be.lib.templates.AutomatedTile;
import cd4017be.lib.templates.Inventory;
import cd4017be.lib.templates.Inventory.Component;
import cd4017be.lib.templates.SlotOutput;
import cd4017be.lib.util.Utils;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagCompound;

/**
 *
 * @author CD4017BE
 */
public class EnergyFurnace extends AutomatedTile implements ISidedInventory, IEnergy
{
    public static float Euse = 200F;
	private float powerScale;
    
    public EnergyFurnace()
    {
        inventory = new Inventory(this, 5, new Component(0, 2, -1), new Component(2, 4, 1)).setInvName("Electric Heated Furnace");
        energy = new PipeEnergy(Config.Umax[1], Config.Rcond[1]);
        netData = new TileEntityData(1, 1, 2, 0);
        netData.ints[0] = Config.Rmin;
        powerScale = Config.Pscale;
    }
    
    @Override
    public void updateEntity() 
    {
        super.updateEntity();
        if (this.worldObj.isRemote) return;
        putItemStack(0, 1);
        putItemStack(2, 3);
        if (inventory.items[4] == null && inventory.items[1] != null)
        {
            ItemStack item = FurnaceRecipes.smelting().getSmeltingResult(inventory.items[1]);
            if (item != null)
            {
                decrStackSize(1, 1);
                inventory.items[4] = item.copy();
            }
        }
        netData.floats[1] = (float)energy.Ucap;
        double power = energy.getEnergy(0, netData.ints[0]) / 1000F;
        if (inventory.items[4] != null)
        {
            if (netData.floats[0] < Euse)
            {
                netData.floats[0] += power;
                energy.Ucap *= powerScale;
            }
            if (netData.floats[0] >= Euse)
            {
                putItemStack(4, 2);
                if (inventory.items[4] == null) netData.floats[0] -= Euse;
            }
        }
    }
    
    private void putItemStack(int src, int dest)
    {
        if (inventory.items[src] == null) return;
        if (inventory.items[dest] == null)
        {
            inventory.items[dest] = inventory.items[src].copy();
            inventory.items[src] = null;
        } else
        if (Utils.itemsEqual(inventory.items[dest], inventory.items[src]))
        {
            int i = inventory.items[dest].getMaxStackSize() - inventory.items[dest].stackSize;
            if (inventory.items[src].stackSize < i) i = inventory.items[src].stackSize;
            inventory.items[src].stackSize -= i;
            inventory.items[dest].stackSize += i;
            if (inventory.items[src].stackSize <= 0) inventory.items[src] = null;
        }
    }

    @Override
    protected void customPlayerCommand(byte cmd, DataInputStream dis, EntityPlayerMP player) throws IOException 
    {
        if (cmd == 0) {
            netData.ints[0] = dis.readInt();
            if (netData.ints[0] < Config.Rmin) netData.ints[0] = Config.Rmin;
            powerScale = (float)Math.sqrt(1.0D - 1.0D / (double)netData.ints[0]);
        }
    }
    
    public int getProgressScaled(int s)
    {
        int i = (int)((float)s * netData.floats[0] / Euse);
        return i < s ? i : s;
    }
    
    public int getPowerScaled(int s)
    {
        return (int)(s * netData.floats[1] * netData.floats[1] / (float)netData.ints[0]) / 200000;
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) 
    {
        super.writeToNBT(nbt);
        nbt.setFloat("progress", netData.floats[0]);
        nbt.setInteger("resistor", netData.ints[0]);
        nbt.setFloat("pScale", powerScale);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) 
    {
        super.readFromNBT(nbt);
        netData.floats[0] = nbt.getFloat("progress");
        netData.ints[0] = nbt.getInteger("resistor");
        powerScale = nbt.getFloat("pScale");
    }
    
    @Override
    public void initContainer(TileContainer container)
    {
        container.addEntitySlot(new Slot(this, 0, 44, 34));
        container.addEntitySlot(new Slot(this, 1, 62, 34));
        
        container.addEntitySlot(new SlotOutput(this, 2, 116, 34));
        container.addEntitySlot(new SlotOutput(this, 3, 134, 34));
        
        container.addPlayerInventory(8, 86);
    }
    
    @Override
    public int[] stackTransferTarget(ItemStack item, int s, TileContainer container) 
    {
        int[] inv = container.getPlayerInv();
        if (s < inv[0]) return inv;
        else return new int[]{0, 2};
    }
    
}