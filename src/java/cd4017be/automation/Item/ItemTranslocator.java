package cd4017be.automation.Item;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import cd4017be.automation.Automation;
import cd4017be.lib.DefaultItem;
import cd4017be.lib.MovedBlock;

public class ItemTranslocator extends DefaultItem
{

	public ItemTranslocator(String id) 
	{
		super(id);
        this.setCreativeTab(Automation.tabAutomation);
        this.setMaxStackSize(1);
	}
	
	@Override
	public String getItemStackDisplayName(ItemStack item) 
	{
		if (item.getTagCompound() == null) {
			return super.getItemStackDisplayName(item) + " (Empty)";
		} else {
			Block block = Block.getBlockById(item.getTagCompound().getShort("id"));
			return super.getItemStackDisplayName(item) + " (" + block.getLocalizedName() + ")";
		}
	}

	@Override
	public boolean onItemUse(ItemStack item, EntityPlayer player, World world, BlockPos pos, EnumFacing s, float X, float Y, float Z) 
	{
		if (item.getTagCompound() == null) {
			if (item.stackSize == 0) return false;
	        else if (!player.canPlayerEdit(pos, s, item)) return false;
	        else if (world.isRemote) return true;
			MovedBlock pickup = MovedBlock.get(world, pos);
			if ((new MovedBlock(Blocks.air.getDefaultState(), null)).set(world, pos)) {
				world.playSoundEffect((double)((float)pos.getX() + 0.5F), (double)((float)pos.getY() + 0.5F), (double)((float)pos.getZ() + 0.5F), pickup.block.getBlock().stepSound.getPlaceSound(), (pickup.block.getBlock().stepSound.getVolume() + 1.0F) / 2.0F, pickup.block.getBlock().stepSound.getFrequency() * 0.8F);
				item.setTagCompound(new NBTTagCompound());
				item.getTagCompound().setShort("id", (short)Block.getStateId(pickup.block));
				if (pickup.nbt != null) item.getTagCompound().setTag("data", pickup.nbt);
			}
			return true;
		} else {
			IBlockState obj = Block.getStateById(item.getTagCompound().getShort("id"));
	        if (obj == Blocks.air) return false;
	        IBlockState state = world.getBlockState(pos);
	        Block block = state.getBlock();
	        if (state == Blocks.snow_layer.getDefaultState()) s = EnumFacing.UP;
	        else if (block != Blocks.vine && block != Blocks.tallgrass && block != Blocks.deadbush && !block.isReplaceable(world, pos)) {
	            pos = pos.offset(s);
	        }
	        if (item.stackSize == 0) return false;
	        else if (!player.canPlayerEdit(pos, s, item)) return false;
	        else if (pos.getY() == 255 && obj.getBlock().getMaterial().isSolid()) return false;
	        else if (world.canBlockBePlaced(obj.getBlock(), pos, false, s, player, item)) {
	        	MovedBlock placement = new MovedBlock(obj, item.getTagCompound().hasKey("data") ? item.getTagCompound().getCompoundTag("data") : null);
	            if (placement.set(world, pos)) {
	                world.playSoundEffect((double)((float)pos.getX() + 0.5F), (double)((float)pos.getY() + 0.5F), (double)((float)pos.getZ() + 0.5F), obj.getBlock().stepSound.getPlaceSound(), (obj.getBlock().stepSound.getVolume() + 1.0F) / 2.0F, obj.getBlock().stepSound.getFrequency() * 0.8F);
	                item.setTagCompound(null);
	            }
	            return true;
	        } else return false;
		}
	}

}
