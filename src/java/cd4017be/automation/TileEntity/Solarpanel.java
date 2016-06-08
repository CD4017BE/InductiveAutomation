/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cd4017be.automation.TileEntity;

import net.minecraft.util.ITickable;

import cd4017be.api.automation.IEnergy;
import cd4017be.api.automation.PipeEnergy;
import cd4017be.automation.Config;
import cd4017be.lib.ModTileEntity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.EnumSkyBlock;

/**
 *
 * @author CD4017BE
 */
public class Solarpanel extends ModTileEntity implements IEnergy, ITickable
{
    protected PipeEnergy energy = new PipeEnergy(this.getUmax(), this.getRcond());
    
    protected float getRcond() {return Config.Rcond[0];}
    protected int getUmax() {return Config.Umax[0] / 2;}
    
    @Override
    public void update() 
    {
        if (this.worldObj.isRemote) return;
        int sl = this.worldObj.getLightFor(EnumSkyBlock.SKY, pos.up());
        int bl = this.worldObj.getLightFor(EnumSkyBlock.BLOCK, pos.up());
        sl -= this.worldObj.getSkylightSubtracted();
        if (sl < 0) sl = 0;
        float power = (float)(sl * sl * sl + bl) / 3.375F; //Skylight = 15 -> power = 1
        energy.addEnergy(power * Config.Psolar[0]);
        energy.update(this);
    }

    @Override
    public PipeEnergy getEnergy(byte side) 
    {
        return energy;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) 
    {
        super.readFromNBT(nbt);
        energy.readFromNBT(nbt, "wire");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) 
    {
        energy.writeToNBT(nbt, "wire");
        return super.writeToNBT(nbt);
    }
    
}
