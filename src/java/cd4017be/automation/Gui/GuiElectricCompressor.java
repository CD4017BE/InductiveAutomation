package cd4017be.automation.Gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import cd4017be.api.automation.PipeEnergy;
import cd4017be.automation.Config;
import cd4017be.automation.TileEntity.ElectricCompressor;
import cd4017be.lib.BlockGuiHandler;
import cd4017be.lib.Gui.GuiMachine;
import cd4017be.lib.Gui.TileContainer;
import cd4017be.lib.templates.AutomatedTile;

/**
 *
 * @author CD4017BE
 */
public class GuiElectricCompressor extends GuiMachine {

	private final ElectricCompressor tile;

	public GuiElectricCompressor(ElectricCompressor tileEntity, EntityPlayer player) {
		super(new TileContainer(tileEntity, player));
		this.tile = tileEntity;
		this.MAIN_TEX = new ResourceLocation("automation", "textures/gui/electricCompressor.png");
	}

	@Override
	public void initGui() {
		this.xSize = 176;
		this.ySize = 168;
		super.initGui();
		guiComps.add(new NumberSel(5, 8, 16, 16, 52, "%d", Config.Rmin, 1000, 5).setTooltip("resistor"));
		guiComps.add(new ProgressBar(6, 26, 16, 8, 52, 176, 0, (byte)1));
		guiComps.add(new Tooltip(7, 26, 16, 8, 52, "energyFlow"));
		guiComps.add(new ProgressBar(8, 99, 37, 32, 10, 184, 0, (byte)0));
	}

	@Override
	protected Object getDisplVar(int id) {
		switch(id) {
		case 5: return tile.Rw;
		case 6: return tile.getPower();
		case 7: return PipeEnergy.getEnergyInfo(tile.Uc, 0, tile.Rw);
		case 8: return tile.getProgress();
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

}
