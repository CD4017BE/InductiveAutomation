package cd4017be.automation.TileEntity;

import java.util.ArrayList;

import cd4017be.api.automation.IItemPipeCon;
import cd4017be.automation.Objects;
import cd4017be.automation.Block.BlockItemPipe;
import cd4017be.automation.Item.ItemItemUpgrade;
import cd4017be.automation.Item.PipeUpgradeItem;
import cd4017be.lib.ModTileEntity;
import cd4017be.lib.templates.IPipe;
import cd4017be.lib.templates.LinkedInventory;
import cd4017be.lib.util.ItemFluidUtil;
import cd4017be.lib.util.TileAccess;
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
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

/**
 *
 * @author CD4017BE
 */
public class ItemPipe extends ModTileEntity implements ITickable, IPipe, IItemPipeCon {

	public static byte ticks = 1;
	public final LinkedInventory invcap;
	public ItemStack inventory, last;
	private PipeUpgradeItem filter = null;
	private Cover cover = null;
	private ArrayList<PipeAccess> targets = new ArrayList<PipeAccess>(5);
	private ArrayList<TileAccess> invs = null;
	/** bits[0-13 (6+1)*2]: (side + total) * dir{0:none, 1:in, 2:out, 3:lock/both} */
	private short flow;
	private boolean updateCon = true;
	private byte timer = 0;

	public ItemPipe() {
		this.invcap = new LinkedInventory(1, (s) -> inventory, (item, s) -> inventory = item);
	}

	@Override
	public void update() {
		if (worldObj.isRemote) return;
		int type = this.getBlockMetadata();
		if (updateCon) this.updateConnections(type);
		if ((flow & 0x3000) == 0x3000)
			switch(type) {
			case BlockItemPipe.ID_Injection:
				if (inventory != null && (filter == null || filter.active(worldObj.isBlockPowered(pos)))) transferIn();
				if (inventory != null && !targets.isEmpty() && (filter == null || filter.transfer(inventory))) transfer();
				break;
			case BlockItemPipe.ID_Extraction:
				timer++;
				if ((filter == null || filter.active(worldObj.isBlockPowered(pos))) && (timer & 0xff) >= ticks) transferEx();
			default:
				if (inventory != null) transfer();
			}
		if (inventory != last) {
			last = inventory;
			markUpdate();
		}
	}

