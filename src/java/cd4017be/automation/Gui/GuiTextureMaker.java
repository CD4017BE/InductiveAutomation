/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cd4017be.automation.Gui;

import java.io.IOException;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import cd4017be.automation.Item.ItemFluidDummy;
import cd4017be.automation.TileEntity.TextureMaker;
import cd4017be.lib.BlockGuiHandler;
import cd4017be.lib.TileContainer;
import cd4017be.lib.templates.GuiMachine;

/**
 *
 * @author CD4017BE
 */
public class GuiTextureMaker extends GuiMachine
{
    public static final short CMD_Button = 0;
    public static final short CMD_Value = 3;
    public static final short CMD_Draw = 1;
    public static final short CMD_Name = 2;
    
    private int oX;
    private int oY;
    private int tool = 0;
    private int block = 0;
    private int sx0;
    private int sy0;
    private int sx1;
    private int sy1;
    private int dx;
    private int dy;
    private int px;
    private int py;
    private final TextureMaker tileEntity;
    private GuiTextField textField;
    
    public GuiTextureMaker(TextureMaker tileEntity, EntityPlayer player)
    {
        super(new TileContainer(tileEntity, player));
        this.tileEntity = tileEntity;
    }
    
    @Override
    public void initGui() 
    {
        this.xSize = 288;
        this.ySize = 346;
        this.oX = 173;
        this.oY = 166;
        super.initGui();
        this.textField = new GuiTextField(0, fontRendererObj, guiLeft + oX + 25, guiTop + oY + 136, 82, 16);
    }

    @Override
    public void updateScreen() 
    {
        super.updateScreen();
        if (dx != tileEntity.width || dy != tileEntity.height) {
        	dx = tileEntity.width;
        	dy = tileEntity.height;
        	sx0 = 0; sx1 = dx;
        	sy0 = 0; sy1 = dy;
        }
        textField.updateCursorCounter();
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mx, int my) 
    {
        super.drawGuiContainerForegroundLayer(mx, my);
        this.drawInfo(59 + oX, 98 + oY, 16, 16, "\\i", "tex.draw");
        this.drawInfo(75 + oX, 98 + oY, 16, 16, "\\i", "tex.fill");
        this.drawInfo(91 + oX, 98 + oY, 16, 16, "\\i", "tex.repl");
        this.drawInfo(59 + oX, 116 + oY, 16, 16, "\\i", "tex.sel");
        this.drawInfo(75 + oX, 116 + oY, 16, 16, "\\i", "tex.copy");
        this.drawInfo(91 + oX, 116 + oY, 16, 16, "\\i", "tex.move");
        this.drawInfo(5 + oX, 98 + oY, 16, 16, "\\i", "tex.load");
        this.drawInfo(5 + oX, 116 + oY, 16, 16, "\\i", "tex.save");
        this.drawInfo(23 + oX, 98 + oY, 16, 16, "\\i", "tex.mirH");
        this.drawInfo(23 + oX, 116 + oY, 16, 16, "\\i", "tex.mirV");
        this.drawInfo(41 + oX, 98 + oY, 16, 16, "\\i", "tex.rotR");
        this.drawInfo(41 + oX, 116 + oY, 16, 16, "\\i", "tex.del");
        this.drawInfo(5 + oX, 156 + oY, 16, 16, "\\i", "tex.ext");
        this.drawInfo(41 + oX, 156 + oY, 16, 16, "\\i", "tex.size");
        this.drawInfo(84 + oX, 156 + oY, 16, 16, "\\i", "tex.ofs");
        this.drawInfo(25 + oX, 136 + oY, 82, 16, "\\i", "tex.name");
    }
    
