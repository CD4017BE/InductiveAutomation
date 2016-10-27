package cd4017be.automation.Item;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import cd4017be.automation.Block.BlockLiquidPipe;
import cd4017be.lib.BlockItemRegistry;
import cd4017be.lib.DefaultItemBlock;

	
/**
 *
 * @author CD4017BE
 */
public class ItemLiquidPipe extends DefaultItemBlock {

	public ItemLiquidPipe(Block id) {
		super(id);
		this.setHasSubtypes(true);
		BlockItemRegistry.registerItemStack(new ItemStack(this, 1, BlockLiquidPipe.ID_Transport), "liquidPipeT");
		BlockItemRegistry.registerItemStack(new ItemStack(this, 1, BlockLiquidPipe.ID_Extraction), "liquidPipeE");
		BlockItemRegistry.registerItemStack(new ItemStack(this, 1, BlockLiquidPipe.ID_Injection), "liquidPipeI");
	}

	@Override
	public int getMetadata(int dmg) {
		return dmg;
	}

}
