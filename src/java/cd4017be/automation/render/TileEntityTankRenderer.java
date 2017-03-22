package cd4017be.automation.render;

import org.lwjgl.opengl.GL11;

import cd4017be.automation.TileEntity.Tank;
import cd4017be.lib.render.TESRModelParser;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

/**
 *
 * @author CD4017BE
 */
public class TileEntityTankRenderer extends TileEntitySpecialRenderer<Tank> {

	public static final String model = "automation:models/tileEntity/fluidTank";

	@Override
	public void renderTileEntityAt(Tank te, double x, double y, double z, float partialTicks, int destroyStage) {
		FluidStack fluid = te.tanks.fluids[0];
		if (fluid != null) renderFluid(fluid, te, x, y, z, 0.875D, (double)fluid.amount / (double)te.tanks.tanks[0].cap);
	}

	public static void renderFluid(FluidStack fluid, TileEntity te, double x, double y, double z, double dxz, double dy) {
		ResourceLocation res = fluid.getFluid().getStill();
		if (res == null) return;
		TextureAtlasSprite tex = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(res.toString());
		if (tex == null) return;
		GlStateManager.disableLighting();
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
		GlStateManager.depthMask(false);
		GlStateManager.color(1, 1, 1, 1);
		int l = te.getWorld().getCombinedLight(te.getPos(), fluid.getFluid().getLuminosity(fluid));
		int c = fluid.getFluid().getColor(fluid);
		Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		GlStateManager.pushMatrix();
		GlStateManager.translate(x + 0.5D, y, z + 0.5D);
		GlStateManager.scale(dxz, dy, dxz);
		VertexBuffer t = Tessellator.getInstance().getBuffer();
		t.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
		TESRModelParser.renderWithTOCB(t, model, tex, -0.5F, 0F, -0.5F, c, l);
		Tessellator.getInstance().draw();
		GlStateManager.popMatrix();
	}

}
