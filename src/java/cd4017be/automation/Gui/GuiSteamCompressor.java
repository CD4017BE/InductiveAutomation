/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cd4017be.automation.Gui;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.I18n;

import org.lwjgl.opengl.GL11;

import cd4017be.automation.TileEntity.SteamCompressor;
import cd4017be.lib.TileContainer;
import cd4017be.lib.templates.GuiMachine;

/**
 *
 * @author CD4017BE
 */
public class GuiSteamCompressor extends GuiMachine
{
    private final SteamCompressor tileEntity;
    
    public GuiSteamCompressor(SteamCompressor tileEntity, EntityPlayer player)
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
        this.drawFormatInfo(48, 16, 8, 52, "steamComp.press", tileEntity.netData.ints[0], 640);
    }
    
    @Override
    protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) 
    {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.renderEngine.bindTexture(new ResourceLocation("automation", "textures/gui/steamCompressor.png"));
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
        int n = this.tileEntity.getPressureScaled(52);
        this.drawTexturedModalRect(this.guiLeft + 48, this.guiTop + 68 - n, 176, 52 - n, 8, n);
        n = this.tileEntity.getProgressScaled(32);
        this.drawTexturedModalRect(this.guiLeft + 108, this.guiTop + 37, 184, 0, n, 10);
        this.drawLiquidTank(tileEntity.tanks, 0, 8, 16, true);
        this.drawLiquidConfig(tileEntity, -18, 7);
        this.drawItemConfig(tileEntity, -72, 7);
        this.drawStringCentered(tileEntity.getName(), this.guiLeft + this.xSize / 2, this.guiTop + 4, 0x404040);
        this.drawStringCentered(I18n.translateToLocal("container.inventory"), this.guiLeft + this.xSize / 2, this.guiTop + 72, 0x404040);
    }
    
    @Override
    protected void mouseClicked(int x, int y, int b) throws IOException 
    {
        super.mouseClicked(x, y, b);
        this.clickLiquidConfig(tileEntity, x - this.guiLeft + 18, y - this.guiTop - 7);
        this.clickItemConfig(tileEntity, x - this.guiLeft + 72, y - this.guiTop - 7);
    }
}
