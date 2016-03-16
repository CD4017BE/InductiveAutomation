/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cd4017be.automation.TileEntity;

import net.minecraft.network.PacketBuffer;

import java.io.IOException;

import cd4017be.automation.Config;
import cd4017be.automation.Objects;
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
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidHandler;

/**
 *
 * @author CD4017BE
 */
public class Collector extends AutomatedTile implements IFluidHandler, ISidedInventory
{
    private static final FluidStack[] Output = {null, new FluidStack(Objects.L_nitrogenG, 100), new FluidStack(Objects.L_oxygenG, 160)};
    
    public Collector()
    {
        netData = new TileEntityData(2, 3, 0, 1);
        inventory = new Inventory(this, 1);
        tanks = new TankContainer(this, new Tank(Config.tankCap[1], 1).setOut(0)).setNetLong(1);
    }

    @Override
    public void update() 
    {
    	super.update();
        if (worldObj.isRemote) return;
        EnumFacing dir = EnumFacing.VALUES[this.getOrientation()];
        BlockPos pos = this.pos.offset(dir);
        if (netData.ints[0] == 0) {
            FluidStack fluid = Utils.getFluid(worldObj, pos, true);
            if (fluid != null && tanks.fill(0, fluid, false) == fluid.amount) {
                tanks.fill(0, fluid, true);
                worldObj.setBlockToAir(pos);
            }
        } else if (worldObj.isAirBlock(pos)) {
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
    protected void customPlayerCommand(byte cmd, PacketBuffer dis, EntityPlayerMP player) throws IOException 
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
