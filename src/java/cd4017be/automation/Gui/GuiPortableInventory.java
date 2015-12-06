package cd4017be.automation.Gui;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.lwjgl.opengl.GL11;

import cd4017be.lib.BlockGuiHandler;
import cd4017be.lib.templates.GuiMachine;
import net.minecraft.util.ResourceLocation;

public class GuiPortableInventory extends GuiMachine 
{
	
	private final ContainerPortableInventory container;
	
	public GuiPortableInventory(ContainerPortableInventory container) 
	{
		super(container);
		this.container = container;
	}

	@Override
    public void initGui() 
    {
        this.xSize = 176;
        this.ySize = 168;
        super.initGui();
    }

	@Override
    protected void drawGuiContainerForegroundLayer(int mx, int my) 
    {
        super.drawGuiContainerForegroundLayer(mx, my);
        this.drawInfo(7, 33, 18, 9, "\\i", "gui.inputFilter");
        this.drawInfo(7, 42, 18, 9, "\\i", "gui.outputFilter");
    }
    
    @Override
    protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) 
    {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.renderEngine.bindTexture(new ResourceLocation("automation", "textures/gui/portableInventory.png"));
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
        if (this.container.isFilterOn(0)) this.drawTexturedModalRect(this.guiLeft + 7, this.guiTop + 34, 176, 0, 18, 8);
        if (this.container.isFilterOn(1)) this.drawTexturedModalRect(this.guiLeft + 7, this.guiTop + 42, 176, 8, 18, 8);
        this.drawStringCentered(this.container.inventory.getInventoryName(), this.guiLeft + this.xSize / 2, this.guiTop + 4, 0x404040);
        this.drawStringCentered("Inventory", this.guiLeft + this.xSize / 2, this.guiTop + 72, 0x404040);
    }
    
    @Override
	protected void mouseClicked(int x, int y, int b) 
	{
		super.mouseClicked(x, y, b);
		byte cmd = -1;
		if (this.func_146978_c(7, 34, 18, 8, x, y)) {
			cmd = 0;
		} else if (this.func_146978_c(7, 42, 18, 8, x, y)) {
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
