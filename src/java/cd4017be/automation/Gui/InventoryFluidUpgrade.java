/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cd4017be.automation.Gui;

import java.io.IOException;
import java.util.Arrays;

import cd4017be.automation.Item.ItemFluidDummy;
import cd4017be.automation.Item.PipeUpgradeFluid;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;

/**
 *
 * @author CD4017BE
 */
public class InventoryFluidUpgrade implements IInventory
{
    public final ItemStack[] inventory = new ItemStack[4];
    public final PipeUpgradeFluid upgrade;
    
    public InventoryFluidUpgrade(ItemStack item)
    {
        if (item.getTagCompound() == null) item.setTagCompound(new NBTTagCompound());
        upgrade = PipeUpgradeFluid.load(item.getTagCompound());
        for (int i = 0; i < upgrade.list.length && i < inventory.length; i++)
        {
            if (upgrade.list[i] != null) inventory[i] = ItemFluidDummy.item(upgrade.list[i].getFluid(), 1);
        }
    }
    
    public void onCommand(PacketBuffer dis) throws IOException
    {
        byte cmd = dis.readByte();
        if (cmd == 0) upgrade.mode = dis.readByte();
        else if (cmd == 1) {
            upgrade.maxAmount = dis.readInt();
            if (upgrade.maxAmount < 0) upgrade.maxAmount = 0;
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
        if (inventory[i] == null) return null;
        ItemStack item = inventory[i].stackSize <= n ? this.removeStackFromSlot(i) : inventory[i].splitStack(n);
        return item;
    }

    @Override
    public ItemStack removeStackFromSlot(int i) 
    {
        ItemStack item = inventory[i];
        inventory[i] = null;
        return item;
    }

    @Override
    public void setInventorySlotContents(int i, ItemStack item) 
    {
        inventory[i] = item;
    }

    @Override
    public int getInventoryStackLimit() 
    {
        return 1;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) 
    {
        return !player.isDead;
    }

    @Override
    public boolean isItemValidForSlot(int i, ItemStack itemstack) 
    {
        return true;
    }
    
    public void onClose(ItemStack item)
    {
        if (item == null) return;
        FluidStack[] list = new FluidStack[4];
        int n = 0;
        for (int i = 0; i < inventory.length; i++) {
            ItemStack stack = inventory[i];
            list[n] = FluidContainerRegistry.getFluidForFilledItem(stack);
            if (list[n] == null && stack != null) {
                if (stack.getItem() instanceof IFluidContainerItem) list[n] = ((IFluidContainerItem)stack.getItem()).getFluid(stack);
                else if (upgrade.list.length > i) list[n] = upgrade.list[i];
            }
            if (list[n] != null) n++;
        }
        upgrade.list = Arrays.copyOf(list, n);
        item.setTagCompound(PipeUpgradeFluid.save(upgrade));
    }

	@Override
	public String getName() {
		return "Fluid Pipe Filter";
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
