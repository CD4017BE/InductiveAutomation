package cd4017be.automation.Block;

import java.util.List;

import cd4017be.api.automation.AreaProtect;
import cd4017be.api.automation.ProtectLvl;
import cd4017be.automation.Automation;
import cd4017be.lib.DefaultBlock;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityWitherSkull;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/**
 *
 * @author CD4017BE
 */
public class BlockUnbreakable extends DefaultBlock {

	public BlockUnbreakable(String id) {
		super(id, Material.ROCK);
		this.setCreativeTab(Automation.tabAutomation);
		this.setBlockUnbreakable();
		this.setResistance(Float.POSITIVE_INFINITY);
	}

	private static final PropertyInteger prop = PropertyInteger.create("type", 0, 15);

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.blockState.getBaseState().withProperty(prop, meta);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(prop);
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, prop);
	}

	@Override
	public int damageDropped(IBlockState m) {
		return this.getMetaFromState(m);
	}

	@Override
	public void getSubBlocks(Item id, CreativeTabs par2CreativeTabs, List<ItemStack> list) {
		for (int i = 0; i < 16; i++) list.add(new ItemStack(id, 1, i));
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, ItemStack item, EnumFacing s, float X, float Y, float Z) {
		ProtectLvl pr = AreaProtect.playerAccess(player.getGameProfile(), world, pos.getX() >> 4, pos.getZ() >> 4);
		if (pr == ProtectLvl.Free && !world.isRemote && player.isSneaking()) {
			dropBlockAsItem(world, pos, state, 0);
			world.setBlockToAir(pos);
		}
		return player.isSneaking();
	}

	@Override
	public boolean canEntityDestroy(IBlockState state, IBlockAccess world, BlockPos pos, Entity entity) {
		//I know this is bad programming practice but...
		//YOU SHALL NOT PASS!
		repellEntity(pos, entity);
		return false;
	}

	@Override
	public float getExplosionResistance(Entity exploder) {
		//prevent WitherSkulls from ignoring explosion resistance
		if (exploder != null && exploder instanceof EntityWitherSkull) ((EntityWitherSkull)exploder).setInvulnerable(false);
		return Float.POSITIVE_INFINITY;
	}

	@Override
	public float getExplosionResistance(World world, BlockPos pos, Entity exploder, Explosion explosion) {
		//prevent WitherSkulls from ignoring explosion resistance
		if (exploder != null && exploder instanceof EntityWitherSkull) ((EntityWitherSkull)exploder).setInvulnerable(false);
		return Float.POSITIVE_INFINITY;
	}

	private static final double repellStr = 0.125;
	public static void repellEntity(BlockPos pos, Entity entity) {
		double x = pos.getX(), y = pos.getY(), z = pos.getZ();
		AxisAlignedBB box = entity.getEntityBoundingBox();
		if (box != null && box.intersects(x, y, z, x + 1.0, y + 1.0, z + 1.0)) {
			double w = entity.width / 2, h = entity.height / 2;
			double dx = entity.posX - (double)pos.getX() - 0.5D,
				dy = (box.maxY + box.minY) * 0.5D - (double)pos.getY() - 0.5D,
				dz = entity.posZ - (double)pos.getZ() - 0.5D;
			double d = dx*dx/w/w + dy*dy/h/h + dz*dz/w/w + 1.0;
			d /= repellStr;
			entity.motionX = MathHelper.clamp_double(dx / d + entity.motionX, -1, 1);
			entity.motionX = MathHelper.clamp_double(dx / d + entity.motionX, -1, 1);
			entity.motionX = MathHelper.clamp_double(dx / d + entity.motionX, -1, 1);
			entity.isAirBorne = true;
		}
	}
}
