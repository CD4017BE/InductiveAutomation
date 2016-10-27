package cd4017be.automation.Item;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

import cd4017be.automation.Automation;
import cd4017be.automation.Gui.GuiFluidUpgrade;
import cd4017be.lib.BlockGuiHandler;
import cd4017be.lib.DefaultItem;
import cd4017be.lib.IGuiItem;
import cd4017be.lib.TooltipInfo;
import cd4017be.lib.Gui.DataContainer;
import cd4017be.lib.Gui.ItemGuiData;
import cd4017be.lib.Gui.TileContainer;
import cd4017be.lib.Gui.TileContainer.TankSlot;
import cd4017be.lib.templates.ITankContainer;
import cd4017be.lib.util.ItemFluidUtil;
import cd4017be.lib.util.Utils;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
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
public class ItemFluidUpgrade extends DefaultItem implements IGuiItem {

	public ItemFluidUpgrade(String id) {
		super(id);
		this.setCreativeTab(Automation.tabAutomation);
	}

	@Override
	public void addInformation(ItemStack item, EntityPlayer player, List<String> list, boolean b) {
		if (item.hasTagCompound()) {
			String[] states = I18n.translateToLocal("gui.cd4017be.filter.state").split(",");
			PipeUpgradeFluid filter = PipeUpgradeFluid.load(item.getTagCompound());
			String s;
			if (states.length >= 8) {
				s = states[(filter.mode & 1) == 0 ? 0 : 1];
				if ((filter.mode & 2) != 0) s += states[5];
				if ((filter.mode & 4) != 0) s += states[(filter.mode & 8) != 0 ? 7 : 6];
			} else s = "<invalid lang entry!>";
			list.add(s);
			for (Fluid stack : filter.list) list.add("> " + stack.getLocalizedName(new FluidStack(stack, 0)));
			if (filter.maxAmount != 0) list.add(TooltipInfo.format("gui.cd4017be.filter.stock", Utils.formatNumber((double)filter.maxAmount / 1000D, 3)));
			if (filter.priority != 0) list.add(TooltipInfo.format("gui.cd4017be.priority", filter.priority));
		}
		super.addInformation(item, player, list, b);
	}

	public Fluid[] getFluids(NBTTagCompound nbt) {
		if (nbt == null || !nbt.hasKey(ItemFluidUtil.Tag_FluidList, 9)) return null;
		NBTTagList list = nbt.getTagList(ItemFluidUtil.Tag_FluidList, 8);
		Fluid[] fluids = new Fluid[list.tagCount()];
		for (int i = 0; i < fluids.length; i++)
			fluids[i] = FluidRegistry.getFluid(list.getStringTagAt(i));
		return fluids;
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
		return new GuiFluidUpgrade(new TileContainer(new GuiData(), player));
	}

	@Override
	public void onPlayerCommand(ItemStack item, EntityPlayer player, PacketBuffer dis) {
		NBTTagCompound nbt;
		if (item.hasTagCompound()) nbt = item.getTagCompound();
		else item.setTagCompound(nbt = new NBTTagCompound());
		byte cmd = dis.readByte();
		switch(cmd) {
		case 5: nbt.setByte("mode", dis.readByte()); return;
		case 6: nbt.setInteger("maxAm", dis.readInt()); return;
		case 7: nbt.setByte("prior", dis.readByte()); return;
		default: if (cmd < 0 || cmd >= 5) return;
			String name = dis.readStringFromBuffer(32);
			NBTTagList list;
			if (nbt.hasKey("types", 9)) list = nbt.getTagList("types", 9);
			else nbt.setTag("types", list = new NBTTagList());
			if (!name.isEmpty()) {
				if (cmd < list.tagCount()) list.set(cmd, new NBTTagString(name));
				else list.appendTag(new NBTTagString(name));
			} else if (cmd < list.tagCount()) list.removeTag(cmd);
		}
	}

	class GuiData extends ItemGuiData implements ITankContainer {

		private InventoryPlayer player;
		public GuiData() {super(ItemFluidUpgrade.this);}

		@Override
		public void initContainer(DataContainer container) {
			TileContainer cont = (TileContainer)container;
			for (int i = 0; i < getTanks(); i++)
				cont.addTankSlot(new TankSlot(this, i, 26 + 18 * i, 16, (byte)0x11));
			cont.addPlayerInventory(8, 50, false, true);
			player = cont.player.inventory;
		}

		@Override
		public int getTanks() {return 5;}

		@Override
		public FluidStack getTank(int i) {
			ItemStack item = player.mainInventory[player.currentItem];
			Fluid[] fluids = item != null ? getFluids(item.getTagCompound()) : null;
			return fluids != null && i < fluids.length ? new FluidStack(fluids[i], 0) : null;
		}

		@Override
		public int getCapacity(int i) {return 0;}

		@Override
		public void setTank(int i, FluidStack fluid) {}

	}

}
