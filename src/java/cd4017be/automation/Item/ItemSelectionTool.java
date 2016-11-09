package cd4017be.automation.Item;

import java.util.List;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;
import cd4017be.api.automation.IOperatingArea;
import cd4017be.automation.Automation;
import cd4017be.automation.Objects;
import cd4017be.automation.Gui.GuiAreaUpgrade;
import cd4017be.lib.BlockGuiHandler;
import cd4017be.lib.BlockItemRegistry;
import cd4017be.lib.DefaultItem;
import cd4017be.lib.IGuiItem;
import cd4017be.lib.Gui.DataContainer;
import cd4017be.lib.Gui.ItemGuiData;
import cd4017be.lib.Gui.SlotItemType;
import cd4017be.lib.Gui.TileContainer;
import cd4017be.lib.util.Utils;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
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

/**
 *
 * @author CD4017BE
 */
public class ItemSelectionTool extends DefaultItem implements IGuiItem {

	public ItemSelectionTool(String id) {
		super(id);
		this.setCreativeTab(Automation.tabAutomation);
		this.setMaxStackSize(1);
		this.setHasSubtypes(true);
	}

	@Override
	public EnumActionResult onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing s, float X, float Y, float Z) {
		if (!world.isRemote) onClick(stack, player, true, pos);
		return EnumActionResult.SUCCESS;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(ItemStack item, World world, EntityPlayer player, EnumHand hand) {
		if (!world.isRemote) onClick(item, player, false, new BlockPos(player.posX, player.posY, player.posZ));
		return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, item);
	}

	private void onClick(ItemStack item, EntityPlayer player, boolean b, BlockPos pos) {
		if (!item.hasTagCompound()) item.setTagCompound(new NBTTagCompound());
		int mode = item.getItemDamage();
		if (player.isSneaking()) {
			if (b) {
				TileEntity te = player.worldObj.getTileEntity(pos);
				if (mode == 5 && te != null && te instanceof IOperatingArea) {
					BlockGuiHandler.openItemGui(player, player.worldObj, pos.getX(), pos.getY(), pos.getZ());
					return;
				} else if (item.getTagCompound().getInteger("mx") == pos.getX() && item.getTagCompound().getInteger("my") == pos.getY()
					&& item.getTagCompound().getInteger("mz") == pos.getZ() && te != null && te instanceof IOperatingArea) {
					item.getTagCompound().setInteger("mx", 0);
					item.getTagCompound().setInteger("my", -1);
					item.getTagCompound().setInteger("mz", 0);
					player.addChatMessage(new TextComponentString("Unlinked Machine"));
					return;
				} else if (te != null && te instanceof IOperatingArea) {
					player.addChatMessage(new TextComponentString("Linked Machine"));
					int[] oa = ((IOperatingArea)te).getOperatingArea();
					item.getTagCompound().setInteger("mx", pos.getX());
					item.getTagCompound().setInteger("my", pos.getY());
					item.getTagCompound().setInteger("mz", pos.getZ());
					this.storeArea(oa, item);
					return;
				}
			}
			if (++mode >= 6) mode = 0;
			item.setItemDamage(mode);
			return;
		}
		String msg = null;
		int[] area = new int[6];
		IOperatingArea mach = this.loadArea(area, item, player.worldObj);
		if (mode == 0) {
			area[0] = pos.getX(); area[3] = pos.getX() + 1;
			area[1] = pos.getY(); area[4] = pos.getY() + 1;
			area[2] = pos.getZ(); area[5] = pos.getZ() + 1;
			msg = "Area set to: X=" + pos.getX() + " Y=" + pos.getY() + " Z=" + pos.getZ();
		} else if (mode == 1) {
			if (pos.getX() < area[0]) area[0] = pos.getX();
			if (pos.getY() < area[1]) area[1] = pos.getY();
			if (pos.getZ() < area[2]) area[2] = pos.getZ();
			if (pos.getX() >= area[3]) area[3] = pos.getX() + 1;
			if (pos.getY() >= area[4]) area[4] = pos.getY() + 1;
			if (pos.getZ() >= area[5]) area[5] = pos.getZ() + 1;
			msg = "Point added: X=" + pos.getX() + " Y=" + pos.getY() + " Z=" + pos.getZ();
		} else if (mode < 5) {
			byte look = Utils.getLookDir(player);
			if (mode == 4) {
				int d = look >> 1;
				int o = (look & 1) == 1 ? -1 : 1;
				if (d == 0){area[1] += o; area[4] += o;}
				else if (d == 1){area[2] += o; area[5] += o;}
				else if (d == 2){area[0] += o; area[3] += o;}
			} else if (mode == 2) {
				if (look == 1) area[1]--;
				else if (look == 0) area[4]++;
				else if (look == 3) area[2]--;
				else if (look == 2) area[5]++;
				else if (look == 5) area[0]--;
				else if (look == 4) area[3]++;
			} else if (mode == 3) {
				if (look == 1 && area[1] < area[4]) area[1]++;
				else if (look == 0 && area[1] < area[4]) area[4]--;
				else if (look == 3 && area[2] < area[5]) area[2]++;
				else if (look == 2 && area[2] < area[5]) area[5]--;
				else if (look == 5 && area[0] < area[3]) area[0]++;
				else if (look == 4 && area[0] < area[3]) area[3]--;
			}
		} else if (mach != null) {
			BlockGuiHandler.openItemGui(player, player.worldObj, 0, -1, 0);
			return;
		}
		if (mach != null) {
			if (IOperatingArea.Handler.setCorrectArea(mach, area, false)) {
				this.storeArea(area, item);
			} else {
				player.addChatMessage(new TextComponentString("Error: selection out of bounds!"));
				return;
			}
		} else this.storeArea(area, item);
		if (msg != null) player.addChatMessage(new TextComponentString(msg));
	}

	@Override
	public void addInformation(ItemStack item, EntityPlayer par2EntityPlayer, List<String> list, boolean par4) {
		if (item.getTagCompound() != null) {
			list.add("P1 = (" + item.getTagCompound().getInteger("px0")
					+ "|" + item.getTagCompound().getInteger("py0")
					+ "|" + item.getTagCompound().getInteger("pz0") + ")");
			list.add("P2 = (" + item.getTagCompound().getInteger("px1")
					+ "|" + item.getTagCompound().getInteger("py1")
					+ "|" + item.getTagCompound().getInteger("pz1") + ")");
			list.add("Link (" + item.getTagCompound().getInteger("mx")
					+ "|" + item.getTagCompound().getInteger("my")
					+ "|" + item.getTagCompound().getInteger("mz") + ")");
		}
		super.addInformation(item, par2EntityPlayer, list, par4);
	}

	@Override
	public Container getContainer(World world, EntityPlayer player, int x, int y, int z) {
		ItemStack item = player.inventory.mainInventory[player.inventory.currentItem];
		if (!item.hasTagCompound()) return null;
		BlockPos pos = new BlockPos(x, y, z);
		TileEntity te = world.getTileEntity(pos);
		if (te == null || !(te instanceof IOperatingArea)) {
			te = world.getTileEntity(pos = new BlockPos(item.getTagCompound().getInteger("mx"), item.getTagCompound().getInteger("my"), item.getTagCompound().getInteger("mz")));
			if (te == null || !(te instanceof IOperatingArea)) return null;
		}
		return new TileContainer(new GuiData((IOperatingArea)te, pos), player);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public GuiContainer getGui(World world, EntityPlayer player, int x, int y, int z) {
		Container c = this.getContainer(world, player, x, y, z);
		return c != null ? new GuiAreaUpgrade((TileContainer)c) : null;
	}

	@Override
	public void onPlayerCommand(ItemStack item, EntityPlayer player, PacketBuffer dis) {
		TileContainer cont;
		if (player.openContainer != null && player.openContainer instanceof TileContainer && (cont = (TileContainer)player.openContainer).data instanceof GuiData) {
			GuiData data = (GuiData)cont.data;
			byte cmd = dis.readByte();
			if (cmd >= 0 && cmd < 6) {
				int[] area = data.tile.getOperatingArea();
				area[cmd] = dis.readInt();
				IOperatingArea.Handler.setCorrectArea(data.tile, area, true);
			} else if (cmd == 6) {
				if (item != null && item.getTagCompound() != null) {
					int[] area = new int[6];
					this.loadArea(area, item, player.worldObj);
					if (!IOperatingArea.Handler.setCorrectArea(data.tile, area, false)) {
						player.addChatMessage(new TextComponentString("Error: selection out of max bounds!"));
					}
				}
			}
		}
		
	}

	private void storeArea(int[] area, ItemStack item) {
		item.getTagCompound().setInteger("px0", area[0]);
		item.getTagCompound().setInteger("py0", area[1]);
		item.getTagCompound().setInteger("pz0", area[2]);
		item.getTagCompound().setInteger("px1", area[3]);
		item.getTagCompound().setInteger("py1", area[4]);
		item.getTagCompound().setInteger("pz1", area[5]);
	}

	private IOperatingArea loadArea(int[] area, ItemStack item, World world) {
		TileEntity te = world.getTileEntity(new BlockPos(item.getTagCompound().getInteger("mx"), item.getTagCompound().getInteger("my"), item.getTagCompound().getInteger("mz")));
		if (te != null && te instanceof IOperatingArea) {
			IOperatingArea mach = (IOperatingArea)te;
			System.arraycopy(mach.getOperatingArea(), 0, area, 0, 6);
			return mach;
		} else {
			area[0] = item.getTagCompound().getInteger("px0");
			area[1] = item.getTagCompound().getInteger("py0");
			area[2] = item.getTagCompound().getInteger("pz0");
			area[3] = item.getTagCompound().getInteger("px1");
			area[4] = item.getTagCompound().getInteger("py1");
			area[5] = item.getTagCompound().getInteger("pz1");
			return null;
		}
	}

	public class GuiData extends ItemGuiData {

		public final IOperatingArea tile;
		public final BlockPos pos;

		public GuiData(IOperatingArea machine, BlockPos pos) {
			super(ItemSelectionTool.this);
			this.tile = machine;
			this.pos = pos;
		}

		@Override
		public void initContainer(DataContainer container) {
			TileContainer cont = (TileContainer)container;
			IItemHandler inv = tile.getUpgradeSlots();
			cont.addItemSlot(new SlotItemType(inv, 0, 152, 16, new ItemStack(Items.GLOWSTONE_DUST)));
			cont.addItemSlot(new SlotItemType(inv, 1, 8, 16, BlockItemRegistry.stack("AreaFrame", 1)));
			cont.addItemSlot(new SlotItemType(inv, 2, 8, 34, BlockItemRegistry.stack("EMatrix", 1)));
			cont.addItemSlot(new SlotItemType(inv, 3, 8, 52, BlockItemRegistry.stack("CoilSC", 1)));
			cont.addItemSlot(new SlotItemType(inv, 4, 134, 16, new ItemStack(Objects.synchronizer, 1, 1), new ItemStack(Objects.synchronizer, 1, 2), new ItemStack(Objects.synchronizer, 1, 3), new ItemStack(Objects.synchronizer, 1, 4)));
			cont.addPlayerInventory(8, 86, false, true);
		}

		@Override
		public BlockPos pos() {
			return pos;
		}

	}

}
