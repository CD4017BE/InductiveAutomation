package cd4017be.automation.Gui;

import org.lwjgl.opengl.GL11;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;
import cd4017be.automation.TileEntity.HeatedFurnace;
import cd4017be.lib.TileContainer;
import cd4017be.lib.templates.GuiMachine;

public class GuiHeatedFurnace extends GuiMachine {

	private final HeatedFurnace tile;
	
	public GuiHeatedFurnace(HeatedFurnace tile, EntityPlayer player) {
		super(new TileContainer(tile, player));
		this.tile = tile;
	}

	@Override
	public void initGui() {
		this.xSize = 176;
		this.ySize = 150;
		super.initGui();
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mx, int my) {
		super.drawGuiContainerForegroundLayer(mx, my);
		
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.renderEngine.bindTexture(new ResourceLocation("automation", "textures/gui/heatedFurnace.png"));
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
        int n = tile.getProgressScaled(32);
        this.drawTexturedModalRect(this.guiLeft + 36, this.guiTop + 19, 176, 8, n, 10);
        n = (int)(tile.netData.floats[1] - 300F) * 70 / 2500;
        if (n > 70) n = 70;
        if (n > 0) this.drawTexturedModalRect(guiLeft + 17, guiTop + 38, 176, 0, n, 8);
        n = (int)(HeatedFurnace.NeededTemp - 300F) * 70 / 2500;
        this.drawTexturedModalRect(guiLeft + 15 + n, guiTop + 34, 251, 0, 5, 16);
		super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
		if (tile.netData.ints[0] > 1) this.drawCenteredString(fontRendererObj, "x" + tile.netData.ints[0], guiLeft + 61, guiTop + 25, 0xc04040);
		this.drawStringCentered(String.format("%.1f / %.0f °C", tile.netData.floats[1] - 273.15F, HeatedFurnace.NeededTemp - 273.15F), guiLeft + 133, guiTop + 38, 0x808040);
		//this.drawStringCentered(TooltipInfo.format("gui.cd4017be.", args), x, y, c);
		this.drawStringCentered(tile.getName(), this.guiLeft + this.xSize / 2, this.guiTop + 4, 0x404040);
		this.drawStringCentered(I18n.translateToLocal("container.inventory"), this.guiLeft + this.xSize / 2, this.guiTop + 54, 0x404040);
	}

}
