package cd4017be.automation.TileEntity;

import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemBlock;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.IFluidHandler;
import cd4017be.api.automation.AutomationRecipes;
import cd4017be.api.automation.AutomationRecipes.GCRecipe;
import cd4017be.api.automation.IEnergy;
import cd4017be.api.automation.PipeEnergy;
import cd4017be.automation.Config;
import cd4017be.lib.TileContainer;
import cd4017be.lib.TileEntityData;
import cd4017be.lib.templates.AutomatedTile;
import cd4017be.lib.templates.Inventory;
import cd4017be.lib.templates.Inventory.Component;
import cd4017be.lib.templates.SlotOutput;
import cd4017be.lib.templates.SlotTank;
import cd4017be.lib.templates.TankContainer;
import cd4017be.lib.templates.TankContainer.Tank;
import cd4017be.lib.util.Utils;

public class GraviCond extends AutomatedTile implements IEnergy, ISidedInventory, IFluidHandler 
{
	public static int itemWeight = 125;
	public static int blockWeight = 1000;
	public static float energyCost = 2.0F;
	public static float forceEnergy = 4.0F;
	public static int maxVoltage = 16000;
	
	public GraviCond()
	{
		/**
		 * ints: storage
		 */
		netData = new TileEntityData(2, 2, 1, 1);
		inventory = new Inventory(this, 4, new Component(0, 1, -1), new Component(1, 2, -1), new Component(2, 3, 1));
		tanks = new TankContainer(this, new Tank(Config.tankCap[1], -1).setIn(3)).setNetLong(1);
		energy = new PipeEnergy(maxVoltage, Config.Rcond[2]);
	}

	@Override
	public void updateEntity() 
	{
		super.updateEntity();
		if (worldObj.isRemote) return;
		int add = 0;
		netData.floats[0] = (float)energy.getEnergy(0, 1);
		int max = (int)Math.floor(netData.floats[0] / forceEnergy);
		if (netData.ints[0] > max) {
			add -= netData.ints[0] - max;
		}
		if (inventory.items[0] != null) {
			if (inventory.items[0].getItem() instanceof ItemBlock) add += inventory.items[0].stackSize * blockWeight;
			else add += inventory.items[0].stackSize * itemWeight;
			inventory.items[0] = null;
		}
		if (tanks.getAmount(0) > 0) {
			add += tanks.getAmount(0) * Math.max(netData.fluids[0].getFluid().getDensity(netData.fluids[0]), 1) / 1000;
			tanks.setFluid(0, null);
		}
		if (add != 0) {
			max = (int)Math.floor(energy.getEnergy(0, 1) / energyCost);
			if (add > max) add = max;
			netData.ints[0] += add;
			energy.addEnergy(-add * energyCost);
		}
		netData.ints[1] = 0;
		if (inventory.items[1] != null) {
			GCRecipe rcp = AutomationRecipes.getRecipeFor(inventory.items[1]);
			if (rcp != null) {
				netData.ints[1] = rcp.matter;
				if (netData.ints[0] >= netData.ints[1] && (inventory.items[2] == null || (Utils.itemsEqual(rcp.output, inventory.items[2]) && rcp.output.stackSize + inventory.items[2].stackSize <= rcp.output.getMaxStackSize()))) {
					netData.ints[0] -= netData.ints[1];
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
		netData.ints[0] = nbt.getInteger("matter");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) 
	{
		super.writeToNBT(nbt);
		nbt.setInteger("matter", netData.ints[0]);
	}

	@Override
	public void initContainer(TileContainer container) 
	{
		container.addPlayerInventory(8, 86);
		container.addEntitySlot(new Slot(this, 0, 8, 52));
		container.addEntitySlot(new Slot(this, 1, 116, 34));
		container.addEntitySlot(new SlotOutput(this, 2, 152, 34));
		container.addEntitySlot(new SlotTank(this, 3, 8, 16));
	}
	
	public int getEnergyScaled(int s)
	{
		int n = (int)(netData.floats[0] * (float)s / (energy.Umax * energy.Umax));
		return n > s ? s : n;
	}
	
	public int getMatterScaled(int s)
	{
		long max = (long)Math.floor(netData.floats[0] / forceEnergy);
		if (max == 0) return 0;
		long n = (long)netData.ints[0] * (long)s / (long)max;
		return (int)(n > s ? s : n);
	}
	
	public int getProgressScaled(int s)
	{
		if (netData.ints[1] == 0) return 0;
		long n = (long)netData.ints[0] * (long)s / (long)netData.ints[1];
		return (int)(n > s ? s : n);
	}
	
}
