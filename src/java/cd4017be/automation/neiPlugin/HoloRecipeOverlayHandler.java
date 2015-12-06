package cd4017be.automation.neiPlugin;

import cd4017be.lib.templates.SlotHolo;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import codechicken.nei.FastTransferManager;
import codechicken.nei.PositionedStack;
import codechicken.nei.api.IOverlayHandler;
import codechicken.nei.recipe.IRecipeHandler;

public class HoloRecipeOverlayHandler implements IOverlayHandler 
{
	private final int[] craftArea = {25, 61, 6, 42};
	public int offsetx;
	public int offsety;
	
	public HoloRecipeOverlayHandler(int ofsX, int ofsY) 
	{
		this.offsetx = ofsX;
		this.offsety = ofsY;
	}
	
	public HoloRecipeOverlayHandler setCraftArea(int x0, int x1, int y0, int y1)
	{
		craftArea[0] = x0;
		craftArea[1] = x1;
		craftArea[2] = y0;
		craftArea[3] = y1;
		return this;
	}

	@Override
	public void overlayRecipe(GuiContainer gui, IRecipeHandler recipe, int r, boolean shift) 
	{
		boolean clear;
		for (Object obj : gui.inventorySlots.inventorySlots) {
			if (!(obj instanceof SlotHolo)) continue;
			SlotHolo slot = (SlotHolo)obj;
			clear = slot.getHasStack();
			int sx = slot.xDisplayPosition - this.offsetx, 
				sy = slot.yDisplayPosition - this.offsety;
			for (PositionedStack pStack : recipe.getIngredientStacks(r)) 
				if (sx == pStack.relx && sy == pStack.rely) {
					this.correctSlot(slot, pStack, gui);
					clear = false;
					break;
				}
			if (clear && sx >= craftArea[0] && sx <= craftArea[1] && sy >= craftArea[2] && sy <= craftArea[3]) {
				FastTransferManager.clickSlot(gui, slot.slotNumber);
			}
		}
	}
	
	private void correctSlot(SlotHolo slot, PositionedStack stack, GuiContainer gui)
	{
		ItemStack src = slot.getStack();
		if (src != null && stack.contains(src)) return;
		for (Object obj : gui.inventorySlots.inventorySlots) {
			Slot s = (Slot)obj;
			if (s.getHasStack() && s.inventory instanceof InventoryPlayer && stack.contains(s.getStack())) {
				FastTransferManager.clickSlot(gui, s.slotNumber);
				FastTransferManager.clickSlot(gui, slot.slotNumber, 1);
				FastTransferManager.clickSlot(gui, s.slotNumber);
				return;
			}
		}
		if (src != null) FastTransferManager.clickSlot(gui, slot.slotNumber);
	}
	
}
