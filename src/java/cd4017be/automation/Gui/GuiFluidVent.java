package cd4017be.automation.Gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import cd4017be.automation.TileEntity.FluidVent;
import cd4017be.lib.BlockGuiHandler;
import cd4017be.lib.Gui.GuiMachine;
import cd4017be.lib.Gui.TileContainer;
import cd4017be.lib.templates.AutomatedTile;

/**
 *
 * @author CD4017BE
 */
public class GuiFluidVent extends GuiMachine {

	private final FluidVent tile;

	public GuiFluidVent(FluidVent tileEntity, EntityPlayer player) {
		super(new TileContainer(tileEntity, player));
		this.tile = tileEntity;
		this.MAIN_TEX = new ResourceLocation("automation", "textures/gui/fluidVent.png");
	}

	@Override
	public void initGui() {
		this.xSize = 226;
		this.ySize = 98;
		super.initGui();
		guiComps.add(new Button(2, 183, 73, 18, 18, 0).texture(226, 0).setTooltip("pump.update"));
		guiComps.add(new NumberSel(3, 201, 73, 18, 18, "%d", 0, 127, 8).setTooltip("vent.range"));
		guiComps.add(new Text(4, 0, ySize, xSize, 8, "vent.pos").font(0xffffffff, 8).center());
	}

	@Override
	protected Object getDisplVar(int id) {
		switch(id) {
		case 2: return tile.mode >> 8 & 1;
		case 3: return tile.mode & 0xff;
		case 4: return new Object[]{(byte)tile.netI1, (byte)(tile.netI1 >> 8), (byte)(tile.netI1 >> 16)};
		default: return null;
		}
	}

	@Override
	protected void setDisplVar(int id, Object obj, boolean send) {
		PacketBuffer dos = tile.getPacketTargetData();
		switch(id) {
		case 2: dos.writeByte(AutomatedTile.CmdOffset); break;
		case 3: dos.writeByte(AutomatedTile.CmdOffset + 1).writeByte(tile.mode = (tile.mode & 0x100) | ((Integer)obj & 0xff));
		}
		if (send) BlockGuiHandler.sendPacketToServer(dos);
	}

}
