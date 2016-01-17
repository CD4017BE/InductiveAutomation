/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cd4017be.automation.Gui;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import org.lwjgl.input.Keyboard;
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
    private GuiTextField[] values = new GuiTextField[6];
    
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
        for (int i = 0; i < 6; i++) values[i] = new GuiTextField(fontRendererObj, this.guiLeft + 80, this.guiTop + 16 + i * 18, 70, 16);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mx, int my) 
    {
        super.drawGuiContainerForegroundLayer(mx, my);
        this.drawInfo(7, 15, 18, 108, "\\i", "detector.obj");
        this.drawInfo(25, 15, 18, 108, "\\i", "detector.dir");
        this.drawInfo(43, 15, 18, 108, "\\i", "detector.filter");
        this.drawInfo(65, 15, 10, 108, "\\i", "detector.comp");
        this.drawInfo(100, 15, 30, 108, "\\i", "detector.ref");
        this.drawInfo(152, 15, 16, 108, "\\i", "detector.out");
    }
    
    @Override
    protected void drawGuiContainerBackgroundLayer(float f, int x, int y) 
    {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.renderEngine.bindTexture(new ResourceLocation("automation", "textures/gui/detector.png"));
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
        for (int i = 0; i < 6; i++) {
            this.drawTexturedModalRect(this.guiLeft + 7, this.guiTop + 15 + i * 18, 176, 18 * (tileEntity.getMode(i) & 3), 18, 18);
            this.drawTexturedModalRect(this.guiLeft + 25, this.guiTop + 15 + i * 18, 194, 18 * (tileEntity.getSide(i) % 6), 18, 18);
            if ((tileEntity.getMode(i) & 4) != 0)
                this.drawTexturedModalRect(this.guiLeft + 65, this.guiTop + 15 + i * 18, 212, 0, 10, 18);
            if (tileEntity.getState(i))
                this.drawTexturedModalRect(this.guiLeft + 151, this.guiTop + 15 + i * 18, 212, 18, 10, 18);
        }
        for (int i = 0; i < 6; i++) values[i].drawTextBox();
        this.drawStringCentered(tileEntity.getInventoryName(), this.guiLeft + this.xSize / 2, this.guiTop + 4, 0x404040);
        this.drawStringCentered(StatCollector.translateToLocal("container.inventory"), this.guiLeft + this.xSize / 2, this.guiTop + 126, 0x404040);
    }
    
    @Override
	public void updateScreen() 
    {
		super.updateScreen();
		for (int i = 0; i < 6; i++)
			if (!values[i].isFocused())
				values[i].setText("" + tileEntity.netData.ints[i + 1]);
	}

	@Override
    protected void mouseClicked(int x, int y, int b) 
    {
        super.mouseClicked(x, y, b);
        for (int i = 0; i < 6; i++) values[i].mouseClicked(x, y, b);
        int s = (y - this.guiTop - 15) / 18;
        y -= s * 18;
        byte cmd = -1;
        byte mode = 0;
        if (s >= 0 && s < 6) {
            if (this.func_146978_c(7, 15, 18, 18, x, y)) {
                cmd = 0;
                mode = tileEntity.getMode(s);
                mode = (byte)((mode & 4) | (mode + 1 & 3));
            } else if (this.func_146978_c(25, 15, 18, 18, x, y)) {
                cmd = 2;
                mode = tileEntity.getSide(s);
                mode = (byte)((mode + 1) % 6);
            } else if (this.func_146978_c(65, 15, 10, 18, x, y)) {
                cmd = 0;
                mode = (byte)(tileEntity.getMode(s) ^ 4);
            }
        }
        if (cmd >= 0) {
            if (tileEntity.netData.ints[s + 1] < 0) tileEntity.netData.ints[s + 1] = 0;
            try {
            ByteArrayOutputStream bos = tileEntity.getPacketTargetData();
            DataOutputStream dos = new DataOutputStream(bos);
            dos.writeByte(cmd + AutomatedTile.CmdOffset);
            dos.writeByte(s);
            if (cmd == 0) dos.writeByte(mode);
            else if (cmd == 2) dos.writeByte(mode);
            BlockGuiHandler.sendPacketToServer(bos);
            } catch (IOException e){}
        }
    }

	@Override
	protected void keyTyped(char c, int k) 
	{
		for (int i = 0; i < 6; i++)
			if (values[i].isFocused()) {
				if (k == Keyboard.KEY_RETURN) {
					try {
						int v = Integer.parseInt(values[i].getText());
						ByteArrayOutputStream bos = tileEntity.getPacketTargetData();
			            DataOutputStream dos = new DataOutputStream(bos);
			            dos.writeByte(1 + AutomatedTile.CmdOffset);
			            dos.writeByte(i);
			            dos.writeInt(v);
			            BlockGuiHandler.sendPacketToServer(bos);
						values[i].setFocused(false);
					} catch (NumberFormatException e) {
					} catch (IOException e) {}
				} else values[i].textboxKeyTyped(c, k);
				break;
			}
		super.keyTyped(c, k);
	}
    
    
    
}
