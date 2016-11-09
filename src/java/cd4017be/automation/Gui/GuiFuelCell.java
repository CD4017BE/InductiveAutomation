package cd4017be.automation.Gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import cd4017be.automation.Config;
import cd4017be.automation.TileEntity.FuelCell;
import cd4017be.lib.BlockGuiHandler;
import cd4017be.lib.Gui.GuiMachine;
import cd4017be.lib.Gui.TileContainer;
import cd4017be.lib.templates.AutomatedTile;

public class GuiFuelCell extends GuiMachine {

	protected FuelCell tile;

	public GuiFuelCell(FuelCell tileEntity, EntityPlayer player) {
		super(new TileContainer(tileEntity, player));
		this.tile = tileEntity;
		this.MAIN_TEX = new ResourceLocation("automation", "textures/gui/fuelCell.png");
	}

	@Override
	public void initGui() {
		this.xSize = 176;
		this.ySize = 168;
		super.initGui();
		guiComps.add(new NumberSel(5, 98, 16, 70, 16, "%dV", 0, Config.Umax[2], 10).setup(30, 0xff404040, 2, true).setTooltip("voltage"));
		guiComps.add(new ProgressBar(6, 84, 16, 8, 52, 176, 0, (byte)1));
		guiComps.add(new ProgressBar(7, 98, 52, 70, 16, 184, 0, (byte)4));
		guiComps.add(new Text(8, 98, 56, 70, 16, "Estor1").center());
		guiComps.add(new Text(9, 98, 38, 70, 16, "antimFab.power"));
	}

	@Override
	protected Object getDisplVar(int id) {
		switch(id) {
		case 5: return tile.Uref;
		case 6: return tile.getPower();
		case 7: return tile.storage();
		case 8: return tile.Estor / 1000F;
		case 9: return (float)tile.power;
		default: return null;
		}
	}

	@Override
	protected void setDisplVar(int id, Object obj, boolean send) {
		PacketBuffer dos = tile.getPacketTargetData();
		switch(id) {
		case 5: dos.writeByte(AutomatedTile.CmdOffset).writeShort(tile.Uref = (Integer)obj);
		}
		if (send) BlockGuiHandler.sendPacketToServer(dos);
	}

}
