package cd4017be.automation.TileEntity;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import cd4017be.automation.gas.GasState;
import cd4017be.automation.shaft.GasContainer;
import cd4017be.automation.shaft.GasPhysics.IGasStorage;
import cd4017be.automation.shaft.HeatReservoir;
import cd4017be.automation.shaft.IHeatReservoir;
import cd4017be.lib.ModTileEntity;
import cd4017be.lib.templates.SharedNetwork;

public class GasVent extends ModTileEntity implements IGasStorage, ITickable {

	public GasContainer gas;
	
	public GasVent() {
		gas = new GasContainer(this, 5F);
	}

	@Override
	public boolean conGas(byte side) {
		return side == this.getOrientation();
	}

	@Override
	public IHeatReservoir getHeat(byte side) {
		return gas;
	}

	@Override
	public float getHeatRes(byte side) {
		return HeatReservoir.def_env;
	}

	@Override
	public GasContainer getGas() {
		return gas;
	}

	@Override
	public void update() {
		if (worldObj.isRemote) return;
		if (gas.updateCon) gas.network.updateLink(gas);
		gas.network.updateTick(gas);
		GasState state = GasContainer.getEnvironmentGas(worldObj, pos, 100);
		if (state.exchange(gas.network.gas) == 0) gas.network.gas.exchange(state);
	}
	
	@Override
	public void onNeighborTileChange(BlockPos pos) {
		gas.updateCon = true;
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		gas.writeToNBT(nbt, "gas");
        return super.writeToNBT(nbt);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) 
	{
		super.readFromNBT(nbt);
		gas = GasContainer.readFromNBT(this, nbt, "gas", 5F);
	}
	
	@Override
	public void validate() {
		super.validate();
		if (!worldObj.isRemote) gas.initUID(SharedNetwork.ExtPosUID(pos, 0));
	}

	@Override
	public void invalidate() {
		super.invalidate();
		gas.remove();
	}

	@Override
	public void onChunkUnload() {
		super.onChunkUnload();
		gas.remove();
	}
}
