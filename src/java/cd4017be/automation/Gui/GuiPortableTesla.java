package cd4017be.automation.Gui;

import java.io.IOException;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.gui.GuiTextField;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import cd4017be.lib.BlockGuiHandler;
import cd4017be.lib.templates.GuiMachine;

public class GuiPortableTesla extends GuiMachine 
{
	private final ContainerPortableTesla container;
	private GuiTextField freq;
	private boolean Enabled;
	private byte[] Ipos = new byte[3];
	private int Estore;
	private short mode;
	
	public GuiPortableTesla(ContainerPortableTesla container) 
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
        freq = new GuiTextField(0, fontRendererObj, this.guiLeft + 8, this.guiTop + 16, 34, 16);
    }

    @Override
    public void updateScreen() 
    {
        super.updateScreen();
        ItemStack item = container.player.getCurrentEquippedItem();
        if (item != null && item.getTagCompound() != null) {
        	mode = item.getTagCompound().getShort("mode");
        	double u = item.getTagCompound().getDouble("voltage");
        	Estore = (int)(u * u * 0.001D);
        	Enabled = (mode & 0x1) != 0;
        	for (int i = 0; i < 3; i++) {
        		
        		Ipos[i] = (byte)(mode >> (8 + i * 2) & 0x3);
        	}
        	if (!freq.isFocused()) 
        		freq.setText("" + item.getTagCompound().getShort("freq"));
        }
        freq.updateCursorCounter();
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mx, int my) 
    {
        super.drawGuiContainerForegroundLayer(mx, my);
        this.drawInfo(44, 16, 16, 16, "\\i", "tesla.set");
        this.drawInfo(62, 25, 16, 7, "\\i", "teslaP.on");
        this.drawInfo(116, 25, 16, 7, "\\i", "teslaP.invH");
        this.drawInfo(134, 25, 16, 7, "\\i", "teslaP.invM");
        this.drawInfo(152, 25, 16, 7, "\\i", "teslaP.invA");
    }
    
    @Override
    protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) 
    {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.renderEngine.bindTexture(new ResourceLocation("automation", "textures/gui/portableTesla.png"));
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
        this.drawTexturedModalRect(guiLeft + 61, guiTop + 24, 230, Enabled ? 18 : 9, 18, 9);
        for (int i = 0; i < 3; i++) {
        	if (Ipos[i] == 1) this.drawTexturedModalRect(guiLeft + 115 + i * 18, guiTop + 24, 176 + i * 18, 9, 18, 9);
        	else if (Ipos[i] == 2) this.drawTexturedModalRect(guiLeft + 115 + i * 18, guiTop + 24, 176 + i * 18, 18, 18, 9);
        }
        freq.drawTextBox();
        this.drawLocString(this.guiLeft + 62, this.guiTop + 16, 8, 0xffdf40, "tesla.stor", Estore);
        this.drawStringCentered(StatCollector.translateToLocal("gui.cd4017be.portableTesla.name"), this.guiLeft + this.xSize / 2, this.guiTop + 4, 0x404040);
        this.drawStringCentered(StatCollector.translateToLocal("container.inventory"), this.guiLeft + this.xSize / 2, this.guiTop + 36, 0x404040);
    }
    
    @Override
    protected void mouseClicked(int x, int y, int b) throws IOException 
    {
        freq.mouseClicked(x, y, b);
    	byte cmd = -1;
        short v = 0;
    	if (this.isPointInRegion(61, 24, 18, 9, x, y)) {
            cmd = 0;
            mode ^= 1;
        } else if (this.isPointInRegion(115, 24, 54, 9, x, y)) {
        	cmd = 0;
            int i = ((x - this.guiLeft - 115) / 18) % 3;
            int p = 8 + i * 2;
            int k = mode >> p & 0x3;
            k = (k + (b == 0 ? 1 : 2)) % 3;
            mode = (short)((mode & ~(0x3 << p)) | (k << p));
        } else
        if (this.isPointInRegion(44, 16, 16, 16, x, y)) {
        	cmd = 1;
        	v = (short)this.getNumber(freq.getText());
        	if (v < 0) v = 0;
        }
        if (cmd >= 0)
        {
            PacketBuffer dos = BlockGuiHandler.getPacketTargetData(new BlockPos(0, -1, 0));
            dos.writeByte(cmd);
            if (cmd == 0) dos.writeShort(mode);
            else if (cmd == 1) dos.writeShort(v); 
            BlockGuiHandler.sendPacketToServer(dos);
        }
        super.mouseClicked(x, y, b);
    }
    
    @Override
    protected void keyTyped(char c, int k) throws IOException 
    {
        if (!freq.isFocused()) super.keyTyped(c, k);
        freq.textboxKeyTyped(c, k);
    }
    
    private int getNumber(String s)
    {
    	try{
    		return Integer.parseInt(s);
    	} catch(NumberFormatException e) {
    		return 0;
    	}
    }

}
