package cd4017be.automation.Gui;

import java.io.IOException;

import org.lwjgl.opengl.GL11;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;
import cd4017be.automation.TileEntity.SolidFuelHeater;
import cd4017be.lib.BlockGuiHandler;
import cd4017be.lib.TileContainer;
import cd4017be.lib.templates.AutomatedTile;
import cd4017be.lib.templates.GuiMachine;

public class GuiSolidFuelHeater extends GuiMachine {

	private final SolidFuelHeater tileEntity;
	
	public GuiSolidFuelHeater(SolidFuelHeater tileEntity, EntityPlayer player) {
		super(new TileContainer(tileEntity, player));
		this.tileEntity = tileEntity;
	}

	@Override
	public void initGui() {
		this.xSize = 176;
		this.ySize = 150;
		super.initGui();
		this.guiComps.add(new Slider(0, 80, 24, 70, 198, 8, 4, 12, true));
	}

	@Override
	protected Object getDisplVar(int id) {
		return (tileEntity.netData.floats[0] - 300F) / 2500F;
	}

	@Override
	protected void setDisplVar(int id, Object obj, boolean send) {
		tileEntity.netData.floats[0] = (Float)obj * 2500F + 300F;
		if (send) {
			PacketBuffer dos = tileEntity.getPacketTargetData();
			dos.writeByte(AutomatedTile.CmdOffset);
			dos.writeFloat(tileEntity.netData.floats[0]);
			BlockGuiHandler.sendPacketToServer(dos);
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mx, int my) {
		super.drawGuiContainerForegroundLayer(mx, my);
		this.drawFormatInfo(54, 17, 14, 14, "fuelHeat", tileEntity.netData.ints[1]);
        this.drawFormatInfo(53, 34, 16, 16, "boiler.burnUp", tileEntity.netData.ints[2]);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.renderEngine.bindTexture(new ResourceLocation("automation", "textures/gui/heater.png"));
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
        int n = tileEntity.netData.ints[0] == 0 ? 0 : tileEntity.netData.ints[1] * 14 / tileEntity.netData.ints[0];
        this.drawTexturedModalRect(this.guiLeft + 54, this.guiTop + 31 - n, 184, 22 - n, 14, n);
        n = this.tileEntity.netData.ints[2] * 2;
        this.drawTexturedModalRect(this.guiLeft + 53, this.guiTop + 50 - n, 176, 24 - n, 8, n);
        n = (int)(tileEntity.netData.floats[1] - 300F) * 70 / 2500;
        if (n > 70) n = 70;
        if (n > 0) this.drawTexturedModalRect(guiLeft + 80, guiTop + 16, 176, 0, n, 8);
        super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
        this.drawStringCentered(String.format("%.1f / %.0f °C", tileEntity.netData.floats[1] - 273.15F, tileEntity.netData.floats[0] - 273.15F), guiLeft + 115, guiTop + 38, 0x808040);
        this.drawStringCentered(tileEntity.getName(), this.guiLeft + this.xSize / 2, this.guiTop + 4, 0x404040);
        this.drawStringCentered(I18n.translateToLocal("container.inventory"), this.guiLeft + this.xSize / 2, this.guiTop + 54, 0x404040);
	}

	@Override
	protected void mouseClicked(int x, int y, int b) throws IOException {
		super.mouseClicked(x, y, b);
		byte cmd = -1;
		if (this.isPointInRegion(62, 35, 6, 6, x, y)) cmd = 1;
		else if (this.isPointInRegion(62, 43, 6, 6, x, y)) cmd = 2;
		if (cmd >= 0) {
			PacketBuffer dos = tileEntity.getPacketTargetData();
			dos.writeByte(AutomatedTile.CmdOffset + cmd);
			BlockGuiHandler.sendPacketToServer(dos);
		}
	}

}