    @Override
    protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) 
    {
    	this.drawHorizontalLine(this.guiLeft - 1, this.guiLeft + 256, this.guiTop - 1, 0xff000000);
        this.drawVerticalLine(this.guiLeft - 1, this.guiTop - 1, this.guiTop + 256, 0xff000000);
    	GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.renderEngine.bindTexture(new ResourceLocation("automation", "textures/gui/texMaker.png"));
        this.drawTexturedModalRect(this.guiLeft, this.guiTop + 256, 0, 0, 173, 90);
        this.drawTexturedModalRect(this.guiLeft + oX, this.guiTop + 256, 0, 90, 115, 90);
        this.drawTexturedModalRect(this.guiLeft + 256, this.guiTop, 224, 0, 32, 256);
        
        this.drawTexturedModalRect(this.guiLeft + 264, this.guiTop + 8 + 16 * block, 131, 90, 16, 16);
        this.drawTexturedModalRect(this.guiLeft + oX + 59 + 16 * (tool % 3), this.guiTop + oY + 98 + 18 * (tool / 3), 115, 90, 16, 16);
        this.drawTexturedModalRect(this.guiLeft + oX + 4, this.guiTop + oY + 155, 115 + 18 * tileEntity.netData.ints[0], 106, 18, 18);
        textField.drawTextBox();
        this.drawStringCentered("" + tileEntity.width, this.guiLeft + oX + 49, this.guiTop + oY + 156, 0x404040);
        this.drawStringCentered("" + tileEntity.height, this.guiLeft + oX + 49, this.guiTop + oY + 165, 0x404040);
        this.drawStringCentered("" + tileEntity.netData.ints[1], this.guiLeft + oX + 92, this.guiTop + oY + 156, 0x404040);
        this.drawStringCentered("" + tileEntity.netData.ints[2], this.guiLeft + oX + 92, this.guiTop + oY + 165, 0x404040);
        
        drawTexture();
    }
    
    private void drawTexture()
    {
        if (dx <= 0 || dy <= 0) return;
        BlockModelShapes bms = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes();
        TextureAtlasSprite[] icons = new TextureAtlasSprite[16];
        IBakedModel model;
        boolean[] opaque = new boolean[16];
        for (int i = 0; i < icons.length; i++) {
        	if (i > 0 && tileEntity.inventory[i] != null) {
        		ItemStack item = tileEntity.inventory[i];
        		if (item.getItem() instanceof ItemBlock) {
        			ItemBlock ib = (ItemBlock)item.getItem();
        			IBlockState state = ib.block.getStateFromMeta(ib.getMetadata(item.getItemDamage()));
        			model = bms.getModelForState(state);
        			if (model != null) icons[i] = model.getParticleTexture();
        			opaque[i] = icons[i] != null && ib.block.isOpaqueCube(state);
        		} else if (item.getItem() instanceof ItemFluidDummy) {
        			Fluid fluid = ItemFluidDummy.fluid(item);
        			if (fluid != null) {
        				ResourceLocation res = fluid.getStill();
        				if (res != null) icons[i] = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(res.toString());
        			}
        			opaque[i] = false;
        		}
        	}
        }
        int dmax = Math.max(dx, dy);
        GL11.glPushMatrix();
        	GL11.glTranslatef(this.guiLeft, this.guiTop, 0);
        	GL11.glScalef(16F / (float)dmax, 16F / (float)dmax, 1F);
        	GL11.glEnable(GL11.GL_BLEND);
            OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        	GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            this.mc.renderEngine.bindTexture(new ResourceLocation("automation", "textures/gui/texMaker.png"));
        	byte p;
            for (int y = 0; y < dy; y++)
        		for (int x = 0; x < dx; x++) {
        			p = tileEntity.getPixel(x, y);
        			if (!opaque[p])
        				this.drawTexturedModalRect(x * 16, y * 16, 208, 16 * p, 16, 16);
        		}
        	this.mc.renderEngine.bindTexture(TextureMap.locationBlocksTexture);
        	for (int y = 0; y < dy; y++)
        		for (int x = 0; x < dx; x++) {
        			TextureAtlasSprite icon = icons[tileEntity.getPixel(x, y)];
        			if (icon != null)
        				this.drawTexturedModalRect(x * 16, y * 16, icon, 16, 16);
        		}
        	GL11.glDisable(GL11.GL_BLEND);
        	int x0 = sx0 * 16, y0 = sy0 * 16, x1 = sx1 * 16 - 1, y1 = sy1 * 16 - 1, c = 0x8000ff00;
        	this.drawHorizontalLine(x0, x1, y0, c);
        	this.drawHorizontalLine(x0, x1, y1, c);
        	this.drawVerticalLine(x0, y0, y1, c);
        	this.drawVerticalLine(x1, y0, y1, c);
        	if (tool > 3) {
        		this.drawGradientRect(px * 16 + 2, py * 16 + 2, px * 16 + 14, py * 16 + 14, 0x4000ff00, 0x4000ff00);
        	}
        GL11.glPopMatrix();
    }

    @Override
    protected void mouseClicked(int x, int y, int b) throws IOException 
    {
        if (this.isPointInRegion(264, 8, 16, 240, x, y)) {
        	block = (y - this.guiTop - 8) / 16;
        	if (block < 0 || block >= 15) block = 0;
        	if (b != 0) super.mouseClicked(x, y, b);
        } else if (this.isPointInRegion(59 + oX, 97 + oY, 48, 36, x, y)) {
        	int t = (x - this.guiLeft - oX - 59) / 16;
        	if (y - this.guiTop - oY >= 115) t += 3;
        	if (t >= 0 && t < 6) tool = t;
        	if (tool > 3) {px = sx0; py = sy0;}
        } else if (this.isPointInRegion(4 + oX, 97 + oY, 54, 36, x, y)) {
        	int t = (x - this.guiLeft - oX - 4) / 18 * 2 + (y - this.guiTop - oY - 97) / 18;
        	if (t >= 0 && t < 6) this.sendButtonClick(t);
        } else if (this.isPointInRegion(0, 0, 256, 256, x, y)) {
            int dmax = Math.max(dx, dy);
        	int ox = (x - this.guiLeft) * dmax / 256;
        	int oy = (y - this.guiTop) * dmax / 256;
        	if (ox < 0 || oy < 0 || ox >= dx || oy >= dy) return;
        	if (tool == 0) {px = ox; py = oy;}
        	if (tool < 3) this.sendToolUse(ox, oy, b == 0 ? block + 1 : 0);
        	else if (tool == 3) {
        		if (b == 0) {
        			if (ox < sx1) sx0 = ox;
        			else {sx0 = sx1 - 1; sx1 = ox + 1;}
        			if (oy < sy1) sy0 = oy;
        			else {sy0 = sy1 - 1; sy1 = oy + 1;}
        		} else {
        			if (ox >= sx0) sx1 = ox + 1;
        			else {sx1 = sx0 + 1; sx0 = ox;}
        			if (oy >= sy0) sy1 = oy + 1;
        			else {sy1 = sy0 + 1; sy0 = oy;}
        		}
        	} else if (b != 0) {
        		px = ox; py = oy;
        	} else {
        		int dx = ox - px, dy = oy - py;
        		this.sendToolUse(dx, dy, 0);
        		sx0 += dx; sy0 += dy;
        		sx1 += dx; sy1 += dy;
        		px += dx; py += dy;
        		if (sx0 < 0) sx0 = 0;
        		if (sy0 < 0) sy0 = 0;
        		if (sx1 > this.dx) sx1 = this.dx;
        		if (sy1 > this.dy) sy1 = this.dy;
        	}
        } else if (this.isPointInRegion(4 + oX, 155 + oY, 18, 18, x, y)) {
        	this.sendButtonClick(6);
        } else if (this.isPointInRegion(33 + oX, 155 + oY, 7, 9, x, y)) {
        	this.sendValueChange(0, tileEntity.width - (b == 0 ? 1 : 8));
        } else if (this.isPointInRegion(58 + oX, 155 + oY, 7, 9, x, y)) {
        	this.sendValueChange(0, tileEntity.width + (b == 0 ? 1 : 8));
        } else if (this.isPointInRegion(33 + oX, 164 + oY, 7, 9, x, y)) {
        	this.sendValueChange(1, tileEntity.height - (b == 0 ? 1 : 8));
        } else if (this.isPointInRegion(58 + oX, 164 + oY, 7, 9, x, y)) {
        	this.sendValueChange(1, tileEntity.height + (b == 0 ? 1 : 8));
        } else if (this.isPointInRegion(76 + oX, 155 + oY, 7, 9, x, y)) {
        	tileEntity.netData.ints[1] -= b == 0 ? 1 : 10;
        	this.sendValueChange(2, tileEntity.netData.ints[1]);
        } else if (this.isPointInRegion(101 + oX, 155 + oY, 7, 9, x, y)) {
        	tileEntity.netData.ints[1] += b == 0 ? 1 : 10;
        	this.sendValueChange(2, tileEntity.netData.ints[1]);
        } else if (this.isPointInRegion(76 + oX, 164 + oY, 7, 9, x, y)) {
        	tileEntity.netData.ints[2] -= b == 0 ? 1 : 10;
        	this.sendValueChange(3, tileEntity.netData.ints[2]);
        } else if (this.isPointInRegion(101 + oX, 164 + oY, 7, 9, x, y)) {
        	tileEntity.netData.ints[2] += b == 0 ? 1 : 10;
        	this.sendValueChange(3, tileEntity.netData.ints[2]);
        } else {
            textField.mouseClicked(x, y, b);
            super.mouseClicked(x, y, b);
        }
    }

    @Override
	protected void mouseClickMove(int x, int y, int b, long t) 
    {
    	if (this.isPointInRegion(0, 0, 256, 256, x, y)) {
    		if (tool != 0) return;
    		int dmax = Math.max(dx, dy);
            int ox = (x - this.guiLeft) * dmax / 256;
            int oy = (y - this.guiTop) * dmax / 256;
            if (ox == px && oy == py) return;
            px = ox; py = oy;
            if (ox < 0 || oy < 0 || ox >= dx || oy >= dy) return;
            this.sendToolUse(ox, oy, b == 0 ? block + 1 : 0);
    	} else super.mouseClickMove(x, y, b, t);
	}

	@Override
    protected void keyTyped(char par1, int par2) throws IOException 
    {
        if (!textField.isFocused()) super.keyTyped(par1, par2);
    	textField.textboxKeyTyped(par1, par2);
        if (par2 == Keyboard.KEY_RETURN)
        {
            sendNameSet(textField.getText());
        }
    }
    
    private void sendToolUse(int x, int y, int b)
    {
	        PacketBuffer dos = tileEntity.getPacketTargetData();
	        dos.writeByte(CMD_Draw);
	        dos.writeByte(x);
	        dos.writeByte(y);
	        dos.writeByte(tool);
	        if (tool < 3) dos.writeByte(b);
	        if (tool != 0) {
	        	dos.writeByte(sx0);
	        	dos.writeByte(sy0);
	        	dos.writeByte(sx1);
	        	dos.writeByte(sy1);
	        }
	        BlockGuiHandler.sendPacketToServer(dos);
    }
    
    private void sendButtonClick(int i)
    {
    	PacketBuffer dos = tileEntity.getPacketTargetData();
	        dos.writeByte(CMD_Button);
	        dos.writeByte(i);
	        if (i >= 2 && i < 6) {
	        	dos.writeByte(sx0);
	        	dos.writeByte(sy0);
	        	dos.writeByte(sx1);
	        	dos.writeByte(sy1);
	        }
	        BlockGuiHandler.sendPacketToServer(dos);
    }
    
    private void sendNameSet(String s)
    {
    	PacketBuffer dos = tileEntity.getPacketTargetData();
	        dos.writeByte(CMD_Name);
	        dos.writeString(s);
	        BlockGuiHandler.sendPacketToServer(dos);
    }
    
    private void sendValueChange(int i, int v)
    {
    	PacketBuffer dos = tileEntity.getPacketTargetData();
            dos.writeByte(CMD_Value);
            dos.writeByte(i);
            dos.writeInt(v);
            BlockGuiHandler.sendPacketToServer(dos);
    }
    
}
