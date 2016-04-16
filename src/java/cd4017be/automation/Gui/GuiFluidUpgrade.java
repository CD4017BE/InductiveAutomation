/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cd4017be.automation.Gui;

import java.io.IOException;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.I18n;

import org.lwjgl.opengl.GL11;

import cd4017be.lib.BlockGuiHandler;
import cd4017be.lib.templates.GuiMachine;

/**
 *
 * @author CD4017BE
 */
public class GuiFluidUpgrade extends GuiMachine
{
    private final ContainerFluidUpgrade container;
    
    public GuiFluidUpgrade(ContainerFluidUpgrade container)
    {
        super(container);
        this.container = container;
    }
    
    @Override
    public void initGui() 
    {
        this.xSize = 176;
        this.ySize = 132;
        super.initGui();
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mx, int my) 
    {
        super.drawGuiContainerForegroundLayer(mx, my);
        this.drawInfo(7, 15, 9, 18, "\\i", "filter.tryF" + (this.container.inventory.upgrade.mode >> 1 & 1));
        this.drawInfo(16, 15, 9, 18, "\\i", "filter.invertF" + (this.container.inventory.upgrade.mode & 1));
        this.drawInfo(113, 16, 32, 16, "\\i", "filter.targetF");
        this.drawInfo(161, 15, 8, 18, "\\i", "rstCtr");
    }
    
    @Override
    protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) 
    {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.renderEngine.bindTexture(new ResourceLocation("automation", "textures/gui/fluidUpgrade.png"));
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
        if ((this.container.inventory.upgrade.mode & 1) != 0) this.drawTexturedModalRect(this.guiLeft + 16, this.guiTop + 15, 185, 0, 9, 18);
        if ((this.container.inventory.upgrade.mode & 2) != 0) this.drawTexturedModalRect(this.guiLeft + 7, this.guiTop + 15, 176, 0, 9, 18);
        if ((this.container.inventory.upgrade.mode & 4) != 0) this.drawTexturedModalRect(this.guiLeft + 161, this.guiTop + 15, (this.container.inventory.upgrade.mode & 8) != 0 ? 202 : 194, 0, 8, 18);
        this.drawStringCentered("" + this.container.inventory.upgrade.maxAmount, this.guiLeft + 129, this.guiTop + 20, 0x404040);
        this.drawStringCentered(this.container.inventory.getName(), this.guiLeft + this.xSize / 2, this.guiTop + 4, 0x404040);
        this.drawStringCentered(I18n.translateToLocal("container.inventory"), this.guiLeft + this.xSize / 2, this.guiTop + 36, 0x404040);
    }
    
    @Override
    protected void mouseClicked(int x, int y, int b) throws IOException 
    {
        byte a = -1;
        if (this.isPointInRegion(7, 15, 9, 18, x, y)) {
            a = 0;
            this.container.inventory.upgrade.mode ^= 2;
        } else
        if (this.isPointInRegion(16, 15, 9, 18, x, y)) {
            a = 0;
            this.container.inventory.upgrade.mode ^= 1;
        } else
        if (this.isPointInRegion(97, 15, 8, 18, x, y))
        {
            a = 1;
            this.container.inventory.upgrade.maxAmount -= b == 0 ? 10 : 1000;
        } else
        if (this.isPointInRegion(105, 15, 8, 18, x, y))
        {
            a = 1;
            this.container.inventory.upgrade.maxAmount -= b == 0 ? 1 : 100;
        } else
        if (this.isPointInRegion(145, 15, 8, 18, x, y))
        {
            a = 1;
            this.container.inventory.upgrade.maxAmount += b == 0 ? 1 : 100;
        } else
        if (this.isPointInRegion(153, 15, 8, 18, x, y))
        {
            a = 1;
            this.container.inventory.upgrade.maxAmount += b == 0 ? 10 : 1000;
        } else 
        if (this.isPointInRegion(161, 15, 8, 18, x, y))
        {
            a = 0;
            byte m = this.container.inventory.upgrade.mode;
            if ((m & 12) != 4) this.container.inventory.upgrade.mode ^= 4;
            if ((m & 4) != 0) this.container.inventory.upgrade.mode ^= 8;
        }
        if (this.container.inventory.upgrade.maxAmount < 0) this.container.inventory.upgrade.maxAmount = 0;
        if (a >= 0)
        {
            PacketBuffer dos = BlockGuiHandler.getPacketTargetData(new BlockPos(0, -1, 0));
            dos.writeByte(a);
            if (a == 0) dos.writeByte(this.container.inventory.upgrade.mode);
            else if (a == 1) dos.writeInt(this.container.inventory.upgrade.maxAmount);
            BlockGuiHandler.sendPacketToServer(dos);
        }
        super.mouseClicked(x, y, b);
    }
}
