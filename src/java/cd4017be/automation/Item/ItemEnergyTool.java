package cd4017be.automation.Item;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import cd4017be.api.energy.EnergyAutomation.EnergyItem;

/**
 *
 * @author CD4017BE
 */
public class ItemEnergyTool extends ItemEnergyCell {

	private final int energyUsage;
	private final int damageVsEntity;
	private final float digSpeed;

	public ItemEnergyTool(String id, int es, int eu, float speed, int ed) {
		super(id, es);
		this.energyUsage = eu;
		this.digSpeed = speed;
		this.damageVsEntity = ed;
		this.setMaxStackSize(1);
	}

	public ItemEnergyTool setToolClass(String[] toolClass, int harvestLevel) {
		for (String clas : toolClass) this.setHarvestLevel(clas, harvestLevel);
		return this;
	}

	@Override
	public boolean canHarvestBlock(IBlockState state, ItemStack item) {
		if (new EnergyItem(item, this, -1).getStorageI() < this.energyUsage) return false;
		if (state.getMaterial().isToolNotRequired()) return true;
		String tool = state.getBlock().getHarvestTool(state);
		int hl = state.getBlock().getHarvestLevel(state);
		if (tool == null) return true;
		if (hl > this.getHarvestLevel(item, tool)) return false;
		else if (hl >= 0) return true;
		if ("pickaxe".equals(tool) && (state.getMaterial() == Material.ROCK || state.getMaterial() == Material.IRON)) return true;
		else if ("axe".equals(tool) && (state.getMaterial() == Material.WOOD || state.getMaterial() == Material.LEAVES)) return true;
		else if ("shovel".equals(tool) && (state.getMaterial() == Material.CLAY || state.getMaterial() == Material.SAND || state.getMaterial() == Material.SNOW || state.getMaterial() == Material.CRAFTED_SNOW)) return true;
		else return false;
	}

	@Override
	public boolean onBlockDestroyed(ItemStack item, World world, IBlockState b, BlockPos pos, EntityLivingBase entityLiving) {
		new EnergyItem(item, this, -1).addEnergyI(-this.energyUsage);
		return true;
	}

	@Override
	public float getStrVsBlock(ItemStack item, IBlockState state) {
		float str = this.canHarvestBlock(state, item) ? this.digSpeed : 1F;
		return str;
	}

	@Override
	public boolean hitEntity(ItemStack item, EntityLivingBase entityLivingHit, EntityLivingBase par3EntityLiving) {
		EnergyItem energy = new EnergyItem(item, this, -1);
		if (energy.getStorageI() >= this.energyUsage) {
			entityLivingHit.attackEntityFrom(DamageSource.causeMobDamage(par3EntityLiving), this.damageVsEntity);
			energy.addEnergyI(-this.energyUsage);
		}
		return true;
	}

}
