package cd4017be.automation.Gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import cd4017be.automation.TileEntity.Trash;
import cd4017be.lib.BlockGuiHandler;
import cd4017be.lib.Gui.GuiMachine;
import cd4017be.lib.Gui.TileContainer;
import cd4017be.lib.templates.AutomatedTile;

/**
 *
 * @author CD4017BE
 */
public class GuiTrash extends GuiMachine {

	private final Trash tile;

	public GuiTrash(Trash tileEntity, EntityPlayer player) {
		super(new TileContainer(tileEntity, player));
		this.tile = tileEntity;
		this.MAIN_TEX = new ResourceLocation("automation", "textures/gui/trash.png");
	}

	@Override
	public void initGui() {
		this.xSize = 176;
		this.ySize = 132;
		super.initGui();
		guiComps.add(new Button(2, 115, 15, 18, 18, 0).texture(176, 0));
		guiComps.add(new Button(3, 43, 15, 18, 18, 0).texture(176, 0));
	}

	@Override
	protected Object getDisplVar(int id) {
		switch(id) {
		case 2: return tile.mode & 1;
		case 3: return tile.mode >> 1 & 1;
		default: return null;
		}
	}

	@Override
	protected void setDisplVar(int id, Object obj, boolean send) {
		PacketBuffer dos = tile.getPacketTargetData();
		dos.writeByte(AutomatedTile.CmdOffset + id - 2);
		if(send) BlockGuiHandler.sendPacketToServer(dos);
	}

}
