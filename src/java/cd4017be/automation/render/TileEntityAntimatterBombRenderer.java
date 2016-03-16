/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cd4017be.automation.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import org.lwjgl.opengl.GL11;

import cd4017be.automation.TileEntity.AntimatterBomb;
import cd4017be.lib.util.Utils;

/**
 *
 * @author CD4017BE
 */
public class TileEntityAntimatterBombRenderer extends TileEntitySpecialRenderer<AntimatterBomb>
{
	@Override
	public void renderTileEntityAt(AntimatterBomb te, double x, double y, double z, float dt, int des) {
		GL11.glPushMatrix();
        GL11.glTranslatef((float)x + 0.5F, (float)y + 0.5F, (float)z + 0.5F);
        
        int r = te.getOrientation();
        float rotationYaw = r == 2 ? 0F : r == 5 ? 90F : r == 3 ? 180F : 270F;
        GL11.glRotatef(rotationYaw, 0F, -1F, 0F);
        
        FontRenderer txtR = Minecraft.getMinecraft().fontRendererObj;
        
        GL11.glTranslatef(0F, 0F, -0.51F);
        float f = 0.03125F;
        GL11.glScalef(-f, -f, f);    
        String s;
        if (te.timer >= 0) {
            s = (te.timer) + "t";
        } else {
            s = Utils.formatNumber(te.antimatter * 0.000000001D, 3, 0) + "g";
        }
        txtR.drawString(s, -txtR.getStringWidth(s) / 2, -4, 0xff0000);
        GL11.glPopMatrix();
	}
}
