package cd4017be.automation.Gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import cd4017be.automation.TileEntity.Teleporter;
import cd4017be.lib.BlockGuiHandler;
import cd4017be.lib.Gui.GuiMachine;
import cd4017be.lib.Gui.TileContainer;
import cd4017be.lib.templates.AutomatedTile;

/**
 *
 * @author CD4017BE
 */
public class GuiTeleporter extends GuiMachine {

	private final Teleporter tile;
	private byte warned = 0;

	public GuiTeleporter(Teleporter tileEntity, EntityPlayer player) {
		super(new TileContainer(tileEntity, player));
		this.tile = tileEntity;
		this.MAIN_TEX = new ResourceLocation("automation", "textures/gui/teleporter.png");
	}

	@Override
	public void initGui() {
		this.xSize = 176;
		this.ySize = 168;
		super.initGui();
		guiComps.add(new TextField(2, 8, 52, 34, 16, 8));
		guiComps.add(new TextField(3, 44, 52, 34, 16, 8));
		guiComps.add(new TextField(4, 80, 52, 34, 16, 8));
		guiComps.add(new TextField(5, 134, 34, 34, 16, 16).setTooltip("teleport.name"));
		guiComps.add(new Button(6, 133, 51, 18, 18, -1).setTooltip("teleport.rw"));
		guiComps.add(new Button(7, 115, 33, 18, 18, 0).texture(194, 0).setTooltip("teleport.rel#"));
		guiComps.add(new Button(8, 97, 33, 18, 18, 0).texture(176, 0).setTooltip("rstCtr"));
		guiComps.add(new Button(9, 7, 33, 90, 18, -1));
		guiComps.add(new ProgressBar(10, 8, 34, 88, 16, 176, 0, (byte)4));
		guiComps.add(new Tooltip(11, 7, 33, 90, 18, "storage"));
		guiComps.add(new Text(12, 8, 38, 88, 8, "teleport.do#"));
		guiComps.add(new Text(13, 121, 4, 48, 8, "teleport.copy#"));
		guiComps.add(new Text(14, 0, ySize, xSize, 0, "teleport.warning#"));
		guiComps.add(new Button(15, 121, 4, 48, 8, -1));
	}

	@Override
	protected Object getDisplVar(int id) {
		switch(id) {
		case 2: return "" + tile.tgX;
		case 3: return "" + tile.tgY;
		case 4: return "" + tile.tgZ;
		case 5: return "";
		case 7: return tile.mode >> 1 & 1;
		case 8: return tile.mode & 1;
		case 10: return tile.getStorage();
		case 11: return new Object[]{tile.Estor / 1000F, tile.Eneed / 1000F};
		case 12: return tile.mode >> 2 & 1;
		case 13: return tile.mode >> 4 & 1;
		case 14: 
			if (tile.isInWorldBounds()) warned = 0;
			else if (warned == 0) warned = 1;
			return warned;
		default: return null;
		}
	}

	@Override
	protected void setDisplVar(int id, Object obj, boolean send) {
		PacketBuffer dos = tile.getPacketTargetData();
		switch(id) {
		case 2: try{dos.writeByte(AutomatedTile.CmdOffset + 1).writeInt(Integer.parseInt((String)obj)); break;} catch (NumberFormatException e) {return;}
		case 3: try{dos.writeByte(AutomatedTile.CmdOffset + 2).writeInt(Integer.parseInt((String)obj)); break;} catch (NumberFormatException e) {return;}
		case 4: try{dos.writeByte(AutomatedTile.CmdOffset + 3).writeInt(Integer.parseInt((String)obj)); break;} catch (NumberFormatException e) {return;}
		case 5: dos.writeByte(AutomatedTile.CmdOffset + 4); dos.writeString((String)obj); break;
		case 6: dos.writeByte(AutomatedTile.CmdOffset).writeByte(3); break;
		case 7: dos.writeByte(AutomatedTile.CmdOffset).writeByte(2); break;
		case 8: dos.writeByte(AutomatedTile.CmdOffset).writeByte(1); break;
		case 9: 
			if (warned == 1 && (tile.mode & 4) == 0) {warned = 2; return;}
			else {dos.writeByte(AutomatedTile.CmdOffset).writeByte(1); break;}
		case 15: dos.writeByte(AutomatedTile.CmdOffset).writeByte(4); break;
		}
		if(send) BlockGuiHandler.sendPacketToServer(dos);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mx, int my) {
		super.drawGuiContainerForegroundLayer(mx, my);
		if (this.isPointInRegion(8, 52, 34, 16, mx, my)) this.drawSideCube(-64, tabsY + 63, 5, (byte)3);
		else if (this.isPointInRegion(44, 52, 34, 16, mx, my)) this.drawSideCube(-64, tabsY + 63, 1, (byte)3);
		else if (this.isPointInRegion(80, 52, 34, 16, mx, my)) this.drawSideCube(-64, tabsY + 63, 3, (byte)3);
	}

}
