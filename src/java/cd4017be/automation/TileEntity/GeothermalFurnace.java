/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cd4017be.automation.TileEntity;


import cd4017be.automation.Config;
import cd4017be.automation.Objects;
import cd4017be.lib.TileContainer;
import cd4017be.lib.TileContainer.TankSlot;
import cd4017be.lib.TileEntityData;
import cd4017be.lib.templates.AutomatedTile;
import cd4017be.lib.templates.IAutomatedInv;
import cd4017be.lib.templates.Inventory;
import cd4017be.lib.templates.Inventory.Component;
import cd4017be.lib.templates.SlotItemType;
import cd4017be.lib.templates.SlotOutput;
import cd4017be.lib.templates.SlotTank;
import cd4017be.lib.templates.TankContainer;
import cd4017be.lib.templates.TankContainer.Tank;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidHandler;

/**
 *
 * @author CD4017BE
 */
public class GeothermalFurnace extends AutomatedTile implements IAutomatedInv, IFluidHandler
{
    public static int Euse = 160;
	private boolean melting = false;
    
    public GeothermalFurnace()
    {
        netData = new TileEntityData(2, 5, 0, 1);
        inventory = new Inventory(this, 7, new Component(4, 5, -1), new Component(5, 6, 1), new Component(0, 1, -1), new Component(1, 2, 0));
        tanks = new TankContainer(this, new Tank(Config.tankCap[1], 0, Objects.L_lava).setIn(2).setOut(3)).setNetLong(1);
        
    }

    @Override
    public void update() 
    {
    	super.update();
        if (worldObj.isRemote) return;
        //Fuel Burn
        int dm = 2;
        if (netData.ints[1] > 0)
        {
        	if (netData.ints[1] - dm < 0) dm = netData.ints[1];
            netData.ints[1] -= dm;
            netData.ints[2] += dm;
        }
        if (netData.ints[1] <= 0 && netData.ints[2] < 640) burnItem();
        //Lava melt/cool 
        dm = 18 - netData.ints[2] / 32;
        if (!melting && dm < 0 && inventory.items[1] != null && inventory.items[1].getItem() == Item.getItemFromBlock(Blocks.STONE))
        {
            this.decrStackSize(1, 1);
            melting = true;
            if (netData.ints[3] >= 2000) netData.ints[3] -= 2000;
        } else
        if (!melting && dm > 0 && tanks.getAmount(0) >= 100)
        {
            tanks.drain(0, 100, true);
            melting = true;
            if (netData.ints[3] <= 0) netData.ints[3] += 2000; 
        }
        if (melting && ((dm < 0 && netData.ints[3] < 2000) || (dm > 0 && netData.ints[3] > 0)))
        {
            if (netData.ints[3] - dm > 2000) dm = netData.ints[3] - 2000;
            if (netData.ints[3] - dm < 0) dm = netData.ints[3];
        	netData.ints[2] += dm;
            netData.ints[3] -= dm;
        }
        if (melting && netData.ints[3] >= 2000 && tanks.fill(0, new FluidStack(Objects.L_lava, 100), false) == 100) {
	        tanks.fill(0, new FluidStack(Objects.L_lava, 100), true);
	        melting = false;
        } else if (melting && netData.ints[3] <= 0 && this.putItemStack(new ItemStack(Blocks.STONE), this, -1, 1) == null) {
        	melting = false;
        }
        if (netData.ints[2] > 640) netData.ints[2] = 640;
        //process Item
        if (inventory.items[6] == null && netData.ints[2] + netData.ints[4] >= Euse && inventory.items[4] != null)
        {
            ItemStack item = FurnaceRecipes.instance().getSmeltingResult(inventory.items[4]);
            if (item != null)
            {
                decrStackSize(4, 1);
                inventory.items[6] = item.copy();
            }
        }
        if (inventory.items[6] != null)
        {
            if (netData.ints[4] < Euse)
            {
                int dp = 1 + netData.ints[2] * 7 / 320;
                if (dp > netData.ints[2]) dp = netData.ints[2];
                netData.ints[4] += dp;
                netData.ints[2] -= dp;
            }
            if (netData.ints[4] >= Euse)
            {
                inventory.items[6] = this.putItemStack(inventory.items[6], this, -1, 5);
                if (inventory.items[6] == null) netData.ints[4] -= Euse;
            }
        }
    }
    
    private void burnItem()
    {
        if (inventory.items[0] != null)
        {
            int n = TileEntityFurnace.getItemBurnTime(inventory.items[0]);
            if (n != 0)
            {
                netData.ints[0] = n;
                netData.ints[1] = netData.ints[0];
                this.decrStackSize(0, 1);
            }
        }
    }
    
    public int getMeltScaled(int s)
    {
        return netData.ints[3] < 0 ? 0 : netData.ints[3] > 2000 ? s : netData.ints[3] * s / 2000;
    }
    
    public int getBurnScaled(int s)
    {
        return netData.ints[0] > 0 ? netData.ints[1] * s / netData.ints[0] : 0;
    }
    
    public int getHeatScaled(int s)
    {
        return netData.ints[2] * s / 640;
    }
    
    public int getProgressScaled(int s)
    {
        return netData.ints[4] > Euse ? s : netData.ints[4] * s / Euse;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) 
    {
        nbt.setInteger("progress", netData.ints[4]);
        nbt.setInteger("heat", netData.ints[2]);
        nbt.setInteger("melt", netData.ints[3]);
        nbt.setInteger("burn", netData.ints[1]);
        nbt.setBoolean("melting", melting);
        return super.writeToNBT(nbt);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) 
    {
        super.readFromNBT(nbt);
        netData.ints[4] = nbt.getInteger("progress");
        netData.ints[2] = nbt.getInteger("heat");
        netData.ints[3] = nbt.getInteger("melt");
        netData.ints[1] = nbt.getInteger("burn");
        netData.ints[0] = netData.ints[1];
        melting = nbt.getBoolean("melting");
    }

    @Override
    public boolean canInsert(ItemStack item, int cmp, int i) 
    {
        return isValid(item, cmp, i);
    }

    @Override
    public boolean canExtract(ItemStack item, int cmp, int i) 
    {
        return true;
    }

    @Override
    public boolean isValid(ItemStack item, int cmp, int i) 
    {
        return cmp != 1 || item == null || item.getItem() == Item.getItemFromBlock(Blocks.STONE);
    }
    
    @Override
    public void initContainer(TileContainer container)
    {
        container.addEntitySlot(new Slot(this, 0, 62, 52));
        container.addEntitySlot(new SlotItemType(this, 1, 62, 16, new ItemStack[]{new ItemStack(Blocks.STONE)}));
        container.addEntitySlot(new SlotTank(this, 2, 8, 34));
        container.addEntitySlot(new SlotTank(this, 3, 26, 34));
        container.addEntitySlot(new Slot(this, 4, 98, 34));
        container.addEntitySlot(new SlotOutput(this, 5, 152, 34));
        
        container.addPlayerInventory(8, 86);
        
        container.addTankSlot(new TankSlot(tanks, 0, 8, 16, true));
    }
    
    @Override
    public int[] stackTransferTarget(ItemStack item, int s, TileContainer container) 
    {
        int[] pi = container.getPlayerInv();
        if (s < pi[0]) return pi;
        else return new int[]{4, 5};
    }

    @Override
    public void slotChange(ItemStack oldItem, ItemStack newItem, int i) {
    }
    
}
