package cd4017be.automation.Gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import cd4017be.automation.TileEntity.ElectricCoil;
import cd4017be.lib.BlockGuiHandler;
import cd4017be.lib.Gui.GuiMachine;
import cd4017be.lib.Gui.TileContainer;
import cd4017be.lib.templates.AutomatedTile;

public class GuiElectricCoil extends GuiMachine {

	private final ElectricCoil tile;

	public GuiElectricCoil(ElectricCoil tileEntity, EntityPlayer player) {
		super(new TileContainer(tileEntity, player));
		this.tile = tileEntity;
		this.MAIN_TEX = new ResourceLocation("automation", "textures/gui/tesla.png");
	}

	@Override
	public void initGui() {
		this.xSize = 122;
		this.ySize = 58;
		super.initGui();
		this.bgTexY = 80;
		this.guiComps.add(new Slider(1, 81, 18, 32, 212, 54, 12, 4, false));
		this.guiComps.add(new Button(2, 7, 15, 18, 18, 0).texture(176, 0).setTooltip("rstCtr"));
		this.guiComps.add(new Button(3, 7, 33, 18, 18, 0).texture(194, 0).setTooltip("rstCtr"));
		this.guiComps.add(new Text(4, 26, 16, 52, 16, "gui.cd4017be.elCoil.stat"));
		this.guiComps.add(new Text(5, 30, 38, 48, 8, "gui.cd4017be.elCoil.N"));
	}

	@Override
	protected Object getDisplVar(int id) {
		switch(id) {
		case 1: return (float)(tile.N - ElectricCoil.Nmin[tile.type]) / (float)(ElectricCoil.Nmax[tile.type] - ElectricCoil.Nmin[tile.type]);
		case 2: return tile.rstCfg & 3;
		case 3: return tile.rstCfg >> 1 & 1;
		case 4: return new Object[]{tile.Uc, tile.power / 1000F};
		case 5: return tile.N;
		default: return null;
		}
	}

	@Override
	protected void setDisplVar(int id, Object obj, boolean send) {
		PacketBuffer dos = tile.getPacketTargetData();
		switch(id) {
		case 1: dos.writeByte(AutomatedTile.CmdOffset).writeInt(tile.N = (int)((Float)obj * (float)(ElectricCoil.Nmax[tile.type] - ElectricCoil.Nmin[tile.type])) + ElectricCoil.Nmin[tile.type]); break;
		case 2: dos.writeByte(AutomatedTile.CmdOffset + 1).writeByte(tile.rstCfg ^= 1); break;
		case 3: dos.writeByte(AutomatedTile.CmdOffset + 1).writeByte(tile.rstCfg ^= 2); break;
		}
		if (send) BlockGuiHandler.sendPacketToServer(dos);
	}

}
