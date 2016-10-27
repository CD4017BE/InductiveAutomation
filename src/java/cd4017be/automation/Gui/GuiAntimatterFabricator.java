package cd4017be.automation.Gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import cd4017be.automation.Config;
import cd4017be.automation.TileEntity.AntimatterFabricator;
import cd4017be.lib.BlockGuiHandler;
import cd4017be.lib.Gui.GuiMachine;
import cd4017be.lib.Gui.TileContainer;
import cd4017be.lib.templates.AutomatedTile;

/**
 *
 * @author CD4017BE
 */
public class GuiAntimatterFabricator extends GuiMachine {

	private AntimatterFabricator tile;

	public GuiAntimatterFabricator(AntimatterFabricator tileEntity, EntityPlayer player) {
		super(new TileContainer(tileEntity, player));
		this.tile = tileEntity;
		this.MAIN_TEX = new ResourceLocation("automation", "textures/gui/antimatterFabricator.png");
	}

	@Override
	public void initGui() {
		this.xSize = 176;
		this.ySize = 168;
		super.initGui();
		guiComps.add(new NumberSel(5, 98, 16, 70, 16, "%dV", 0, Config.Umax[2], 10).setup(8, 0xff404040, 2, true).setTooltip("voltage"));
		guiComps.add(new Button(6, 52, 51, 18, 18, 0).setTooltip("rstCtr"));
		guiComps.add(new ProgressBar(7, 53, 38, 70, 8, 184, 0, (byte)0));
		guiComps.add(new Tooltip(8, 53, 38, 70, 8, "progress"));
		guiComps.add(new Text(9, 70, 56, 54, 8, "gui.cd4017be.antimFab.power").center());
	}

	@Override
	protected Object getDisplVar(int id) {
		switch(id) {
		case 5: return tile.Uref;
		case 6: return tile.rs;
		case 7: return tile.getPower();
		case 8: return new Object[]{(int)(tile.Estor / 1000F), AntimatterFabricator.AMEnergy / 1000};
		case 9: return String.format("%." + (tile.power >= 100 ? "0" : "1") + "f");
		default: return null;
		}
	}

	@Override
	protected void setDisplVar(int id, Object obj, boolean send) {
		PacketBuffer dos = tile.getPacketTargetData();
		switch(id) {
		case 5: dos.writeByte(AutomatedTile.CmdOffset).writeInt(tile.Uref = (Integer)obj);
		case 6: dos.writeByte(AutomatedTile.CmdOffset).writeByte(tile.rs = (tile.rs + ((Integer)obj == 0 ? 1 : 2)) % 4);
		}
		if (send) BlockGuiHandler.sendPacketToServer(dos);
	}

}
