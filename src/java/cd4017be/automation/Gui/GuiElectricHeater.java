package cd4017be.automation.Gui;

import java.io.IOException;

import org.lwjgl.opengl.GL11;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import cd4017be.api.automation.PipeEnergy;
import cd4017be.automation.Config;
import cd4017be.automation.TileEntity.HeatingCoil;
import cd4017be.lib.BlockGuiHandler;
import cd4017be.lib.Gui.GuiMachine;
import cd4017be.lib.Gui.TileContainer;
import cd4017be.lib.Gui.GuiMachine.Text;
import cd4017be.lib.templates.AutomatedTile;

public class GuiElectricHeater extends GuiMachine {

	private final HeatingCoil tile;
	
	public GuiElectricHeater(HeatingCoil tile, EntityPlayer player) {
		super(new TileContainer(tile, player));
		this.tile = tile;
	}

	@Override
	public void initGui() {
		this.xSize = 132;
		this.ySize = 76;
		super.initGui();
		this.guiComps.add(new Slider(0, 45, 24, 70, 198, 8, 4, 12, true));

		guiComps.add(new Text(10, 0, 4, xSize, 0, "gui.cd4017be.advancedFurnace.name"));
	}

	@Override
	protected Object getDisplVar(int id) {
		return (tile.netF0 - 300F) / 2500F;
	}

	@Override
	protected void setDisplVar(int id, Object obj, boolean send) {
		tile.netF0 = (Float)obj * 2500F + 300F;
		if (send) {
			PacketBuffer dos = tile.getPacketTargetData();
			dos.writeByte(AutomatedTile.CmdOffset + 1);
			dos.writeFloat(tile.netF0);
			BlockGuiHandler.sendPacketToServer(dos);
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mx, int my) {
		super.drawGuiContainerForegroundLayer(mx, my);
		this.drawInfo(26, 16, 8, 52, PipeEnergy.getEnergyInfo(tile.netF1, 0F, (float)tile.netI0));
		this.drawInfo(8, 36, 16, 12, "\\i", "resistor");
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.renderEngine.bindTexture(new ResourceLocation("automation", "textures/gui/heater.png"));
		this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 160, this.xSize, this.ySize);
		this.drawTexturedModalRect(guiLeft + 43, guiTop + 51, 212 + (tile.netI1 & 2) * 9, 8, 18, 18);
		this.drawTexturedModalRect(guiLeft + 61, guiTop + 51, 176 + (tile.netI1 & 3) * 18, 26, 18, 18);
		int n = tile.getPowerScaled(52);
		this.drawTexturedModalRect(this.guiLeft + 26, this.guiTop + 68 - n, 248, 52 - n, 8, n);
		n = (int)(tile.netF2 - 300F) * 70 / 2500;
		if (n > 70) n = 70;
		if (n > 0) this.drawTexturedModalRect(guiLeft + 45, guiTop + 16, 176, 0, n, 8);
		super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
		this.drawStringCentered(String.format("%.1f / %.0f °C", tile.netF2 - 273.15F, tile.netF0 - 273.15F), guiLeft + 80, guiTop + 38, 0x808040);
		this.drawStringCentered("" + tile.netI0, this.guiLeft + 16, this.guiTop + 38, 0x404040);
		this.drawStringCentered(tile.getName(), this.guiLeft + this.xSize / 2, this.guiTop + 4, 0x404040);
	}

	@Override
	protected void mouseClicked(int x, int y, int b) throws IOException {
		int cmd = -1;
		if (this.isPointInRegion(44, 52, 16, 16, x, y)) {
			tile.netI1 ^= 2;
			cmd = 2;
		} else if (this.isPointInRegion(62, 52, 16, 16, x, y)) {
			tile.netI1 ^= 1;
			cmd = 2;
		} else if (this.isPointInRegion(8, 16, 16, 10, x, y)) {
			tile.netI0 += 10;
			cmd = 0;
		} else if (this.isPointInRegion(8, 26, 16, 10, x, y)) {
			tile.netI0 ++;
			cmd = 0;
		} else if (this.isPointInRegion(8, 48, 16, 10, x, y)) {
			tile.netI0 --;
			cmd = 0;
		} else if (this.isPointInRegion(8, 58, 16, 10, x, y)) {
			tile.netI0 -= 10;
			cmd = 0;
		}
		if (cmd >= 0) {
			if (tile.netI0 < Config.Rmin) tile.netI0 = Config.Rmin;
			PacketBuffer dos = tile.getPacketTargetData();
			dos.writeByte(cmd + AutomatedTile.CmdOffset);
			if (cmd == 0) dos.writeShort(tile.netI0);
			else if (cmd == 2) dos.writeByte(tile.netI1);
			BlockGuiHandler.sendPacketToServer(dos);
		}
		super.mouseClicked(x, y, b);
	}

}
