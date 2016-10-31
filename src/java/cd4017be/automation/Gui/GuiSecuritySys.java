package cd4017be.automation.Gui;

import java.io.IOException;

import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import cd4017be.automation.TileEntity.SecuritySys;
import cd4017be.lib.BlockGuiHandler;
import cd4017be.lib.TooltipInfo;
import cd4017be.lib.Gui.GuiMachine;
import cd4017be.lib.Gui.TileContainer;

/**
 *
 * @author CD4017BE
 */
public class GuiSecuritySys extends GuiMachine {

	private final SecuritySys tile;
	private int scroll;
	private int listS;
	private int listN = -1;

	public GuiSecuritySys(SecuritySys tileEntity, EntityPlayer player) {
		super(new TileContainer(tileEntity, player));
		this.tile = tileEntity;
		this.MAIN_TEX = new ResourceLocation("automation", "textures/gui/securitySys.png");
	}

	@Override
	public void initGui() {
		this.xSize = 179;
		this.ySize = 201;
		super.initGui();
		guiComps.add(new TextField(1, 137, 117, 34, 16, 32));
	}

	@Override
	protected Object getDisplVar(int id) {
		switch(id) {
		case 1: return "";
		default: return null;
		}
	}

	@Override
	protected void setDisplVar(int id, Object obj, boolean send) {
		PacketBuffer dos = tile.getPacketTargetData();
		switch(id) {
		case 1: dos.writeByte(2).writeByte(listN<0 ? 0:listN); dos.writeString((String)obj); break;
		}
		BlockGuiHandler.sendPacketToServer(dos);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mx, int my) {
		super.drawGuiContainerForegroundLayer(mx, my);
		this.drawInfo(136, 134, 36, 10, "\\i", "security.add");
		this.drawInfo(136, 69, 18, 18, "\\i", "security.rstL");
		this.drawInfo(154, 69, 18, 18, "\\i", "security.rstP");
		this.drawInfo(155, 36, 16, 12, "\\i", "voltage");
		this.drawInfo(137, 145, 34, 12, "\\i", "security.load");
		this.drawInfo(137, 157, 34, 12, "\\i", "security.admin");
		this.drawInfo(137, 169, 34, 24, "\\i", "security.special");
		this.drawInfo(137, 96, 34, 8, "\\i", "security.costL");
		this.drawInfo(137, 105, 34, 8, "\\i", "security.costP");
		this.drawInfo(137, 16, 16, 52, "Energy:", (int)tile.netF0 + TooltipInfo.getEnergyUnit());
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
		listS = tile.prot.getGroupSize(listN<0 ? 0:listN);
		int n = tile.getStorageScaled(52);
		this.drawTexturedModalRect(this.guiLeft + 137, this.guiTop + 68 - n, 179, 52 - n, 16, n);
		n = tile.netI2 & 3;
		this.drawTexturedModalRect(this.guiLeft + 154, this.guiTop + 69, 213, 18 * n, 18, 18);
		n = tile.netI2 >> 2 & 3;
		this.drawTexturedModalRect(this.guiLeft + 136, this.guiTop + 69, 195, 18 * n, 18, 18);
		n = listN;
		this.drawTexturedModalRect(this.guiLeft + 137, this.guiTop + 157 + 12 * n, 179, 72, 34, 12);
		n = listS <= 6 ? 0 : scroll * 36 / (listS - 6);
		this.drawTexturedModalRect(this.guiLeft + 8, this.guiTop + 145 + n, 179, 52, 8, 12);
		n = listS > 6 ? 6 : listS;
		for (int i = 0; i < n; i++) this.drawTexturedModalRect(this.guiLeft + 18, this.guiTop + 145 + i * 8, 187, 52, 8, 8);
		drawArea(this.guiLeft + 8, this.guiTop + 16);
		this.drawTexturedModalRect(this.guiLeft + 62 + ((tile.getPos().getX() + 8) & 15), this.guiTop + 70 + ((tile.getPos().getZ() + 8) & 15), 179, 64, 4, 4);
		this.drawStringCentered(tile.getName(), this.guiLeft + this.xSize / 2, this.guiTop + 4, 0x404040);
		this.drawStringCentered(tile.netI0 + "V", this.guiLeft + 163, this.guiTop + 38, 0x404040);
		this.drawStringCentered("Power:", this.guiLeft + 154, this.guiTop + 89, 0x404040);
		this.drawStringCentered("L:" + tile.netI3 + TooltipInfo.getPowerUnit(), this.guiLeft + 154, this.guiTop + 97, 0x405757);
		this.drawStringCentered("P:" + tile.netI1 + TooltipInfo.getPowerUnit(), this.guiLeft + 154, this.guiTop + 106, 0x604040);
		this.drawStringCentered("Load", this.guiLeft + 154, this.guiTop + 147, 0x404040);
		this.drawStringCentered("Owner", this.guiLeft + 154, this.guiTop + 159, 0x404040);
		this.drawStringCentered("List 1", this.guiLeft + 154, this.guiTop + 171, 0x404040);
		this.drawStringCentered("List 2", this.guiLeft + 154, this.guiTop + 183, 0x404040);
		this.drawStringCentered("N", this.guiLeft + 72, this.guiTop + 16, 0x404040);
		this.drawStringCentered("S", this.guiLeft + 72, this.guiTop + 136, 0x404040);
		this.fontRendererObj.drawString("W", this.guiLeft + 8, this.guiTop + 76, 0x404040);
		this.fontRendererObj.drawString("E", this.guiLeft + 130, this.guiTop + 76, 0x404040);
		for (int i = 0; i < n; i++) {
			String s = tile.prot.getPlayer(i + scroll, listN<0 ? 0:listN);
			this.fontRendererObj.drawString(s, this.guiLeft + 27, this.guiTop + 146 + i * 8, 0x404040);
		}
		super.drawGuiContainerBackgroundLayer(var1, var2, var3);
	}
	
