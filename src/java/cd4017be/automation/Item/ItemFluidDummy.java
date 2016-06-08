/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cd4017be.automation.Item;

import java.util.List;
import cd4017be.automation.Automation;
import cd4017be.automation.Objects;
import cd4017be.lib.DefaultItem;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
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
	
    public ItemFluidDummy(String id)
    {
        super(id);
        this.setCreativeTab(Automation.tabFluids);
    }

    @Override
	public String getUnlocalizedName(ItemStack item) {
        FluidStack fluid = this.getFluid(item);
        return fluid != null ? fluid.getUnlocalizedName() : super.getUnlocalizedName(item);
	}

	@Override
	public void getSubItems(Item item, CreativeTabs tab, List<ItemStack> list) 
    {
		if (tab == Automation.tabFluids)
			for (Fluid fluid : FluidRegistry.getRegisteredFluids().values()) 
				list.add(item(fluid, 1));
	}
    
    @Override
    public ActionResult<ItemStack> onItemRightClick(ItemStack item, World world, EntityPlayer player, EnumHand hand)
    {
        RayTraceResult movingobjectposition = this.rayTrace(world, player, false);

        if (movingobjectposition == null) return new ActionResult<ItemStack>(EnumActionResult.PASS, item);
        else if (movingobjectposition.typeOfHit == Type.BLOCK)
        {
            BlockPos pos = movingobjectposition.getBlockPos();
            if (!world.canMineBlockBody(player, pos)) return new ActionResult<ItemStack>(EnumActionResult.FAIL, item);
            
            pos = pos.offset(movingobjectposition.sideHit);
            
            if (!player.canPlayerEdit(pos, movingobjectposition.sideHit, item)) return new ActionResult<ItemStack>(EnumActionResult.FAIL, item);
            Fluid fluid = fluid(item);
            if (fluid == null) item.stackSize--;
            else if (this.tryPlaceContainedLiquid(world, pos, fluid) && !player.capabilities.isCreativeMode) item.stackSize--;
        }
        return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, item);
    }
    
    public boolean tryPlaceContainedLiquid(World world, BlockPos pos, Fluid fluid)
    {
    	Block block = fluid.getBlock();
    	if (block == null) return false;
    	IBlockState state = world.getBlockState(pos);
    	Material material = state.getBlock().getMaterial(state);
        if (!world.isAirBlock(pos) && material.isSolid()) return false;
        world.setBlockState(pos, block.getDefaultState(), 3);
        return true;
    }
    
    @Override
    public FluidStack getFluid(ItemStack item)
    {
    	Fluid fluid = fluid(item);
    	return fluid == null ? null : new FluidStack(fluid, 1000);
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
		FluidStack fluid = this.getFluid(item);
		if (doDrain) {
			item.stackSize = 0;
			item.setItem(null);
		}
		return fluid;
	}
	
	public static Fluid fluid(ItemStack item) {
		return item.getItem() == Objects.fluidDummy && item.getTagCompound() != null ? FluidRegistry.getFluid(item.getTagCompound().getString("i")) : null;
	}
	
	public static ItemStack item(Fluid fluid, int am) {
		ItemStack item = new ItemStack(Objects.fluidDummy, am);
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setString("i", fluid.getName());
		item.setTagCompound(nbt);
		return item;
	}
    
}
