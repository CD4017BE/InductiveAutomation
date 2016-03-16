package cd4017be.automation.Gui;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

import cd4017be.automation.Config;
import cd4017be.automation.TileEntity.FuelCell;
import cd4017be.lib.BlockGuiHandler;
import cd4017be.lib.TileContainer;
import cd4017be.lib.templates.AutomatedTile;
import cd4017be.lib.templates.GuiMachine;

public class GuiFuelCell extends GuiMachine 
{
	
protected FuelCell tileEntity;
    
    public GuiFuelCell(FuelCell tileEntity, EntityPlayer player)
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
        this.drawInfo(118, 16, 30, 16, "\\i", "voltage");
    }
    
    @Override
    protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) 
    {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.renderEngine.bindTexture(new ResourceLocation("automation", "textures/gui/fuelCell.png"));
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
        int n = tileEntity.getStorageScaled(70);
        this.drawTexturedModalRect(this.guiLeft + 98, this.guiTop + 52, 184, 0, n, 16);
        n = tileEntity.getPowerScaled(52);
        this.drawTexturedModalRect(this.guiLeft + 84, this.guiTop + 68 - n, 176, 52 - n, 8, n);
        this.drawLiquidTank(tileEntity.tanks, 0, 8, 16, false);
        this.drawLiquidTank(tileEntity.tanks, 1, 26, 16, false);
        this.drawLiquidTank(tileEntity.tanks, 2, 62, 16, false);
        this.drawLiquidConfig(tileEntity, -36, 7);
        this.drawEnergyConfig(tileEntity, -54, 7);
        this.drawStringCentered(tileEntity.getName(), this.guiLeft + this.xSize / 2, this.guiTop + 4, 0x404040);
        this.drawStringCentered(StatCollector.translateToLocal("container.inventory"), this.guiLeft + this.xSize / 2, this.guiTop + 72, 0x404040);
        this.drawStringCentered(tileEntity.netData.ints[0] + "V", this.guiLeft + 133, this.guiTop + 20, 0x404040);
        this.drawStringCentered("P = " + tileEntity.netData.ints[1] + " kW", this.guiLeft + 133, this.guiTop + 38, 0x804040);
        this.drawStringCentered((int)(tileEntity.netData.floats[0] / 1000F) + " kJ", this.guiLeft + 133, this.guiTop + 56, 0x404040);
    }
    
    @Override
    protected void mouseClicked(int x, int y, int b) throws IOException 
    {
        byte a = -1;
        this.clickLiquidConfig(tileEntity, x - this.guiLeft + 36, y - this.guiTop - 7);
        this.clickEnergyConfig(tileEntity, x - this.guiLeft + 54, y - this.guiTop - 7);
        if (this.isPointInRegion(98, 16, 10, 16, x, y))
        {
            tileEntity.netData.ints[0] -= b == 0 ? 10 : 1000;
            a = 0;
        } else
        if (this.isPointInRegion(108, 16, 10, 16, x, y))
        {
            tileEntity.netData.ints[0] -= b == 0 ? 1 : 100;
            a = 0;
        } else
        if (this.isPointInRegion(148, 16, 10, 16, x, y))
        {
            tileEntity.netData.ints[0] += b == 0 ? 1 : 100;
            a = 0;
        } else
        if (this.isPointInRegion(158, 16, 10, 16, x, y))
        {
            tileEntity.netData.ints[0] += b == 0 ? 10 : 1000;
            a = 0;
        }
        if (a >= 0)
        {
            if (tileEntity.netData.ints[0] < 0) tileEntity.netData.ints[0] = 0;
            if (tileEntity.netData.ints[0] > Config.Umax[2]) tileEntity.netData.ints[0] = Config.Umax[2];
            PacketBuffer dos = tileEntity.getPacketTargetData();
            dos.writeByte(AutomatedTile.CmdOffset);
            dos.writeShort(tileEntity.netData.ints[0]);
            BlockGuiHandler.sendPacketToServer(dos);
        }
        super.mouseClicked(x, y, b);
    }
	
}
