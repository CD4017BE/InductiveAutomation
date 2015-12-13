/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cd4017be.automation.Gui;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import cd4017be.api.automation.MatterOrbItemHandler;
import cd4017be.automation.TileEntity.MatterInterface;
import cd4017be.lib.BlockGuiHandler;
import cd4017be.lib.TileContainer;
import cd4017be.lib.templates.AutomatedTile;
import cd4017be.lib.templates.GuiMachine;

/**
 *
 * @author CD4017BE
 */
public class GuiMatterInterface extends GuiMachine
{
    private final MatterInterface tileEntity;
    private int amount = 0;
    private int scroll = 0;
    private ItemStack[] list = new ItemStack[0]; 
    
    public GuiMatterInterface(MatterInterface tileEntity, EntityPlayer player)
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
    public void updateScreen() 
    {
        super.updateScreen();
        int l = list.length;
        list = MatterOrbItemHandler.getAllItems(this.tileEntity.inventory.items[0]);
        if (list.length != l) scroll = 0;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mx, int my) 
    {
        super.drawGuiContainerForegroundLayer(mx, my);
        this.drawInfo(8, 16, 16, 16, "\\i", "matterI.rst");
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
        this.drawTexturedModalRect(this.guiLeft + 7, this.guiTop + 15, (tileEntity.netData.ints[0] & 2) == 0 ? 184 : 202, 0, 18, 18);
        this.drawItemConfig(tileEntity, -36, 7);
        for (int i = scroll; i < scroll + 6 && i < list.length; i++) {
            fontRendererObj.drawString(String.format("%d %s", list[i].stackSize, list[i].getDisplayName()), this.guiLeft + 70, this.guiTop + 17 + (i - scroll) * 8, 0x7fffff);
        }
        this.drawStringCentered(amount > 0 ? "" + amount : "All", this.guiLeft + 16, this.guiTop + 52, 0x808040);
        this.drawStringCentered(tileEntity.getInventoryName(), this.guiLeft + this.xSize / 2, this.guiTop + 4, 0x404040);
        this.drawStringCentered("Inventory", this.guiLeft + this.xSize / 2, this.guiTop + 72, 0x404040);
    }
    
    @Override
    public void handleMouseInput() 
    {
        this.scroll -= Mouse.getEventDWheel() / 120;
        if (this.scroll > list.length - 6) this.scroll = list.length - 6;
        if (this.scroll < 0) this.scroll = 0;
        super.handleMouseInput();
    }
    
    @Override
    protected void mouseClicked(int x, int y, int b) 
    {
        byte a = -1;
        this.clickItemConfig(tileEntity, x - this.guiLeft + 36, y - this.guiTop - 7);
        if (this.func_146978_c(26, 34, 16, 16, x, y)) {
            a = 0;
        } else
        if (this.func_146978_c(7, 51, 18, 9, x, y)) {
            a = 1;
        } else
        if (this.func_146978_c(8, 61, 4, 8, x, y))
        {
            amount += b == 0 ? 8 : 512;
        } else
        if (this.func_146978_c(12, 61, 4, 8, x, y))
        {
            amount += b == 0 ? 1 : 64;
        } else
        if (this.func_146978_c(16, 61, 4, 8, x, y))
        {
            amount -= b == 0 ? 1 : 64;
        } else
        if (this.func_146978_c(20, 61, 4, 8, x, y))
        {
            amount -= b == 0 ? 8 : 512;
        } else
        if (this.func_146978_c(7, 15, 18, 18, x, y))
        {
            tileEntity.netData.ints[0] ^= 2;
            a = 2;
        }
        if (amount < 0) amount = 0;
        if (amount > 4096) amount = 4096;
        if (a >= 0)
        {
            try {
            ByteArrayOutputStream bos = tileEntity.getPacketTargetData();
            DataOutputStream dos = new DataOutputStream(bos);
            dos.writeByte(AutomatedTile.CmdOffset + a);
            if (a == 1) dos.writeShort(amount);
            if (a == 2) dos.writeByte(tileEntity.netData.ints[0]);
            BlockGuiHandler.sendPacketToServer(bos);
            } catch (IOException e){}
        }
        super.mouseClicked(x, y, b);
    }
}
