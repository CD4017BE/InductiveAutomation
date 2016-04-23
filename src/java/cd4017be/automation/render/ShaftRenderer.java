package cd4017be.automation.render;

import org.lwjgl.opengl.GL11;

import cd4017be.automation.TileEntity.Shaft;
import cd4017be.automation.shaft.ShaftComponent;
import cd4017be.automation.shaft.ShaftPhysics;
import cd4017be.lib.render.SpecialModelLoader;
import cd4017be.lib.render.TESRModelParser;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ResourceLocation;

public class ShaftRenderer extends TileEntitySpecialRenderer<Shaft> {

	@Override
	public void renderTileEntityAt(Shaft te, double x, double y, double z, float partialTicks, int destroyStage) {
		if (te.shaft.network.isCore(te)) this.renderShaft(te.shaft.network, x - te.getPos().getX(), y - te.getPos().getY(), z - te.getPos().getZ(), partialTicks);
	}
	
	private void renderShaft(ShaftPhysics shaft, double x, double y, double z, float t) {
		RenderHelper.disableStandardItemLighting();
        GlStateManager.blendFunc(770, 771);
        GlStateManager.enableBlend();
        GlStateManager.enableCull();
        if (Minecraft.isAmbientOcclusionEnabled()) GlStateManager.shadeModel(7425);
        else GlStateManager.shadeModel(7424);
        GlStateManager.color(1, 1, 1, 1);
		BlockPos pos = shaft.pos();
		byte ax = shaft.ax();
		x += pos.getX() + 0.5D;
		y += pos.getY() + 0.5D;
		z += pos.getZ() + 0.5D;
		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, z);
		if (ax == 1) GlStateManager.rotate(90F, 1, 0, 0);
		else if (ax == 2) GlStateManager.rotate(90F, 0, 0, 1);
		GlStateManager.rotate((shaft.s + t * 0.05F * shaft.v) * 360F, 0, 1, 0);
		this.bindTexture(new ResourceLocation("automation:textures/tileEntity/shaft.png"));
		WorldRenderer render = Tessellator.getInstance().getWorldRenderer();
		render.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
		int[] data = SpecialModelLoader.instance.tesrModelData.get(new ResourceLocation("automation:models/tesr/shaft.txt"));
		for (ShaftComponent comp : shaft.components.values()) {
			pos = comp.shaft.getPos().subtract(shaft.pos());
			if (ax == 1) TESRModelParser.renderWithOffset(render, data, pos.getX(), pos.getZ(), -pos.getY());
			else if (ax == 2) TESRModelParser.renderWithOffset(render, data, pos.getY(), -pos.getX(), pos.getZ());
			else TESRModelParser.renderWithOffset(render, data, pos.getX(), pos.getY(), pos.getZ());
		}
		Tessellator.getInstance().draw();
		GlStateManager.popMatrix();
	}

}