package cd4017be.automation.Gui;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.ResourceLocation;

import cd4017be.api.automation.IOperatingArea;
import cd4017be.automation.Item.ItemSelectionTool;
import cd4017be.lib.BlockGuiHandler;
import cd4017be.lib.Gui.GuiMachine;
import cd4017be.lib.Gui.TileContainer;

public class GuiAreaUpgrade extends GuiMachine {

	private final ItemSelectionTool.GuiData data;
	private int[] area = new int[6];
	private int distance, maxDist, mx, my, mz, Umax;

	public GuiAreaUpgrade(TileContainer c) {
		super(c);
		this.MAIN_TEX = new ResourceLocation("automation", "textures/gui/areaConfig.png");
		this.data = (ItemSelectionTool.GuiData)c.data;
	}

	@Override
	public void initGui() {
		this.xSize = 176;
		this.ySize = 168;
		super.initGui();
		for (int i = 0; i < 2; i++)
			for (int j = 0; j < 3; j++)
				guiComps.add(new TextField(i * 3 + j, 110 + i * 33, 36 + j * 12, 25, 8, 5));
		guiComps.add(new Text(6, 26, 16, 88, 16, "areaCfg.size_").setTooltip("areaCfg.size"));
		guiComps.add(new Text(7, 26, 34, 88, 16, "areaCfg.dist_").setTooltip("areaCfg.dist"));
		guiComps.add(new Text(8, 26, 52, 88, 16, "areaCfg.Umax_").setTooltip("areaCfg.Umax"));
		guiComps.add(new Button(9, 115, 15, 18, 18, -1).setTooltip("areaCfg.copy"));
		guiComps.add(new GuiComp(10, 152, 16, 16, 16).setTooltip("areaCfg.dspl"));
		guiComps.add(new GuiComp(11, 134, 16, 16, 16).setTooltip("areaCfg.synch"));
	}

	@Override
	protected Object getDisplVar(int id) {
		switch(id) {
		case 6: return new Object[]{area[3] - area[0], area[4] - area[1], area[5] - area[2], mx, my, mz};
		case 7: return new Object[]{distance, maxDist == Integer.MAX_VALUE ? "inf" : "" + maxDist};
		case 8: return Umax;
		default: if (id < 6) return "" + area[id];
		else return null;
		}
	}

	@Override
	protected void setDisplVar(int id, Object obj, boolean send) {
		PacketBuffer dos = BlockGuiHandler.getPacketTargetData(new BlockPos(0, -1, 0));
		if (id == 9) dos.writeByte((byte)6);
		else if (id < 6) try {
				dos.writeByte((byte)id);
				dos.writeInt(Integer.parseInt((String)obj));
			} catch (NumberFormatException e) {return;}
		if (send) BlockGuiHandler.sendPacketToServer(dos);
	}

	@Override
	public void updateScreen() {
		area = data.tile.getOperatingArea();
		int[] max = IOperatingArea.Handler.maxSize(data.tile);
		mx = max[0];
		my = max[1];
		mz = max[2];
		maxDist = IOperatingArea.Handler.maxRange(data.tile);
		Umax = IOperatingArea.Handler.Umax(data.tile);
		int dx = data.pos.getX() + 1 < area[0] ? area[0] - data.pos.getX() - 1 : data.pos.getX() > area[3] ? data.pos.getX() - area[3] : 0;
		int dy = data.pos.getY() + 1 < area[1] ? area[1] - data.pos.getY() - 1 : data.pos.getY() > area[4] ? data.pos.getY() - area[4] : 0;
		int dz = data.pos.getZ() + 1 < area[2] ? area[2] - data.pos.getZ() - 1 : data.pos.getZ() > area[5] ? data.pos.getZ() - area[5] : 0;
		distance = dx > dy ? (dz > dx ? dz : dx) : (dz > dy ? dz : dy);
		super.updateScreen();
	}

}
