package cd4017be.automation.Gui;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import cd4017be.api.automation.IOperatingArea;
import cd4017be.lib.BlockGuiHandler;
import cd4017be.lib.templates.GuiMachine;

public class GuiAreaUpgrade extends GuiMachine 
{

	private final ContainerAreaUpgrade data;
	private final GuiTextField[] areaDsp;
	private int distance, maxDist, sx, sy, sz, mx, my, mz, Umax;
	
	public GuiAreaUpgrade(ContainerAreaUpgrade container) 
	{
		super(container);
		this.data = container;
		this.areaDsp = new GuiTextField[6];
	}

	@Override
    public void initGui() 
    {
        this.xSize = 176;
        this.ySize = 168;
        super.initGui();
        int n = 0;
        for (int i = 0; i < 2; i++)
        	for (int j = 0; j < 3; j++) {
        		areaDsp[n] = new GuiTextField(this.fontRendererObj, this.guiLeft + 110 + i * 33, this.guiTop + 36 + j * 12, 25, 8);
        		areaDsp[n++].setEnableBackgroundDrawing(false);
        	}
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mx, int my) 
    {
        super.drawGuiContainerForegroundLayer(mx, my);
        this.drawInfo(26, 16, 88, 16, "\\i", "areaCfg.size");
        this.drawInfo(26, 34, 70, 16, "\\i", "areaCfg.dist");
        this.drawInfo(26, 52, 70, 16, "\\i", "areaCfg.Umax");
        this.drawInfo(116, 16, 16, 16, "\\i", "areaCfg.copy");
        this.drawInfo(152, 16, 16, 16, "\\i", "areaCfg.dspl");
        this.drawInfo(134, 16, 16, 16, "\\i", "areaCfg.synch");
    }
    
    @Override
    protected void drawGuiContainerBackgroundLayer(float var1, int x, int y) 
    {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.renderEngine.bindTexture(new ResourceLocation("automation", "textures/gui/areaConfig.png"));
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
        for (GuiTextField gui: areaDsp) gui.drawTextBox();
        this.fontRendererObj.drawString(String.format("Size: %dx%dx%d", sx, sy, sz), this.guiLeft + 26, this.guiTop + 16, 0x404040);
        this.fontRendererObj.drawString(String.format("Max: %dx%dx%d", mx, my, mz), this.guiLeft + 26, this.guiTop + 24, 0x404040);
        this.fontRendererObj.drawString("Distance:", this.guiLeft + 26, this.guiTop + 34, 0x404040);
        if (maxDist == Integer.MAX_VALUE) this.fontRendererObj.drawString(String.format("%d", distance), this.guiLeft + 26, this.guiTop + 42, 0x404040);
        else this.fontRendererObj.drawString(String.format("%d / %d", distance, maxDist), this.guiLeft + 26, this.guiTop + 42, 0x404040);
        this.fontRendererObj.drawString("Max Voltage:", this.guiLeft + 26, this.guiTop + 52, 0x404040);
        this.fontRendererObj.drawString(String.format("%d V", Umax), this.guiLeft + 26, this.guiTop + 60, 0x404040);
        this.drawStringCentered("Machine Area Configurations", this.guiLeft + this.xSize / 2, this.guiTop + 4, 0x404040);
        this.drawStringCentered("Inventory", this.guiLeft + this.xSize / 2, this.guiTop + 72, 0x404040);
    }
    
    @Override
    protected void mouseClicked(int x, int y, int b) 
    {
        for (GuiTextField gui: areaDsp) gui.mouseClicked(x, y, b);
        if (this.func_146978_c(116, 16, 16, 16, x, y)) {
        	try {
	            ByteArrayOutputStream bos = BlockGuiHandler.getPacketTargetData(0, -1, 0);
	            DataOutputStream dos = new DataOutputStream(bos);
	            dos.writeByte((byte)6);
	            BlockGuiHandler.sendPacketToServer(bos);
	        } catch (IOException e){}
        }
        super.mouseClicked(x, y, b);
    }

	@Override
	protected void keyTyped(char c, int k) 
	{
		for (int i = 0; i < areaDsp.length; i++)
			if (areaDsp[i].isFocused()) {
				if (k == Keyboard.KEY_RETURN) {
					try {
			            ByteArrayOutputStream bos = BlockGuiHandler.getPacketTargetData(0, -1, 0);
			            DataOutputStream dos = new DataOutputStream(bos);
			            dos.writeByte((byte)i);
			            dos.writeInt(Integer.parseInt(areaDsp[i].getText()));
			            BlockGuiHandler.sendPacketToServer(bos);
			            areaDsp[i].setFocused(false);
			        } catch (IOException e){
			        } catch (NumberFormatException e) {}
				} else areaDsp[i].textboxKeyTyped(c, k);
				return;
			}
		super.keyTyped(c, k);
	}

	@Override
	public void updateScreen() 
	{
		int[] area = data.machine.getOperatingArea();
		for (int i = 0; i < areaDsp.length; i++)
			if (!areaDsp[i].isFocused()) {
				areaDsp[i].setText("" + area[i]);
			}
		sx = area[3] - area[0];
		sy = area[4] - area[1];
		sz = area[5] - area[2];
		area = IOperatingArea.Handler.maxSize(data.machine);
		mx = area[0];
		my = area[1];
		mz = area[2];
		distance = data.getDistance();
		maxDist = IOperatingArea.Handler.maxRange(data.machine);
		Umax = IOperatingArea.Handler.Umax(data.machine);
		super.updateScreen();
	}
    

}
