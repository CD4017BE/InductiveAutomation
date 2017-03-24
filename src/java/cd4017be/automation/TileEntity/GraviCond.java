package cd4017be.automation.TileEntity;

import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
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
import cd4017be.lib.Gui.TileContainer.ISlotClickHandler;
import cd4017be.lib.templates.AutomatedTile;
import cd4017be.lib.templates.Inventory;
import cd4017be.lib.templates.TankContainer;
import cd4017be.lib.util.Utils;

public class GraviCond extends AutomatedTile implements IGuiData, ISlotClickHandler {
	public static int itemWeight = 125;
	public static int blockWeight = 1000;
	public static float energyCost = 2.0F;
	public static float forceEnergy = 4.0F;
	public static int Umax = 16000;
	public int matter, need;
	public float Estor;
	
	public GraviCond()
	{
		/**
		 * ints: storage
		 */
		inventory = new Inventory(4, 3, null).group(0, 0, 1, Utils.IN).group(1, 1, 2, Utils.IN).group(2, 2, 3, Utils.OUT);
		tanks = new TankContainer(1, 1).tank(0, Config.tankCap[1], Utils.IN, 3, -1);
		energy = new PipeEnergy(Umax, Config.Rcond[2]);
	}

	@Override
	public void update() 
	{
		super.update();
		if (worldObj.isRemote) return;
		int add = 0;
		Estor = (float)energy.getEnergy(0, 1);
		int max = (int)Math.floor(Estor / forceEnergy);
		if (matter > max) {
			add -= matter - max;
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
			matter += add;
			energy.addEnergy(-add * energyCost);
		}
		need = 0;
		if (inventory.items[1] != null) {
			GCRecipe rcp = AutomationRecipes.getRecipeFor(inventory.items[1]);
			if (rcp != null) {
				need = rcp.matter;
				if (matter >= need && (inventory.items[2] == null || (Utils.itemsEqual(rcp.output, inventory.items[2]) && rcp.output.stackSize + inventory.items[2].stackSize <= rcp.output.getMaxStackSize()))) {
					matter -= need;
					inventory.items[1].stackSize -= rcp.input.stackSize;
					if (inventory.items[1].stackSize <= 0) inventory.items[1] = null;
					if (inventory.items[2] == null) inventory.items[2] = rcp.output.copy();
					else inventory.items[2].stackSize += rcp.output.stackSize;
				}
			}
		}
	}

	@Override
	public int redstoneLevel(int s, boolean str) {
		return 256 - (int)(this.getMatter() * 256F);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) 
	{
		super.readFromNBT(nbt);
		matter = nbt.getInteger("matter");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) 
	{
		nbt.setInteger("matter", matter);
		return super.writeToNBT(nbt);
	}

	@Override
	public void initContainer(DataContainer cont) {
		TileContainer container = (TileContainer)cont;
		container.clickHandler = this;
		container.addItemSlot(new SlotItemHandler(inventory, 0, 8, 52));
		container.addItemSlot(new SlotItemHandler(inventory, 1, 116, 34));
		container.addItemSlot(new SlotItemType(inventory, 2, 152, 34));
		container.addItemSlot(new SlotTank(inventory, 3, 8, 16));
		
		container.addPlayerInventory(8, 86);
	}

	@Override
	public boolean transferStack(ItemStack item, int s, TileContainer container) {
		if (s < container.invPlayerS) container.mergeItemStack(item, container.invPlayerS, container.invPlayerE, false);
		else container.mergeItemStack(item, 0, 2, true);
		return true;
	}

	public float getEnergy() {
		return Estor / (energy.Umax * energy.Umax);
	}

	public float getMatter() {
		return (float)matter / Estor * forceEnergy;
	}

	public float getProgress() {
		return (float)matter / (float)need;
	}

	@Override
	public int[] getSyncVariables() {
		return new int[]{matter, need, Float.floatToIntBits(Estor)};
	}

	@Override
	public void setSyncVariable(int i, int v) {
		switch(i) {
		case 0: matter = v; break;
		case 1: need = v; break;
		case 2: Estor = Float.intBitsToFloat(v); break;
		}
	}

}
