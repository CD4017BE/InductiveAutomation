package cd4017be.automation.Gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import cd4017be.automation.TileEntity.ItemBuffer;
import cd4017be.lib.BlockGuiHandler;
import cd4017be.lib.Gui.GuiMachine;
import cd4017be.lib.Gui.TileContainer;
import cd4017be.lib.templates.AutomatedTile;

public class GuiItemBuffer extends GuiMachine {

	private final ItemBuffer tile;

	public GuiItemBuffer(ItemBuffer tileEntity, EntityPlayer player) {
		super(new TileContainer(tileEntity, player));
		this.tile = tileEntity;
		this.MAIN_TEX = new ResourceLocation("automation", "textures/gui/buffer.png");
	}

	@Override
	public void initGui() {
		this.xSize = 176;
		this.ySize = 168;
		super.initGui();
		guiComps.add(new Button(1, 7, 51, 18, 18, 0).texture(176, 0).setTooltip("buffer.stack#"));
		guiComps.add(new NumberSel(2, 43, 51, 18, 18, "%d", 0, 18, 9).setTooltip("buffer.overfl"));
		guiComps.add(new NumberSel(3, 88, 51, 18, 18, "%d", 0, 64, 8).setTooltip("buffer.split"));
		guiComps.add(new NumberSel(3, 133, 51, 18, 18, "%d", 0, 64, 8).setTooltip("buffer.split"));
	}

	@Override
	protected Object getDisplVar(int id) {
		switch(id) {
		case 1: return tile.mode >> 8 & 1;
		case 2: return tile.mode & 0xff;
		case 3: return tile.amA;
		case 4: return tile.amB;
		default: return null;
		}
	}

	@Override
	protected void setDisplVar(int id, Object obj, boolean send) {
		PacketBuffer dos = tile.getPacketTargetData();
		switch(id) {
		case 1: dos.writeByte(AutomatedTile.CmdOffset).writeInt(tile.mode ^= 0x100); break;
		case 2: dos.writeByte(AutomatedTile.CmdOffset).writeInt(tile.mode = (tile.mode & 0x100) | ((Integer)obj & 0xff)); break;
		case 3: dos.writeByte(AutomatedTile.CmdOffset + 1).writeInt(tile.amA = Math.min(64 - tile.amB, (Integer)obj)); break;
		case 4: dos.writeByte(AutomatedTile.CmdOffset + 2).writeInt(tile.amB = Math.min(64 - tile.amA, (Integer)obj)); break;
		}
		if (send) BlockGuiHandler.sendPacketToServer(dos);
	}

}
