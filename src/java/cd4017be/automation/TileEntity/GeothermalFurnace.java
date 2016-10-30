package cd4017be.automation.TileEntity;

import cd4017be.automation.Config;
import cd4017be.automation.Objects;
import cd4017be.lib.Gui.DataContainer;
import cd4017be.lib.Gui.DataContainer.IGuiData;
import cd4017be.lib.Gui.SlotItemType;
import cd4017be.lib.Gui.SlotTank;
import cd4017be.lib.Gui.TileContainer;
import cd4017be.lib.Gui.TileContainer.TankSlot;
import cd4017be.lib.templates.AutomatedTile;
import cd4017be.lib.templates.Inventory;
import cd4017be.lib.templates.Inventory.IAccessHandler;
import cd4017be.lib.templates.TankContainer;
import cd4017be.lib.util.ItemFluidUtil;
import cd4017be.lib.util.Utils;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.SlotItemHandler;

/**
 *
 * @author CD4017BE
 */
public class GeothermalFurnace extends AutomatedTile implements IGuiData, IAccessHandler {

	public static int Euse = 160;
	private boolean melting = false;
	public int fuel, burn, heat, melt, progress;
	
	public GeothermalFurnace()
	{
		inventory = new Inventory(7, 4, null).group(0, 4, 5, Utils.IN).group(1, 5, 6, Utils.OUT).group(2, 0, 1, Utils.IN).group(3, 1, 2, Utils.ACC);
		tanks = new TankContainer(1, 1).tank(0, Config.tankCap[1], Utils.ACC, 2, 3, Objects.L_lava);
	}

	@Override
	public void update() 
	{
		super.update();
		if (worldObj.isRemote) return;
		//Fuel Burn
		int dm = 2;
		if (burn > 0)
		{
			if (burn - dm < 0) dm = burn;
			burn -= dm;
			heat += dm;
		}
		if (burn <= 0 && heat < 640) burnItem();
		//Lava melt/cool 
		dm = 18 - heat / 32;
		if (!melting && dm < 0 && inventory.items[1] != null && inventory.items[1].getItem() == Item.getItemFromBlock(Blocks.STONE))
		{
			inventory.extractItem(1, 1, false);
			melting = true;
			if (melt >= 2000) melt -= 2000;
		} else
		if (!melting && dm > 0 && tanks.getAmount(0) >= 100)
		{
			tanks.drain(0, 100, true);
			melting = true;
			if (melt <= 0) melt += 2000; 
		}
		if (melting && ((dm < 0 && melt < 2000) || (dm > 0 && melt > 0)))
		{
			if (melt - dm > 2000) dm = melt - 2000;
			if (melt - dm < 0) dm = melt;
			heat += dm;
			melt -= dm;
		}
		if (melting && melt >= 2000 && tanks.fill(0, new FluidStack(Objects.L_lava, 100), false) == 100) {
			tanks.fill(0, new FluidStack(Objects.L_lava, 100), true);
			melting = false;
		} else if (melting && melt <= 0 && ItemFluidUtil.putInSlots(inventory, new ItemStack(Blocks.STONE), 1) == null) {
			melting = false;
		}
		if (heat > 640) heat = 640;
		//process Item
		if (inventory.items[6] == null && heat + progress >= Euse && inventory.items[4] != null)
		{
			ItemStack item = FurnaceRecipes.instance().getSmeltingResult(inventory.items[4]);
			if (item != null)
			{
				inventory.extractItem(4, 1, false);
				inventory.items[6] = item.copy();
			}
		}
		if (inventory.items[6] != null)
		{
			if (progress < Euse)
			{
				int dp = 1 + heat * 7 / 320;
				if (dp > heat) dp = heat;
				progress += dp;
				heat -= dp;
			}
			if (progress >= Euse)
			{
				inventory.items[6] = ItemFluidUtil.putInSlots(inventory, inventory.items[6], 5);
				if (inventory.items[6] == null) progress -= Euse;
			}
		}
	}
	
	private void burnItem()
	{
		if (inventory.items[0] != null)
		{
			int n = TileEntityFurnace.getItemBurnTime(inventory.items[0]);
			if (n != 0)
			{
				fuel = n;
				burn = fuel;
				inventory.extractItem(0, 1, false);
			}
		}
	}

	public float getMelt() {
		return melt < 0 ? 0F : melt > 2000 ? 1F : (float)melt / 2000F;
	}

	public float getBurn() {
		return (float)burn / (float)fuel;
	}

	public float getHeat() {
		return (float)heat / 640F;
	}
	
	public float getProgress() {
		return (float)progress / (float)Euse;
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) 
	{
		nbt.setInteger("progress", progress);
		nbt.setInteger("heat", heat);
		nbt.setInteger("melt", melt);
		nbt.setInteger("burn", burn);
		nbt.setBoolean("melting", melting);
		return super.writeToNBT(nbt);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) 
	{
		super.readFromNBT(nbt);
		progress = nbt.getInteger("progress");
		heat = nbt.getInteger("heat");
		melt = nbt.getInteger("melt");
		burn = nbt.getInteger("burn");
		fuel = burn;
		melting = nbt.getBoolean("melting");
	}

	@Override
	public void setSlot(int g, int s, ItemStack item) {
		inventory.items[s] = item;
	}

	@Override
	public int insertAm(int g, int s, ItemStack item, ItemStack insert) {
		if (s == 1 && insert.getItem() != Item.getItemFromBlock(Blocks.STONE)) return 0;
		return super.insertAm(g, s, item, insert);
	}

	@Override
	public void initContainer(DataContainer cont) {
		TileContainer container = (TileContainer)cont;
		cont.refInts = new int[5];
		container.addItemSlot(new SlotItemHandler(inventory, 0, 62, 52));
		container.addItemSlot(new SlotItemType(inventory, 1, 62, 16, new ItemStack[]{new ItemStack(Blocks.STONE)}));
		container.addItemSlot(new SlotTank(inventory, 2, 8, 34));
		container.addItemSlot(new SlotTank(inventory, 3, 26, 34));
		container.addItemSlot(new SlotItemHandler(inventory, 4, 98, 34));
		container.addItemSlot(new SlotItemType(inventory, 5, 152, 34));
		
		container.addPlayerInventory(8, 86);
		
		container.addTankSlot(new TankSlot(tanks, 0, 8, 16, (byte)0x23));
	}

	@Override
	public boolean transferStack(ItemStack item, int s, TileContainer container) {
		if (s < container.invPlayerS) container.mergeItemStack(item, container.invPlayerS, container.invPlayerE, false);
		else container.mergeItemStack(item, 4, 5, false);
		return true;
	}

	@Override
	public int[] getSyncVariables() {
		return new int[]{fuel, burn, heat, melt, progress};
	}

	@Override
	public void setSyncVariable(int i, int v) {
		switch(i) {
		case 0: fuel = v; break;
		case 1: burn = v; break;
		case 2: heat = v; break;
		case 3: melt = v; break;
		case 4: progress = v; break;
		}
	}

}
