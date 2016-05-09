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

import cd4017be.automation.TileEntity.Collector;
import cd4017be.lib.BlockGuiHandler;
import cd4017be.lib.TileContainer;
import cd4017be.lib.templates.AutomatedTile;
import cd4017be.lib.templates.GuiMachine;

/**
 *
 * @author CD4017BE
 */
public class GuiCollector extends GuiMachine
{
    private final Collector tileEntity;
    
    public GuiCollector(Collector tileEntity, EntityPlayer player)
    {
        super(new TileContainer(tileEntity, player));
        this.tileEntity = tileEntity;
    }
    
    @Override
    public void initGui() 
    {
        this.xSize = 226;
        this.ySize = 98;
        super.initGui();
    }
    
    @Override
    protected void drawGuiContainerBackgroundLayer(float f, int i, int j) 
    {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.renderEngine.bindTexture(new ResourceLocation("automation", "textures/gui/collector.png"));
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
        int n = tileEntity.netData.ints[0];
        if (n > 0) this.drawTexturedModalRect(this.guiLeft + 183, this.guiTop + 73, 238, n * 18 - 18, 18, 18);
        this.drawLiquidTank(tileEntity.tanks, 0, 184, 16, true);
        this.drawLiquidConfig(tileEntity, -18, 7);
        fontRendererObj.drawString(tileEntity.getName(), this.guiLeft + this.xSize - 8 - fontRendererObj.getStringWidth(tileEntity.getName()), this.guiTop + 4, 0x404040);
        fontRendererObj.drawString(I18n.translateToLocal("container.inventory"), this.guiLeft + 8, this.guiTop + 4, 0x404040);
    }
    
    @Override
    protected void mouseClicked(int x, int y, int b) throws IOException 
    {
        super.mouseClicked(x, y, b);
        this.clickLiquidConfig(tileEntity, x - this.guiLeft + 18, y - this.guiTop - 7);
        if (this.isPointInRegion(183, 73, 18, 18, x, y))
        {
            PacketBuffer dos = tileEntity.getPacketTargetData();
            dos.writeByte(AutomatedTile.CmdOffset);
            BlockGuiHandler.sendPacketToServer(dos);
        }
    }
    
}
