/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cd4017be.automation.Block;

import cd4017be.api.automation.AreaProtect;
import cd4017be.api.automation.ProtectLvl;
import cd4017be.automation.Automation;
import cd4017be.lib.DefaultBlock;
import cd4017be.lib.DefaultItemBlock;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/**
 *
 * @author CD4017BE
 */
public class GlassUnbreakable extends DefaultBlock
{
    
    public GlassUnbreakable(String id, int tex)
    {
        super(id, Material.glass, DefaultItemBlock.class, "unbr/glass");
        this.setCreativeTab(Automation.tabAutomation);
        this.setBlockUnbreakable();
        this.setResistance(Float.POSITIVE_INFINITY);
    }

    @Override
    public boolean isOpaqueCube() 
    {
        return false;
    }

    @Override
    public boolean isBlockSolid(IBlockAccess world, int x, int y, int z, int side) 
    {
        return true;
    }
    
    @Override
    public boolean renderAsNormalBlock()
    {
        return false;
    }

    @Override
    public boolean shouldSideBeRendered(IBlockAccess world, int x, int y, int z, int s) 
    {
        if (world.getBlock(x, y, z) == this) return false;
        else return super.shouldSideBeRendered(world, x, y, z, s);
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
	public boolean canEntityDestroy(IBlockAccess world, int x, int y, int z, Entity entity) 
	{
		return false;
	}
    
}
