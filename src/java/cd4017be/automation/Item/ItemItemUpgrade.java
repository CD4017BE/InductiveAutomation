package cd4017be.automation.Item;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

import cd4017be.automation.Automation;
import cd4017be.automation.Gui.GuiItemUpgrade;
import cd4017be.lib.BlockGuiHandler;
import cd4017be.lib.DefaultItem;
import cd4017be.lib.IGuiItem;
import cd4017be.lib.TooltipInfo;
import cd4017be.lib.Gui.DataContainer;
import cd4017be.lib.Gui.ItemGuiData;
import cd4017be.lib.Gui.SlotHolo;
import cd4017be.lib.Gui.TileContainer;
import cd4017be.lib.templates.InventoryItem;
import cd4017be.lib.templates.InventoryItem.IItemInventory;
import cd4017be.lib.util.ItemFluidUtil;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

/**
 *
 * @author CD4017BE
 */
public class ItemItemUpgrade extends DefaultItem implements IGuiItem, IItemInventory {

	public ItemItemUpgrade(String id) {
		super(id);
		this.setCreativeTab(Automation.tabAutomation);
	}

	@Override
	public void addInformation(ItemStack item, EntityPlayer player, List<String> list, boolean b) {
		if (item.hasTagCompound()) {
			String[] states = I18n.translateToLocal("gui.cd4017be.filter.state").split(",");
			PipeUpgradeItem filter = PipeUpgradeItem.load(item.getTagCompound());
			String s;
			if (states.length >= 8) {
				s = states[(filter.mode & 1) == 0 ? 0 : 1];
				if ((filter.mode & 4) != 0) s += states[2];
				if ((filter.mode & 8) != 0) s += states[3];
				if ((filter.mode & 16) != 0) s += states[4];
				if ((filter.mode & 2) != 0) s += states[5];
				if ((filter.mode & 64) != 0) s += states[(filter.mode & 128) != 0 ? 7 : 6];
			} else s = "<invalid lang entry!>";
			list.add(s);
			boolean num = (filter.mode & 32) != 0;
			for (ItemStack stack : filter.list) list.add((num ? stack.stackSize + "x " : "> ") + stack.getDisplayName());
			if (filter.priority != 0) list.add(TooltipInfo.format("gui.cd4017be.priority", filter.priority));
		}
		super.addInformation(item, player, list, b);
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(ItemStack item, World world, EntityPlayer player, EnumHand hand) {
		BlockGuiHandler.openItemGui(player, world, 0, -1, 0);
		return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, item);
	}

	@Override
	public Container getContainer(World world, EntityPlayer player, int x, int y, int z) {
		return new TileContainer(new GuiData(), player);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public GuiContainer getGui(World world, EntityPlayer player, int x, int y, int z) {
		return new GuiItemUpgrade(new TileContainer(new GuiData(), player));
	}

	@Override
	public void onPlayerCommand(ItemStack item, EntityPlayer player, PacketBuffer dis) {
		NBTTagCompound nbt;
		if (item.hasTagCompound()) nbt = item.getTagCompound();
		else item.setTagCompound(nbt = new NBTTagCompound());
		byte cmd = dis.readByte();
		if (cmd == 0) nbt.setByte("mode", dis.readByte());
		else if (cmd == 1) nbt.setInteger("maxAm", dis.readInt());
		else if (cmd == 2) nbt.removeTag(ItemFluidUtil.Tag_ItemIndex);
	}

	@Override
	public ItemStack[] loadInventory(ItemStack inv, EntityPlayer player) {
		ItemStack[] items = new ItemStack[12];
		if (inv.hasTagCompound())
			ItemFluidUtil.loadItems(inv.getTagCompound().getTagList(ItemFluidUtil.Tag_ItemList, 10), items);
		return items;
	}

	@Override
	public void saveInventory(ItemStack inv, EntityPlayer player, ItemStack[] items) {
		if (!inv.hasTagCompound()) inv.setTagCompound(new NBTTagCompound());
		inv.getTagCompound().setTag(ItemFluidUtil.Tag_ItemList, ItemFluidUtil.saveItems(items));
	}

	class GuiData extends ItemGuiData {

		public GuiData() {super(ItemItemUpgrade.this);}

		@Override
		public void initContainer(DataContainer container) {
			TileContainer cont = (TileContainer)container;
			this.inv = new InventoryItem(cont.player);
			for (int j = 0; j < 2; j++)
				for (int i = 0; i < 6; i++)
					cont.addItemSlot(new SlotHolo(inv, j * 6 + i, 8 + i * 18, 16 + j * 18, false, true));
			cont.addPlayerInventory(8, 68, false, true);
		}

	}

}
