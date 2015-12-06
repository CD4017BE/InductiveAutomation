/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cd4017be.automation.Item;

import cd4017be.api.automation.EnergyItemHandler;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

/**
 *
 * @author CD4017BE
 */
public class ItemEnergyTool extends ItemEnergyCell
{
    private final int energyUsage;
    private final int damageVsEntity;
    private final float digSpeed;
    
    public ItemEnergyTool(String id, String tex, int es, int eu, float speed, int ed)
    {
        super(id, tex, es);
        this.energyUsage = eu;
        this.digSpeed = speed;
        this.damageVsEntity = ed;
        this.setMaxStackSize(1);
    }
    
    public ItemEnergyTool setToolClass(String[] toolClass, int harvestLevel)
    {
        for (String clas : toolClass) this.setHarvestLevel(clas, harvestLevel);
        return this;
    }
    
    @Override
    public boolean canHarvestBlock(Block block, ItemStack item) 
    {
    	if (EnergyItemHandler.getEnergy(item) < this.energyUsage) return false;
    	if (block.getMaterial().isToolNotRequired()) return true;
        String tool = block.getHarvestTool(0);
        int hl = block.getHarvestLevel(0);
        if (tool == null) return true;
    	if (hl > this.getHarvestLevel(item, tool)) return false;
    	else if (hl >= 0) return true;
    	if ("pickaxe".equals(tool) && (block.getMaterial() == Material.rock || block.getMaterial() == Material.iron)) return true;
        else if ("axe".equals(tool) && (block.getMaterial() == Material.wood || block.getMaterial() == Material.leaves)) return true;
        else if ("shovel".equals(tool) && (block.getMaterial() == Material.clay || block.getMaterial() == Material.sand || block.getMaterial() == Material.snow || block.getMaterial() == Material.craftedSnow)) return true;
        else return false;
    }
    
    @Override
    public boolean onBlockDestroyed(ItemStack item, World world, Block b, int x, int y, int z, EntityLivingBase entityLiving) 
    {
        EnergyItemHandler.addEnergy(item, -this.energyUsage, false);
        return true;
    }

    @Override
    public float getDigSpeed(ItemStack item, Block block, int m) 
    {
    	float str = this.canHarvestBlock(block, item) ? this.digSpeed : 1F;
        return str;
    }
    
    @Override
    public boolean hitEntity(ItemStack item, EntityLivingBase entityLivingHit, EntityLivingBase par3EntityLiving) 
    {
        if (EnergyItemHandler.getEnergy(item) >= this.energyUsage)
        {
            entityLivingHit.attackEntityFrom(DamageSource.causeMobDamage(par3EntityLiving), this.damageVsEntity);
            EnergyItemHandler.addEnergy(item, -this.energyUsage, false);
        }
        return true;
    }
    
}
