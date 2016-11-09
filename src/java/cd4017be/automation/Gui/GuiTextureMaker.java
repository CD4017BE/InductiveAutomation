package cd4017be.automation.Gui;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.GlStateManager;
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
import cd4017be.automation.Item.ItemFluidDummy;
import cd4017be.automation.TileEntity.TextureMaker;
import cd4017be.lib.BlockGuiHandler;
import cd4017be.lib.Gui.GuiMachine;
import cd4017be.lib.Gui.TileContainer;

/**
 *
 * @author CD4017BE
 */
public class GuiTextureMaker extends GuiMachine {

	public static final short CMD_Button = 0, CMD_Value = 3, CMD_Draw = 1, CMD_Name = 2;
	private final TextureMaker tile;
	private int tool = 0, block = 0, sx0, sy0, sx1, sy1, spx, spy;

	public GuiTextureMaker(TextureMaker tileEntity, EntityPlayer player) {
		super(new TileContainer(tileEntity, player));
		this.tile = tileEntity;
		this.MAIN_TEX = new ResourceLocation("automation", "textures/gui/texMaker.png");
		this.drawBG = false;
	}

	@Override
	public void initGui() {
		this.xSize = 288;
		this.ySize = 346;
		super.initGui();
		guiComps.add(new TextField(0, 198, 302, 82, 16, 16).setTooltip("tex.name"));
		guiComps.add(new Button(1, 177, 321, 18, 18, 0).texture(147, 90).setTooltip("tex.ext#"));
		guiComps.add(new NumberSel(2, 206, 321, 32, 9, "%d", 1, 32, 8).setup(18, 0x404040, 1, true).setTooltip("tex.size"));
		guiComps.add(new NumberSel(3, 206, 330, 32, 9, "%d", 1, 32, 8).setup(18, 0x404040, 1, true).setTooltip("tex.size"));
		guiComps.add(new NumberSel(4, 249, 321, 32, 9, "%d", -128, 128, 8).setup(18, 0x404040, 1, true).setTooltip("tex.ofs"));
		guiComps.add(new NumberSel(5, 249, 330, 32, 9, "%d", -128, 128, 8).setup(18, 0x404040, 1, true).setTooltip("tex.ofs"));
		guiComps.add(new Button(6, 177, 263, 18, 18, -1).setTooltip("tex.load"));
		guiComps.add(new Button(7, 177, 281, 18, 18, -1).setTooltip("tex.save"));
		guiComps.add(new Button(8, 195, 263, 18, 18, -1).setTooltip("tex.mirH"));
		guiComps.add(new Button(9, 195, 281, 18, 18, -1).setTooltip("tex.mirV"));
		guiComps.add(new Button(10, 217, 263, 18, 18, -1).setTooltip("tex.rotR"));
		guiComps.add(new Button(11, 217, 281, 18, 18, -1).setTooltip("tex.del"));
		guiComps.add(new Button(12, 232, 264, 16, 16, 0).texture(115, 90).setTooltip("tex.draw"));
		guiComps.add(new Button(13, 248, 264, 16, 16, 0).texture(115, 90).setTooltip("tex.fill"));
		guiComps.add(new Button(14, 264, 264, 16, 16, 0).texture(115, 90).setTooltip("tex.repl"));
		guiComps.add(new Button(15, 232, 282, 16, 16, 0).texture(115, 90).setTooltip("tex.sel"));
		guiComps.add(new Button(16, 248, 282, 16, 16, 0).texture(115, 90).setTooltip("tex.copy"));
		guiComps.add(new Button(17, 264, 282, 16, 16, 0).texture(115, 90).setTooltip("tex.move"));
		for (int i = 0; i < 15; i++)
			guiComps.add(new Button(18 + i, 264, 8 + 16 * i, 16, 16, 0).texture(131, 90));
		guiComps.add(new Grid(33, 0, 0, 256, 256));
	}

	@Override
	protected Object getDisplVar(int id) {
		switch(id) {
		case 0: return tile.texName();
		case 1: return tile.extMode;
		case 2: return (int)tile.width;
		case 3: return (int)tile.height;
		case 4: return tile.ofsX;
		case 5: return tile.ofsY;
		default: if (id < 18) return id - 12 == tool ? 0 : 1;
			else if (id < 33) return id - 18 == block ? 0 : 1;
			else return null;
		}
	}

	@Override
	protected void setDisplVar(int id, Object obj, boolean send) {
		PacketBuffer dos = tile.getPacketTargetData();
		switch(id) {
		case 0: dos.writeByte(CMD_Name); dos.writeString((String)obj); break;
		case 1: dos.writeByte(CMD_Button).writeByte(6); break;
		case 2: dos.writeByte(CMD_Value).writeByte(0).writeInt(((Integer)obj).byteValue()); break;
		case 3: dos.writeByte(CMD_Value).writeByte(1).writeInt(((Integer)obj).byteValue()); break;
		case 4: dos.writeByte(CMD_Value).writeByte(2).writeInt(tile.ofsX = (Integer)obj); break;
		case 5: dos.writeByte(CMD_Value).writeByte(3).writeInt(tile.ofsY = (Integer)obj); break;
		default: if (id < 12) {
				dos.writeByte(CMD_Button).writeByte(id - 6);
				if (id >= 8) dos.writeByte(sx0).writeByte(sy0).writeByte(sx1).writeByte(sy1);
				break; }
			else if (id < 18) {if ((tool = id - 12) > 3) {spx = sx0; spy = sy0;}}
			else if (id < 33) block = id - 18;
			return;
		}
		if (send) BlockGuiHandler.sendPacketToServer(dos);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
		drawHorizontalLine(guiLeft - 1, guiLeft + 256, guiTop - 1, 0xff000000);
		drawVerticalLine(guiLeft - 1, guiTop - 1, guiTop + 256, 0xff000000);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(MAIN_TEX);
		drawTexturedModalRect(guiLeft, guiTop + 256, 0, 0, 173, 90);
		drawTexturedModalRect(guiLeft + 173, guiTop + 256, 0, 90, 115, 90);
		drawTexturedModalRect(guiLeft + 256, guiTop, 224, 0, 32, 256);
		super.drawGuiContainerBackgroundLayer(var1, var2, var3);
	}

