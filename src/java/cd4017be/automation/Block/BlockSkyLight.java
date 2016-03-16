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
import net.minecraft.item.ItemBlock;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
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
        super(id, Material.air, ItemBlock.class);
        ID = this;
        this.setLightOpacity(0);
    }

    @Override
    public boolean isOpaqueCube() 
    {
        return false;
    }

    @Override
    public void onBlockAdded(World world, BlockPos pos, IBlockState state) 
    {
        this.onNeighborBlockChange(world, pos, state, this);
    }

    @Override
	public int getRenderType() {
		return -1;
	}

	@Override
    public void onNeighborBlockChange(World world, BlockPos pos, IBlockState state, Block b) 
    {
        Block id = world.getBlockState(pos.up()).getBlock();
        if (id == this || id == Objects.lightShaft) {
            if (world.getBlockState(pos.down()) == Blocks.air.getDefaultState()) {
                world.setBlockState(pos.down(), this.getDefaultState());
            }
        } else world.setBlockToAir(pos);
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox(World world, BlockPos pos, IBlockState state) {
        return null;
    }

    @Override
    public AxisAlignedBB getSelectedBoundingBox(World world, BlockPos pos) {
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
