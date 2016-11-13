package cd4017be.automation.Gui;

import cd4017be.automation.Item.ItemFilteredSubInventory;
import cd4017be.automation.Item.ItemRemoteInv.GuiData;
import cd4017be.lib.BlockGuiHandler;
import cd4017be.lib.Gui.GuiMachine;
import cd4017be.lib.Gui.TileContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

public class GuiRemoteInventory extends GuiMachine {

	private final GuiData data;
	private final InventoryPlayer inv;

	public GuiRemoteInventory(TileContainer cont) {
		super(cont);
		this.data = (GuiData)cont.data;
		this.inv = cont.player.inventory;
		this.MAIN_TEX = new ResourceLocation("automation", "textures/gui/portableRemoteInv.png");
		this.drawBG = 6;
	}

	@Override
	public void initGui() {
		this.xSize = 230;
		this.ySize = 150 + data.ofsY;
		super.initGui();
		guiComps.add(new Button(0, 11, ySize - 83, 10, 18, 0).texture(230, 0).setTooltip("inputFilter"));
		guiComps.add(new Button(1, 29, ySize - 83, 10, 18, 0).texture(240, 0).setTooltip("outputFilter"));
		guiComps.add(new GuiComp(2, 12, ySize - 20, 7, 8).setTooltip("specialSlot"));
	}

	@Override
	protected Object getDisplVar(int id) {
		ItemStack item = inv.mainInventory[inv.currentItem];
		if (id < 2) return ItemFilteredSubInventory.isFilterOn(item, id == 0) ? 1 : 0;
		else return null;
	}

	@Override
	protected void setDisplVar(int id, Object obj, boolean send) {
		PacketBuffer dos = BlockGuiHandler.getPacketTargetData(((TileContainer)inventorySlots).data.pos());
		dos.writeByte(id);
		if (send) BlockGuiHandler.sendPacketToServer(dos);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
		mc.renderEngine.bindTexture(MAIN_TEX);
		int h = data.size / 12;
		int w = data.size % 12;
		this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, 15);
		for (int i = 0; i < h; i++)
			this.drawTexturedModalRect(this.guiLeft, this.guiTop + 15 + i * 18, 0, 15, this.xSize, 18);
		if (w != 0) {
			int n = 7 + w * 18;
			this.drawTexturedModalRect(this.guiLeft, this.guiTop + 15 + h * 18, 0, 15, n, 18);
			this.drawTexturedModalRect(this.guiLeft + n, this.guiTop + 15 + h * 18, n, 33, this.xSize - n, 18);
		}
		this.drawTexturedModalRect(this.guiLeft, this.guiTop + 51 + data.ofsY, 0, 51, this.xSize, 99);
		super.drawGuiContainerBackgroundLayer(var1, var2, var3);
	}

}
