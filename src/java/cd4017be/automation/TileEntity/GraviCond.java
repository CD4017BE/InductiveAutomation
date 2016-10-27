package cd4017be.automation.TileEntity;

import net.minecraft.item.ItemBlock;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.items.SlotItemHandler;
import cd4017be.api.automation.PipeEnergy;
import cd4017be.api.recipes.AutomationRecipes;
import cd4017be.api.recipes.AutomationRecipes.GCRecipe;
import cd4017be.automation.Config;
import cd4017be.lib.Gui.DataContainer;
import cd4017be.lib.Gui.DataContainer.IGuiData;
import cd4017be.lib.Gui.SlotItemType;
import cd4017be.lib.Gui.SlotTank;
import cd4017be.lib.Gui.TileContainer;
import cd4017be.lib.templates.AutomatedTile;
import cd4017be.lib.templates.Inventory;
import cd4017be.lib.templates.TankContainer;
import cd4017be.lib.util.Utils;

public class GraviCond extends AutomatedTile implements IGuiData
{
	public static int itemWeight = 125;
	public static int blockWeight = 1000;
	public static float energyCost = 2.0F;
	public static float forceEnergy = 4.0F;
	public static int maxVoltage = 16000;
	public int netI0, netI1;
	public float netF0;
	
	public GraviCond()
	{
		/**
		 * ints: storage
		 */
		inventory = new Inventory(4, 3, null).group(0, 0, 1, Utils.IN).group(1, 1, 2, Utils.IN).group(2, 2, 3, Utils.OUT);
		tanks = new TankContainer(1, 1).tank(0, Config.tankCap[1], Utils.IN, 3, -1);
		energy = new PipeEnergy(maxVoltage, Config.Rcond[2]);
	}

	@Override
	public void update() 
	{
		super.update();
		if (worldObj.isRemote) return;
		int add = 0;
		netF0 = (float)energy.getEnergy(0, 1);
		int max = (int)Math.floor(netF0 / forceEnergy);
		if (netI0 > max) {
			add -= netI0 - max;
		}
		if (inventory.items[0] != null) {
			if (inventory.items[0].getItem() instanceof ItemBlock) add += inventory.items[0].stackSize * blockWeight;
			else add += inventory.items[0].stackSize * itemWeight;
			inventory.items[0] = null;
		}
		if (tanks.getAmount(0) > 0) {
			add += tanks.getAmount(0) * Math.max(tanks.fluids[0].getFluid().getDensity(tanks.fluids[0]), 1) / 1000;
			tanks.setFluid(0, null);
		}
		if (add != 0) {
			max = (int)Math.floor(energy.getEnergy(0, 1) / energyCost);
			if (add > max) add = max;
			netI0 += add;
			energy.addEnergy(-add * energyCost);
		}
		netI1 = 0;
		if (inventory.items[1] != null) {
			GCRecipe rcp = AutomationRecipes.getRecipeFor(inventory.items[1]);
			if (rcp != null) {
				netI1 = rcp.matter;
				if (netI0 >= netI1 && (inventory.items[2] == null || (Utils.itemsEqual(rcp.output, inventory.items[2]) && rcp.output.stackSize + inventory.items[2].stackSize <= rcp.output.getMaxStackSize()))) {
					netI0 -= netI1;
					inventory.items[1].stackSize -= rcp.input.stackSize;
					if (inventory.items[1].stackSize <= 0) inventory.items[1] = null;
					if (inventory.items[2] == null) inventory.items[2] = rcp.output.copy();
					else inventory.items[2].stackSize += rcp.output.stackSize;
				}
			}
		}
	}

	@Override
	public int redstoneLevel(int s, boolean str) 
	{
		return this.getMatterScaled(16);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) 
	{
		super.readFromNBT(nbt);
		netI0 = nbt.getInteger("matter");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) 
	{
		nbt.setInteger("matter", netI0);
		return super.writeToNBT(nbt);
	}

	@Override
	public void initContainer(DataContainer cont) {
		TileContainer container = (TileContainer)cont;
		cont.refInts = new int[3];
		container.addPlayerInventory(8, 86);
		container.addItemSlot(new SlotItemHandler(inventory, 0, 8, 52));
		container.addItemSlot(new SlotItemHandler(inventory, 1, 116, 34));
		container.addItemSlot(new SlotItemType(inventory, 2, 152, 34));
		container.addItemSlot(new SlotTank(inventory, 3, 8, 16));
	}
	
	public int getEnergyScaled(int s)
	{
		int n = (int)(netF0 * (float)s / (energy.Umax * energy.Umax));
		return n > s ? s : n;
	}
	
	public int getMatterScaled(int s)
	{
		long max = (long)Math.floor(netF0 / forceEnergy);
		if (max == 0) return 0;
		long n = (long)netI0 * (long)s / (long)max;
		return (int)(n > s ? s : n);
	}
	
	public int getProgressScaled(int s)
	{
		if (netI1 == 0) return 0;
		long n = (long)netI0 * (long)s / (long)netI1;
		return (int)(n > s ? s : n);
	}

	@Override
	public int[] getSyncVariables() {
		return new int[]{netI0, netI1, Float.floatToIntBits(netF0)};
	}

	@Override
	public void setSyncVariable(int i, int v) {
		switch(i) {
		case 0: netI0 = v; break;
		case 1: netI1 = v; break;
		case 2: netF0 = Float.intBitsToFloat(v); break;
		}
	}

}
