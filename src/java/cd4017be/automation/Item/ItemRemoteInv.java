package cd4017be.automation.Item;

import cd4017be.automation.Automation;
import cd4017be.automation.Objects;
import cd4017be.automation.Gui.GuiRemoteInventory;
import cd4017be.lib.BlockGuiHandler;
import cd4017be.lib.DefaultItem;
import cd4017be.lib.IGuiItem;
import cd4017be.lib.Gui.DataContainer;
import cd4017be.lib.Gui.GlitchSaveSlot;
import cd4017be.lib.Gui.ItemGuiData;
import cd4017be.lib.Gui.SlotItemType;
import cd4017be.lib.Gui.TileContainer;
import cd4017be.lib.templates.BasicInventory;
import cd4017be.lib.templates.InventoryItem;
import cd4017be.lib.templates.InventoryItem.IItemInventory;
import cd4017be.lib.util.IFilter;
import cd4017be.lib.util.ItemFluidUtil;
import cd4017be.lib.util.Utils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

public class ItemRemoteInv extends DefaultItem implements IGuiItem, IItemInventory {

	public ItemRemoteInv(String id) {
		super(id);
		this.setCreativeTab(Automation.tabAutomation);
		this.setMaxStackSize(1);
	}

	@Override
	public Container getContainer(World world, EntityPlayer player, int x, int y, int z) {
		return new TileContainer(new GuiData(), player);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public GuiContainer getGui(World world, EntityPlayer player, int x, int y, int z) {
		return new GuiRemoteInventory(new TileContainer(new GuiData(), player));
	}

	@Override
	public void onPlayerCommand(ItemStack item, EntityPlayer player, PacketBuffer dis) {
		byte cmd = dis.readByte();
		if (cmd >= 0 && cmd < 2) {
			String name = cmd == 0 ? "fin" : "fout";
			if (item.hasTagCompound() && item.getTagCompound().hasKey(name, 10)) {
				NBTTagCompound tag = item.getTagCompound().getCompoundTag(name);
				byte m = tag.getByte("mode");
				m |= 64;
				m ^= 128;
				tag.setByte("mode", m);
				ItemGuiData.updateInventory(player, player.inventory.currentItem);
			}
		}
	}

	@Override
	public EnumActionResult onItemUse(ItemStack item, EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing s, float X, float Y, float Z) {
		if (!world.isRemote && player.isSneaking()) {
			TileEntity te = world.getTileEntity(pos);
			if (te == null || !(te instanceof IInventory)) {
				player.addChatMessage(new TextComponentString("Block has no inventory!"));
				return EnumActionResult.SUCCESS;
			}
			if (item.getTagCompound() == null) item.setTagCompound(new NBTTagCompound());
			item.getTagCompound().setInteger("x", pos.getX());
			item.getTagCompound().setInteger("y", pos.getY());
			item.getTagCompound().setInteger("z", pos.getZ());
			item.getTagCompound().setByte("s", (byte)s.getIndex());
			item.getTagCompound().setInteger("d", player.dimension);
			item.getTagCompound().setInteger("size", Utils.accessibleSlots((IInventory)te, s.getIndex()).length);
			player.addChatMessage(new TextComponentString("Block inventory linked"));
			return EnumActionResult.SUCCESS;
		}
		return EnumActionResult.PASS;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(ItemStack item, World world, EntityPlayer player, EnumHand hand) {
		if (!player.isSneaking()) BlockGuiHandler.openItemGui(player, world, 0, -1, 0);
		return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, item);
	}

	@Override
	public void onUpdate(ItemStack item, World world, Entity entity, int slot, boolean b) {
		if (world.isRemote) return;
		if (entity instanceof EntityPlayer) {
			if (item.getTagCompound() == null) item.setTagCompound(new NBTTagCompound());
			int t = item.getTagCompound().getByte("t") + 1;
			if (t >= 20) {
				t = 0;
				EntityPlayer player = (EntityPlayer)entity;
				InventoryPlayer inv = player.inventory;
				TileEntity te = getLink(item);
				IItemHandler link = te != null ? te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.VALUES[(item.getTagCompound().getByte("s") & 0xff) % 6]) : null;
				if (link != null) {
					PipeUpgradeItem in = item.getTagCompound().hasKey("fin") ? PipeUpgradeItem.load(item.getTagCompound().getCompoundTag("fin")) : null;
					PipeUpgradeItem out = item.getTagCompound().hasKey("fout") ? PipeUpgradeItem.load(item.getTagCompound().getCompoundTag("fout")) : null;
					this.updateTransfer(link, inv, in, out);
					item.getTagCompound().setInteger("size", link.getSlots());
				} else item.getTagCompound().setInteger("size", 0);
			}
			item.getTagCompound().setByte("t", (byte)t);
		}
	}

	public static TileEntity getLink(ItemStack item) {
		if (item == null || item.getTagCompound() == null) return null;
		int x = item.getTagCompound().getInteger("x");
		int y = item.getTagCompound().getInteger("y");
		int z = item.getTagCompound().getInteger("z");
		int d = item.getTagCompound().getInteger("d");
		if (y < 0) return null;
		World world = DimensionManager.getWorld(d);
		if (world == null) return null;
		return world.getTileEntity(new BlockPos(x, y, z));
	}

