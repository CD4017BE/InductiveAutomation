/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cd4017be.automation.Gui;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

import cd4017be.automation.TileEntity.AntimatterFabricator;
import cd4017be.lib.BlockGuiHandler;
import cd4017be.lib.TileContainer;
import cd4017be.lib.TooltipInfo;
import cd4017be.lib.templates.AutomatedTile;
import cd4017be.lib.templates.GuiMachine;

/**
 *
 * @author CD4017BE
 */
public class GuiAntimatterFabricator extends GuiMachine
{
    private AntimatterFabricator tileEntity;
    
    public GuiAntimatterFabricator(AntimatterFabricator tileEntity, EntityPlayer player)
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
        this.drawFormatInfo(62, 38, 70, 8, "progress", (int)(tileEntity.netData.floats[0] / 1000F), AntimatterFabricator.AMEnergy / 1000);
        this.drawInfo(73, 16, 30, 16, "\\i", "voltage");
        this.drawInfo(53, 52, 16, 16, "\\i", "rstCtr");
    }
    
    @Override
    protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) 
    {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.renderEngine.bindTexture(new ResourceLocation("automation", "textures/gui/antimatterFabricator.png"));
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
        int n = tileEntity.getPowerScaled(70);
        this.drawTexturedModalRect(this.guiLeft + 53, this.guiTop + 38, 184, 0, n, 8);
        this.drawTexturedModalRect(this.guiLeft + 52, this.guiTop + 51, 184 + tileEntity.netData.ints[2] * 18, 16, 18, 18);
        this.drawLiquidTank(tileEntity.tanks, 0, 8, 16, false);
        this.drawLiquidTank(tileEntity.tanks, 1, 26, 16, false);
        this.drawLiquidTank(tileEntity.tanks, 2, 134, 16, true);
        this.drawLiquidConfig(tileEntity, -36, 7);
        this.drawEnergyConfig(tileEntity, -54, 7);
        this.drawStringCentered(String.format("%." + (tileEntity.netData.floats[1] >= 100 ? "0" : "1") + "f %s", tileEntity.netData.floats[1], TooltipInfo.getPowerUnit()), this.guiLeft + 97, this.guiTop + 56, 0x404040);
        this.drawStringCentered(tileEntity.netData.ints[0] + "V", this.guiLeft + 88, this.guiTop + 20, 0x404040);
        this.drawStringCentered(tileEntity.getName(), this.guiLeft + this.xSize / 2, this.guiTop + 4, 0x404040);
        this.drawStringCentered(StatCollector.translateToLocal("container.inventory"), this.guiLeft + this.xSize / 2, this.guiTop + 72, 0x404040);
    }

    @Override
    protected void mouseClicked(int x, int y, int b) throws IOException 
    {
        super.mouseClicked(x, y, b);
        byte cmd = -1;
        this.clickLiquidConfig(tileEntity, x - this.guiLeft + 36, y - this.guiTop - 7);
        this.clickEnergyConfig(tileEntity, x - this.guiLeft + 54, y - this.guiTop - 7);
        if (this.isPointInRegion(113, 16, 10, 16, x, y))
        {
            tileEntity.netData.ints[0] += b == 0 ? 10 : 1000;
            cmd = 0;
        } else
        if (this.isPointInRegion(103, 16, 10, 16, x, y))
        {
            tileEntity.netData.ints[0]+= b == 0 ? 1 : 100;
            cmd = 0;
        } else
        if (this.isPointInRegion(63, 16, 10, 16, x, y))
        {
            tileEntity.netData.ints[0]-= b == 0 ? 1 : 100;
            cmd = 0;
        } else
        if (this.isPointInRegion(53, 16, 10, 16, x, y))
        {
            tileEntity.netData.ints[0] -= b == 0 ? 10 : 1000;
            cmd = 0;
        } else
        if (this.isPointInRegion(52, 51, 18, 18, x, y))
        {
            tileEntity.netData.ints[2] ++;
            tileEntity.netData.ints[2] &= 3;
            cmd = 1;
        }
        if (cmd >= 0)
        {
            if (tileEntity.netData.ints[0] < 0) tileEntity.netData.ints[0] = 0;
            if (tileEntity.netData.ints[0] > tileEntity.energy.Umax) tileEntity.netData.ints[0] = tileEntity.energy.Umax;
            PacketBuffer dos = tileEntity.getPacketTargetData();
            dos.writeByte(AutomatedTile.CmdOffset + cmd);
            if (cmd == 0) dos.writeInt(tileEntity.netData.ints[0]);
            else if (cmd == 1) dos.writeByte(tileEntity.netData.ints[2]);
            BlockGuiHandler.sendPacketToServer(dos);
        }
    }
    
}
