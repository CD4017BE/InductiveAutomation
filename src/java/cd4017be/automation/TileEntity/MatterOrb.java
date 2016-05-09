/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cd4017be.automation.TileEntity;

import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.ITextComponent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cd4017be.api.automation.IMatterStorage;
import cd4017be.api.automation.MatterOrbItemHandler;
import cd4017be.automation.Item.ItemMatterOrb;
import cd4017be.lib.ModTileEntity;
import cd4017be.lib.util.Utils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

/**
 *
 * @author CD4017BE
 */
public class MatterOrb extends ModTileEntity implements IMatterStorage, IInventory
{
    private ArrayList<ItemStack> storage = new ArrayList<ItemStack>();
    private ItemStack in;
    
    @Override
    public ArrayList<ItemStack> dropItem(IBlockState state, int fortune) 
    {
        ArrayList<ItemStack> list = new ArrayList<ItemStack>();
        ItemStack item = new ItemStack(state.getBlock(), 1, 0);
        MatterOrbItemHandler.addItemStacks(item, storage.toArray(new ItemStack[storage.size()]));
        list.add(item);
        return list;
    }

    @Override
    public void onPlaced(EntityLivingBase entity, ItemStack item) 
    {
        if (item != null && item.getItem() == Item.getItemFromBlock(this.getBlockType()) && MatterOrbItemHandler.isMatterOrb(item))
        {
            storage.clear();
            storage.addAll(Arrays.asList(MatterOrbItemHandler.getAllItems(item)));
        }
    }

    @Override
    public ItemStack getFirstItem() 
    {
        return storage.size() > 0 ? storage.get(0) : null;
    }

    @Override
    public ArrayList<ItemStack> getAllItems() 
    {
        return storage;
    }

    @Override
    public ItemStack removeItems(int n) 
    {
        ItemStack item = this.getFirstItem();
        if (item == null || n <= 0) return null;
        if (item.stackSize <= n) return storage.remove(0);
        else return item.splitStack(n);
    }

    @Override
    public ArrayList<ItemStack> addItems(List<ItemStack> items) 
    {
        ArrayList<ItemStack> remain = new ArrayList<ItemStack>();
        for (ItemStack item : items) {
            if (item == null || item.getItem() == null) continue;
        	for (ItemStack stack : storage) {
                if (Utils.itemsEqual(item, stack)) {
                    stack.stackSize += item.stackSize;
                    item.stackSize = 0;
                    break;
                }
            }
            if (item.stackSize <= 0) {}
            else if (storage.size() < ItemMatterOrb.MaxTypes) storage.add(item);
            else remain.add(item);
        }
        return remain;
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) 
    {
        super.writeToNBT(nbt);
        nbt.setTag("Items", this.writeItems());
        if (in != null) {
            NBTTagCompound tag = new NBTTagCompound();
            in.writeToNBT(tag);
            nbt.setTag("In", tag);
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) 
    {
        super.readFromNBT(nbt);
        this.readItems(nbt.getTagList("Items", 10));
        if (nbt.hasKey("In")) {
            in = ItemStack.loadItemStackFromNBT(nbt.getCompoundTag("In"));
        } else in = null;
    }
    
    public void readItems(NBTTagList list) {
    	storage.clear();
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound tag = list.getCompoundTagAt(i);
            ItemStack item = new ItemStack(Item.getItemById(tag.getShort("i")), tag.getInteger("n"), tag.getShort("d"));
            if (tag.hasKey("t")) item.setTagCompound(tag.getCompoundTag("t"));
            storage.add(item);
        }
    }
    
    public NBTTagList writeItems() {
    	NBTTagList list = new NBTTagList();
        for (ItemStack item : storage) {
            NBTTagCompound tag = new NBTTagCompound();
            if (item.getItem() == null) continue;
            tag.setShort("i", (short)Item.getIdFromItem(item.getItem()));
            tag.setShort("d", (short)item.getItemDamage());
            tag.setInteger("n", item.stackSize);
            if (item.getTagCompound() != null) tag.setTag("t", item.getTagCompound());
            list.appendTag(tag);
        }
        return list;
    }

    @Override
    public int getSizeInventory() 
    {
        return 1;
    }

    @Override
    public ItemStack getStackInSlot(int i) 
    {
        return in;
    }

    @Override
    public ItemStack decrStackSize(int i, int n) 
    {
        if (in == null) return null;
        if (n >= in.stackSize) return this.removeStackFromSlot(i);
        else return in.splitStack(n);
    }

    @Override
    public ItemStack removeStackFromSlot(int i) 
    {
        ItemStack item = in;
        in = null;
        return item;
    }

    @Override
    public void setInventorySlotContents(int i, ItemStack item) 
    {
        in = item;
        ArrayList<ItemStack> remain = this.addItems(new ArrayList<ItemStack>(Arrays.asList(in)));
        in = remain.size() > 0 ? remain.get(0) : null;
    }

    @Override
    public String getName() 
    {
        return "Matter Orb";
    }

    @Override
    public int getInventoryStackLimit() 
    {
        return 64;
    }

    @Override
    public boolean isItemValidForSlot(int i, ItemStack item) 
    {
        return true;
    }

	@Override
	public boolean hasCustomName() {
		return true;
	}

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
	public void setField(int id, int value) {
	}

	@Override
	public int getFieldCount() {
		return 0;
	}

	@Override
	public void clear() {
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer player) {
		return false;
	}
    
}
