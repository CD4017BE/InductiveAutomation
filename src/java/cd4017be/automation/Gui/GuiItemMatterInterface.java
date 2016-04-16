/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cd4017be.automation.Gui;

import java.io.IOException;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.I18n;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import cd4017be.api.automation.MatterOrbItemHandler;
import cd4017be.lib.BlockGuiHandler;
import cd4017be.lib.templates.GuiMachine;

/**
 *
 * @author CD4017BE
 */
public class GuiItemMatterInterface extends GuiMachine
{
    private final ContainerItemMatterInterface container;
    private int amount = 0;
    private int scroll = 0;
    private ItemStack[] list = new ItemStack[0]; 
    
    public GuiItemMatterInterface(ContainerItemMatterInterface container)
    {
        super(container);
        this.container = container;
    }
    
    @Override
    public void initGui() 
    {
        this.xSize = 176;
        this.ySize = 168;
        super.initGui();
    }

    @Override
    public void updateScreen() 
    {
        super.updateScreen();
        int l = list.length;
        list = MatterOrbItemHandler.getAllItems(this.container.inventory.inventory[0]);
        if (list.length != l) scroll = 0;
        container.inventory.update();
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mx, int my) 
    {
        super.drawGuiContainerForegroundLayer(mx, my);
        this.drawInfo(26, 34, 16, 16, "\\i", "matterI.next");
        this.drawInfo(7, 51, 18, 9, "\\i", "matterI.put");
    }
    
    @Override
    protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) 
    {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.renderEngine.bindTexture(new ResourceLocation("automation", "textures/gui/matterInterface.png"));
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
        int n = list.length <= 6 ? 0 : scroll * 40 / (list.length - 6);
        this.drawTexturedModalRect(this.guiLeft + 160, this.guiTop + 16 + n, 176, 0, 8, 12);
        for (int i = scroll; i < scroll + 6 && i < list.length; i++) {
            fontRendererObj.drawString(String.format("%d %s", list[i].stackSize, list[i].getDisplayName()), this.guiLeft + 70, this.guiTop + 17 + (i - scroll) * 8, 0x7fffff);
        }
        this.drawStringCentered(amount > 0 ? "" + amount : "ALL", this.guiLeft + 16, this.guiTop + 52, 0x808040);
        this.drawStringCentered(this.container.inventory.getName(), this.guiLeft + this.xSize / 2, this.guiTop + 4, 0x404040);
        this.drawStringCentered(I18n.translateToLocal("container.inventory"), this.guiLeft + this.xSize / 2, this.guiTop + 72, 0x404040);
    }
    
    @Override
    public void handleMouseInput() throws IOException 
    {
        this.scroll -= Mouse.getDWheel() / 120;
        if (this.scroll > list.length - 6) this.scroll = list.length - 6;
        if (this.scroll < 0) this.scroll = 0;
        super.handleMouseInput();
    }
    
    @Override
    protected void mouseClicked(int x, int y, int b) throws IOException 
    {
        byte a = -1;
        if (this.isPointInRegion(26, 34, 16, 16, x, y)) {
            a = 0;
        } else
        if (this.isPointInRegion(7, 51, 18, 9, x, y)) {
            a = 1;
        } else
        if (this.isPointInRegion(8, 61, 4, 8, x, y))
        {
            amount -= b == 0 ? 8 : 512;
        } else
        if (this.isPointInRegion(12, 61, 4, 8, x, y))
        {
            amount -= b == 0 ? 1 : 64;
        } else
        if (this.isPointInRegion(16, 61, 4, 8, x, y))
        {
            amount += b == 0 ? 1 : 64;
        } else
        if (this.isPointInRegion(20, 61, 4, 8, x, y))
        {
            amount += b == 0 ? 8 : 512;
        }
        if (amount < 0) amount = 0;
        if (amount > 4096) amount = 4096;
        if (a >= 0)
        {
            PacketBuffer dos = BlockGuiHandler.getPacketTargetData(new BlockPos(0, -1, 0));
            dos.writeByte(a);
            if (a == 1) dos.writeShort(amount);
            BlockGuiHandler.sendPacketToServer(dos);
        }
        super.mouseClicked(x, y, b);
    }
}
