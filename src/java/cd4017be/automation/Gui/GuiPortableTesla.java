package cd4017be.automation.Gui;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.gui.GuiTextField;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import cd4017be.lib.BlockGuiHandler;
import cd4017be.lib.templates.GuiMachine;

public class GuiPortableTesla extends GuiMachine 
{
	private final ContainerPortableTesla container;
	private GuiTextField freq;
	private boolean[] Etype = new boolean[3];
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
        freq = new GuiTextField(fontRendererObj, this.guiLeft + 8, this.guiTop + 16, 34, 16);
    }

    @Override
    public void updateScreen() 
    {
        super.updateScreen();
        ItemStack item = container.player.getCurrentEquippedItem();
        if (item != null && item.stackTagCompound != null) {
        	mode = item.stackTagCompound.getShort("mode");
        	double u = item.stackTagCompound.getDouble("voltage");
        	Estore = (int)(u * u * 0.001D);
        	for (int i = 0; i < 3; i++) {
        		Etype[i] = (mode >> i & 0x1) != 0;
        		Ipos[i] = (byte)(mode >> (8 + i * 2) & 0x3);
        	}
        	if (!freq.isFocused()) 
        		freq.setText("" + item.stackTagCompound.getShort("freq"));
        }
        freq.updateCursorCounter();
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mx, int my) 
    {
        super.drawGuiContainerForegroundLayer(mx, my);
        this.drawInfo(44, 16, 16, 16, "\\i", "gui.tesla.set");
        this.drawInfo(62, 25, 16, 7, "\\i", "gui.teslaP.kJ");
        this.drawInfo(80, 25, 16, 7, "\\i", "gui.teslaP.RF");
        this.drawInfo(98, 25, 16, 7, "\\i", "gui.teslaP.EU");
        this.drawInfo(116, 25, 16, 7, "\\i", "gui.teslaP.invH");
        this.drawInfo(134, 25, 16, 7, "\\i", "gui.teslaP.invM");
        this.drawInfo(152, 25, 16, 7, "\\i", "gui.teslaP.invA");
    }
    
    @Override
    protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) 
    {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.renderEngine.bindTexture(new ResourceLocation("automation", "textures/gui/portableTesla.png"));
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
        for (int i = 0; i < 3; i++) {
        	if (Etype[i]) this.drawTexturedModalRect(guiLeft + 61 + i * 18, guiTop + 24, 176 + i * 18, 0, 18, 9);
        	if (Ipos[i] == 1) this.drawTexturedModalRect(guiLeft + 115 + i * 18, guiTop + 24, 176 + i * 18, 9, 18, 9);
        	else if (Ipos[i] == 2) this.drawTexturedModalRect(guiLeft + 115 + i * 18, guiTop + 24, 176 + i * 18, 18, 18, 9);
        }
        freq.drawTextBox();
        this.drawStringCentered("Energy = " + Estore + "kJ", this.guiLeft + 115, this.guiTop + 16, 0xffdf40);
        this.drawStringCentered("Portable Tesla Transmitter", this.guiLeft + this.xSize / 2, this.guiTop + 4, 0x404040);
        this.drawStringCentered("Inventory", this.guiLeft + this.xSize / 2, this.guiTop + 36, 0x404040);
    }
    
    @Override
    protected void mouseClicked(int x, int y, int b) 
    {
        freq.mouseClicked(x, y, b);
    	byte cmd = -1;
        short v = 0;
    	if (this.func_146978_c(61, 24, 54, 9, x, y)) {
            cmd = 0;
            int i = ((x - this.guiLeft - 61) / 18) % 3;
            mode ^= 1 << i;
        } else if (this.func_146978_c(115, 24, 54, 9, x, y)) {
        	cmd = 0;
            int i = ((x - this.guiLeft - 115) / 18) % 3;
            int p = 8 + i * 2;
            int k = mode >> p & 0x3;
            k = (k + (b == 0 ? 1 : 2)) % 3;
            mode = (short)((mode & ~(0x3 << p)) | (k << p));
        } else
        if (this.func_146978_c(44, 16, 16, 16, x, y)) {
        	cmd = 1;
        	v = (short)this.getNumber(freq.getText());
        	if (v < 0) v = 0;
        }
        if (cmd >= 0)
        {
            try {
            ByteArrayOutputStream bos = BlockGuiHandler.getPacketTargetData(0, -1, 0);
            DataOutputStream dos = new DataOutputStream(bos);
            dos.writeByte(cmd);
            if (cmd == 0) dos.writeShort(mode);
            else if (cmd == 1) dos.writeShort(v); 
            BlockGuiHandler.sendPacketToServer(bos);
            } catch (IOException e){}
        }
        super.mouseClicked(x, y, b);
    }
    
    @Override
    protected void keyTyped(char c, int k) 
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