	private void drawArea(int x, int y)
	{
		if (listN >= 0) {
			byte[] area = tile.prot.groups[listN].area;
			for (int i = 0; i < 8; i++)
				for (int j = 0; j < 8; j++) {
					byte n = area[i + j * 8];
					if (n > 0 && n < 4) this.drawTexturedModalRect(x + i * 16, y + j * 16, 240, n * 16 - 16, 16, 16);
				}
		} else {
			for (int i = 0; i < 64; i++) 
				if ((tile.prot.loadedChunks >> i & 1) != 0)
					this.drawTexturedModalRect(x + (i % 8) * 16, y + (i / 8) * 16, 240, 48, 16, 16);
		}
		
	}
	
	@Override
	public void handleMouseInput() throws IOException 
	{
		this.scroll -= Mouse.getDWheel() / 120;
		if (this.scroll > listS - 6) this.scroll = listS - 6;
		if (this.scroll < 0) this.scroll = 0;
		super.handleMouseInput();
	}

	@Override
	protected void mouseClicked(int x, int y, int b) throws IOException 
	{
		super.mouseClicked(x, y, b);
		if (this.isPointInRegion(155, 16, 16, 10, x, y)) this.sendButtonClick(b==0 ? 5 : 7);//+10
		else if (this.isPointInRegion(155, 26, 16, 10, x, y)) this.sendButtonClick(b==0 ? 4 : 6);//++
		else if (this.isPointInRegion(155, 48, 16, 10, x, y)) this.sendButtonClick(b==0 ? 0 : 2);//--
		else if (this.isPointInRegion(155, 58, 16, 10, x, y)) this.sendButtonClick(b==0 ? 1 : 3);//-10
		else if (this.isPointInRegion(154, 69, 18, 18, x, y)) this.sendButtonClick(8);//mode Prot
		else if (this.isPointInRegion(136, 69, 18, 18, x, y)) this.sendButtonClick(9);//mode Load
		else if (this.isPointInRegion(137, 145, 34, 48, x, y)) {
			int n = (y - this.guiTop - 145) / 12;
			listN = n - 1;
			if (listN > 2) listN = 2;
			if (listN < -1) listN = -1;
			listS = tile.prot.groups[listN<0 ? 0:listN].members.size();
			if (this.scroll > listS - 6) this.scroll = listS - 6;
			if (this.scroll < 0) this.scroll = 0;
		} else if (this.isPointInRegion(136, 134, 36, 10, x, y)) {
			
		} else if (this.isPointInRegion(18, 145, 8, 48, x, y)) {
			int n = (y - this.guiTop - 145) / 8 + scroll;
			if (n >= 0 && n < listS) sendPlayerDelete(n, listN<0 ? 0:listN);
		} else if (this.isPointInRegion(8, 16, 128, 128, x, y)) {
			int dx = (x - this.guiLeft - 8) / 16;
			int dy = (y - this.guiTop - 16) / 16;
			int n = dx + dy * 8;
			if (n >= 0 && n < 64) sendAreaChange(n, listN);
		}
	}

	private void sendButtonClick(int b) throws IOException
	{
			PacketBuffer dos = tile.getPacketTargetData();
			dos.writeByte(0);
			dos.writeByte(b);
			BlockGuiHandler.sendPacketToServer(dos);
	}
	
	private void sendPlayerDelete(int p, int g) throws IOException
	{
		PacketBuffer dos = tile.getPacketTargetData();
			dos.writeByte(1);
			dos.writeByte(g);
			dos.writeByte(p);
		BlockGuiHandler.sendPacketToServer(dos);
	}

	private void sendAreaChange(int i, int g) throws IOException
	{
		PacketBuffer dos = tile.getPacketTargetData();
			dos.writeByte(3);
			dos.writeByte(g);
			dos.writeByte(i);
			BlockGuiHandler.sendPacketToServer(dos);
	}
	
}
