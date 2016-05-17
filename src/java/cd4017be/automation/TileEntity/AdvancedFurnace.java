/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cd4017be.automation.TileEntity;

import net.minecraft.network.PacketBuffer;

import java.io.IOException;

import cd4017be.api.automation.AutomationRecipes;
import cd4017be.api.automation.AutomationRecipes.LFRecipe;
import cd4017be.api.automation.IEnergy;
import cd4017be.api.automation.PipeEnergy;
import cd4017be.automation.Config;
import cd4017be.lib.TileContainer;
import cd4017be.lib.TileContainer.TankSlot;
import cd4017be.lib.TileEntityData;
import cd4017be.lib.templates.AutomatedTile;
import cd4017be.lib.templates.Inventory;
import cd4017be.lib.templates.Inventory.Component;
import cd4017be.lib.templates.SlotOutput;
import cd4017be.lib.templates.SlotTank;
import cd4017be.lib.templates.TankContainer;
import cd4017be.lib.templates.TankContainer.Tank;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidHandler;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.ISidedInventory;

/**
 *
 * @author CD4017BE
 */
public class AdvancedFurnace extends AutomatedTile implements IEnergy, ISidedInventory, IFluidHandler
{
    private float powerScale;
    private LFRecipe recipe;
    
    public AdvancedFurnace()
    {
        netData = new TileEntityData(2, 1, 3, 2);
        inventory = new Inventory(this, 8, new Component(2, 5, -1), new Component(5, 8, 1));
        tanks = new TankContainer(this, new Tank(Config.tankCap[1], -1).setIn(0), new Tank(Config.tankCap[1], 1).setOut(1)).setNetLong(1);
        energy = new PipeEnergy(Config.Umax[1], Config.Rcond[1]);
        netData.ints[0] = Config.Rmin;
        powerScale = Config.Pscale;
    }
    
    @Override
    public void update()
    {
        super.update();
        if (this.worldObj.isRemote) return;
        if (recipe == null && (tanks.getAmount(0) > 0 || inventory.items[2] != null || inventory.items[3] != null || inventory.items[4] != null))
        {
            ItemStack[] items = new ItemStack[3];
            System.arraycopy(inventory.items, 2, items, 0, items.length);
            recipe = AutomationRecipes.getRecipeFor(tanks.getFluid(0), items);
            if (recipe != null)
            {
                if (recipe.Linput != null) tanks.drain(0, recipe.Linput.amount, true);
                if (recipe.Iinput != null)
                for (Object item : recipe.Iinput)
                {
                    if (item == null) continue;
                    int n = AutomationRecipes.getStacksize(item);
                    for (int i = 2; i < 5 && n > 0; i++)
                    {
                        if (!AutomationRecipes.isItemEqual(inventory.items[i], item)) continue;
                        else if (inventory.items[i].stackSize <= n)
                        {
                            n -= inventory.items[i].stackSize;
                            inventory.items[i] = null;
                        } else
                        {
                            inventory.items[i].stackSize -= n;
                            n = 0;
                        }
                    }
                }
                netData.floats[1] = recipe.energy;
            }
        }
        netData.floats[2] = (float)energy.Ucap;
        double power = energy.getEnergy(0, netData.ints[0]) / 1000F;
        if (recipe != null)
        {
            if (netData.floats[0] < recipe.energy)
            {
                netData.floats[0] += power;
                energy.Ucap *= powerScale;
            }
            if (netData.floats[0] >= recipe.energy)
            {
                boolean r = false;
                if (recipe.Loutput != null) 
                {
                    recipe.Loutput.amount -= tanks.fill(1, recipe.Loutput, true);
                    if (recipe.Loutput.amount > 0) r = true;
                    else recipe.Loutput = null;
                }
                if (recipe.Ioutput != null)
                for (int i = 0; i < recipe.Ioutput.length; i++)
                if (recipe.Ioutput[i] != null)
                {
                    recipe.Ioutput[i] = this.putItemStack(recipe.Ioutput[i], this, -1, 5, 6, 7);
                    r |= recipe.Ioutput[i] != null;
                }
                if (!r)
                {
                    netData.floats[0] -= recipe.energy;
                    recipe = null;
                }
            }
        }
    }

    @Override
    protected void customPlayerCommand(byte cmd, PacketBuffer dis, EntityPlayerMP player) throws IOException 
    {
        if (cmd == 0)
        {
            netData.ints[0] = dis.readInt();
            if (netData.ints[0] < Config.Rmin) netData.ints[0] = Config.Rmin;
            powerScale = (float)Math.sqrt(1.0D - 1.0D / (double)netData.ints[0]);
        } else
        if (cmd == 1)
        {
            FluidStack ls = tanks.getFluid(0);
            tanks.setFluid(0, tanks.getFluid(1));
            tanks.setFluid(1, ls);
        }
    }

    
    public int getProgressScaled(int s)
    {
        int i = netData.floats[1] <= 0 ? 0 : (int)((float)s * netData.floats[0] / netData.floats[1]);
        return i < s ? i : s;
    }
    
    public int getPowerScaled(int s)
    {
        return (int)(s * netData.floats[2] * netData.floats[2] / (float)netData.ints[0]) / 200000;
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) 
    {
        super.writeToNBT(nbt);
        if (recipe != null) nbt.setTag("recipe", AutomationRecipes.writeToNBT(recipe));
        nbt.setFloat("progress", netData.floats[0]);
        nbt.setInteger("resistor", netData.ints[0]);
        nbt.setFloat("pScale", powerScale);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) 
    {
        super.readFromNBT(nbt);
        if (nbt.hasKey("recipe")) recipe = AutomationRecipes.readFromNBT(nbt.getCompoundTag("recipe"));
        else recipe = null;
        netData.floats[0] = nbt.getFloat("progress");
        netData.ints[0] = nbt.getInteger("resistor");
        powerScale = nbt.getFloat("pScale");
    }
    
    @Override
    public void initContainer(TileContainer container)
    {
        container.addEntitySlot(new SlotTank(this, 0, 62, 34));
        container.addEntitySlot(new SlotTank(this, 1, 152, 34));
        for (int i = 0; i < 3; i++)
        {
            container.addEntitySlot(new Slot(this, 2 + i, 80, 16 + 18 * i));
        }
        for (int i = 0; i < 3; i++)
        {
            container.addEntitySlot(new SlotOutput(this, 5 + i, 116, 16 + 18 * i));
        }
        
        container.addPlayerInventory(8, 86);
        
        container.addTankSlot(new TankSlot(tanks, 0, 44, 16, true));
        container.addTankSlot(new TankSlot(tanks, 1, 134, 16, true));
    }

    @Override
    public int[] stackTransferTarget(ItemStack item, int s, TileContainer container) 
    {
        int[] pi = container.getPlayerInv();
        if (s < pi[0]) return pi;
        else return new int[]{2, 5};
    }
    
}
