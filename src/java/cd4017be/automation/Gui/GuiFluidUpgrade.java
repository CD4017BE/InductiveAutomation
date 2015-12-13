/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cd4017be.automation.Gui;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.util.ResourceLocation;

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
        this.drawInfo(7, 15, 9, 18, "\\i", "filter.try");
        this.drawInfo(16, 15, 9, 18, "\\i", "filter.invert");
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
        this.drawStringCentered(this.container.inventory.getInventoryName(), this.guiLeft + this.xSize / 2, this.guiTop + 4, 0x404040);
        this.drawStringCentered("Inventory", this.guiLeft + this.xSize / 2, this.guiTop + 36, 0x404040);
    }
    
    @Override
    protected void mouseClicked(int x, int y, int b) 
    {
        byte a = -1;
        if (this.func_146978_c(7, 15, 9, 18, x, y)) {
            a = 0;
            this.container.inventory.upgrade.mode ^= 2;
        } else
        if (this.func_146978_c(16, 15, 9, 18, x, y)) {
            a = 0;
            this.container.inventory.upgrade.mode ^= 1;
        } else
        if (this.func_146978_c(97, 15, 8, 18, x, y))
        {
            a = 1;
            this.container.inventory.upgrade.maxAmount -= b == 0 ? 10 : 1000;
        } else
        if (this.func_146978_c(105, 15, 8, 18, x, y))
        {
            a = 1;
            this.container.inventory.upgrade.maxAmount -= b == 0 ? 1 : 100;
        } else
        if (this.func_146978_c(145, 15, 8, 18, x, y))
        {
            a = 1;
            this.container.inventory.upgrade.maxAmount += b == 0 ? 1 : 100;
        } else
        if (this.func_146978_c(153, 15, 8, 18, x, y))
        {
            a = 1;
            this.container.inventory.upgrade.maxAmount += b == 0 ? 10 : 1000;
        } else 
        if (this.func_146978_c(161, 15, 8, 18, x, y))
        {
            a = 0;
            byte m = this.container.inventory.upgrade.mode;
            if ((m & 12) != 4) this.container.inventory.upgrade.mode ^= 4;
            if ((m & 4) != 0) this.container.inventory.upgrade.mode ^= 8;
        }
        if (this.container.inventory.upgrade.maxAmount < 0) this.container.inventory.upgrade.maxAmount = 0;
        if (a >= 0)
        {
            try {
            ByteArrayOutputStream bos = BlockGuiHandler.getPacketTargetData(0, -1, 0);
            DataOutputStream dos = new DataOutputStream(bos);
            dos.writeByte(a);
            if (a == 0) dos.writeByte(this.container.inventory.upgrade.mode);
            else if (a == 1) dos.writeInt(this.container.inventory.upgrade.maxAmount);
            BlockGuiHandler.sendPacketToServer(bos);
            } catch (IOException e){}
        }
        super.mouseClicked(x, y, b);
    }
}
