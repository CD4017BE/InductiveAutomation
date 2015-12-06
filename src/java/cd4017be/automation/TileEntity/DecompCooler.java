/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cd4017be.automation.TileEntity;

import java.io.DataInputStream;
import java.io.IOException;

import cd4017be.api.automation.AutomationRecipes;
import cd4017be.api.automation.AutomationRecipes.CoolRecipe;
import cd4017be.api.automation.IEnergy;
import cd4017be.api.automation.PipeEnergy;
import cd4017be.automation.Config;
import cd4017be.lib.TileContainer;
import cd4017be.lib.TileEntityData;
import cd4017be.lib.templates.AutomatedTile;
import cd4017be.lib.templates.Inventory;
import cd4017be.lib.templates.Inventory.Component;
import cd4017be.lib.templates.TankContainer;
import cd4017be.lib.templates.TankContainer.Tank;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.IFluidHandler;

/**
 *
 * @author CD4017BE
 */
public class DecompCooler extends AutomatedTile implements IEnergy, IFluidHandler, ISidedInventory
{
    private float powerScale; 
    private CoolRecipe recipe;
    
    public DecompCooler()
    {
        netData = new TileEntityData(2, 1, 3, 4);
        inventory = new Inventory(this, 4, new Component(0, 1, -1), new Component(1, 2, -1), new Component(2, 3, 1), new Component(3, 4, 1)).setInvName("Decompression Freezer");
        energy = new PipeEnergy(Config.Umax[1], Config.Rcond[1]);
        tanks = new TankContainer(this, new Tank(Config.tankCap[1], -1).setIn(0), new Tank(Config.tankCap[1], -1).setIn(1), new Tank(Config.tankCap[1], 1).setOut(2), new Tank(Config.tankCap[1], 1).setOut(3)).setNetLong(1);
       
        netData.ints[0] = Config.Rmin;
        powerScale = Config.Pscale;
    }

    @Override
    public void updateEntity() 
    {
        super.updateEntity();
        if (this.worldObj.isRemote) return;
        if (recipe == null) {
            recipe = AutomationRecipes.getRecipeFor(inventory.items, 0, tanks.getFluid(0), tanks.getFluid(1));
            if (recipe != null) {
                recipe.useRes(inventory, 0, 1, tanks, 0, 1);
                netData.floats[1] = recipe.energy;
            }
        }
        netData.floats[2] = (float)energy.Ucap;
        double power = energy.getEnergy(0, netData.ints[0]) / 1000D;
        if (recipe != null) {
            if (netData.floats[0] < netData.floats[1]) {
                netData.floats[0] += power;
                energy.Ucap *= powerScale;
            }
            if (netData.floats[0] >= netData.floats[1] && recipe.output(inventory, 2, 3, tanks, 2, 3)) {
                recipe = null;
                netData.floats[0] -= netData.floats[1];
                netData.floats[1] = 0;
            }
        }
    }
    
    public int getProgressScaled(int s)
    {
        if (netData.floats[1] == 0) return 0;
        else if (netData.floats[0] > netData.floats[1]) return s;
        else return (int)(netData.floats[0] / netData.floats[1] * (float)s);
    }
    
    public int getPowerScaled(int s)
    {
        return (int)(s * netData.floats[2] * netData.floats[2] / (float)netData.ints[0]) / 200000;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) 
    {
        super.readFromNBT(nbt);
        if (nbt.hasKey("recipe")) recipe = AutomationRecipes.readCoolRecipeFromNBT(nbt.getCompoundTag("recipe"));
        else recipe = null;
        netData.floats[0] = nbt.getFloat("progress");
        netData.ints[0] = nbt.getInteger("resistor");
        powerScale = nbt.getFloat("pScale");
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) 
    {
        super.writeToNBT(nbt);
        if (recipe != null) nbt.setTag("recipe", AutomationRecipes.writeCoolRecipeToNBT(recipe));
        nbt.setFloat("progress", netData.floats[0]);
        nbt.setInteger("resistor", netData.ints[0]);
        nbt.setFloat("pScale", powerScale);
    }

    @Override
    protected void customPlayerCommand(byte cmd, DataInputStream dis, EntityPlayerMP player) throws IOException 
    {
        if (cmd == 0)
        {
            netData.ints[0] = dis.readInt();
            if (netData.ints[0] < Config.Rmin) netData.ints[0] = Config.Rmin;
            powerScale = (float)Math.sqrt(1.0D - 1.0D / (double)netData.ints[0]);
        }
    }

    @Override
    public void initContainer(TileContainer container) 
    {
        container.addEntitySlot(new Slot(this, 0, 44, 34));
        container.addEntitySlot(new Slot(this, 1, 116, 34));
        container.addEntitySlot(new Slot(this, 2, 80, 34));
        container.addEntitySlot(new Slot(this, 3, 152, 34));
        
        container.addPlayerInventory(8, 86);
    }
    
}