	class Grid extends GuiComp {

		private int dx, dy, dmax;

		public Grid(int id, int px, int py, int w, int h) {
			super(id, px, py, w, h);
		}

		@Override
		public void draw() {
			if (dx != tile.width || dy != tile.height) {
				dx = tile.width;
				dy = tile.height;
				dmax = Math.max(dx, dy);
				sx0 = 0; sx1 = dx;
				sy0 = 0; sy1 = dy;
			}
			if (dx <= 0 || dy <= 0) return;
			BlockModelShapes bms = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes();
			TextureAtlasSprite[] icons = new TextureAtlasSprite[16];
			IBakedModel model;
			boolean[] opaque = new boolean[16];
			for (int i = 1; i < icons.length; i++) {
				ItemStack item = tile.inventory.items[i];
				if (item == null) continue;
				if (item.getItem() instanceof ItemBlock) {
					ItemBlock ib = (ItemBlock)item.getItem();
					IBlockState state = ib.block.getStateFromMeta(ib.getMetadata(item.getItemDamage()));
					model = bms.getModelForState(state);
					if (model != null) icons[i] = model.getParticleTexture();
					opaque[i] = icons[i] != null && state.isOpaqueCube();
				} else if (item.getItem() instanceof ItemFluidDummy) {
					Fluid fluid = ItemFluidDummy.fluid(item);
					if (fluid != null) {
						ResourceLocation res = fluid.getStill();
						if (res != null) icons[i] = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(res.toString());
					}
					opaque[i] = false;
				}
			}
			GlStateManager.pushMatrix();
			GlStateManager.translate(px, py, 0);
			GlStateManager.scale(16F / (float)dmax, 16F / (float)dmax, 1F);
			GlStateManager.enableBlend();
			OpenGlHelper.glBlendFunc(770, 771, 1, 0);
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			mc.renderEngine.bindTexture(MAIN_TEX);
			byte p;
			for (int y = 0; y < dy; y++)
				for (int x = 0; x < dx; x++) {
					p = tile.getPixel(x, y);
					if (!opaque[p])
						drawTexturedModalRect(x * 16, y * 16, 208, 16 * p, 16, 16);
				}
			mc.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
			for (int y = 0; y < dy; y++)
				for (int x = 0; x < dx; x++) {
					TextureAtlasSprite icon = icons[tile.getPixel(x, y)];
					if (icon != null)
						drawTexturedModalRect(x * 16, y * 16, icon, 16, 16);
				}
			GlStateManager.disableBlend();
			int x0 = sx0 * 16, y0 = sy0 * 16, x1 = sx1 * 16 - 1, y1 = sy1 * 16 - 1, c = 0x8000ff00;
			drawHorizontalLine(x0, x1, y0, c);
			drawHorizontalLine(x0, x1, y1, c);
			drawVerticalLine(x0, y0, y1, c);
			drawVerticalLine(x1, y0, y1, c);
			if (tool > 3) drawGradientRect(spx * 16 + 2, spy * 16 + 2, spx * 16 + 14, spy * 16 + 14, 0x4000ff00, 0x4000ff00);
			GlStateManager.popMatrix();
		}

		@Override
		public boolean mouseIn(int x, int y, int b, int d) {
			if (d == 2) return true;
			int ox = (x - px) * dmax / 256;
			int oy = (y - py) * dmax / 256;
			if (ox < 0 || oy < 0 || ox >= dx || oy >= dy) return false;
			if (d == 0) {
				if (tool == 0) {spx = ox; spy = oy;}
				if (tool < 3) sendToolUse(ox, oy, b == 0 ? block + 1 : 0);
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
					spx = ox; spy = oy;
				} else {
					int dx = ox - spx, dy = oy - spy;
					sendToolUse(dx, dy, 0);
					sx0 += dx; sy0 += dy;
					sx1 += dx; sy1 += dy;
					spx += dx; spy += dy;
					if (sx0 < 0) sx0 = 0;
					if (sy0 < 0) sy0 = 0;
					if (sx1 > this.dx) sx1 = this.dx;
					if (sy1 > this.dy) sy1 = this.dy;
				}
			} else if (d == 1) {
				if (tool != 0 || (ox == spx && oy == spy)) return true;
				spx = ox; spy = oy;
				sendToolUse(ox, oy, b == 0 ? block + 1 : 0);
			}
			return true;
		}

		@Override
		public boolean focus() {
			return true;
		}

		void sendToolUse(int x, int y, int b) {
			PacketBuffer dos = tile.getPacketTargetData();
			dos.writeByte(CMD_Draw).writeByte(x).writeByte(y).writeByte(tool);
			if (tool < 3) dos.writeByte(b);
			if (tool != 0) dos.writeByte(sx0).writeByte(sy0).writeByte(sx1).writeByte(sy1);
			BlockGuiHandler.sendPacketToServer(dos);
		}

	}

}
