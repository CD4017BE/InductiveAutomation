package cd4017be.automation.Gui;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;

import org.lwjgl.opengl.GL11;

import cd4017be.api.automation.PipeEnergy;
import cd4017be.automation.Config;
import cd4017be.automation.TileEntity.Electrolyser;
import cd4017be.lib.BlockGuiHandler;
import cd4017be.lib.Gui.GuiMachine;
import cd4017be.lib.Gui.TileContainer;
import cd4017be.lib.templates.AutomatedTile;

public class GuiElectrolyser extends GuiMachine 
{
	
private final Electrolyser tileEntity;
    
    public GuiElectrolyser(Electrolyser tileEntity, EntityPlayer player)
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
        this.drawInfo(8, 36, 16, 12, "\\i", "resistor");
    }
    
    @Override
    protected void drawGuiContainerBackgroundLayer(float f, int i, int j) 
    {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.renderEngine.bindTexture(new ResourceLocation("automation", "textures/gui/electrolyser.png"));
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
        int n = tileEntity.getProgressScaled(32);
        this.drawTexturedModalRect(this.guiLeft + 95 - n, this.guiTop + 55, 216 - n, 0, n, 10);
        this.drawTexturedModalRect(this.guiLeft + 117, this.guiTop + 55, 184, 10, n, 10);
        n = tileEntity.getPowerScaled(52);
        this.drawTexturedModalRect(this.guiLeft + 26, this.guiTop + 68 - n, 176, 52 - n, 8, n);
        this.drawStringCentered(tileEntity.getName(), this.guiLeft + this.xSize / 2, this.guiTop + 4, 0x404040);
        this.drawStringCentered(I18n.translateToLocal("container.inventory"), this.guiLeft + this.xSize / 2, this.guiTop + 72, 0x404040);
        this.drawStringCentered("" + tileEntity.netData.ints[0], this.guiLeft + 16, this.guiTop + 38, 0x404040);
        super.drawGuiContainerBackgroundLayer(f, i, j);
    }
    
    @Override
    protected void mouseClicked(int x, int y, int b) throws IOException 
    {
        boolean a = false;
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
            dos.writeByte(AutomatedTile.CmdOffset);
            dos.writeInt(tileEntity.netData.ints[0]);
            BlockGuiHandler.sendPacketToServer(dos);
        }
        super.mouseClicked(x, y, b);
    }
	
}
