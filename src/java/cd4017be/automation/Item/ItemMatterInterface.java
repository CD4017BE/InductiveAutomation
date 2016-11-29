package cd4017be.automation.Item;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import cd4017be.api.automation.MatterOrbItemHandler;
import cd4017be.api.automation.MatterOrbItemHandler.Access;
import cd4017be.automation.Automation;
import cd4017be.automation.Gui.GuiItemMatterInterface;
import cd4017be.lib.BlockGuiHandler;
import cd4017be.lib.DefaultItem;
import cd4017be.lib.IGuiItem;
import cd4017be.lib.Gui.DataContainer;
import cd4017be.lib.Gui.GlitchSaveSlot;
import cd4017be.lib.Gui.ItemGuiData;
import cd4017be.lib.Gui.TileContainer;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

/**
 *
 * @author CD4017BE
 */
public class ItemMatterInterface extends DefaultItem implements IGuiItem {

	public ItemMatterInterface(String id) {
		super(id);
		this.setCreativeTab(Automation.tabAutomation);
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(ItemStack item, World world, EntityPlayer player, EnumHand hand) {
		BlockGuiHandler.openItemGui(player, world, 0, -1, 0);
		return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, item);
	}

	@Override
	public EnumActionResult onItemUse(ItemStack item, EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float X, float Y, float Z) {
		BlockGuiHandler.openItemGui(player, world, pos.getX(), pos.getY(), pos.getZ());
		return EnumActionResult.SUCCESS;
	}

	@Override
	public Container getContainer(World world, EntityPlayer player, int x, int y, int z) {
		return new TileContainer(new GuiData(), player);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public GuiContainer getGui(World world, EntityPlayer player, int x, int y, int z) {
		return new GuiItemMatterInterface(new GuiData(), player);
	}

	@Override
	public void onPlayerCommand(ItemStack item, EntityPlayer player, PacketBuffer dis) {
		if (player.openContainer instanceof TileContainer && ((TileContainer)player.openContainer).data instanceof GuiData) {
			Access inv = ((GuiData)((TileContainer)player.openContainer).data).inv;
			byte cmd = dis.readByte();
			if (cmd == 0) {
				int dst = dis.readByte(), am = dis.readInt();
				if (am <= 0 || dst < 0 || dst >= inv.inv.mainInventory.length) return;
				ItemStack A = inv.inv.mainInventory[inv.tool];
				ItemStack B = inv.inv.mainInventory[dst];
				if (!MatterOrbItemHandler.isMatterOrb(B)) return;
				ItemStack extr = MatterOrbItemHandler.decrStackSize(A, inv.slot, am);
				if (extr == null) return;
				ItemStack[] rem = MatterOrbItemHandler.addItemStacks(B, extr);
				if (rem.length > 0) MatterOrbItemHandler.addItemStacks(A, rem);
			} else if (cmd == 1) {
				inv.tool = dis.readByte();
				if (inv.tool < 0 || inv.tool >= inv.inv.mainInventory.length) inv.tool = inv.inv.currentItem;
			} else if (cmd == 2) {
				inv.slot = dis.readByte() & 0xff;
			}
		}
	}

	public class GuiData extends ItemGuiData {

		public Access inv;

		public GuiData() {
			super(ItemMatterInterface.this);
		}

		@Override
		public void initContainer(DataContainer container) {
			TileContainer cont = (TileContainer)container;
			inv = new Access(cont.player.inventory);
			cont.addItemSlot(new GlitchSaveSlot(inv, 0, 26, 66, false));
			cont.addItemSlot(new GlitchSaveSlot(inv, 1, 44, 66, false));
			cont.addPlayerInventory(8, 100, false, true);
		}

		@Override
		public int[] getSyncVariables() {
			return new int[]{inv.slot, inv.tool};
		}

		@Override
		public void setSyncVariable(int i, int v) {
			if (i == 0) inv.slot = v;
			else inv.tool = v;
		}

	}

}
