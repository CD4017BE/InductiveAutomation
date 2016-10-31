package cd4017be.automation.Gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import cd4017be.automation.Config;
import cd4017be.automation.TileEntity.SteamBoiler;
import cd4017be.lib.BlockGuiHandler;
import cd4017be.lib.Gui.GuiMachine;
import cd4017be.lib.Gui.TileContainer;
import cd4017be.lib.templates.AutomatedTile;

/**
 *
 * @author CD4017BE
 */
public class GuiSteamBoiler extends GuiMachine {

	private final SteamBoiler tile;

	public GuiSteamBoiler(SteamBoiler tileEntity, EntityPlayer player) {
		super(new TileContainer(tileEntity, player));
		this.tile = tileEntity;
		this.MAIN_TEX = new ResourceLocation("automation", "textures/gui/steamBoiler.png");
	}

	@Override
	public void initGui() {
		this.xSize = 176;
		this.ySize = 168;
		super.initGui();
		guiComps.add(new ProgressBar(4, 107, 59-34, 16, 34, 184, 0, (byte)1));
		guiComps.add(new ProgressBar(5, 52, 68-52, 8, 52, 176, 0, (byte)1));
		guiComps.add(new ProgressBar(6, 27, 49-14, 14, 14, 184, 48-14, (byte)1));
		guiComps.add(new ProgressBar(7, 26, 68-16, 8, 16, 176, 68-16, (byte)1).setTooltip("x*8+0;boiler.burnUp"));
		guiComps.add(new Tooltip(8, 52, 68-52, 8, 52, "heat"));
		guiComps.add(new Button(9, 34, 60, 8, 8, -1));
		guiComps.add(new Button(10, 34, 52, 8, 8, -1));
	}

	@Override
	protected Object getDisplVar(int id) {
		switch(id) {
		case 4: return tile.getCook();
		case 5: return tile.getTemp();
		case 6: return tile.getBurn();
		case 7: return (float)tile.blow / 8F;
		case 8: return new Object[]{tile.temp, Config.maxK_steamBoiler};
		default: return null;
		}
	}

	@Override
	protected void setDisplVar(int id, Object obj, boolean send) {
		if (!send || id < 9) return;
		PacketBuffer dos = tile.getPacketTargetData();
		dos.writeByte(AutomatedTile.CmdOffset + id - 9);
		BlockGuiHandler.sendPacketToServer(dos);
	}

}
