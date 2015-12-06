/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cd4017be.automation.Gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import cd4017be.automation.TileEntity.Farm;
import cd4017be.lib.TileContainer;
import cd4017be.lib.templates.GuiMachine;

/**
 *
 * @author CD4017BE
 */
public class GuiFarm extends GuiMachine
{
    
    private final Farm tileEntity;
    
    public GuiFarm(Farm tileEntity, EntityPlayer player)
    {
        super(new TileContainer(tileEntity, player));
        this.tileEntity = tileEntity;
    }

    @Override
    public void initGui() 
    {
        this.xSize = 176;
        this.ySize = 186;
        super.initGui();
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mx, int my) 
    {
        super.drawGuiContainerForegroundLayer(mx, my);
        this.drawInfo(79, 52, 16, 16, "\\i", "gui.farm.in");
        this.drawInfo(81, 70, 16, 16, "\\i", "gui.farm.out");
    }
    
    @Override
    protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) 
    {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.renderEngine.bindTexture(new ResourceLocation("automation", "textures/gui/farm.png"));
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
        this.drawItemConfig(tileEntity, -27, 7);
        this.drawEnergyConfig(tileEntity, -45, 7);
        this.drawStringCentered("Automatic Farm", this.guiLeft + this.xSize / 2, this.guiTop + 4, 0x404040);
        this.drawStringCentered("Inventory", this.guiLeft + this.xSize / 2, this.guiTop + 92, 0x404040);
    }

    @Override
    protected void mouseClicked(int x, int y, int b) 
    {
        super.mouseClicked(x, y, b);
        this.clickItemConfig(tileEntity, x - this.guiLeft + 27, y - this.guiTop - 7);
        this.clickEnergyConfig(tileEntity, x - this.guiLeft + 45, y - this.guiTop - 7);
    }
    
}
