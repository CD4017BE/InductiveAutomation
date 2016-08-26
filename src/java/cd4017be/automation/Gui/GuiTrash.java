/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cd4017be.automation.Gui;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;

import org.lwjgl.opengl.GL11;

import cd4017be.automation.TileEntity.Trash;
import cd4017be.lib.BlockGuiHandler;
import cd4017be.lib.Gui.GuiMachine;
import cd4017be.lib.Gui.TileContainer;
import cd4017be.lib.templates.AutomatedTile;

/**
 *
 * @author CD4017BE
 */
public class GuiTrash extends GuiMachine
{
    private final Trash tileEntity;
    
    public GuiTrash(Trash tileEntity, EntityPlayer player)
    {
        super(new TileContainer(tileEntity, player));
        this.tileEntity = tileEntity;
    }
    
    @Override
    public void initGui() 
    {
        this.xSize = 176;
        this.ySize = 132;
        super.initGui();
    }
    
    @Override
    protected void drawGuiContainerBackgroundLayer(float f, int i, int j) 
    {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.renderEngine.bindTexture(new ResourceLocation("automation", "textures/gui/trash.png"));
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
        int n = tileEntity.netData.ints[0];
        if ((n & 1) == 0) this.drawTexturedModalRect(this.guiLeft + 115, this.guiTop + 15, 176, 0, 18, 18);
        if ((n & 2) == 0) this.drawTexturedModalRect(this.guiLeft + 43, this.guiTop + 15, 176, 0, 18, 18);
        this.drawStringCentered(tileEntity.getName(), this.guiLeft + this.xSize / 2, this.guiTop + 4, 0x404040);
        this.drawStringCentered(I18n.translateToLocal("container.inventory"), this.guiLeft + this.xSize / 2, this.guiTop + 36, 0x404040);
        super.drawGuiContainerBackgroundLayer(f, i, j);
    }
    
    @Override
    protected void mouseClicked(int x, int y, int b) throws IOException 
    {
        super.mouseClicked(x, y, b);
        byte cmd = -1;
        if (this.isPointInRegion(115, 15, 18, 18, x, y))
        {
            cmd = 0;
        } else
        if (this.isPointInRegion(43, 15, 18, 18, x, y))
        {
            cmd = 1;
        }
        if (cmd >= 0) {
            PacketBuffer dos = tileEntity.getPacketTargetData();
            dos.writeByte(AutomatedTile.CmdOffset + cmd);
            BlockGuiHandler.sendPacketToServer(dos);
        }
    }
}
