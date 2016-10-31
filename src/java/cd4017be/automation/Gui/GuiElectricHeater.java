package cd4017be.automation.Gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import cd4017be.api.automation.PipeEnergy;
import cd4017be.automation.Config;
import cd4017be.automation.TileEntity.HeatingCoil;
import cd4017be.lib.BlockGuiHandler;
import cd4017be.lib.Gui.GuiMachine;
import cd4017be.lib.Gui.TileContainer;
import cd4017be.lib.templates.AutomatedTile;

public class GuiElectricHeater extends GuiMachine {

	private final HeatingCoil tile;

	public GuiElectricHeater(HeatingCoil tile, EntityPlayer player) {
		super(new TileContainer(tile, player));
		this.tile = tile;
		this.MAIN_TEX = new ResourceLocation("automation", "textures/gui/heater.png");
		this.bgTexY = 160;
	}

	@Override
	public void initGui() {
		this.xSize = 132;
		this.ySize = 76;
		super.initGui();
		guiComps.add(new NumberSel(1, 8, 16, 16, 52, "%d", Config.Rmin, 1000, 5).setTooltip("resistor"));
		guiComps.add(new ProgressBar(2, 26, 16, 8, 52, 176, 0, (byte)1));
		guiComps.add(new Tooltip(3, 26, 16, 8, 52, "energyFlow"));
		guiComps.add(new Slider(4, 45, 24, 70, 198, 8, 4, 12, true));
		guiComps.add(new ProgressBar(5, 45, 16, 70, 8, 176, 0, (byte)0));
		guiComps.add(new Button(6, 43, 51, 18, 18, 0).texture(212, 8));
		guiComps.add(new Button(7, 61, 51, 18, 18, 0).texture(176, 26));
		guiComps.add(new Text(8, 80, 38, 0, 8, "temp").center().font(0x808040, 8));
	}

	@Override
	protected Object getDisplVar(int id) {
		switch(id) {
		case 1: return tile.Rw;
		case 2: return tile.getPower();
		case 3: return PipeEnergy.getEnergyInfo(tile.Uc, 0, tile.Rw);
		case 4: return (tile.Tref - 300F) / 2500F;
		case 5: return (tile.temp - 300F) / 2500F;
		case 6: return tile.rstCtr >> 1 & 1;
		case 7: return tile.rstCtr & 3;
		case 8: return new Object[]{tile.temp - 273.15F, tile.Tref - 273.15F};
		default: return null;
		}
	}

	@Override
	protected void setDisplVar(int id, Object obj, boolean send) {
		PacketBuffer dos = tile.getPacketTargetData();
		switch(id) {
		case 1: dos.writeByte(AutomatedTile.CmdOffset).writeInt(tile.Rw = (Integer)obj); break;
		case 2: dos.writeByte(AutomatedTile.CmdOffset + 1).writeFloat(tile.Tref = (Float)obj * 2500F + 300F); break;
		case 6: dos.writeByte(AutomatedTile.CmdOffset + 2).writeByte(tile.rstCtr ^= 2); break;
		case 7: dos.writeByte(AutomatedTile.CmdOffset + 2).writeByte(tile.rstCtr ^= 1); break;
		}
		if (send) BlockGuiHandler.sendPacketToServer(dos);
	}

}
