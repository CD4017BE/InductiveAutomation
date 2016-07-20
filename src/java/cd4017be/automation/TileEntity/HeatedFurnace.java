package cd4017be.automation.TileEntity;

import net.minecraft.block.Block;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import cd4017be.automation.shaft.HeatReservoir;
import cd4017be.automation.shaft.IHeatReservoir;
import cd4017be.automation.shaft.IHeatReservoir.IHeatStorage;
import cd4017be.lib.TileContainer;
import cd4017be.lib.TileEntityData;
import cd4017be.lib.templates.AutomatedTile;
import cd4017be.lib.templates.Inventory;
import cd4017be.lib.templates.Inventory.Component;
import cd4017be.lib.templates.SlotOutput;

public class HeatedFurnace extends AutomatedTile implements IHeatStorage {
	public static final float NeededTemp = 1200F, TRwork = 20, Energy = 250000F;
	public HeatReservoir heat;
	private boolean done = true;
	
	public HeatedFurnace() {
		netData = new TileEntityData(1, 1, 2, 0);
		inventory = new Inventory(this, 3, new Component(0, 1, -1), new Component(1, 2, 1));
		heat = new HeatReservoir(10000F);
	}

	@Override
	public void update() {
		super.update();
		if (worldObj.isRemote) return;
		heat.update(this);
		float e = (heat.T - NeededTemp) * heat.C;
		if (inventory.items[2] == null && heat.T > NeededTemp && inventory.items[0] != null && FurnaceRecipes.instance().getSmeltingResult(inventory.items[0]) != null) {
			inventory.items[2] = this.decrStackSize(0, (int)Math.ceil(e / Energy));
			done = false;
		}
		if (inventory.items[2] != null) {
			if (!done){
				int sz = inventory.items[2].stackSize;
				float req = (float)sz * Energy;
				if (netData.floats[0] < req && e > 0) {
					float dQ = e / TRwork;
					netData.floats[0] += dQ;
					heat.addHeat(-dQ);
				}
				if (netData.floats[0] >= req) {
					ItemStack item = FurnaceRecipes.instance().getSmeltingResult(inventory.items[2]);
					if (item != null) {
						netData.floats[0] -= req;
						inventory.items[2] = item.copy();
						inventory.items[2].stackSize *= sz;
					}
					done = true;
				}
			}
			if (done) {
				int n = inventory.items[2].getMaxStackSize();
				if (inventory.items[1] == null) 
					inventory.items[1] = this.decrStackSize(2, n);
				else if (inventory.items[1].isItemEqual(inventory.items[2]) && (n -= inventory.items[1].stackSize) > 0) 
					inventory.items[1].stackSize += this.decrStackSize(2, n).stackSize;
			}
		} else if (netData.floats[0] > 0) {
			heat.addHeat(netData.floats[0]);
			netData.floats[0] = 0;
		}
		netData.floats[1] = heat.T;
		netData.ints[0] = inventory.items[2] == null ? 0 : inventory.items[2].stackSize;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		heat.load(nbt, "heat");
		netData.floats[0] = nbt.getFloat("progress");
		done = nbt.getBoolean("done");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		heat.save(nbt, "heat");
		nbt.setFloat("progress", netData.floats[0]);
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
	public void initContainer(TileContainer container) {
		container.addEntitySlot(new Slot(this, 0, 17, 16));
		container.addEntitySlot(new SlotOutput(this, 1, 71, 16));
		container.addPlayerInventory(8, 68);
	}

	@Override
	public int[] stackTransferTarget(ItemStack item, int s, TileContainer container) {
		int[] pi = container.getPlayerInv();
        if (s < pi[0] || s >= pi[1]) return pi;
        else return new int[]{0, 1};
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
		int i = netData.ints[0] == 0 ? 0 : (int)((float)s * netData.floats[0] / Energy / (float)netData.ints[0]);
        return i < s ? i : s;
	}

}
