/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cd4017be.automation.Gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;

import org.lwjgl.opengl.GL11;

import cd4017be.automation.TileEntity.Farm;
import cd4017be.lib.Gui.GuiMachine;
import cd4017be.lib.Gui.TileContainer;

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
		this.drawInfo(79, 52, 16, 16, "\\i", "farm.in");
		this.drawInfo(81, 70, 16, 16, "\\i", "farm.out");
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) 
	{
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.renderEngine.bindTexture(new ResourceLocation("automation", "textures/gui/farm.png"));
		this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
		this.drawStringCentered(tileEntity.getName(), this.guiLeft + this.xSize / 2, this.guiTop + 4, 0x404040);
		this.drawStringCentered(I18n.translateToLocal("container.inventory"), this.guiLeft + this.xSize / 2, this.guiTop + 92, 0x404040);
		super.drawGuiContainerBackgroundLayer(var1, var2, var3);
	}
	
}
