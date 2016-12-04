package cd4017be.automation.Gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import cd4017be.automation.TileEntity.CEU;
import cd4017be.lib.BlockGuiHandler;
import cd4017be.lib.Gui.GuiMachine;
import cd4017be.lib.Gui.TileContainer;
import cd4017be.lib.templates.AutomatedTile;

public class GuiCEU extends GuiMachine {

	private final CEU tile;

	public GuiCEU(CEU tile, EntityPlayer player) {
		super(new TileContainer(tile, player));
		this.tile = tile;
		this.MAIN_TEX = new ResourceLocation("automation", "textures/gui/CEU.png");
	}

	@Override
	public void initGui() {
		this.xSize = 176;
		this.ySize = 176;
		super.initGui();
		guiComps.add(new TextField(2, 8, 16, 70, 8, 6).setTooltip("voltage"));
		guiComps.add(new ProgressBar(3, 8, 46, 80, 4, 0, 236, (byte)4));
		guiComps.add(new ProgressBar(4, 88, 46, 80, 4, 80, 236, (byte)4));
		guiComps.add(new Text(5, 8, 52, 160, 24, "CEU.power").center());
		guiComps.add(new Tooltip(6, 8, 46, 160, 4, "esu.energyFlow"));
		guiComps.add(new Button(7, 8, 52, 160, 24, -1).setTooltip("CEU.reset"));
	}

	@Override
	protected Object getDisplVar(int id) {
		switch(id) {
		case 2: return "" + tile.Uref;
		case 3: return tile.power < 0 ? tile.getDiff() : Float.NaN;
		case 4: return tile.power > 0 ? tile.getDiff() : Float.NaN;
		case 5: return new Object[]{tile.Pmin / 1000F, tile.Pmax / 1000F, tile.power / 1000F, tile.Estor / 1000F / (float)tile.timer, tile.Estor / 1000F};
		case 6: return new Object[]{tile.power / 1000F, tile.power / (tile.power > 0 ? (float)Math.sqrt((float)tile.Uref * (float)tile.Uref + tile.power) : -(float)tile.Uref)};
		default: return null;
		}
	}

	@Override
	protected void setDisplVar(int id, Object obj, boolean send) {
		PacketBuffer dos = tile.getPacketTargetData();
		switch(id) {
		case 2: try {dos.writeByte(AutomatedTile.CmdOffset).writeInt(tile.Uref = Integer.parseInt((String)obj));} catch(NumberFormatException e) {return;} break;
		case 7: dos.writeByte(AutomatedTile.CmdOffset + 1); break;
		}
		if (send) BlockGuiHandler.sendPacketToServer(dos);
	}

}
