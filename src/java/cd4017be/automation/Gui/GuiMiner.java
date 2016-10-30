package cd4017be.automation.Gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import cd4017be.automation.TileEntity.Miner;
import cd4017be.lib.BlockGuiHandler;
import cd4017be.lib.Gui.GuiMachine;
import cd4017be.lib.Gui.TileContainer;
import cd4017be.lib.templates.AutomatedTile;

/**
 *
 * @author CD4017BE
 */
public class GuiMiner extends GuiMachine {

	private final Miner tile;

	public GuiMiner(Miner tileEntity, EntityPlayer player) {
		super(new TileContainer(tileEntity, player));
		this.tile = tileEntity;
		this.MAIN_TEX = new ResourceLocation("automation", "textures/gui/miner.png");
	}

	@Override
	public void initGui() {
		this.xSize = 176;
		this.ySize = 168;
		super.initGui();
		guiComps.add(new Button(2, 115, 15, 18, 18, 0).texture(176, 0).setTooltip("miner.on"));
		guiComps.add(new GuiComp(3, 117, 52, 16, 16).setTooltip("miner.drill"));
		guiComps.add(new GuiComp(4, 115, 34, 16, 16).setTooltip("miner.res"));
	}

	@Override
	protected Object getDisplVar(int id) {
		return tile.active ? 1 : 0;
	}

	@Override
	protected void setDisplVar(int id, Object obj, boolean send) {
		if (!send || id != 2) return;
		PacketBuffer dos = tile.getPacketTargetData();
		dos.writeByte(AutomatedTile.CmdOffset);
		BlockGuiHandler.sendPacketToServer(dos);
	}

}
