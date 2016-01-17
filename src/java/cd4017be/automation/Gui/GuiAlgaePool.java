/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cd4017be.automation.Gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

import cd4017be.automation.TileEntity.AlgaePool;
import cd4017be.lib.TileContainer;
import cd4017be.lib.templates.GuiMachine;

/**
 *
 * @author CD4017BE
 */
public class GuiAlgaePool extends GuiMachine
{
    private final AlgaePool tileEntity;
    
    public GuiAlgaePool(AlgaePool tileEntity, EntityPlayer player)
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
        this.drawFormatInfo(62, 16, 16, 52, "algae.nutr", tileEntity.getNutrientsScaled(100));
        this.drawFormatInfo(80, 16, 34, 52, "algae.algae", (int)tileEntity.netData.floats[0], tileEntity.netData.ints[0]);
    }
    
    @Override
    protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) 
    {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.renderEngine.bindTexture(new ResourceLocation("automation", "textures/gui/algaePool.png"));
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
        int n = this.tileEntity.getNutrientsScaled(52);
        this.drawTexturedModalRect(this.guiLeft + 62, this.guiTop + 68 - n, 210, 104 - n, 16, n);
        n = this.tileEntity.getAlgaeScaled(52);
        this.drawTexturedModalRect(this.guiLeft + 80, this.guiTop + 68 - n, 176, 104 - n, 34, n);
        this.drawLiquidTank(tileEntity.tanks, 0, 8, 16, true);
        this.drawLiquidTank(tileEntity.tanks, 1, 134, 16, true);
        this.drawLiquidConfig(tileEntity, -27, 7);
        this.drawItemConfig(tileEntity, -45, 7);
        this.drawStringCentered(tileEntity.getInventoryName(), this.guiLeft + this.xSize / 2, this.guiTop + 4, 0x404040);
        this.drawStringCentered(StatCollector.translateToLocal("container.inventory"), this.guiLeft + this.xSize / 2, this.guiTop + 72, 0x404040);
    }

    @Override
    protected void mouseClicked(int x, int y, int b) 
    {
        super.mouseClicked(x, y, b);
        this.clickLiquidConfig(tileEntity, x - this.guiLeft + 27, y - this.guiTop - 7);
        this.clickItemConfig(tileEntity, x - this.guiLeft + 45, y - this.guiTop - 7);
    }
    
}
