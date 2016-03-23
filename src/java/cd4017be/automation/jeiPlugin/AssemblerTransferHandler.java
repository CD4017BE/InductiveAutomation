package cd4017be.automation.jeiPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import cd4017be.automation.TileEntity.ElectricCompressor;
import cd4017be.automation.TileEntity.SteamCompressor;
import cd4017be.automation.jeiPlugin.Utils.TransferEntry;
import cd4017be.lib.TileContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.gui.ingredients.IGuiIngredient;

public class AssemblerTransferHandler implements IRecipeTransferHandler {

	private final IRecipeTransferHandlerHelper helper;
	public AssemblerTransferHandler(IRecipeTransferHandlerHelper helper) {
		this.helper = helper;
	}
	
	@Override
	public Class<? extends Container> getContainerClass() {
		return TileContainer.class;
	}

	@Override
	public String getRecipeCategoryUid() {
		return "automation.assembler";
	}

	@Override
	public IRecipeTransferError transferRecipe(Container container, IRecipeLayout recipeLayout, EntityPlayer player, boolean maxTransfer, boolean doTransfer) {
		TileContainer cont = (TileContainer)container; //all machines use TileContainer so we have to check if its the right one
		if (!(cont.tileEntity instanceof SteamCompressor || cont.tileEntity instanceof ElectricCompressor)) return helper.createInternalError();
		IGuiItemStackGroup items = recipeLayout.getItemStacks();
		//init recipe
		ArrayList<TransferEntry> transfer = new ArrayList<TransferEntry>();
		TransferEntry entry;
		int slot, slotsN = cont.inventorySlots.size();
		ItemStack stack;
		List<ItemStack> var;
		for (Entry<Integer, ? extends IGuiIngredient<ItemStack>> ingr : items.getGuiIngredients().entrySet()) {
			if ((slot = ingr.getKey()) >= 4 || (var = ingr.getValue().getAllIngredients()).isEmpty()) continue;
			entry = new TransferEntry(slot);
			for (int i = 0; i < slotsN && !entry.hasEnough(maxTransfer); i++) 
				if ((stack = Utils.stackInSlot(cont, i)) != null) {
					if (entry.targetI == null) {
						for (ItemStack item : var)
							if (stack.isItemEqual(item)) {
								entry.targetI = stack.copy();
								entry.reqAm = item.stackSize;
								entry.src.add(i);
								break;
							}
					} else if (entry.targetI.isItemEqual(stack)) {
						entry.targetI.stackSize += stack.stackSize;
						entry.src.add(i);
					}
				}
			transfer.add(entry);
		}
		//check recipe
		ArrayList<Integer> missing = new ArrayList<Integer>();
		for (TransferEntry e : transfer) if (!e.hasEnough(false)) missing.add(e.dest);
		if (!missing.isEmpty()) return helper.createUserErrorForSlots("missing Items!", missing);
		//execute recipe
		if (!doTransfer) return null;
		boolean[] cleared = new boolean[4];
		for (TransferEntry e : transfer) {
			if (!Utils.transferSlots(cont, player, e, maxTransfer)) 
				return helper.createUserErrorWithTooltip("Can't clear crafting area!");
			cleared[e.dest] = true;
		}
		for (int i = 0; i < cleared.length; i++) 
			if (!cleared[i] && cont.getSlot(i).getHasStack() && !Utils.clearSlot(cont, player, i)) 
				return helper.createUserErrorWithTooltip("Can't clear crafting area!");
		return null;
	}
	
}
