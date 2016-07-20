package cd4017be.automation.TileEntity;

import java.io.IOException;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.math.BlockPos;
import cd4017be.automation.shaft.HeatReservoir;
import cd4017be.automation.shaft.IHeatReservoir;
import cd4017be.automation.shaft.IHeatReservoir.IHeatStorage;
import cd4017be.lib.TileContainer;
import cd4017be.lib.TileEntityData;
import cd4017be.lib.templates.AutomatedTile;
import cd4017be.lib.templates.Inventory;
import cd4017be.lib.templates.Inventory.Component;

public class SolidFuelHeater extends AutomatedTile implements IHeatStorage {
	
	private static final float FuelEnergy = 10000F;
	private static final int BurnRate = 8;
	
	HeatReservoir heat;
	
	public SolidFuelHeater() {
		//ints: maxFuel, curFuel, burn; floats: targetTemp, Temp
		netData = new TileEntityData(1, 3, 2, 0);
		heat = new HeatReservoir(10000F);
        inventory = new Inventory(this, 2, new Component(0, 2, -1));
        netData.ints[2] = 1;
        netData.floats[0] = 300F;
	}
	
	@Override
	public void update() {
		super.update();
		if (worldObj.isRemote) return;
		heat.update(this);
		if (netData.ints[1] < netData.ints[2] && heat.T < netData.floats[0] && !worldObj.isBlockPowered(pos)) {
			for (int i = 0; i < 2; i++) {
				int n = TileEntityFurnace.getItemBurnTime(inventory.items[i]);
				if (n != 0) {
					netData.ints[0] = n;
					netData.ints[1] += netData.ints[0];
					this.decrStackSize(i, 1);
					break;
			    }
			}
		}
		if (netData.ints[1] != 0) {
			int p = Math.min(netData.ints[2], netData.ints[1]);
			netData.ints[1] -= p;
			heat.T += p * FuelEnergy / heat.C;
		}
		netData.floats[1] = heat.T;
	}
	
	@Override
	protected void customPlayerCommand(byte cmd, PacketBuffer dis, EntityPlayerMP player) throws IOException {
		if (cmd == 0) netData.floats[0] = dis.readFloat();
		else if (cmd == 1 && netData.ints[2] < BurnRate) netData.ints[2]++;
		else if (cmd == 2 && netData.ints[2] > 1) netData.ints[2]--;
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
	public IHeatReservoir getHeat(byte side) {
		return heat;
	}

	@Override
	public float getHeatRes(byte side) {
		return (side^1) == this.getOrientation() ? HeatReservoir.def_con : HeatReservoir.def_discon;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		heat.load(nbt, "heat");
		netData.floats[0] = nbt.getFloat("refT");
		netData.ints[1] = nbt.getInteger("fuel");
		netData.ints[0] = netData.ints[1];
		netData.ints[2] = nbt.getInteger("burn");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		heat.save(nbt, "heat");
		nbt.setFloat("refT", netData.floats[0]);
		nbt.setInteger("fuel", netData.ints[1]);
		nbt.setInteger("burn", netData.ints[2]);
        return super.writeToNBT(nbt);
	}

	@Override
	public void initContainer(TileContainer container) {
		container.addEntitySlot(new Slot(this, 0, 26, 16));
		container.addEntitySlot(new Slot(this, 1, 26, 34));
		container.addPlayerInventory(8, 68);
	}

	@Override
	public int[] stackTransferTarget(ItemStack item, int s, TileContainer container) {
		int[] pi = container.getPlayerInv();
		if (s < pi[0] || s >= pi[1]) return pi;
        else return new int[]{0, 2};
	}

}
