package cd4017be.automation.render;

import org.lwjgl.opengl.GL11;

import cd4017be.automation.Gui.GuiVertexShematicGen;
import cd4017be.automation.TileEntity.VertexShematicGen;
import cd4017be.automation.TileEntity.VertexShematicGen.Polygon;
import cd4017be.lib.util.Vec3;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

public class Render3DVertexShem extends TileEntitySpecialRenderer<VertexShematicGen> {

	@Override
	public void renderTileEntityAt(VertexShematicGen te, double x, double y, double z, float dt, int des) {
		//set state
		GlStateManager.bindTexture(0);
		GlStateManager.disableCull();
		GlStateManager.disableLighting(); 
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
		GlStateManager.depthMask(false);
		GlStateManager.color(1, 1, 1, 1);
		int density = 0x30;
		VertexBuffer t = Tessellator.getInstance().getBuffer();
		t.setTranslation(x, y, z);
		t.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_COLOR);
		Polygon p;
		int c, cr, cg, cb, ca;
		for (int k = 0; k < te.polygons.size(); k++) {
			p = te.polygons.get(k);
			c = GuiVertexShematicGen.colors[p.texId & 0x7];
			cr = c >> 16 & 0xff;
			cg = c >> 8 & 0xff;
			cb = c & 0xff;
			ca = (k == te.sel ? density * 2 : density);
			Vec3 v = Vec3.Def(p.dir==4?-1:p.dir==5?1:0, p.dir==0?-1:p.dir==1?1:0, p.dir==2?-1:p.dir==3?1:0).scale(p.thick);
			for (int i = 0; i <= p.vert.length - 3; i++) {
				t.pos(p.vert[0].x[0], p.vert[0].x[1], p.vert[0].x[2]).color(cr, cg, cb, ca).endVertex();
				t.pos(p.vert[i + 1].x[0], p.vert[i + 1].x[1], p.vert[i + 1].x[2]).color(cr, cg, cb, ca).endVertex();
				t.pos(p.vert[i + 2].x[0], p.vert[i + 2].x[1], p.vert[i + 2].x[2]).color(cr, cg, cb, ca).endVertex();
				
				t.pos(p.vert[0].x[0] + v.x, p.vert[0].x[1] + v.y, p.vert[0].x[2] + v.z).color(cr, cg, cb, ca).endVertex();
				t.pos(p.vert[i + 1].x[0] + v.x, p.vert[i + 1].x[1] + v.y, p.vert[i + 1].x[2] + v.z).color(cr, cg, cb, ca).endVertex();
				t.pos(p.vert[i + 2].x[0] + v.x, p.vert[i + 2].x[1] + v.y, p.vert[i + 2].x[2] + v.z).color(cr, cg, cb, ca).endVertex();
			}
		}
		t.setTranslation(0, 0, 0);
		Tessellator.getInstance().draw();
	}

}
