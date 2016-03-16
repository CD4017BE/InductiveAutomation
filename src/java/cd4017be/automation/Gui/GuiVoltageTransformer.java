/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cd4017be.automation.Gui;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import cd4017be.automation.TileEntity.VoltageTransformer;
import cd4017be.lib.BlockGuiHandler;
import cd4017be.lib.TileContainer;
import cd4017be.lib.templates.GuiMachine;

/**
 *
 * @author CD4017BE
 */
public class GuiVoltageTransformer extends GuiMachine
{
    
    private final VoltageTransformer tileEntity;
    
    public GuiVoltageTransformer(VoltageTransformer tileEntity, EntityPlayer player)
    {
        super(new TileContainer(tileEntity, player));
        this.tileEntity = tileEntity;
    }

    @Override
    public void initGui() 
    {
        this.xSize = 176;
        this.ySize = 40;
        super.initGui();
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mx, int my) 
    {
        super.drawGuiContainerForegroundLayer(mx, my);
        this.drawInfo(28, 16, 30, 16, "\\i", "transf");
        this.drawInfo(97, 15, 92, 18, "\\i", "rstCtr");
    }
    
    @Override
    protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) 
    {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.renderEngine.bindTexture(new ResourceLocation("automation", "textures/gui/tesla.png"));
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
        if ((tileEntity.ctrMode & 1) == 0)
        {
            this.drawTexturedModalRect(this.guiLeft + 97, this.guiTop + 15, 176, 18, 36, 18);
            this.drawStringCentered((tileEntity.ctrMode & 2) == 0 ? "Off" : "On", this.guiLeft + 151, this.guiTop + 20, 0x404040);
        } else
        {
            this.drawTexturedModalRect(this.guiLeft + 97, this.guiTop + 15, 176, 0, 36, 18);
            this.drawTexturedModalRect(this.guiLeft + 133, this.guiTop + 15, 212, (tileEntity.ctrMode & 2) == 0 ? 0 : 18, 36, 18);
        }
        this.drawStringCentered(String.format("%.1f x", (float)tileEntity.faktor * 0.1F), this.guiLeft + 43, this.guiTop + 20, 0x404040);
        this.drawStringCentered(tileEntity.getName(), this.guiLeft + this.xSize / 2, this.guiTop + 4, 0x404040);
    }

    @Override
    protected void mouseClicked(int x, int y, int b) throws IOException 
    {
        super.mouseClicked(x, y, b);
        boolean d = false;
        if (this.isPointInRegion(8, 16, 10, 16, x, y))
        {
            tileEntity.faktor -= 10;
            d = true;
        } else
        if (this.isPointInRegion(18, 16, 10, 16, x, y))
        {
            tileEntity.faktor--;
            d = true;
        } else
        if (this.isPointInRegion(58, 16, 10, 16, x, y))
        {
            tileEntity.faktor++;
            d = true;
        } else
        if (this.isPointInRegion(68, 16, 10, 16, x, y))
        {
            tileEntity.faktor += 10;
            d = true;
        } else
        if (this.isPointInRegion(97, 15, 36, 18, x, y))
        {
            tileEntity.ctrMode ^= 1;
            d = true;
        } else
        if (this.isPointInRegion(133, 15, 36, 18, x, y))
        {
            tileEntity.ctrMode ^= 2;
            d = true;
        }
        if (d)
        {
            if (tileEntity.faktor < 10) tileEntity.faktor = 10;
            if (tileEntity.faktor > 200) tileEntity.faktor = 200;
            PacketBuffer dos = tileEntity.getPacketTargetData();
            dos.writeInt(tileEntity.faktor);
            dos.writeByte(tileEntity.ctrMode);
            BlockGuiHandler.sendPacketToServer(dos);
        }
    }
    
}
