package cd4017be.automation.Gui;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import cd4017be.automation.TileEntity.VertexShematicGen;
import cd4017be.automation.TileEntity.VertexShematicGen.Polygon;
import cd4017be.lib.BlockGuiHandler;
import cd4017be.lib.TileContainer;
import cd4017be.lib.templates.AutomatedTile;
import cd4017be.lib.templates.GuiMachine;

public class GuiVertexShematicGen extends GuiMachine 
{
	public static int[] colors = {0xff7f0000, 0xff7f3f00, 0xff3f7f00, 0xff007f00, 0xff007f3f, 0xff003f7f, 0xff00007f, 0xff3f007f};
	private final VertexShematicGen tile;
	private GuiTextField tfX;
    private GuiTextField tfY;
    private GuiTextField tfZ;
    private GuiTextField tfU;
    private GuiTextField tfV;
    private GuiTextField tfT;
    private GuiTextField name;
    
    private int sel1, sel2;
    private int scroll1 = 0, scroll2 = 0;
	
	public GuiVertexShematicGen(VertexShematicGen tile, EntityPlayer player) 
	{
		super(new TileContainer(tile, player));
		this.tile = tile;
		sel1 = tile.sel;
		sel2 = -1;
	}

	@Override
    public void initGui() 
    {
        this.xSize = 176;
        this.ySize = 179;
        super.initGui();
        tfX = new GuiTextField(fontRendererObj, this.guiLeft + 20, this.guiTop + 16, 25, 8);
        tfY = new GuiTextField(fontRendererObj, this.guiLeft + 20, this.guiTop + 27, 25, 8);
        tfZ = new GuiTextField(fontRendererObj, this.guiLeft + 20, this.guiTop + 38, 25, 8);
        tfU = new GuiTextField(fontRendererObj, this.guiLeft + 20, this.guiTop + 49, 25, 8);
        tfV = new GuiTextField(fontRendererObj, this.guiLeft + 20, this.guiTop + 60, 25, 8);
        tfT = new GuiTextField(fontRendererObj, this.guiLeft + 20, this.guiTop + 71, 25, 8);
        name = new GuiTextField(fontRendererObj, this.guiLeft + 134, this.guiTop + 71, 34, 8);
        tfX.setEnableBackgroundDrawing(false);
        tfY.setEnableBackgroundDrawing(false);
        tfZ.setEnableBackgroundDrawing(false);
        tfU.setEnableBackgroundDrawing(false);
        tfV.setEnableBackgroundDrawing(false);
        tfT.setEnableBackgroundDrawing(false);
        name.setEnableBackgroundDrawing(false);
    }

    @Override
    public void updateScreen() 
    {
        super.updateScreen();
        tfX.updateCursorCounter();
        tfY.updateCursorCounter();
        tfZ.updateCursorCounter();
        tfU.updateCursorCounter();
        tfV.updateCursorCounter();
        tfT.updateCursorCounter();
        name.updateCursorCounter();
        boolean isSel1 = sel1 >= 0 && sel1 < tile.polygons.size();
        Polygon p = isSel1 ? tile.polygons.get(sel1) : null;
        boolean isSel2 = p != null && sel2 >= 0 && sel2 < p.vert.length;
        if (!tfX.isFocused()) tfX.setText(isSel2 ? "" + p.vert[sel2].x[0] : "");
        if (!tfY.isFocused()) tfY.setText(isSel2 ? "" + p.vert[sel2].x[1] : "");
        if (!tfZ.isFocused()) tfZ.setText(isSel2 ? "" + p.vert[sel2].x[2] : "");
        if (!tfU.isFocused()) tfU.setText(isSel2 ? "" + p.vert[sel2].x[3] : "");
        if (!tfV.isFocused()) tfV.setText(isSel2 ? "" + p.vert[sel2].x[4] : "");
        if (!tfT.isFocused()) tfT.setText(isSel1 ? "" + p.thick : "");
        if (!name.isFocused()) name.setText(tile.name);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mx, int my) 
    {
        super.drawGuiContainerForegroundLayer(mx, my);
        this.drawInfo(152, 51, 16, 16, "\\i", "gui.vertex.schematic");
        this.drawInfo(134, 71, 34, 8, "\\i", "gui.vertex.name");
        this.drawInfo(133, 59, 18, 9, "\\i", "gui.vertex.load");
        this.drawInfo(133, 48, 18, 9, "\\i", "gui.vertex.save");
        this.drawInfo(133, 37, 36, 9, "\\i", "gui.vertex.saveSel");
        this.drawInfo(133, 26, 36, 9, "\\i", "gui.vertex.delSel");
        this.drawInfo(133, 15, 36, 9, "\\i", "gui.vertex.clear");
        this.drawInfo(66, 71, 18, 9, "\\i", "gui.vertex.new");
        this.drawInfo(66, 51, 18, 9, "\\i", "gui.vertex.add");
        this.drawInfo(48, 51, 18, 9, "\\i", "gui.vertex.del");
        this.drawInfo(48, 61, 36, 9, "\\i", "gui.vertex.autoTex");
        this.drawInfo(7, 70, 10, 10, "\\i", "gui.vertex.extDir");
        this.drawInfo(20, 71, 25, 8, "\\i", "gui.vertex.extAm");
        this.drawInfo(53, 72, 8, 7, "\\i", "gui.vertex.texId");
        this.drawInfo(8, 16, 10, 30, "\\i", "gui.vertex.blockC");
        this.drawInfo(8, 49, 10, 19, "\\i", "gui.vertex.texC");
        this.drawInfo(87, 16, 8, 63, "Polygons");
        this.drawInfo(49, 16, 8, 32, "Vertices");
    }
    
