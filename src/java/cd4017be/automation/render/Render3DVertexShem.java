package cd4017be.automation.render;

import org.lwjgl.opengl.GL11;

import cd4017be.automation.Gui.GuiVertexShematicGen;
import cd4017be.automation.TileEntity.VertexShematicGen;
import cd4017be.automation.TileEntity.VertexShematicGen.Polygon;
import cd4017be.lib.util.Vec3;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

public class Render3DVertexShem extends TileEntitySpecialRenderer 
{
    
	private void render(VertexShematicGen te, double d0, double d1, double d2) 
	{	
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDepthMask(false);
        GL11.glColor4f(1, 1, 1, 1);
        int density = 0x30;
        Tessellator t = Tessellator.instance;
        t.setTranslation(d0, d1, d2);
        t.startDrawing(GL11.GL_TRIANGLES);
        Polygon p;
        for (int k = 0; k < te.polygons.size(); k++) {
        	p = te.polygons.get(k);
        	int c = GuiVertexShematicGen.colors[p.texId & 0x7];
        	Vec3 v = Vec3.Def(p.dir==4?-1:p.dir==5?1:0, p.dir==0?-1:p.dir==1?1:0, p.dir==2?-1:p.dir==3?1:0).scale(p.thick);
        	t.setColorRGBA(c >> 16 & 0xff, c >> 8 & 0xff, c & 0xff, k == te.sel ? density * 2 : density);
        	for (int i = 0; i <= p.vert.length - 3; i++) {
        		t.addVertex(p.vert[0].x[0], p.vert[0].x[1], p.vert[0].x[2]);
        		t.addVertex(p.vert[i + 1].x[0], p.vert[i + 1].x[1], p.vert[i + 1].x[2]);
        		t.addVertex(p.vert[i + 2].x[0], p.vert[i + 2].x[1], p.vert[i + 2].x[2]);
        		
        		t.addVertex(p.vert[0].x[0] + v.x, p.vert[0].x[1] + v.y, p.vert[0].x[2] + v.z);
        		t.addVertex(p.vert[i + 1].x[0] + v.x, p.vert[i + 1].x[1] + v.y, p.vert[i + 1].x[2] + v.z);
        		t.addVertex(p.vert[i + 2].x[0] + v.x, p.vert[i + 2].x[1] + v.y, p.vert[i + 2].x[2] + v.z);
        	}
        }
        t.draw();
        t.setTranslation(0, 0, 0);
        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
	}
	
    @Override
    public void renderTileEntityAt(TileEntity te, double d0, double d1, double d2, float f) 
    {
        this.render((VertexShematicGen)te, d0, d1, d2);
    }

}
