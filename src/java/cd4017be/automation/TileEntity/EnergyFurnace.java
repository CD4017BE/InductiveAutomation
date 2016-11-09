package cd4017be.automation.TileEntity;

import net.minecraft.network.PacketBuffer;

import java.io.IOException;

import cd4017be.api.automation.PipeEnergy;
import cd4017be.automation.Config;
import cd4017be.lib.Gui.DataContainer;
import cd4017be.lib.Gui.DataContainer.IGuiData;
import cd4017be.lib.Gui.SlotItemType;
import cd4017be.lib.Gui.TileContainer;
import cd4017be.lib.templates.AutomatedTile;
import cd4017be.lib.templates.Inventory;
import cd4017be.lib.util.Utils;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.items.SlotItemHandler;

/**
 *
 * @author CD4017BE
 */
public class EnergyFurnace extends AutomatedTile implements IGuiData {

	public static float Euse = 200F;
	private float powerScale;
	public int Rw;
	public float netF0, Uc;

	public EnergyFurnace() {
		inventory = new Inventory(5, 2, null).group(0, 0, 2, Utils.IN).group(1, 2, 4, Utils.OUT);
		energy = new PipeEnergy(Config.Umax[1], Config.Rcond[1]);
		Rw = Config.Rmin;
		powerScale = Config.Pscale;
	}
	
	@Override
	public void update() 
	{
		super.update();
		if (this.worldObj.isRemote) return;
		putItemStack(0, 1);
		putItemStack(2, 3);
		if (inventory.items[4] == null && inventory.items[1] != null)
		{
			ItemStack item = FurnaceRecipes.instance().getSmeltingResult(inventory.items[1]);
			if (item != null)
			{
				inventory.extractItem(1, 1, false);
				inventory.items[4] = item.copy();
			}
		}
		Uc = (float)energy.Ucap;
		double power = energy.getEnergy(0, Rw) / 1000F;
		if (inventory.items[4] != null)
		{
			if (netF0 < Euse)
			{
				netF0 += power;
				energy.Ucap *= powerScale;
			}
			if (netF0 >= Euse)
			{
				putItemStack(4, 2);
				if (inventory.items[4] == null) netF0 -= Euse;
			}
		}
	}
	
	private void putItemStack(int src, int dest)
	{
		if (inventory.items[src] == null) return;
		if (inventory.items[dest] == null)
		{
			inventory.items[dest] = inventory.items[src].copy();
			inventory.items[src] = null;
		} else
		if (Utils.itemsEqual(inventory.items[dest], inventory.items[src]))
		{
			int i = inventory.items[dest].getMaxStackSize() - inventory.items[dest].stackSize;
			if (inventory.items[src].stackSize < i) i = inventory.items[src].stackSize;
			inventory.items[src].stackSize -= i;
			inventory.items[dest].stackSize += i;
			if (inventory.items[src].stackSize <= 0) inventory.items[src] = null;
		}
	}

	@Override
	protected void customPlayerCommand(byte cmd, PacketBuffer dis, EntityPlayerMP player) throws IOException 
	{
		if (cmd == 0) {
			Rw = dis.readInt();
			if (Rw < Config.Rmin) Rw = Config.Rmin;
			powerScale = (float)Math.sqrt(1.0D - 1.0D / (double)Rw);
		}
	}
	
	public float getProgress() {
		return netF0 / Euse;
	}

	public float getPower() {
		return Uc * Uc / (float)Rw / 200000F;
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) 
	{
		nbt.setFloat("progress", netF0);
		nbt.setInteger("resistor", Rw);
		nbt.setFloat("pScale", powerScale);
		return super.writeToNBT(nbt);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) 
	{
		super.readFromNBT(nbt);
		netF0 = nbt.getFloat("progress");
		Rw = nbt.getInteger("resistor");
		powerScale = nbt.getFloat("pScale");
	}
	
	@Override
	public void initContainer(DataContainer cont) {
		TileContainer container = (TileContainer)cont;
		container.addItemSlot(new SlotItemHandler(inventory, 0, 44, 34));
		container.addItemSlot(new SlotItemHandler(inventory, 1, 62, 34));
		
		container.addItemSlot(new SlotItemType(inventory, 2, 116, 34));
		container.addItemSlot(new SlotItemType(inventory, 3, 134, 34));
		
		container.addPlayerInventory(8, 86);
	}

	@Override
	public boolean transferStack(ItemStack item, int s, TileContainer container) {
		if (s < container.invPlayerS) container.mergeItemStack(item, container.invPlayerS, container.invPlayerE, false);
		else container.mergeItemStack(item, 0, 2, false);
		return true;
	}

	@Override
	public int[] getSyncVariables() {
		return new int[]{Rw, Float.floatToIntBits(netF0), Float.floatToIntBits(Uc)};
	}

	@Override
	public void setSyncVariable(int i, int v) {
		switch(i) {
		case 0: Rw = v; break;
		case 1: netF0 = Float.intBitsToFloat(v); break;
		case 2: Uc = Float.intBitsToFloat(v); break;
		}
	}

}
