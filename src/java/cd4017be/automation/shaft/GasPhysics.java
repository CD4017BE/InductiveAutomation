package cd4017be.automation.shaft;

import java.util.HashMap;

import net.minecraft.tileentity.TileEntity;
import cd4017be.automation.gas.GasState;
import cd4017be.automation.shaft.IHeatReservoir.IHeatStorage;
import cd4017be.lib.templates.SharedNetwork;
import cd4017be.lib.util.Utils;

public class GasPhysics extends SharedNetwork<GasContainer, GasPhysics> {

	public final GasState gas;
	public float heatCond, refTemp;
	
	public GasPhysics(GasContainer core, GasState gas) {
		super(core);
		this.gas = gas;
		this.heatCond = core.heatCond;
		this.refTemp = core.refTemp;
	}
	
	public GasPhysics(HashMap<Long, GasContainer> comps, GasState gas) {
		super(comps);
		this.gas = gas;
	}

	@Override
	public GasPhysics onSplit(HashMap<Long, GasContainer> comps) {
		float V = 0, C = 0, T = 0;
		for (GasContainer c : comps.values()) {
			V += c.V; C += c.heatCond; T += c.refTemp;
		}
		this.heatCond -= C;
		this.refTemp -= T;
		GasPhysics gp = new GasPhysics(comps, gas.split(V));
		gp.heatCond = C;
		gp.refTemp = T;
		return gp;
	}

	@Override
	public void onMerged(GasPhysics network) {
		super.onMerged(network);
		gas.merge(network.gas);
		this.heatCond += network.heatCond;
		this.refTemp += network.refTemp;
	}

	@Override
	public void remove(GasContainer comp) {
		if (this.components.containsKey(comp.getUID())) gas.split(comp.V);
		this.heatCond -= comp.heatCond;
		this.refTemp -= comp.refTemp;
		super.remove(comp);
	}

	public static interface IGasStorage extends IGasCon, IHeatStorage {
		public GasContainer getGas();
	}
	
	public static interface IGasCon {
		public boolean conGas(byte side);
	}

	public void updateLink(GasContainer pipe) {
		TileEntity te;
		byte con = 0;
		for (byte i = 0; i < 6; i++) 
			if (pipe.canConnect(i) && (te = Utils.getTileOnSide(pipe.tile, i)) != null) {
				if (te instanceof IGasStorage) {
					GasContainer p = ((IGasStorage)te).getGas();
					if (p.canConnect((byte)(i^1))) add(p);
				}
				if (te instanceof IHeatStorage) con |= 1 << i;
			}
		con ^= 0x3f;
		float dC = HeatReservoir.getEnvHeatCond((IHeatStorage)pipe.tile, pipe.tile.getWorld(), pipe.tile.getPos(), con) - pipe.heatCond;
		pipe.heatCond += dC;
		this.heatCond += dC;
		float dT = HeatReservoir.getEnvironmentTemp(pipe.tile.getWorld(), pipe.tile.getPos()) * pipe.heatCond - pipe.refTemp;
		pipe.refTemp += dT;
		this.refTemp += dT;
	}

	@Override
	protected void updatePhysics() {
		float R = 1F / this.heatCond;
		float T = R * this.refTemp;
		if (R < 1F / gas.nR) gas.T = T;
		else gas.T -= (gas.T - T) / R / gas.nR;
	}
	
}
