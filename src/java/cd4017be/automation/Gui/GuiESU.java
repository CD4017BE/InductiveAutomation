package cd4017be.automation.Gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import cd4017be.automation.TileEntity.ESU;
import cd4017be.lib.BlockGuiHandler;
import cd4017be.lib.Gui.GuiMachine;
import cd4017be.lib.Gui.TileContainer;
import cd4017be.lib.templates.AutomatedTile;

/**
 *
 * @author CD4017BE
 */
public class GuiESU extends GuiMachine {

	protected ESU tile;

	public GuiESU(ESU tileEntity, EntityPlayer player) {
		super(new TileContainer(tileEntity, player));
		this.tile = tileEntity;
		this.MAIN_TEX = new ResourceLocation("automation", "textures/gui/ESU.png");
	}

	@Override
	public void initGui() {
		this.xSize = 176;
		this.ySize = 168;
		super.initGui();
		guiComps.add(new NumberSel(2, 8, 16, 70, 16, "%dV", 0, tile.energy.Umax, 10).setup(30, 0xff404040, 2, true).setTooltip("voltage"));
		guiComps.add(new ProgressBar(3, 8, 52, 160, 16, 0, 240, (byte)4));
		guiComps.add(new ProgressBar(4, 8, 46, 80, 4, 0, 236, (byte)4));
		guiComps.add(new ProgressBar(5, 88, 46, 80, 4, 0, 236, (byte)4));
		guiComps.add(new Text(6, 8, 56, 160, 8, "gui.cd4017be.Estor"));
		guiComps.add(new Tooltip(7, 8, 46, 160, 4, "gui.cd4017be.esu.energyFlow"));
	}

	@Override
	protected Object getDisplVar(int id) {
		switch(id) {
		case 2: return tile.Uref;
		case 3: return tile.getStorage();
		case 4: return tile.power < 0 ? tile.getDiff() : Float.NaN;
		case 5: return tile.power > 0 ? tile.getDiff() : Float.NaN;
		case 6: return new Object[]{tile.Estor / 1000F, tile.getMaxStorage()};
		case 7: return new Object[]{tile.power / 1000F, tile.power / (tile.power > 0 ? (float)Math.sqrt((float)tile.Uref * (float)tile.Uref + tile.power) : -(float)tile.Uref)};
		default: return null;
		}
	}

	@Override
	protected void setDisplVar(int id, Object obj, boolean send) {
		PacketBuffer dos = tile.getPacketTargetData();
		switch(id) {
		case 2: dos.writeByte(AutomatedTile.CmdOffset).writeInt(tile.Uref = (Integer)obj);
		}
		if (send) BlockGuiHandler.sendPacketToServer(dos);
	}

}
