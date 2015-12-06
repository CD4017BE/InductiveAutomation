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
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/**
 *
 * @author CD4017BE
 */
public class BlockUnbreakable extends DefaultBlock
{   
    
    public BlockUnbreakable(String id, int tex)
    {
        super(id, Material.rock, ItemBlockUnbreakable.class, "unbr/stone");
        this.setCreativeTab(Automation.tabAutomation);
        this.setBlockUnbreakable();
        this.setResistance(Float.POSITIVE_INFINITY);
    }

    @Override
    public int damageDropped(int m) 
    {
        return m;
    }

    @Override
    public void getSubBlocks(Item id, CreativeTabs par2CreativeTabs, List list) 
    {
        for (int i = 0; i < 16; i++)
        {
            list.add(new ItemStack(id, 1, i));
        }
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int s, float X, float Y, float Z) 
    {
        ProtectLvl pr = AreaProtect.instance.getPlayerAccess(player.getCommandSenderName(), world, x >> 4, z >> 4);
        if (pr == ProtectLvl.Free && !world.isRemote && player.isSneaking()) {
            dropBlockAsItem(world, x, y, z, world.getBlockMetadata(x, y, z), 0);
            world.setBlockToAir(x, y, z);
        }
        return player.isSneaking();
    }

    @Override
    public int getRenderColor(int m) 
    {
        return ItemDye.field_150922_c[~m & 15];
    }

    @Override
    public int colorMultiplier(IBlockAccess world, int x, int y, int z) 
    {
        return this.getRenderColor(world.getBlockMetadata(x, y, z));
    }

	@Override
	public boolean canEntityDestroy(IBlockAccess world, int x, int y, int z, Entity entity) 
	{
		return false;
	}
    
}
