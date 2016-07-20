package cd4017be.automation.gas;

import net.minecraft.nbt.NBTTagCompound;

/**
 * Stores thermodynamic data about a gas volume
 * @author CD4017BE
 *
 */
public class GasState {
	public GasState(NBTTagCompound nbt, String k, float V) {this.nR = nbt.getFloat(k + "nR"); this.T = nbt.getFloat(k + "T"); this.V = V;}
	public GasState(float T, float nR, float V) {this.T = T; this.nR = nR; this.V = V;}
	/**[K] Temperature */
	public float T;
	/**[J/K] Gas amount multiplied by universal gas constant */
	public float nR;
	/**[m³] Volume */
	public float V;
	/**@return [Pa] Pressure */
	public float P() {return nR * T / V;}
	/**@return [J] Stored heat energy */
	public float E() {return nR * T;};
	/**
	 * Performs an adiabatic expansion/compression on this gas volume towards the given volume.
	 * @param n_V new volume
	 */
	public void adiabat(float n_V) {
		T *= V / n_V;
		V = n_V;
	}
	/**
	 * Splits the given volume off from this gas volume and returns it as new GasState.
	 * @param n_V split volume
	 * @return new GasState
	 */
	public GasState split(float n_V) {
		nR /= V;
		GasState s = new GasState(T, nR * n_V, n_V);
		V -= n_V;
		nR *= V;
		return s;
	}
	/**
	 * Extracts the given volume out of this volume by adiabatically expanding the gas.
	 * @param n_V extract volume
	 * @return new GasState
	 */
	public GasState extract(float n_V) {
		float x = V / (V + n_V);
		T *= x;
		nR *= x;
		return new GasState(T, nR * n_V / V, n_V);
	}
	/**
	 * Combines this gas volume with the given gas volume.
	 * @param s other gas
	 */
	public void merge(GasState s) {
		V += s.V;
		T = nR * T + s.nR * s.T;
		nR += s.nR;
		T /= nR;
	}
	/**
	 * Injects the given gas volume into this gas volume by adiabatically compressing the gas.
	 * @param s other gas
	 */
	public void inject(GasState s) {
		T = nR * T + s.nR * s.T;
		nR += s.nR;
		T *= (V + s.V) / V / nR;
	}
	/**
	 * Moves gas from this volume into the given volume to achieve equal pressure.
	 * @param s other gas
	 * @return amount of gas moved (nR)
	 */
	public float exchange(GasState s) {
		if (P() <= s.P()) return 0F;
		float x = (float)Math.sqrt(s.V * s.nR * s.T / V / nR / T);
		float dV = (s.V - V * x) / (1 + x);
		T *= V / (dV + V);
		s.T *= s.V / (s.V - dV);
		float dnR = nR * dV / V;
		nR -= dnR; s.T = (s.T * s.nR + dnR * T) / (s.nR += dnR);
		return dnR;
	}
	public GasState copy(float n_V) {
		return new GasState(T, nR * n_V / V, n_V);
	}
	public void write(NBTTagCompound nbt, String k) {
		nbt.setFloat(k + "nR", nR);
		nbt.setFloat(k + "T", T);
	}
}
