package cd4017be.automation.shaft;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import cd4017be.automation.Objects;
import cd4017be.automation.gas.GasState;
import cd4017be.lib.ModTileEntity;
import cd4017be.lib.templates.MultiblockComp;

public class GasContainer extends  MultiblockComp<GasContainer, GasPhysics> implements IHeatReservoir {

	public float V;
	public float heatCond, refTemp;

	public GasContainer(ModTileEntity tile, float V) {
		super(tile);
		this.V = V;
	}

	public void setUID(long uid) {
		super.setUID(uid);
		if (network == null) new GasPhysics(this, getEnvironmentGas(((TileEntity)tile).getWorld(), ((TileEntity)tile).getPos(), V));
	}

	public static GasContainer readFromNBT(ModTileEntity tile, NBTTagCompound nbt, String k, float V) {
		GasContainer pipe = new GasContainer(tile, V);
		new GasPhysics(pipe, new GasState(nbt, k, V));
		return pipe;
	}

	public void writeToNBT(NBTTagCompound nbt, String k) {
		if (network != null) network.gas.copy(V).write(nbt, k);
	}

	public void remove() {
		if (network != null) network.remove(this);
	}

	public static final float DefaultPressure = 101300F;

	public static GasState getEnvironmentGas(World world, BlockPos pos, float V) {
		float T = HeatReservoir.getEnvironmentTemp(world, pos);
		return new GasState(T, DefaultPressure * V / T, V);
	}

	@Override
	public float T() {
		return network.gas.T;
	}

	@Override
	public float C() {
		return network.gas.nR;
	}

	@Override
	public void addHeat(float dQ) {
		network.gas.T += dQ / network.gas.nR;
	}

	@Override
	public Capability<GasContainer> getCap() {
		return Objects.GAS_CAP;
	}

}
