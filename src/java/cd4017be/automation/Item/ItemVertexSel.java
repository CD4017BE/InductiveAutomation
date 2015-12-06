package cd4017be.automation.Item;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
import cd4017be.automation.Automation;
import cd4017be.automation.TileEntity.VertexShematicGen;
import cd4017be.automation.TileEntity.VertexShematicGen.Polygon;
import cd4017be.lib.BlockGuiHandler;
import cd4017be.lib.DefaultItem;

public class ItemVertexSel extends DefaultItem {

	public ItemVertexSel(String id, String tex) {
		super(id);
		this.setTextureName(tex);
        this.setCreativeTab(Automation.tabAutomation);
        this.setMaxStackSize(1);
	}
	
	@Override
    public boolean onItemUse(ItemStack item, EntityPlayer player, World world, int x, int y, int z, int s, float X, float Y, float Z) 
	{
		if (world.isRemote) return true;
		if (item.stackTagCompound == null) item.stackTagCompound = new NBTTagCompound();
        int mx = item.stackTagCompound.getInteger("mx"), my = item.stackTagCompound.getInteger("my"), mz = item.stackTagCompound.getInteger("mz");
        byte mode = item.stackTagCompound.getByte("sel");
        if (mode <= 0 && player.isSneaking()) {
        	TileEntity te = world.getTileEntity(x, y, z);
            if (x != mx && y != my && z != mz && te != null && te instanceof VertexShematicGen) {
            	item.stackTagCompound.setInteger("mx", x);
            	item.stackTagCompound.setInteger("my", y);
            	item.stackTagCompound.setInteger("mz", z);
            	item.stackTagCompound.setShort("pol", ((VertexShematicGen)te).sel);
            	player.addChatMessage(new ChatComponentText("Linked"));
            }
            return true;
        }
        if (my < 0) return false;
        TileEntity te = world.getTileEntity(mx, my, mz);
        if (te == null || !(te instanceof VertexShematicGen)) {
        	item.stackTagCompound.setInteger("my", -1);
        	return false;
        }
        VertexShematicGen tile = (VertexShematicGen)te;
        if (mode <= 0) {
        	BlockGuiHandler.openGui(player, world, mx, my, mz);
        } else {
        	this.use(item, player, tile, (byte)(mode - 1), X + (float)x, Y + (float)y, Z + (float)z);
        }
        return true;
    }


    @Override
    public ItemStack onItemRightClick(ItemStack item, World world, EntityPlayer player) 
    {
        if (world.isRemote || item.stackTagCompound == null) return item;
        int x = item.stackTagCompound.getInteger("mx"), y = item.stackTagCompound.getInteger("my"), z = item.stackTagCompound.getInteger("mz");
        byte mode = item.stackTagCompound.getByte("sel");
        if (y < 0) return item;
        TileEntity te = world.getTileEntity(x, y, z);
        if (te == null || !(te instanceof VertexShematicGen)) {
        	item.stackTagCompound.setInteger("my", -1);
        	return item;
        }
        VertexShematicGen tile = (VertexShematicGen)te;
        if (player.isSneaking()) {
        	if (tile.sel >= 0 && tile.sel < tile.polygons.size()) {
        		mode++;
        		if (mode > tile.polygons.get(tile.sel).vert.length) mode = 0;
        		item.stackTagCompound.setShort("pol", tile.sel);
        	} else mode = 0;
        	item.stackTagCompound.setByte("sel", mode);
        	return item;
        }
        if (mode <= 0) {
        	BlockGuiHandler.openGui(player, world, x, y, z);
        } else {
        	this.use(item, player, tile, (byte)(mode - 1), (float)player.posX, (float)player.posY, (float)player.posZ);
        }
        return item;
    }
    
    private void use(ItemStack item, EntityPlayer player, VertexShematicGen tile, byte mode, float X, float Y, float Z)
    {
    	short pol = item.stackTagCompound.getShort("pol");
    	Polygon p = pol < 0 || pol >= tile.polygons.size() ? null : tile.polygons.get(pol);
    	if (mode >= p.vert.length) {
    		item.stackTagCompound.setByte("sel", (byte)0);
    		return;
    	}
    	int x = Math.round(X) - tile.xCoord, y = Math.round(Y) - tile.yCoord, z = Math.round(Z) - tile.zCoord;
    	p.vert[mode].x[0] = x;
    	p.vert[mode].x[1] = y;
    	p.vert[mode].x[2] = z;
    	tile.getWorldObj().markBlockForUpdate(tile.xCoord, tile.yCoord, tile.zCoord);
    	return;
    }
    
    @Override
    public String getItemStackDisplayName(ItemStack item) 
    {
        if (item.stackTagCompound == null) return super.getItemStackDisplayName(item);
        byte sel = item.stackTagCompound.getByte("sel");
        return super.getItemStackDisplayName(item) + " (" + (sel == 0 ? "open GUI" : sel - 1) + ")";
    }
    
    @Override
    public void addInformation(ItemStack item, EntityPlayer par2EntityPlayer, List list, boolean par4) 
    {
        if (item.stackTagCompound != null) {
            list.add("Link (" + item.stackTagCompound.getInteger("mx")
                    + "|" + item.stackTagCompound.getInteger("my")
                    + "|" + item.stackTagCompound.getInteger("mz") + ")");
            list.add("Polygon " + item.stackTagCompound.getShort("pol"));
        }
        super.addInformation(item, par2EntityPlayer, list, par4);
    }

}
