/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cd4017be.automation.TileEntity;

import cd4017be.automation.Config;
import net.minecraft.world.EnumSkyBlock;

/**
 *
 * @author CD4017BE
 */
public class HPSolarpanel extends Solarpanel
{
    
    @Override
	protected float getRcond() {return Config.Rcond[1];}

	@Override
	protected int getUmax() {return Config.Umax[0];}

	@Override
    public void updateEntity() 
    {
        if (this.worldObj.isRemote) return;
        int sl = this.worldObj.getSavedLightValue(EnumSkyBlock.Sky, xCoord, yCoord + 1, zCoord);
        int bl = 45;
        sl -= this.worldObj.skylightSubtracted;
        if (sl < 0) sl = 0;
        if (worldObj.provider.getDimensionName().equals("The End")) sl = 5;
        float power = (float)(sl * sl * sl + bl) / 3.375F; //Skylight = 15 -> power = 1
        energy.addEnergy(power * Config.Psolar[1]);
        energy.update(this);
    }
    
}
