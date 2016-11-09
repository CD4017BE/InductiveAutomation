package cd4017be.automation.Gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import cd4017be.automation.TileEntity.TeslaTransmitter;
import cd4017be.lib.BlockGuiHandler;
import cd4017be.lib.Gui.GuiMachine;
import cd4017be.lib.Gui.TileContainer;

/**
 *
 * @author CD4017BE
 */
public class GuiTeslaTransmitter extends GuiMachine {

	private final TeslaTransmitter tile;

	public GuiTeslaTransmitter(TeslaTransmitter tileEntity, EntityPlayer player) {
		super(new TileContainer(tileEntity, player));
		this.tile = tileEntity;
		this.MAIN_TEX = new ResourceLocation("automation", "textures/gui/tesla.png");
	}

	@Override
	public void initGui() {
		this.xSize = 176;
		this.ySize = 40;
		super.initGui();
		guiComps.add(new TextField(0, 8, 16, 34, 8, 5).setTooltip("tesla.set"));
		guiComps.add(new Text(1, 44, 16, 124, 8, "tesla.stor").center().font(0xffdf40, 8));
	}

	@Override
	protected Object getDisplVar(int id) {
		switch(id) {
		case 0: return "" + tile.freq;
		case 1: return tile.Estor / 1000F;
		default: return null;
		}
	}

	@Override
	protected void setDisplVar(int id, Object obj, boolean send) {
		PacketBuffer dos = tile.getPacketTargetData();
		switch(id) {
		case 0: try{dos.writeShort(tile.freq = Short.parseShort((String)obj));} catch(NumberFormatException e) {return;}
		}
		if (send) BlockGuiHandler.sendPacketToServer(dos);
	}

}
