/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cd4017be.automation.render;

import org.lwjgl.opengl.GL11;

import cd4017be.automation.TileEntity.Tank;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
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
	public static final byte[] CUBE = {
		//xyz uv
		0b001000, //B
		0b101010, 
		0b100011, 
		0b000001, 
		0b010000, //T
		0b110010, 
		0b111011, 
		0b011001, 
		0b000000, //N
		0b100010, 
		0b110011, 
		0b010001, 
		0b011000, //S
		0b111010, 
		0b101011, 
		0b001001, 
		0b000000, //W
		0b001010, 
		0b011011, 
		0b010001,
		0b110000, //E
		0b111010,
		0b101011,
		0b100001
	};
	
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
        
        this.renderCube(x + 0.0625D, y, z + 0.0625D, x + 0.9375D, y + n, z + 0.9375D, tex, c, l);
        
        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
	}
	
	public void renderCube(double X0, double Y0, double Z0, double X1, double Y1, double Z1, TextureAtlasSprite tex, int c, int l) {
		WorldRenderer t = Tessellator.getInstance().getWorldRenderer();
        t.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_LMAP_COLOR);
		double U0 = tex.getMinU(), U1 = tex.getMaxU(), V0 = tex.getMinV(), V1 = tex.getMaxV();
        int R = c >> 16 & 0xff, G = c >> 8 & 0xff, B = c & 0xff, A = c >> 24 & 0xff;
		int BL = l >> 16 & 0xffff, SL = l & 0xffff;
        
		t.pos(X0, Y0, Z0).tex(U0, V0).lightmap(BL, SL).color(R, G, B, A).endVertex();
        t.pos(X1, Y0, Z0).tex(U1, V0).lightmap(BL, SL).color(R, G, B, A).endVertex();
        t.pos(X1, Y0, Z1).tex(U1, V1).lightmap(BL, SL).color(R, G, B, A).endVertex();
        t.pos(X0, Y0, Z1).tex(U0, V1).lightmap(BL, SL).color(R, G, B, A).endVertex();
        //Top
        t.pos(X0, Y1, Z1).tex(U0, V0).lightmap(BL, SL).color(R, G, B, A).endVertex();
        t.pos(X1, Y1, Z1).tex(U1, V0).lightmap(BL, SL).color(R, G, B, A).endVertex();
        t.pos(X1, Y1, Z0).tex(U1, V1).lightmap(BL, SL).color(R, G, B, A).endVertex();
        t.pos(X0, Y1, Z0).tex(U0, V1).lightmap(BL, SL).color(R, G, B, A).endVertex();
        //North
        t.pos(X0, Y1, Z0).tex(U0, V0).lightmap(BL, SL).color(R, G, B, A).endVertex();
        t.pos(X1, Y1, Z0).tex(U1, V0).lightmap(BL, SL).color(R, G, B, A).endVertex();
        t.pos(X1, Y0, Z0).tex(U1, V1).lightmap(BL, SL).color(R, G, B, A).endVertex();
        t.pos(X0, Y0, Z0).tex(U0, V1).lightmap(BL, SL).color(R, G, B, A).endVertex();
        //South
        t.pos(X0, Y0, Z1).tex(U0, V0).lightmap(BL, SL).color(R, G, B, A).endVertex();
        t.pos(X1, Y0, Z1).tex(U1, V0).lightmap(BL, SL).color(R, G, B, A).endVertex();
        t.pos(X1, Y1, Z1).tex(U1, V1).lightmap(BL, SL).color(R, G, B, A).endVertex();
        t.pos(X0, Y1, Z1).tex(U0, V1).lightmap(BL, SL).color(R, G, B, A).endVertex();
        //East
        t.pos(X0, Y1, Z0).tex(U0, V0).lightmap(BL, SL).color(R, G, B, A).endVertex();
        t.pos(X0, Y0, Z0).tex(U1, V0).lightmap(BL, SL).color(R, G, B, A).endVertex();
        t.pos(X0, Y0, Z1).tex(U1, V1).lightmap(BL, SL).color(R, G, B, A).endVertex();
        t.pos(X0, Y1, Z1).tex(U0, V1).lightmap(BL, SL).color(R, G, B, A).endVertex();
        //West
        t.pos(X1, Y0, Z0).tex(U0, V0).lightmap(BL, SL).color(R, G, B, A).endVertex();
        t.pos(X1, Y1, Z0).tex(U1, V0).lightmap(BL, SL).color(R, G, B, A).endVertex();
        t.pos(X1, Y1, Z1).tex(U1, V1).lightmap(BL, SL).color(R, G, B, A).endVertex();
        t.pos(X1, Y0, Z1).tex(U0, V1).lightmap(BL, SL).color(R, G, B, A).endVertex();
        
        Tessellator.getInstance().draw();
	}
}
