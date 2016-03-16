package cd4017be.automation.Gui;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;

public class InventoryPlacement implements IInventory
{

	public final ItemStack[] inventory = new ItemStack[8];
    public byte[] placement = new byte[8];
    public float[] Vxy = new float[]{0.5F, 0.5F, 0.5F, 0.5F, 0.5F, 0.5F, 0.5F, 0.5F, 0.5F, 0.5F, 0.5F, 0.5F, 0.5F, 0.5F, 0.5F, 0.5F};
    public boolean useBlock;
	
    public InventoryPlacement(ItemStack item)
    {
        if (item.stackTagCompound == null) item.stackTagCompound = new NBTTagCompound();
        useBlock = item.stackTagCompound.getBoolean("block");
        if (item.stackTagCompound.hasKey("list")) {
            NBTTagList list = item.stackTagCompound.getTagList("list", 10);
            for (int i = 0; i < list.tagCount(); i++) {
                NBTTagCompound tag = list.getCompoundTagAt(i);
            	inventory[i] = ItemStack.loadItemStackFromNBT(tag);
            	placement[i] = tag.getByte("mode");
            	Vxy[i] = tag.getFloat("Vx");
            	Vxy[i|8] = tag.getFloat("Vy");
            }
        }
    }
    
    public boolean useDamage(int i)
    {
    	return (placement[i] & 16) == 0;
    }
    
    public boolean sneak(int i)
    {
    	return (placement[i] & 8) != 0;
    }
    
    public byte getDir(int i)
    {
    	return (byte)(placement[i] & 7);
    }
    
    public void onCommand(PacketBuffer dis) throws IOException
    {
        byte cmd = dis.readByte();
        if (cmd <= 1) {
        	useBlock = cmd == 1;
        } else if (cmd == 3) {
        	byte s = dis.readByte();
        	if (s >= 0 && s < placement.length) placement[s] = dis.readByte();
        } else if (cmd == 2) {
        	byte s = dis.readByte();
        	if (s >= 0 && s < Vxy.length) Vxy[s] = dis.readFloat(); 
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
        if (item.stackTagCompound == null) item.stackTagCompound = new NBTTagCompound();
        item.stackTagCompound.setBoolean("block", useBlock);
        NBTTagList list = new NBTTagList();
        for (int i = 0; i < inventory.length; i++) {
        	if (inventory[i] == null) continue;
        	NBTTagCompound tag = new NBTTagCompound();
        	inventory[i].writeToNBT(tag);
        	tag.setByte("mode", placement[i]);
        	tag.setFloat("Vx", Vxy[i]);
        	tag.setFloat("Vy", Vxy[i|8]);
        	list.appendTag(tag);
        }
        item.stackTagCompound.setTag("list", list);
        
    }

	@Override
	public String getName() {
		return "Block placement controller";
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
	public IChatComponent getDisplayName() {
		return new ChatComponentText(this.getName());
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
