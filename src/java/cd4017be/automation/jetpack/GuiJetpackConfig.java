/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cd4017be.automation.jetpack;

import java.io.IOException;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;

import org.lwjgl.input.Keyboard;

import cd4017be.automation.Gui.GuiSlider;
import cd4017be.automation.jetpack.JetPackConfig.Mode;

/**
 *
 * @author CD4017BE
 */
public class GuiJetpackConfig extends GuiScreen
{
    private GuiTextField nameTF;
    private int ofsX;
    private int ofsY;
    private final int sizeX = 250;
    private final int sizeY = 200;
    private int selMode = 0;
    
    public GuiJetpackConfig()
    {
        
    }

    @Override
    public void onGuiClosed() 
    {
        JetPackConfig.saveData();
    }

    @Override
    public void initGui() 
    {
        ofsX = (this.width - sizeX) / 2;
        ofsY = (this.height - sizeY) / 2;
        this.buttonList.add(new GuiButton(1, ofsX, ofsY + sizeY + 10, 80, 20, "Add Mode"));
        this.buttonList.add(new GuiButton(0, width / 2 - 40, ofsY + sizeY + 10, 80, 20, "Done"));
        this.buttonList.add(new GuiButton(2, width - ofsX - 80, ofsY + sizeY + 10, 80, 20, "Delete"));
        //options
        this.nameTF = new GuiTextField(-1, fontRendererObj, ofsX + 100, ofsY, 80, 20);
        this.buttonList.add(new GuiButton(3, ofsX + 190, ofsY, 60, 20, "Active"));
        //key-Control
        this.buttonList.add(new GuiSlider(4, ofsX + 100, ofsY + 60, 150, 20, "Vertical= %.1f : 1", 0.2F, 5F));
        this.buttonList.add(new GuiSlider(5, ofsX + 100, ofsY + 90, 150, 20, "Acceleration= %.2f", 0F, 1F));
        //mouse-Control
        this.buttonList.add(new GuiSlider(6, ofsX + 100, ofsY + 150, 150, 20, "Angle Offset= %+.0f", -180F, 180F));
        this.buttonList.add(new GuiSlider(7, ofsX + 100, ofsY + 180, 150, 20, "Angle Factor= %.2f", 0F, 2F));
        
        updateModes();
    }

    @Override
    public void updateScreen() 
    {
        nameTF.updateCursorCounter();
    }

    @Override
    protected void mouseClicked(int x, int y, int b) throws IOException
    {
        nameTF.mouseClicked(x, y, b);
        Mode mode = this.getMode();
        mode.name = nameTF.getText();
        mode.verticalComp = ((GuiSlider)this.buttonList.get(4)).getValue();
        mode.moveStrength = ((GuiSlider)this.buttonList.get(5)).getValue();
        mode.vertAngleOffset = ((GuiSlider)this.buttonList.get(6)).getValue();
        mode.vertAngleFaktor = ((GuiSlider)this.buttonList.get(7)).getValue();
        if (x > ofsX && y > ofsY && x < ofsX + 80) {
            selMode = (y - ofsY) / 8;
            updateModes();
        }
        super.mouseClicked(x, y, b);
    }

    @Override
    protected void keyTyped(char c, int k) throws IOException
    {
        super.keyTyped(c, k);
        nameTF.textboxKeyTyped(c, k);
        if (c == Keyboard.KEY_ESCAPE) this.mc.displayGuiScreen(null);
    }

    @Override
    protected void actionPerformed(GuiButton button) 
    {
        Mode mode = this.getMode();
        if (button.id == 0) {
            this.mc.displayGuiScreen(null);
        } else if (button.id == 1) {
            JetPackConfig.allModes.add(new Mode("-", 1, 0, 90, 0, false));
            selMode = JetPackConfig.allModes.size() - 1;
            this.updateModes();
        } else if (button.id == 2) {
            JetPackConfig.allModes.remove(selMode);
            this.updateModes();
        } else if (button.id == 3) {
            mode.active = !mode.active;
            button.displayString = mode.active ? "Active" : "Disabled";
        } else if (button.id == 4 && button instanceof GuiSlider) {
            //mode.verticalComp = ((GuiSlider)button).getValue();
        } else if (button.id == 5 && button instanceof GuiSlider) {
            //mode.moveStrength = ((GuiSlider)button).getValue();
        } else if (button.id == 6 && button instanceof GuiSlider) {
            //mode.vertAngleOffset = ((GuiSlider)button).getValue();
        } else if (button.id == 7 && button instanceof GuiSlider) {
            //mode.vertAngleFaktor = ((GuiSlider)button).getValue();
        }
    }
    
    private void updateModes()
    {
        Mode mode = this.getMode();
        nameTF.setText(mode.name);
        ((GuiButton)this.buttonList.get(3)).displayString = mode.active ? "Active" : "Disabled";
        ((GuiSlider)this.buttonList.get(4)).setValue(mode.verticalComp);
        ((GuiSlider)this.buttonList.get(5)).setValue(mode.moveStrength);
        ((GuiSlider)this.buttonList.get(6)).setValue(mode.vertAngleOffset);
        ((GuiSlider)this.buttonList.get(7)).setValue(mode.vertAngleFaktor);
    }
    
    private Mode getMode()
    {
        if (selMode < 0) selMode = 0;
        if (selMode >= JetPackConfig.allModes.size()) selMode = JetPackConfig.allModes.size() - 1;
        return selMode >= 0 ? JetPackConfig.allModes.get(selMode) : new Mode();
    }

    @Override
    public void drawScreen(int par1, int par2, float par3) 
    {
        this.drawDefaultBackground();
        this.drawCenteredString(fontRendererObj, "Edit Jetpack Modes", this.width / 2, 15, 0xffffff);
        this.drawList(this.ofsX, this.ofsY);
        this.nameTF.drawTextBox();
        this.drawString(fontRendererObj, "Key-Control:", ofsX + 100, ofsY + 36, 0xc0c080);
        this.drawString(fontRendererObj, "View-Control:", ofsX + 100, ofsY + 126, 0xc0c080);
        super.drawScreen(par1, par2, par3);
    }
    
    private void drawList(int x, int y)
    {
        int n = JetPackConfig.allModes.size();
        this.drawGradientRect(x, y, x + 80, y + n * 8, 0x80303030, 0x80202020);
        this.drawHorizontalLine(x, x + 80, y, 0xff400080);
        this.drawHorizontalLine(x, x + 80, y + n * 8, 0xff400080);
        this.drawVerticalLine(x, y, y + n * 8, 0xff400080);
        this.drawVerticalLine(x + 80, y, y + n * 8, 0xff400080);
        this.drawGradientRect(x + 1, y + selMode * 8 + 1, x + 80, y + selMode * 8 + 8, 0xff006090, 0xff004080);
        for (int i = 0; i < n; i++) {
            Mode m = JetPackConfig.allModes.get(i);
            fontRendererObj.drawString(m.name, x + 1, y + i * 8 + 1, m.active ? 0x40ff20 : 0xe0e0e0);
        }
    }
    
}
