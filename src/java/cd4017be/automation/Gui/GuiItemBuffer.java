package cd4017be.automation.Gui;

import java.io.IOException;

import org.lwjgl.opengl.GL11;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;
import cd4017be.automation.TileEntity.ItemBuffer;
import cd4017be.lib.BlockGuiHandler;
import cd4017be.lib.Gui.GuiMachine;
import cd4017be.lib.Gui.TileContainer;
import cd4017be.lib.templates.AutomatedTile;

public class GuiItemBuffer extends GuiMachine {

private final ItemBuffer tileEntity;
    
    public GuiItemBuffer(ItemBuffer tileEntity, EntityPlayer player)
    {
        super(new TileContainer(tileEntity, player));
        this.tileEntity = tileEntity;
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
		this.drawInfo(8, 52, 16, 16, "\\i", "buffer.stack" + (tileEntity.netData.ints[0] >> 8 & 1));
		this.drawInfo(44, 56, 16, 8, "\\i", "buffer.overfl");
		this.drawInfo(89, 56, 16, 8, "\\i", "buffer.split");
		this.drawInfo(134, 56, 16, 8, "\\i", "buffer.split");
	}

	@Override
    protected void drawGuiContainerBackgroundLayer(float f, int x, int y) 
    {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.renderEngine.bindTexture(new ResourceLocation("automation", "textures/gui/buffer.png"));
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
        if ((tileEntity.netData.ints[0] & 0x100) != 0) this.drawTexturedModalRect(guiLeft + 7, guiTop + 51, 176, 0, 18, 18);
        int n = tileEntity.netData.ints[0] & 0xff;
        String s;
        if (n == 0) s = "OFF";
        else if (n >= 18) s = "ALL";
        else s = "" + n;
        this.drawStringCentered(s, guiLeft + 52, guiTop + 56, 0x000000);
        this.drawStringCentered("" + tileEntity.netData.ints[1], guiLeft + 97, guiTop + 56, 0x000000);
        this.drawStringCentered("" + tileEntity.netData.ints[2], guiLeft + 142, guiTop + 56, 0x000000);
        this.drawStringCentered(tileEntity.getName(), this.guiLeft + this.xSize / 2, this.guiTop + 4, 0x404040);
        this.drawStringCentered(I18n.translateToLocal("container.inventory"), this.guiLeft + this.xSize / 2, this.guiTop + 72, 0x404040);
        super.drawGuiContainerBackgroundLayer(f, x, y);
    }
    
    @Override
    protected void mouseClicked(int x, int y, int b) throws IOException 
    {
        super.mouseClicked(x, y, b);
        byte cmd = -1;
        if (this.isPointInRegion(8, 52, 16, 16, x, y)) {
        	cmd = 0;
        	tileEntity.netData.ints[0] ^= 0x100;
        } else if (this.isPointInRegion(43, 51, 18, 5, x, y)) {
        	cmd = 0;
        	int n = tileEntity.netData.ints[0] & 0xff;
        	n += b == 0 ? 1 : 6;
        	if (n > 18) n = 18;
        	tileEntity.netData.ints[0] = (tileEntity.netData.ints[0] & 0x100) | n;
        } else if (this.isPointInRegion(43, 64, 18, 5, x, y)) {
        	cmd = 0;
        	int n = tileEntity.netData.ints[0] & 0xff;
        	n -= b == 0 ? 1 : 6;
        	if (n < 0) n = 0;
        	tileEntity.netData.ints[0] = (tileEntity.netData.ints[0] & 0x100) | n;
        } else if (this.isPointInRegion(88, 51, 18, 5, x, y)) {
        	cmd = 1;
        	tileEntity.netData.ints[1] += b == 0 ? 1 : 8;
        	if (tileEntity.netData.ints[1] > 64 - tileEntity.netData.ints[2]) tileEntity.netData.ints[1] = 64 - tileEntity.netData.ints[2];
        } else if (this.isPointInRegion(88, 64, 18, 5, x, y)) {
        	cmd = 1;
        	tileEntity.netData.ints[1] -= b == 0 ? 1 : 8;
        	if (tileEntity.netData.ints[1] < 0) tileEntity.netData.ints[1] = 0;
        } else if (this.isPointInRegion(133, 51, 18, 5, x, y)) {
        	cmd = 2;
        	tileEntity.netData.ints[2] += b == 0 ? 1 : 8;
        	if (tileEntity.netData.ints[2] > 64 - tileEntity.netData.ints[1]) tileEntity.netData.ints[2] = 64 - tileEntity.netData.ints[1];
        } else if (this.isPointInRegion(133, 64, 18, 5, x, y)) {
        	cmd = 2;
        	tileEntity.netData.ints[2] -= b == 0 ? 1 : 8;
        	if (tileEntity.netData.ints[2] < 0) tileEntity.netData.ints[2] = 0;
        }
        if (cmd >= 0) {
                PacketBuffer dos = tileEntity.getPacketTargetData();
                dos.writeByte(AutomatedTile.CmdOffset + cmd);
                dos.writeInt(tileEntity.netData.ints[cmd]);
                BlockGuiHandler.sendPacketToServer(dos);
        }
    }

}
