/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cd4017be.automation.TileEntity;


import cd4017be.api.automation.AutomationRecipes;
import cd4017be.api.automation.AutomationRecipes.CmpRecipe;
import cd4017be.automation.Config;
import cd4017be.automation.Objects;
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
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.IFluidHandler;

/**
 *
 * @author CD4017BE
 */
public class SteamCompressor extends AutomatedTile implements ISidedInventory, IFluidHandler
{
    public static int Euse = 160;
    private boolean craftInvChange = true;
    
    public SteamCompressor()
    {
        netData = new TileEntityData(2, 2, 0, 1);
        inventory = new Inventory(this, 7, new Component(0, 1, -1), new Component(1, 2, -1), new Component(2, 3, -1), new Component(3, 4, -1), new Component(4, 5, 1));
        tanks = new TankContainer(this, new Tank(Config.tankCap[1], -1, Objects.L_steam).setIn(6)).setNetLong(1);
        
    }
    
    @Override
    public void update() 
    {
    	super.update();
        if(worldObj.isRemote) return;
        //steam netData.ints[0]
        int dp = 16 - netData.ints[0] / 40;
        if (tanks.getAmount(0) < 5 * dp) dp = tanks.getAmount(0) / 5;
        if (dp > 0)
        {
            tanks.drain(0, dp * 5, true);
            netData.ints[0] += dp;
        }
        //Item process
        craftInvChange |= checkInvChange();
        if (netData.ints[0] >= Euse && inventory.items[5] == null && craftInvChange)
        {
            CmpRecipe recipe = AutomationRecipes.getRecipeFor(inventory.items, 0, 4);
            if (recipe != null)
            {
                int n;
            	for (int i = 0; i < recipe.input.length; i++)
                	if ((n = AutomationRecipes.getStacksize(recipe.input[i])) > 0) this.decrStackSize(i, n);
                inventory.items[5] = recipe.output.copy();
            } else
            craftInvChange = false;
        }
        if (inventory.items[5] != null)
        {
            if (netData.ints[1] < Euse)
            {
                dp = 1 + netData.ints[0] * 7 / 320;
                if (dp > netData.ints[0]) dp = netData.ints[0];
                netData.ints[0] -= dp;
                netData.ints[1] += dp;
            }
            if (netData.ints[1] >= Euse)
            {
                inventory.items[5] = this.putItemStack(inventory.items[5], this, -1, 4);
                if (inventory.items[5] == null) netData.ints[1] -= Euse;
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
    
    public int getPressureScaled(int s)
    {
        return netData.ints[0] * s / 640;
    }
    
    public int getProgressScaled(int s)
    {
        return netData.ints[1] > Euse ? 160 : netData.ints[1] * s / Euse;
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) 
    {
        super.writeToNBT(nbt);
        nbt.setInteger("progress", netData.ints[1]);
        nbt.setInteger("pressure", netData.ints[0]);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) 
    {
        super.readFromNBT(nbt);
        netData.ints[1] = nbt.getInteger("progress");
        netData.ints[0] = nbt.getInteger("pressure");
        craftInvChange = true;
    }
    
    @Override
    public void initContainer(TileContainer container)
    {
        for (int j = 0; j < 2; j++)
        	for (int i = 0; i < 2; i++)
        		container.addEntitySlot(new Slot(this, i + 2 * j, 71 + 18 * i, 25 + 18 * j));
        container.addEntitySlot(new SlotOutput(this, 4, 143, 34));
        container.addEntitySlot(new SlotTank(this, 6, 17, 34));
        
        container.addPlayerInventory(8, 86);
        
        container.addTankSlot(new TankSlot(tanks, 0, 8, 16, true));
    }
    
    @Override
    public int[] stackTransferTarget(ItemStack item, int s, TileContainer container) 
    {
        int[] pi = container.getPlayerInv();
        if (s < pi[0]) return pi;
        else return new int[]{0, 4};
    }
    
}
