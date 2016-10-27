package cd4017be.automation.TileEntity;

import cd4017be.automation.Config;

public class TeslaTransmitterLV extends TeslaTransmitter
{

	@Override
	protected int getMaxVoltage() 
	{
		return Config.Umax[3];
	}
	
	public boolean checkAlive()
	{
		return !this.isInvalid() && worldObj.isBlockLoaded(getPos()) && worldObj.getTileEntity(getPos()) == this;
	}
	
}
