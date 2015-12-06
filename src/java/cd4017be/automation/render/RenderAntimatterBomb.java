/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cd4017be.automation.render;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import cd4017be.automation.Entity.EntityAntimatterExplosion1;
import cd4017be.lib.BlockItemRegistry;
import cd4017be.lib.util.VecN;

/**
 *
 * @author CD4017BE
 */
public class RenderAntimatterBomb extends Render
{
    private static final VecN[] ExplBallTex = {
        new VecN(0, 0, 0, 0, 0, 0),
        new VecN(0, 0, 0, 0, 0, 0),
        new VecN(0, 0, 0, 0, 0, 0),
        new VecN(0, 0, 0, 0, 0, 0),
        new VecN(0, 0, 0, 0, 0, 0),
        new VecN(0, 0, 0, 0, 0, 0),
        new VecN(0, 0, 0, 0, 0, 0),
        new VecN(0, 0, 0, 0, 0, 0),
        new VecN(0, 0, 0, 0, 0, 0),
        new VecN(0, 0, 0, 0, 0, 0),
        new VecN(0, 0, 0, 0, 0, 0),
        new VecN(0, 0, 0, 0, 0, 0),
        new VecN(0, 0, 0, 0, 0, 0),
        new VecN(0, 0, 0, 0, 0, 0),
        new VecN(0, 0, 0, 0, 0, 0),
        new VecN(0, 0, 0, 0, 0, 0),
        new VecN(0, 0, 0, 0, 0, 0),
        new VecN(0, 0, 0, 0, 0, 0),
        new VecN(0, 0, 0, 0, 0, 0),
        new VecN(0, 0, 0, 0, 0, 0)};
    private static final VecN[] ExplBallVert = {
        new VecN(0, 0, 0, 0, 0, 0, 0, 0, 0),
        new VecN(0, 0, 0, 0, 0, 0, 0, 0, 0),
        new VecN(0, 0, 0, 0, 0, 0, 0, 0, 0),
        new VecN(0, 0, 0, 0, 0, 0, 0, 0, 0),
        new VecN(0, 0, 0, 0, 0, 0, 0, 0, 0),
        new VecN(0, 0, 0, 0, 0, 0, 0, 0, 0),
        new VecN(0, 0, 0, 0, 0, 0, 0, 0, 0),
        new VecN(0, 0, 0, 0, 0, 0, 0, 0, 0),
        new VecN(0, 0, 0, 0, 0, 0, 0, 0, 0),
        new VecN(0, 0, 0, 0, 0, 0, 0, 0, 0),
        new VecN(0, 0, 0, 0, 0, 0, 0, 0, 0),
        new VecN(0, 0, 0, 0, 0, 0, 0, 0, 0),
        new VecN(0, 0, 0, 0, 0, 0, 0, 0, 0),
        new VecN(0, 0, 0, 0, 0, 0, 0, 0, 0),
        new VecN(0, 0, 0, 0, 0, 0, 0, 0, 0),
        new VecN(0, 0, 0, 0, 0, 0, 0, 0, 0),
        new VecN(0, 0, 0, 0, 0, 0, 0, 0, 0),
        new VecN(0, 0, 0, 0, 0, 0, 0, 0, 0),
        new VecN(0, 0, 0, 0, 0, 0, 0, 0, 0),
        new VecN(0, 0, 0, 0, 0, 0, 0, 0, 0)};
    private final RenderBlocks blockRenderer = new RenderBlocks();
    
    public RenderAntimatterBomb()
    {
        this.shadowSize = 0.5F;
    }

    public void renderAntimatterBomb(EntityAntimatterExplosion1 amBomb, double x, double y, double z, float par8, float par9)
    {
        GL11.glPushMatrix();
        GL11.glTranslatef((float)x, (float)y, (float)z);
        GL11.glRotatef(amBomb.rotationYaw, 0F, -1F, 0F);
        
        this.renderManager.renderEngine.bindTexture(TextureMap.locationBlocksTexture);
        this.blockRenderer.renderBlockAsItem(BlockItemRegistry.getBlock("tile.antimatterBombF"), 2, amBomb.getBrightness(par9));
        FontRenderer txtR = this.getFontRendererFromRenderManager();
        
        GL11.glTranslatef(0F, 0F, -0.51F);
        float f = 0.03125F;
        GL11.glScalef(-f, -f, f);
        if (amBomb.size <= 0)
        {    
            String s = (-amBomb.size) + "t";
            txtR.drawString(s, -txtR.getStringWidth(s) / 2, -4, 0xff0000);
        }
        
        GL11.glPopMatrix();
    }
    
    private void renderExplosion(EntityAntimatterExplosion1 amBomb, double x, double y, double z, float par8, float par9)
    {
        GL11.glPushMatrix();
        GL11.glTranslatef((float)x, (float)y, (float)z);
        
        Tessellator t = Tessellator.instance;
        t.startDrawing(GL11.GL_TRIANGLES);
        
        t.draw();
        GL11.glPopMatrix();
    }
    
    @Override
    public void doRender(Entity entity, double x, double y, double z, float j, float p) 
    {
        //if (((EntityAntimatterExplosion1)entity).size <= 0) this.renderAntimatterBomb((EntityAntimatterExplosion1)entity, x, y, z, j, p);
        //else this.renderExplosion((EntityAntimatterExplosion1)entity, x, y, z, j, p);
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity entity) 
    {
        return null;
    }
    
}
