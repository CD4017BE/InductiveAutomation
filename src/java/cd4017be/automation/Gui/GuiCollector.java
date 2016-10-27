package cd4017be.automation.Gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import cd4017be.automation.TileEntity.Collector;
import cd4017be.lib.BlockGuiHandler;
import cd4017be.lib.Gui.GuiMachine;
import cd4017be.lib.Gui.TileContainer;
import cd4017be.lib.templates.AutomatedTile;

/**
 *
 * @author CD4017BE
 */
public class GuiCollector extends GuiMachine {

	private final Collector tile;

	public GuiCollector(Collector tileEntity, EntityPlayer player) {
		super(new TileContainer(tileEntity, player));
		this.tile = tileEntity;
		this.MAIN_TEX = new ResourceLocation("automation", "textures/gui/collector.png");
	}

	@Override
	public void initGui() {
		this.xSize = 226;
		this.ySize = 98;
		super.initGui();
		guiComps.add(new Button(2, 183, 73, 18, 18, 0).texture(238, 0).setTooltip("collect.m"));
	}

	@Override
	protected Object getDisplVar(int id) {
		return tile.mode;
	}

	@Override
	protected void setDisplVar(int id, Object obj, boolean send) {
		PacketBuffer dos = tile.getPacketTargetData();
		dos.writeByte(AutomatedTile.CmdOffset);
		if(send) BlockGuiHandler.sendPacketToServer(dos);
	}

}
