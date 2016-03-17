/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cd4017be.automation.Block;

import java.util.List;

import cd4017be.api.automation.AreaProtect;
import cd4017be.api.automation.ProtectLvl;
import cd4017be.automation.Automation;
import cd4017be.automation.Item.ItemBlockUnbreakable;
import cd4017be.lib.DefaultBlock;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/**
 *
 * @author CD4017BE
 */
public class BlockUnbreakable extends DefaultBlock
{   
    
    public BlockUnbreakable(String id)
    {
        super(id, Material.rock, ItemBlockUnbreakable.class);
        this.setCreativeTab(Automation.tabAutomation);
        this.setBlockUnbreakable();
        this.setResistance(Float.POSITIVE_INFINITY);
    }

    private static final PropertyInteger prop = PropertyInteger.create("type", 0, 15);
    
    @Override
	public IBlockState getStateFromMeta(int meta) {
		return this.blockState.getBaseState().withProperty(prop, meta);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(prop);
	}
    
    @Override
	protected BlockState createBlockState() {
		return new BlockState(this, prop);
	}

	@Override
    public int damageDropped(IBlockState m) 
    {
        return this.getMetaFromState(m);
    }

    @Override
    public void getSubBlocks(Item id, CreativeTabs par2CreativeTabs, List<ItemStack> list) 
    {
        for (int i = 0; i < 16; i++) list.add(new ItemStack(id, 1, i));
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
    
}
