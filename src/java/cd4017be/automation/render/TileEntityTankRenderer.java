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
        float n = (float)te.tanks.getAmount(0) / (float)te.tanks.tanks[0].cap;
        int l = te.getWorld().getCombinedLight(te.getPos(), fluid.getFluid().getLuminosity(fluid));
        int c = fluid.getFluid().getColor(fluid);
        Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.locationBlocksTexture);
        WorldRenderer t = Tessellator.getInstance().getWorldRenderer();
        t.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_LMAP_COLOR);
        t.addVertexData(this.renderCube(0.0625F, 0F, 0.0625F, 0.9375F, n, 0.9375F, tex, c, l));
        Tessellator.getInstance().draw();
        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
	}
	
	public int[] renderCube(float X0, float X1, float Y0, float Y1, float Z0, float Z1, TextureAtlasSprite tex, int c, int l) {
		int[] data = new int[CUBE.length * 7];
		int x0 = Float.floatToIntBits(X0), x1 = Float.floatToIntBits(X1), 
			y0 = Float.floatToIntBits(Y0), y1 = Float.floatToIntBits(Y1), 
			z0 = Float.floatToIntBits(Z0), z1 = Float.floatToIntBits(Z1),
			u0 = Float.floatToIntBits(tex.getMinU()), u1 = Float.floatToIntBits(tex.getMaxU()),
			v0 = Float.floatToIntBits(tex.getMinV()), v1 = Float.floatToIntBits(tex.getMaxV());
		int j;
		byte d;
		for (int i = 0; i < CUBE.length; i++) {
			j = i * 7;
			d = CUBE[i];
			data[j] 	= (d & 0b100000) != 0 ? x1 : x0;
			data[j + 1] = (d & 0b010000) != 0 ? y1 : y0;
			data[j + 2] = (d & 0b001000) != 0 ? z1 : z0;
			data[j + 3] = c;
			data[j + 4] = (d & 0b000010) != 0 ? u1 : u0;
			data[j + 5] = (d & 0b000001) != 0 ? v1 : v0;
			data[j + 6] = l;
		}
		return data;
	}
}
