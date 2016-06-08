package cd4017be.automation.TileEntity;

import net.minecraft.network.PacketBuffer;

import java.io.IOException;
import java.util.ArrayList;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import cd4017be.lib.TileContainer;
import cd4017be.lib.TileEntityData;
import cd4017be.lib.templates.AutomatedTile;
import cd4017be.lib.templates.Inventory;
import cd4017be.lib.templates.SlotItemType;
import cd4017be.lib.util.VecN;

public class VertexShematicGen extends AutomatedTile
{

	public VertexShematicGen() 
	{
		netData = new TileEntityData(1, 0, 0, 0);
		inventory = new Inventory(this, 1);
	}

	public ArrayList<Polygon> polygons = new ArrayList<Polygon>();
	public String name = "";
	public short sel;
	
	public void load(NBTTagCompound nbt)
	{
		NBTTagList list = nbt.getTagList("pol", 10);
		polygons = new ArrayList<Polygon>(list.tagCount());
		for (int i = 0; i < list.tagCount(); i++) polygons.add(new Polygon(list.getCompoundTagAt(i)));
	}
		
	public void save(NBTTagCompound nbt) 
	{
		NBTTagList list = new NBTTagList();
		for (Polygon p : polygons) list.appendTag(p.save());
		nbt.setTag("pol", list);
	}
	
	public static class Polygon {
		public VecN[] vert;
		public byte texId;
		public short thick;
		public byte dir;
		
		public Polygon()
		{
			vert = new VecN[0];
			texId = 0;
			thick = 1;
			dir = 0;
		}
		
		public Polygon(NBTTagCompound nbt)
		{
			dir = nbt.getByte("dir");
			texId = nbt.getByte("tex");
			thick = nbt.getShort("th");
			vert = new VecN[(int)nbt.getByte("vn") & 0xff];
			double[] d;
			for (int i = 0; i < vert.length; i++) {
				d = new double[5];
				for (int j = 0; j < d.length; j++) {
					d[j] = nbt.getFloat("v" + Integer.toHexString(i) + j);
				}
				vert[i] = new VecN(d);
			}
		}
		
		public NBTTagCompound save()
		{
			NBTTagCompound nbt = new NBTTagCompound();
			nbt.setByte("dir", dir);
			nbt.setByte("tex", texId);
			nbt.setShort("th", thick);
			nbt.setByte("vn", (byte)vert.length);
			for (int i = 0; i < vert.length; i++) {
				for (int j = 0; j < vert[i].x.length; j++) {
					nbt.setFloat("v" + Integer.toHexString(i) + j, (float)vert[i].x[j]);
				}
			}
			return nbt;
		}
		
		public void addVertex(int s)
		{
			if (s < 0) s = 0;
			if (s > vert.length) s = vert.length;
			VecN[] buff = new VecN[vert.length + 1];
			buff[s] = new VecN(5);
			if (s > 0) System.arraycopy(vert, 0, buff, 0, s);
			if (s < vert.length) System.arraycopy(vert, s, buff, s + 1, vert.length - s);
			vert = buff;
		}
		
		public void remVertex(int s)
		{
			if (s < 0) s = 0;
			if (s >= vert.length) s = vert.length;
			VecN[] buff = new VecN[vert.length - 1];
			if (s > 0) System.arraycopy(vert, 0, buff, 0, s);
			if (s < vert.length - 1) System.arraycopy(vert, s + 1, buff, s, vert.length - s - 1);
			vert = buff;
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) 
	{
		super.readFromNBT(nbt);
		this.load(nbt);
		name = nbt.getString("name");
		sel = nbt.getShort("sel");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) 
	{
		this.save(nbt);
		nbt.setString("name", name);
		nbt.setShort("sel", sel);
		return super.writeToNBT(nbt);
	}

