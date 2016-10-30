package cd4017be.automation.Gui;

import java.io.IOException;

import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import cd4017be.automation.TileEntity.Teleporter;
import cd4017be.lib.BlockGuiHandler;
import cd4017be.lib.Gui.GuiMachine;
import cd4017be.lib.Gui.TileContainer;
import cd4017be.lib.templates.AutomatedTile;

/**
 *
 * @author CD4017BE
 */
public class GuiTeleporter extends GuiMachine {

	private final Teleporter tile;
	private byte warned = 0;

	public GuiTeleporter(Teleporter tileEntity, EntityPlayer player) {
		super(new TileContainer(tileEntity, player));
		this.tile = tileEntity;
		this.MAIN_TEX = new ResourceLocation("automation", "textures/gui/teleporter.png");
	}

	@Override
	public void initGui() {
		this.xSize = 176;
		this.ySize = 168;
		super.initGui();
		guiComps.add(new TextField(2, 8, 52, 34, 16, 8));
		guiComps.add(new TextField(3, 44, 52, 34, 16, 8));
		guiComps.add(new TextField(4, 80, 52, 34, 16, 8));
		guiComps.add(new TextField(5, 134, 34, 34, 16, 16).setTooltip("teleport.name"));
	}

	@Override
	protected Object getDisplVar(int id) {
		switch(id) {
		case 2: return "" + tile.tgX;
		case 3: return "" + tile.tgY;
		case 4: return "" + tile.tgZ;
		case 5: return "";
		
		default: return null;
		}
	}

	@Override
	protected void setDisplVar(int id, Object obj, boolean send) {
		PacketBuffer dos = tile.getPacketTargetData();
		switch(id) {
		case 2: try{dos.writeByte(AutomatedTile.CmdOffset + 1).writeInt(Integer.parseInt((String)obj)); break;} catch (NumberFormatException e) {return;}
		case 3: try{dos.writeByte(AutomatedTile.CmdOffset + 2).writeInt(Integer.parseInt((String)obj)); break;} catch (NumberFormatException e) {return;}
		case 4: try{dos.writeByte(AutomatedTile.CmdOffset + 3).writeInt(Integer.parseInt((String)obj)); break;} catch (NumberFormatException e) {return;}
		case 5: dos.writeByte(AutomatedTile.CmdOffset + 4); dos.writeString((String)obj); break;
		}
		if(send) BlockGuiHandler.sendPacketToServer(dos);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mx, int my) {
		super.drawGuiContainerForegroundLayer(mx, my);
		this.drawFormatInfo(8, 34, 88, 16, "storage", (long)Math.floor(tile.netF0 / 1000F), (long)Math.floor(tile.netF1 / 1000F));
		this.drawInfo(134, 52, 16, 16, "\\i", "teleport.rw");
		this.drawInfo(98, 34, 16, 16, "\\i", "rstCtr");
		this.drawInfo(116, 34, 16, 16, "\\i", "teleport." + ((tile.netI3 & 2) == 0 ? "abs" : "rel"));
		if (this.isPointInRegion(8, 52, 34, 16, mx, my)) this.drawSideCube(-64, tabsY + 63, 5, (byte)3);
		else if (this.isPointInRegion(44, 52, 34, 16, mx, my)) this.drawSideCube(-64, tabsY + 63, 1, (byte)3);
		else if (this.isPointInRegion(80, 52, 34, 16, mx, my)) this.drawSideCube(-64, tabsY + 63, 3, (byte)3);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) 
	{
		int n = tile.getStorageScaled(88);
		this.drawTexturedModalRect(this.guiLeft + 8, this.guiTop + 34, 0, 240, n, 16);
		if ((tile.netI3 & 1) == 0) this.drawTexturedModalRect(this.guiLeft + 98, this.guiTop + 34, 176, 0, 16, 16);
		this.drawStringCentered((tile.netI3 & 2) == 0 ? "abs" : "rel", this.guiLeft + 124, this.guiTop + 38, 0x404040);
		this.drawLocString(this.guiLeft + 9, this.guiTop + 38, 0, 0x404040, "teleport." + ((tile.netI3 & 4) == 0 ? "teleport" : "charge"));
		this.drawStringCentered(I18n.translateToLocal("gui.cd4017be.teleport." + ((tile.netI3 & 16) != 0 ?"copy":"move")), this.guiLeft + this.xSize * 3 / 4, this.guiTop + 4, 0xff4040);
		showWarning();
		super.drawGuiContainerBackgroundLayer(var1, var2, var3);
	}
	
	private void showWarning() {
		if (!tile.isInWorldBounds()) {
			this.drawStringCentered(I18n.translateToLocal("gui.cd4017be.teleport.warning"), this.guiLeft + this.xSize / 2, this.guiTop + this.ySize, 0xff8080);
			if (warned == 0) warned = 1;
			if (warned == 2) this.drawStringCentered(I18n.translateToLocal("gui.cd4017be.teleport.warning2"), this.guiLeft + this.xSize / 2, this.guiTop + this.ySize + 12, 0xffc040);
		} else warned = 0;
	}

	@Override
	protected void mouseClicked(int x, int y, int b) throws IOException 
	{
		super.mouseClicked(x, y, b);
		int kb = -1;
		if (this.isPointInRegion(7, 33, 90, 18, x, y)) {
			if (warned == 1 && (tile.netI3 & 4) == 0) warned = 2;
			else kb = 0;
		} else
		if (this.isPointInRegion(97, 33, 18, 18, x, y))
		{
			kb = 1;
		} else
		if (this.isPointInRegion(115, 33, 18, 18, x, y))
		{
			kb = 2;
		} else
		if (this.isPointInRegion(133, 51, 18, 18, x, y))
		{
			kb = 3;
		} else
		if (this.isPointInRegion(this.xSize / 2, 4, this.xSize / 2, 8, x, y))
		{
			kb = 4;
		}
		if (kb >= 0)
		{
			PacketBuffer dos = tile.getPacketTargetData();
			dos.writeByte(AutomatedTile.CmdOffset);
			dos.writeByte(kb);
			BlockGuiHandler.sendPacketToServer(dos);
		}
	}

}
