package cd4017be.automation.TileEntity;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;
import cd4017be.automation.gas.GasState;
import cd4017be.automation.shaft.GasContainer;
import cd4017be.automation.shaft.GasPhysics;
import cd4017be.automation.shaft.GasPhysics.IGasCon;
import cd4017be.automation.shaft.HeatReservoir;
import cd4017be.automation.shaft.IHeatReservoir;
import cd4017be.automation.shaft.IHeatReservoir.IHeatStorage;
import cd4017be.lib.templates.MutiblockTile;

public class GasVent extends MutiblockTile<GasContainer, GasPhysics> implements IGasCon, IHeatStorage, ITickable {

	public GasVent() {
		comp = new GasContainer(this, 5F);
	}

	@Override
	public boolean conGas(byte side) {
		return side == this.getOrientation();
	}

	@Override
	public IHeatReservoir getHeat(byte side) {
		return comp;
	}

	@Override
	public float getHeatRes(byte side) {
		return HeatReservoir.def_env;
	}

	@Override
	public void update() {
		if (worldObj.isRemote) return;
		super.update();
		GasState state = GasContainer.getEnvironmentGas(worldObj, pos, 100);
		if (state.exchange(comp.network.gas) == 0) comp.network.gas.exchange(state);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		comp.writeToNBT(nbt, "gas");
		return super.writeToNBT(nbt);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		comp = GasContainer.readFromNBT(this, nbt, "gas", 5F);
	}

}
