package cd4017be.automation.shaft;

import java.util.HashMap;

import cd4017be.lib.util.Obj2;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class HeatReservoir {
	
	public static interface IHeatComp {
		public HeatReservoir getHeat(byte side);
		public float getHeatCond(byte side);
	}
	
	/**
	 * [K] temperature
	 */
	public float T;

	/**
	 * @return [J/K] heat capacity
	 */
	public float getC() {
		return Float.POSITIVE_INFINITY;
	}
	
	/**
	 * perform heat exchange between this and another reservoir
	 * @param H the other heat reservoir
	 * @param R [K/J] the heat resistance of transfer
	 */
	public void exchangeHeat(HeatReservoir H, float R) {
		final float Ca = H.getC(), Cb = getC();
		final float Rmin = 1F / Ca + 1F / Cb;
		final float dQ = (H.T - T) / (R < Rmin ? Rmin : R);
		H.T -= dQ / Ca;
		T += dQ / Cb;
	}
	/*
	public static Obj2<HeatReservoir, Float> getHeat(World world, BlockPos pos, byte side) {
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof IHeatComp) {
			return new Obj2<HeatReservoir, Float>(((IHeatComp)te).getHeat(side), ((IHeatComp)te).getHeatCond(side));
		} else {
			float R = blocks.getOrDefault(world.getBlockState(pos).getBlock().getMaterial(), def_block);
			HeatReservoir H = new HeatReservoir();
			H.T = getEnvironmentTemp(world, pos);
			return new Obj2<HeatReservoir, Float>(H, R);
		}
	}
	*/
	
	public static float getEnvironmentTemp(World world, BlockPos pos) {
		if (world.provider.getDimensionName() == "Nether") return 380F; //107°C
		else if (world.provider.getDimensionName() == "The End") return 210F; //-43°C
		else return 270F + world.getBiomeGenForCoords(pos).getFloatTemperature(pos) * 25F; 
		//coldTaiga= -18.1°C; icePlains= -3.1°C; plains= 11.9°C; jungle= 25.4°C; savanna= 32.9°C; desert= 56.9°C
	}
	
	public static float def_block = 0.1F; //0.05W -> 0.1K
	public static HashMap<Material, Float> blocks = new HashMap<Material, Float>();
}
