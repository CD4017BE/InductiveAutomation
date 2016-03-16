package cd4017be.automation.Gui;

import java.io.IOException;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import cd4017be.api.automation.EnergyItemHandler;
import cd4017be.automation.Item.ItemPortableTeleporter;
import cd4017be.lib.BlockGuiHandler;
import cd4017be.lib.templates.GuiMachine;
import cd4017be.lib.util.Utils;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

public class GuiPortableTeleporter extends GuiMachine 
{
	private static final int size = 8;
	private final ContainerPortableTeleporter container;
	private int amount = 0;
    private int scroll = 0;
    private int sel = -1;
    private Entry[] list = new Entry[0];
    private double maxDist = 0;
    private GuiTextField tfX;
    private GuiTextField tfY;
    private GuiTextField tfZ;
    private GuiTextField name;
    
	public GuiPortableTeleporter(ContainerPortableTeleporter container) 
	{
		super(container);
		this.container = container;
	}

	@Override
    public void initGui() 
    {
        this.xSize = 176;
        this.ySize = 216;
        super.initGui();
        tfX = new GuiTextField(0, fontRendererObj, this.guiLeft + 26, this.guiTop + 100, 34, 16);
        tfY = new GuiTextField(1, fontRendererObj, this.guiLeft + 80, this.guiTop + 100, 34, 16);
        tfZ = new GuiTextField(2, fontRendererObj, this.guiLeft + 134, this.guiTop + 100, 34, 16);
        name = new GuiTextField(3, fontRendererObj, this.guiLeft + 62, this.guiTop + 82, 106, 16);
    }

    @Override
    public void updateScreen() 
    {
        super.updateScreen();
        int l = list.length;
        ItemStack item = container.player.getCurrentEquippedItem();
        if (item == null || item.stackTagCompound == null) {
        	list = new Entry[0];
        } else {
        	int x = MathHelper.floor_double(container.player.posX);
        	int y = MathHelper.floor_double(container.player.posY) - 1;
        	int z = MathHelper.floor_double(container.player.posZ);
        	NBTTagList data = item.stackTagCompound.getTagList("points", 10);
        	list = new Entry[data.tagCount()];
        	for (int i = 0; i < list.length; i++) {
        		list[i] = new Entry(data.getCompoundTagAt(i), x, y, z);
        	}
        	maxDist = (double)EnergyItemHandler.getEnergy(item) / (double)ItemPortableTeleporter.energyUse;
        }
        if (list.length != l) scroll = 0;
        tfX.updateCursorCounter();
        tfY.updateCursorCounter();
        tfZ.updateCursorCounter();
        name.updateCursorCounter();
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mx, int my) 
    {
        super.drawGuiContainerForegroundLayer(mx, my);
        this.drawInfo(8, 82, 16, 16, "\\i", "teleporter.add");
        this.drawInfo(26, 82, 34, 16, "\\i", "teleporter.save");
        this.drawInfo(134, 16, 34, 64, "\\i", "teleporter.move");
    }
    
