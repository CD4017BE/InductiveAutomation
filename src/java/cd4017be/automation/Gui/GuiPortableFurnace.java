package cd4017be.automation.Gui;

import java.io.IOException;

import org.lwjgl.opengl.GL11;

import cd4017be.api.energy.EnergyAPI;
import cd4017be.api.energy.EnergyAutomation.IEnergyItem;
import cd4017be.lib.BlockGuiHandler;
import cd4017be.lib.Gui.GuiMachine;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;

public class GuiPortableFurnace extends GuiMachine {
	
	private final ContainerPortableFurnace furnace;
	private int energy;
	private int capacity;
	
	public GuiPortableFurnace(ContainerPortableFurnace container) 
	{
		super(container);
		this.furnace = container;
	}

	@Override
    public void initGui() 
    {
        this.xSize = 176;
        this.ySize = 132;
        super.initGui();
        ItemStack item = this.furnace.player.getHeldItemMainhand();
        if (item.getItem() instanceof IEnergyItem) {
        	capacity = ((IEnergyItem)item.getItem()).getEnergyCap(item);
        }
    }

    @Override
	public void updateScreen() 
    {
		energy = (int)(EnergyAPI.get(this.furnace.player.getHeldItemMainhand()).getStorage(0) / 1000D);
		super.updateScreen();
	}

	@Override
    protected void drawGuiContainerForegroundLayer(int mx, int my) 
    {
        super.drawGuiContainerForegroundLayer(mx, my);
        this.drawFormatInfo(116, 16, 34, 16, "storage", energy, capacity);
        this.drawInfo(61, 15, 18, 18, "\\i", "inputFilter");
    }
    
    @Override
    protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) 
    {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.renderEngine.bindTexture(new ResourceLocation("automation", "textures/gui/portableFurnace.png"));
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
        this.drawTexturedModalRect(this.guiLeft + 116, this.guiTop + 16, 176, 0, energy * 34 / capacity, 16);
        if (this.furnace.isFilterOn(0)) this.drawTexturedModalRect(this.guiLeft + 61, this.guiTop + 19, 176, 16, 18, 10);
        this.drawStringCentered(this.furnace.inventory.getName(), this.guiLeft + this.xSize / 2, this.guiTop + 4, 0x404040);
        this.drawStringCentered(I18n.translateToLocal("container.inventory"), this.guiLeft + this.xSize / 2, this.guiTop + 36, 0x404040);
    }

	@Override
	protected void mouseClicked(int x, int y, int b) throws IOException 
	{
		super.mouseClicked(x, y, b);
		byte cmd = -1;
		if (this.isPointInRegion(61, 19, 18, 10, x, y)) {
			cmd = 0;
		}
		if (cmd >= 0) {
	            PacketBuffer dos = BlockGuiHandler.getPacketTargetData(new BlockPos(0, -1, 0));
	            dos.writeByte(cmd);
	            BlockGuiHandler.sendPacketToServer(dos);
		}
	}

}
