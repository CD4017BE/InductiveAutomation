package cd4017be.automation.Item;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import cd4017be.automation.Automation;
import cd4017be.lib.DefaultItem;
import cd4017be.lib.MovedBlock;

public class ItemTranslocator extends DefaultItem
{

	public ItemTranslocator(String id, String tex) 
	{
		super(id);
		this.setTextureName(tex);
        this.setCreativeTab(Automation.tabAutomation);
        this.setMaxStackSize(1);
	}
	
	@Override
	public String getItemStackDisplayName(ItemStack item) 
	{
		if (item.stackTagCompound == null) {
			return super.getItemStackDisplayName(item) + " (Empty)";
		} else {
			Block block = Block.getBlockById(item.stackTagCompound.getShort("id"));
			return super.getItemStackDisplayName(item) + " (" + block.getLocalizedName() + ")";
		}
	}

	@Override
	public boolean onItemUse(ItemStack item, EntityPlayer player, World world, int x, int y, int z, int s, float X, float Y, float Z) 
	{
		if (item.stackTagCompound == null) {
			if (item.stackSize == 0) return false;
	        else if (!player.canPlayerEdit(x, y, z, s, item)) return false;
	        else if (world.isRemote) return true;
			MovedBlock pickup = MovedBlock.get(world, x, y, z);
			if ((new MovedBlock(Blocks.air, 0, null)).set(world, x, y, z)) {
				world.playSoundEffect((double)((float)x + 0.5F), (double)((float)y + 0.5F), (double)((float)z + 0.5F), pickup.blockId.stepSound.func_150496_b(), (pickup.blockId.stepSound.getVolume() + 1.0F) / 2.0F, pickup.blockId.stepSound.getPitch() * 0.8F);
				item.stackTagCompound = new NBTTagCompound();
				item.stackTagCompound.setShort("id", (short)Block.getIdFromBlock(pickup.blockId));
				item.stackTagCompound.setByte("m", pickup.metadata);
				if (pickup.nbt != null) item.stackTagCompound.setTag("data", pickup.nbt);
			}
			return true;
		} else {
			Block obj = Block.getBlockById(item.stackTagCompound.getShort("id"));
	        if (obj == Blocks.air) return false;
	        Block block = world.getBlock(x, y, z);
	        if (block == Blocks.snow_layer && (world.getBlockMetadata(x, y, z) & 7) < 1) s = 1;
	        else if (block != Blocks.vine && block != Blocks.tallgrass && block != Blocks.deadbush && !block.isReplaceable(world, x, y, z)) {
	            if (s == 0) --y;
	            if (s == 1) ++y;
	            if (s == 2) --z;
	            if (s == 3) ++z;
	            if (s == 4) --x;
	            if (s == 5) ++x;
	        }
	        if (item.stackSize == 0) return false;
	        else if (!player.canPlayerEdit(x, y, z, s, item)) return false;
	        else if (y == 255 && obj.getMaterial().isSolid()) return false;
	        else if (world.canPlaceEntityOnSide(obj, x, y, z, false, s, player, item)) {
	        	MovedBlock placement = new MovedBlock(obj, item.stackTagCompound.getByte("m"), item.stackTagCompound.hasKey("data") ? item.stackTagCompound.getCompoundTag("data") : null);
	            if (placement.set(world, x, y, z)) {
	                world.playSoundEffect((double)((float)x + 0.5F), (double)((float)y + 0.5F), (double)((float)z + 0.5F), obj.stepSound.func_150496_b(), (obj.stepSound.getVolume() + 1.0F) / 2.0F, obj.stepSound.getPitch() * 0.8F);
	                item.stackTagCompound = null;
	            }
	            return true;
	        } else return false;
		}
	}

}
