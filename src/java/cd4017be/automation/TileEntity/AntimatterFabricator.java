/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cd4017be.automation.TileEntity;

import net.minecraft.network.PacketBuffer;

import java.io.IOException;

import cd4017be.api.automation.IEnergy;
import cd4017be.api.automation.PipeEnergy;
import cd4017be.automation.Config;
import cd4017be.automation.Objects;
import cd4017be.lib.Gui.SlotTank;
import cd4017be.lib.Gui.TileContainer;
import cd4017be.lib.Gui.TileContainer.TankSlot;
import cd4017be.lib.TileEntityData;
import cd4017be.lib.templates.AutomatedTile;
import cd4017be.lib.templates.Inventory;
import cd4017be.lib.templates.TankContainer;
import cd4017be.lib.templates.TankContainer.Tank;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidHandler;

/**
 *
 * @author CD4017BE
 */
public class AntimatterFabricator extends AutomatedTile implements IFluidHandler, ISidedInventory, IEnergy
{
    public static final int AMEnergy = 14400000;
    public static final int AMamount = 160;
    public static final int AMHeat = 160;
    
    public AntimatterFabricator()
    {
        netData = new TileEntityData(1, 3, 2, 3);
        energy = new PipeEnergy(Config.Umax[2], Config.Rcond[2]);
        inventory = new Inventory(this, 3);
        tanks = new TankContainer(this, new Tank(Config.tankCap[1], -1, Objects.L_heliumL).setIn(0), new Tank(Config.tankCap[1], 1, Objects.L_heliumG).setOut(1), new Tank(Config.tankCap[1], 1, Objects.L_antimatter).setOut(2));
        /**
         * long: tcfg
         * int: voltage, HeBuff, rs
         * float: storage, power
         * fluid: in, out, am
         */
    }
    
    @Override
    public void update() 
    {
    	super.update();
        if (this.worldObj.isRemote) return;
        //cooling
        if (netData.ints[1] < AMHeat && tanks.getAmount(0) > 0) {
            netData.ints[1] += 800;
            tanks.drain(0, 1, true);
        }
        //energy
        if (netData.floats[0] < AMEnergy && ((netData.ints[2] & 1) != 0 ^ ((netData.ints[2] & 2) != 0 && worldObj.isBlockPowered(pos)))) {
            double e = energy.getEnergy(netData.ints[0], 1);
            if (e < 0) e = 0;
            netData.floats[1] = (float)e / 1000F;
            netData.floats[0] += e;
            energy.Ucap = netData.ints[0];
        } else netData.floats[1] = 0;
        //antimatter
        if (netData.floats[0] >= AMEnergy && netData.ints[1] >= AMHeat && tanks.getSpace(2) >= AMamount) {
            netData.floats[0] -= AMEnergy;
            netData.ints[1] -= AMHeat;
            tanks.fill(1, new FluidStack(Objects.L_heliumG, AMHeat), true);
            tanks.fill(2, new FluidStack(Objects.L_antimatter, AMamount), true);
        }
    }
    
    public int getPowerScaled(int s)
    {
        return (int)(netData.floats[0] / AMEnergy * s);
    }

    @Override
    protected void customPlayerCommand(byte cmd, PacketBuffer dis, EntityPlayerMP player) throws IOException 
    {
        if (cmd == 0) {
            netData.ints[0] = dis.readInt();
        } else if (cmd == 1) {
            netData.ints[2] = dis.readByte();
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) 
    {
        nbt.setInteger("voltage", netData.ints[0]);
        nbt.setInteger("drop", netData.ints[1]);
        nbt.setByte("mode", (byte)netData.ints[2]);
        nbt.setFloat("storage", netData.floats[0]);
        return super.writeToNBT(nbt);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) 
    {
        super.readFromNBT(nbt);
        netData.ints[0] = nbt.getInteger("voltage");
        netData.ints[1] = nbt.getInteger("drop");
        netData.ints[2] = nbt.getByte("mode");
        netData.floats[0] = nbt.getFloat("storage");
    }
    
    @Override
    public void initContainer(TileContainer container)
    {
        container.addItemSlot(new SlotTank(this, 0, 8, 34));
        container.addItemSlot(new SlotTank(this, 1, 26, 34));
        container.addItemSlot(new SlotTank(this, 2, 143, 34));
        
        container.addPlayerInventory(8, 86);
        
        container.addTankSlot(new TankSlot(tanks, 0, 8, 16, false));
        container.addTankSlot(new TankSlot(tanks, 1, 26, 16, false));
        container.addTankSlot(new TankSlot(tanks, 2, 134, 16, true));
    }
    
}
