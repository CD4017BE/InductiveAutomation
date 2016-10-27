package cd4017be.automation.TileEntity;

import net.minecraft.network.PacketBuffer;

import java.io.IOException;

import cd4017be.automation.Config;
import cd4017be.lib.Gui.DataContainer;
import cd4017be.lib.Gui.DataContainer.IGuiData;
import cd4017be.lib.Gui.SlotTank;
import cd4017be.lib.Gui.TileContainer;
import cd4017be.lib.templates.AutomatedTile;
import cd4017be.lib.templates.Inventory;
import cd4017be.lib.templates.TankContainer;
import cd4017be.lib.util.Utils;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.items.SlotItemHandler;

/**
 *
 * @author CD4017BE
 */
public class Trash extends AutomatedTile implements IGuiData {
	
	public int netI0;
	
	public Trash() {
		inventory = new Inventory(2, 1, null).group(0, 0, 1, Utils.IN);
		tanks = new TankContainer(1, 1).tank(0, Config.tankCap[1], Utils.IN, 1, -1);
	}

	@Override
	public void update() 
	{
		super.update();
		if (worldObj.isRemote) return;
		if ((netI0 & 1) != 0) inventory.items[0] = null;
		if ((netI0 & 2) != 0) tanks.setFluid(0, null);
	}

	@Override
	protected void customPlayerCommand(byte cmd, PacketBuffer dis, EntityPlayerMP player) throws IOException 
	{
		if (cmd == 0) netI0 ^= 1;
		else if (cmd == 1) netI0 ^= 2;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) 
	{
		super.readFromNBT(nbt);
		netI0 = nbt.getByte("active");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) 
	{
		nbt.setByte("active", (byte)netI0);
		return super.writeToNBT(nbt);
	}

	@Override
	public void initContainer(DataContainer cont) {
		TileContainer container = (TileContainer)cont;
		container.addItemSlot(new SlotItemHandler(inventory, 0, 134, 16));
		container.addItemSlot(new SlotTank(inventory, 1, 26, 16));
		container.addPlayerInventory(8, 50);
	}

	@Override
	public boolean transferStack(ItemStack item, int s, TileContainer container) {
		if (s < container.invPlayerS) container.mergeItemStack(item, container.invPlayerS, container.invPlayerE, false);
		else container.mergeItemStack(item, 0, 1, false);
		return true;
	}

	@Override
	public int[] getSyncVariables() {
		return new int[]{netI0};
	}

	@Override
	public void setSyncVariable(int i, int v) {
		netI0 = v;
	}

}
