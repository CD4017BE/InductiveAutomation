/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cd4017be.automation.Gui;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;

import org.lwjgl.opengl.GL11;

import cd4017be.api.automation.PipeEnergy;
import cd4017be.automation.Config;
import cd4017be.automation.TileEntity.AdvancedFurnace;
import cd4017be.lib.BlockGuiHandler;
import cd4017be.lib.TileContainer;
import cd4017be.lib.templates.AutomatedTile;
import cd4017be.lib.templates.GuiMachine;

/**
 *
 * @author CD4017BE
 */
public class GuiAdvancedFurnace extends GuiMachine
{
    private final AdvancedFurnace tileEntity;
    
    public GuiAdvancedFurnace(AdvancedFurnace tileEntity, EntityPlayer player)
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
        this.drawInfo(26, 16, 8, 52, PipeEnergy.getEnergyInfo(tileEntity.netData.floats[2], 0F, (float)tileEntity.netData.ints[0]));
        this.drawInfo(100, 18, 12, 12, "\\i", "advancedFurnace.swap");
        this.drawInfo(8, 36, 16, 12, "\\i", "resistor");
    }
    
    @Override
    protected void drawGuiContainerBackgroundLayer(float var1, int x, int y) 
    {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.renderEngine.bindTexture(new ResourceLocation("automation", "textures/gui/advancedFurnace.png"));
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
        int n = tileEntity.getProgressScaled(18);
        this.drawTexturedModalRect(this.guiLeft + 97, this.guiTop + 37, 184, 0, n, 10);
        n = tileEntity.getPowerScaled(52);
        this.drawTexturedModalRect(this.guiLeft + 26, this.guiTop + 68 - n, 176, 52 - n, 8, n);
        this.drawLiquidTank(tileEntity.tanks, 0, 44, 16, true);
        this.drawLiquidTank(tileEntity.tanks, 1, 134, 16, true);
        this.drawLiquidConfig(tileEntity, -27, 7);
        this.drawItemConfig(tileEntity, -54, 7);
        this.drawEnergyConfig(tileEntity, -72, 7);
        this.drawStringCentered(tileEntity.getName(), this.guiLeft + this.xSize / 2, this.guiTop + 4, 0x404040);
        this.drawStringCentered(I18n.translateToLocal("container.inventory"), this.guiLeft + this.xSize / 2, this.guiTop + 72, 0x404040);
        this.drawStringCentered("" + tileEntity.netData.ints[0], this.guiLeft + 16, this.guiTop + 38, 0x404040);
    }
    
    @Override
    protected void mouseClicked(int x, int y, int b) throws IOException
    {
        boolean a = false;
        int cmd = 0;
        this.clickLiquidConfig(tileEntity, x - this.guiLeft + 27, y - this.guiTop - 7);
        this.clickItemConfig(tileEntity, x - this.guiLeft + 54, y - this.guiTop - 7);
        this.clickEnergyConfig(tileEntity, x - this.guiLeft + 72, y - this.guiTop - 7);
        if (this.isPointInRegion(100, 18, 12, 12, x, y))
        {
            cmd = 1;
            a = true;
        } else
        if (this.isPointInRegion(8, 16, 16, 10, x, y))
        {
            tileEntity.netData.ints[0] += 10;
            a = true;
        } else
        if (this.isPointInRegion(8, 26, 16, 10, x, y))
        {
            tileEntity.netData.ints[0] ++;
            a = true;
        } else
        if (this.isPointInRegion(8, 48, 16, 10, x, y))
        {
            tileEntity.netData.ints[0] --;
            a = true;
        } else
        if (this.isPointInRegion(8, 58, 16, 10, x, y))
        {
            tileEntity.netData.ints[0] -= 10;
            a = true;
        }
        if (a)
        {
        	if (tileEntity.netData.ints[0] < Config.Rmin) tileEntity.netData.ints[0] = Config.Rmin;
            PacketBuffer dos = tileEntity.getPacketTargetData();
            dos.writeByte(cmd + AutomatedTile.CmdOffset);
            if (cmd == 0) dos.writeInt(tileEntity.netData.ints[0]);
            BlockGuiHandler.sendPacketToServer(dos);
        }
        super.mouseClicked(x, y, b);
    }
    
}
