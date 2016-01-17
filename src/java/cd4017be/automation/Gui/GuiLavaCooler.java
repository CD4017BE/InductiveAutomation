/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cd4017be.automation.Gui;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

import cd4017be.automation.TileEntity.LavaCooler;
import cd4017be.lib.BlockGuiHandler;
import cd4017be.lib.TileContainer;
import cd4017be.lib.templates.AutomatedTile;
import cd4017be.lib.templates.GuiMachine;

/**
 *
 * @author CD4017BE
 */
public class GuiLavaCooler extends GuiMachine
{
    
    private final LavaCooler tileEntity;
    
    public GuiLavaCooler(LavaCooler tileEntity, EntityPlayer player)
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
        this.drawInfo(80, 16, 16, 52, tileEntity.getOutputName().split("\n"));
    }
    
    @Override
    protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) 
    {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.renderEngine.bindTexture(new ResourceLocation("automation", "textures/gui/lavaCooler.png"));
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
        int n = this.tileEntity.getCoolScaled(52);
        this.drawTexturedModalRect(this.guiLeft + 80, this.guiTop + 16, 176, n, 16, 52);
        this.drawTexturedModalRect(this.guiLeft + ((tileEntity.netData.ints[0] & 0x10) == 0 ? 8 : 26), this.guiTop + ((tileEntity.netData.ints[0] & 0x20) == 0 ? 16 : 34), 176, 104, 16, 16);
        this.drawLiquidTank(tileEntity.tanks, 0, 98, 16, true);
        this.drawLiquidTank(tileEntity.tanks, 1, 44, 16, true);
        this.drawLiquidTank(tileEntity.tanks, 2, 134, 16, true);
        this.drawLiquidConfig(tileEntity, -36, 7);
        this.drawItemConfig(tileEntity, -54, 7);
        this.drawStringCentered(tileEntity.getInventoryName(), this.guiLeft + this.xSize / 2, this.guiTop + 4, 0x404040);
        this.drawStringCentered(StatCollector.translateToLocal("container.inventory"), this.guiLeft + this.xSize / 2, this.guiTop + 72, 0x404040);
    }
    
    @Override
    protected void mouseClicked(int x, int y, int b) 
    {
        this.clickLiquidConfig(tileEntity, x - this.guiLeft + 36, y - this.guiTop - 7);
        this.clickItemConfig(tileEntity, x - this.guiLeft + 54, y - this.guiTop - 7);
        int cmd = -1;
        if (this.func_146978_c(8, 16, 16, 16, x, y)) cmd = 0;
        else if (this.func_146978_c(26, 16, 16, 16, x, y)) cmd = 1;
        else if (this.func_146978_c(8, 34, 16, 16, x, y)) cmd = 2;
        else if (this.func_146978_c(26, 34, 16, 16, x, y)) cmd = 3;
        else super.mouseClicked(x, y, b);
        if (cmd >= 0) {
        	try {
                ByteArrayOutputStream bos = tileEntity.getPacketTargetData();
                DataOutputStream dos = new DataOutputStream(bos);
                dos.writeByte(AutomatedTile.CmdOffset + cmd);
                BlockGuiHandler.sendPacketToServer(bos);
            } catch (IOException e){}
        }
    }
    
}
