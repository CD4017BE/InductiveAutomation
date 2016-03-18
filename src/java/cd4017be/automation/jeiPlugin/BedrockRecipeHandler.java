/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cd4017be.automation.jeiPlugin;

import cd4017be.automation.Entity.EntityAntimatterExplosion1;
import net.minecraft.init.Blocks;

/**
 *
 * @author CD4017BE
 */
public class BedrockRecipeHandler
{
    public static final float neededAm;
    static {
        float i = (2 * EntityAntimatterExplosion1.Density + 1) / EntityAntimatterExplosion1.Density;
        neededAm = Blocks.bedrock.getExplosionResistance(null) / EntityAntimatterExplosion1.PowerFactor / EntityAntimatterExplosion1.explMult * i * i * 9.375F;
    }
    
}
