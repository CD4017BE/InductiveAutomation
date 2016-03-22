/*
 * To change this template, choose netData.ints[0]s | Templates
 * and open the template in the editor.
 */
package cd4017be.automation.TileEntity;

import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IChatComponent;

import java.io.IOException;

import cd4017be.automation.Objects;
import cd4017be.automation.Gui.GuiTextureMaker;
import cd4017be.automation.Item.ItemBuilderTexture;
import cd4017be.automation.Item.ItemFluidDummy;
import cd4017be.lib.ModTileEntity;
import cd4017be.lib.TileContainer;
import cd4017be.lib.TileEntityData;
import cd4017be.lib.templates.SlotHolo;
import cd4017be.lib.util.Utils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraftforge.fluids.FluidStack;

/**
 *
 * @author CD4017BE
 */
public class TextureMaker extends ModTileEntity implements ISidedInventory
{
    public ItemStack[] inventory = new ItemStack[16];
    public byte[] drawing = new byte[0];
    public byte width = 0;
    public byte height = 0;
    
    public TextureMaker()
    {
    	netData = new TileEntityData(0, 3, 0, 0);
    }

    @Override
    public Packet getDescriptionPacket() 
    {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setByteArray("tex", drawing);
        nbt.setByte("width", width);
        return new S35PacketUpdateTileEntity(getPos(), -1, nbt);
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) 
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
            else if (id == 6) netData.ints[0] = (netData.ints[0] + 1) % 4;
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
            	worldObj.markBlockForUpdate(getPos());
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
                worldObj.markBlockForUpdate(getPos());
            }
        } else if (cmd == GuiTextureMaker.CMD_Value) {
        	byte i = dis.readByte();
        	int v = dis.readInt();
        	if (v < 0) v = 0;
        	if (i < 2 && v > 32) v = 32;
        	if (i == 0) this.resize(v, height);
        	else if (i == 1) this.resize(width, v);
        	else if (i == 2) netData.ints[1] = v;
        	else if (i == 3) netData.ints[2] = v;
        } else if (cmd == GuiTextureMaker.CMD_Name) {
            String name = dis.readStringFromBuffer(64);
            if (inventory[0] != null && inventory[0].getItem() instanceof ItemBuilderTexture && inventory[0].getTagCompound() != null)
            {
                inventory[0].getTagCompound().setString("name", name);
            }
        }
    }
    
    private void load()
    {
        if (inventory[0] == null) return;
        boolean validItem = inventory[0].getItem() instanceof ItemBuilderTexture;
        if (inventory[0].getItem() == Items.paper || (validItem && inventory[0].getTagCompound() == null)) {
        	drawing = new byte[0];
        	width = 0;
        	height = 0;
        	netData.ints[0] = 0;
        	netData.ints[1] = 0;
        	netData.ints[2] = 0;
        	for (int i = 1; i < inventory.length; i++) inventory[i] = null;
        	worldObj.markBlockForUpdate(getPos());
        } else if (validItem && inventory[0].getTagCompound() != null) {
        	NBTTagCompound nbt = inventory[0].getTagCompound();
        	drawing = nbt.getByteArray("data");
        	width = nbt.getByte("width");
        	netData.ints[0] = nbt.getByte("mode");
            netData.ints[1] = nbt.getShort("ofsX");
            netData.ints[2] = nbt.getShort("ofsY");
        	if (drawing.length == 0) width = 0;
            height = (byte)(width == 0 ? 0 : drawing.length / width);
            ItemStack item = inventory[0];
            inventory = this.readItemsFromNBT(nbt, "def", inventory.length);
            inventory[0] = item;
            worldObj.markBlockForUpdate(getPos());
        }
    }
    
    private void save()
    {
    	if (inventory[0] == null || !(inventory[0].getItem() instanceof ItemBuilderTexture || inventory[0].getItem() == Items.paper)) return;
    	String name = inventory[0].getTagCompound() != null ? inventory[0].getTagCompound().getString("name") : "";
    	int n = inventory[0].stackSize;
    	inventory[0] = null;
    	NBTTagCompound nbt = new NBTTagCompound();
    	nbt.setString("name", name);
    	nbt.setByteArray("data", drawing);
        nbt.setByte("width", width);
        nbt.setByte("mode", (byte)netData.ints[0]);
        nbt.setShort("ofsX", (short)netData.ints[1]);
        nbt.setShort("ofsY", (short)netData.ints[2]);
        this.writeItemsToNBT(nbt, "def", inventory);
        inventory[0] = new ItemStack(Objects.builderTexture, n);
        inventory[0].setTagCompound(nbt);
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
        worldObj.markBlockForUpdate(getPos());
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
        inventory = this.readItemsFromNBT(nbt, "Items", inventory.length);
        drawing = nbt.getByteArray("drawing");
        width = nbt.getByte("width");
        netData.ints[0] = nbt.getByte("mode");
        netData.ints[1] = nbt.getShort("ofsX");
        netData.ints[2] = nbt.getShort("ofsY");
        if (drawing.length == 0) width = 0;
        height = (byte)(width == 0 ? 0 : drawing.length / width);
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) 
    {
        super.writeToNBT(nbt);
        this.writeItemsToNBT(nbt, "Items", inventory);
        nbt.setByteArray("drawing", drawing);
        nbt.setByte("width", width);
        nbt.setByte("mode", (byte)netData.ints[0]);
        nbt.setShort("ofsX", (short)netData.ints[1]);
        nbt.setShort("ofsY", (short)netData.ints[2]);
    }
    
    @Override
    public int[] getSlotsForFace(EnumFacing s) 
    {
        return new int[0];
    }

    @Override
    public boolean canInsertItem(int i, ItemStack item, EnumFacing s) 
    {
        return false;
    }

    @Override
    public boolean canExtractItem(int i, ItemStack item, EnumFacing s) 
    {
        return false;
    }

    @Override
    public boolean isItemValidForSlot(int i, ItemStack item) 
    {
        return true;
    }

    @Override
    public int getSizeInventory() 
    {
        return inventory.length;
    }

    @Override
    public ItemStack getStackInSlot(int id) 
    {
        return inventory[id];
    }

    @Override
    public ItemStack decrStackSize(int id, int n) 
    {
        if (inventory[id] == null) return null;
        else if (inventory[id].stackSize <= n)
        {
            ItemStack item = inventory[id];
            inventory[id] = null;
            return item;
        } else
        return inventory[id].splitStack(n);
    }

    @Override
    public ItemStack removeStackFromSlot(int id) 
    {
        ItemStack item = inventory[id];
        inventory[id] = null;
        if (id == 0) return item;
        else return null;
    }

    @Override
    public void setInventorySlotContents(int id, ItemStack item) 
    {
        inventory[id] = item;
        if (id > 0 && item != null) {
        	FluidStack fluid = Utils.getFluid(item);
        	if (fluid != null) inventory[id] = ItemFluidDummy.getFluidContainer(fluid.getFluid().getID(), 1);
        }
    }

    @Override
    public int getInventoryStackLimit() 
    {
        return 64;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer var1) 
    {
        return true;
    }
    
    @Override
    public void initContainer(TileContainer container)
    {
        container.addEntitySlot(new Slot(this, 0, 178, 302));
        for (int i = 0; i < 15; i++)
        	container.addEntitySlot(new SlotHolo(this, i + 1, 264, 8 + i * 16, false, false));
        container.addPlayerInventory(8, 264);
    }

	@Override
	public String getName() {
        return "TextureMaker";
	}

	@Override
	public boolean hasCustomName() {
		return true;
	}

	@Override
	public void openInventory(EntityPlayer player) {}

	@Override
	public void closeInventory(EntityPlayer player) {}

	@Override
	public int getField(int id) {
		return 0;
	}

	@Override
	public void setField(int id, int value) {
	}

	@Override
	public int getFieldCount() {
		return 0;
	}

	@Override
	public void clear() {
	}

	@Override
	public IChatComponent getDisplayName() {
		return new ChatComponentText(this.getName());
	}
    
}