	@Override
	public boolean hasCapability(Capability<?> cap, EnumFacing facing) {
		return cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getCapability(Capability<T> cap, EnumFacing facing) {
		return cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY ? (T)invcap : null;
	}

	private void updateConnections(int type) {
		targets.clear();
		if (invs != null) invs.clear();
		else if (type != BlockItemPipe.ID_Transport) invs = new ArrayList<TileAccess>(5);
		int tp = 0;
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
			if (te == null) setFlowBit(i, 0);
			else if (te instanceof ItemPipe) {
				ItemPipe pipe = (ItemPipe)te;
				int pHasIO = pipe.getFlowBit(6);
				int pDirIO = pipe.getFlowBit(i ^ 1);
				if (pDirIO == 3) nDirIO = 3;
				else if ((nDirIO = pHasIO & ~pDirIO) == 3) 
					nDirIO = lHasIO == 1 && (lDirIO & 1) == 0 ? 2 : lHasIO == 2 && (lDirIO & 2) == 0 ? 1 : 0;
				setFlowBit(i, nDirIO);
				if (nDirIO != 3) nHasIO |= nDirIO;
				if (nDirIO == 1) targets.add(new PipeAccess(pipe, dir));
				updateList.add(pipe);
			} else if (te instanceof IItemPipeCon) {
				byte d = ((IItemPipeCon)te).getItemConnectType(i^1);
				d = d == 1 ? 2 : d == 2 ? 1 : (byte)0;
				setFlowBit(i, d);
				nHasIO |= d;
				if (d == 1) targets.add(tp++, new PipeAccess((IItemPipeCon)te, dir.getOpposite()));
			} else if (type != BlockItemPipe.ID_Transport && te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, dir.getOpposite())) {
				setFlowBit(i, type);
				nHasIO |= type;
				invs.add(new TileAccess(te, dir.getOpposite()));
			} else setFlowBit(i, 0);
		}
		setFlowBit(6, nHasIO);
		if (flow != lFlow) {
			this.markUpdate();
			for (ItemPipe pipe : updateList) pipe.updateCon = true;
		}
		updateCon = false;
	}

	private void transfer() {
		for (PipeAccess pipe : targets)
			if (pipe.te.isInvalid()) updateCon = true;
			else if ((inventory = pipe.pipe.insert(inventory, pipe.side)) == null) return;
	}

	private void transferIn() {
		IItemHandler acc;
		for (TileAccess inv : invs)
			if (inv.te.isInvalid() || (acc = inv.te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, inv.side)) == null) updateCon = true;
			else if (PipeUpgradeItem.isNullEq(filter)) {
				inventory = ItemHandlerHelper.insertItem(acc, inventory, false);
				if (inventory == null) break;
			} else {
				int m = filter.insertAmount(inventory, acc);
				if (m > 0) {
					ItemStack res = ItemHandlerHelper.insertItem(acc, inventory.splitStack(m), false);
					if (res != null) inventory.stackSize += res.stackSize;
					else if (inventory.stackSize <= 0) {
						inventory = null;
						break;
					}
				}
			}
	}

	private void transferEx() {
		timer = 0;
		IItemHandler acc;
		for (TileAccess inv : invs)
			if (inv.te.isInvalid() || (acc = inv.te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, inv.side)) == null) updateCon = true;
			else if (PipeUpgradeItem.isNullEq(filter)) {
				if (inventory == null) inventory = ItemFluidUtil.drain(acc, -1);
				else {
					int m = inventory.getMaxStackSize() - inventory.stackSize;
					if (m <= 0) break;
					inventory.stackSize += ItemFluidUtil.drain(acc, ItemHandlerHelper.copyStackWithSize(inventory, m));
				}
			} else {
				int n;
				if (inventory == null) n = 0;
				else if ((n = inventory.stackSize) >= inventory.getMaxStackSize()) break;
				ItemStack extr = filter.getExtract(inventory, acc);
				if (extr == null) continue;
				int m = extr.getMaxStackSize() - n;
				if (m < extr.stackSize) extr.stackSize = m;
				extr.stackSize = ItemFluidUtil.drain(acc, extr) + n;
				inventory = extr;
			}
	}

	@Override
	public void onNeighborTileChange(BlockPos pos) {
		updateCon = true;
	}

	@Override
	public void onNeighborBlockChange(Block b) {
		updateCon = true;
	}

	@Override
	public boolean onActivated(EntityPlayer player, EnumHand hand, ItemStack item, EnumFacing dir, float X, float Y, float Z) {
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
			dir = getClickedSide(X, Y, Z);
			int s = dir.getIndex();
			int lock = this.getFlowBit(s) == 3 ? 0 : 3;
			this.setFlowBit(s, lock);
			updateCon = true;
			this.markUpdate();
			ICapabilityProvider te = getTileOnSide(dir);
			if (te != null && te instanceof ItemPipe) {
				ItemPipe pipe = (ItemPipe)te;
				pipe.setFlowBit(s^1, lock);
				pipe.updateCon = true;
				pipe.markUpdate();
			}
			return true;
		} else if (!player.isSneaking() && item == null && filter != null) {
			if (worldObj.isRemote) return true;
			item = new ItemStack(Objects.itemUpgrade);
			item.setTagCompound(PipeUpgradeItem.save(filter));
			filter = null;
			player.setHeldItem(hand, item);
			markUpdate();
			return true;
		} else if (!player.isSneaking() && cover == null && item != null && (cover = Cover.create(item)) != null) {
			if (worldObj.isRemote) return true;
			item.stackSize--;
			if (item.stackSize <= 0) item = null;
			player.setHeldItem(hand, item);
			markUpdate();
			return true;
		} else if (filter == null && canF && item != null && item.getItem() instanceof ItemItemUpgrade && item.getTagCompound() != null) {
			if (worldObj.isRemote) return true;
			filter = PipeUpgradeItem.load(item.getTagCompound());
			item.stackSize--;
			if (item.stackSize <= 0) item = null;
			player.setHeldItem(hand, item);
			markUpdate();
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
		if (inventory != null) nbt.setTag("item", inventory.writeToNBT(new NBTTagCompound()));
		return super.writeToNBT(nbt);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		flow = nbt.getShort("flow");
		if (nbt.hasKey("filter")) filter = PipeUpgradeItem.load(nbt.getCompoundTag("filter"));
		if (nbt.hasKey("item")) inventory = ItemStack.loadItemStackFromNBT(nbt.getCompoundTag("item"));
		cover = Cover.read(nbt, "cover");
		updateCon = true;
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		NBTTagCompound nbt = pkt.getNbtCompound();
		short nf = nbt.getShort("flow");
		boolean f = nbt.getBoolean("filt");
		Cover nc = Cover.read(nbt, "cover");
		if (nbt.hasKey("it", 10)) inventory = ItemStack.loadItemStackFromNBT(nbt.getCompoundTag("it"));
		else inventory = null;
		if (nf != flow || (f ^ filter != null) || (cover == null ^ nc == null) || (cover != null && nc.item.isItemEqual(cover.item))) {
			if (f) filter = new PipeUpgradeItem();
			else filter = null;
			flow = nf;
			cover = nc;
			this.markUpdate();
		}
	}

	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setShort("flow", flow);
		nbt.setBoolean("filt", filter != null);
		if (last != null) nbt.setTag("it", last.writeToNBT(new NBTTagCompound()));
		if (cover != null) cover.write(nbt, "cover");
		return new SPacketUpdateTileEntity(getPos(), -1, nbt);
	}

	@Override
	public int textureForSide(byte s) {
		if (s == -1) return this.getBlockMetadata();
		int b = getFlowBit(s);
		EnumFacing f = EnumFacing.VALUES[s];
		ICapabilityProvider p = getTileOnSide(f);
		if (b == 3 || p == null || !p.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, f.getOpposite())) return -1;
		if (filter != null && b != 0 && !(b == 2 && p instanceof ItemPipe)) b += 2;
		return b;
	}

	@Override
	public void breakBlock() {
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
	public Cover getCover() {
		return cover;
	}

	@Override
	public byte getItemConnectType(int s) {return 0;}

	@Override
	public ItemStack insert(ItemStack item, EnumFacing side) {
		if (inventory == null) {
			inventory = item;
			return null;
		}
		if (ItemHandlerHelper.canItemStacksStack(inventory, item)) {
			int n = inventory.getMaxStackSize() - inventory.stackSize;
			if (n >= item.stackSize) {
				inventory.stackSize += item.stackSize;
				return null;
			}
			inventory.stackSize += n;
			item.stackSize -= n;
		}
		return item;
	}

	private class PipeAccess extends TileAccess {
		IItemPipeCon pipe;
		PipeAccess(IItemPipeCon pipe, EnumFacing side) {super((TileEntity)pipe, side); this.pipe = pipe;}
	}

}
