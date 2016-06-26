/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cd4017be.automation.Gui;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

import cd4017be.automation.TileEntity.Detector;
import cd4017be.lib.BlockGuiHandler;
import cd4017be.lib.TileContainer;
import cd4017be.lib.templates.AutomatedTile;
import cd4017be.lib.templates.GuiMachine;

/**
 *
 * @author CD4017BE
 */
public class GuiDetector extends GuiMachine
{
    
    private final Detector tileEntity;
    private int sel = -1;
    private TextField text = new TextField("", 11);
    
    public GuiDetector(Detector tileEntity, EntityPlayer player)
    {
        super(new TileContainer(tileEntity, player));
        this.tileEntity = tileEntity;
    }

    @Override
    public void initGui() 
    {
        this.xSize = 176;
        this.ySize = 222;
        super.initGui();
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mx, int my) 
    {
        super.drawGuiContainerForegroundLayer(mx, my);
        this.drawInfo(7, 15, 18, 108, "\\i", "detector.obj" + tileEntity.getConfig((my - guiTop - 15) / 18));
        this.drawInfo(25, 15, 18, 108, "\\i", "detector.dir");
        this.drawInfo(43, 15, 18, 108, "\\i", "detector.filter");
        this.drawInfo(71, 16, 61, 8, "\\i", "detector.refH");
        this.drawInfo(71, 24, 61, 8, "\\i", "detector.refL");
        this.drawInfo(142, 15, 27, 108, "\\i", "detector.out");
        if (this.isPointInRegion(8, 16, 160, 106, mx, my)) 
        	this.drawSideCube(-64, 16, (my - guiTop - 15) / 18, (byte)0);
    }
    
    @Override
    protected void drawGuiContainerBackgroundLayer(float f, int mx, int my) 
    {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.renderEngine.bindTexture(new ResourceLocation("automation", "textures/gui/detector.png"));
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
        for (int i = 0; i < 6; i++) {
            this.drawTexturedModalRect(this.guiLeft + 7, this.guiTop + 15 + i * 18, 176, 18 * (tileEntity.getConfig(i) & 3), 18, 18);
            this.drawTexturedModalRect(this.guiLeft + 25, this.guiTop + 15 + i * 18, 194, 18 * (tileEntity.getConfig(i + 8) % 6), 18, 18);
            int n = (tileEntity.getState(i) >> 4 & 0xf) + 1;
            this.drawTexturedModalRect(this.guiLeft + 142, this.guiTop + 32 - n + i * 18, 212, 16 - n, 8, n);
        }
        int x = guiLeft + 71, y;
        boolean b;
        for (int i = 0; i < 12; i++) {
        	b = i % 2 == 0;
        	y = guiTop + i * 9 + (b ? 16 : 15);
        	if (i == sel) text.draw(x, y, b ? 0xffff0000 : 0xff0000ff, 0xff00ff00);
        	else this.fontRendererObj.drawString("" + tileEntity.netData.ints[i], x, y, b ? 0xff0000 : 0x0000ff);
        }
        this.drawStringCentered(tileEntity.getName(), this.guiLeft + this.xSize / 2, this.guiTop + 4, 0x404040);
        this.drawStringCentered(StatCollector.translateToLocal("container.inventory"), this.guiLeft + this.xSize / 2, this.guiTop + 126, 0x404040);
    }

	@Override
    protected void mouseClicked(int x, int y, int b) throws IOException 
    {
        super.mouseClicked(x, y, b);
		byte cmd = -1;
        byte mode = 0;
		int s = (y - this.guiTop - 15) / 18;
        if (s < 0 || s >= 6) s = -1;
        else y -= s * 18;
        if (this.isPointInRegion(7, 15, 18, 18, x, y)) {
            cmd = 1;
            mode = tileEntity.getConfig(s);
            mode = (byte)((mode & 4) | (mode + 1 & 3));
        } else if (this.isPointInRegion(25, 15, 18, 18, x, y)) {
            cmd = 2;
            mode = tileEntity.getConfig(s + 8);
            mode = (byte)((mode + 1) % 6);
        }
        if (cmd > 0) {
            PacketBuffer dos = tileEntity.getPacketTargetData();
            dos.writeByte(cmd + AutomatedTile.CmdOffset);
            dos.writeByte(s);
            dos.writeByte(mode);
            BlockGuiHandler.sendPacketToServer(dos);
        }
        if (this.isPointInRegion(71, 16, 61, 8, x, y)) s *= 2;
        else if (this.isPointInRegion(71, 24, 61, 8, x, y)) s = s * 2 + 1;
        else s = -1;
        if (s != sel) this.setTextField(s);
    }

	@Override
	protected void keyTyped(char c, int k) throws IOException 
	{
		if (sel >= 0) {
    		byte r = text.keyTyped(c, k);
    		if (r == 1) this.setTextField(-1);
    		else if (r >= 0) this.setTextField((sel + r + 11) % 12);
    	} else super.keyTyped(c, k);
	}
    
	private void setTextField(int k) throws IOException {
		if (k == sel) return;
		if (sel >= 0) try {
			tileEntity.netData.ints[sel] = Integer.parseInt(text.text);
			this.sendCurrentChange();
		} catch(NumberFormatException e) {}
		if (k >= 0) {
			text.text = "" + tileEntity.netData.ints[k];
			if (sel < 0 || text.cur > text.text.length()) text.cur = text.text.length();
		}
		sel = k;
	}
	
	private void sendCurrentChange() throws IOException {
		PacketBuffer dos = tileEntity.getPacketTargetData();
		dos.writeByte(AutomatedTile.CmdOffset);
		dos.writeByte(sel);
		dos.writeInt(tileEntity.netData.ints[sel]);
		BlockGuiHandler.sendPacketToServer(dos);
	}
    
}
