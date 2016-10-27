/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cd4017be.automation.Gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import org.lwjgl.opengl.GL11;

/**
 *
 * @author CD4017BE
 */
public class GuiSlider  extends GuiButton
{
	public float sliderValue;
	public float maxValue = 1.0F;
	public float minValue = 0.0F;
	public String formatString;
	public boolean dragging;
	public boolean update;
	
	public GuiSlider(int id, int x, int y, int w, int h, String name, float min, float max)
	{
		super(id, x, y, w, h, String.format(name, min));
		this.formatString = name;
		this.minValue = min;
		this.maxValue = max;
		this.sliderValue = min;
	}

	@Override
	public int getHoverState(boolean par1)
	{
		return 0;
	}

	@Override
	protected void mouseDragged(Minecraft par1Minecraft, int x, int y)
	{
		if (this.visible)
		{
			if (this.dragging)
			{
				this.sliderValue = (float)(x - (this.xPosition + 4)) / (float)(this.width - 8);

				if (this.sliderValue < 0)
				{
					this.sliderValue = 0;
				}

				if (this.sliderValue > 1)
				{
					this.sliderValue = 1;
				}

				this.displayString = String.format(formatString, this.sliderValue * (this.maxValue - this.minValue) + this.minValue);
			}

			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			this.drawTexturedModalRect(this.xPosition + (int)(this.sliderValue * (float)(this.width - 8)), this.yPosition, 0, 66, 4, 20);
			this.drawTexturedModalRect(this.xPosition + (int)(this.sliderValue * (float)(this.width - 8)) + 4, this.yPosition, 196, 66, 4, 20);
		}
	}

	@Override
	public boolean mousePressed(Minecraft par1Minecraft, int x, int y)
	{
		if (super.mousePressed(par1Minecraft, x, y)) {
			this.sliderValue = (float)(x - (this.xPosition + 4)) / (float)(this.width - 8);

			if (this.sliderValue < 0.0F)
			{
				this.sliderValue = 0.0F;
			}

			if (this.sliderValue > 1.0F)
			{
				this.sliderValue = 1.0F;
			}

			this.displayString = String.format(formatString, this.sliderValue * (this.maxValue - this.minValue) + this.minValue);
			this.dragging = true;
			update = true;
			return true;
		} else if (update) {
			update = false;
			return true;
		} else return false;
	}

	@Override
	public void mouseReleased(int par1, int par2)
	{
		this.dragging = false;
	}
	
	public void setValue(float v)
	{
		this.sliderValue = (v - this.minValue) / (this.maxValue - this.minValue);
		if (this.sliderValue < 0) this.sliderValue = 0;
		if (this.sliderValue > 1) this.sliderValue = 1;
		this.displayString = String.format(formatString, this.sliderValue * (this.maxValue - this.minValue) + this.minValue);
	}
	
	public float getValue()
	{
		return this.sliderValue * (this.maxValue - this.minValue) + this.minValue;
	}
}
