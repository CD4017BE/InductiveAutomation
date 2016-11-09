package cd4017be.automation.Gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import cd4017be.automation.TileEntity.AntimatterTank;
import cd4017be.lib.Gui.GuiMachine;
import cd4017be.lib.Gui.TileContainer;
import cd4017be.lib.util.Utils;

/**
 *
 * @author CD4017BE
 */
public class GuiAntimatterTank extends GuiMachine {

	private AntimatterTank tile;

	public GuiAntimatterTank(AntimatterTank tileEntity, EntityPlayer player) {
		super(new TileContainer(tileEntity, player));
		this.tile = tileEntity;
		this.MAIN_TEX = new ResourceLocation("automation", "textures/gui/antimatterTank.png");
	}

	@Override
	public void initGui() {
		this.xSize = 176;
		this.ySize = 166;
		super.initGui();
		guiComps.add(new ProgressBar(2, 8, 16, 160, 50, 0, 206, (byte)4));
		guiComps.add(new Text(3, 8, 28, 160, 16, "antimTank.am").center());
	}

	@Override
	protected Object getDisplVar(int id) {
		switch(id) {
		case 2: return tile.getStorage();
		case 3: {
			int m = tile.tanks.getAmount(0);
			return new Object[]{m, Utils.formatNumber((double)m * 90000.0, 6, 0)};
		} default: return null;
		}
	}

}
