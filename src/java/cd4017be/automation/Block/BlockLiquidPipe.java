/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cd4017be.automation.Block;

import java.util.ArrayList;
import java.util.List;

import cd4017be.automation.Item.ItemLiquidPipe;
import cd4017be.lib.templates.BlockPipe;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 *
 * @author CD4017BE
 */
public class BlockLiquidPipe extends BlockPipe
{
    
    public static final byte ID_Transport = 0;
    public static final byte ID_Extraction = 2;
    public static final byte ID_Injection = 1;
    
    public BlockLiquidPipe(String id, SoundType sound, Material m, int type)
    {
        super(id, m, sound, ItemLiquidPipe.class, type);
    }
    
    @Override
    public void getSubBlocks(Item par1, CreativeTabs par2CreativeTabs, List<ItemStack> list) 
    {
        list.add(new ItemStack(this, 1, ID_Transport));
        list.add(new ItemStack(this, 1, ID_Extraction));
        list.add(new ItemStack(this, 1, ID_Injection));
    }

    private static final PropertyInteger prop = PropertyInteger.create("type", 0, 2);
    
    @Override
	public IBlockState getStateFromMeta(int meta) {
		return this.blockState.getBaseState().withProperty(prop, meta);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(prop);
	}

	@Override
	protected void addProperties(ArrayList<IProperty> main) {
		main.add(prop);
	}
    
    @Override
    public int damageDropped(IBlockState m) 
    {
        return this.getMetaFromState(m);
    }
    
}
