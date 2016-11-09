package cd4017be.automation.Gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import cd4017be.automation.TileEntity.GraviCond;
import cd4017be.lib.Gui.GuiMachine;
import cd4017be.lib.Gui.TileContainer;
import cd4017be.lib.util.Utils;

public class GuiGraviCond extends GuiMachine {

	private final GraviCond tile;

	public GuiGraviCond(GraviCond tileEntity, EntityPlayer player) {
		super(new TileContainer(tileEntity, player));
		this.tile = tileEntity;
		this.MAIN_TEX = new ResourceLocation("automation", "textures/gui/gravicompr.png");
	}

	@Override
	public void initGui() {
		this.xSize = 176;
		this.ySize = 168;
		super.initGui();
		guiComps.add(new ProgressBar(2, 133, 37, 18, 10, 176, 24, (byte)0));
		guiComps.add(new ProgressBar(3, 26, 16, 70, 12, 176, 0, (byte)4).setTooltip("grav.energy"));
		guiComps.add(new ProgressBar(4, 26, 34, 70, 12, 176, 12, (byte)4).setTooltip("grav.trash"));
		guiComps.add(new Tooltip(5, 133, 37, 18, 10, "grav.need"));
		guiComps.add(new Text(6, 26, 18, 70, 8, "Estor1").center());
		guiComps.add(new Text(7, 26, 36, 70, 8, "percent").center());
		guiComps.add(new Text(8, 26, 52, 70, 16, "grav.matter"));
	}

	@Override
	protected Object getDisplVar(int id) {
		switch(id) {
		case 2: return tile.getProgress();
		case 3: return tile.getEnergy();
		case 4: return tile.getMatter();
		case 5: return Utils.formatNumber((double)tile.need * 1000D, 4, 0);
		case 6: return tile.Estor / 1000F;
		case 7: return tile.getMatter() * 100F;
		case 8: return Utils.formatNumber((double)tile.matter * 1000D, 4, 0);
		default: return null;
		}
	}

}
