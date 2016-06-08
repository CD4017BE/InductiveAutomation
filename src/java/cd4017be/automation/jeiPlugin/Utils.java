package cd4017be.automation.jeiPlugin;

import java.util.ArrayList;
import java.util.Arrays;

import cd4017be.lib.templates.SlotHolo;
import cd4017be.lib.util.OreDictStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
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
    
    @SideOnly(Side.CLIENT)
    public static boolean transferSlots(Container cont, EntityPlayer player, TransferEntry trans, boolean max) {
    	PlayerControllerMP contr = Minecraft.getMinecraft().playerController;
    	ItemStack stack, stack1;
    	int n = 0;
    	for (int s : trans.src) {
    		if (!max && n >= trans.reqAm) break;
    		stack = stackInSlot(cont, s);
    		if (stack == null) continue;
    		n += stack.stackSize;
    		if (s == trans.dest) continue;
    		if (max || n <= trans.reqAm) {
    			contr.windowClick(cont.windowId, s, 0, ClickType.PICKUP, player);
    			contr.windowClick(cont.windowId, trans.dest, 0, ClickType.PICKUP, player);
    			contr.windowClick(cont.windowId, s, 0, ClickType.PICKUP, player);
    		} else {
    			if ((stack1 = stackInSlot(cont, trans.dest)) != null && !cd4017be.lib.util.Utils.itemsEqual(stack, stack1) && !clearSlot(cont, player, trans.dest)) return false;
    			contr.windowClick(cont.windowId, s, 0, ClickType.PICKUP, player);
    			for (int i = trans.reqAm - n + stack.stackSize; i > 0; i--)
    				contr.windowClick(cont.windowId, trans.dest, 1, ClickType.PICKUP, player);
    			contr.windowClick(cont.windowId, s, 0, ClickType.PICKUP, player);
    			return true;
    		}
    	}
    	return true;
    }
    
    @SideOnly(Side.CLIENT)
    public static boolean clearSlot(Container cont, EntityPlayer player, int slot) {
    	Minecraft.getMinecraft().playerController.windowClick(cont.windowId, slot, 0, ClickType.QUICK_MOVE, player);
    	return !cont.getSlot(slot).getHasStack();
    }
    
    public static ItemStack stackInSlot(Container cont, int s) {
    	Slot slot = cont.getSlot(s);
    	return slot == null && !(slot instanceof SlotHolo) ? null : slot.getStack();
    }
    
    public static class TransferEntry {
		public TransferEntry(int dest) {
			this.dest = dest;
		}
		public final int dest;
		public int reqAm;
    	public ArrayList<Integer> src = new ArrayList<Integer>();
		public ItemStack targetI;
		
		public boolean hasEnough(boolean max) {
			return targetI != null && targetI.stackSize >= (max ? targetI.getMaxStackSize() : reqAm);
		}
	}
	
}
