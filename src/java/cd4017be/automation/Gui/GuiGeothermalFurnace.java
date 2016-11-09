package cd4017be.automation.Gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import cd4017be.automation.TileEntity.GeothermalFurnace;
import cd4017be.lib.Gui.GuiMachine;
import cd4017be.lib.Gui.TileContainer;

/**
 *
 * @author CD4017BE
 */
public class GuiGeothermalFurnace extends GuiMachine {

	private GeothermalFurnace tile;

	public GuiGeothermalFurnace(GeothermalFurnace tileEntity, EntityPlayer player) {
		super(new TileContainer(tileEntity, player));
		this.tile = tileEntity;
		this.MAIN_TEX = new ResourceLocation("automation", "textures/gui/geothermalFurnace.png");
	}

	@Override
	public void initGui() {
		this.xSize = 176;
		this.ySize = 168;
		super.initGui();
		guiComps.add(new ProgressBar(3, 117, 37, 32, 10, 192, 0, (byte)0));
		guiComps.add(new ProgressBar(4, 63, 35, 14, 14, 192, 10, (byte)1));
		guiComps.add(new ProgressBar(5, 84, 16, 8, 52, 184, 0, (byte)1));
		guiComps.add(new ProgressBar(6, 48, 16, 8, 52, 176, 0, (byte)3));
		guiComps.add(new Tooltip(7, 63, 35, 14, 14, "fuelHeat"));
		guiComps.add(new Tooltip(8, 84, 16, 8, 52, "heat"));
		guiComps.add(new Tooltip(9, 48, 16, 8, 52, "lavaHeat"));
	}

	@Override
	protected Object getDisplVar(int id) {
		switch(id) {
		case 3: return tile.getProgress();
		case 4: return tile.getBurn();
		case 5: return tile.getHeat();
		case 6: return tile.getMelt();
		case 7: return tile.burn;
		case 8: return new Object[]{tile.heat, 640};
		case 9: return new Object[]{tile.melt, 2000};
		default: return null;
		}
	}

}
