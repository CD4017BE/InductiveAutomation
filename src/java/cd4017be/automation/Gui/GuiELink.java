package cd4017be.automation.Gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import cd4017be.automation.TileEntity.ELink;
import cd4017be.lib.BlockGuiHandler;
import cd4017be.lib.Gui.GuiMachine;
import cd4017be.lib.Gui.TileContainer;
import cd4017be.lib.templates.AutomatedTile;

/**
 *
 * @author CD4017BE
 */
public class GuiELink extends GuiMachine {

	private final ELink tile;

	public GuiELink(ELink tileEntity, EntityPlayer player) {
		super(new TileContainer(tileEntity, player));
		this.tile = tileEntity;
		this.MAIN_TEX = new ResourceLocation("automation", "textures/gui/ELink.png");
	}

	@Override
	public void initGui() {
		this.xSize = 176;
		this.ySize = 168;
		super.initGui();
		guiComps.add(new Button(1, 7, 51, 18, 18, 0).texture(176, 0).setTooltip("rstCtr"));
		guiComps.add(new Button(2, 25, 51, 18, 18, 0).texture(194, 0).setTooltip("rstCtr"));
		guiComps.add(new NumberSel(3, 98, 52, 70, 16, "%dV", 0, tile.energy.Umax, 10).setup(30, 0xff0000ff, 2, true).setTooltip("link.ref0"));
		guiComps.add(new NumberSel(4, 98, 34, 70, 16, "%dV", 0, tile.energy.Umax, 10).setup(30, 0xffff0000, 2, true).setTooltip("link.ref1"));
		guiComps.add(new ProgressBar(5, 8, 16, 160, 8, 0, 240, (byte)4).setTooltip("link.link"));
		guiComps.add(new ProgressBar(6, 8, 24, 160, 8, 0, 248, (byte)0).setTooltip("link.int"));
		guiComps.add(new Text(7, 8, 16, 160, 16, "link.stor").center());
		guiComps.add(new Text(8, 34, 38, 0, 8, "Power").center());
	}

	@Override
	protected Object getDisplVar(int id) {
		switch(id) {
		case 1: return tile.rstCtr & 3;
		case 2: return tile.rstCtr >> 1 & 1;
		case 3: return tile.Umin;
		case 4: return tile.Umax;
		case 5: return tile.getStorage();
		case 6: return tile.getVoltage();
		case 7: return new Object[]{tile.Estor / 1000F, tile.Ecap / 1000F, tile.Uref};
		case 8: return tile.power / 1000F;
		default: return null;
		}
	}

	@Override
	protected void setDisplVar(int id, Object obj, boolean send) {
		PacketBuffer dos = tile.getPacketTargetData();
		switch(id) {
		case 1: dos.writeByte(AutomatedTile.CmdOffset + 2).writeByte(tile.rstCtr ^= 1); break;
		case 2: dos.writeByte(AutomatedTile.CmdOffset + 2).writeByte(tile.rstCtr ^= 2); break;
		case 3: dos.writeByte(AutomatedTile.CmdOffset + 0).writeShort(tile.Umin = (Integer)obj); break;
		case 4: dos.writeByte(AutomatedTile.CmdOffset + 1).writeShort(tile.Umax = (Integer)obj); break;
		}
		if (send) BlockGuiHandler.sendPacketToServer(dos);
	}

}