	private void updateTransfer(IItemHandler link, InventoryPlayer inv, PipeUpgradeItem in, PipeUpgradeItem out) {
		if (in == null && out == null) return;
		if (in != null && (in.mode & 128) != 0) {
			in.mode |= 64;
			ItemFluidUtil.transferItems(new InvWrapper(inv), link, in, notMe);
		}
		if (out != null && (out.mode & 128) != 0) {
			out.mode |= 64;
			ItemFluidUtil.transferItems(link, new InvWrapper(inv), notMe, out);
		}
	}

	@Override
	public ItemStack[] loadInventory(ItemStack inv, EntityPlayer player) {
		ItemStack[] items = new ItemStack[2];
		if (inv.hasTagCompound()) {
			NBTTagCompound nbt = inv.getTagCompound();
			if (nbt.hasKey("fin")) {
				items[0] = new ItemStack(Objects.itemUpgrade);
				items[0].setTagCompound(nbt.getCompoundTag("fin"));
			} else items[0] = null;
			if (nbt.hasKey("fout")) {
				items[1] = new ItemStack(Objects.itemUpgrade);
				items[1].setTagCompound(nbt.getCompoundTag("fout"));
			} else items[1] = null;
		}
		return items;
	}

	@Override
	public void saveInventory(ItemStack inv, EntityPlayer player, ItemStack[] items) {
		NBTTagCompound nbt;
		if (inv.hasTagCompound()) nbt = inv.getTagCompound();
		else inv.setTagCompound(nbt = new NBTTagCompound());
		if (items[0] != null && items[0].getItem() == Objects.itemUpgrade) {
			if (!items[0].hasTagCompound()) items[0].setTagCompound(new NBTTagCompound());
			nbt.setTag("fin", items[0].getTagCompound());
		} else nbt.removeTag("fin");
		if (items[1] != null && items[1].getItem() == Objects.itemUpgrade) {
			if (!items[1].hasTagCompound()) items[1].setTagCompound(new NBTTagCompound());
			nbt.setTag("fout", items[1].getTagCompound());
		} else nbt.removeTag("fout");
	}

	public static final IFilter<ItemStack, IItemHandler> notMe = new IFilter<ItemStack, IItemHandler>(){
		@Override
		public int insertAmount(ItemStack obj, IItemHandler inv) {
			return obj != null && !(obj.getItem() instanceof ItemRemoteInv || obj.getItem() instanceof ItemFilteredSubInventory) ? obj.stackSize : null;
		}
		@Override
		public ItemStack getExtract(ItemStack obj, IItemHandler inv) {
			return obj != null && !(obj.getItem() instanceof ItemRemoteInv || obj.getItem() instanceof ItemFilteredSubInventory) ? obj : null;
		}
		@Override
		public boolean transfer(ItemStack obj) {
			return true;
		}
	};

	public class GuiData extends ItemGuiData {

		public TileEntity link;
		public IItemHandler linkedInv;
		public int size, ofsY;

		public GuiData() {
			super(ItemRemoteInv.this);
		}

		@Override
		public void initContainer(DataContainer container) {
			TileContainer cont = (TileContainer)container;
			this.inv = new InventoryItem(cont.player);
			ItemStack item = cont.player.inventory.mainInventory[cont.player.inventory.currentItem];
			if (!cont.player.worldObj.isRemote) {
				link = getLink(item);
				linkedInv = link != null ? link.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.VALUES[(item.getTagCompound().getByte("s") & 0xff) % 6]) : null;
			} else if (item != null && item.hasTagCompound() && (size = item.getTagCompound().getInteger("size")) > 0) linkedInv = new BasicInventory(size);
			if (linkedInv == null) linkedInv = new BasicInventory(0); 
			size = linkedInv.getSlots();
			if (size > 0) {
				ofsY = (size - 1) / 12 * 18 - 18;
				int h = size / 12;
				int w = size % 12;
				for (int j = 0; j < h; j++)
					for (int i = 0; i < 12; i++)
						cont.addItemSlot(new GlitchSaveSlot(linkedInv, i + 12 * j, 8 + i * 18, 16 + j * 18));
				for (int i = 0; i < w; i++)
					cont.addItemSlot(new GlitchSaveSlot(linkedInv, i + 12 * h, 8 + i * 18, 16 + h * 18));
			} else ofsY = -18;
			cont.addItemSlot(new SlotItemType(inv, 0, 8, 86 + ofsY, new ItemStack(Objects.itemUpgrade)));
			cont.addItemSlot(new SlotItemType(inv, 1, 26, 86 + ofsY, new ItemStack(Objects.itemUpgrade)));
			cont.addPlayerInventory(62, 68 + ofsY, false, true);
		}

		@Override
		public boolean canPlayerAccessUI(EntityPlayer player) {
			ItemStack item = player.inventory.mainInventory[player.inventory.currentItem];
			return item != null && item.getItem() == this.item && item.hasTagCompound() && item.getTagCompound().getInteger("size") == size && link != null && !link.isInvalid() && link.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.VALUES[(item.getTagCompound().getByte("s") & 0xff) % 6]);
		}

	}

}
