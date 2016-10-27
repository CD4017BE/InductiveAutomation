/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cd4017be.automation.jeiPlugin;

import cd4017be.automation.TileEntity.AntimatterBomb;
import net.minecraft.init.Blocks;

/**
 *
 * @author CD4017BE
 */
public class BedrockRecipeHandler
{
	public static final float neededAm;
	static {
		float i = (2 * AntimatterBomb.Density + 1) / AntimatterBomb.Density;
		neededAm = Blocks.BEDROCK.getExplosionResistance(null) / AntimatterBomb.PowerFactor / AntimatterBomb.explMult * i * i * 9.375F;
	}
	
}
