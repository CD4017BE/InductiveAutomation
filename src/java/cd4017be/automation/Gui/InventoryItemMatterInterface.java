/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cd4017be.automation.Gui;

import java.io.IOException;

import cd4017be.api.automation.MatterOrbItemHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.ITextComponent;

/**
 *
 * @author CD4017BE
 */
public class InventoryItemMatterInterface implements IInventory
{
    public final ItemStack[] inventory = new ItemStack[5];
    
    public void update()
    {
        if (MatterOrbItemHandler.isMatterOrb(inventory[0])) {
            if (inventory[1] != null) {
                ItemStack[] remain = MatterOrbItemHandler.addItemStacks(inventory[0], inventory[1]);
                inventory[1] = remain.length == 0 ? null : remain[0];
            }
            this.updateOutput();
        } else {
            inventory[2] = null;
        }
    }
    
    public void onCommand(PacketBuffer dis) throws IOException
    {
        byte cmd = dis.readByte();
        if (cmd == 0) { //flip Stack
            ItemStack item = MatterOrbItemHandler.decrStackSize(inventory[0], 0, Integer.MAX_VALUE);
            if (item != null) {
                MatterOrbItemHandler.addItemStacks(inventory[0], item);
                this.updateOutput();
            }
        } else if (cmd == 1) {
            int n = dis.readShort();
            if (n > 4096) n = 4096;
            ItemStack item = MatterOrbItemHandler.getItem(inventory[0], 0);
            if (item == null) return;
            if (n > 0 && item.stackSize > n) item.stackSize = n;
            else n = item.stackSize;
            ItemStack[] remain = MatterOrbItemHandler.addItemStacks(inventory[3], item);
            if (remain.length == 0) {
                MatterOrbItemHandler.decrStackSize(inventory[0], 0, n);
                this.updateOutput();
            }
        }
    }
    
    @Override
    public int getSizeInventory() 
    {
        return inventory.length;
    }

    @Override
    public ItemStack getStackInSlot(int i) 
    {
        return inventory[i];
    }

    @Override
    public ItemStack decrStackSize(int i, int n) 
    {
        if (i == 2) {
            ItemStack item = MatterOrbItemHandler.decrStackSize(inventory[0], 0, n);
            this.updateOutput();
            return item;
        }
        if (inventory[i] == null) return null;
        ItemStack item = inventory[i].stackSize <= n ? this.removeStackFromSlot(i) : inventory[i].splitStack(n);
        if (i == 0) this.updateOutput();
        return item;
    }

    @Override
    public ItemStack removeStackFromSlot(int i) 
    {
        if (i == 2 || i == 4) return null;
        else if (i == 0) this.updateOutput();
        ItemStack item = inventory[i];
        inventory[i] = null;
        return item;
    }

    @Override
    public void setInventorySlotContents(int i, ItemStack item) 
    {
        if (i == 2) {
            inventory[2] = item;
            int n = (inventory[4] != null ? inventory[4].stackSize : 0) - (inventory[2] != null ? inventory[2].stackSize : 0);
            inventory[4] = inventory[2] != null ? inventory[2].copy() : null;
            if (n > 0) MatterOrbItemHandler.decrStackSize(inventory[0], 0, n);
        } else {
            inventory[i] = item;
            if (i == 0) this.updateOutput();
        }
    }

    @Override
    public int getInventoryStackLimit() 
    {
        return 64;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) 
    {
        return !player.isDead;
    }

    @Override
    public boolean isItemValidForSlot(int i, ItemStack itemstack) 
    {
        return i != 2;
    }
    
    private void updateOutput()
    {
        inventory[2] = MatterOrbItemHandler.getItem(inventory[0], 0);
        if (inventory[2] != null && inventory[2].stackSize > 64) inventory[2].stackSize = 64;
        inventory[4] = inventory[2] != null ? inventory[2].copy() : null;
    }

	@Override
	public String getName() {
		return "Portable Matter Interface";
	}

	@Override
	public boolean hasCustomName() {
		return true;
	}

	@Override
	public void markDirty() {}

	@Override
	public void openInventory(EntityPlayer player) {}

	@Override
	public void closeInventory(EntityPlayer player) {}
	
	@Override
	public ITextComponent getDisplayName() {
		return new TextComponentString(this.getName());
	}

	@Override
	public int getField(int id) {
		return 0;
	}

	@Override
	public void setField(int id, int value) {}

	@Override
	public int getFieldCount() {
		return 0;
	}

	@Override
	public void clear() {}
	
}
