package cd4017be.automation.Gui;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.lwjgl.opengl.GL11;

import cd4017be.lib.BlockGuiHandler;
import cd4017be.lib.templates.GuiMachine;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class GuiPortableGenerator extends GuiMachine 
{

	private final ContainerPortableGenerator container;
	private int energy;
	
	public GuiPortableGenerator(ContainerPortableGenerator container) 
	{
		super(container);
		this.container = container;
	}

	@Override
    public void initGui() 
    {
        this.xSize = 176;
        this.ySize = 132;
        super.initGui();
    }

    @Override
	public void updateScreen() 
    {
    	ItemStack item = this.container.player.getCurrentEquippedItem();
		if (item != null && item.stackTagCompound != null) energy = item.stackTagCompound.getInteger("buff");
		else energy = 0;
		super.updateScreen();
	}

	@Override
    protected void drawGuiContainerForegroundLayer(int mx, int my) 
    {
        super.drawGuiContainerForegroundLayer(mx, my);
        this.drawInfo(61, 15, 18, 18, "\\i", "inputFilter");
    }
    
    @Override
    protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) 
    {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.renderEngine.bindTexture(new ResourceLocation("automation", "textures/gui/portableGenerator.png"));
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
        if (this.container.isFilterOn(0)) this.drawTexturedModalRect(this.guiLeft + 61, this.guiTop + 19, 176, 16, 18, 10);
        if (energy > 0) {
        	this.drawTexturedModalRect(this.guiLeft + 99, this.guiTop + 18, 176, 0, 14, 14);
        	this.drawStringCentered(energy + " kJ", this.guiLeft + 142, this.guiTop + 20, 0x404040);
        }
        this.drawStringCentered(this.container.inventory.getInventoryName(), this.guiLeft + this.xSize / 2, this.guiTop + 4, 0x404040);
        this.drawStringCentered("Inventory", this.guiLeft + this.xSize / 2, this.guiTop + 36, 0x404040);
    }

	@Override
	protected void mouseClicked(int x, int y, int b) 
	{
		super.mouseClicked(x, y, b);
		byte cmd = -1;
		if (this.func_146978_c(61, 19, 18, 10, x, y)) {
			cmd = 0;
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
