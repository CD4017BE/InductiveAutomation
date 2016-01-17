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

import cd4017be.automation.Config;
import cd4017be.automation.TileEntity.SteamBoiler;
import cd4017be.lib.BlockGuiHandler;
import cd4017be.lib.TileContainer;
import cd4017be.lib.templates.AutomatedTile;
import cd4017be.lib.templates.GuiMachine;

/**
 *
 * @author CD4017BE
 */
public class GuiSteamBoiler extends GuiMachine
{
    
    private final SteamBoiler tileEntity;
    
    public GuiSteamBoiler(SteamBoiler tileEntity, EntityPlayer player)
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
        this.drawFormatInfo(26, 34, 16, 16, "fuelHeat", tileEntity.netData.ints[1]);
        this.drawFormatInfo(26, 52, 8, 16, "boiler.burnUp", tileEntity.netData.ints[4]);
        this.drawFormatInfo(52, 16, 8, 52, "heat", tileEntity.netData.ints[2], Config.maxK_steamBoiler);
    }
    
    @Override
    protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) 
    {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.renderEngine.bindTexture(new ResourceLocation("automation", "textures/gui/steamBoiler.png"));
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
        int n = this.tileEntity.getCookScaled(34);
        this.drawTexturedModalRect(this.guiLeft + 107, this.guiTop + 59 - n, 184, 34 - n, 16, n);
        n = this.tileEntity.getTempScaled(52);
        this.drawTexturedModalRect(this.guiLeft + 52, this.guiTop + 68 - n, 176, 52 - n, 8, n);
        n = this.tileEntity.getBurnScaled(14);
        this.drawTexturedModalRect(this.guiLeft + 27, this.guiTop + 49 - n, 184, 48 - n, 14, n);
        n = this.tileEntity.netData.ints[4] * 2;
        this.drawTexturedModalRect(this.guiLeft + 26, this.guiTop + 68 - n, 176, 68 - n, 8, n);
        this.drawLiquidTank(tileEntity.tanks, 0, 62, 16, true);
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
        byte cmd = -1;
        if (this.func_146978_c(34, 60, 8, 8, x, y)) {
        	cmd = 0;
        } else
        if (this.func_146978_c(34, 52, 8, 8, x, y)) {
        	cmd = 1;
        }
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
