package cd4017be.automation.jeiPlugin;

import java.util.Arrays;

import cd4017be.lib.util.OreDictStack;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;

public class Utils 
{
    
    public static Object getItems(Object o) {
    	if (o instanceof ItemStack) return (ItemStack)o;
    	else if (o instanceof OreDictStack) {
    		OreDictStack obj = (OreDictStack)o;
			ItemStack[] list = obj.getItems();
			return Arrays.asList(list);
    	} else return null;
    }
    
    public static boolean valid(Object o) {
    	if (o == null || o instanceof ItemStack) return true;
    	else if (o instanceof OreDictStack) return !OreDictionary.getOres(((OreDictStack)o).id).isEmpty();
    	else if (o instanceof FluidStack) return true;
    	else return false;
    }
	
}
