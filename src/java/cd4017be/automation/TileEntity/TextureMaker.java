package cd4017be.automation.TileEntity;

import java.io.IOException;

import cd4017be.automation.Objects;
import cd4017be.automation.Gui.GuiTextureMaker;
import cd4017be.automation.Item.ItemBuilderTexture;
import cd4017be.lib.Gui.DataContainer;
import cd4017be.lib.Gui.DataContainer.IGuiData;
import cd4017be.lib.Gui.SlotHolo;
import cd4017be.lib.Gui.TileContainer;
import cd4017be.lib.templates.AutomatedTile;
import cd4017be.lib.templates.Inventory;
import cd4017be.lib.util.ItemFluidUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraftforge.items.SlotItemHandler;

/**
 *
 * @author CD4017BE
 */
public class TextureMaker extends AutomatedTile implements IGuiData {

	public byte[] drawing = new byte[0];
	public byte width = 0;
	public byte height = 0;
	public int netI0, netI1, netI2;

	public TextureMaker() {
		inventory = new Inventory(16, 0, null);
	}

	@Override
	public SPacketUpdateTileEntity getUpdatePacket() 
	{
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setByteArray("tex", drawing);
		nbt.setByte("width", width);
		return new SPacketUpdateTileEntity(getPos(), -1, nbt);
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) 
	{
		drawing = pkt.getNbtCompound().getByteArray("tex");
		width = pkt.getNbtCompound().getByte("width");
		if (drawing == null)
		{
			drawing = new byte[0];
			width = 0;
		}
		height = (byte)(width == 0 ? 0 : drawing.length / width);
	}

	@Override
	public void onPlayerCommand(PacketBuffer dis, EntityPlayerMP player) throws IOException 
	{
		byte cmd = dis.readByte();
		if (cmd == GuiTextureMaker.CMD_Button) {
			byte id = dis.readByte();
			if (id == 0) load();
			else if (id == 1) save();
			else if (id == 6) netI0 = (netI0 + 1) % 4;
			else if (id < 6) {
				byte x0 = dis.readByte();
				byte y0 = dis.readByte();
				byte x1 = dis.readByte();
				byte y1 = dis.readByte();
				if (x0 > x1) {byte i = x0; x0 = x1; x1 = i;}
				if (y0 > y1) {byte i = y0; y0 = y1; y1 = i;}
				if (x0 < 0) x0 = 0;
				if (y0 < 0) y0 = 0;
				if (x1 > width) x1 = width;
				if (y1 > height) y1 = height;
				if (id == 2) transform(x0, y0, x1, y1, Transformation.mirrorH);
				else if (id == 3) transform(x0, y0, x1, y1, Transformation.mirrorV);
				else if (id == 4) transform(x0, y0, x1, y1, Transformation.rotateR);
				else if (id == 5) {
					for (int y = y0; y < y1; y++)
						for (int x = x0; x < x1; x++)
							drawing[x + y * width] = 0;
				}
				this.markUpdate();
			} 
		} else if (cmd == GuiTextureMaker.CMD_Draw) {
			byte x = dis.readByte();
			byte y = dis.readByte();
			byte t = dis.readByte();
			if ((x >= 0 && x < width && y >= 0 && y < height) || t >= 3)
			{
				byte b = 0, x0 = 0, y0 = 0, x1 = 0, y1 = 0;
				if (t < 3) b = dis.readByte();
				if (t != 0) {
					x0 = dis.readByte();
					y0 = dis.readByte();
					x1 = dis.readByte();
					y1 = dis.readByte();
					if (x0 > x1) {byte i = x0; x0 = x1; x1 = i;}
					if (y0 > y1) {byte i = y0; y0 = y1; y1 = i;}
					if (x0 < 0) x0 = 0;
					if (y0 < 0) y0 = 0;
					if (x1 > width) x1 = width;
					if (y1 > height) y1 = height;
				}
				if (t == 0) drawing[x + y * width] = b;
				else if (t == 1) fill(x, y, b, x0, y0, x1, y1);
				else if (t == 2) replace(x, y, b, x0, y0, x1, y1);
				else if (t == 4) move(x, y, true, x0, y0, x1, y1);
				else if (t == 5) move(x, y, false, x0, y0, x1, y1);
				this.markUpdate();
			}
		} else if (cmd == GuiTextureMaker.CMD_Value) {
			byte i = dis.readByte();
			int v = dis.readInt();
			if (v < 0) v = 0;
			if (i < 2 && v > 32) v = 32;
			if (i == 0) this.resize(v, height);
			else if (i == 1) this.resize(width, v);
			else if (i == 2) netI1 = v;
			else if (i == 3) netI2 = v;
		} else if (cmd == GuiTextureMaker.CMD_Name) {
			String name = dis.readStringFromBuffer(64);
			if (inventory.items[0] != null && inventory.items[0].getItem() instanceof ItemBuilderTexture && inventory.items[0].getTagCompound() != null)
			{
				inventory.items[0].getTagCompound().setString("name", name);
			}
		}
	}
	
