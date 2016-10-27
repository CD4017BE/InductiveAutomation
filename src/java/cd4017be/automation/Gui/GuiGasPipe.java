package cd4017be.automation.Gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;

import org.lwjgl.opengl.GL11;

import cd4017be.automation.TileEntity.GasPipe;
import cd4017be.lib.TileEntityData;
import cd4017be.lib.Gui.GuiMachine;
import cd4017be.lib.Gui.TileContainer;
import cd4017be.lib.util.Utils;

public class GuiGasPipe extends GuiMachine {
	
	private TileEntityData data;
	
	public GuiGasPipe(GasPipe tileEntity, EntityPlayer player) {
		super(new TileContainer(tileEntity, player));
	}

	@Override
	public void initGui() {
		data = ((TileContainer)this.inventorySlots).refData;
		this.xSize = 80;
		this.ySize = 56;
		super.initGui();
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.renderEngine.bindTexture(new ResourceLocation("automation", "textures/gui/tesla.png"));
		this.drawTexturedModalRect(this.guiLeft, this.guiTop, 176, 80, this.xSize, this.ySize);
		this.drawStringCentered(I18n.translateToLocal("gui.cd4017be.gasPipe.name"), this.guiLeft + this.xSize / 2, this.guiTop + 4, 0x404040);
		fontRendererObj.drawString(String.format("V= %sm³", Utils.formatNumber(data.floats[0], 3)), guiLeft + 8, guiTop + 16, 0x808040);
		fontRendererObj.drawString(String.format("P= %sPa", Utils.formatNumber(data.floats[3], 4, 0)), guiLeft + 8, guiTop + 24, 0x808040);
		fontRendererObj.drawString(String.format("T= %.1f°C", data.floats[2] - 273.15F), guiLeft + 8, guiTop + 32, 0x808040);
		fontRendererObj.drawString(String.format("n= %smol", Utils.formatNumber(data.floats[1] / 8.314472F, 3, 0)), guiLeft + 8, guiTop + 40, 0x808040);
		super.drawGuiContainerBackgroundLayer(var1, var2, var3);
	}

}
