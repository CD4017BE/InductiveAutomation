package cd4017be.automation.Gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import cd4017be.automation.TileEntity.AlgaePool;
import cd4017be.lib.Gui.GuiMachine;
import cd4017be.lib.Gui.TileContainer;
import cd4017be.lib.util.Utils;

/**
 *
 * @author CD4017BE
 */
public class GuiAlgaePool extends GuiMachine {

	private final AlgaePool tile;

	public GuiAlgaePool(AlgaePool tileEntity, EntityPlayer player) {
		super(new TileContainer(tileEntity, player));
		this.tile = tileEntity;
		this.MAIN_TEX = new ResourceLocation("automation", "textures/gui/algaePool.png");
	}

	@Override
	public void initGui() {
		this.xSize = 176;
		this.ySize = 168;
		super.initGui();
		guiComps.add(new ProgressBar(4, 62, 16, 16, 52, 210, 0, (byte)1).setTooltip("x*100+0;algae.nutr"));
		guiComps.add(new ProgressBar(5, 80, 16, 34, 52, 176, 0, (byte)1));
		guiComps.add(new Tooltip(6, 80, 16, 34, 52, "algae.algae"));
	}

	@Override
	protected Object getDisplVar(int id) {
		switch(id) {
		case 4: return tile.getNutrients();
		case 5: return tile.getAlgae();
		case 6: return new Object[]{Utils.formatNumber(tile.algae / 1000F, 3), Utils.formatNumber((float)tile.cap / 1000F, 3)};
		default: return null;
		}
	}

}
