package cd4017be.automation.Gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import cd4017be.automation.TileEntity.Builder;
import cd4017be.lib.BlockGuiHandler;
import cd4017be.lib.TooltipInfo;
import cd4017be.lib.Gui.GuiMachine;
import cd4017be.lib.Gui.TileContainer;
import cd4017be.lib.templates.AutomatedTile;

/**
 *
 * @author CD4017BE
 */
public class GuiBuilder extends GuiMachine {
	private String[] steps = {"Inactive", "Frame Y", "Frame Z", "Frame X", "Bottom", "Top", "North", "South", "West", "East", "Filling"};
	private Builder tile;

	public GuiBuilder(Builder tileEntity, EntityPlayer player) {
		super(new TileContainer(tileEntity, player));
		this.tile = tileEntity;
		this.MAIN_TEX = new ResourceLocation("automation", "textures/gui/builder.png");
	}

	@Override
	public void initGui() {
		this.xSize = 176;
		this.ySize = 240;
		super.initGui();
		String[] s = TooltipInfo.getLocFormat("gui.cd4017be.builder.state").split("\\n");
		if (s.length >= steps.length) steps = s;
		for (int i = 0; i < 8; i++)
			guiComps.add(new NumberSel(2 + i, 7 + i * 18, 69, 18, 18, "%d", 0, 256, 8).setTooltip("builder.size"));
		guiComps.add(new Text(10, 120, 15, 54, 8, "gui.cd4017be.builder.state_").center());
		guiComps.add(new Button(11, 151, 69, 18, 18, 0).setTooltip("builder.dir"));
		guiComps.add(new Button(12, 115, 15, 54, 18, -1).setTooltip("builder.run"));
		guiComps.add(new GuiComp(13, 7, 15, 54, 18).setTooltip("builder.frame"));
		guiComps.add(new GuiComp(13, 61, 15, 54, 18).setTooltip("builder.wall"));
		guiComps.add(new GuiComp(13, 7, 15, 18, 18).setTooltip("builder.stack"));
	}

	@Override
	protected Object getDisplVar(int id) {
		if (id < 10) return tile.thick[id - 1];
		else if (id == 10) return steps[tile.thick[8]];
		else if (id == 11) return tile.thick[9];
		else return null;
	}

	@Override
	protected void setDisplVar(int id, Object obj, boolean send) {
		PacketBuffer dos = tile.getPacketTargetData();
		if (id < 10) {
			dos.writeByte(AutomatedTile.CmdOffset + id - 2);
			dos.writeShort((Integer)obj);
		} else if (id == 11) {
			dos.writeByte(AutomatedTile.CmdOffset + 9);
			dos.writeByte(tile.thick[8] = (tile.thick[8] + 1) % 3);
		} else if (id == 12) {
			dos.writeByte(AutomatedTile.CmdOffset + 8);
		}
		BlockGuiHandler.sendPacketToServer(dos);
	}

}
