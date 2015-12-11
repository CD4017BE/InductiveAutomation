/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cd4017be.automation.TileEntity;

import java.io.DataInputStream;
import java.io.IOException;

import cd4017be.api.automation.AutomationRecipes;
import cd4017be.api.automation.AutomationRecipes.CmpRecipe;
import cd4017be.api.automation.IEnergy;
import cd4017be.api.automation.PipeEnergy;
import cd4017be.automation.Config;
import cd4017be.lib.TileContainer;
import cd4017be.lib.TileEntityData;
import cd4017be.lib.templates.AutomatedTile;
import cd4017be.lib.templates.Inventory;
import cd4017be.lib.templates.Inventory.Component;
import cd4017be.lib.templates.SlotOutput;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

/**
 *
 * @author CD4017BE
 */
public class ElectricCompressor extends AutomatedTile implements ISidedInventory, IEnergy
{
    public static float Energy = 200F;
	private float powerScale = 0;
    private boolean craftInvChange = true;
    
    public ElectricCompressor()
    {
        inventory = new Inventory(this, 6, new Component(0, 1, -1), new Component(1, 2, -1), new Component(2, 3, -1), new Component(3, 4, -1), new Component(4, 5, 1));
        energy = new PipeEnergy(Config.Umax[1], Config.Rcond[1]);
        netData = new TileEntityData(1, 1, 2, 0);
        netData.ints[0] = Config.Rmin;
        powerScale = Config.Pscale;
    }
    
    @Override
    public void updateEntity() 
    {
        super.updateEntity();
        if(worldObj.isRemote) return;
        //power
        netData.floats[1] = (float)energy.Ucap;
        double power = energy.getEnergy(0, netData.ints[0]) / 1000F;
        //Item process
        craftInvChange |= this.checkInvChange();
        if (inventory.items[5] == null && craftInvChange)
        {
            CmpRecipe recipe = AutomationRecipes.getRecipeFor(inventory.items, 0, 4);
            if (recipe != null) {
            	int n;
            	for (int i = 0; i < recipe.input.length; i++)
                	if ((n = AutomationRecipes.getStacksize(recipe.input[i])) > 0) this.decrStackSize(i, n);
                inventory.items[5] = recipe.output.copy();
            } else
            craftInvChange = false;
        }
        if (inventory.items[5] != null)
        {
            if (netData.floats[0] < Energy)
            {
                netData.floats[0] += power;
                energy.Ucap *= powerScale;
            }
            if (netData.floats[0] >= Energy)
            {
                inventory.items[5] = this.putItemStack(inventory.items[5], this, -1, 4);
                if (inventory.items[5] == null) netData.floats[0] -= Energy;
            }
        }
    }
    
    private boolean checkInvChange()
    {
        boolean change = false;
        for (int i = 0; i < 4; i++) {
            change |= inventory.componets[i].invChange;
            inventory.componets[i].invChange = false;
        }
        return change;
    }

	@Override
	public void markDirty() {
		super.markDirty();
		craftInvChange = true;
	}

	public int getPowerScaled(int s)
    {
        return (int)(s * netData.floats[1] * netData.floats[1] / (float)netData.ints[0]) / 200000;
    }
    
    public int getProgressScaled(int s)
    {
        int i = (int)((double)s * netData.floats[0] / Energy);
        return i < s ? i : s;
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) 
    {
        super.writeToNBT(nbt);
        nbt.setFloat("progress", netData.floats[0]);
        nbt.setShort("resistor", (short)netData.ints[0]);
        nbt.setFloat("pScale", powerScale);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) 
    {
        super.readFromNBT(nbt);
        netData.floats[0] = nbt.getFloat("progress");
        netData.ints[0] = nbt.getShort("resistor");
        powerScale = nbt.getFloat("pScale");
        craftInvChange = true;
    }

    @Override
    protected void customPlayerCommand(byte cmd, DataInputStream dis, EntityPlayerMP player) throws IOException {
        if (cmd == 0) {
            netData.ints[0] = dis.readShort();
            if (netData.ints[0] < Config.Rmin) netData.ints[0] = Config.Rmin;
            powerScale = (float)Math.sqrt(1.0D - 1.0D / (double)netData.ints[0]);
        }
    }
    
    @Override
    public void initContainer(TileContainer container)
    {
        for (int i = 0; i < 2; i++)
        for (int j = 0; j < 2; j++)
        container.addEntitySlot(new Slot(this, i + 2 * j, 62 + 18 * i, 25 + 18 * j));
        container.addEntitySlot(new SlotOutput(this, 4, 134, 34));
        
        container.addPlayerInventory(8, 86);
    }
    
    @Override
    public int[] stackTransferTarget(ItemStack item, int s, TileContainer container) 
    {
        int[] inv = container.getPlayerInv();
        if (s < inv[0]) return inv;
        else return new int[]{0, 4};
    }
    
}
