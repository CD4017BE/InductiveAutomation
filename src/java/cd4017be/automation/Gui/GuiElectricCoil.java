package cd4017be.automation.Gui;

import java.io.IOException;

import org.lwjgl.opengl.GL11;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import cd4017be.automation.TileEntity.ElectricCoil;
import cd4017be.lib.BlockGuiHandler;
import cd4017be.lib.TileContainer;
import cd4017be.lib.TooltipInfo;
import cd4017be.lib.templates.AutomatedTile;
import cd4017be.lib.templates.GuiMachine;

public class GuiElectricCoil extends GuiMachine {

	private final ElectricCoil tileEntity;
    
    public GuiElectricCoil(ElectricCoil tileEntity, EntityPlayer player)
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
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mx, int my) 
    {
        super.drawGuiContainerForegroundLayer(mx, my);
        //this.drawInfo(28, 16, 30, 16, "\\i", "transf");
        //this.drawInfo(97, 15, 92, 18, "\\i", "rstCtr");
    }
    
    @Override
    protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) 
    {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.renderEngine.bindTexture(new ResourceLocation("automation", "textures/gui/tesla.png"));
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 80, this.xSize, this.ySize);
        this.drawTexturedModalRect(guiLeft + 7, guiTop + 15, 176 + (tileEntity.netData.ints[1] & 2) * 9, 54, 18, 18);
        this.drawTexturedModalRect(guiLeft + 7, guiTop + 33, 176 + (tileEntity.netData.ints[1] & 3) * 18, 36, 18, 18);
        this.drawTexturedModalRect(guiLeft + 81, guiTop + 16 + tileEntity.getNscaled(32), 212, 54, 12, 4);
        this.drawStringCentered(tileEntity.getName(), this.guiLeft + this.xSize / 2, this.guiTop + 4, 0x404040);
        fontRendererObj.drawString(String.format("U:%.0fV", tileEntity.netData.floats[1]), guiLeft + 26, guiTop + 16, 0x808040);
        fontRendererObj.drawString(String.format("P:%+.1f" + TooltipInfo.getPowerUnit(), tileEntity.netData.floats[0] / 1000F), guiLeft + 26, guiTop + 24, 0x808040);
        fontRendererObj.drawString(String.format("N = %d", tileEntity.netData.ints[0]), guiLeft + 30, guiTop + 38, 0x404040);
    }

    @Override
    protected void mouseClicked(int x, int y, int b) throws IOException 
    {
        super.mouseClicked(x, y, b);
        byte a = -1;
        if (this.isPointInRegion(8, 16, 16, 16, x, y)) {
            tileEntity.netData.ints[1] ^= 2;
            a = 1;
        } else if (this.isPointInRegion(8, 34, 16, 16, x, y)) {
        	tileEntity.netData.ints[1] ^= 1;
        	a = 1;
        } else if (this.isPointInRegion(82, 16, 10, 36, x, y)) {
			int p = y - this.guiTop - 18;
			if (p < 0) p = 0;
			else if (p > 30) p = 30;
        	tileEntity.netData.ints[0] = p * (ElectricCoil.Nmax[tileEntity.type] - ElectricCoil.Nmin[tileEntity.type]) / 30 + ElectricCoil.Nmin[tileEntity.type];
        	a = 0;
		}
        if (a >= 0) {
            PacketBuffer dos = tileEntity.getPacketTargetData();
            dos.writeByte(AutomatedTile.CmdOffset + a);
            if (a == 0) dos.writeInt(tileEntity.netData.ints[0]);
            else if (a == 1) dos.writeByte(tileEntity.netData.ints[1]);
            BlockGuiHandler.sendPacketToServer(dos);
        }
    }

	@Override
	protected void mouseClickMove(int x, int y, int b, long t) {
		if (this.isPointInRegion(82, 16, 10, 36, x, y)) {
			int p = y - this.guiTop - 18;
			if (p < 0) p = 0;
			else if (p > 30) p = 30;
        	tileEntity.netData.ints[0] = p * (ElectricCoil.Nmax[tileEntity.type] - ElectricCoil.Nmin[tileEntity.type]) / 30 + ElectricCoil.Nmin[tileEntity.type];
		} else super.mouseClickMove(x, y, b, t);
	}

	@Override
	protected void mouseReleased(int x, int y, int b) {
		if (this.isPointInRegion(82, 16, 10, 36, x, y)) {
			int p = y - this.guiTop - 18;
			if (p < 0) p = 0;
			else if (p > 30) p = 30;
        	tileEntity.netData.ints[0] = p * (ElectricCoil.Nmax[tileEntity.type] - ElectricCoil.Nmin[tileEntity.type]) / 30 + ElectricCoil.Nmin[tileEntity.type];
        	PacketBuffer dos = tileEntity.getPacketTargetData();
            dos.writeByte(AutomatedTile.CmdOffset);
            dos.writeInt(tileEntity.netData.ints[0]);
            BlockGuiHandler.sendPacketToServer(dos);
		}
		super.mouseReleased(x, y, b);
	}

}
