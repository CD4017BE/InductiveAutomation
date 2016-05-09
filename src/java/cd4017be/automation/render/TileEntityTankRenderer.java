/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cd4017be.automation.render;

import org.lwjgl.opengl.GL11;

import cd4017be.automation.TileEntity.Tank;
import cd4017be.lib.render.TESRModelParser;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

/**
 *
 * @author CD4017BE
 */
public class TileEntityTankRenderer extends TileEntitySpecialRenderer<Tank>
{
	public static final String model = "automation:models/tileEntity/fluidTank";
	
	@Override
	public void renderTileEntityAt(Tank te, double x, double y, double z, float partialTicks, int destroyStage) {
		FluidStack fluid = te.tanks.getFluid(0);
        if (fluid == null) return;
        ResourceLocation res = fluid.getFluid().getStill();
        if (res == null) return;
        TextureAtlasSprite tex = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(res.toString());
        if (tex == null) return;
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDepthMask(false);
        GL11.glColor4f(1, 1, 1, 1);
        double n = (double)te.tanks.getAmount(0) / (double)te.tanks.tanks[0].cap;
        int l = te.getWorld().getCombinedLight(te.getPos(), fluid.getFluid().getLuminosity(fluid));
        int c = fluid.getFluid().getColor(fluid);
        Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.locationBlocksTexture);
        GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5D, y, z + 0.5D);
        GL11.glScaled(0.875D, n, 0.875D);
        VertexBuffer t = Tessellator.getInstance().getBuffer();
        t.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
        TESRModelParser.renderWithTOCB(t, model, tex, -0.5F, 0F, -0.5F, c, l);
        Tessellator.getInstance().draw();
        GL11.glPopMatrix();
        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
	}
	
}
