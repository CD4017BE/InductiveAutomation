package cd4017be.automation.Gui;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.ItemHandlerHelper;
import cd4017be.api.automation.MatterOrbItemHandler;
import cd4017be.automation.Item.ItemMatterInterface.GuiData;
import cd4017be.lib.BlockGuiHandler;
import cd4017be.lib.Gui.GuiMachine;
import cd4017be.lib.Gui.TileContainer;

/**
 *
 * @author CD4017BE
 */
public class GuiItemMatterInterface extends GuiMachine {

	private GuiData data;
	private int amount = 0, target = -1, scroll = 0, size = 0;

	public GuiItemMatterInterface(GuiData tile, EntityPlayer player) {
		super(new TileContainer(tile, player));
		this.data = tile;
		this.MAIN_TEX = new ResourceLocation("automation", "textures/gui/matterInterface.png");
	}

	@Override
	public void initGui() {
		this.xSize = 176;
		this.ySize = 182;
		super.initGui();
		guiComps.add(new TextField(0, 116, 66, 34, 8, 6));
		guiComps.add(new Button(1, 115, 74, 36, 9, -1));
		guiComps.add(new Slider(2, 160, 22, 36, 248, 0, 8, 12, false));
		guiComps.add(new SlotSel(3, 7, 99, 162, 76));
		guiComps.add(new ItemList(4, 8, 16, 150, 48));
	}

	@Override
	protected Object getDisplVar(int id) {
		switch(id) {
		case 0: return "" + amount;
		case 2: return size > 1 ? (float)scroll / (float)(size - 1) : 0F;
		case 3: return data.inv.tool;
		case 4: return data.inv.slot;
		default: return null;
		}
	}

	@Override
	protected void setDisplVar(int id, Object obj, boolean send) {
		PacketBuffer dos = BlockGuiHandler.getPacketTargetData(((TileContainer)inventorySlots).data.pos());
		switch(id) {
		case 0: try {amount = Integer.parseInt((String)obj);} catch(NumberFormatException e) {} return;
		case 1: dos.writeByte(0).writeByte(target).writeInt(amount); break;
		case 2: scroll = Math.round((float)(size - 1) * (Float)obj); return;
		case 3: dos.writeByte(1).writeByte((Integer)obj); break;
		case 4: dos.writeByte(2).writeByte((Integer)obj); break;
		}
		if (send) BlockGuiHandler.sendPacketToServer(dos);
	}

	class ItemList extends GuiComp {

		ItemStack[] list = new ItemStack[0];

		public ItemList(int id, int px, int py, int w, int h) {
			super(id, px, py, w, h);
		}

		@Override
		public void draw() {
			list = MatterOrbItemHandler.getAllItems(data.inv.inv.mainInventory[data.inv.tool]);
			size = (list.length + 11) / 12;
			if (scroll >= size) scroll = size - 1;
			if (scroll < 0) scroll = 0;
			int scr = scroll * 12, w2 = w / 2;
			GlStateManager.pushMatrix();
			GlStateManager.translate(px, py, 0);
			GlStateManager.scale(0.5F, 0.5F, 0.5F);
			for (int i = 0; i < 12 && i < list.length - scr; i++)
				drawItemStack(ItemHandlerHelper.copyStackWithSize(list[i + scr], 1), (i % 2) * w, i / 2 * 16, null);
			GlStateManager.disableLighting();
			GlStateManager.popMatrix();
			int sel = (Integer)getDisplVar(id) - scr;
			if (sel >= 0 && sel < 12) {
				int x = sel % 2, y = sel / 2;
				drawGradientRect(px + x * w2, py + y * 8, px + (x + 1) * w2, py + (y + 1) * 8, 0x80ff4040, 0x80ff4040);
			}
			for (int i = 0; i < 12 && i < list.length - scr; i++)
				fontRendererObj.drawString("" + list[i + scr].stackSize, px + 8 + (i % 2) * w2, py + i / 2 * 8, 0x7fffff);
		}

		@Override
		public void drawOverlay(int mx, int my) {
			int s = (my - py) / 8 * 2 + scroll * 12 + (mx > px + w / 2 ? 1 : 0);
			if (s >= 0 && s < list.length) renderToolTip(list[s], mx - guiLeft, my - guiTop);
		}

		@Override
		public boolean mouseIn(int x, int y, int b, int d) {
			setDisplVar(id, (y - py) / 8 * 2 + scroll * 12 + (x > px + w / 2 ? 1 : 0), true);
			return true;
		}

	}

	class SlotSel extends GuiComp {

		public SlotSel(int id, int px, int py, int w, int h) {
			super(id, px, py, w, h);
		}

		@Override
		public void drawOverlay(int mx, int my) {
			mc.renderEngine.bindTexture(MAIN_TEX);
			TileContainer cont = (TileContainer)inventorySlots;
			int src = (Integer)getDisplVar(id);
			for (int i = cont.invPlayerS; i < cont.invPlayerE; i++) {
				Slot slot = cont.inventorySlots.get(i);
				if (slot.getSlotIndex() == src) drawTexturedModalRect(slot.xDisplayPosition, slot.yDisplayPosition, 194, 18, 16, 16);
				else if (slot.getSlotIndex() == target) drawTexturedModalRect(slot.xDisplayPosition, slot.yDisplayPosition, 210, 18, 16, 16);
			}
		}

		@Override
		public boolean mouseIn(int x, int y, int b, int d) {
			Slot slot = getSlotUnderMouse();
			if (slot == null || !MatterOrbItemHandler.isMatterOrb(slot.getStack())) return false;
			int i = slot.getSlotIndex();
			if (i == target) target = -1;
			if (b == 0) setDisplVar(id, i, true);
			else target = i;
			return true;
		}

	}

}
