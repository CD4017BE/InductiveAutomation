/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cd4017be.automation.TileEntity;


import java.util.ArrayList;

import cd4017be.automation.Config;
import cd4017be.lib.TileContainer;
import cd4017be.lib.TileContainer.TankSlot;
import cd4017be.lib.TileEntityData;
import cd4017be.lib.templates.AutomatedTile;
import cd4017be.lib.templates.Inventory;
import cd4017be.lib.templates.Inventory.Component;
import cd4017be.lib.templates.SlotTank;
import cd4017be.lib.templates.TankContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidHandler;

/**
 *
 * @author CD4017BE
 */
public class Tank extends AutomatedTile implements IFluidHandler, ISidedInventory
{
    private int lastAmount;
    
    public Tank()
    {
        netData = new TileEntityData(2, 0, 0, 1);
        inventory = new Inventory(this, 2, new Component(0, 1, 0), new Component(1, 2, 0));
        tanks = new TankContainer(this, new TankContainer.Tank(this.capacity(), 1).setIn(0).setOut(1)).setNetLong(1);
    }

    protected int capacity() {return Config.tankCap[2];}
    
    @Override
    public void update() 
    {
    	super.update();
        if (worldObj.isRemote) return;
        int d = tanks.getAmount(0) - lastAmount;
        if ((d != 0 && (tanks.getAmount(0) == 0 || lastAmount == 0)) || d * d > 250000){
            lastAmount = tanks.getAmount(0);
            worldObj.markBlockForUpdate(getPos());
        } 
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) 
    {
        tanks.readFromNBT(pkt.getNbtCompound(), "tank");
    }

    @Override
    public Packet getDescriptionPacket() 
    {
        NBTTagCompound nbt = new NBTTagCompound();
        tanks.writeToNBT(nbt, "tank");
        return new S35PacketUpdateTileEntity(getPos(), -1, nbt);
    }
    
    @Override
    public void initContainer(TileContainer container) 
    {
        container.addEntitySlot(new SlotTank(this, 0, 184, 74));
        container.addEntitySlot(new SlotTank(this, 1, 202, 74));
        
        container.addPlayerInventory(8, 16);
        
        container.addTankSlot(new TankSlot(tanks, 0, 184, 16, true));
    }

    @Override
    public void onPlaced(EntityLivingBase entity, ItemStack item) 
    {
        tanks.setFluid(0, FluidStack.loadFluidStackFromNBT(item.getTagCompound()));
    }

    @Override
    public ArrayList<ItemStack> dropItem(IBlockState state, int fortune) 
    {
        ItemStack item = new ItemStack(this.getBlockType());
        if (tanks.getAmount(0) > 0) {
            item.setTagCompound(new NBTTagCompound());
            tanks.getFluid(0).writeToNBT(item.getTagCompound());
        }
        ArrayList<ItemStack> list = new ArrayList<ItemStack>();
        list.add(item);
        return list;
    }
    
}
