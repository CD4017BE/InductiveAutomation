/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cd4017be.automation.TileEntity;


import java.util.ArrayList;

import cd4017be.automation.Config;
import cd4017be.automation.Objects;
import cd4017be.lib.TileContainer;
import cd4017be.lib.TileEntityData;
import cd4017be.lib.templates.AutomatedTile;
import cd4017be.lib.templates.Inventory;
import cd4017be.lib.templates.Inventory.Component;
import cd4017be.lib.templates.TankContainer;
import cd4017be.lib.templates.TankContainer.Tank;
import cd4017be.lib.util.Obj2;
import cd4017be.lib.util.Utils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;
import net.minecraftforge.fluids.IFluidHandler;

/**
 *
 * @author CD4017BE
 */
public class AntimatterTank extends AutomatedTile implements ISidedInventory, IFluidHandler
{
    public static final int MaxCap = 160000000;// =0.16g
    private int fillRate = 0;
    
    public AntimatterTank()
    {
        netData = new TileEntityData(2, 0, 0, 1);
    	tanks = new TankContainer(this, new Tank(Config.tankCap[4], 0, Objects.L_antimatter));
    	inventory = new Inventory(this, 2, new Component(0, 1, 0), new Component(1, 2, 0)).setNetLong(1);
    }
    
    @Override
    public void update() 
    {
    	super.update();
        if (worldObj.isRemote) return;
        boolean f = false;
        if (inventory.items[0] != null && inventory.items[0].getItem() instanceof IFluidContainerItem)
        {
            if (fillRate < 5) fillRate = 5;
            int am = tanks.getAmount(0);
            if (am - fillRate < 0) fillRate = am;
            Obj2<ItemStack, Integer> obj = Utils.fillFluid(inventory.items[0], new FluidStack(Objects.L_antimatter, fillRate));
            tanks.drain(0, obj.objB, true);
            inventory.items[0] = obj.objA;
            f = true;
        }
        if (inventory.items[1] != null && inventory.items[1].getItem() instanceof IFluidContainerItem)
        {
        	IFluidContainerItem fc = (IFluidContainerItem)inventory.items[1].getItem();
        	if (fillRate < 5) fillRate = 5;
            int am = tanks.getSpace(0);
            if (am - fillRate < 0) fillRate = am;
            FluidStack cont = fc.getFluid(inventory.items[1]);
            if (cont != null && cont.getFluid() == Objects.L_antimatter) {
            	Obj2<ItemStack, FluidStack> obj = Utils.drainFluid(inventory.items[1], fillRate);
            	tanks.fill(0, obj.objB, true);
            	inventory.items[1] = obj.objA;
                f = true;
            }
        }
        if (!f) fillRate = 0;
        else fillRate += 5;
    }
    
    @Override
    public void onPlaced(EntityLivingBase entity, ItemStack item)  
    {
        if (item != null && item.getItem() == Item.getItemFromBlock(this.getBlockType()));
        {
            if (item.stackTagCompound != null) tanks.setFluid(0, new FluidStack(Objects.L_antimatter, item.stackTagCompound.getInteger("antimatter")));
            else this.tanks.setFluid(0, new FluidStack(Objects.L_antimatter, 0));
        }
    }

    @Override
    public ArrayList<ItemStack> dropItem(IBlockState state, int fortune) 
    {
        ArrayList<ItemStack> list = new ArrayList<ItemStack>();
        ItemStack item = new ItemStack(this.getBlockType(), 1, 0);
        if (tanks.getAmount(0) >= 0) {
            item.stackTagCompound = new NBTTagCompound();
            item.stackTagCompound.setInteger("antimatter", tanks.getAmount(0));
        }
        list.add(item);
        return list;
    }
    
    public int getStorageScaled(int s)
    {
        return tanks.getAmount(0) / (tanks.tanks[0].cap / s);
    }
    
    @Override
    public void initContainer(TileContainer container) 
    {
        container.addEntitySlot(new Slot(this, 0, 152, 24));
        container.addEntitySlot(new Slot(this, 1, 152, 42));
        
        container.addPlayerInventory(8, 84);
    }
    
}
