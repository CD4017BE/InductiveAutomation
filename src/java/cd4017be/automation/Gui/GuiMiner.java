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

import cd4017be.automation.TileEntity.Miner;
import cd4017be.lib.BlockGuiHandler;
import cd4017be.lib.TileContainer;
import cd4017be.lib.templates.AutomatedTile;
import cd4017be.lib.templates.GuiMachine;

/**
 *
 * @author CD4017BE
 */
public class GuiMiner extends GuiMachine
{
    
    private final Miner tileEntity;
    
    public GuiMiner(Miner tileEntity, EntityPlayer player)
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
        this.drawInfo(117, 52, 16, 16, "\\i", "miner.drill");
        this.drawInfo(115, 34, 16, 16, "\\i", "miner.res");
    }
    
    @Override
    protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) 
    {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.renderEngine.bindTexture(new ResourceLocation("automation", "textures/gui/miner.png"));
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
        if (tileEntity.active()) this.drawTexturedModalRect(this.guiLeft + 115, this.guiTop + 15, 176, 0, 18, 18);
        this.drawStringCentered(tileEntity.getName(), this.guiLeft + this.xSize / 2, this.guiTop + 4, 0x404040);
        this.drawStringCentered(I18n.translateToLocal("container.inventory"), this.guiLeft + this.xSize / 2, this.guiTop + 72, 0x404040);
        super.drawGuiContainerBackgroundLayer(var1, var2, var3);
    }

    @Override
    protected void mouseClicked(int x, int y, int b) throws IOException 
    {
        super.mouseClicked(x, y, b);
        byte cmd = -1;
        if (this.isPointInRegion(115, 15, 18, 18, x, y)) {
            cmd = 0;
            PacketBuffer dos = tileEntity.getPacketTargetData();
            dos.writeByte(AutomatedTile.CmdOffset + cmd);
            BlockGuiHandler.sendPacketToServer(dos);
        }
            
    }
    
}
