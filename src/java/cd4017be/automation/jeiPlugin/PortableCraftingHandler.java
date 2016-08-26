package cd4017be.automation.jeiPlugin;

import java.util.List;
import java.util.Map.Entry;

import cd4017be.automation.Gui.ContainerPortableCrafting;
import cd4017be.lib.BlockGuiHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import mezz.jei.api.gui.IGuiIngredient;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;

public class PortableCraftingHandler implements IRecipeTransferHandler<ContainerPortableCrafting> {

	public PortableCraftingHandler(IRecipeTransferHandlerHelper helper) {}
	
	@Override
	public Class<ContainerPortableCrafting> getContainerClass() {
		return ContainerPortableCrafting.class;
	}

	@Override
	public String getRecipeCategoryUid() {
		return VanillaRecipeCategoryUid.CRAFTING;
	}

	@Override
	public IRecipeTransferError transferRecipe(ContainerPortableCrafting container, IRecipeLayout recipeLayout, EntityPlayer player, boolean maxTransfer, boolean doTransfer) {
		if (!doTransfer) return null;
		IGuiItemStackGroup items = recipeLayout.getItemStacks();
		ItemStack type, stack;
		List<ItemStack> var;
		NBTTagList grid = new NBTTagList();
		NBTTagCompound entry;
		for (Entry<Integer, ? extends IGuiIngredient<ItemStack>> ingr : items.getGuiIngredients().entrySet()) {
			if (!ingr.getValue().isInput() || (var = ingr.getValue().getAllIngredients()).isEmpty()) continue;
			entry = new NBTTagCompound();
			type = null;
			for (int i = 0; type == null && i < player.inventory.mainInventory.length; i++) 
				if ((stack = player.inventory.mainInventory[i]) != null) 
					for (ItemStack item : var) 
						if (stack.isItemEqual(item)) {
							type = stack;
							break;
						}
			if (type == null) type = var.get(0);
			type.writeToNBT(entry);
			entry.setByte("Count", (byte)1);
			entry.setByte("slot", (byte)(ingr.getKey() - 1));
			grid.appendTag(entry);
		}
		entry = new NBTTagCompound();
		entry.setTag("grid", grid);
		entry.setByte("amount", maxTransfer ? (byte)64 : (byte)1);
		PacketBuffer packet = BlockGuiHandler.getPacketTargetData(new BlockPos(0, -1, 0));
		packet.writeByte(4);
		packet.writeNBTTagCompoundToBuffer(entry);
		BlockGuiHandler.sendPacketToServer(packet);
		return null;
	}

}
