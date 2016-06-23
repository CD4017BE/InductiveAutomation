package cd4017be.automation.shaft;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import cd4017be.automation.gas.GasState;
import cd4017be.lib.ModTileEntity;
import cd4017be.lib.templates.IComponent;
import cd4017be.lib.templates.SharedNetwork;
import cd4017be.lib.util.Obj2;

public class GasContainer implements IComponent<GasContainer, GasPhysics>, IHeatReservoir {
	
	public GasPhysics network;
	public final ModTileEntity tile;
	protected long Uid;
	public byte con = 0x3f;
	public float V;
	public float heatCond, refTemp;
	public boolean updateCon;
	
	public GasContainer(ModTileEntity tile, float V) {
		this.tile = tile;
		this.Uid = 0;
		this.V = V;
	}
	
	public void initUID(long Uid) {
		if (this.Uid != 0) return;
		this.Uid = Uid;
		if (network == null) new GasPhysics(this, getEnvironmentGas(tile.getWorld(), tile.getPos(), V));
		else {
			network.components.remove(0);
			network.components.put(Uid, this);
		}
		updateCon = true;
	}

	@Override
	public void setNetwork(GasPhysics network) {
		this.network = network;
	}

	@Override
	public GasPhysics getNetwork() {
		return network;
	}

	@Override
	public long getUID() {
		return Uid;
	}

	@Override
	public boolean canConnect(byte side) {
		return (con >> side & 1) != 0;
	}

	@Override
	public List<Obj2<Long, Byte>> getConnections() {
		ArrayList<Obj2<Long, Byte>> list = new ArrayList<Obj2<Long, Byte>>(Integer.bitCount(con));
		for (byte i = 0; i < 6; i++) 
			if ((con >> i & 1) != 0) 
				list.add(new Obj2<Long, Byte>(SharedNetwork.ExtPosUID(tile.getPos().offset(EnumFacing.VALUES[i]), 0), i));
		return list;
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

}
