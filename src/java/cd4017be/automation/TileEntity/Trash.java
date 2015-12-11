/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cd4017be.automation.TileEntity;

import java.io.DataInputStream;
import java.io.IOException;

import cd4017be.automation.Config;
import cd4017be.lib.TileContainer;
import cd4017be.lib.TileEntityData;
import cd4017be.lib.templates.AutomatedTile;
import cd4017be.lib.templates.Inventory;
import cd4017be.lib.templates.Inventory.Component;
import cd4017be.lib.templates.SlotTank;
import cd4017be.lib.templates.TankContainer;
import cd4017be.lib.templates.TankContainer.Tank;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.IFluidHandler;

/**
 *
 * @author CD4017BE
 */
public class Trash extends AutomatedTile implements ISidedInventory, IFluidHandler
{
    
    public Trash()
    {
        netData = new TileEntityData(2, 1, 0, 1);
        inventory = new Inventory(this, 2, new Component(0, 1, -1));
        tanks = new TankContainer(this, new Tank(Config.tankCap[1], -1).setIn(1)).setNetLong(1);
        
    }

    @Override
    public void updateEntity() 
    {
        super.updateEntity();
        if (worldObj.isRemote) return;
        if ((netData.ints[0] & 1) != 0)inventory.setInventorySlotContents(0, null);
        if ((netData.ints[0] & 2) != 0)tanks.setFluid(0, null);
    }

    @Override
    protected void customPlayerCommand(byte cmd, DataInputStream dis, EntityPlayerMP player) throws IOException 
    {
        if (cmd == 0) netData.ints[0] ^= 1;
        else if (cmd == 1) netData.ints[0] ^= 2;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) 
    {
        super.readFromNBT(nbt);
        netData.ints[0] = nbt.getByte("active");
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) 
    {
        super.writeToNBT(nbt);
        nbt.setByte("active", (byte)netData.ints[0]);
    }

    @Override
    public void initContainer(TileContainer container) 
    {
        container.addEntitySlot(new Slot(this, 0, 134, 16));
        container.addEntitySlot(new SlotTank(this, 1, 26, 16));
        container.addPlayerInventory(8, 50);
    }

    @Override
    public int[] stackTransferTarget(ItemStack item, int s, TileContainer container) 
    {
        int[] pi = container.getPlayerInv();
        if (s >= pi[0] && s < pi[1]) return new int[]{0, 1};
        return null;
    }
    
}
