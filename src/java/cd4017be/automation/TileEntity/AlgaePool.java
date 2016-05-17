/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cd4017be.automation.TileEntity;

import java.util.ArrayList;
import java.util.List;

import cd4017be.automation.Automation;
import cd4017be.automation.Config;
import cd4017be.automation.Objects;
import cd4017be.lib.BlockItemRegistry;
import cd4017be.lib.TileContainer;
import cd4017be.lib.TileContainer.TankSlot;
import cd4017be.lib.TileEntityData;
import cd4017be.lib.templates.AutomatedTile;
import cd4017be.lib.templates.Inventory;
import cd4017be.lib.templates.Inventory.Component;
import cd4017be.lib.templates.SlotTank;
import cd4017be.lib.templates.TankContainer;
import cd4017be.lib.templates.TankContainer.Tank;
import net.minecraft.init.Items;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.EnumSkyBlock;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidHandler;
import net.minecraft.inventory.ISidedInventory;

/**
 *
 * @author CD4017BE
 */
public class AlgaePool extends AutomatedTile implements ISidedInventory, IFluidHandler
{
    public static final short UpdateTime = 10;
    private float amountBiomass;
    private short updateCounter;
    private float midBrightness;
    
    public AlgaePool()
    {
        netData = new TileEntityData(2, 1, 2, 2);
        inventory = new Inventory(this, 5, new Component(4, 5, -1));
        tanks = new TankContainer(this, new Tank(Config.tankCap[1], -1, Objects.L_water).setIn(2), new Tank(Config.tankCap[1], 1, Objects.L_biomass).setOut(3)).setNetLong(1);
        
    }
    
    @Override
    public void update() 
    {
        super.update();
        if (worldObj.isRemote) return;
        updateCounter++;
        if (updateCounter >= UpdateTime)
        {
            updateCounter = 0;
            midBrightness = 0;
            List<BlockPos> list = new ArrayList<BlockPos>();
            updateMultiblock(pos, list);
            if (list.size() > 0) midBrightness /= list.size();
            netData.ints[0] = list.size() * Config.tankCap[1];
        }
        if (inventory.items[0] != null && inventory.items[0].isItemEqual(BlockItemRegistry.stack("LCAlgae", 1))) {
        	netData.floats[0] += inventory.items[0].stackSize * 1000;
        	inventory.items[0] = new ItemStack(Items.glass_bottle, inventory.items[0].stackSize);
        }
        if (inventory.items[1] != null && inventory.items[1].isItemEqual(new ItemStack(Items.glass_bottle)) && netData.floats[0] >= inventory.items[1].stackSize * 1000) {
        	netData.floats[0] -= inventory.items[1].stackSize * 1000;
        	inventory.items[1] = BlockItemRegistry.stack("LCAlgae", inventory.items[1].stackSize);
        }
        if (netData.floats[0] > 0) updateAlgaeGrowing();
        int[] n = Automation.proxy.getLnutrients(inventory.items[4]);
        if (n != null && netData.floats[1] + n[0] <= Config.tankCap[1] && tanks.getAmount(0) >= n[0])
        {
            tanks.drain(0, n[0], true);
            netData.floats[1] += n[0];
            netData.floats[0] += n[1];
            inventory.items[4].stackSize--;
            if (inventory.items[4].stackSize <= 0) inventory.items[4] = null;
        }
    }
    
    private void updateMultiblock(BlockPos p, List<BlockPos> list)
    {
        for (int i = 0; i < 6 && list.size() < 16; i++)
        {
            EnumFacing dir = EnumFacing.VALUES[i];
            BlockPos pos = p.offset(dir);
            if (worldObj.getBlockState(pos).getBlock() != Objects.pool) continue;
            boolean b = true;
            for (int j = 0; j < list.size(); j++)
            {
                if (list.get(j).equals(pos))
                {
                    b = false;
                    break;
                }
            }
            if (b)
            {
                list.add(pos);
                midBrightness += worldObj.getLightFor(EnumSkyBlock.SKY, pos.up());
                updateMultiblock(pos, list);
            }
        }
    }
    
    private void updateAlgaeGrowing()
    {
        if (netData.floats[0] > netData.ints[0])
        {
            amountBiomass += netData.floats[0] - netData.ints[0];
            netData.floats[0] = netData.ints[0];
        }
        float Rf = netData.floats[0] * sq(getBrightness()) * Config.algaeGrowing;
        float Rd = netData.floats[0] * sq(netData.floats[0] / (float)netData.ints[0] / 2F) * Config.algaeDecaying;
        netData.floats[0] -= Rd;
        amountBiomass += Rd;
        if (netData.floats[1] < Rf) Rf = netData.floats[1];
        netData.floats[0] += Rf;
        netData.floats[1] -= Rf;
        int b = (int)Math.floor(amountBiomass);
        tanks.fill(1, new FluidStack(Objects.L_biomass, b), true);
        amountBiomass -= b;
    }
    
    private float getBrightness()
    {
        float l = midBrightness - worldObj.getSkylightSubtracted();
        if (l < 0) l = 0;
        return l * (1F - netData.floats[0] / (float)netData.ints[0] / 4F) / 15F;
    }
    
    private float sq(float v)
    {
        return v * v;
    }
    
    public int getAlgaeScaled(int s)
    {
        return netData.ints[0] == 0 ? 0 : (int)netData.floats[0] * s / netData.ints[0];
    }
    
    public int getNutrientsScaled(int s)
    {
        return (int)netData.floats[1] * s / Config.tankCap[1];
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) 
    {
        super.readFromNBT(nbt);
        netData.floats[1] = nbt.getFloat("nutrients");
        netData.floats[0] = nbt.getFloat("algae");
        amountBiomass = nbt.getFloat("ofBiomass");
        updateCounter = UpdateTime;
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) 
    {
        super.writeToNBT(nbt);
        nbt.setFloat("nutrients", netData.floats[1]);
        nbt.setFloat("algae", netData.floats[0]);
        nbt.setFloat("ofBiomass", amountBiomass);
    }
    
    @Override
    public void initContainer(TileContainer container) 
    {
        container.addEntitySlot(new Slot(this, 0, 80, 34));
        container.addEntitySlot(new Slot(this, 1, 98, 34));
        container.addEntitySlot(new SlotTank(this, 2, 17, 34));
        container.addEntitySlot(new SlotTank(this, 3, 143, 34));
        container.addEntitySlot(new Slot(this, 4, 44, 34));
        
        container.addPlayerInventory(8, 86);
        
        container.addTankSlot(new TankSlot(tanks, 0, 8, 16, true));
        container.addTankSlot(new TankSlot(tanks, 1, 134, 16, true));
    }

    @Override
    public int[] stackTransferTarget(ItemStack item, int s, TileContainer container) 
    {
        int[] inv = container.getPlayerInv();
        if (s < inv[0]) return inv;
        else return new int[]{4, 5};
    }
    
}
