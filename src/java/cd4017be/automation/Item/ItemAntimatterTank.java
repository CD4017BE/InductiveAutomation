/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cd4017be.automation.Item;

import java.util.List;

import cd4017be.api.automation.AntimatterItemHandler;
import cd4017be.api.automation.AntimatterItemHandler.IAntimatterItem;
import cd4017be.automation.Automation;
import cd4017be.automation.Config;
import cd4017be.automation.Entity.EntityAntimatterExplosion1;
import cd4017be.lib.BlockItemRegistry;
import cd4017be.lib.DefaultItemBlock;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;

/**
 *
 * @author CD4017BE
 */
public class ItemAntimatterTank extends DefaultItemBlock implements IAntimatterItem, IFluidContainerItem
{
    public static double explFaktor;
    public static int BombMaxCap = 160000000;
    private final int cap;
    
    public ItemAntimatterTank(Block id)
    {
        super(id);
        this.setMaxStackSize(1);
        cap = id == BlockItemRegistry.blockId("tile.antimatterBombF") ? BombMaxCap : Config.tankCap[4];
    }

    @Override
	public EnumRarity getRarity(ItemStack item) 
    {
		return item.isItemEqual(BlockItemRegistry.stack("tile.antimatterBombF", 1)) ? EnumRarity.epic : EnumRarity.uncommon;
	}
    
    @Override
    public void addInformation(ItemStack item, EntityPlayer player, List list, boolean par4) 
    {
        AntimatterItemHandler.addInformation(item, list);
        if (item.stackTagCompound != null && item.isItemEqual(BlockItemRegistry.stack("tile.antimatterBombF", 1)))
        {
            int d = (int)Math.cbrt((double)item.stackTagCompound.getInteger(getAntimatterTag(item)) * explFaktor);
            list.add("Crater radius (Stone) ~ " + d + " m");
            if (d > EntityAntimatterExplosion1.maxSize) list.add(String.format("Explosion will stop at %d m !", EntityAntimatterExplosion1.maxSize));
        }
        super.addInformation(item, player, list, par4);
    }

    @Override
    public void getSubItems(Item id, CreativeTabs tab, List list) 
    {
        super.getSubItems(id, tab, list);
        if (id == Item.getItemFromBlock(BlockItemRegistry.blockId("tile.antimatterTank"))) {
            ItemStack item = new ItemStack(id, 1, 0);
            item.stackTagCompound = new NBTTagCompound();
            item.stackTagCompound.setInteger("antimatter", Config.tankCap[4]);
            list.add(item);
        }
    }

    @Override
    public int getAmCapacity(ItemStack item) 
    {
        return cap;
    }

    @Override
    public String getAntimatterTag(ItemStack item) 
    {
        return "antimatter";
    }

	@Override
	public FluidStack getFluid(ItemStack item) 
	{
		return new FluidStack(Automation.L_antimatter, AntimatterItemHandler.getAntimatter(item));
	}

	@Override
	public int getCapacity(ItemStack item) 
	{
		return getAmCapacity(item);
	}

	@Override
	public int fill(ItemStack item, FluidStack resource, boolean doFill) 
	{
		if (resource == null || resource.getFluid() != Automation.L_antimatter) return 0;
		if (doFill) return AntimatterItemHandler.addAntimatter(item, resource.amount);
		else return Math.min(resource.amount, getAmCapacity(item) - AntimatterItemHandler.getAntimatter(item));
	}

	@Override
	public FluidStack drain(ItemStack item, int maxDrain, boolean doDrain) {
		if (doDrain) return new FluidStack(Automation.L_antimatter, -AntimatterItemHandler.addAntimatter(item, -maxDrain));
		else return new FluidStack(Automation.L_antimatter, Math.min(maxDrain, AntimatterItemHandler.getAntimatter(item)));
	}
    
}
