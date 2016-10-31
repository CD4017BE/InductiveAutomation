package cd4017be.automation.Gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import cd4017be.automation.TileEntity.Tank;
import cd4017be.lib.Gui.GuiMachine;
import cd4017be.lib.Gui.TileContainer;

/**
 *
 * @author CD4017BE
 */
public class GuiTank extends GuiMachine {

	public GuiTank(Tank tileEntity, EntityPlayer player) {
		super (new TileContainer(tileEntity, player));
		this.MAIN_TEX = new ResourceLocation("automation", "textures/gui/tank.png");
		this.xSize = 226;
		this.ySize = 98;
	}

}