	private void load()
	{
		if (inventory.items[0] == null) return;
		boolean validItem = inventory.items[0].getItem() instanceof ItemBuilderTexture;
		if (inventory.items[0].getItem() == Items.PAPER || (validItem && inventory.items[0].getTagCompound() == null)) {
			drawing = new byte[0];
			width = 0;
			height = 0;
			netI0 = 0;
			netI1 = 0;
			netI2 = 0;
			for (int i = 1; i < inventory.items.length; i++) inventory.items[i] = null;
			this.markUpdate();
		} else if (validItem && inventory.items[0].getTagCompound() != null) {
			NBTTagCompound nbt = inventory.items[0].getTagCompound();
			drawing = nbt.getByteArray("data");
			width = nbt.getByte("width");
			netI0 = nbt.getByte("mode");
			netI1 = nbt.getShort("ofsX");
			netI2 = nbt.getShort("ofsY");
			if (drawing.length == 0) width = 0;
			height = (byte)(width == 0 ? 0 : drawing.length / width);
			ItemStack item = inventory.items[0];
			ItemFluidUtil.loadInventory(nbt.getTagList("def", 10), inventory.items);
			inventory.items[0] = item;
			this.markUpdate();
		}
	}
	
	private void save()
	{
		if (inventory.items[0] == null || !(inventory.items[0].getItem() instanceof ItemBuilderTexture || inventory.items[0].getItem() == Items.PAPER)) return;
		String name = inventory.items[0].getTagCompound() != null ? inventory.items[0].getTagCompound().getString("name") : "";
		int n = inventory.items[0].stackSize;
		inventory.items[0] = null;
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setString("name", name);
		nbt.setByteArray("data", drawing);
		nbt.setByte("width", width);
		nbt.setByte("mode", (byte)netI0);
		nbt.setShort("ofsX", (short)netI1);
		nbt.setShort("ofsY", (short)netI2);
		nbt.setTag("def", ItemFluidUtil.saveInventory(inventory.items));
		inventory.items[0] = new ItemStack(Objects.builderTexture, n);
		inventory.items[0].setTagCompound(nbt);
	}
	
	private void resize(int x, int y)
	{
		if (x == width && y == height) return;
		byte[] newDraw = new byte[x * y];
		int sx = width;
		int sy = height;
		if (sx > x) sx = x;
		if (sy > y) sy = y;
		for (int i = 0; i < sx; i++)
			for (int j = 0; j < sy; j++)
			{
				newDraw[i + j * x] = drawing[i + j * width];
			}
		drawing = newDraw;
		width = (byte)x;
		height = (byte)y;
		this.markUpdate();
	}
	
	private static enum Transformation {
		rotateR(true) {
			@Override
			public int[] target(int x, int y, int w, int h) {
				return new int[]{y, w - x - 1};
			}
		}, rotateL(true) {
			@Override
			public int[] target(int x, int y, int w, int h) {
				return new int[]{h - y - 1, x};
			}
		}, mirrorH(false) {
			@Override
			public int[] target(int x, int y, int w, int h) {
				return new int[]{w - x - 1, y};
			}
		}, mirrorV(false) {
			@Override
			public int[] target(int x, int y, int w, int h) {
				return new int[]{x, h - y - 1};
			}
		};
		private Transformation(boolean sq) {squareOnly = sq;}
		public final boolean squareOnly;
		public abstract int[] target(int x, int y, int w, int h);
	}
	
