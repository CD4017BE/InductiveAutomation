package cd4017be.automation.TileEntity;

import java.io.IOException;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.SlotItemHandler;
import cd4017be.automation.shaft.HeatReservoir;
import cd4017be.automation.shaft.IHeatReservoir;
import cd4017be.automation.shaft.IHeatReservoir.IHeatStorage;
import cd4017be.lib.Gui.DataContainer;
import cd4017be.lib.Gui.DataContainer.IGuiData;
import cd4017be.lib.Gui.TileContainer;
import cd4017be.lib.templates.AutomatedTile;
import cd4017be.lib.templates.Inventory;
import cd4017be.lib.util.Utils;

public class SolidFuelHeater extends AutomatedTile implements IHeatStorage, IGuiData {

	private static final float FuelEnergy = 10000F;
	private static final int BurnRate = 8;
	public int netI0, netI1, netI2;
	public float netF0, netF1;
	HeatReservoir heat;

	public SolidFuelHeater() {
		//ints: maxFuel, curFuel, burn; floats: targetTemp, Temp
		heat = new HeatReservoir(10000F);
		inventory = new Inventory(2, 1, null).group(0, 0, 2, Utils.IN);
		netI2 = 1;
		netF0 = 300F;
	}

	@Override
	public void update() {
		super.update();
		if (worldObj.isRemote) return;
		heat.update(this);
		if (netI1 < netI2 && heat.T < netF0 && !worldObj.isBlockPowered(pos)) {
			for (int i = 0; i < 2; i++) {
				int n = TileEntityFurnace.getItemBurnTime(inventory.items[i]);
				if (n != 0) {
					netI0 = n;
					netI1 += netI0;
					inventory.extractItem(i, 1, false);
					break;
				}
			}
		}
		if (netI1 != 0) {
			int p = Math.min(netI2, netI1);
			netI1 -= p;
			heat.T += p * FuelEnergy / heat.C;
		}
		netF1 = heat.T;
	}

	@Override
	protected void customPlayerCommand(byte cmd, PacketBuffer dis, EntityPlayerMP player) throws IOException {
		if (cmd == 0) netF0 = dis.readFloat();
		else if (cmd == 1 && netI2 < BurnRate) netI2++;
		else if (cmd == 2 && netI2 > 1) netI2--;
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
		netF0 = nbt.getFloat("refT");
		netI1 = nbt.getInteger("fuel");
		netI0 = netI1;
		netI2 = nbt.getInteger("burn");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		heat.save(nbt, "heat");
		nbt.setFloat("refT", netF0);
		nbt.setInteger("fuel", netI1);
		nbt.setInteger("burn", netI2);
		return super.writeToNBT(nbt);
	}

	@Override
	public void initContainer(DataContainer cont) {
		TileContainer container = (TileContainer)cont;
		cont.refInts = new int[5];
		container.addItemSlot(new SlotItemHandler(inventory, 0, 26, 16));
		container.addItemSlot(new SlotItemHandler(inventory, 1, 26, 34));
		container.addPlayerInventory(8, 68);
	}

	@Override
	public boolean transferStack(ItemStack item, int s, TileContainer container) {
		if (s < container.invPlayerS) container.mergeItemStack(item, container.invPlayerS, container.invPlayerE, false);
		else container.mergeItemStack(item, 0, 2, false);
		return true;
	}

	@Override
	public int[] getSyncVariables() {
		return new int[]{netI0, netI1, netI2, Float.floatToIntBits(netF0), Float.floatToIntBits(netF1)};
	}

	@Override
	public void setSyncVariable(int i, int v) {
		switch(i) {
		case 0: netI0 = v; break;
		case 1: netI1 = v; break;
		case 2: netI2 = v; break;
		case 3: netF0 = Float.intBitsToFloat(v); break;
		case 4: netF1 = Float.intBitsToFloat(v); break;
		}
	}

}
