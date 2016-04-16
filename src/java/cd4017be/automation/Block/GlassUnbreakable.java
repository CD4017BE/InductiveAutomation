/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cd4017be.automation.Block;

import cd4017be.api.automation.AreaProtect;
import cd4017be.api.automation.ProtectLvl;
import cd4017be.automation.Automation;
import cd4017be.lib.DefaultBlock;
import cd4017be.lib.DefaultItemBlock;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 *
 * @author CD4017BE
 */
public class GlassUnbreakable extends DefaultBlock
{
    
    public GlassUnbreakable(String id)
    {
        super(id, Material.glass, DefaultItemBlock.class);
        this.setCreativeTab(Automation.tabAutomation);
        this.setBlockUnbreakable();
        this.setResistance(Float.POSITIVE_INFINITY);
    }

    @Override
    public boolean isOpaqueCube() 
    {
        return false;
    }

    @Override
    public boolean isBlockSolid(IBlockAccess world, BlockPos pos, EnumFacing side) {
        return true;
    }
    
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getBlockLayer()
    {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
	public boolean shouldSideBeRendered(IBlockAccess world, BlockPos pos, EnumFacing side) {
    	if (world.getBlockState(pos) == this) return false;
        else return super.shouldSideBeRendered(world, pos, side);
	}

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing s, float X, float Y, float Z) 
    {
        ProtectLvl pr = AreaProtect.instance.getPlayerAccess(player.getName(), world, pos.getX() >> 4, pos.getZ() >> 4);
        if (pr == ProtectLvl.Free && !world.isRemote && player.isSneaking()) {
            dropBlockAsItem(world, pos, state, 0);
            world.setBlockToAir(pos);
        }
        return player.isSneaking();
    }
    
    @Override
	public boolean canEntityDestroy(IBlockAccess world, BlockPos pos, Entity entity) 
	{
		return false;
	}

	@Override
	public float getExplosionResistance(Entity exploder) {
		return Float.POSITIVE_INFINITY;
	}

	@Override
	public float getExplosionResistance(World world, BlockPos pos, Entity exploder, Explosion explosion) {
		return Float.POSITIVE_INFINITY;
	}
    
}
