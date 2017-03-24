package cd4017be.automation.Block;

import cd4017be.api.automation.AreaProtect;
import cd4017be.api.automation.ProtectLvl;
import cd4017be.automation.Automation;
import cd4017be.lib.DefaultBlock;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityWitherSkull;
import net.minecraft.util.math.BlockPos;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumHand;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 *
 * @author CD4017BE
 */
public class GlassUnbreakable extends DefaultBlock {

	public GlassUnbreakable(String id) {
		super(id, Material.GLASS);
		this.setCreativeTab(Automation.tabAutomation);
		this.setBlockUnbreakable();
		this.setResistance(Float.POSITIVE_INFINITY);
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean isBlockSolid(IBlockAccess world, BlockPos pos, EnumFacing side) {
		return true;
	}

	@SideOnly(Side.CLIENT)
	public BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.CUTOUT;
	}

	@Override
	public boolean shouldSideBeRendered(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
		if (world.getBlockState(pos) == this) return false;
		else return super.shouldSideBeRendered(state, world, pos, side);
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
		BlockUnbreakable.repellEntity(pos, entity);
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

}
