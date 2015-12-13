package cd4017be.automation.Gui;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.lwjgl.opengl.GL11;

import cd4017be.lib.BlockGuiHandler;
import cd4017be.lib.templates.GuiMachine;
import net.minecraft.util.ResourceLocation;

public class GuiRemoteInventory extends GuiMachine 
{
	
	private final ContainerRemoteInventory container;
	
	public GuiRemoteInventory(ContainerRemoteInventory container) 
	{
		super(container);
		this.container = container;
	}

	@Override
    public void initGui() 
    {
		this.xSize = 230;
        this.ySize = 150 + container.ofsY;
        super.initGui();
    }

	@Override
    protected void drawGuiContainerForegroundLayer(int mx, int my) 
    {
        super.drawGuiContainerForegroundLayer(mx, my);
        this.drawInfo(11, 67 + container.ofsY, 10, 18, "\\i", "inputFilter");
        this.drawInfo(29, 67 + container.ofsY, 10, 18, "\\i", "outputFilter");
    }
    
    @Override
    protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) 
    {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.renderEngine.bindTexture(new ResourceLocation("automation", "textures/gui/portableRemoteInv.png"));
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, 15);
        int h = container.size / 12;
        int w = container.size % 12;
        for (int i = 0; i < h; i++)
        	this.drawTexturedModalRect(this.guiLeft, this.guiTop + 15 + i * 18, 0, 15, this.xSize, 18);
        if (w != 0) {
        	int n = 7 + w * 18;
        	this.drawTexturedModalRect(this.guiLeft, this.guiTop + 15 + h * 18, 0, 15, n, 18);
        	this.drawTexturedModalRect(this.guiLeft + n, this.guiTop + 15 + h * 18, n, 33, this.xSize - n, 18);
        }
        this.drawTexturedModalRect(this.guiLeft, this.guiTop + 51 + container.ofsY, 0, 51, this.xSize, 99);
        if (this.container.isFilterOn(0)) this.drawTexturedModalRect(this.guiLeft + 11, this.guiTop + 67 + container.ofsY, 230, 0, 10, 18);
        if (this.container.isFilterOn(1)) this.drawTexturedModalRect(this.guiLeft + 29, this.guiTop + 67 + container.ofsY, 240, 0, 10, 18);
        this.drawStringCentered(this.container.inventoryName(), this.guiLeft + this.xSize / 2, this.guiTop + 4, 0x404040);
        this.drawStringCentered("Inventory", this.guiLeft + this.xSize / 2, this.guiTop + 54 + container.ofsY, 0x404040);
    }

	@Override
	protected void mouseClicked(int x, int y, int b) 
	{
		super.mouseClicked(x, y, b);
		byte cmd = -1;
		if (this.func_146978_c(11, 67 + container.ofsY, 10, 18, x, y)) {
			cmd = 0;
		} else if (this.func_146978_c(29, 67 + container.ofsY, 10, 18, x, y)) {
			cmd = 1;
		}
		if (cmd >= 0) {
			try {
	            ByteArrayOutputStream bos = BlockGuiHandler.getPacketTargetData(0, -1, 0);
	            DataOutputStream dos = new DataOutputStream(bos);
	            dos.writeByte(cmd);
	            BlockGuiHandler.sendPacketToServer(bos);
	        } catch (IOException e){}
		}
	}

}
