/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cd4017be.automation.Item;

import java.util.List;

import cd4017be.automation.Automation;
import cd4017be.lib.BlockItemRegistry;
import cd4017be.lib.DefaultItem;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;

/**
 *
 * @author CD4017BE
 */
public class ItemFluidDummy extends DefaultItem implements IFluidContainerItem
{
    public static ItemFluidDummy instance;
	
    public ItemFluidDummy(String id)
    {
        super(id);
        this.setHasSubtypes(true);
        this.setCreativeTab(Automation.tabFluids);
        this.setTextureName(BlockItemRegistry.texPath() + "invalid");
        instance = this;
    }

    @Override
    public String getItemStackDisplayName(ItemStack item) 
    {
        int id = item.getItemDamage();
        Fluid fluid = FluidRegistry.getFluid(id);
        return fluid != null ? fluid.getLocalizedName() : "Invalid Fluid";
    }

    @Override
	public void getSubItems(Item item, CreativeTabs tab, List list) 
    {
		if (tab == Automation.tabFluids)
			for (int i = 1; i <= FluidRegistry.getMaxID(); i++)
				list.add(new ItemStack(item, 1, i));
	}

	@Override
    public IIcon getIconFromDamage(int d) 
    {
        Fluid fluid = FluidRegistry.getFluid(d);
        if (fluid == null) return super.getIconFromDamage(d);
        IIcon tex = fluid.getIcon();
        if (tex == null) {
            Block block = fluid.getBlock();
            if (block != null) tex = block.getBlockTextureFromSide(1);
        }
        return tex;
    }

    @Override
    public int getSpriteNumber() 
    {
        return 0;
    }
    
    @Override
    public ItemStack onItemRightClick(ItemStack item, World world, EntityPlayer player)
    {
        MovingObjectPosition movingobjectposition = this.getMovingObjectPositionFromPlayer(world, player, false);

        if (movingobjectposition == null) return item;
        else if (movingobjectposition.typeOfHit == MovingObjectType.BLOCK)
        {
            int x = movingobjectposition.blockX;
            int y = movingobjectposition.blockY;
            int z = movingobjectposition.blockZ;
            if (!world.canMineBlock(player, x, y, z)) return item;

            if (movingobjectposition.sideHit == 0) --y;
            if (movingobjectposition.sideHit == 1) ++y;
            if (movingobjectposition.sideHit == 2) --z;
            if (movingobjectposition.sideHit == 3) ++z;
            if (movingobjectposition.sideHit == 4) --x;
            if (movingobjectposition.sideHit == 5) ++x;
            
            if (!player.canPlayerEdit(x, y, z, movingobjectposition.sideHit, item)) return item;
            Fluid fluid = FluidRegistry.getFluid(item.getItemDamage());
            if (fluid == null) {
                item.stackSize--;
            } else if (this.tryPlaceContainedLiquid(world, x, y, z, fluid) && !player.capabilities.isCreativeMode) item.stackSize--;
        }
        return item;
    }
    
    public boolean tryPlaceContainedLiquid(World world, int x, int y, int z, Fluid fluid)
    {
    	Block block = fluid.getBlock();
    	if (block == null) return false;
    	Block id = world.getBlock(x, y, z);
    	Material material = id.getMaterial();
        if (!world.isAirBlock(x, y, z) && material.isSolid()) return false;
        world.setBlock(x, y, z, block, 0, 3);
        return true;
    }
    
    @Override
    public FluidStack getFluid(ItemStack item)
    {
    	Fluid fluid = FluidRegistry.getFluid(item.getItemDamage());
    	return fluid == null ? null : new FluidStack(fluid, 1000);
    }
    
    public static ItemStack getFluidContainer(int id, int am)
    {
    	return new ItemStack(instance, am, id);
    }

	@Override
	public int getCapacity(ItemStack container) 
	{
		return 1000;
	}

	@Override
	public int fill(ItemStack container, FluidStack resource, boolean doFill) {
		return 0;
	}

	@Override
	public FluidStack drain(ItemStack item, int maxDrain, boolean doDrain) {
		if (maxDrain < 1000) return null;
		if (doDrain) item.stackSize = 0;
		return this.getFluid(item);
	}
    
}
