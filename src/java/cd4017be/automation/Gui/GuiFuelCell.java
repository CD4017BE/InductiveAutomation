package cd4017be.automation.Gui;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;

import org.lwjgl.opengl.GL11;

import cd4017be.automation.Config;
import cd4017be.automation.TileEntity.FuelCell;
import cd4017be.lib.BlockGuiHandler;
import cd4017be.lib.TooltipInfo;
import cd4017be.lib.Gui.GuiMachine;
import cd4017be.lib.Gui.TileContainer;
import cd4017be.lib.templates.AutomatedTile;

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
		this.drawStringCentered(tileEntity.getName(), this.guiLeft + this.xSize / 2, this.guiTop + 4, 0x404040);
		this.drawStringCentered(I18n.translateToLocal("container.inventory"), this.guiLeft + this.xSize / 2, this.guiTop + 72, 0x404040);
		this.drawStringCentered(tileEntity.netI0 + "V", this.guiLeft + 133, this.guiTop + 20, 0x404040);
		this.drawStringCentered("P = " + tileEntity.netI1 + " " + TooltipInfo.getPowerUnit(), this.guiLeft + 133, this.guiTop + 38, 0x804040);
		this.drawStringCentered((int)(tileEntity.netF0 / 1000F) + " " + TooltipInfo.getEnergyUnit(), this.guiLeft + 133, this.guiTop + 56, 0x404040);
		super.drawGuiContainerBackgroundLayer(var1, var2, var3);
	}
	
	@Override
	protected void mouseClicked(int x, int y, int b) throws IOException 
	{
		byte a = -1;
		if (this.isPointInRegion(98, 16, 10, 16, x, y))
		{
			tileEntity.netI0 -= b == 0 ? 10 : 1000;
			a = 0;
		} else
		if (this.isPointInRegion(108, 16, 10, 16, x, y))
		{
			tileEntity.netI0 -= b == 0 ? 1 : 100;
			a = 0;
		} else
		if (this.isPointInRegion(148, 16, 10, 16, x, y))
		{
			tileEntity.netI0 += b == 0 ? 1 : 100;
			a = 0;
		} else
		if (this.isPointInRegion(158, 16, 10, 16, x, y))
		{
			tileEntity.netI0 += b == 0 ? 10 : 1000;
			a = 0;
		}
		if (a >= 0)
		{
			if (tileEntity.netI0 < 0) tileEntity.netI0 = 0;
			if (tileEntity.netI0 > Config.Umax[2]) tileEntity.netI0 = Config.Umax[2];
			PacketBuffer dos = tileEntity.getPacketTargetData();
			dos.writeByte(AutomatedTile.CmdOffset);
			dos.writeShort(tileEntity.netI0);
			BlockGuiHandler.sendPacketToServer(dos);
		}
		super.mouseClicked(x, y, b);
	}
	
}
