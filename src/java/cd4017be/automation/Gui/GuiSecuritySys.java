package cd4017be.automation.Gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

import cd4017be.automation.TileEntity.SecuritySys;
import cd4017be.lib.BlockGuiHandler;
import cd4017be.lib.Gui.GuiMachine;
import cd4017be.lib.Gui.TileContainer;

/**
 *
 * @author CD4017BE
 */
public class GuiSecuritySys extends GuiMachine {

	private final SecuritySys tile;
	private int scroll;
	private int listS;
	private int listN = -1;

	public GuiSecuritySys(SecuritySys tileEntity, EntityPlayer player) {
		super(new TileContainer(tileEntity, player));
		this.tile = tileEntity;
		this.MAIN_TEX = new ResourceLocation("automation", "textures/gui/securitySys.png");
	}

	@Override
	public void initGui() {
		this.xSize = 179;
		this.ySize = 201;
		super.initGui();
		guiComps.add(new TextField(1, 137, 117, 34, 16, 32).setTooltip("security.add"));
		guiComps.add(new NumberSel(2, 155, 16, 16, 52, "%d", 0, tile.energy.Umax, 10).setTooltip("voltage"));
		guiComps.add(new ProgressBar(3, 137, 16, 16, 52, 180, 0, (byte)1));
		guiComps.add(new Button(4, 136, 69, 18, 18, 0).texture(195, 0).setTooltip("security.rstL"));
		guiComps.add(new Button(5, 154, 69, 18, 18, 0).texture(213, 0).setTooltip("security.rstP"));
		guiComps.add(new Text(6, 137, 88, 34, 27, "security.power").setTooltip("security.cost"));
		guiComps.add(new Slider(7, 8, 145, 48, 179, 52, 8, 12, false));
		guiComps.add(new Button(8, 137, 145, 34, 12, 0).setTooltip("security.load"));
		guiComps.add(new Button(9, 137, 157, 34, 12, 0).setTooltip("security.admin"));
		guiComps.add(new Button(10, 137, 169, 34, 12, 0).setTooltip("security.special"));
		guiComps.add(new Button(11, 137, 181, 34, 12, 0).setTooltip("security.special"));
		for (int i = 0; i < 6; i++)
			guiComps.add(new Button(12 + i, 18, 45 + i * 8, 8, 8, 0).texture(187, 52));
		for (int i = 0; i < 6; i++)
			guiComps.add(new Text(18 + i, 27, 145 + i * 8, 108, 8, "\\%s"));
		guiComps.add(new Text(24, 137, 145, 34, 48, "security.lists").center().font(0x404040, 12));
		guiComps.add(new Matrix(25, 8, 16, 128, 128));
		guiComps.add(new Tooltip(26, 137, 16, 16, 52, "energy"));
	}

	@Override
	protected Object getDisplVar(int id) {
		listS = tile.prot.getGroupSize(listN<0 ? 0:listN);
		switch(id) {
		case 1: return "";
		case 2: return tile.Uref;
		case 3: return tile.getStorage();
		case 4: return tile.rstCtr >> 2 & 3;
		case 5: return tile.rstCtr & 3;
		case 6: return new Object[]{tile.EuseL, tile.EuseP};
		case 7: return listS <= 6 ? 0F : (float)scroll / (float)(listS - 6);
		case 26: return tile.Estor;
		default: if (id < 12) return listN == id - 9 ? 0 : 1;
			else if (id < 18) return listS >= 6 || id - 12 < listS ? 0 : 1;
			else if (id < 24) return id - 18 + scroll < listS ? tile.prot.getPlayer(id - 18 + scroll, listN<0 ? 0:listN) : "";
			else return null;
		}
	}

	@Override
	protected void setDisplVar(int id, Object obj, boolean send) {
		PacketBuffer dos = tile.getPacketTargetData();
		switch(id) {
		case 1: dos.writeByte(2).writeByte(listN<0 ? 0:listN); dos.writeString((String)obj); break;
		case 2: dos.writeByte(4).writeInt(tile.Uref = (Integer)obj); break;
		case 4: dos.writeByte(0).writeByte(tile.rstCtr ^= (Integer)obj == 0 ? 0x4 : 0x8); break;
		case 5: dos.writeByte(0).writeByte(tile.rstCtr ^= (Integer)obj == 0 ? 0x1 : 0x2); break;
		case 7: scroll = (int)((Float)obj * (float)(listS - 6)); return;
		case 25: dos.writeByte(3).writeByte(listN).writeByte((Integer)obj); break;
		default: if (id < 12) {
				listN = id - 9;
				listS = tile.prot.groups[listN<0 ? 0:listN].members.size();
				scroll = Math.max(0, Math.min(listS - 6, scroll));
				return;
			} else if(id < 18) {
				int n = id - 12 + scroll;
				if (n < listS) dos.writeByte(1).writeByte(listN<0 ? 0:listN).writeByte(n);
				else return;
			}
		}
		if (send) BlockGuiHandler.sendPacketToServer(dos);
	}

	class Matrix extends GuiComp {

		public Matrix(int id, int px, int py, int w, int h) {
			super(id, px, py, w, h);
		}

		@Override
		public void draw() {
			mc.renderEngine.bindTexture(MAIN_TEX);
			if (listN >= 0) {
				byte[] area = tile.prot.groups[listN].area;
				for (int i = 0; i < 8; i++)
					for (int j = 0; j < 8; j++) {
						byte n = area[i + j * 8];
						if (n > 0 && n < 4) drawTexturedModalRect(px + i * 16, py + j * 16, 240, n * 16 - 16, 16, 16);
					}
			} else {
				for (int i = 0; i < 64; i++) 
					if ((tile.prot.loadedChunks >> i & 1) != 0)
						drawTexturedModalRect(px + (i % 8) * 16, py + (i / 8) * 16, 240, 48, 16, 16);
			}
			drawTexturedModalRect(px + 54 + (tile.getPos().getX() + 8 & 15), py + 54 + (tile.getPos().getZ() + 8 & 15), 179, 64, 4, 4);
			drawTexturedModalRect(px + 124, py, 231, 0, 7, 7);
			drawTexturedModalRect(px + 124, py + 248, 231, 7, 7, 7);
			drawTexturedModalRect(px, py + 124, 231, 14, 7, 7);
			drawTexturedModalRect(px + 248, py + 124, 231, 21, 7, 7);
		}

		@Override
		public boolean mouseIn(int x, int y, int b, int d) {
			if (d != 0) return false;
			int n = (x - px) / 16 + (y - py) / 16 * 8;
			if (n >= 0 && n < 64) setDisplVar(id, n, true);
			return true;
		}

	}

}
