package cd4017be.automation.TileEntity;

import cd4017be.automation.Config;

/**
 *
 * @author CD4017BE
 */
public class SteamTurbine extends SteamEngine {
	@Override
	protected int getUmax() {return Config.Ugenerator[2];}
	@Override
	public float getPower() {return Config.Pgenerator[2];}
}
