package cd4017be.automation.TileEntity;

import java.util.ArrayList;

import cd4017be.api.automation.IItemPipeCon;
import cd4017be.automation.Objects;
import cd4017be.automation.Block.BlockItemPipe;
import cd4017be.automation.Item.ItemItemUpgrade;
import cd4017be.automation.Item.PipeUpgradeItem;
import cd4017be.lib.templates.AutomatedTile;
import cd4017be.lib.templates.IPipe;
import cd4017be.lib.templates.Inventory;
import cd4017be.lib.util.ItemFluidUtil;
import cd4017be.lib.util.Utils;
import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

/**
 *
 * @author CD4017BE
 */
public class ItemPipe extends AutomatedTile implements IPipe {
	/** bits[0-13 (6+1)*2]: (side + total) * dir{0:none, 1:in, 2:out, 3:lock/both} */
	private short flow;
	private boolean updateCon = true;
	private PipeUpgradeItem filter = null;
	private Cover cover = null;

	public ItemPipe() {
		inventory = new Inventory(1, 1, null).group(0, 0, 1, Utils.ACC);
		inventory.sideCfg = 0x555;
	}

	@Override
	public void update() {
		if (worldObj.isRemote) return;
		int type = this.getBlockMetadata();
		if (updateCon) this.updateConnections(type);
		if ((flow & 0x3000) == 0x3000) this.transferItem(type);
	}

