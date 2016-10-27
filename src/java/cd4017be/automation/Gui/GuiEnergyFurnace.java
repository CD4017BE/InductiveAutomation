package cd4017be.automation.Gui;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;

import org.lwjgl.opengl.GL11;

import cd4017be.api.automation.PipeEnergy;
import cd4017be.automation.Config;
import cd4017be.automation.TileEntity.EnergyFurnace;
import cd4017be.lib.BlockGuiHandler;
import cd4017be.lib.Gui.GuiMachine;
import cd4017be.lib.Gui.TileContainer;
import cd4017be.lib.Gui.GuiMachine.NumberSel;
import cd4017be.lib.Gui.GuiMachine.ProgressBar;
import cd4017be.lib.Gui.GuiMachine.Tooltip;
import cd4017be.lib.templates.AutomatedTile;

/**
 *
 * @author CD4017BE
 */
public class GuiEnergyFurnace extends GuiMachine
{
	private final EnergyFurnace tile;
	
	public GuiEnergyFurnace(EnergyFurnace tileEntity, EntityPlayer player)
	{
		super(new TileContainer(tileEntity, player));
		this.tile = tileEntity;
	}

	@Override
	public void initGui() 
	{
		this.xSize = 176;
		this.ySize = 168;
		super.initGui();
		guiComps.add(new NumberSel(5, 8, 16, 16, 52, "%d", Config.Rmin, 1000, 5).setTooltip("resistor"));
		guiComps.add(new ProgressBar(6, 26, 16, 8, 52, 176, 0, (byte)1));
		guiComps.add(new Tooltip(7, 26, 16, 8, 52, "energyFlow"));
	}

	@Override
	protected Object getDisplVar(int id) {
		switch(id) {
		case 5: return tile.Rw;
		case 6: return tile.getPower();
		case 7: return PipeEnergy.getEnergyInfo(tile.Uc, 0, tile.Rw);
		default: return null;
		}
	}

	@Override
	protected void setDisplVar(int id, Object obj, boolean send) {
		PacketBuffer dos = tile.getPacketTargetData();
		switch(id) {
		case 5: dos.writeByte(AutomatedTile.CmdOffset).writeInt(tile.Rw = (Integer)obj);
		}
		if (send) BlockGuiHandler.sendPacketToServer(dos);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) 
	{
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.renderEngine.bindTexture(new ResourceLocation("automation", "textures/gui/energyFurnace.png"));
		this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
		int n = tile.getProgressScaled(32);
		this.drawTexturedModalRect(this.guiLeft + 81, this.guiTop + 37, 184, 0, n, 10);
		n = tile.getPowerScaled(52);
		this.drawTexturedModalRect(this.guiLeft + 26, this.guiTop + 68 - n, 176, 52 - n, 8, n);
		this.drawStringCentered(tile.getName(), this.guiLeft + this.xSize / 2, this.guiTop + 4, 0x404040);
		this.drawStringCentered(I18n.translateToLocal("container.inventory"), this.guiLeft + this.xSize / 2, this.guiTop + 72, 0x404040);
		this.drawStringCentered("" + tile.Rw, this.guiLeft + 16, this.guiTop + 38, 0x404040);
		super.drawGuiContainerBackgroundLayer(var1, var2, var3);
	}

}
