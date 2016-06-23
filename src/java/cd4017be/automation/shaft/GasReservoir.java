package cd4017be.automation.shaft;

import java.util.HashMap;
import java.util.HashSet;

import cd4017be.lib.templates.IComponent;
import cd4017be.lib.util.Obj2;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

/**
 * 
 * @author CD4017BE
 *
 */
public class GasReservoir extends HeatReservoir {

	public static interface IGasComp extends IComponent {
		public boolean canConnect(IGasComp comp, EnumFacing side);
		public GasReservoir getGas();
		public void setGas(GasReservoir G);
	}
	
	public HashMap<Integer, Obj2<IGasComp, Float>> comps = new HashMap<Integer, Obj2<IGasComp, Float>>();
	
	/**
	 * @field T [K] the temperature of the gas.
	 * @field N [J/K] the amount of gas molecules multiplied with the Bolzman-constant kB.
	 * @field V [m³] the volume of this container.
	 */
	public float T, N, V;
	
	/**
	 * list of contained substances mapped with their relative concentration
	 */
	public HashMap<Gas, Float> content = new HashMap<Gas, Float>();
	
	/**
	 * @return [Pa] the gas pressure
	 */
	public float P() {
		return N * T / V;
	}
	
	/**
	 * @return [J] the heat stored in the gas
	 */
	public float U() {
		return N * T;
	}
	
	/**
	 * add gas to this container
	 * @param gas list of contained substances mapped with their relative concentration
	 * @param N [J/K] gas amount
	 * @param T [K] gas temperature
	 */
	public void addGas(HashMap<Gas, Float> gas, float N, float T) {
		final float x0 = this.N / (this.N + N);
		float x1 = 1F - x0;
		this.T *= x0;
		this.T += T * x1;
		this.N += N;
		/*if (!content.equals(gas)) {
			content.replaceAll((g, c) -> c * x0);
			gas.forEach((g, c) -> content.merge(g, c * x1, (c0, c1) -> c0 + c1));
		}*/
	}
	
	public void register(IGasComp comp) {
		GasReservoir G = comp.getGas();
		if (G == this) return;
		this.V += G.V;
		this.addGas(G.content, G.N, G.T);
		//G.comps.forEach((i, obj) -> obj.objA.setGas(this));
		comps.putAll(G.comps);
	}
	
	public void remove(IGasComp comp) {
		//Obj2<IGasComp, Float> obj = comps.remove(comp.posHash());
		//if (obj != null) this.onDisconnect();
	}
	
	@SuppressWarnings("unchecked")
	public void onDisconnect() {
		for (Obj2<IGasComp, Float> obj : comps.values()) {
			if (obj.objA.getGas() != this) continue;
			GasReservoir G = new GasReservoir();
			G.T = T;
			HashSet<Obj2<IGasComp, Float>> set = new HashSet<Obj2<IGasComp, Float>>();
			HashSet<Obj2<IGasComp, Float>> set1;
			set.add(obj);
			while (!set.isEmpty()) {
				set1 = new HashSet<Obj2<IGasComp, Float>>();
				for (Obj2<IGasComp, Float> obj1 : set) {
					G.V += obj1.objB;
					//G.comps.put(obj1.objA.posHash(), obj1);
					obj1.objA.setGas(G);
					for (EnumFacing side : EnumFacing.VALUES) {
						//Obj2<IGasComp, Float> obj2 = comps.get(obj1.objA.getPos().offset(side).hashCode());
						//if (obj2 != null && obj2.objA.getGas() != G && obj1.objA.canConnect(obj2.objA, side) && obj2.objA.canConnect(obj1.objA, side.getOpposite()))
						//	set1.add(obj2);
					}
				}
				set = set1;
			}
			G.N = N * G.V / V;
			if (G.comps.size() >= comps.size()) {
				G.content = content;
				return;
			}
			G.content = (HashMap<Gas, Float>)content.clone();
		}
	}

	@Override
	public float getC() {
		return N;
	}
	
	public static GasReservoir getEnvironmentGas(World world, BlockPos pos, float V)
	{
		GasReservoir gas = new GasReservoir();
		gas.content.put(Gas.nitrogen, 0.78F);
		gas.content.put(Gas.oxygen, 0.22F);
		gas.V = V;
		gas.T = getEnvironmentTemp(world, pos);
		gas.N = getEnvironmentPressure(world) * gas.V / gas.T;
		return gas;
	}
	
	public static float getEnvironmentPressure(World world) {
		if (world.provider.getDimensionName() == "Nether") return 150000F; //+0.5bar
		else if (world.provider.getDimensionName() == "The End") return 80000F; //-0.2bar
		else return 100000F; 
	}
	
}
