/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cd4017be.automation.TileEntity;

import net.minecraft.network.PacketBuffer;

import java.io.IOException;

import cd4017be.lib.TileEntityData;
import cd4017be.lib.Gui.SlotOutput;
import cd4017be.lib.Gui.TileContainer;
import cd4017be.lib.templates.AutomatedTile;
import cd4017be.lib.templates.IAutomatedInv;
import cd4017be.lib.templates.Inventory;
import cd4017be.lib.templates.Inventory.Group;
import cd4017be.lib.util.Utils;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.nbt.NBTTagCompound;

/**
 *
 * @author CD4017BE
 */
public class AutoCrafting extends AutomatedTile implements IAutomatedInv
{
    private ItemStack craftOut = null;
    private boolean craftChange;
    
    public AutoCrafting()
    {
        inventory = new Inventory(this, 11, new Group(0, 1, -1), new Group(1, 2, -1), new Group(2, 3, -1), new Group(3, 9, -1), new Group(9, 11, 1));
        netData = new TileEntityData(2, 0, 0, 0);
    }
    
    @Override
    public void update() 
    {
    	super.update();
        if (worldObj.isRemote) return;
        boolean rst = worldObj.isBlockPowered(pos);
        if (craftChange) craftOut = getCraftingOutput();
        boolean craft = false;
        int mode = this.getRef(9);
        if (mode == 0) craft = inventory.items[9] == null;
        else if (mode == 1 && rst) craft = inventory.items[9] == null;
        else if (mode == 2 && rst && this.getRef(10) == 0) craft = true;
        this.setRef(10, rst ? (byte)1 : (byte)0);
        if (craft && craftOut != null) {
            if (inventory.items[9] == null) {
                inventory.items[9] = craftOut;
                craftItem();
                craftChange = true;
            } else if (Utils.itemsEqual(inventory.items[9], craftOut) && inventory.items[9].stackSize <= craftOut.getMaxStackSize() - craftOut.stackSize) {
                inventory.items[9].stackSize += craftOut.stackSize;
                craftItem();
                craftChange = true;
            }
        }
    }
    
    private ItemStack getCraftingOutput()
    {
        int[] am = new int[9];
        for (int i = 0; i < 9; i++) {
            int s = this.getRef(i);
            if (s < 9) am[s]++;
        }
        for (int i = 0; i < am.length; i++)
        if (inventory.items[i] != null && inventory.items[i].stackSize < am[i]) return null;
        InventoryCrafting icr = new InventoryCrafting(new Container() {
            @Override
            public boolean canInteractWith(EntityPlayer var1) {
                return true;
            }
            @Override
            public void onCraftMatrixChanged(IInventory par1IInventory) {}
        }, 3, 3);
        for (int i = 0; i < 9; i++) {
            int s = this.getRef(i);
            icr.setInventorySlotContents(i, s < 9 ? inventory.items[s] : null);
        }
        craftChange = false;
        return CraftingManager.getInstance().findMatchingRecipe(icr, worldObj);
    }
    
    private void craftItem()
    {
        craftOut = null;
        for (int i = 0; i < 9; i++) {
            int s = this.getRef(i);
            ItemStack item = s < 9 ? inventory.items[s] : null;
            if (item != null) {
                this.decrStackSize(s, 1);
                if (item.getItem().hasContainerItem(item)) {
                    ItemStack iout = item.getItem().getContainerItem(item);
                    if (iout.isItemStackDamageable() && iout.getItemDamage() > iout.getMaxDamage()) iout = null;
                    if (iout != null) {
                        if (inventory.items[s] == null) this.setInventorySlotContents(s, iout);
                        else this.putToUnusedSlotOrDrop(iout);
                    }
                }
            }
        }
    }
    
    private void putToUnusedSlotOrDrop(ItemStack item)
    {
        boolean[] used = new boolean[9];
        for (int i = 0; i < 9; i++) {
            int s = this.getRef(i);
            if (s < 9) used[s] = true;
        }
        for (int i = 0; i < used.length; i++)
        if (!used[i]) {
            if (inventory.items[i] == null) {
                inventory.items[i] = item;
                return;
            } else if (Utils.itemsEqual(inventory.items[i], item)) {
                if (item.stackSize <= inventory.items[i].getMaxStackSize() - inventory.items[i].stackSize) {
                    inventory.items[i].stackSize += item.stackSize;
                    return;
                } else
                {
                    item.stackSize -= inventory.items[i].getMaxStackSize() - inventory.items[i].stackSize;
                    inventory.items[i].stackSize = inventory.items[i].getMaxStackSize();
                }
            }
        }
        EntityItem entity = new EntityItem(worldObj, pos.getX() + 0.5D, pos.getY() + 1.5D, pos.getZ() + 0.5D, item);
        worldObj.spawnEntityInWorld(entity);
    }
    
    public int getRef(int i)
    {
        return (int)(netData.longs[1] >> (i * 4) & 0xf);
    }
    
    public void setRef(int i, byte v)
    {
        netData.longs[1] &= ~((long)0xf << (i * 4));
        netData.longs[1] |= (long)(v & 0xf) << (i * 4);
    }
    
    @Override
    protected void customPlayerCommand(byte cmd, PacketBuffer dis, EntityPlayerMP player) throws IOException 
    {
        if (cmd == 0) {
            netData.longs[1] = dis.readLong();
            craftChange = true;
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) 
    {
        super.readFromNBT(nbt);
        netData.longs[1] = nbt.getLong("data");
        craftOut = null;
        craftChange = true;
    }


    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) 
    {
        nbt.setLong("data", netData.longs[1]);
        return super.writeToNBT(nbt);
    }
    
    @Override
    public void initContainer(TileContainer container) 
    {
        for (int j = 0; j < 3; j++)
        for (int i = 0; i < 3; i++)
        container.addItemSlot(new Slot(this, i + j * 3, 8 + i * 18, 16 + j * 18));
        container.addItemSlot(new SlotOutput(this, 9, 152, 34));
        container.addItemSlot(new SlotOutput(this, 10, 152, 52));
        
        container.addPlayerInventory(8, 86);
    }

    @Override
    public int[] stackTransferTarget(ItemStack item, int s, TileContainer container) 
    {
        int[] pi = container.getPlayerInv();
        if (s < pi[0]) return pi;
        else return new int[]{0, 12};
    }

    @Override
    public boolean canInsert(ItemStack item, int cmp, int i) {
        return true;
    }

    @Override
    public boolean canExtract(ItemStack item, int cmp, int i) {
        return true;
    }

    @Override
    public boolean isValid(ItemStack item, int cmp, int i) {
        return true;
    }

    @Override
    public void slotChange(ItemStack oldItem, ItemStack newItem, int i) 
    {
        this.craftChange = true;
    }
    
}
