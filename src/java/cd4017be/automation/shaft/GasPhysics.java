package cd4017be.automation.shaft;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import net.minecraft.tileentity.TileEntity;
import cd4017be.automation.gas.GasState;
import cd4017be.automation.shaft.IHeatReservoir.IHeatStorage;
import cd4017be.lib.templates.SharedNetwork;
import cd4017be.lib.util.Utils;

public class GasPhysics extends SharedNetwork<GasContainer, GasPhysics> {

	public final GasState gas;
	public float heatCond, refTemp;
	public HashMap<Long, HeatExchCon> heatExch = new HashMap<Long, HeatExchCon>();
	
	static class HeatExchCon {
		HeatExchCon(GasContainer pipe, IHeatStorage other, byte dir) {
			this.pipe = pipe;
			this.other = other;
			this.dir = dir;
		}
		final GasContainer pipe;
		final IHeatStorage other;
		final byte dir;
	}
	
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
		for (Iterator<Entry<Long, HeatExchCon>> it = heatExch.entrySet().iterator(); it.hasNext();) {
			Entry<Long, HeatExchCon> e = it.next();
			if (e.getValue().pipe.network == gp) {
				gp.heatExch.put(e.getKey(), e.getValue());
				it.remove();
			}
		}
		return gp;
	}

	@Override
	public void onMerged(GasPhysics network) {
		super.onMerged(network);
		gas.merge(network.gas);
		this.heatCond += network.heatCond;
		this.refTemp += network.refTemp;
		this.heatExch.putAll(network.heatExch);
	}

	@Override
	public void remove(GasContainer comp) {
		if (this.components.containsKey(comp.getUID())) gas.split(comp.V);
		this.heatCond -= comp.heatCond;
		this.refTemp -= comp.refTemp;
		for (int i = 0; i < 6; i+=2) this.heatExch.remove(SharedNetwork.SidedPosUID(comp.Uid, i));
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
				if (te instanceof IHeatStorage) {
					con |= 1 << i;
					if (i % 2 == 0 && !(te instanceof IGasStorage && ((IGasStorage)te).getGas().network == this)) {
						HeatExchCon entr = new HeatExchCon(pipe, (IHeatStorage)te, i);
						heatExch.put(SharedNetwork.SidedPosUID(pipe.Uid, i), entr);
					}
				}
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
		for (Iterator<HeatExchCon> it = heatExch.values().iterator(); it.hasNext();) {
			HeatExchCon hc = it.next();
			if (((TileEntity)hc.other).isInvalid() || (hc.other instanceof GasContainer && ((GasContainer)hc.other).network == this)) { it.remove(); continue; }
			IHeatReservoir hr = hc.other.getHeat((byte)(hc.dir | 1));
			R = hc.other.getHeatRes((byte)(hc.dir | 1)) + ((IHeatStorage)hc.pipe.tile).getHeatRes((byte)(hc.dir));
			HeatReservoir.exchangeHeat(hc.pipe, hr, R);
		}
	}
	
}
