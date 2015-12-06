/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cd4017be.automation.TileEntity;

import java.io.DataInputStream;
import java.io.IOException;

import cd4017be.api.automation.MatterOrbItemHandler;
import cd4017be.lib.TileContainer;
import cd4017be.lib.TileEntityData;
import cd4017be.lib.templates.AutomatedTile;
import cd4017be.lib.templates.IAutomatedInv;
import cd4017be.lib.templates.Inventory;
import cd4017be.lib.templates.Inventory.Component;
import cd4017be.lib.templates.SlotOutput;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

/**
 *
 * @author CD4017BE
 */
public class MatterInterface extends AutomatedTile implements IAutomatedInv
{
    
    public MatterInterface()
    {
        inventory = new Inventory(this, 5, new Component(1, 2, -1), new Component(2, 3, 1), new Component(0, 1, 0)).setInvName("Matter Interface");
        netData = new TileEntityData(1, 1, 0, 0);
    }

    @Override
    public void updateEntity() 
    {
        super.updateEntity();
        if (MatterOrbItemHandler.isMatterOrb(inventory.items[0])) {
            if (inventory.items[1] != null) {
                ItemStack[] remain = MatterOrbItemHandler.addItemStacks(inventory.items[0], inventory.items[1]);
                inventory.items[1] = remain.length == 0 ? null : remain[0];
            }
            boolean rs = worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord);
            if (rs ^ (netData.ints[0] & 1) != 0) {
                netData.ints[0] ^= 1;
                if (rs && (netData.ints[0] & 2) != 0) {
                    ItemStack item = MatterOrbItemHandler.decrStackSize(inventory.items[0], 0, Integer.MAX_VALUE);
                    if (item != null) MatterOrbItemHandler.addItemStacks(inventory.items[0], item);
                }
            }
            this.updateOutput();
        } else {
            inventory.items[2] = null;
        }
    }
    
    private void updateOutput()
    {
        inventory.items[2] = MatterOrbItemHandler.getItem(inventory.items[0], 0);
        if (inventory.items[2] != null && inventory.items[2].stackSize > 64) inventory.items[2].stackSize = 64;
        inventory.items[4] = inventory.items[2] != null ? inventory.items[2].copy() : null;
    }
    
    @Override
    protected void customPlayerCommand(byte cmd, DataInputStream dis, EntityPlayerMP player) throws IOException 
    {
        if (cmd == 0) { //flip Stack
            ItemStack item = MatterOrbItemHandler.decrStackSize(inventory.items[0], 0, Integer.MAX_VALUE);
            if (item != null) {
                MatterOrbItemHandler.addItemStacks(inventory.items[0], item);
                this.updateOutput();
            }
        } else if (cmd == 1) {
            int n = dis.readShort();
            if (n > 4096) n = 4096;
            ItemStack item = MatterOrbItemHandler.getItem(inventory.items[0], 0);
            if (item == null) return;
            if (n > 0 && item.stackSize > n) item.stackSize = n;
            else n = item.stackSize;
            ItemStack[] remain = MatterOrbItemHandler.addItemStacks(inventory.items[3], item);
            if (remain.length == 0) {
                MatterOrbItemHandler.decrStackSize(inventory.items[0], 0, n);
                this.updateOutput();
            }
        } else if (cmd == 2) {
            netData.ints[0] = dis.readByte(); 
        }
    }

    @Override
    public int[] stackTransferTarget(ItemStack item, int s, TileContainer container) 
    {
        int[] pi = container.getPlayerInv();
        if (s < pi[0] || s >= pi[1]) return pi;
        else if (MatterOrbItemHandler.isMatterOrb(item)) return new int[]{0, 1};
        else return new int[]{1, 2};
    }

    @Override
    public void initContainer(TileContainer container) 
    {
        container.addEntitySlot(new Slot(this, 0, 44, 34));
        container.addEntitySlot(new Slot(this, 1, 26, 16));
        container.addEntitySlot(new SlotOutput(this, 2, 26, 52));
        container.addEntitySlot(new Slot(this, 3, 8, 34));
        
        container.addPlayerInventory(8, 86);
    }

    @Override
    public ItemStack decrStackSize(int i, int n) 
    {
        if (i == 2) {
            ItemStack item = MatterOrbItemHandler.decrStackSize(inventory.items[0], 0, n);
            this.updateOutput();
            return item;
        } else return super.decrStackSize(i, n);
    }

    @Override
    public void setInventorySlotContents(int i, ItemStack item) 
    {
        if (i == 2) {
            inventory.items[2] = item;
            int n = (inventory.items[4] != null ? inventory.items[4].stackSize : 0) - (inventory.items[2] != null ? inventory.items[2].stackSize : 0);
            inventory.items[4] = inventory.items[2] != null ? inventory.items[2].copy() : null;
            if (n > 0) MatterOrbItemHandler.decrStackSize(inventory.items[0], 0, n);
        } super.setInventorySlotContents(i, item);
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int i) 
    {
        if (i == 2 || i == 4) return null;
        else return super.getStackInSlotOnClosing(i);
    }
    
    @Override
    public boolean canInsert(ItemStack item, int cmp, int i) 
    {
        return i != 2;
    }

    @Override
    public boolean canExtract(ItemStack item, int cmp, int i) 
    {
        return true;
    }

    @Override
    public boolean isValid(ItemStack item, int cmp, int i) 
    {
        return i != 2;
    }

    @Override
    public void slotChange(ItemStack oldItem, ItemStack newItem, int i) 
    {
        if (i == 0) {
            this.updateOutput();
        } else if (i == 2) {
            int n = (oldItem == null ? 0 : oldItem.stackSize) - (newItem == null ? 0 : newItem.stackSize);
            if (n > 0) MatterOrbItemHandler.decrStackSize(inventory.items[0], 0, n);
            this.updateOutput();
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) 
    {
        super.readFromNBT(nbt);
        netData.ints[0] = nbt.getByte("mode");
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) 
    {
        super.writeToNBT(nbt);
        nbt.setByte("mode", (byte)netData.ints[0]);
    }
    
}
