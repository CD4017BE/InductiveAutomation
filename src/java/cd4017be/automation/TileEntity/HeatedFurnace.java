package cd4017be.automation.TileEntity;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.SlotItemHandler;
import cd4017be.automation.shaft.HeatReservoir;
import cd4017be.automation.shaft.IHeatReservoir;
import cd4017be.automation.shaft.IHeatReservoir.IHeatStorage;
import cd4017be.lib.Gui.DataContainer;
import cd4017be.lib.Gui.DataContainer.IGuiData;
import cd4017be.lib.Gui.SlotItemType;
import cd4017be.lib.Gui.TileContainer;
import cd4017be.lib.templates.AutomatedTile;
import cd4017be.lib.templates.Inventory;
import cd4017be.lib.util.Utils;

public class HeatedFurnace extends AutomatedTile implements IHeatStorage, IGuiData {

	public static final float NeededTemp = 1200F, TRwork = 20, Energy = 250000F;
	public HeatReservoir heat;
	private boolean done = true;
	public int netI0;
	public float netF0, netF1;

	public HeatedFurnace() {
		inventory = new Inventory(3, 2, null).group(0, 0, 1, Utils.IN).group(1, 1, 2, Utils.OUT);
		heat = new HeatReservoir(10000F);
	}

	@Override
	public void update() {
		super.update();
		if (worldObj.isRemote) return;
		heat.update(this);
		float e = (heat.T - NeededTemp) * heat.C;
		if (inventory.items[2] == null && heat.T > NeededTemp && inventory.items[0] != null && FurnaceRecipes.instance().getSmeltingResult(inventory.items[0]) != null) {
			inventory.items[2] = inventory.extractItem(0, (int)Math.ceil(e / Energy), false);
			done = false;
		}
		if (inventory.items[2] != null) {
			if (!done){
				int sz = inventory.items[2].stackSize;
				float req = (float)sz * Energy;
				if (netF0 < req && e > 0) {
					float dQ = e / TRwork;
					netF0 += dQ;
					heat.addHeat(-dQ);
				}
				if (netF0 >= req) {
					ItemStack item = FurnaceRecipes.instance().getSmeltingResult(inventory.items[2]);
					if (item != null) {
						netF0 -= req;
						inventory.items[2] = item.copy();
						inventory.items[2].stackSize *= sz;
					}
					done = true;
				}
			}
			if (done) {
				int n = inventory.items[2].getMaxStackSize();
				if (inventory.items[1] == null) 
					inventory.items[1] = inventory.extractItem(2, n, false);
				else if (inventory.items[1].isItemEqual(inventory.items[2]) && (n -= inventory.items[1].stackSize) > 0) 
					inventory.items[1].stackSize += inventory.extractItem(2, n, false).stackSize;
			}
		} else if (netF0 > 0) {
			heat.addHeat(netF0);
			netF0 = 0;
		}
		netF1 = heat.T;
		netI0 = inventory.items[2] == null ? 0 : inventory.items[2].stackSize;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		heat.load(nbt, "heat");
		netF0 = nbt.getFloat("progress");
		done = nbt.getBoolean("done");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		heat.save(nbt, "heat");
		nbt.setFloat("progress", netF0);
		nbt.setBoolean("done", done);
		return super.writeToNBT(nbt);
	}
	
	@Override
	public void onNeighborBlockChange(Block b) {
		heat.check = true;
	}

	@Override
	public void onNeighborTileChange(BlockPos pos) {
		heat.check = true;
	}

	@Override
	public void initContainer(DataContainer container) {
		TileContainer cont = (TileContainer)container;
		cont.refInts = new int[3];
		cont.addItemSlot(new SlotItemHandler(inventory, 0, 17, 16));
		cont.addItemSlot(new SlotItemType(inventory, 1, 71, 16));
		cont.addPlayerInventory(8, 68);
	}

	@Override
	public boolean transferStack(ItemStack item, int s, TileContainer container) {
		if (s < container.invPlayerS) container.mergeItemStack(item, container.invPlayerS, container.invPlayerE, false);
		else container.mergeItemStack(item, 0, 1, false);
		return true;
	}

	@Override
	public IHeatReservoir getHeat(byte side) {
		return heat;
	}

	@Override
	public float getHeatRes(byte side) {
		return (side^1) == this.getOrientation() ? HeatReservoir.def_con : HeatReservoir.def_discon;
	}

	public int getProgressScaled(int s) {
		int i = netI0 == 0 ? 0 : (int)((float)s * netF0 / Energy / (float)netI0);
		return i < s ? i : s;
	}

	@Override
	public int[] getSyncVariables() {
		return new int[]{netI0, Float.floatToIntBits(netF0), Float.floatToIntBits(netF1)};
	}

	@Override
	public void setSyncVariable(int i, int v) {
		switch(i) {
		case 0: netI0 = v; break;
		case 1: netF0 = Float.intBitsToFloat(v); break;
		case 2: netF1 = Float.intBitsToFloat(v); break;
		}
	}

}
