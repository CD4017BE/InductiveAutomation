package cd4017be.automation.Gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import cd4017be.automation.TileEntity.Farm;
import cd4017be.lib.Gui.GuiMachine;
import cd4017be.lib.Gui.TileContainer;

/**
 *
 * @author CD4017BE
 */
public class GuiFarm extends GuiMachine {

	public GuiFarm(Farm tileEntity, EntityPlayer player) {
		super(new TileContainer(tileEntity, player));
		this.MAIN_TEX = new ResourceLocation("automation", "textures/gui/farm.png");
	}

	@Override
	public void initGui() {
		this.xSize = 176;
		this.ySize = 186;
		super.initGui();
		guiComps.add(new GuiComp(2, 79, 52, 16, 16).setTooltip("farm.in"));
		guiComps.add(new GuiComp(3, 81, 70, 16, 16).setTooltip("farm.out"));
	}

}
