package cd4017be.automation.Item;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import cd4017be.api.automation.IOperatingArea;
import cd4017be.automation.Automation;
import cd4017be.automation.TileEntity.Builder;
import cd4017be.automation.TileEntity.Miner;
import cd4017be.automation.TileEntity.Pump;
import cd4017be.lib.DefaultItem;
import cd4017be.lib.ModTileEntity;

public class ItemMachineSynchronizer extends DefaultItem {

	public static String[] types = {"Unlinked", "Linked", "Pump", "Miner", "Builder"};

	public ItemMachineSynchronizer(String id) {
		super(id);
		this.setCreativeTab(Automation.tabAutomation);
		this.setMaxStackSize(1);
		this.setHasSubtypes(true);
	}

	@Override
	public EnumActionResult onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing s, float X, float Y, float Z) {
		if (player.isSneaking()) {
			TileEntity te = world.getTileEntity(pos);
			if (te == null || !(te instanceof IOperatingArea)) return EnumActionResult.SUCCESS;
			if (world.isRemote) return EnumActionResult.SUCCESS;
			int m;
			if (te instanceof Pump) m = 2;
			else if (te instanceof Miner) m = 3;
			else if (te instanceof Builder) m = 4;
			else m = 1;
			stack = new ItemStack(this, stack.stackSize, m);
			stack.setTagCompound(new NBTTagCompound());
			stack.getTagCompound().setInteger("lx", pos.getX());
			stack.getTagCompound().setInteger("ly", pos.getY());
			stack.getTagCompound().setInteger("lz", pos.getZ());
			player.setHeldItem(hand, stack);
			player.addChatMessage(new TextComponentString("Linked to machine"));
			return EnumActionResult.SUCCESS;
		}
		return EnumActionResult.PASS;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(ItemStack item, World world, EntityPlayer player, EnumHand hand) {
		if (!player.isSneaking() || item.getItemDamage() == 0) return new ActionResult<ItemStack>(EnumActionResult.FAIL, item);
		if (!world.isRemote) player.addChatMessage(new TextComponentString("Link cleared"));
		return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, new ItemStack(this, item.stackSize, 0));
	}

	@Override
	public String getItemStackDisplayName(ItemStack item) {
		return super.getItemStackDisplayName(item);
	}

	@Override
	public void addInformation(ItemStack item, EntityPlayer par2EntityPlayer, List<String> list, boolean par4) {
		if (item.getTagCompound() != null) {
			list.add("Linked to (" + item.getTagCompound().getInteger("lx")
					+ "|" + item.getTagCompound().getInteger("ly")
					+ "|" + item.getTagCompound().getInteger("lz") + ")");
		}
		super.addInformation(item, par2EntityPlayer, list, par4);
	}

	public static IOperatingArea getLink(ItemStack item, ModTileEntity mach) {
		if (item == null || !(item.getItem() instanceof ItemMachineSynchronizer) || item.getTagCompound() == null) return null;
		int lx = item.getTagCompound().getInteger("lx");
		int ly = item.getTagCompound().getInteger("ly");
		int lz = item.getTagCompound().getInteger("lz");
		TileEntity te = mach.getLoadedTile(new BlockPos(lx, ly, lz));
		if (te != null && te instanceof IOperatingArea) {
			((IOperatingArea)te).remoteOperation(new BlockPos(0, -1, 0));
			return (IOperatingArea)te;
		}
		return null;
	}

}