	@Override
	protected void customPlayerCommand(byte cmd, PacketBuffer dis, EntityPlayerMP player) throws IOException 
	{
		if (cmd == 0) {//load add
			if (inventory.items[0] == null || inventory.items[0].getTagCompound() == null) return;
			NBTTagList list = inventory.items[0].getTagCompound().getTagList("pol", 10);
			for (int i = 0; i < list.tagCount(); i++) polygons.add(new Polygon(list.getCompoundTagAt(i)));
			this.name = inventory.items[0].getDisplayName();
		} else if (cmd == 1) {//save
			if (inventory.items[0] == null) return;
			inventory.items[0].setTagCompound(new NBTTagCompound());
			if (inventory.items[0].getItem() == Items.BOOK) this.save(inventory.items[0].getTagCompound());
			else if (inventory.items[0].getItem() == Items.PAPER) this.triangulate(inventory.items[0].getTagCompound());
			inventory.items[0].setStackDisplayName(name);
			NBTTagCompound tag = inventory.items[0].getTagCompound().getCompoundTag("display");
			NBTTagList info = new NBTTagList();
			info.appendTag(new NBTTagString(inventory.items[0].getTagCompound().getTagList("pol", 10).tagCount() + (inventory.items[0].getItem() == Items.BOOK ? " Polygons" : " Triangles")));
			tag.setTag("Lore", info);
			inventory.items[0].getTagCompound().setTag("ench", new NBTTagList());
			return;
		} else if (cmd == 2) {//save selected
			int sel = (int)dis.readShort();
			if (inventory.items[0] == null || inventory.items[0].getTagCompound() == null || inventory.items[0].getItem() != Items.BOOK || sel < 0 || sel >= polygons.size()) return;
			NBTTagList list = inventory.items[0].getTagCompound().getTagList("pol", 10);
			list.appendTag(polygons.get(sel).save());
			return;
		} else if (cmd == 3) {//clear
			sel = dis.readShort();
			if (sel < 0) polygons.clear();
			else if (sel < polygons.size()) polygons.remove(sel);
		} else if (cmd == 4) {//new
			polygons.add(new Polygon());
		} else if (cmd == 5) {//add
			int sel1 = dis.readShort();
			int sel2 = dis.readByte();
			if (sel1 < 0 || sel1 >= polygons.size()) return;
			Polygon p = polygons.get(sel1);
			p.addVertex(sel2);
		} else if (cmd == 6) {//del
			sel = dis.readShort();
			int sel2 = dis.readByte();
			if (sel < 0 || sel >= polygons.size()) return;
			Polygon p = polygons.get(sel);
			p.remVertex(sel2);
		} else if (cmd == 7) {//texId
			sel = dis.readShort();
			if (sel < 0 || sel >= polygons.size()) return;
			Polygon p = polygons.get(sel);
			p.texId = (byte)(dis.readByte() & 0x7);
		} else if (cmd == 8) {//extDir
			sel = dis.readShort();
			if (sel < 0 || sel >= polygons.size()) return;
			Polygon p = polygons.get(sel);
			p.dir = (byte)(dis.readByte() % 6);
		} else if (cmd == 9) {//extAm
			sel = dis.readShort();
			if (sel < 0 || sel >= polygons.size()) return;
			Polygon p = polygons.get(sel);
			p.thick = dis.readShort();
		} else if (cmd == 10) {//set Vertex
			sel = dis.readShort();
			int sel2 = dis.readByte();
			int var = dis.readByte();
			if (sel < 0 || sel >= polygons.size()) return;
			Polygon p = polygons.get(sel);
			if (sel2 < 0 || sel2 >= p.vert.length || var < 0 || var >= 5) return;
			p.vert[sel2].x[var] = dis.readFloat();
		} else if (cmd == 11) {//name
			this.name = dis.readStringFromBuffer(64);
		} else if (cmd == 12) {//sel
			this.sel = dis.readShort();
		} else if (cmd == 13) {//auto tex
			this.sel = dis.readShort();
			if (sel < 0 || sel >= polygons.size()) return;
			Polygon p = polygons.get(sel);
			if (p.dir == 0 || p.dir == 1) {
				for (int i = 0; i < p.vert.length; i++) {
					p.vert[i].x[3] = p.vert[i].x[0];
					p.vert[i].x[4] = p.vert[i].x[2];
				}
			} else if (p.dir == 2 || p.dir == 3) {
				for (int i = 0; i < p.vert.length; i++) {
					p.vert[i].x[3] = p.vert[i].x[0];
					p.vert[i].x[4] = p.vert[i].x[1];
				}
			} else {
				for (int i = 0; i < p.vert.length; i++) {
					p.vert[i].x[3] = p.vert[i].x[2];
					p.vert[i].x[4] = p.vert[i].x[1];
				}
			}
		}
		this.markUpdate();
	}
		
	public void triangulate(NBTTagCompound nbt)
	{
		NBTTagList list = new NBTTagList();
		Polygon pol;
		for (Polygon p : polygons) {
			for (int i = 0; i <= p.vert.length - 3; i++) {
				pol = new Polygon();
				pol.dir = p.dir; pol.texId = p.texId; pol.thick = p.thick;
				pol.vert = new VecN[]{p.vert[0], p.vert[i + 1], p.vert[i + 2]};
				list.appendTag(pol.save());
			}
		}
		nbt.setTag("pol", list);
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox() 
	{
		return TileEntity.INFINITE_EXTENT_AABB;
	}

	@Override
	public double getMaxRenderDistanceSquared() 
	{
		return 65536D;
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) 
	{
		this.load(pkt.getNbtCompound());
		this.name = pkt.getNbtCompound().getString("name");
		this.sel = pkt.getNbtCompound().getShort("sel");
	}

	@Override
	public SPacketUpdateTileEntity getUpdatePacket() 
	{
		NBTTagCompound nbt = new NBTTagCompound();
		this.save(nbt);
		nbt.setString("name", name);
		nbt.setShort("sel", sel);
		return new SPacketUpdateTileEntity(pos, -1, nbt);
	}

	@Override
	public void initContainer(TileContainer container) 
	{
		container.addEntitySlot(new SlotItemType(this, 0, 152, 51, new ItemStack(Items.PAPER), new ItemStack(Items.BOOK)));
		
		container.addPlayerInventory(8, 97);
	}
	
}
