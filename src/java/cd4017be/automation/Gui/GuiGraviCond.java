package cd4017be.automation.Gui;

import org.lwjgl.opengl.GL11;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;
import cd4017be.automation.TileEntity.GraviCond;
import cd4017be.lib.TileContainer;
import cd4017be.lib.TooltipInfo;
import cd4017be.lib.templates.GuiMachine;
import cd4017be.lib.util.Utils;

public class GuiGraviCond extends GuiMachine 
{
	
	private final GraviCond tileEntity;
	
	public GuiGraviCond(GraviCond tileEntity, EntityPlayer player) 
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
        this.drawFormatInfo(133, 37, 18, 10, "grav.need", Utils.formatNumber((double)tileEntity.netData.ints[1] * 1000D, 4, 0));
        this.drawInfo(26, 16, 70, 12, "\\i", "grav.energy");
        this.drawInfo(26, 34, 70, 12, "\\i", "grav.trash");
    }
    
    @Override
    protected void drawGuiContainerBackgroundLayer(float var1, int x, int y) 
    {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.renderEngine.bindTexture(new ResourceLocation("automation", "textures/gui/gravicompr.png"));
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
        int n = tileEntity.getEnergyScaled(70);
        this.drawTexturedModalRect(this.guiLeft + 26, this.guiTop + 16, 176, 0, n, 12);
        n = tileEntity.getMatterScaled(70);
        this.drawTexturedModalRect(this.guiLeft + 26, this.guiTop + 34, 176, 12, n, 12);
        n = tileEntity.getProgressScaled(18);
        this.drawTexturedModalRect(this.guiLeft + 133, this.guiTop + 37, 176, 24, n, 10);
        this.drawLocString(this.guiLeft + 26, this.guiTop + 52, 8, 0x404040, "grav.matter", Utils.formatNumber((double)tileEntity.netData.ints[0] * 1000D, 4, 0));
        this.drawStringCentered(String.format("%.0f %s", tileEntity.netData.floats[0] / 1000F, TooltipInfo.getEnergyUnit()), this.guiLeft + 61, this.guiTop + 18, 0x404040);
        this.drawStringCentered(tileEntity.getMatterScaled(100) + " %", this.guiLeft + 61, this.guiTop + 36, 0x404040);
        this.drawStringCentered(tileEntity.getName(), this.guiLeft + this.xSize / 2, this.guiTop + 4, 0x404040);
        this.drawStringCentered(I18n.translateToLocal("container.inventory"), this.guiLeft + this.xSize / 2, this.guiTop + 72, 0x404040);
        super.drawGuiContainerBackgroundLayer(var1, x, y);
    }

}
