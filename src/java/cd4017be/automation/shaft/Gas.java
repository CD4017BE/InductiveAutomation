package cd4017be.automation.shaft;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public class Gas {
	public static Gas oxygen;
	public static Gas nitrogen;
	public static Gas steam;
	
	public static Gas[] gases = new Gas[16];
	
	public final int id;
	public final float Ng, Nl, Ns;
	public final FluidStack gas, liquid;
	public final ItemStack solid;
	
	public Gas(int id, FluidStack gas, float Ng, FluidStack liquid, float Nl, ItemStack solid, float Ns) {
		this.id = id;
		gases[id] = this;
		this.Ng = Ng / gas.amount;
		this.Nl = Nl / liquid.amount;
		this.Ns = Ns / solid.stackSize;
		this.gas = new FluidStack(gas, 1);
		this.liquid = new FluidStack(liquid, 1);
		this.solid = solid.copy();
		this.solid.stackSize = 1;
	}
}
