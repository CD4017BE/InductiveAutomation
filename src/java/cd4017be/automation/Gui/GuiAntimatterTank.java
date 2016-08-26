/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cd4017be.automation.Gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;

import org.lwjgl.opengl.GL11;

import cd4017be.automation.TileEntity.AntimatterTank;
import cd4017be.lib.Gui.GuiMachine;
import cd4017be.lib.Gui.TileContainer;
import cd4017be.lib.util.Utils;

/**
 *
 * @author CD4017BE
 */
public class GuiAntimatterTank extends GuiMachine
{
    
    private AntimatterTank tileEntity;
    
    public GuiAntimatterTank(AntimatterTank tileEntity, EntityPlayer player)
    {
        super(new TileContainer(tileEntity, player));
        this.tileEntity = tileEntity;
    }

    @Override
    public void initGui() 
    {
        this.xSize = 176;
        this.ySize = 166;
        super.initGui();
    }
    
    @Override
    protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) 
    {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.renderEngine.bindTexture(new ResourceLocation("automation", "textures/gui/antimatterTank.png"));
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
        int n = tileEntity.getStorageScaled(8000);
        int n0 = n / 50;
        int n1 = n % 50;
        this.drawTexturedModalRect(this.guiLeft + 8, this.guiTop + 16, 0, 206, n0, 50);
        this.drawTexturedModalRect(this.guiLeft + 8 + n0, this.guiTop + 16, n0, 206, 1, n1);
        int m = tileEntity.tanks.getAmount(0);
        double e = m * 90000.0;
        this.drawStringCentered(m + "ng", this.guiLeft + this.xSize / 2 - 8, this.guiTop + 28, 0x404040);
        this.drawStringCentered(Utils.formatNumber(e, 6, 0) + "J", this.guiLeft + this.xSize / 2 - 8, this.guiTop + 46, 0x404040);
        this.drawStringCentered(tileEntity.getName(), this.guiLeft + this.xSize / 2, this.guiTop + 4, 0x404040);
        this.drawStringCentered(I18n.translateToLocal("container.inventory"), this.guiLeft + this.xSize / 2, this.guiTop + 71, 0x404040);
        super.drawGuiContainerBackgroundLayer(var1, var2, var3);
    }
    
}
