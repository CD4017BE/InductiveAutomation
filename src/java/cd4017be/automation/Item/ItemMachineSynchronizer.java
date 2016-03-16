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
import cd4017be.api.automation.IOperatingArea;
import cd4017be.automation.Automation;
import cd4017be.automation.TileEntity.Builder;
import cd4017be.automation.TileEntity.Miner;
import cd4017be.automation.TileEntity.Pump;
import cd4017be.lib.DefaultItem;
import cd4017be.lib.ModTileEntity;

public class ItemMachineSynchronizer extends DefaultItem 
{

	public static String[] types = {"Unlinked", "Linked", "Pump", "Miner", "Builder"};
	
	public ItemMachineSynchronizer(String id) 
	{
		super(id);
        this.setCreativeTab(Automation.tabAutomation);
        this.setMaxStackSize(1);
	}
	
	@Override
	 public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing s, float X, float Y, float Z) 
	{
        if (player.isSneaking()) {
        	TileEntity te = world.getTileEntity(pos);
        	if (te == null || !(te instanceof IOperatingArea)) return false;
        	if (world.isRemote) return true;
        	int m;
        	if (te instanceof Pump) m = 2;
        	else if (te instanceof Miner) m = 3;
        	else if (te instanceof Builder) m = 4;
        	else m = 1;
        	stack = new ItemStack(this, stack.stackSize, m);
        	stack.stackTagCompound = new NBTTagCompound();
        	stack.stackTagCompound.setInteger("lx", pos.getX());
        	stack.stackTagCompound.setInteger("ly", pos.getY());
        	stack.stackTagCompound.setInteger("lz", pos.getZ());
        	player.setCurrentItemOrArmor(0, stack);
        	player.addChatMessage(new ChatComponentText("Linked to machine"));
        	return true;
        }
        return false;
    }

    @Override
    public ItemStack onItemRightClick(ItemStack item, World world, EntityPlayer player) 
    {
        if (!player.isSneaking() || item.getItemDamage() == 0) return item;
        if (!world.isRemote) player.addChatMessage(new ChatComponentText("Link cleared"));
        return new ItemStack(this, item.stackSize, 0);
    }
    
    @Override
    public String getItemStackDisplayName(ItemStack item) 
    {
    	int d = item.getItemDamage();
        return super.getItemStackDisplayName(item) + " (" + types[d <= 0 || d >= types.length ? 0 : d] + ")";
    }
    
    @Override
    public void addInformation(ItemStack item, EntityPlayer par2EntityPlayer, List list, boolean par4) 
    {
        if (item.stackTagCompound != null) {
            list.add("Linked to (" + item.stackTagCompound.getInteger("lx")
                    + "|" + item.stackTagCompound.getInteger("ly")
                    + "|" + item.stackTagCompound.getInteger("lz") + ")");
        }
        super.addInformation(item, par2EntityPlayer, list, par4);
    }
    
    public static IOperatingArea getLink(ItemStack item, ModTileEntity mach)
    {
    	if (item == null || !(item.getItem() instanceof ItemMachineSynchronizer) || item.stackTagCompound == null) return null;
        int lx = item.stackTagCompound.getInteger("lx");
        int ly = item.stackTagCompound.getInteger("ly");
        int lz = item.stackTagCompound.getInteger("lz");
        TileEntity te = mach.getLoadedTile(new BlockPos(lx, ly, lz));
        if (te != null && te instanceof IOperatingArea) {
        	((IOperatingArea)te).remoteOperation(new BlockPos(0, -1, 0));
        	return (IOperatingArea)te;
        }
		return null;
    }

}