    @Override
    protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) 
    {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.renderEngine.bindTexture(new ResourceLocation("automation", "textures/gui/portableTeleporter.png"));
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
        int n = list.length <= size ? 0 : scroll * 52 / (list.length - size);
        this.drawTexturedModalRect(this.guiLeft + 8, this.guiTop + 16 + n, 176, 0, 8, 12);
        int x, y;
        for (int i = scroll; i < scroll + size && i < list.length; i++) {
            x = this.guiLeft + 18; y = this.guiTop + 16 + (i - scroll) * 8;
        	if (i == sel) this.drawGradientRect(x, y, x + 114, y + 8, 0xff40c0c0, 0xff40c0c0);
        	list[i].draw(this, x, y);
        }
        tfX.drawTextBox();
        tfY.drawTextBox();
        tfZ.drawTextBox();
        name.drawTextBox();
        this.drawStringCentered("Waypoints", this.guiLeft + this.xSize / 2, this.guiTop + 4, 0x404040);
        this.drawStringCentered(StatCollector.translateToLocal("container.inventory"), this.guiLeft + this.xSize / 2, this.guiTop + 120, 0x404040);
    }
    
    @Override
    public void handleMouseInput() throws IOException 
    {
        this.scroll -= Mouse.getDWheel() / 120;
        if (this.scroll > list.length - size) this.scroll = list.length - size;
        if (this.scroll < 0) this.scroll = 0;
        super.handleMouseInput();
    }
    
    @Override
    protected void mouseClicked(int x, int y, int b) throws IOException 
    {
    	tfX.mouseClicked(x, y, b);
        tfY.mouseClicked(x, y, b);
        tfZ.mouseClicked(x, y, b);
        name.mouseClicked(x, y, b);
    	byte a = -1;
        int obj = -1;
        if (this.isPointInRegion(8, 82, 16, 16, x, y)) {
            a = 1;
        } else
        if (this.isPointInRegion(26, 82, 34, 16, x, y)) {
            if (sel >= 0 && sel < list.length) {
            	list[sel].x = this.getNumber(this.tfX.getText());
            	list[sel].y = this.getNumber(this.tfY.getText());
            	list[sel].z = this.getNumber(this.tfZ.getText());
            	list[sel].name = this.name.getText();
            	a = 2;
            	obj = sel;
            }
        } else
        if (this.isPointInRegion(18, 16, 7, 64, x, y))
        {
            a = 3;
        	obj = (y - this.guiTop - 16) / 8 + scroll;
        } else
        if (this.isPointInRegion(134, 16, 34, 64, x, y))
        {
        	a = 0;
        	obj = (y - this.guiTop - 16) / 8 + scroll;
        } else
        if (this.isPointInRegion(26, 16, 106, 64, x, y))
        {
            sel = (y - this.guiTop - 16) / 8 + scroll;
            if (sel >= 0 && sel < list.length) {
            	this.tfX.setText("" + list[sel].x);
            	this.tfY.setText("" + list[sel].y);
            	this.tfZ.setText("" + list[sel].z);
            	this.name.setText(list[sel].name);
            }
        }
        if (a >= 0)
        {
            PacketBuffer dos = BlockGuiHandler.getPacketTargetData(new BlockPos(0, -1, 0));
            dos.writeByte(a);
            if (a == 0 || a == 2 || a == 3) dos.writeByte(obj);
            if (a == 2) list[sel].write(dos); 
            BlockGuiHandler.sendPacketToServer(dos);
        }
        super.mouseClicked(x, y, b);
    }
    
    @Override
    protected void keyTyped(char c, int k) throws IOException 
    {
        if (!tfX.isFocused() && !tfY.isFocused() && !tfZ.isFocused() && !name.isFocused()) super.keyTyped(c, k);
        tfX.textboxKeyTyped(c, k);
        tfY.textboxKeyTyped(c, k);
        tfZ.textboxKeyTyped(c, k);
        name.textboxKeyTyped(c, k);
    }
    
    private int getNumber(String s)
    {
    	try{
    		return Integer.parseInt(s);
    	} catch(NumberFormatException e) {
    		return 0;
    	}
    }
    
    private static class Entry {
    	public int x;
    	public int y;
    	public int z;
    	public String name;
    	public double dist;
    	public Entry(NBTTagCompound tag, int rx, int ry, int rz)
    	{
    		x = tag.getInteger("x");
    		y = tag.getInteger("y");
    		z = tag.getInteger("z");
    		name = tag.getString("n");
    		rx -= x; ry -= y; rz -= z;
    		dist = Math.sqrt(rx * rx + ry * ry + rz * rz);
    	}
    	
    	public void draw(GuiPortableTeleporter gui, int x, int y)
    	{
    		gui.mc.renderEngine.bindTexture(new ResourceLocation("automation", "textures/gui/portableTeleporter.png"));
    		gui.drawTexturedModalRect(x, y, 176, 12, 7, 7);
    		gui.fontRendererObj.drawString(name, x + 8, y, 0x404040);
    		gui.drawStringCentered(Utils.formatNumber(dist, 3, 0) + "m", x + 133, y, dist <= gui.maxDist ? dist <= gui.maxDist / 2D ? 0x40ff40 : 0xffff40 : 0xff4040);
    		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    		
    	}
    	
    	public void write(PacketBuffer dos) throws IOException
    	{
    		dos.writeInt(x);
    		dos.writeInt(y);
    		dos.writeInt(z);
    		dos.writeString(name);
    	}
    }

}
