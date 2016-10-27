package cd4017be.automation.TileEntity;

import cd4017be.automation.Config;

public class QuantumTank extends Tank {

	@Override
	protected int capacity()
	{
		return Config.tankCap[5];
	}
	
}
