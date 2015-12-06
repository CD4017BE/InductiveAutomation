/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cd4017be.automation.Block;

import java.util.Random;

import cd4017be.lib.BlockItemRegistry;
import cd4017be.lib.DefaultBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/**
 *
 * @author CD4017BE
 */
public class BlockSkyLight extends DefaultBlock
{
    
    public static Block ID;
    public static Block sID;
    
    public BlockSkyLight(String id)
    {
        super(id, Material.air, ItemBlock.class);
        ID = this;
        sID = BlockItemRegistry.blockId("tile.lightShaft");
        this.setLightOpacity(0);
    }

    @Override
    public boolean isOpaqueCube() 
    {
        return false;
    }

    @Override
    public void onBlockAdded(World world, int x, int y, int z) 
    {
        this.onNeighborBlockChange(world, x, y, z, this);
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block b) 
    {
        Block id = world.getBlock(x, y+1, z);
        if (id == this || id == sID) {
            if (world.getBlock(x, y-1, z) == Blocks.air) {
                world.setBlock(x, y-1, z, this);
            }
        } else world.setBlockToAir(x, y, z);
    }

    @Override
    public boolean shouldSideBeRendered(IBlockAccess par1IBlockAccess, int par2, int par3, int par4, int par5) 
    {
        return false;
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World par1World, int par2, int par3, int par4) 
    {
        return null;
    }

    @Override
    public AxisAlignedBB getSelectedBoundingBoxFromPool(World par1World, int par2, int par3, int par4) 
    {
        return null;
    }

    @Override
    public boolean isCollidable() 
    {
        return false;
    }

    @Override
    public int quantityDropped(int meta, int fortune, Random random) 
    {
        return 0;
    }
    
}
