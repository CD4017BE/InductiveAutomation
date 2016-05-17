/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cd4017be.automation.Gui;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

import cd4017be.automation.TileEntity.Pump;
import cd4017be.lib.BlockGuiHandler;
import cd4017be.lib.TileContainer;
import cd4017be.lib.templates.AutomatedTile;
import cd4017be.lib.templates.GuiMachine;

/**
 *
 * @author CD4017BE
 */
public class GuiPump extends GuiMachine
{
    
    private final Pump tileEntity;
    
    public GuiPump(Pump tileEntity, EntityPlayer player)
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
    protected void drawGuiContainerForegroundLayer(int mx, int my) 
    {
        super.drawGuiContainerForegroundLayer(mx, my);
        this.drawInfo(183, 73, 18, 18, "\\i", "pump.update");
        this.drawInfo(201, 73, 18, 18, "\\i", "pump.range");
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) 
    {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.renderEngine.bindTexture(new ResourceLocation("automation", "textures/gui/pump.png"));
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
        if ((tileEntity.netData.ints[0] & 0x100) != 0) this.drawTexturedModalRect(this.guiLeft + 183, this.guiTop + 73, 226, 0, 18, 18);
        this.drawStringCentered(StatCollector.translateToLocal("container.inventory"), this.guiLeft + 88, this.guiTop + 4, 0x404040);
        this.drawStringCentered(tileEntity.getName(), this.guiLeft + 201, this.guiTop + 4, 0x404040);
        int n = tileEntity.netData.ints[0] & 0xff;
        this.drawStringCentered(n == 0 ? "off" : "" + n, this.guiLeft + 210, this.guiTop + 78, 0x404040);
        super.drawGuiContainerBackgroundLayer(var1, var2, var3);
    }

    @Override
    protected void mouseClicked(int x, int y, int b) throws IOException 
    {
        super.mouseClicked(x, y, b);
        int i = -1, n = 0;
        if (this.isPointInRegion(183, 73, 18, 18, x, y)) {
            i = 0;
        } else if (this.isPointInRegion(201, 73, 18, 8, x, y)) {
            i = 1;
            n = (tileEntity.netData.ints[0] & 0xff) + (b == 0 ? 1 : 8);
            if (n > 127) n = 127;
        } else if (this.isPointInRegion(201, 83, 18, 8, x, y)) {
            i = 1;
            n = (tileEntity.netData.ints[0] & 0xff) - (b == 0 ? 1 : 8);
            if (n < 0) n = 0;
        }
        if (i >= 0)
        {
            PacketBuffer dos = tileEntity.getPacketTargetData();
            dos.writeByte(AutomatedTile.CmdOffset + i);
            if (i == 1) dos.writeByte((byte)n);
            BlockGuiHandler.sendPacketToServer(dos);
        }
    }
    
}
