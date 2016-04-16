/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cd4017be.automation.TileEntity;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.ITickable;
import cd4017be.automation.Block.BlockSkyLight;
import cd4017be.lib.ModTileEntity;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.EnumSkyBlock;

/**
 *
 * @author CD4017BE
 */
public class LightShaft extends ModTileEntity implements ITickable
{

    private int counter = 0;
    
    @Override
    public void onNeighborBlockChange(Block b) 
    {
        if (worldObj.getBlockState(pos.down()) == Blocks.air.getDefaultState()) worldObj.setBlockState(pos.down(), BlockSkyLight.ID.getDefaultState());
    }

    @Override
    public void update() 
    {
        if (counter == 0) this.onNeighborBlockChange(Blocks.air);
        if (++counter > 20){
            counter = 1;
            worldObj.setLightFor(EnumSkyBlock.SKY, getPos(), EnumSkyBlock.SKY.defaultLightValue);
            BlockPos y = new BlockPos(pos);
            while (worldObj.getBlockState(y = y.down()) == BlockSkyLight.ID.getDefaultState()) {
                worldObj.setLightFor(EnumSkyBlock.SKY, y, EnumSkyBlock.SKY.defaultLightValue);
            }
        }
    }
    
}
