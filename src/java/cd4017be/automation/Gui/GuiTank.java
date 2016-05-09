/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cd4017be.automation.Gui;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;

import org.lwjgl.opengl.GL11;

import cd4017be.automation.TileEntity.Tank;
import cd4017be.lib.TileContainer;
import cd4017be.lib.templates.GuiMachine;

/**
 *
 * @author CD4017BE
 */
public class GuiTank extends GuiMachine
{
    
    private final Tank tileEntity;
    
    public GuiTank(Tank tileEntity, EntityPlayer player)
    {
        super (new TileContainer(tileEntity, player));
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
    protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) 
    {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.renderEngine.bindTexture(new ResourceLocation("automation", "textures/gui/tank.png"));
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
        this.drawLiquidTank(tileEntity.tanks, 0, 184, 16, true);
        this.drawLiquidConfig(tileEntity, -18, 7);
        this.drawItemConfig(tileEntity, -45, 7);
        this.drawStringCentered(I18n.translateToLocal("container.inventory"), this.guiLeft + 88, this.guiTop + 4, 0x404040);
        this.drawStringCentered(tileEntity.getName(), this.guiLeft + 201, this.guiTop + 4, 0x404040);
    }
    
    @Override
    protected void mouseClicked(int x, int y, int b) throws IOException 
    {
        super.mouseClicked(x, y, b);
        this.clickLiquidConfig(tileEntity, x - this.guiLeft + 18, y - this.guiTop - 7);
        this.clickItemConfig(tileEntity, x - this.guiLeft + 45, y - this.guiTop - 7);
    }
    
}
