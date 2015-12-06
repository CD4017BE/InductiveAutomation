/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cd4017be.automation.Gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import cd4017be.automation.TileEntity.SteamEngine;
import cd4017be.lib.TileContainer;
import cd4017be.lib.templates.GuiMachine;

/**
 *
 * @author CD4017BE
 */
public class GuiSteamEngine extends GuiMachine
{
    private final SteamEngine tileEntity;
    
    public GuiSteamEngine(SteamEngine tileEntity, EntityPlayer player)
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
        this.drawInfo(57, 16, 8, 52, "Speed:", String.format("%d ", tileEntity.getPowerScaled(100)).concat("%"));
    }
    
    @Override
    protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) 
    {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.renderEngine.bindTexture(new ResourceLocation("automation", "textures/gui/steamEngine.png"));
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
        int n = tileEntity.getPowerScaled(52);
        this.drawTexturedModalRect(this.guiLeft + 57, this.guiTop + 68 - n, 176, 52 - n, 8, n);
        this.drawLiquidTank(tileEntity.tanks, 0, 8, 16, true);
        this.drawLiquidTank(tileEntity.tanks, 1, 80, 16, true);
        this.drawLiquidConfig(tileEntity, -27, 7);
        this.drawEnergyConfig(tileEntity, -45, 7);
        this.drawStringCentered(tileEntity.getInventoryName(), this.guiLeft + this.xSize / 2, this.guiTop + 4, 0x404040);
        this.drawStringCentered("Inventory", this.guiLeft + this.xSize / 2, this.guiTop + 72, 0x404040);
        this.drawStringCentered("Power", this.guiLeft + 142, this.guiTop + 20, 0xc06060);
        this.drawStringCentered(String.format("%.1f kW", tileEntity.getEnergyOut()), this.guiLeft + 142, this.guiTop + 32, 0xc06060);
        this.drawStringCentered(String.format("max %d kW", (int)tileEntity.getPower()), this.guiLeft + 142, this.guiTop + 48, 0xc06060);
    }
    
    @Override
    protected void mouseClicked(int x, int y, int b) 
    {
        super.mouseClicked(x, y, b);
        this.clickLiquidConfig(tileEntity, x - this.guiLeft + 27, y - this.guiTop - 7);
        this.clickEnergyConfig(tileEntity, x - this.guiLeft + 45, y - this.guiTop - 7);
    }
    
}
