/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cd4017be.automation.TileEntity;

import cd4017be.automation.Config;

/**
 *
 * @author CD4017BE
 */
public class HugeTank extends Tank
{

    @Override
    protected int capacity()
    {
        return Config.tankCap[3];
    }
    
}
