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

import cd4017be.automation.TileEntity.FluidPacker;
import cd4017be.lib.TileContainer;
import cd4017be.lib.templates.GuiMachine;

/**
 *
 * @author CD4017BE
 */
public class GuiFluidPacker extends GuiMachine
{
    private final FluidPacker tileEntity;
    
    public GuiFluidPacker(FluidPacker tileEntity, EntityPlayer player)
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
    protected void drawGuiContainerBackgroundLayer(float var1, int x, int y) 
    {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.renderEngine.bindTexture(new ResourceLocation("automation", "textures/gui/fluidPacker.png"));
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
        this.drawLiquidTank(tileEntity.tanks, 3, 8, 16, true);
        this.drawLiquidTank(tileEntity.tanks, 0, 62, 16, false);
        this.drawLiquidTank(tileEntity.tanks, 1, 98, 16, false);
        this.drawLiquidTank(tileEntity.tanks, 2, 134, 16, false);
        this.drawLiquidConfig(tileEntity, -45, 7);
        this.drawItemConfig(tileEntity, -81, 7);
        this.drawStringCentered(tileEntity.getName(), this.guiLeft + this.xSize / 2, this.guiTop + 4, 0x404040);
        this.drawStringCentered(I18n.translateToLocal("container.inventory"), this.guiLeft + this.xSize / 2, this.guiTop + 72, 0x404040);
    }
    
    @Override
    protected void mouseClicked(int x, int y, int b) throws IOException 
    {
        this.clickLiquidConfig(tileEntity, x - this.guiLeft + 45, y - this.guiTop - 7);
        this.clickItemConfig(tileEntity, x - this.guiLeft + 81, y - this.guiTop - 7);
        super.mouseClicked(x, y, b);
    }
    
}
