/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cd4017be.automation.Gui;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import cd4017be.automation.TileEntity.Builder;
import cd4017be.lib.BlockGuiHandler;
import cd4017be.lib.TileContainer;
import cd4017be.lib.templates.AutomatedTile;
import cd4017be.lib.templates.GuiMachine;

/**
 *
 * @author CD4017BE
 */
public class GuiBuilder extends GuiMachine
{
    private static final String[] steps = {"Inactive", "Frame Y", "Frame Z", "Frame X", "Bottom", "Top", "North", "South", "West", "East", "Filling"};
    private static final String[] dirs = {"XZ", "XY", "ZY"};
    private Builder tileEntity;

    public GuiBuilder(Builder tileEntity, EntityPlayer player)
    {
        super(new TileContainer(tileEntity, player));
        this.tileEntity = tileEntity;
    }

    @Override
    public void initGui() 
    {
        this.xSize = 176;
        this.ySize = 240;
        super.initGui();
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mx, int my) 
    {
        super.drawGuiContainerForegroundLayer(mx, my);
        this.drawInfo(116, 16, 52, 16, "\\i", "gui.builder.frame");
        this.drawInfo(116, 34, 52, 16, "\\i", "gui.builder.wall");
        this.drawInfo(152, 52, 16, 16, "\\i", "gui.builder.stack");
        this.drawInfo(7, 75, 144, 6, "\\i", "gui.builder.size");
        this.drawInfo(152, 70, 16, 16, "\\i", "gui.builder.dir");
    }
    
    @Override
    protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) 
    {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.renderEngine.bindTexture(new ResourceLocation("automation", "textures/gui/builder.png"));
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
        this.drawItemConfig(tileEntity, -18, 7);
        this.drawEnergyConfig(tileEntity, -36, 7);
        for (int i = 0; i < tileEntity.stackIter.length; i++)
        {
            this.drawStringCentered("" + tileEntity.stackIter[i], this.guiLeft + 16 + 18 * i, this.guiTop + 74, 0x404040);
        }
        this.drawStringCentered(dirs[tileEntity.stackDir], this.guiLeft + 160, this.guiTop + 74, 0x404040);
        this.drawStringCentered(steps[tileEntity.step], this.guiLeft + 34, this.guiTop + 20, 0x404040);
        this.drawStringCentered("< Frame", this.guiLeft + 142, this.guiTop + 20, 0x404040);
        this.drawStringCentered("< Walls", this.guiLeft + 142, this.guiTop + 38, 0x404040);
        this.drawStringCentered("Builder", this.guiLeft + this.xSize / 2, this.guiTop + 6, 0x404040);
        this.drawStringCentered("Inventory", this.guiLeft + this.xSize / 2, this.guiTop + 145, 0x404040);
    }

    @Override
    protected void mouseClicked(int x, int y, int b) 
    {
        this.clickItemConfig(tileEntity, x - this.guiLeft + 18, y - this.guiTop - 7);
        this.clickEnergyConfig(tileEntity, x - this.guiLeft + 36, y - this.guiTop - 7);
        if (this.func_146978_c(7, 69, 144, 5, x, y))
        {
            sendClick((((x - this.guiLeft - 7) / 18) << 1) | 1);
        } else
        if (this.func_146978_c(7, 82, 144, 5, x, y))
        {
            sendClick(((x - this.guiLeft - 7) / 18) << 1);
        } else
        if (this.func_146978_c(151, 69, 18, 18, x, y))
        {
            sendClick(16);
        } else
        if (this.func_146978_c(7, 15, 54, 18, x, y))
        {
            sendClick(17);
        }
        super.mouseClicked(x, y, b);
    }
    
    private void sendClick(int n)
    {
        try {
        ByteArrayOutputStream bos = tileEntity.getPacketTargetData();
        DataOutputStream dos = new DataOutputStream(bos);
        dos.writeByte(n + AutomatedTile.CmdOffset);
        BlockGuiHandler.sendPacketToServer(bos);
        } catch (IOException e){}
    }
    
}
