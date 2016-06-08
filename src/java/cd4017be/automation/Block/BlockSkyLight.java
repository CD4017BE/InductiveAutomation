/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cd4017be.automation.Block;

import java.util.Random;

import cd4017be.automation.Objects;
import cd4017be.lib.DefaultBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/**
 *
 * @author CD4017BE
 */
public class BlockSkyLight extends DefaultBlock
{
    
    public static Block ID;
    
    public BlockSkyLight(String id)
    {
        super(id, Material.AIR, null);
        ID = this;
        this.setLightOpacity(0);
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) 
    {
        return false;
    }

    @Override
    public void onBlockAdded(World world, BlockPos pos, IBlockState state) 
    {
        this.neighborChanged(state, world, pos, this);
    }

    @Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.INVISIBLE;
	}

	@Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block b) 
    {
        Block id = world.getBlockState(pos.up()).getBlock();
        if (id == this || id == Objects.lightShaft) {
            if (world.getBlockState(pos.down()) == Blocks.AIR.getDefaultState()) {
                world.setBlockState(pos.down(), this.getDefaultState());
            }
        } else world.setBlockToAir(pos);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        return null;
    }

    @Override
    public boolean isCollidable() {
        return false;
    }

    @Override
    public int quantityDropped(IBlockState meta, int fortune, Random random) {
        return 0;
    }
    
}
