package cd4017be.automation.Gui;

import java.io.IOException;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.gui.GuiTextField;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.I18n;
import cd4017be.lib.BlockGuiHandler;
import cd4017be.lib.templates.GuiMachine;

public class GuiPlacement extends GuiMachine 
{
	
	private final ContainerPlacement container;
	private int sel = 0;
	private static final byte[] axis = {0, 0, 0, 0, 2, 2,
										2, 2, 1, 1, 1, 1};
	private GuiTextField[] Vxy = new GuiTextField[2];

    public GuiPlacement(ContainerPlacement container)
    {
        super(container);
        this.container = container;
    }
    
    @Override
    public void initGui() 
    {
        this.xSize = 176;
        this.ySize = 150;
        super.initGui();
        for (int i = 0; i < Vxy.length; i++) Vxy[i] = new GuiTextField(0, fontRendererObj, guiLeft + 42 + i * 54, guiTop + 36, 34, 12);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mx, int my) 
    {
        super.drawGuiContainerForegroundLayer(mx, my);
        this.drawInfo(152, 16, 16, 16, "\\i", "place.useBlock");
        this.drawInfo(8, 34, 16, 16, "\\i", "place.dir");
        this.drawInfo(134, 34, 16, 16, "\\i", "place.sneak");
        this.drawInfo(152, 34, 16, 16, "\\i", "filter.meta");
        this.drawInfo(28, 36, 102, 12, "\\i", "place.aim");
    }
    
    @Override
    protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) 
    {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.renderEngine.bindTexture(new ResourceLocation("automation", "textures/gui/blockPlacement.png"));
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
        this.drawTexturedModalRect(this.guiLeft + 8 + 18 * sel, this.guiTop + 16, 176, 0, 16, 16);
        if (!this.container.inventory.useBlock) this.drawTexturedModalRect(this.guiLeft + 152, this.guiTop + 16, 176, 64, 16, 16);
        if ((this.container.inventory.placement[sel] & 8) == 0) this.drawTexturedModalRect(this.guiLeft + 134, this.guiTop + 34, 176, 64, 16, 16);
        if ((this.container.inventory.placement[sel] & 16) != 0) this.drawTexturedModalRect(guiLeft + 151, guiTop + 33, 176, 90, 18, 18);
        this.drawTexturedModalRect(this.guiLeft + 7, this.guiTop + 33, 194, 18 * (this.container.inventory.placement[sel] & 7), 18, 18);
        this.drawTexturedModalRect(this.guiLeft + 25, this.guiTop + 36, 176, 18 + 16 * axis[(this.container.inventory.placement[sel] & 7) % 6], 16, 12);
        this.drawTexturedModalRect(this.guiLeft + 79, this.guiTop + 36, 176, 18 + 16 * axis[6 + (this.container.inventory.placement[sel] & 7) % 6], 16, 12);
        for (GuiTextField tf : Vxy) tf.drawTextBox();
        this.drawStringCentered(this.container.type.getDisplayName(), this.guiLeft + this.xSize / 2, this.guiTop + 4, 0x404040);
        this.drawStringCentered(I18n.translateToLocal("container.inventory"), this.guiLeft + this.xSize / 2, this.guiTop + 54, 0x404040);
    }
    
    @Override
	public void updateScreen() 
    {
    	for (int i = 0; i < Vxy.length; i++) 
    		if (!Vxy[i].isFocused())
    			Vxy[i].setText(String.format("%.2f", container.inventory.Vxy[sel + 8 * i]).replace(',', '.'));
		super.updateScreen();
	}

	@Override
	protected void keyTyped(char c, int k) throws IOException 
	{
		for (int i = 0; i < Vxy.length; i++)
			if (Vxy[i].isFocused()) {
				if (k == Keyboard.KEY_RETURN) {
					try {
			            PacketBuffer dos = BlockGuiHandler.getPacketTargetData(new BlockPos(0, -1, 0));
			            dos.writeByte(2);
			            dos.writeByte(i * 8 + sel);
			            float f = Float.parseFloat(Vxy[i].getText());
			            if (f < 0) f = 0;
			            else if (f > 1) f = 1;
			            container.inventory.Vxy[sel + i * 8] = f;
			            dos.writeFloat(f);
			            BlockGuiHandler.sendPacketToServer(dos);
			            Vxy[i].setFocused(false);
			        } catch (NumberFormatException e) {}
				} else Vxy[i].textboxKeyTyped(c, k);
				return;
			}
		super.keyTyped(c, k);
	}

	@Override
    protected void mouseClicked(int x, int y, int b) throws IOException
    {
        for (GuiTextField tf : Vxy) tf.mouseClicked(x, y, b);
		byte a = -1;
        if (this.isPointInRegion(7, 15, 144, 18, x, y) && b == 0) {
            sel = (x - this.guiLeft - 7) / 18;
            if (sel < 0 || sel >= 8) sel = 0;
            return;
        } else
        if (this.isPointInRegion(152, 16, 16, 16, x, y)) {
            a = (container.inventory.useBlock = !container.inventory.useBlock) ? (byte)1 : 0;
        } else
        if (this.isPointInRegion(8, 34, 16, 16, x, y)) {
            a = 3;
            container.inventory.placement[sel] = (byte)((container.inventory.placement[sel] & 24) | ((container.inventory.placement[sel] & 7) + 1) % 6);
        } else
        if (this.isPointInRegion(134, 34, 16, 16, x, y)) {
            a = 3;
            container.inventory.placement[sel] ^= 8;
        } else
        if (this.isPointInRegion(152, 34, 16, 16, x, y)) {
            a = 3;
            container.inventory.placement[sel] ^= 16;
        }
        if (a >= 0)
        {
            PacketBuffer dos = BlockGuiHandler.getPacketTargetData(new BlockPos(0, -1, 0));
            dos.writeByte(a);
            if (a == 3) {
            	dos.writeByte(sel);
            	dos.writeByte(container.inventory.placement[sel]);
            }
            BlockGuiHandler.sendPacketToServer(dos);
        }
        super.mouseClicked(x, y, b);
    }

}
