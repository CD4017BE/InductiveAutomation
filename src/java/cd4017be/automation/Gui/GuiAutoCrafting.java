package cd4017be.automation.Gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import cd4017be.automation.TileEntity.AutoCrafting;
import cd4017be.lib.BlockGuiHandler;
import cd4017be.lib.Gui.GuiMachine;
import cd4017be.lib.Gui.TileContainer;
import cd4017be.lib.templates.AutomatedTile;

/**
 *
 * @author CD4017BE
 */
public class GuiAutoCrafting extends GuiMachine {

	private final AutoCrafting tile;

	public GuiAutoCrafting(AutoCrafting tileEntity, EntityPlayer player) {
		super(new TileContainer(tileEntity, player));
		this.tile = tileEntity;
		this.MAIN_TEX = new ResourceLocation("automation", "textures/gui/autoCraft.png");
	}

	@Override
	public void initGui() {
		this.xSize = 176;
		this.ySize = 168;
		super.initGui();
		for (int j = 0; j < 3; j++)
			for (int i = 0; i < 3; i++)
				guiComps.add(new NumberSel(1 + i + j * 3, 79 + i * 18, 15 + j * 18, 18, 18, "%d", -1, 9, 1));
		guiComps.add(new Button(10, 151, 15, 18, 18, 0).setTooltip("autoCrafting.mode#"));
		guiComps.add(new GuiComp(11, 67, 31, 7, 8).setTooltip("autoCrafting"));
	}

	@Override
	protected Object getDisplVar(int id) {
		if(id < 11) {
			int s = tile.getRef(id - 1);
			return s < 9 ? s : -1;
		} else return null;
	}

	@Override
	protected void setDisplVar(int id, Object obj, boolean send) {
		PacketBuffer dos = tile.getPacketTargetData();
		if (id < 10) {
			byte n = ((Integer)obj).byteValue();
			tile.setRef(id - 1, n < 0 ? -1 : n);
		} else if (id == 10) tile.setRef(9, (byte)((tile.getRef(9) + 1) % 3));
		dos.writeByte(AutomatedTile.CmdOffset);
		dos.writeLong(tile.cfg);
		if (send) BlockGuiHandler.sendPacketToServer(dos);
	}

}
