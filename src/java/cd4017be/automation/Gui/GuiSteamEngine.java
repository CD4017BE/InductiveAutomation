package cd4017be.automation.Gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import cd4017be.automation.TileEntity.SteamEngine;
import cd4017be.lib.Gui.GuiMachine;
import cd4017be.lib.Gui.TileContainer;

/**
 *
 * @author CD4017BE
 */
public class GuiSteamEngine extends GuiMachine {

	private final SteamEngine tile;

	public GuiSteamEngine(SteamEngine tileEntity, EntityPlayer player) {
		super(new TileContainer(tileEntity, player));
		this.tile = tileEntity;
		this.MAIN_TEX = new ResourceLocation("automation", "textures/gui/steamEngine.png");
	}

	@Override
	public void initGui() {
		this.xSize = 176;
		this.ySize = 168;
		super.initGui();
		guiComps.add(new ProgressBar(4, 57, 16, 8, 52, 176, 0, (byte)1).setTooltip("x*100+0;steamEng.speed"));
		guiComps.add(new Text(5, 120, 24, 0, 24, "steamEng.power").font(0xc06060, 12));
	}

	@Override
	protected Object getDisplVar(int id) {
		switch(id) {
		case 4: return tile.power();
		case 5: return new Object[]{tile.getEnergyOut(), tile.getPower()};
		default: return null;
		}
	}

}
