package cd4017be.automation.Gui;

import java.io.IOException;

import org.lwjgl.opengl.GL11;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import cd4017be.automation.TileEntity.PneumaticPiston;
import cd4017be.lib.BlockGuiHandler;
import cd4017be.lib.Gui.GuiMachine;
import cd4017be.lib.Gui.TileContainer;
import cd4017be.lib.templates.AutomatedTile;

public class GuiPneumaticPiston extends GuiMachine {

	private final PneumaticPiston tileEntity;
    
    public GuiPneumaticPiston(PneumaticPiston tileEntity, EntityPlayer player)
    {
        super(new TileContainer(tileEntity, player));
        this.tileEntity = tileEntity;
    }

    @Override
    public void initGui() 
    {
        this.xSize = 122;
        this.ySize = 58;
        super.initGui();
        this.guiComps.add(new Slider(0, 81, 16, 34, 212, 54, 12, 4, false));
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mx, int my) 
    {
        super.drawGuiContainerForegroundLayer(mx, my);
        if (this.isPointInRegion(62, 43, 16, 7, mx, my)) {
        	boolean d = mx - this.guiLeft > 70;
        	this.drawSideCube(-64, 8, ((tileEntity.netData.ints[0] >> (d ? 4 : 0) & 0xf) + tileEntity.getOrientation()) % 6, d ? (byte)3 : (byte)2);
        }
        //this.drawInfo(28, 16, 30, 16, "\\i", "transf");
        //this.drawInfo(97, 15, 92, 18, "\\i", "rstCtr");
    }
    
    @Override
    protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) 
    {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.renderEngine.bindTexture(new ResourceLocation("automation", "textures/gui/tesla.png"));
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 144, this.xSize, this.ySize);
        this.drawTexturedModalRect(guiLeft + 61, guiTop + 42, 122, 144 + ((tileEntity.netData.ints[0] & 0xf) + tileEntity.getOrientation()) % 6 * 9, 9, 9);
        this.drawTexturedModalRect(guiLeft + 70, guiTop + 42, 122, 144 + ((tileEntity.netData.ints[0] >> 4 & 0xf) + tileEntity.getOrientation()) % 6 * 9, 9, 9);
        this.drawTexturedModalRect(guiLeft + 7, guiTop + 15, 176 + (tileEntity.netData.ints[0] >> 8 & 2) * 9, 54, 18, 18);
        this.drawTexturedModalRect(guiLeft + 7, guiTop + 33, 176 + (tileEntity.netData.ints[0] >> 8 & 3) * 18, 36, 18, 18);
        this.drawTexturedModalRect(guiLeft + 93, guiTop + 16, 131, 144 + (int)(31F * tileEntity.netData.floats[1] / PneumaticPiston.Amax), 20, 34);
        super.drawGuiContainerBackgroundLayer(var1, var2, var3);
        this.drawStringCentered(tileEntity.getName(), this.guiLeft + this.xSize / 2, this.guiTop + 4, 0x404040);
        fontRendererObj.drawString(String.format("in : %.0fL/r", tileEntity.netData.floats[0] * 1000F), guiLeft + 26, guiTop + 16, 0x808040);
        fontRendererObj.drawString(String.format("out: %.0fL/r", tileEntity.netData.floats[1] * 1000F), guiLeft + 26, guiTop + 24, 0x808040);
        fontRendererObj.drawString(String.format("P: %.1f kW", tileEntity.netData.floats[2] / 1000F), guiLeft + 26, guiTop + 32, 0x808040);
    }

    @Override
    protected void mouseClicked(int x, int y, int b) throws IOException 
    {
        super.mouseClicked(x, y, b);
        byte a = -1;
        if (this.isPointInRegion(8, 16, 16, 16, x, y)) {
            tileEntity.netData.ints[0] ^= 0x200;
            a = 1;
        } else if (this.isPointInRegion(8, 34, 16, 16, x, y)) {
        	tileEntity.netData.ints[0] ^= 0x100;
        	a = 1;
        } else if (this.isPointInRegion(62, 43, 7, 7, x, y)) {
        	int s = tileEntity.netData.ints[0] & 0xf;
        	s = (s + (b == 0 ? 1 : 5)) % 6;
        	tileEntity.netData.ints[0] &= ~0x0f;
        	tileEntity.netData.ints[0] |= s;
        	a = 1;
        } else if (this.isPointInRegion(71, 43, 7, 7, x, y)) {
        	int s = tileEntity.netData.ints[0] >> 4 & 0xf;
        	s = (s + (b == 0 ? 1 : 5)) % 6;
        	tileEntity.netData.ints[0] &= ~0xf0;
        	tileEntity.netData.ints[0] |= s << 4;
        	a = 1;
        }
        if (a >= 0) {
            PacketBuffer dos = tileEntity.getPacketTargetData();
            dos.writeByte(AutomatedTile.CmdOffset + a);
            if (a == 1) dos.writeInt(tileEntity.netData.ints[0]);
            BlockGuiHandler.sendPacketToServer(dos);
        }
    }

	@Override
	protected Object getDisplVar(int id) {
		return 1F - tileEntity.netData.floats[0] / PneumaticPiston.Amax;
	}

	@Override
	protected void setDisplVar(int id, Object obj, boolean send) {
		tileEntity.netData.floats[0] = (1F - (Float)obj) * PneumaticPiston.Amax;
		if (send) {
			PacketBuffer dos = tileEntity.getPacketTargetData();
            dos.writeByte(AutomatedTile.CmdOffset);
            dos.writeFloat(tileEntity.netData.floats[0]);
            BlockGuiHandler.sendPacketToServer(dos);
		}
	}

}
