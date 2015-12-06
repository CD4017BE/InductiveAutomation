package cd4017be.automation.neiPlugin;

import java.util.ArrayList;

import cd4017be.lib.BlockItemRegistry;
import cd4017be.lib.util.Obj2;
import codechicken.nei.NEIServerUtils;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;
import net.minecraftforge.oredict.OreDictionary;

public class Utils 
{

    public static ItemStack getItem(Object obj, boolean info)
    {
    	if (obj == null) return null;
    	else if (obj instanceof ItemStack) return (ItemStack)obj;
    	else if (obj instanceof FluidStack) {
            ItemStack item = BlockItemRegistry.stack("item.fluidDummy", 1, ((FluidStack)obj).fluidID);
            if (info && item != null) {
    		NBTTagCompound nbt = new NBTTagCompound();
    		NBTTagList list = new NBTTagList();
    		list.appendTag(new NBTTagString(((FluidStack)obj).amount + "L"));
    		nbt.setTag("Lore", list);
    		item.setTagInfo("display", nbt);
            }
            return item;
    	} else return null;
    }

    public static boolean isOrContains(ItemStack item, Object obj)
    {
	if (item == null || obj == null) return false;
	else if (obj instanceof ItemStack) return NEIServerUtils.areStacksSameTypeCrafting(item, (ItemStack)obj);
	else if (obj instanceof FluidStack) {
            FluidStack fluid = (FluidStack)obj;
            if (Item.getItemFromBlock(fluid.getFluid().getBlock()) == item.getItem()) return true;
            else if (item.getItem() instanceof IFluidContainerItem && fluid.isFluidEqual(((IFluidContainerItem)item.getItem()).getFluid(item))) return true;
            else return fluid.isFluidEqual(FluidContainerRegistry.getFluidForFilledItem(item));
	} else return false;
    }
    
    public static ItemStack[] getItems(Object o) {
    	if (o instanceof ItemStack) return new ItemStack[]{(ItemStack)o};
    	else if (o instanceof Obj2) {
    		Obj2<Short, Integer> obj = (Obj2<Short, Integer>)o;
			ArrayList<ItemStack> list = OreDictionary.getOres(obj.objB);
			if (list.isEmpty()) {
				ItemStack repl = BlockItemRegistry.stack("item.fluidDummy", obj.objA);
				repl.setStackDisplayName("<" + OreDictionary.getOreName(obj.objB) + ">");
				NBTTagCompound displ = repl.stackTagCompound.getCompoundTag("display");
				NBTTagList lore = new NBTTagList();
				lore.appendTag(new NBTTagString("This oredictionary type"));
				lore.appendTag(new NBTTagString("has no registered items!"));
				displ.setTag("Lore", lore);
				repl.setTagInfo("display", displ);
				return new ItemStack[]{repl};
			}
			ItemStack[] items = new ItemStack[list.size()];
			for (int j = 0; j < items.length; j++) {
				items[j] = list.get(j).copy();
				items[j].stackSize = obj.objA;
			}
			return items;
    	} else return null;
    }
	
}
