/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cd4017be.automation.TileEntity;

import cd4017be.automation.Block.BlockSkyLight;
import cd4017be.lib.ModTileEntity;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.EnumSkyBlock;

/**
 *
 * @author CD4017BE
 */
public class LightShaft extends ModTileEntity
{

    private int counter = 0;
    
    @Override
    public void onNeighborBlockChange(Block b) 
    {
        if (worldObj.getBlock(xCoord, yCoord-1, zCoord) == Blocks.air) worldObj.setBlock(xCoord, yCoord-1, zCoord, BlockSkyLight.ID);
    }

    @Override
    public void updateEntity() 
    {
        if (counter == 0) this.onNeighborBlockChange(Blocks.air);
        if (++counter > 20){
            counter = 1;
            worldObj.setLightValue(EnumSkyBlock.Sky, xCoord, yCoord, zCoord, EnumSkyBlock.Sky.defaultLightValue);
            int y = yCoord;
            while (worldObj.getBlock(xCoord, --y, zCoord) == BlockSkyLight.ID) {
                worldObj.setLightValue(EnumSkyBlock.Sky, xCoord, y, zCoord, EnumSkyBlock.Sky.defaultLightValue);
            }
        }
    }
    
}
