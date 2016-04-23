package cd4017be.api.automation.therm;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

/**
 * 
 * @author CD4017BE
 *
 */
public class Shaft {
	
	public interface IShaftComp {
		public Shaft getShaft();
		public void setShaft(Shaft network);
	}
	public interface IKineticComp {
		public boolean valid();
		/**
		 * calculate the estimated force of this part on the shaft. 
		 * This value doesn't need to be exact, but it shouldn't be greater than the actual force added during work().
		 * Otherwise the shaft could probably get negative kinetic energy.
		 * @param s [m] the current shaft position
		 * @param ds [m] the distance the shaft will move during this calculation tick
		 * @return [kg*m/s²] the force on the shaft (positive values accelerate, negative slow down)
		 */
		public float estimatedForce(float s, float ds);
		/**
		 * update the mechanical physics of this connected part
		 * @param s [m] the current shaft position
		 * @param ds [m] the distance the shaft will move during this calculation tick
		 * @return [J] the amount of work added to the shaft
		 */
		public float work(float s, float ds);
	}
	/**
	 * set of contained shaft parts mapped with their individual effective rotation mass.
	 */
	public final HashMap<IShaftComp, Float> parts = new HashMap<IShaftComp, Float>();
	/**
	 * set of connected mechanical components
	 */
	public final HashSet<IKineticComp> comps = new HashSet<IKineticComp>();
	/**
	 * the "handle" radius at which components interact. 
	 * Used for calculating the effective rotation mass of shaft parts.
	 */
	public static final float R = (float)(1D / Math.PI);
	/**
	 * @field m [kg] the total effective rotation mass of the whole shaft structure
	 * @field v [m/s] the speed of the shaft
	 * @field s [m] the shaft's current position (1m = one full rotation)
	 */
	public float m, v, s;
	
	/**
	 * add a shaft part to the system or change its properties
	 * @param part
	 * @param m [kg] the effective rotation mass of the part
	 * @param v [m/s] the saved speed of the part
	 */
	public void register(IShaftComp part, float m, float v) {
		Float oldm = parts.get(part);
		if (oldm != null) this.m -= oldm;
		parts.put(part, m);
		if (part.getShaft() != this) {
			if (part.getShaft() != null) part.getShaft().unload(part);
			part.setShaft(this);
		}
		this.v = this.v * this.m + v * m;
		this.m += m;
		this.v /= this.m;
	}
	
	/**
	 * notify the structure that a shaft part was removed
	 * @param shaft
	 */
	public void unload(IShaftComp shaft) {
		Float oldm = parts.remove(shaft);
		shaft.setShaft(null);
		if (oldm != null) this.m -= oldm;
	}
	
	/**
	 * add a component to the shafts mechanical structure
	 * @param comp
	 */
	public void register(IKineticComp comp) {
		comps.add(comp);
	}
	
	/**
	 * update the physics for the shaft and the components attached to it.
	 * @param dt [s] time span to calculate over (usually 50ms)
	 */
	public void update(float dt) {
		float ds = v * dt, F = 0, a, E;
		Iterator<IKineticComp> it = comps.iterator();
		while (it.hasNext()) {//get the estimated total force of all components on the shaft
			IKineticComp comp = it.next();
			if (!comp.valid()) it.remove();
			else F += comp.estimatedForce(s, ds);
		}
		a = F / m; //convert to acceleration
		if (v == 0 && a < 0) return;
		if (-a * dt > v) dt = -v / a; //if rotation would stop, only calculate till that point
		ds = 0.5F * a * dt * dt + v * dt; //now use the real distance moved
		E = 0.5F * m * v * v; //get the kinetic Energy of the shaft and add the work of all components to it
		for (IKineticComp comp : comps) {
			E += comp.work(s, ds);
		}
		v = (float)Math.sqrt(2F * E / m); //convert back to speed
		if (Float.isNaN(v)) v = 0;
		s += ds; //move the shaft forward
		if (s > 1F) s -= Math.floor(s);
	}
	
}
