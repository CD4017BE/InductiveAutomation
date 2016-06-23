package cd4017be.automation.gas;

public enum Gas {
	O2(3.85E-3F), N2(3.37E-3F), He(4.81E-4F), H2(2.41E-4F), H2O(2.16E-3F), CO2(5.29E-3F);
	float specMass;
	private Gas(float specMass) {
		this.specMass = specMass;
	}
}
// m = x / 371Pa/K * 1kg/m³ * nR