package cd4017be.automation.Gui;

import java.util.Arrays;

import cd4017be.api.energy.EnergyAPI;
import cd4017be.automation.Item.ItemPortableTeleporter;
import cd4017be.lib.BlockGuiHandler;
import cd4017be.lib.Gui.GuiMachine;
import cd4017be.lib.Gui.TileContainer;
import cd4017be.lib.util.Utils;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.ResourceLocation;

public class GuiPortableTeleporter extends GuiMachine {

	private static final int size = 8;
	private final InventoryPlayer inv;
	private int scroll = 0;
	private int sel = 0;
	private Entry[] list = new Entry[0];
	private double maxDist = 0;
	private boolean isFull = false;

	public GuiPortableTeleporter(TileContainer container) {
		super(container);
		this.inv = container.player.inventory;
		this.MAIN_TEX = new ResourceLocation("automation", "textures/gui/portableTeleporter.png");
	}

	@Override
	public void initGui() {
		this.xSize = 176;
		this.ySize = 115;
		super.initGui();
		for (int i = 0; i < 8; i++)
			guiComps.add(new Button(i, 18, 16 + i * 8, 7, 7, 0).texture(176, 12));
		for (int i = 0; i < 8; i++)
			guiComps.add(new Button(i + 8, 26, 16 + i * 8, 106, 8, 0).texture(0, 115));
		for (int i = 0; i < 8; i++)
			guiComps.add(new Button(i + 16, 134, 16 + i * 8, 34, 8, -1).setTooltip("teleporter.move"));
		guiComps.add(new Slider(24, 8, 22, 52, 176, 0, 8, 12, false));
		guiComps.add(new Button(25, 152, 82, 16, 16, -1).setTooltip("teleporter.move"));
		guiComps.add(new Button(26, 134, 82, 16, 16, -1).setTooltip("teleporter.pos"));
		guiComps.add(new TextField(27, 26, 82, 106, 7, 20));
		guiComps.add(new TextField(28, 26, 91, 52, 7, 8));
		guiComps.add(new TextField(29, 98, 91, 34, 7, 5));
		guiComps.add(new TextField(30, 26, 100, 52, 7, 8));
		guiComps.add(new TextField(31, 98, 100, 34, 7, 5));
	}

	@Override
	public void updateScreen() {
		super.updateScreen();
		int l = list.length;
		ItemStack item = inv.mainInventory[inv.currentItem];
		if (item == null || !item.hasTagCompound()) {
			list = new Entry[0];
		} else {
			int x = MathHelper.floor_double(inv.player.posX);
			int y = MathHelper.floor_double(inv.player.posY) - 1;
			int z = MathHelper.floor_double(inv.player.posZ);
			int d = inv.player.worldObj.provider.getDimension();
			NBTTagList data = item.getTagCompound().getTagList("points", 10);
			list = new Entry[data.tagCount()];
			isFull = list.length >= 64;
			int n = 0;
			for (int i = 0; i < list.length; i++)
				list[i] = new Entry(data.getCompoundTagAt(i), x, y, z, d);
			//if (n < list.length) list = Arrays.copyOf(list, n);
			maxDist = EnergyAPI.get(item, 0).getStorage() / (double)ItemPortableTeleporter.energyUse / 1000D;
		}
		if (list.length != l) scroll = 0;
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
		super.drawGuiContainerBackgroundLayer(var1, var2, var3);
		int x, y;
		for (int i = scroll; i < scroll + size && i < list.length; i++) {
			x = this.guiLeft + 18; y = this.guiTop + 16 + (i - scroll) * 8;
			list[i].draw(this, x, y);
		}
	}

	@Override
	protected Object getDisplVar(int id) {
		switch(id) {
		case 24: return list.length < size ? 0F : (float)scroll / (float)(list.length + 1 - size);
		case 27: return sel < list.length ? list[sel].name : "";
		case 28: return sel < list.length ? "" + list[sel].x : "0";
		case 29: return sel < list.length ? "" + list[sel].y : "0";
		case 30: return sel < list.length ? "" + list[sel].z : "0";
		case 31: return sel < list.length ? "" + list[sel].dim : "0";
		default:
			if (id < 8) return scroll + id < list.length ? 0 : scroll + id == list.length && !isFull ? 1 : 2;
			if (id < 16) return scroll + id - 8 == sel ? 1 : 0;
			return null;
		}
	}

	@Override
	protected void setDisplVar(int id, Object obj, boolean send) {
		PacketBuffer dos = BlockGuiHandler.getPacketTargetData(((TileContainer)inventorySlots).data.pos());
		if(id == 24){scroll = Math.max(0, Math.round((Float)obj * (float)(list.length + 1 - size))); return;}
		else if(id == 25){dos.writeByte(0); dos.writeByte(sel);}
		else if (id < 8) {
			if (scroll + id >= list.length) dos.writeByte(1);
			else dos.writeByte(3).writeByte(scroll + id);
		} else if (id < 16) {
			sel = scroll + id - 8; return;
		} else if (id < 24) dos.writeByte(0).writeByte(scroll + id - 16);
		else if(id < 32) {
			if (sel >= list.length) return;
			dos.writeByte(2);
			dos.writeByte(sel);
			Entry e = list[sel];
			if (id == 26) {
				e.x = MathHelper.floor_double(inv.player.posX);
				e.y = MathHelper.floor_double(inv.player.posY) - 1;
				e.z = MathHelper.floor_double(inv.player.posZ);
				e.dim = inv.player.worldObj.provider.getDimension();
			} else if (id == 27) e.name = (String)obj;
			else if (id == 28) e.x = Integer.parseInt((String)obj);
			else if(id == 29) e.y = Integer.parseInt((String)obj);
			else if(id == 30) e.z = Integer.parseInt((String)obj);
			else if (id == 31) e.dim = Integer.parseInt((String)obj);
			e.write(dos);
		} else return;
		if (send) BlockGuiHandler.sendPacketToServer(dos);
	}

	private static class Entry {
		public int x;
		public int y;
		public int z;
		public int dim;
		public String name;
		public float dist;

		public Entry(NBTTagCompound tag, int rx, int ry, int rz, int rd) {
			x = tag.getInteger("x");
			y = tag.getInteger("y");
			z = tag.getInteger("z");
			dim = tag.getByte("d");
			name = tag.getString("n");
			rx -= x; ry -= y; rz -= z;
			dist = dim != rd ? Float.POSITIVE_INFINITY : (float)Math.sqrt(rx * rx + ry * ry + rz * rz);
		}

		public void draw(GuiPortableTeleporter gui, int x, int y) {
			gui.fontRendererObj.drawString(name, x + 8, y, 0x404040);
			gui.drawStringCentered(Utils.formatNumber(dist, 3, 0) + "m", x + 133, y, dist <= gui.maxDist ? dist <= gui.maxDist / 2D ? 0x40ff40 : 0xffff40 : 0xff4040);
		}

		public void write(PacketBuffer dos) {
			dos.writeInt(x);
			dos.writeInt(y);
			dos.writeInt(z);
			dos.writeByte(dim);
			dos.writeString(name);
		}
	}

}