    @Override
    protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) 
    {
    	boolean isSel1 = sel1 >= 0 && sel1 < tile.polygons.size();
        Polygon p = isSel1 ? tile.polygons.get(sel1) : null;
    	GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.renderEngine.bindTexture(new ResourceLocation("automation", "textures/gui/vertexShematicGen.png"));
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
        if (isSel1) this.drawTexturedModalRect(this.guiLeft + 7, this.guiTop + 70, 184, p.dir * 10, 10, 10);
        int i = isSel1 ? p.vert.length : 0;
        int n = i > 4 ? 20 * scroll2 / (i - 4) : 0; 
        this.drawTexturedModalRect(this.guiLeft + 49, this.guiTop + 16 + n, 176, 0, 8, 12);
        i = tile.polygons.size();
        n = i > 8 ? 51 * scroll1 / (i - 8) : 0;
        this.drawTexturedModalRect(this.guiLeft + 87, this.guiTop + 16 + n, 176, 0, 8, 12);
        tfX.drawTextBox();
        tfY.drawTextBox();
        tfZ.drawTextBox();
        tfU.drawTextBox();
        tfV.drawTextBox();
        tfT.drawTextBox();
        name.drawTextBox();
        if (sel1 >= scroll1 && sel1 < scroll1 + 8) drawRect(this.guiLeft + 97, this.guiTop + 16 + (sel1 - scroll1) * 8, this.guiLeft + 130, this.guiTop + 24 + (sel1 - scroll1) * 8, 0x7f007f40);
        if (sel2 >= scroll2 && sel2 < scroll2 + 4) drawRect(this.guiLeft + 59, this.guiTop + 16 + (sel2 - scroll2) * 8, this.guiLeft + 83, this.guiTop + 24 + (sel2 - scroll2) * 8, 0x7f007f40);
        if (isSel1) this.drawStringCentered("" + p.texId, this.guiLeft + 57, this.guiTop + 72, 0x404040);
        if (isSel1)
        	for (i = scroll2; i < p.vert.length && i < scroll2 + 4; i++) {
        		this.fontRendererObj.drawString(i + ":", this.guiLeft + 59, this.guiTop + 16 + (i - scroll2) * 8, 0x404040);
        	}
        
        for (i = scroll1; i < tile.polygons.size() && i < scroll1 + 8; i++) {
        	p = tile.polygons.get(i);
        	int c = colors[p.texId & 0x7];
        	this.fontRendererObj.drawString(i + ": " + p.vert.length + "V", this.guiLeft + 97, this.guiTop + 16 + 8 * (i - scroll1), c);
        }
        this.drawStringCentered("3D-Vertex Schematic Printer", this.guiLeft + this.xSize / 2, this.guiTop + 4, 0x404040);
        this.drawStringCentered("Inventory", this.guiLeft + this.xSize / 2, this.guiTop + 83, 0x404040);
    }

    @Override
    protected void mouseClicked(int x, int y, int b) 
    {
        super.mouseClicked(x, y, b);
        tfX.mouseClicked(x, y, b);
        tfY.mouseClicked(x, y, b);
        tfZ.mouseClicked(x, y, b);
        tfU.mouseClicked(x, y, b);
        tfV.mouseClicked(x, y, b);
        tfT.mouseClicked(x, y, b);
        name.mouseClicked(x, y, b);
        int kb = -1;
        int s = 0, s1 = 0;
        if (this.func_146978_c(133, 59, 18, 9, x, y)) {
        	kb = 0;
        } else if (this.func_146978_c(133, 48, 18, 9, x, y)) {
        	kb = 1;
        } else if (this.func_146978_c(133, 37, 36, 9, x, y)) {
        	kb = 2;
        	s = sel1;
        } else if (this.func_146978_c(133, 26, 36, 9, x, y)) {
        	kb = 3;
        	s = sel1;
        } else if (this.func_146978_c(133, 15, 36, 9, x, y)) {
        	kb = 3;
        	s = -1;
        } else if (this.func_146978_c(66, 71, 18, 9, x, y)) {
        	kb = 4;
        } else if (this.func_146978_c(66, 51, 18, 9, x, y)) {
        	kb = 5;
        	s = sel1;
        	s1 = ++sel2;
        } else if (this.func_146978_c(48, 51, 18, 9, x, y)) {
        	kb = 6;
        	s = sel1;
        	s1 = sel2;
        } else if (this.func_146978_c(7, 70, 10, 10, x, y) && sel1 >= 0 && sel1 < tile.polygons.size()) {
        	kb = 8;
        	s = sel1;
        	s1 = (tile.polygons.get(sel1).dir + 1) % 6;
        } else if (this.func_146978_c(48, 71, 5, 9, x, y) && sel1 >= 0 && sel1 < tile.polygons.size()) {
        	kb = 7;
        	s = sel1;
        	s1 = (tile.polygons.get(sel1).texId + 1) % 8;
        } else if (this.func_146978_c(61, 71, 5, 9, x, y) && sel1 >= 0 && sel1 < tile.polygons.size()) {
        	kb = 7;
        	s = sel1;
        	s1 = (tile.polygons.get(sel1).texId + 7) % 8;
        } else if (this.func_146978_c(59, 16, 24, 32, x, y) && sel1 >= 0 && sel1 < tile.polygons.size()) {
        	int sel = (y - this.guiTop - 16) / 8 + scroll2;
        	if (sel >= tile.polygons.get(sel1).vert.length) sel2 = -1;
        	else sel2 = sel;
        } else if (this.func_146978_c(97, 16, 33, 63, x, y)) {
        	int sel = (y - this.guiTop - 16) / 8 + scroll1;
        	if (sel >= tile.polygons.size()) sel1 = -1;
        	else sel1 = sel;
        	s = sel1;
        	kb = 12;
        } else if (this.func_146978_c(48, 61, 34, 9, x, y)) {
        	s = sel1;
        	kb = 13;
        }
        if (kb >= 0)
        {
            try {
            ByteArrayOutputStream bos = tile.getPacketTargetData();
            DataOutputStream dos = new DataOutputStream(bos);
            dos.writeByte(AutomatedTile.CmdOffset + kb);
            if ((kb >= 2 && kb <= 8 && kb != 4) || kb == 12 || kb == 13) dos.writeShort(s);
            if (kb >= 5 && kb <= 8) dos.writeByte(s1);
            BlockGuiHandler.sendPacketToServer(bos);
            } catch (IOException e){}
        }
    }

    @Override
	protected void mouseClickMove(int x, int y, int b, long t) 
    {
    	boolean isSel1 = sel1 >= 0 && sel1 < tile.polygons.size();
        Polygon p = isSel1 ? tile.polygons.get(sel1) : null;
        int i = (isSel1 ? p.vert.length : 0) - 4;
    	if (this.func_146978_c(49, 16, 8, 32, x, y) && i > 0) {
			scroll2 = (y - this.guiTop - 22) * i / 20;
		}
    	if (scroll2 > i) scroll2 = i;
    	if (scroll2 < 0) scroll2 = 0;
        i = tile.polygons.size() - 8;
        if (this.func_146978_c(87, 16, 8, 63, x, y) && i > 0) {
        	scroll1 = (y - this.guiTop - 22) * i / 51;
        }
        if (scroll1 > i) scroll1 = i;
        if (scroll1 < 0) scroll1 = 0;
    	super.mouseClickMove(x, y, b, t);
	}

	@Override
    protected void keyTyped(char c, int k) 
    {
        if (!tfX.isFocused() && !tfY.isFocused() && !tfZ.isFocused() && !tfU.isFocused() && !tfV.isFocused() && !tfT.isFocused() && !name.isFocused()) super.keyTyped(c, k);
        tfX.textboxKeyTyped(c, k);
        tfY.textboxKeyTyped(c, k);
        tfZ.textboxKeyTyped(c, k);
        tfU.textboxKeyTyped(c, k);
        tfV.textboxKeyTyped(c, k);
        tfT.textboxKeyTyped(c, k);
        name.textboxKeyTyped(c, k);
        if (k == Keyboard.KEY_RETURN) {
            int i = -1, j = 0;
            GuiTextField tf = null;
            if (tfX.isFocused()) {
                i = 10;
                j = 0;
                tf = tfX;
            } else if (tfY.isFocused()) {
                i = 10;
                j = 1;
                tf = tfY;
            } else if (tfZ.isFocused()) {
                i = 10;
                j = 2;
                tf = tfZ;
            } else if (tfU.isFocused()) {
                i = 10;
                j = 3;
                tf = tfU;
            } else if (tfV.isFocused()) {
                i = 10;
                j = 4;
                tf = tfV;
            } else if (tfT.isFocused()) {
                i = 9;
                tf = tfT;
            } else if (name.isFocused()) {
                i = 11;
                tf = name;
            }
            if (tf != null)
            try {
                ByteArrayOutputStream bos = tile.getPacketTargetData();
                DataOutputStream dos = new DataOutputStream(bos);
                dos.writeByte(AutomatedTile.CmdOffset + i);
                if (i == 10) {
                	dos.writeShort(sel1);
                	dos.writeByte(sel2);
                	dos.writeByte(j);
                	dos.writeFloat(Float.parseFloat(tf.getText()));
                } else if (i == 9) {
                	dos.writeShort(sel1);
                	dos.writeShort(Short.parseShort(tf.getText()));
                } else if (i == 11) dos.writeUTF(tf.getText());
                BlockGuiHandler.sendPacketToServer(bos);
            } catch (NumberFormatException e) {
            } catch (IOException e) {}
        }
    }

}
