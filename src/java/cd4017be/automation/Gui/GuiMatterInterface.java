package cd4017be.automation.Gui;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.ItemHandlerHelper;
import cd4017be.api.automation.MatterOrbItemHandler;
import cd4017be.automation.TileEntity.MatterInterface;
import cd4017be.lib.BlockGuiHandler;
import cd4017be.lib.Gui.GuiMachine;
import cd4017be.lib.Gui.TileContainer;
import cd4017be.lib.templates.AutomatedTile;

public class GuiMatterInterface extends GuiMachine {

	private MatterInterface tile;
	private int scroll = 0, size = 0;

	public GuiMatterInterface(MatterInterface tile, EntityPlayer player) {
		super(new TileContainer(tile, player));
		this.tile = tile;
		this.MAIN_TEX = new ResourceLocation("automation", "textures/gui/matterInterface.png");
		this.drawBG = 6;
	}

	@Override
	public void initGui() {
		this.xSize = 176;
		this.ySize = 182;
		super.initGui();
		guiComps.add(new Button(1, 133, 65, 18, 18, 0).texture(176, 18).setTooltip("matterI.rst#"));
		guiComps.add(new Slider(2, 160, 22, 36, 248, 0, 8, 12, false));
		guiComps.add(new ItemList(3, 8, 16, 150, 48));
	}

	@Override
	protected Object getDisplVar(int id) {
		switch(id) {
		case 1: return (int)tile.mode;
		case 2: return size > 1 ? (float)scroll / (float)(size - 1) : 0F;
		case 3: return tile.getSelSlot();
		default: return null;
		}
	}

	@Override
	protected void setDisplVar(int id, Object obj, boolean send) {
		PacketBuffer dos = BlockGuiHandler.getPacketTargetData(((TileContainer)inventorySlots).data.pos());
		switch(id) {
		case 1: dos.writeByte(AutomatedTile.CmdOffset + 1).writeByte(tile.mode = (byte)(((tile.mode) + ((Integer)obj == 0 ? 1 : 2)) % 3)); break;
		case 2: scroll = Math.round((float)(size - 1) * (Float)obj); return;
		case 3: dos.writeByte(AutomatedTile.CmdOffset).writeByte((Integer)obj); break;
		}
		if (send) BlockGuiHandler.sendPacketToServer(dos);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		mc.renderEngine.bindTexture(MAIN_TEX);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
		drawTexturedModalRect(guiLeft + 79, guiTop + 65, 0, 182, 90, 18);
		super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
	}

	class ItemList extends GuiComp {

		ItemStack[] list = new ItemStack[0];

		public ItemList(int id, int px, int py, int w, int h) {
			super(id, px, py, w, h);
		}

		@Override
		public void draw() {
			list = MatterOrbItemHandler.getAllItems(tile.inventory.items[0]);
			size = (list.length + 11) / 12;
			if (scroll >= size) scroll = size - 1;
			if (scroll < 0) scroll = 0;
			int scr = scroll * 12, w2 = w / 2;
			GlStateManager.pushMatrix();
			GlStateManager.translate(px, py, 0);
			GlStateManager.scale(0.5F, 0.5F, 0.5F);
			RenderHelper.enableGUIStandardItemLighting();
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

}
