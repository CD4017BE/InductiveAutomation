package cd4017be.automation.render;

import org.lwjgl.opengl.GL11;

import cd4017be.automation.Gui.GuiVertexShematicGen;
import cd4017be.automation.TileEntity.VertexShematicGen;
import cd4017be.automation.TileEntity.VertexShematicGen.Polygon;
import cd4017be.lib.util.Vec3;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

public class Render3DVertexShem extends TileEntitySpecialRenderer<VertexShematicGen>
{

	@Override
	public void renderTileEntityAt(VertexShematicGen te, double x, double y, double z, float dt, int des) {
		//save state
        int Vbs = GL11.glGetInteger(GL11.GL_BLEND_SRC),
        		Vbd = GL11.glGetInteger(GL11.GL_BLEND_DST);
        boolean Vc = GL11.glIsEnabled(GL11.GL_CULL_FACE),
        		Vl = GL11.glIsEnabled(GL11.GL_LIGHTING),
        		Vb = GL11.glIsEnabled(GL11.GL_BLEND);
        //set state
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_LIGHTING); 
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDepthMask(false);
        GL11.glColor4f(1, 1, 1, 1);
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
        //reset state
        GL11.glDepthMask(true);
        GL11.glBlendFunc(Vbs, Vbd);
        if (!Vb) GL11.glDisable(GL11.GL_BLEND);
        if (Vl) GL11.glEnable(GL11.GL_LIGHTING);
        if (Vc) GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, Minecraft.getMinecraft().getTextureMapBlocks().getGlTextureId());
	}

}