	private void updateConnections(int type) {
		EnumFacing dir;
		TileEntity te;
		ArrayList<ItemPipe> updateList = new ArrayList<ItemPipe>();
		int lHasIO = getFlowBit(6), nHasIO = 0, lDirIO, nDirIO;
		short lFlow = flow;
		for (int i = 0; i < 6; i++) {
			lDirIO = getFlowBit(i);
			if (lDirIO == 3) continue;
			dir = EnumFacing.VALUES[i];
			te = worldObj.getTileEntity(pos.offset(dir));
			if (te != null && te instanceof ItemPipe) {
				ItemPipe pipe = (ItemPipe)te;
				int pHasIO = pipe.getFlowBit(6);
				int pDirIO = pipe.getFlowBit(i ^ 1);
				if (pDirIO == 3) nDirIO = 3;
				else if ((nDirIO = pHasIO & ~pDirIO) == 3) 
					nDirIO = lHasIO == 1 && (lDirIO & 1) == 0 ? 2 : lHasIO == 2 && (lDirIO & 2) == 0 ? 1 : 0;
				setFlowBit(i, nDirIO);
				if (nDirIO != 3) nHasIO |= nDirIO;
				updateList.add(pipe);
			} else if (te != null && te instanceof IItemPipeCon) {
				byte d = ((IItemPipeCon)te).getItemConnectType(i^1);
				d = d == 1 ? 2 : d == 2 ? 1 : (byte)0;
				setFlowBit(i, d);
				nHasIO |= d;
			} else if (type != BlockItemPipe.ID_Transport && te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, dir.getOpposite())) {
				setFlowBit(i, type);
				nHasIO |= type;
			} else setFlowBit(i, 0);
		}
		setFlowBit(6, nHasIO);
		if (flow != lFlow) {
			this.markUpdate();
			for (ItemPipe pipe : updateList) {
				pipe.onNeighborBlockChange(this.getBlockType());
			}
		}
		updateCon = false;
	}

	private void transferItem(int type) {
		boolean rs = worldObj.isBlockPowered(getPos());
		int d;
		EnumFacing dir;
		if (type == BlockItemPipe.ID_Transport || (filter != null && !filter.active(rs)));
		else if (type == BlockItemPipe.ID_Extraction) {
			for (int i = 0; i < 6; i++) {
				d = this.getFlowBit(i);
				if (d != 1) continue;
				dir = EnumFacing.VALUES[i];
				ICapabilityProvider te = this.getTileOnSide(dir);
				if (te == null || te instanceof ItemPipe) continue;
				IItemHandler acc = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, dir.getOpposite());
				if (acc != null) inventory.items[0] = this.extract(acc, inventory.items[0]);
				else updateCon = true;
			}
		} else if (inventory.items[0] != null) {
			for (int i = 0; i < 6; i++) {
				d = this.getFlowBit(i);
				if (d != 2) continue;
				dir = EnumFacing.VALUES[i];
				ICapabilityProvider te = this.getTileOnSide(dir);
				if (te == null || te instanceof ItemPipe) continue;
				IItemHandler acc = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, dir.getOpposite());
				if (acc != null) inventory.items[0] = this.insert(acc, inventory.items[0]);
				else updateCon = true;
			}
		} 
		if (inventory.items[0] == null || (type == BlockItemPipe.ID_Injection && filter != null && !filter.transfer(inventory.items[0]))) return;
		ArrayList<Inventory> flowList = new ArrayList<Inventory>(5);
		for (int i = 0; i < 6; i++)
			if ((d = this.getFlowBit(i)) == 2) {
				dir = EnumFacing.VALUES[i];
				TileEntity te = worldObj.getTileEntity(pos.offset(dir));
				if (te != null && te instanceof ItemPipe) flowList.add(((ItemPipe)te).inventory);
			}
		for (Inventory dst : flowList) {
			if (dst.items[0] == null) {
				dst.items[0] = inventory.items[0];
				inventory.items[0] = null;
				return;
			} else if (ItemStack.areItemsEqual(dst.items[0], inventory.items[0])) {
				ItemStack item = inventory.extractItem(0, dst.items[0].getMaxStackSize() - dst.items[0].stackSize, false);
				if (item != null) dst.items[0].stackSize += item.stackSize;
				if (inventory.items[0] == null) return;
			}
		}
	}

	private ItemStack extract(IItemHandler inv, ItemStack item) {
		ItemStack extr;
		if (PipeUpgradeItem.isNullEq(filter)) {
			if (item == null) return ItemFluidUtil.drain(inv, -1);
			extr = item.copy();
			extr.stackSize = item.getMaxStackSize() - item.stackSize;
		} else if ((extr = filter.getExtract(item, inv)) == null) return item;
		else extr.stackSize = Math.min(extr.stackSize, item.getMaxStackSize() - item.stackSize);
		int n = ItemFluidUtil.drain(inv, extr);
		return n == 0 ? null : ItemHandlerHelper.copyStackWithSize(item, n);
	}

	private ItemStack insert(IItemHandler inv, ItemStack item) {
		if (PipeUpgradeItem.isNullEq(filter)) return ItemHandlerHelper.insertItem(inv, item, false);
		int m = filter.insertAmount(inventory.items[0], inv);
		if (m > 0) {
			ItemStack res = ItemHandlerHelper.insertItem(inv, item.splitStack(m), false);
			if (res != null) item.stackSize += res.stackSize;
			else if (item.stackSize <= 0) return null;
		}
		return item;
	}

	@Override
	public void onNeighborBlockChange(Block b) {
		updateCon = true;
	}

	@Override
	public boolean onActivated(EntityPlayer player, EnumHand hand, ItemStack item, EnumFacing dir, float X, float Y, float Z) {
		int s = dir.getIndex();
		int type = this.getBlockMetadata();
		boolean canF = type == BlockItemPipe.ID_Extraction || type == BlockItemPipe.ID_Injection;
		if (player.isSneaking() && item == null) {
			if (worldObj.isRemote) return true;
			if (cover != null) {
				player.setHeldItem(hand, cover.item);
				cover = null;
				this.markUpdate();
				return true;
			}
			X -= 0.5F;
			Y -= 0.5F;
			Z -= 0.5F;
			float dx = Math.abs(X);
			float dy = Math.abs(Y);
			float dz = Math.abs(Z);
			if (dy > dz && dy > dx) s = Y < 0 ? 0 : 1;
			else if (dz > dx) s = Z < 0 ? 2 : 3;
			else s = X < 0 ? 4 : 5;
			int lock = this.getFlowBit(s) == 3 ? 0 : 3;
			this.setFlowBit(s, lock);
			this.onNeighborBlockChange(this.getBlockType());
			this.markUpdate();
			TileEntity te = Utils.getTileOnSide(this, (byte)s);
			if (te != null && te instanceof ItemPipe) {
				ItemPipe pipe = (ItemPipe)te;
				pipe.setFlowBit(s^1, lock);
				pipe.onNeighborBlockChange(this.getBlockType());
				pipe.markUpdate();
			}
			return true;
		} else if (!player.isSneaking() && item == null && filter != null) {
			if (worldObj.isRemote) return true;
			item = new ItemStack(Objects.itemUpgrade);
			item.setTagCompound(PipeUpgradeItem.save(filter));
			filter = null;
			player.setHeldItem(hand, item);
			return true;
		} else if (!player.isSneaking() && cover == null && item != null && (cover = Cover.create(item)) != null) {
			if (worldObj.isRemote) return true;
			item.stackSize--;
			if (item.stackSize <= 0) item = null;
			player.setHeldItem(hand, item);
			this.markUpdate();
			return true;
		} else if (filter == null && canF && item != null && item.getItem() instanceof ItemItemUpgrade && item.getTagCompound() != null) {
			if (worldObj.isRemote) return true;
			filter = PipeUpgradeItem.load(item.getTagCompound());
			item.stackSize--;
			if (item.stackSize <= 0) item = null;
			player.setHeldItem(hand, item);
			return true;
		} else return false;
	}

	private int getFlowBit(int b) {
		return flow >> (b * 2) & 3;
	}

	private void setFlowBit(int b, int v) {
		b *= 2;
		flow = (short)(flow & ~(3 << b) | (v & 3) << b);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		nbt.setShort("flow", flow);
		if (filter != null) nbt.setTag("filter", PipeUpgradeItem.save(filter));
		if (cover != null) cover.write(nbt, "cover");
		return super.writeToNBT(nbt);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		flow = nbt.getShort("flow");
		if (nbt.hasKey("filter")) filter = PipeUpgradeItem.load(nbt.getCompoundTag("filter"));
		cover = Cover.read(nbt, "cover");
		updateCon = true;
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		flow = pkt.getNbtCompound().getShort("flow");
		cover = Cover.read(pkt.getNbtCompound(), "cover");
		this.markUpdate();
	}

	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setShort("flow", flow);
		if (cover != null) cover.write(nbt, "cover");
		return new SPacketUpdateTileEntity(getPos(), -1, nbt);
	}

	@Override
	public int textureForSide(byte s) {
		if (s == -1) return this.getBlockMetadata();
		TileEntity p = Utils.getTileOnSide(this, s);
		int b = getFlowBit(s);
		if (b == 3 || p == null || !p.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.VALUES[s^1])) return -1;
		return b;
	}

	@Override
	public void breakBlock() 
	{
		super.breakBlock();
		if (filter != null) {
			ItemStack item = new ItemStack(Objects.itemUpgrade);
			item.setTagCompound(PipeUpgradeItem.save(filter));
			filter = null;
			EntityItem entity = new EntityItem(worldObj, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, item);
			worldObj.spawnEntityInWorld(entity);
		}
		if (cover != null) {
			EntityItem entity = new EntityItem(worldObj, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, cover.item);
			cover = null;
			worldObj.spawnEntityInWorld(entity);
		}
	}

	@Override
	public Cover getCover() 
	{
		return cover;
	}
}
