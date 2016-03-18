package cd4017be.automation.Item;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import cd4017be.automation.Automation;
import cd4017be.automation.TileEntity.VertexShematicGen;
import cd4017be.automation.TileEntity.VertexShematicGen.Polygon;
import cd4017be.lib.BlockGuiHandler;
import cd4017be.lib.DefaultItem;

public class ItemVertexSel extends DefaultItem {

	public ItemVertexSel(String id) {
		super(id);
        this.setCreativeTab(Automation.tabAutomation);
        this.setMaxStackSize(1);
	}
	
	@Override
    public boolean onItemUse(ItemStack item, EntityPlayer player, World world, BlockPos pos, EnumFacing s, float X, float Y, float Z) 
	{
		if (world.isRemote) return true;
		if (item.getTagCompound() == null) item.setTagCompound(new NBTTagCompound());
        int mx = item.getTagCompound().getInteger("mx"), my = item.getTagCompound().getInteger("my"), mz = item.getTagCompound().getInteger("mz");
        byte mode = item.getTagCompound().getByte("sel");
        if (mode <= 0 && player.isSneaking()) {
        	TileEntity te = world.getTileEntity(pos);
            if (pos.getX() != mx && pos.getY() != my && pos.getZ() != mz && te != null && te instanceof VertexShematicGen) {
            	item.getTagCompound().setInteger("mx", pos.getX());
            	item.getTagCompound().setInteger("my", pos.getY());
            	item.getTagCompound().setInteger("mz", pos.getZ());
            	item.getTagCompound().setShort("pol", ((VertexShematicGen)te).sel);
            	player.addChatMessage(new ChatComponentText("Linked"));
            }
            return true;
        }
        if (my < 0) return false;
        TileEntity te = world.getTileEntity(new BlockPos(mx, my, mz));
        if (te == null || !(te instanceof VertexShematicGen)) {
        	item.getTagCompound().setInteger("my", -1);
        	return false;
        }
        VertexShematicGen tile = (VertexShematicGen)te;
        if (mode <= 0) {
        	BlockGuiHandler.openGui(player, world, mx, my, mz);
        } else {
        	this.use(item, player, tile, (byte)(mode - 1), X + (float)pos.getX(), Y + (float)pos.getY(), Z + (float)pos.getZ());
        }
        return true;
    }


    @Override
    public ItemStack onItemRightClick(ItemStack item, World world, EntityPlayer player) 
    {
        if (world.isRemote || item.getTagCompound() == null) return item;
        int x = item.getTagCompound().getInteger("mx"), y = item.getTagCompound().getInteger("my"), z = item.getTagCompound().getInteger("mz");
        byte mode = item.getTagCompound().getByte("sel");
        if (y < 0) return item;
        TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
        if (te == null || !(te instanceof VertexShematicGen)) {
        	item.getTagCompound().setInteger("my", -1);
        	return item;
        }
        VertexShematicGen tile = (VertexShematicGen)te;
        if (player.isSneaking()) {
        	if (tile.sel >= 0 && tile.sel < tile.polygons.size()) {
        		mode++;
        		if (mode > tile.polygons.get(tile.sel).vert.length) mode = 0;
        		item.getTagCompound().setShort("pol", tile.sel);
        	} else mode = 0;
        	item.getTagCompound().setByte("sel", mode);
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
    	short pol = item.getTagCompound().getShort("pol");
    	Polygon p = pol < 0 || pol >= tile.polygons.size() ? null : tile.polygons.get(pol);
    	if (mode >= p.vert.length) {
    		item.getTagCompound().setByte("sel", (byte)0);
    		return;
    	}
    	int x = Math.round(X) - tile.getPos().getX(), y = Math.round(Y) - tile.getPos().getY(), z = Math.round(Z) - tile.getPos().getZ();
    	p.vert[mode].x[0] = x;
    	p.vert[mode].x[1] = y;
    	p.vert[mode].x[2] = z;
    	tile.getWorld().markBlockForUpdate(tile.getPos());
    	return;
    }
    
    @Override
    public String getItemStackDisplayName(ItemStack item) 
    {
        if (item.getTagCompound() == null) return super.getItemStackDisplayName(item);
        byte sel = item.getTagCompound().getByte("sel");
        return super.getItemStackDisplayName(item) + " (" + (sel == 0 ? "open GUI" : sel - 1) + ")";
    }
    
    @Override
    public void addInformation(ItemStack item, EntityPlayer par2EntityPlayer, List list, boolean par4) 
    {
        if (item.getTagCompound() != null) {
            list.add("Link (" + item.getTagCompound().getInteger("mx")
                    + "|" + item.getTagCompound().getInteger("my")
                    + "|" + item.getTagCompound().getInteger("mz") + ")");
            list.add("Polygon " + item.getTagCompound().getShort("pol"));
        }
        super.addInformation(item, par2EntityPlayer, list, par4);
    }

}
