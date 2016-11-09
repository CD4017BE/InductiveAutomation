package cd4017be.automation.render;

import org.lwjgl.opengl.GL11;
import cd4017be.automation.TileEntity.Shaft;
import cd4017be.automation.jetpack.TickHandler;
import cd4017be.automation.shaft.ShaftComponent;
import cd4017be.automation.shaft.ShaftPhysics;
import cd4017be.lib.render.TESRModelParser;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ShaftRenderer extends TileEntitySpecialRenderer<Shaft> {

	@Override
	public void renderTileEntityAt(Shaft te, double x, double y, double z, float partialTicks, int destroyStage) {
		ShaftPhysics phys = te.physics();
		if (phys.lastRendered != TickHandler.tick) this.renderShaft(te.getWorld(), phys, x - te.getPos().getX(), y - te.getPos().getY(), z - te.getPos().getZ(), partialTicks);
	}

	private static final ResourceLocation texture = new ResourceLocation("automation:textures/tileEntity/shaft.png");
	private static final String[] models = {
		"automation:models/tileEntity/shaft",
		"automation:models/tileEntity/shaftPermMag",
		"automation:models/tileEntity/shaftCoilC",
		"automation:models/tileEntity/shaftCoilA",
		"automation:models/tileEntity/shaftCoilH",
		"automation:models/tileEntity/shaftGear" //Mass
		};
	
	private void renderShaft(World world, ShaftPhysics shaft, double x, double y, double z, float t) {
		synchronized(shaft) {
			shaft.lastRendered = TickHandler.tick;
			RenderHelper.disableStandardItemLighting();
			GlStateManager.blendFunc(770, 771);
			GlStateManager.enableBlend();
			GlStateManager.enableCull();
			if (Minecraft.isAmbientOcclusionEnabled()) GlStateManager.shadeModel(7425);
			else GlStateManager.shadeModel(7424);
			x += shaft.pos.getX() + 0.5D;
			y += shaft.pos.getY() + 0.5D;
			z += shaft.pos.getZ() + 0.5D;
			GlStateManager.pushMatrix();
			GlStateManager.translate(x, y, z);
			if (shaft.axis == 1) GlStateManager.rotate(90F, 1, 0, 0);
			else if (shaft.axis == 2) GlStateManager.rotate(90F, 0, 0, 1);
			GlStateManager.rotate((shaft.s + t * 0.05F * shaft.v) * 360F, 0, 1, 0);
			this.bindTexture(texture);
			VertexBuffer render = Tessellator.getInstance().getBuffer();
			render.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
			if (shaft.model != null) render.addVertexData(shaft.model);
			else {
				for (ShaftComponent comp : shaft.components.values()) {
					BlockPos pos = ((TileEntity)comp.tile).getPos();
					int l = world.getCombinedLight(pos, 0);
					pos = pos.subtract(shaft.pos);
					String model = models[comp.type];
					if (shaft.axis == 1) TESRModelParser.renderWithOffsetAndBrightness(render, model, pos.getX(), pos.getZ(), -pos.getY(), l);
					else if (shaft.axis == 2) TESRModelParser.renderWithOffsetAndBrightness(render, model, pos.getY(), -pos.getX(), pos.getZ(), l);
					else TESRModelParser.renderWithOffsetAndBrightness(render, model, pos.getX(), pos.getY(), pos.getZ(), l);
				}
				//shaft.model = new int[render.getVertexCount() * 7];
				//ByteBuffer buf = render.getByteBuffer();
				//buf.position(0);
				//buf.asIntBuffer().get(shaft.model);
			}
			Tessellator.getInstance().draw();
			GlStateManager.popMatrix();
		}
	}

}
