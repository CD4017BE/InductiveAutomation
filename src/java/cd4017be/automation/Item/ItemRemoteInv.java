package cd4017be.automation.Item;

import java.io.IOException;
import java.util.List;

import cd4017be.automation.Automation;
import cd4017be.automation.Gui.ContainerRemoteInventory;
import cd4017be.automation.Gui.GuiRemoteInventory;
import cd4017be.lib.BlockGuiHandler;
import cd4017be.lib.DefaultItem;
import cd4017be.lib.IGuiItem;
import cd4017be.lib.util.Utils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

public class ItemRemoteInv extends DefaultItem implements IGuiItem 
{

	public ItemRemoteInv(String id) 
	{
		super(id);
        this.setCreativeTab(Automation.tabAutomation);
        this.setMaxStackSize(1);
	}

	@Override
	public Container getContainer(World world, EntityPlayer player, int x, int y, int z) 
	{
		return new ContainerRemoteInventory(player);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public GuiContainer getGui(World world, EntityPlayer player, int x, int y, int z) 
	{
		return new GuiRemoteInventory(new ContainerRemoteInventory(player));
	}

	@Override
	public void onPlayerCommand(World world, EntityPlayer player, PacketBuffer dis) throws IOException 
	{
		if (player.openContainer != null && player.openContainer instanceof ContainerRemoteInventory) ((ContainerRemoteInventory)player.openContainer).onPlayerCommand(world, player, dis);
	}

	@Override
	public void addInformation(ItemStack item, EntityPlayer player, List list, boolean b) 
	{
		super.addInformation(item, player, list, b);
	}

	@Override
	public boolean onItemUse(ItemStack item, EntityPlayer player, World world, BlockPos pos, EnumFacing s, float X, float Y, float Z) 
	{
		if (!world.isRemote && player.isSneaking()) {
			TileEntity te = world.getTileEntity(pos);
			if (te == null || !(te instanceof IInventory)) {
				player.addChatMessage(new TextComponentString("Block has no inventory!"));
				return true;
			}
			if (item.getTagCompound() == null) item.setTagCompound(new NBTTagCompound());
			item.getTagCompound().setInteger("x", pos.getX());
			item.getTagCompound().setInteger("y", pos.getY());
			item.getTagCompound().setInteger("z", pos.getZ());
			item.getTagCompound().setByte("s", (byte)s.getIndex());
			item.getTagCompound().setInteger("d", player.dimension);
			item.getTagCompound().setInteger("size", Utils.accessibleSlots((IInventory)te, s.getIndex()).length);
			player.addChatMessage(new TextComponentString("Block inventory linked"));
			return true;
		}
		return false;
	}

	@Override
	public ItemStack onItemRightClick(ItemStack item, World world, EntityPlayer player) 
	{
		if (!player.isSneaking()) BlockGuiHandler.openItemGui(player, world, 0, -1, 0);
        return item;
	}

	@Override
	public void onUpdate(ItemStack item, World world, Entity entity, int slot, boolean b) 
	{
		if (world.isRemote) return;
		if (entity instanceof EntityPlayer) {
			if (item.getTagCompound() == null) item.setTagCompound(new NBTTagCompound());
			int t = item.getTagCompound().getByte("t") + 1;
			if (t >= 20) {
				t = 0;
				EntityPlayer player = (EntityPlayer)entity;
				InventoryPlayer inv = player.inventory;
				ContainerRemoteInventory container = null;
				if (b && player.openContainer != null && player.openContainer instanceof ContainerRemoteInventory) container = (ContainerRemoteInventory)player.openContainer;
				if (container != null) container.filters.save(item);
				IInventory link = getLink(item);
				if (link != null) {
					PipeUpgradeItem in = item.getTagCompound().hasKey("fin") ? PipeUpgradeItem.load(item.getTagCompound().getCompoundTag("fin")) : null;
					PipeUpgradeItem out = item.getTagCompound().hasKey("fout") ? PipeUpgradeItem.load(item.getTagCompound().getCompoundTag("fout")) : null;
					byte s = item.getTagCompound().getByte("s");
					this.updateTransfer(link, s, inv, slot, in, out);
					item.getTagCompound().setInteger("size", Utils.accessibleSlots(link, s).length);
				} else item.getTagCompound().setInteger("size", 0);
				if (container != null) container.filters.load(item);
			}
			item.getTagCompound().setByte("t", (byte)t);
		}
	}
	
	public static IInventory getLink(ItemStack item)
	{
		if (item == null || item.getTagCompound() == null) return null;
		int x = item.getTagCompound().getInteger("x");
		int y = item.getTagCompound().getInteger("y");
		int z = item.getTagCompound().getInteger("z");
		int d = item.getTagCompound().getInteger("d");
		if (y < 0) return null;
		World world = DimensionManager.getWorld(d);
		if (world == null) return null;
		TileEntity tile = world.getTileEntity(new BlockPos(x, y, z));
		if (tile == null || !(tile instanceof IInventory) || tile.isInvalid()) return null;
		return (IInventory)tile;
	}
	
	private void updateTransfer(IInventory link, int side, InventoryPlayer inv, int slot, PipeUpgradeItem in, PipeUpgradeItem out)
	{
		if (in == null && out == null) return;
		int[] sl, si;
		sl = Utils.accessibleSlots(link, side);
		si = new int[inv.mainInventory.length - 1];
		for (int i = 0; i < slot; i++) si[i] = i;
		for (int i = slot; i < si.length; i++) si[i] = i + 1;
		if (in != null && (in.mode & 128) != 0) {
			in.mode |= 64;
			this.transferItems(inv, si, 0, link, sl, side, in);
		}
		if (out != null && (out.mode & 128) != 0) {
			out.mode |= 64;
			this.transferItems(link, sl, side, inv, si, 0, out);
		}
	}
	
	private void transferItems(IInventory srcI, int[] srcS, int sS, IInventory dstI, int[] dstS, int sD, PipeUpgradeItem filter)
	{
		boolean siS = srcI instanceof ISidedInventory;
		ISidedInventory srcSI = siS ? (ISidedInventory)srcI : null;
		ItemStack stack0;
		ItemStack[] rem;
		int n;
		for (int s : srcS) {
			stack0 = srcI.getStackInSlot(s);
			if (stack0 == null || (siS && !srcSI.canExtractItem(s, stack0, EnumFacing.VALUES[sS]))) continue;
			n = filter.getInsertAmount(stack0, dstI, dstS, false);
			if (n <= 0) continue;
			stack0 = srcI.decrStackSize(s, n);
 			rem = Utils.fill(dstI, sD, dstS, stack0);
 			if (rem.length > 0) {
 				stack0 = srcI.getStackInSlot(s);
 				if (stack0 == null) stack0 = rem[0];
 				else stack0.stackSize += rem[0].stackSize;
 				srcI.setInventorySlotContents(s, stack0);
 			}
		}
	}

}
