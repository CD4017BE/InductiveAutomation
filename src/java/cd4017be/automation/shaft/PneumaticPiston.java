package cd4017be.api.automation.therm;

import cd4017be.api.automation.therm.Shaft.IKineticComp;

public class PneumaticPiston implements IKineticComp {

	private GasReservoir input, output;
	public float Vin, Vout, Vmax;
	
	@Override
	public boolean valid() {
		return true;
	}

	@Override
	public float estimatedForce(float s, float ds) {
		if (input == null || output == null) return 0;
		float x0;
		if (ds <= 0) {
			x0 = Vin / input.V;
			x0 *= 2F - Vin / Vout;
			return input.U() * x0 - output.U() * Vout / output.V;
		}
		float x1, x2, dV = ds * Vin;
		x0 = Vin / (input.V + dV);
		x1 = output.N / (output.N + input.N * x0 * ds);
		x2 = 1F - Vin / Vout + x1 * dV / output.V;
		x2 *= input.V / (input.V + dV);
		x1 *= Vout / output.V;
		x0 *= 1F + x2;
		return input.U() * x0 - output.U() * x1;
	}

	@Override
	public float work(float s, float ds) {
		if (input == null || output == null) return 0;
		float x, n, T, W;
		//adiabatic expansion of input reservoir
		x = input.V / (input.V + ds * Vin);
		n = input.N * (1F - x);
		W = input.T * n;
		T = input.T *= x;
		input.N *= x; 
		//adjust target volume
		x = n * T * Vin / (output.P() * ds);
		if (x > Vmax * Vmax) Vout = Vmax;
		else Vout = (float)Math.sqrt(x);
		//adiabatic expansion/compression towards target volume
		x = Vin / Vout;
		W += T * n * (1F - x);
		T *= x;
		//adiabatic compression of output reservoir
		x = Vout * ds / output.V;
		output.addGas(input.content, n, T);
		W -= output.T * output.N * x;
		output.T *= 1F + x;
		return W;
	}

}