	private void transform(int x0, int y0, int x1, int y1, Transformation type)
	{
		byte[] buff = new byte[drawing.length];
		System.arraycopy(drawing, 0, buff, 0, buff.length);
		int w = x1 - x0, h = y1 - y0;
		if (w != h && type.squareOnly) return;
		int n0, n1;
		int[] p;
		for (int y = 0; y < h; y++)
			for (int x = 0; x < w; x++) {
				p = type.target(x, y, w, h);
				n0 = x + x0 + (y + y0) * width;
				n1 = p[0] + x0 + (p[1] + y0) * width;
				buff[n0] = drawing[n1];
			}
		drawing = buff;
	}
	
	private void fill(int x, int y, byte obj, int x0, int y0, int x1, int y1)
	{
		if (x < x0 || y < y0 || x >= x1 || y >= y1) return;
		int n = x + y * width;
		int pre = drawing[n];
		if (pre == obj) return;
		drawing[n] = obj;
		if (x >= 1 && drawing[n - 1] == pre) fill(x - 1, y, obj, x0, y0, x1, y1);
		if (x < width - 1 && drawing[n + 1] == pre) fill(x + 1, y, obj, x0, y0, x1, y1);
		if (y >= 1 && drawing[n - width] == pre) fill(x, y - 1, obj, x0, y0, x1, y1);
		if (y < height - 1 && drawing[n + width] == pre) fill(x, y + 1, obj, x0, y0, x1, y1);
	}
	
	private void replace(int x, int y, byte obj, int x0, int y0, int x1, int y1)
	{
		int n = x + y * width;
		int pre = drawing[n];
		if (pre == obj) return;
		for (y = y0; y < y1; y++)
			for (x = x0; x < x1; x++)
				if (drawing[n = x + y * width] == pre) drawing[n] = obj;
	}
	
	private void move(int dx, int dy, boolean copy, int x0, int y0, int x1, int y1)
	{
		int n0, n1, nx, ny;
		boolean ox = dx < 0, oy = dy < 0;
		for (int y = oy ? y0 : y1 - 1; oy ? y < y1 : y >= y0; y += oy ? 1 : -1)
			for (int x = ox ? x0 : x1 - 1; ox ? x < x1 : x >= x0; x += ox ? 1 : -1) {
				nx = x + dx; ny = y + dy;
				n0 = x + y * width;
				if (nx >= 0 && ny >= 0 && nx < width && ny < height) {
					n1 = nx + ny * width;
					drawing[n1] = drawing[n0];
				}
				if (!copy) drawing[n0] = 0;
			}
	}
	
	public byte getPixel(int x, int y)
	{
		if (x < 0 || y < 0 || x >= width || y >= height) return 0;
		else return drawing[x + y * width];
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) 
	{
		super.readFromNBT(nbt);
		drawing = nbt.getByteArray("drawing");
		width = nbt.getByte("width");
		netI0 = nbt.getByte("mode");
		netI1 = nbt.getShort("ofsX");
		netI2 = nbt.getShort("ofsY");
		if (drawing.length == 0) width = 0;
		height = (byte)(width == 0 ? 0 : drawing.length / width);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) 
	{
		nbt.setByteArray("drawing", drawing);
		nbt.setByte("width", width);
		nbt.setByte("mode", (byte)netI0);
		nbt.setShort("ofsX", (short)netI1);
		nbt.setShort("ofsY", (short)netI2);
		return super.writeToNBT(nbt);
	}

	@Override
	public void initContainer(DataContainer cont) {
		TileContainer container = (TileContainer)cont;
		cont.refInts = new int[3];
		container.addItemSlot(new SlotItemHandler(inventory, 0, 178, 302));
		for (int i = 0; i < 15; i++)
			container.addItemSlot(new SlotHolo(inventory, i + 1, 264, 8 + i * 16, false, false));
		container.addPlayerInventory(8, 264);
	}

	@Override
	public int[] getSyncVariables() {
		return new int[]{netI0, netI1, netI2};
	}

	@Override
	public void setSyncVariable(int i, int v) {
		switch(i) {
		case 0: netI0 = v; break;
		case 1: netI1 = v; break;
		case 2: netI2 = v; break;
		}
	}

}
