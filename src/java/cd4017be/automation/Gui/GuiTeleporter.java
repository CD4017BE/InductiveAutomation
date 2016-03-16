/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cd4017be.automation.Gui;

import java.io.IOException;

import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import cd4017be.automation.TileEntity.Teleporter;
import cd4017be.lib.BlockGuiHandler;
import cd4017be.lib.TileContainer;
import cd4017be.lib.templates.AutomatedTile;
import cd4017be.lib.templates.GuiMachine;

/**
 *
 * @author CD4017BE
 */
public class GuiTeleporter extends GuiMachine
{
    
    private final Teleporter tileEntity;
    private GuiTextField tfX;
    private GuiTextField tfY;
    private GuiTextField tfZ;
    private GuiTextField name;
    
    private byte warned = 0;
    
    public GuiTeleporter(Teleporter tileEntity, EntityPlayer player)
    {
        super(new TileContainer(tileEntity, player));
        this.tileEntity = tileEntity;
    }
    
    @Override
    public void initGui() 
    {
        this.xSize = 176;
        this.ySize = 168;
        super.initGui();
        tfX = new GuiTextField(0, fontRendererObj, this.guiLeft + 8, this.guiTop + 52, 34, 16);
        tfY = new GuiTextField(1, fontRendererObj, this.guiLeft + 44, this.guiTop + 52, 34, 16);
        tfZ = new GuiTextField(2, fontRendererObj, this.guiLeft + 80, this.guiTop + 52, 34, 16);
        name = new GuiTextField(3, fontRendererObj, this.guiLeft + 134, this.guiTop + 34, 34, 16);
    }

    @Override
    public void updateScreen() 
    {
        super.updateScreen();
        tfX.updateCursorCounter();
        tfY.updateCursorCounter();
        tfZ.updateCursorCounter();
        name.updateCursorCounter();
        if (!tfX.isFocused()) tfX.setText("" + tileEntity.netData.ints[0]);
        if (!tfY.isFocused()) tfY.setText("" + tileEntity.netData.ints[1]);
        if (!tfZ.isFocused()) tfZ.setText("" + tileEntity.netData.ints[2]);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mx, int my) 
    {
        super.drawGuiContainerForegroundLayer(mx, my);
        this.drawFormatInfo(8, 34, 88, 16, "storage", (long)Math.floor(tileEntity.netData.floats[0] / 1000F), (long)Math.floor(tileEntity.netData.floats[1] / 1000F));
        this.drawInfo(134, 52, 16, 16, "\\i", "teleport.rw");
        this.drawInfo(98, 34, 16, 16, "\\i", "rstCtr");
        this.drawInfo(116, 34, 16, 16, "\\i", "teleport." + ((tileEntity.netData.ints[3] & 2) == 0 ? "abs" : "rel"));
        this.drawInfo(134, 34, 34, 16, "\\i", "teleport.name");
        this.drawInfo(8, 52, 34, 16, "X");
        this.drawInfo(44, 52, 34, 16, "Y");
        this.drawInfo(80, 52, 34, 16, "Z");
    }
    
