/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cd4017be.automation.Gui;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import cd4017be.automation.TileEntity.TeslaTransmitter;
import cd4017be.lib.BlockGuiHandler;
import cd4017be.lib.TileContainer;
import cd4017be.lib.templates.GuiMachine;

/**
 *
 * @author CD4017BE
 */
public class GuiTeslaTransmitter extends GuiMachine
{
    
    private GuiTextField textField;
    private final TeslaTransmitter tileEntity;
    
    public GuiTeslaTransmitter(TeslaTransmitter tileEntity, EntityPlayer player)
    {
        super(new TileContainer(tileEntity, player));
        this.tileEntity = tileEntity;
    }

    @Override
    public void initGui() 
    {
        this.xSize = 176;
        this.ySize = 40;
        super.initGui();
        textField = new GuiTextField(this.fontRendererObj, this.guiLeft + 8, this.guiTop + 16, 34, 16);
    }
    
    @Override
    public void updateScreen() 
    {
        super.updateScreen();
        textField.updateCursorCounter();
        if (!textField.isFocused())
        {
            textField.setText("" + tileEntity.getFrequency());
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mx, int my) 
    {
        super.drawGuiContainerForegroundLayer(mx, my);
        this.drawInfo(44, 16, 16, 16, "\\i", "tesla.set");
    }
    
    @Override
    protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) 
    {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.renderEngine.bindTexture(new ResourceLocation("automation", "textures/gui/tesla.png"));
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 40, this.xSize, this.ySize);
        textField.drawTextBox();
        this.drawStringCentered("Energy = " + (int)(tileEntity.netData.floats[0] / 1000F) + "kJ", this.guiLeft + 115, this.guiTop + 20, 0xffdf40);
        this.drawStringCentered((tileEntity.interdim ? "Interdimensional " : "") + tileEntity.getInventoryName(), this.guiLeft + this.xSize / 2, this.guiTop + 4, 0x404040);
    }
    
    @Override
    protected void mouseClicked(int x, int y, int b) 
    {
        if (this.func_146978_c(43, 15, 18, 18, x, y))
        {
            try {
                tileEntity.netData.ints[0] = Short.parseShort(textField.getText());
                ByteArrayOutputStream bos = tileEntity.getPacketTargetData();
                DataOutputStream dos = new DataOutputStream(bos);
                dos.writeShort(tileEntity.getFrequency());
                BlockGuiHandler.sendPacketToServer(bos);
            } catch (NumberFormatException e) {
            } catch (IOException e) {}
        }
        {
            textField.mouseClicked(x, y, b);
            super.mouseClicked(x, y, b);
        }
    }
    
    @Override
    protected void keyTyped(char par1, int par2) 
    {
        if (textField.isFocused()) textField.textboxKeyTyped(par1, par2);
        else super.keyTyped(par1, par2);
    }
    
}
