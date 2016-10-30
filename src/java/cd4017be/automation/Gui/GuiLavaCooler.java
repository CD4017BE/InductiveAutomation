package cd4017be.automation.Gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import cd4017be.automation.TileEntity.LavaCooler;
import cd4017be.lib.BlockGuiHandler;
import cd4017be.lib.Gui.GuiMachine;
import cd4017be.lib.Gui.TileContainer;
import cd4017be.lib.templates.AutomatedTile;

/**
 *
 * @author CD4017BE
 */
public class GuiLavaCooler extends GuiMachine {

	private final LavaCooler tile;

	public GuiLavaCooler(LavaCooler tileEntity, EntityPlayer player) {
		super(new TileContainer(tileEntity, player));
		this.tile = tileEntity;
		this.MAIN_TEX = new ResourceLocation("automation", "textures/gui/lavaCooler.png");
	}

	@Override
	public void initGui() {
		this.xSize = 176;
		this.ySize = 168;
		super.initGui();
		guiComps.add(new ProgressBar(5, 80, 16, 16, 52, 176, 0, (byte)3));
		guiComps.add(new Tooltip(6, 80, 18, 16, 52, "\\%s"));
		for (int i = 0; i < 4; i++)
			guiComps.add(new Button(7 + i, 8 + 18 * (i % 2), 16 + 18 * (i / 2), 16, 16, 0).texture(176, 104));
	}

	@Override
	protected Object getDisplVar(int id) {
		switch(id) {
		case 5: return tile.getCool();
		case 6: return tile.getOutputName();
		default: return (tile.cfg >> 4 & 3) == id - 7 ? 1 : 0;
		}
	}

	@Override
	protected void setDisplVar(int id, Object obj, boolean send) {
		PacketBuffer dos = tile.getPacketTargetData();
		if (id >= 7) dos.writeByte(AutomatedTile.CmdOffset + id - 7);
		if (send) BlockGuiHandler.sendPacketToServer(dos);
	}

}
