package cd4017be.automation.TileEntity;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.items.SlotItemHandler;
import cd4017be.api.automation.MatterOrbItemHandler;
import cd4017be.lib.Gui.DataContainer;
import cd4017be.lib.Gui.DataContainer.IGuiData;
import cd4017be.lib.Gui.GlitchSaveSlot;
import cd4017be.lib.Gui.TileContainer;
import cd4017be.lib.templates.AutomatedTile;
import cd4017be.lib.templates.Inventory;
import cd4017be.lib.templates.Inventory.IAccessHandler;
import cd4017be.lib.util.Utils;

/**
 *
 * @author CD4017BE
 */
public class MatterInterface extends AutomatedTile implements IGuiData, IAccessHandler {

	private int selSlot = 0;
	/** &3=0: no redstone, &3=1: pulse mode, &3=2: strength mode, &4: last RS */
	public byte mode = 0;

	public MatterInterface() {
		inventory = new Inventory(4, 3, this).group(0, 1, 2, Utils.IN).group(1, 2, 3, Utils.OUT).group(2, 0, 1, Utils.ACC);
	}

	@Override
	public void update() {
		super.update();
		if(worldObj.isRemote) return;
		if ((mode & 3) == 2) {
			int rs = 0;
			for (EnumFacing s : EnumFacing.VALUES)
				rs |= worldObj.getRedstonePower(pos, s);
			rs &= 0xff;
			if (rs != selSlot) {
				selSlot = rs;
				updateOrb();
			}
		} else if ((mode & 3) == 1) {
			boolean rs = worldObj.isBlockPowered(pos);
			if ((mode & 4) == 0 && rs) {
				selSlot++;
				updateOrb();
			}
			if(rs) mode |= 4;
			else mode &= 3;
		}
	}

	public int getSelSlot() {
		return selSlot;
	}

	@Override
	protected void customPlayerCommand(byte cmd, PacketBuffer dis, EntityPlayerMP player) {
		switch(cmd) {
		case 0: 
			selSlot = dis.readByte() & 0xff;
			updateOrb();
			break;
		case 1:
			mode &= 4;
			mode |= dis.readByte() & 3;
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		mode = nbt.getByte("mode");
		selSlot = nbt.getByte("sel") & 0xff;
		updateOrb();
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		nbt.setByte("mode", mode);
		nbt.setByte("sel", (byte)selSlot);
		return super.writeToNBT(nbt);
	}

	private void updateOrb() {
		int n = MatterOrbItemHandler.getUsedTypes(inventory.items[0]);
		if (n > 0) selSlot %= n;
		ItemStack out = MatterOrbItemHandler.getItem(inventory.items[0], selSlot);
		inventory.items[2] = out != null ? out.copy() : null;
		inventory.items[3] = out;
	}

	@Override
	public void setSlot(int g, int s, ItemStack item) {
		if(worldObj.isRemote) inventory.items[s] = item;
		else switch(s) {
		case 0:
			inventory.items[0] = item;
			updateOrb();
			return;
		case 1:
			if (item == null) return;
			MatterOrbItemHandler.addItemStacks(inventory.items[0], item);
			inventory.items[1] = null;
			if (item.isItemEqual(inventory.items[2])) updateOrb();
			return;
		case 2:
			ItemStack last = inventory.items[3];
			if (last == null) return;
			int n = last.stackSize - (item != null ? item.stackSize : 0);
			MatterOrbItemHandler.decrStackSize(inventory.items[0], selSlot, n);
			updateOrb();
		}
	}

	@Override
	public int insertAm(int g, int s, ItemStack item, ItemStack insert) {
		switch(s) {
		case 0: return item == null && MatterOrbItemHandler.isMatterOrb(insert) ? 1 : 0;
		case 1: return MatterOrbItemHandler.canInsert(inventory.items[0], insert) ? insert.stackSize : 0;
		default: return 0;
		}
	}

	@Override
	public void initContainer(DataContainer cont) {
		TileContainer container = (TileContainer)cont;
		cont.extraRef = new NBTTagList();
		container.addItemSlot(new SlotItemHandler(inventory, 0, 116, 66));
		container.addItemSlot(new GlitchSaveSlot(inventory, 1, 26, 66, false));
		container.addItemSlot(new GlitchSaveSlot(inventory, 2, 44, 66, false));
		
		container.addPlayerInventory(8, 100);
	}

	@Override
	public boolean transferStack(ItemStack item, int s, TileContainer cont) {
		if (s < cont.invPlayerS) cont.mergeItemStack(item, cont.invPlayerS, cont.invPlayerE, true);
		else if (MatterOrbItemHandler.isMatterOrb(item)) cont.mergeItemStack(item, 0, 1, false);
		else cont.mergeItemStack(item, 1, 2, false);
		return true;
	}

	@Override
	public int[] getSyncVariables() {
		return new int[]{mode, selSlot};
	}

	@Override
	public void setSyncVariable(int i, int v) {
		switch(i) {
		case 0: mode = (byte)(v & 3); break;
		case 1: selSlot = v;
		}
	}

}
