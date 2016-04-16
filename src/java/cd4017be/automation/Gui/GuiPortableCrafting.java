package cd4017be.automation.Gui;

import java.io.IOException;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import cd4017be.lib.BlockGuiHandler;
import cd4017be.lib.templates.GuiMachine;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.I18n;

public class GuiPortableCrafting extends GuiMachine 
{
	
	private final ContainerPortableCrafting container;
	
	public GuiPortableCrafting(ContainerPortableCrafting container) 
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
        this.drawInfo(124, 33, 18, 18, "\\i", "startCraft");
        this.drawInfo(142, 33, 18, 18, "\\i", "autoCraft" + (this.container.isAuto()?"1":"0"));
        this.drawInfo(92, 55, 10, 10, "\\i", "directCraft");
        this.drawInfo(74, 55, 10, 10, "\\i", "clearRecipe");
    }
    
    @Override
    protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) 
    {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.renderEngine.bindTexture(new ResourceLocation("automation", "textures/gui/portableCrafting.png"));
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
        if (this.container.isActive()) this.drawTexturedModalRect(this.guiLeft + 124, this.guiTop + 33, 176, 0, 18, 18);
        if (this.container.isAuto()) this.drawTexturedModalRect(this.guiLeft + 142, this.guiTop + 33, 194, 0, 18, 18);
        this.drawStringCentered("" + this.container.getAmount(), this.guiLeft + 115, this.guiTop + 38, 0x404040);
        this.drawStringCentered(this.container.inventoryName(), this.guiLeft + this.xSize / 2, this.guiTop + 4, 0x404040);
        this.drawStringCentered(I18n.translateToLocal("container.inventory"), this.guiLeft + this.xSize / 2, this.guiTop + 72, 0x404040);
    }
    
    @Override
	protected void mouseClicked(int x, int y, int b) throws IOException 
	{
		int n = 0;
		byte cmd = -1;
		if (this.isPointInRegion(124, 33, 18, 18, x, y)) {
			cmd = 0;
		} else if (this.isPointInRegion(142, 33, 18, 18, x, y)) {
			cmd = 1;
		} else if (this.isPointInRegion(106, 33, 18, 6, x, y)) {
			n = b == 0 ? 1 : 8;
			cmd = 2;
		} else if (this.isPointInRegion(106, 45, 18, 6, x, y)) {
			n = b == 0 ? -1 : -8;
			cmd = 2;
		} else if (this.isPointInRegion(89, 34, 16, 16, x, y)) {
			n = (b == 0 ? 1 : 8) * (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) ? 64 : 1);
			cmd = 3;
		} else if (this.isPointInRegion(74, 55, 10, 10, x, y)) {
			for (int i = 0; i < 9; i++) 
				this.mc.playerController.windowClick(this.inventorySlots.windowId, i + 36, 0, 0, this.mc.thePlayer);
		}
		if (cmd >= 0) {
	            PacketBuffer dos = BlockGuiHandler.getPacketTargetData(new BlockPos(0, -1, 0));
	            dos.writeByte(cmd);
	            if (cmd == 2 || cmd == 3) dos.writeByte(n);
	            BlockGuiHandler.sendPacketToServer(dos);
		} else super.mouseClicked(x, y, b);
	}
}
