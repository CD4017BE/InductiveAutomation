package cd4017be.automation.Gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import cd4017be.api.automation.PipeEnergy;
import cd4017be.automation.Config;
import cd4017be.automation.TileEntity.AdvancedFurnace;
import cd4017be.lib.BlockGuiHandler;
import cd4017be.lib.Gui.GuiMachine;
import cd4017be.lib.Gui.TileContainer;
import cd4017be.lib.templates.AutomatedTile;

/**
 *
 * @author CD4017BE
 */
public class GuiAdvancedFurnace extends GuiMachine {

	private final AdvancedFurnace tile;

	public GuiAdvancedFurnace(AdvancedFurnace tileEntity, EntityPlayer player) {
		super(new TileContainer(tileEntity, player));
		this.tile = tileEntity;
		this.MAIN_TEX = new ResourceLocation("automation", "textures/gui/advancedFurnace.png");
	}

	@Override
	public void initGui() {
		this.xSize = 176;
		this.ySize = 168;
		super.initGui();
		guiComps.add(new NumberSel(5, 8, 16, 16, 52, "%d", Config.Rmin, 1000, 10).setTooltip("resistor"));
		guiComps.add(new ProgressBar(6, 26, 16, 8, 52, 176, 0, (byte)1));
		guiComps.add(new Tooltip(7, 26, 16, 8, 52, "energyFlow"));
		guiComps.add(new Button(8, 100, 18, 12, 12, -1).setTooltip("advancedFurnace.swap"));
		guiComps.add(new ProgressBar(9, 97, 37, 18, 10, 184, 0, (byte)0));
	}

	@Override
	protected Object getDisplVar(int id) {
		switch(id) {
		case 5: return tile.Rw;
		case 6: return tile.getPower();
		case 7: return PipeEnergy.getEnergyInfo(tile.Uc, 0, tile.Rw);
		case 9: return tile.getProgress();
		default: return null;
		}
	}

	@Override
	protected void setDisplVar(int id, Object obj, boolean send) {
		PacketBuffer dos = tile.getPacketTargetData();
		switch(id) {
		case 5: dos.writeByte(AutomatedTile.CmdOffset).writeInt(tile.Rw = (Integer)obj); break;
		case 8: dos.writeByte(AutomatedTile.CmdOffset + 1); break;
		}
		if (send) BlockGuiHandler.sendPacketToServer(dos);
	}

}
