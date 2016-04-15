/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cd4017be.automation.Item;

import java.util.Arrays;

import cd4017be.lib.util.Utils.ItemType;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

/**
 *
 * @author CD4017BE
 */
public class PipeUpgradeItem 
{
    public ItemStack[] list = new ItemStack[0];
    public byte mode;//1=invert; 2=force; 4=meta; 8=nbt; 16=ore; 32=count; 64=redstone; 128=invertRS
    public byte priority;
    
    public int getInsertAmount(ItemStack stack, IInventory inv, int[] slots, boolean rs)
    {
        if ((mode & 64) != 0 && (rs ^ (mode & 128) == 0)) return 0;
        if (stack == null) return 0;
        int i = this.getFilter().getMatch(stack);
        if ((mode & 32) != 0 && i >= 0) {
            int n = list[i].stackSize;
            ItemType filter = new ItemType((mode&4)!=0, (mode&8)!=0, (mode&16)!=0, list[i]);
            for (int s : slots) {
                ItemStack item = inv.getStackInSlot(s);
                if (filter.matches(item)) n -= item.stackSize;
                if (n <= 0) return 0;
            }
            return n;
        } else return i >= 0 ^ (mode & 1) != 0 ? stack.stackSize : 0;
    }
    
    public ItemStack getExtractItem(ItemStack stack, IInventory inv, int[] slots, boolean rs)
    {
        if ((mode & 64) != 0 && (rs ^ (mode & 128) == 0)) return null;
        ItemType filter = this.getFilter();
        if (stack != null) {
            int i = filter.getMatch(stack);
            if ((mode & 32) != 0 && i >= 0) {
                int n = -list[i].stackSize;
                filter = new ItemType((mode&4)!=0, (mode&8)!=0, (mode&16)!=0, list[i]);
                for (int s : slots) {
                    ItemStack item = inv.getStackInSlot(s);
                    if (filter.matches(item)) n += item.stackSize;
                    if (n >= 64) {
                        n = 64;
                        break;
                    }
                }
                if (n > 0) {
                    ItemStack item = stack.copy();
                    item.stackSize = n;
                    return item;
                } else return null;
            } else if (i >= 0 ^ (mode & 1) != 0) {
                ItemStack item = stack.copy();
                item.stackSize = 64;
                return item;
            } else return null;
        } else {
            for (int i = 0; i < slots.length; i++) {
                ItemStack item = inv.getStackInSlot(slots[i]);
                if (item != null) {
                    item = this.getExtractItem(item, inv, Arrays.copyOfRange(slots, i, slots.length), rs);
                    if (item != null) return item;
                }
            }
            return null;
        }
    }
    
    public ItemType getFilter()
    {
        return new ItemType((mode&4)!=0, (mode&8)!=0, (mode&16)!=0, list);
    }
    
    public boolean transferItem(ItemStack stack)
    {
        if ((mode & 2) == 0) return true;
        else if (stack == null) return false;
        return getFilter().matches(stack) ^ (mode & 1) == 0;
    }
    
    public static PipeUpgradeItem load(NBTTagCompound nbt)
    {
        PipeUpgradeItem upgrade = new PipeUpgradeItem();
        upgrade.mode = nbt.getByte("mode");
        upgrade.priority = nbt.getByte("prior");
        if (nbt.hasKey("list")) {
            NBTTagList list = nbt.getTagList("list", 10);
            upgrade.list = new ItemStack[list.tagCount()];
            for (int i = 0; i < list.tagCount(); i++) {
                upgrade.list[i] = ItemStack.loadItemStackFromNBT(list.getCompoundTagAt(i));
            }
        }
        return upgrade;
    }
    
    public void save(NBTTagCompound nbt) {
    	nbt.setByte("mode", mode);
        nbt.setByte("prior", priority);
        if (list.length > 0) {
            NBTTagList tlist = new NBTTagList();
            for (ItemStack item : list) {
                NBTTagCompound tag = new NBTTagCompound();
                item.writeToNBT(tag);
                tlist.appendTag(tag);
            }
            nbt.setTag("list", tlist);
        }
    }
    
    public static NBTTagCompound save(PipeUpgradeItem upgrade)
    {
        NBTTagCompound nbt = new NBTTagCompound();
        upgrade.save(nbt);
        return nbt;
    }
    
    public boolean isEmpty() {
    	return list.length == 0 && (mode & 1) == 0;
    }
    
    public static boolean isNullEq(PipeUpgradeItem filter) {
    	return filter == null || (filter.list.length == 0 && (filter.mode & 1) != 0);
    }
    
}
