package cd4017be.automation.TileEntity;

import cd4017be.automation.Config;
import cd4017be.lib.BlockItemRegistry;

public class TeslaTransmitterLV extends TeslaTransmitter
{

	@Override
	protected int getMaxVoltage() 
	{
		return Config.Umax[3];
	}
	
	public boolean checkAlive()
    {
		return !this.isInvalid() && worldObj.blockExists(xCoord, yCoord, zCoord) && worldObj.getTileEntity(xCoord, yCoord, zCoord) == this;
    }
	
}
