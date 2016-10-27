package cd4017be.automation.Block;

import java.util.List;

import cd4017be.automation.Automation;
import cd4017be.lib.DefaultBlock;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;

/**
 *
 * @author CD4017BE
 */
public class BlockOre extends DefaultBlock {

	public static enum Ore implements IStringSerializable {
		Silver("silver"), Copper("copper"), Aluminium("aluminium");
		public final String name;
		private Ore(String name) {
			this.name = name;
		}
		@Override
		public String getName() {
			return name;
		}
	}

	public static PropertyEnum<Ore> prop = PropertyEnum.create("ore", Ore.class);

	public BlockOre(String id) {
		super(id, Material.ROCK);
		this.setCreativeTab(Automation.tabAutomation);
	}

	@Override
	public void getSubBlocks(Item par1, CreativeTabs par2CreativeTabs, List<ItemStack> list) {
		list.add(new ItemStack(this, 1, Ore.Silver.ordinal()));
		list.add(new ItemStack(this, 1, Ore.Copper.ordinal()));
		list.add(new ItemStack(this, 1, Ore.Aluminium.ordinal()));
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.blockState.getBaseState().withProperty(prop, Ore.values()[meta]);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(prop).ordinal();
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, prop);
	}

	@Override
	public int damageDropped(IBlockState m) {
		return this.getMetaFromState(m);
	}

}
