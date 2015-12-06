/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cd4017be.automation.TileEntity;

import java.io.DataInputStream;
import java.io.IOException;

import cd4017be.automation.Automation;
import cd4017be.automation.Config;
import cd4017be.lib.TileContainer;
import cd4017be.lib.TileEntityData;
import cd4017be.lib.templates.AutomatedTile;
import cd4017be.lib.templates.Inventory;
import cd4017be.lib.templates.SlotTank;
import cd4017be.lib.templates.TankContainer;
import cd4017be.lib.templates.TankContainer.Tank;
import cd4017be.lib.util.Utils;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidHandler;

/**
 *
 * @author CD4017BE
 */
public class Collector extends AutomatedTile implements IFluidHandler, ISidedInventory
{
    private static final FluidStack[] Output = {null, new FluidStack(Automation.L_nitrogenG, 100), new FluidStack(Automation.L_oxygenG, 160)};
    
    public Collector()
    {
        netData = new TileEntityData(2, 3, 0, 1);
        inventory = new Inventory(this, 1).setInvName("Collector");
        tanks = new TankContainer(this, new Tank(Config.tankCap[1], 1).setOut(0)).setNetLong(1);
    }

    @Override
    public void updateEntity() 
    {
        super.updateEntity();
        if (worldObj.isRemote) return;
        ForgeDirection dir = ForgeDirection.getOrientation(worldObj.getBlockMetadata(xCoord, yCoord, zCoord));
        int x1 = xCoord + dir.offsetX, y1 = yCoord + dir.offsetY, z1 = zCoord + dir.offsetZ;
        if (netData.ints[0] == 0) {
            FluidStack fluid = Utils.getFluid(worldObj, x1, y1, z1, true);
            if (fluid != null && tanks.fill(0, fluid, false) == fluid.amount) {
                tanks.fill(0, fluid, true);
                worldObj.setBlockToAir(x1, y1, z1);
            }
        } else if (worldObj.isAirBlock(x1, y1, z1)) {
            FluidStack fluid = Output[netData.ints[0]];
            tanks.fill(0, fluid, true);
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) 
    {
        super.writeToNBT(nbt);
        nbt.setInteger("type", netData.ints[0]);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) 
    {
        super.readFromNBT(nbt);
        netData.ints[0] = nbt.getInteger("type");
    }
    
    @Override
    protected void customPlayerCommand(byte cmd, DataInputStream dis, EntityPlayerMP player) throws IOException 
    {
        if (cmd == 0) netData.ints[0] = (netData.ints[0] + 1) % Output.length;
    }

    @Override
    public void initContainer(TileContainer container) 
    {
        container.addEntitySlot(new SlotTank(this, 0, 202, 74));
        container.addPlayerInventory(8, 16);
    }
    
}
