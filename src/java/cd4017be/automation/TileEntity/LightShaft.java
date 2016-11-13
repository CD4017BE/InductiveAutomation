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
public class LightShaft extends ModTileEntity implements ITickable {

	private int counter = 0;
	private int delay = 10;

	@Override
	public void onNeighborBlockChange(Block b) {
		if (worldObj.getBlockState(pos.down()) == Blocks.AIR.getDefaultState()) worldObj.setBlockState(pos.down(), BlockSkyLight.ID.getDefaultState());
	}

	@Override
	public void update() {
		if (++counter < delay) return;
		counter = 0;
		int l = EnumSkyBlock.SKY.defaultLightValue;
		worldObj.setLightFor(EnumSkyBlock.SKY, getPos(), l);
		BlockPos y = new BlockPos(pos);
		boolean inactive = true;
		while ((y = y.down()).getY() >= 0 && worldObj.isAirBlock(y)) 
			if (worldObj.getLightFor(EnumSkyBlock.SKY, y) < l) {
				worldObj.setLightFor(EnumSkyBlock.SKY, y, l);
				inactive = false;
			}
		if (inactive) delay++;
		else if (delay-- < 1) delay = 1;
	}

}
