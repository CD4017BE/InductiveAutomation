/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cd4017be.automation.Gui;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.I18n;

import org.lwjgl.opengl.GL11;

import cd4017be.automation.Config;
import cd4017be.automation.TileEntity.ESU;
import cd4017be.lib.BlockGuiHandler;
import cd4017be.lib.TileContainer;
import cd4017be.lib.TooltipInfo;
import cd4017be.lib.templates.AutomatedTile;
import cd4017be.lib.templates.GuiMachine;

/**
 *
 * @author CD4017BE
 */
public class GuiESU extends GuiMachine
{
    protected ESU tileEntity;
    
    public GuiESU(ESU tileEntity, EntityPlayer player)
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
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mx, int my) 
    {
        super.drawGuiContainerForegroundLayer(mx, my);
        double I = tileEntity.netData.floats[1] / (tileEntity.netData.floats[1] <= 0 ? -(float)tileEntity.netData.ints[0] : (float)Math.sqrt((double)tileEntity.netData.ints[0] * (double)tileEntity.netData.ints[0] + (double)tileEntity.netData.floats[1]));
        this.drawFormatInfo(8, 46, 160, 4, "esu.energyFlow", tileEntity.netData.floats[1] / 1000F, I);
        this.drawInfo(28, 16, 30, 16, "\\i", "voltage");
    }
    
    @Override
    protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) 
    {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.renderEngine.bindTexture(new ResourceLocation("automation", "textures/gui/ESU.png"));
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
        int n = tileEntity.getStorageScaled(160);
        this.drawTexturedModalRect(this.guiLeft + 8, this.guiTop + 52, 0, 240, n, 16);
        n = tileEntity.getDiff(80);
        if (n > 0){
            this.drawTexturedModalRect(this.guiLeft + 88, this.guiTop + 46, 80, 236, n, 4);
        } else {
            this.drawTexturedModalRect(this.guiLeft + 88 + n, this.guiTop + 46, 80 + n, 236, -n, 4);
        }
        this.drawStringCentered(tileEntity.getName(), this.guiLeft + this.xSize / 2, this.guiTop + 4, 0x404040);
        this.drawStringCentered(I18n.translateToLocal("container.inventory"), this.guiLeft + this.xSize / 2, this.guiTop + 72, 0x404040);
        this.drawStringCentered(tileEntity.netData.ints[0] + "V", this.guiLeft + 43, this.guiTop + 20, 0x404040);
        this.drawStringCentered((int)(tileEntity.netData.floats[0] / 1000F) + "/" + tileEntity.getMaxStorage() + " " + TooltipInfo.getEnergyUnit(), this.guiLeft + 88, this.guiTop + 56, 0x404040);
        super.drawGuiContainerBackgroundLayer(var1, var2, var3);
    }
    
    @Override
    protected void mouseClicked(int x, int y, int b) throws IOException 
    {
        byte a = -1;
        if (this.isPointInRegion(8, 16, 10, 16, x, y))
        {
            tileEntity.netData.ints[0] -= b == 0 ? 10 : 1000;
            a = 0;
        } else
        if (this.isPointInRegion(18, 16, 10, 16, x, y))
        {
            tileEntity.netData.ints[0] -= b == 0 ? 1 : 100;
            a = 0;
        } else
        if (this.isPointInRegion(58, 16, 10, 16, x, y))
        {
            tileEntity.netData.ints[0] += b == 0 ? 1 : 100;
            a = 0;
        } else
        if (this.isPointInRegion(68, 16, 10, 16, x, y))
        {
            tileEntity.netData.ints[0] += b == 0 ? 10 : 1000;
            a = 0;
        }
        if (a >= 0)
        {
            if (tileEntity.netData.ints[0] < 0) tileEntity.netData.ints[0] = 0;
            if (tileEntity.netData.ints[0] > Config.Umax[tileEntity.type]) tileEntity.netData.ints[0] = Config.Umax[tileEntity.type];
            PacketBuffer dos = tileEntity.getPacketTargetData();
            dos.writeByte(AutomatedTile.CmdOffset);
            dos.writeInt(tileEntity.netData.ints[0]);
            BlockGuiHandler.sendPacketToServer(dos);
        }
        super.mouseClicked(x, y, b);
    }
    
}
