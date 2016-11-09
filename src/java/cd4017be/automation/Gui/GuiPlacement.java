package cd4017be.automation.Gui;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import cd4017be.lib.BlockGuiHandler;
import cd4017be.lib.Gui.GuiMachine;
import cd4017be.lib.Gui.TileContainer;

public class GuiPlacement extends GuiMachine {

	private final InventoryPlayer inv;
	private InventoryPlacement pl;
	private int sel = 0;
	private static final byte[] axis = {0, 0, 0, 0, 2, 2,
										2, 2, 1, 1, 1, 1};

	public GuiPlacement(TileContainer container) {
		super(container);
		this.inv = container.player.inventory;
		this.pl = new InventoryPlacement(inv.mainInventory[inv.currentItem]);
		this.MAIN_TEX = new ResourceLocation("automation", "textures/gui/blockPlacement.png");
	}

	@Override
	public void initGui() {
		this.xSize = 176;
		this.ySize = 150;
		super.initGui();
		for (int i = 0; i < 8; i++)
			guiComps.add(new Button(i, 8 + i * 18, 16, 16, 16, 0).texture(212, 0));
		guiComps.add(new TextField(8, 42, 36, 34, 12, 5).setTooltip("place.aim"));
		guiComps.add(new TextField(9, 96, 36, 34, 12, 5).setTooltip("place.aim"));
		guiComps.add(new Button(10, 7, 33, 18, 18, 2).texture(194, 0).setTooltip("place.dir"));
		guiComps.add(new Button(11, 133, 33, 18, 18, 0).texture(212, 36).setTooltip("place.sneak"));
		guiComps.add(new Button(12, 151, 33, 18, 18, 0).texture(230, 36).setTooltip("filter.meta#"));
		guiComps.add(new Button(13, 151, 15, 18, 18, 0).texture(230, 0).setTooltip("place.raytrace#"));
		guiComps.add(new ProgressBar(14, 25, 34, 16, 16, 176, 0, (byte)3));
		guiComps.add(new ProgressBar(15, 79, 34, 16, 16, 176, 0, (byte)3));
	}

	@Override
	public void updateScreen() {
		pl = new InventoryPlacement(inv.mainInventory[inv.currentItem]);
		super.updateScreen();
	}

	@Override
	protected Object getDisplVar(int id) {
		switch(id) {
		case 8: return sel < pl.placement.length ? "" + (pl.placement[sel] & 0xf) : "8";
		case 9: return sel < pl.placement.length ? "" + (pl.placement[sel] >> 4 & 0xf) : "8";
		case 10: return sel < pl.placement.length ? pl.getDir(sel) : EnumFacing.DOWN;
		case 11: return sel < pl.placement.length && pl.sneak(sel) ? 1 : 0;
		case 12: return sel < pl.placement.length && pl.useDamage(sel) ? 1 : 0;
		case 13: return sel < pl.placement.length && pl.rayTrace(sel) ? 1 : 0;
		case 14: return sel < pl.placement.length ? (float)axis[(pl.placement[sel] >> 8 & 0xf) % 6] : 0F;
		case 15: return sel < pl.placement.length ? (float)axis[(pl.placement[sel] >> 8 & 0xf) % 6 + 6] : 2F;
		default: if (id < 8) return id == sel ? 1 : 0;
		else return null;
		}
	}

	@Override
	protected void setDisplVar(int id, Object obj, boolean send) {
		PacketBuffer dos = BlockGuiHandler.getPacketTargetData(((TileContainer)inventorySlots).data.pos());
		int mode = sel < pl.placement.length ? pl.placement[sel] : 0x0088; 
		dos.writeByte(sel);
		switch(id) {
		case 8: try {mode &= 0xfff0; mode |= Integer.parseInt((String)obj) & 0x000f; break;} catch (NumberFormatException e) {return;}
		case 9: try {mode &= 0xff0f; mode |= Integer.parseInt((String)obj) << 4 & 0x00f0; break;} catch (NumberFormatException e) {return;}
		case 10: mode = mode & 0xf0ff | (((mode >> 8 & 0xf) + ((Integer)obj == 0 ? 1 : 5)) % 6) << 8; break;
		case 11: mode ^= 0x1000; break;
		case 12: mode ^= 0x2000; break;
		case 13: mode ^= 0x4000; break;
		default: if (id < 8) sel = id;
			return;
		}
		dos.writeInt(mode);
		if (send) BlockGuiHandler.sendPacketToServer(dos);
	}

}
