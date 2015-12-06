/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cd4017be.automation.render;

import org.lwjgl.opengl.GL11;

import cd4017be.automation.TileEntity.Tank;
import cd4017be.lib.BlockItemRegistry;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraftforge.fluids.FluidStack;

/**
 *
 * @author CD4017BE
 */
public class TileEntityTankRenderer extends TileEntitySpecialRenderer
{
    private RenderBlocks renderer;
    private RenderManager manager;
    
    public TileEntityTankRenderer()
    {
        renderer = new RenderBlocks();
        renderer.enableAO = false;
        manager = RenderManager.instance;
    }
    
    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float f) 
    {
        this.render((Tank)te, x, y, z, f);
    }
    
    private void render(Tank te, double x, double y, double z, float f)
    {
        FluidStack fluid = te.tanks.getFluid(0);
        if (fluid == null) return;
        IIcon tex = fluid.getFluid().getIcon(fluid);
        if (tex == null) return;
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDepthMask(false);
        GL11.glColor4f(1, 1, 1, 1);
        float n = (float)te.tanks.getAmount(0) / (float)te.tanks.tanks[0].cap;
        renderer.blockAccess = te.getWorldObj();
        manager.renderEngine.bindTexture(TextureMap.locationBlocksTexture);
        Tessellator t = Tessellator.instance;
        t.startDrawingQuads();
        renderer.setRenderBounds(0.0625D, 0D, 0.0625D, 0.9375D, n, 0.9375D);
        Block block = BlockItemRegistry.getBlock("tile.tank");
        t.setBrightness(block.getMixedBrightnessForBlock(te.getWorldObj(), te.xCoord, te.yCoord, te.zCoord));
        renderer.renderFaceYNeg(block, x, y, z, tex);
        renderer.renderFaceYPos(block, x, y, z, tex);
        renderer.renderFaceZNeg(block, x, y, z, tex);
        renderer.renderFaceZPos(block, x, y, z, tex);
        renderer.renderFaceXNeg(block, x, y, z, tex);
        renderer.renderFaceXPos(block, x, y, z, tex);
        t.draw();
        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
    }
}
