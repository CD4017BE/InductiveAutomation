package cd4017be.automation.Gui;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;

import org.lwjgl.opengl.GL11;

import cd4017be.automation.TileEntity.ELink;
import cd4017be.lib.BlockGuiHandler;
import cd4017be.lib.TooltipInfo;
import cd4017be.lib.Gui.GuiMachine;
import cd4017be.lib.Gui.TileContainer;
import cd4017be.lib.templates.AutomatedTile;

/**
 *
 * @author CD4017BE
 */
public class GuiELink extends GuiMachine {

	private final ELink tile;

	public GuiELink(ELink tileEntity, EntityPlayer player) {
		super(new TileContainer(tileEntity, player));
		this.tile = tileEntity;
		this.MAIN_TEX = new ResourceLocation("automation", "textures/gui/ELink.png");
	}

	@Override
	public void initGui() {
		this.xSize = 176;
		this.ySize = 168;
		super.initGui();
		guiComps.add(new Button(1, 7, 51, 18, 18, 0).texture(176, 0).setTooltip("rstCtr"));
		guiComps.add(new Button(2, 25, 51, 18, 18, 0).texture(194, 0).setTooltip("rstCtr"));
		guiComps.add(new NumberSel(3, 98, 52, 70, 16, "%d", 0, tile.energy.Umax, 10).setup(30, 0xff0000ff, 2, true).setTooltip("link.ref0"));
		guiComps.add(new NumberSel(4, 98, 34, 70, 16, "%d", 0, tile.energy.Umax, 10).setup(30, 0xffff0000, 2, true).setTooltip("link.ref1"));
		guiComps.add(new ProgressBar(5, 8, 16, 160, 8, 0, 240, (byte)4).setTooltip("link.link"));
		guiComps.add(new ProgressBar(6, 8, 24, 160, 8, 0, 248, (byte)0).setTooltip("link.int"));
	}

	@Override
	protected Object getDisplVar(int id) {
		switch(id) {
		case 1: return tile.rstCtr & 3;
		case 2: return tile.rstCtr >> 1 & 1;
		case 3: return tile.Umin;
		case 4: return tile.Umax;
		
		default: return null;
		}
	}

	@Override
	protected void setDisplVar(int id, Object obj, boolean send) {
		PacketBuffer dos = tile.getPacketTargetData();
		switch(id) {
		
		}
		if (send) BlockGuiHandler.sendPacketToServer(dos);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
		int n = tile.getStorageScaled(160);
		this.drawTexturedModalRect(this.guiLeft + 8, this.guiTop + 16, 0, 240, n, 8);
		n = tile.getVoltageScaled(160);
		this.drawTexturedModalRect(this.guiLeft + 8, this.guiTop + 24, 0, 248, n, 8);
		this.drawStringCentered(tile.Umin + "V", this.guiLeft + 133, this.guiTop + 56, 0x404040);
		this.drawStringCentered(tile.Umax + "V", this.guiLeft + 133, this.guiTop + 38, 0x404040);
		this.drawStringCentered(String.format("%+.1f %s", tile.netF3 / 1000F, TooltipInfo.getPowerUnit()), this.guiLeft + 34, this.guiTop + 38, 0x404040);
		this.drawStringCentered(String.format("%.0f / %.0f %s", tile.netF1 / 1000F, tile.netF2 / 1000F, TooltipInfo.getEnergyUnit()), this.guiLeft + 88, this.guiTop + 16, 0x404040);
		this.drawStringCentered(String.format("%.2f V", tile.netF0), this.guiLeft + 88, this.guiTop + 24, 0x404040);
		this.drawStringCentered("Umin=", this.guiLeft + 79, this.guiTop + 56, 0x404040);
		this.drawStringCentered("Umax=", this.guiLeft + 79, this.guiTop + 38, 0x404040);
		super.drawGuiContainerBackgroundLayer(var1, var2, var3);
	}

	@Override
	protected void mouseClicked(int x, int y, int b) throws IOException {
		byte cmd = -1;
		if (this.isPointInRegion(7, 51, 18, 18, x, y))
		{
			tile.rstCtr ^= 2;
			cmd = 2;
		} else
		if (this.isPointInRegion(25, 51, 18, 18, x, y))
		{
			tile.rstCtr ^= 1;
			cmd = 2;
		} else
		if (this.isPointInRegion(98, 52, 10, 16, x, y))
		{
			tile.Umin -= b == 0 ? 10 : 1000;
			cmd = 0;
		} else
		if (this.isPointInRegion(108, 52, 10, 16, x, y))
		{
			tile.Umin -= b == 0 ? 1 : 100;
			cmd = 0;
		} else
		if (this.isPointInRegion(148, 52, 10, 16, x, y))
		{
			tile.Umin += b == 0 ? 1 : 100;
			cmd = 0;
		} else
		if (this.isPointInRegion(158, 52, 10, 16, x, y))
		{
			tile.Umin += b == 0 ? 10 : 1000;
			cmd = 0;
		} else
		if (this.isPointInRegion(98, 34, 10, 16, x, y))
		{
			tile.Umax -= b == 0 ? 10 : 1000;
			cmd = 1;
		} else
		if (this.isPointInRegion(108, 34, 10, 16, x, y))
		{
			tile.Umax -= b == 0 ? 1 : 100;
			cmd = 1;
		} else
		if (this.isPointInRegion(148, 34, 10, 16, x, y))
		{
			tile.Umax += b == 0 ? 1 : 100;
			cmd = 1;
		} else
		if (this.isPointInRegion(158, 34, 10, 16, x, y))
		{
			tile.Umax += b == 0 ? 10 : 1000;
			cmd = 1;
		}
		if (cmd >= 0)
		{
			if (tile.Umin < 0) tile.Umin = 0;
			if (tile.Umin > tile.energy.Umax) tile.Umin = tile.energy.Umax;
			if (tile.Umax < 0) tile.Umax = 0;
			if (tile.Umax > tile.energy.Umax) tile.Umax = tile.energy.Umax;
			
			dos.writeByte(AutomatedTile.CmdOffset + cmd);
			dos.writeInt(tile.netData.ints[cmd]);
			
		}
		super.mouseClicked(x, y, b);
	}

}
