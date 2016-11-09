package cd4017be.automation.Gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import cd4017be.automation.Config;
import cd4017be.automation.TileEntity.AntimatterAnihilator;
import cd4017be.lib.BlockGuiHandler;
import cd4017be.lib.Gui.GuiMachine;
import cd4017be.lib.Gui.TileContainer;
import cd4017be.lib.templates.AutomatedTile;

/**
 *
 * @author CD4017BE
 */
public class GuiAntimatterAnihilator extends GuiMachine {

	private final AntimatterAnihilator tile;

	public GuiAntimatterAnihilator(AntimatterAnihilator tileEntity, EntityPlayer player) {
		super(new TileContainer(tileEntity, player));
		this.tile = tileEntity;
		this.MAIN_TEX = new ResourceLocation("automation", "textures/gui/antimatterAnihilator.png");
	}

	@Override
	public void initGui() {
		this.xSize = 176;
		this.ySize = 168;
		super.initGui();
		guiComps.add(new NumberSel(5, 98, 16, 70, 16, "%dV", 0, Config.Umax[2], 10).setup(30, 0xff404040, 2, true).setTooltip("voltage"));
		guiComps.add(new ProgressBar(6, 48, 16, 8, 52, 176, 0, (byte)1).setTooltip("x*100+0;antimAn.heat"));
		guiComps.add(new ProgressBar(7, 98, 52, 70, 16, 170, 0, (byte)4));
		guiComps.add(new Text(8, 98, 56, 70, 16, "Estor1").center());
		guiComps.add(new Text(9, 98, 34, 70, 16, "antimAn.power"));
	}

	@Override
	protected Object getDisplVar(int id) {
		switch(id) {
		case 5: return tile.Uref;
		case 6: return tile.getHeat();
		case 7: return tile.storage();
		case 8: return tile.Estor / 1000F;
		case 9: return new Object[]{(float)tile.power, tile.power * AntimatterAnihilator.AMEnergy / 1000};
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