    @Override
    protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) 
    {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.renderEngine.bindTexture(new ResourceLocation("automation", "textures/gui/teleporter.png"));
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
        int n = tileEntity.getStorageScaled(88);
        this.drawTexturedModalRect(this.guiLeft + 8, this.guiTop + 34, 0, 240, n, 16);
        if ((tileEntity.netData.ints[3] & 1) == 0) this.drawTexturedModalRect(this.guiLeft + 98, this.guiTop + 34, 176, 0, 16, 16);
        this.drawItemConfig(tileEntity, -18, 7);
        this.drawEnergyConfig(tileEntity, -36, 7);
        this.drawStringCentered((tileEntity.netData.ints[3] & 2) == 0 ? "abs" : "rel", this.guiLeft + 124, this.guiTop + 38, 0x404040);
        this.drawLocString(this.guiLeft + 9, this.guiTop + 38, 0, 0x404040, "teleport." + ((tileEntity.netData.ints[3] & 4) == 0 ? "teleport" : "charge"));
        tfX.drawTextBox();
        tfY.drawTextBox();
        tfZ.drawTextBox();
        name.drawTextBox();
        this.drawStringCentered(tileEntity.getName(), this.guiLeft + this.xSize / 4, this.guiTop + 4, 0x404040);
        this.drawStringCentered(StatCollector.translateToLocal("container.inventory"), this.guiLeft + this.xSize / 2, this.guiTop + 72, 0x404040);
        this.drawStringCentered(StatCollector.translateToLocal("gui.cd4017be.teleport." + ((tileEntity.netData.ints[3] & 16) != 0 ?"copy":"move")), this.guiLeft + this.xSize * 3 / 4, this.guiTop + 4, 0xff4040);
        showWarning();
    }
    
    private void showWarning() {
        if (!tileEntity.isInWorldBounds()) {
        	this.drawStringCentered(StatCollector.translateToLocal("gui.cd4017be.teleport.warning"), this.guiLeft + this.xSize / 2, this.guiTop + this.ySize, 0xff8080);
        	if (warned == 0) warned = 1;
        	if (warned == 2) this.drawStringCentered(StatCollector.translateToLocal("gui.cd4017be.teleport.warning2"), this.guiLeft + this.xSize / 2, this.guiTop + this.ySize + 12, 0xffc040);
        } else warned = 0;
    }

    @Override
    protected void mouseClicked(int x, int y, int b) throws IOException 
    {
        super.mouseClicked(x, y, b);
        tfX.mouseClicked(x, y, b);
        tfY.mouseClicked(x, y, b);
        tfZ.mouseClicked(x, y, b);
        name.mouseClicked(x, y, b);
        int kb = -1;
        this.clickItemConfig(tileEntity, x - this.guiLeft + 18, y - this.guiTop - 7);
        this.clickEnergyConfig(tileEntity, x - this.guiLeft + 36, y - this.guiTop - 7);
        if (this.isPointInRegion(7, 33, 90, 18, x, y)) {
            if (warned == 1 && (tileEntity.netData.ints[3] & 4) == 0) warned = 2;
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
            PacketBuffer dos = tileEntity.getPacketTargetData();
            dos.writeByte(AutomatedTile.CmdOffset);
            dos.writeByte(kb);
            BlockGuiHandler.sendPacketToServer(dos);
        }
    }

    @Override
    protected void keyTyped(char c, int k) throws IOException 
    {
        if (!tfX.isFocused() && !tfY.isFocused() && !tfZ.isFocused() && !name.isFocused())super.keyTyped(c, k);
        tfX.textboxKeyTyped(c, k);
        tfY.textboxKeyTyped(c, k);
        tfZ.textboxKeyTyped(c, k);
        name.textboxKeyTyped(c, k);
        if (k == Keyboard.KEY_RETURN)
        {
            int i = -1;
            GuiTextField tf = null;
            if (tfX.isFocused())
            {
                i = 1;
                tf = tfX;
            } else
            if (tfY.isFocused())
            {
                i = 2;
                tf = tfY;
            } else
            if (tfZ.isFocused())
            {
                i = 3;
                tf = tfZ;
            } else
            if (name.isFocused())
            {
                i = 4;
                tf = name;
            }
            if (tf != null)
            try
            {
                int n = (i == 4 ? 0 : Integer.parseInt(tf.getText()));
                PacketBuffer dos = tileEntity.getPacketTargetData();
                dos.writeByte(i + AutomatedTile.CmdOffset);
                if (i != 4) dos.writeInt(n);
                else dos.writeString(tf.getText());
                BlockGuiHandler.sendPacketToServer(dos);
            } catch (NumberFormatException e) {}
        }
    }
    
}
