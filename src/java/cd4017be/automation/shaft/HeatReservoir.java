package cd4017be.automation.shaft;

import java.util.HashMap;
import java.util.Map.Entry;

import cd4017be.automation.Objects;
import cd4017be.lib.ModTileEntity;
import cd4017be.lib.util.Utils;
import net.minecraft.block.material.Material;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class HeatReservoir implements IHeatReservoir {
	
	public HeatReservoir(float C) {
		this.C = C;
	}
	
	/**	[J/K] heat capacity */
	public final float C;
	/** [K] temperature */
	public float T;
	
	public float envCond, envTemp;
	public byte con;
	public boolean check = true;
	private IHeatStorage[] ref = new IHeatStorage[3];
	
	@Override
	public float T() {return T;}

	@Override
	public float C() {return C;}

	@Override
	public void addHeat(float dQ) {T += dQ / C;}
	
	public void update(ModTileEntity tile) {
		TileEntity te;
		if (check) {
			for (byte i = 0; i < 6; i++) {
				te = Utils.getTileOnSide(tile, i);
				if (te != null && te instanceof IHeatStorage) {
					if ((i & 1) == 0) ref[i >> 1] = (IHeatStorage)te;
					con &= ~(1 << i);
				} else {
					if ((i & 1) == 0) ref[i >> 1] = null;
					con |= 1 << i;
				}
			}
			envTemp = getEnvironmentTemp(tile.getWorld(), tile.getPos());
			envCond = getEnvHeatCond((IHeatStorage)tile, tile.getWorld(), tile.getPos(), con);
			check = false;
		}
		if (envCond > C) T = envTemp;
		else T += (envTemp - T) * envCond / C;
		for (int i = 0; i < 3; i++) {
			if (ref[i] == null) continue;
			if (((TileEntity)ref[i]).isInvalid()) {ref[i] = null; check = true; continue;}
			byte s = (byte)(i * 2 + 1);
			IHeatReservoir H = ref[i].getHeat(s);
			float R = ((IHeatStorage)tile).getHeatRes((byte)(i * 2)) + ref[i].getHeatRes(s);
			final float Rmin = 1F / C + 1F / H.C();
			final float dQ = (T - H.T()) / (R < Rmin ? Rmin : R);
			H.addHeat(dQ);
			T -= dQ / C;
		}
	}
	
	public void save(NBTTagCompound nbt, String k) {
		nbt.setFloat(k + "T", T);
	}
	
	public void load(NBTTagCompound nbt, String k) {
		T = nbt.getFloat(k + "T");
		check = true;
	}
	
	public static float exchangeHeat(IHeatReservoir Ha, IHeatReservoir Hb, float R) {
		final float Rmin = 1F / Ha.C() + 1F / Hb.C();
		final float dQ = (Ha.T() - Hb.T()) / (R < Rmin ? Rmin : R);
		Ha.addHeat(-dQ);
		Hb.addHeat(dQ);
		return dQ;
	}
	
	public static float exchangeHeat(IHeatReservoir H, float T, float R) {
		final float Rmin = 1F / H.C();
		final float dQ = (H.T() - T) / (R < Rmin ? Rmin : R);
		H.addHeat(dQ);
		return dQ;
	}
	
	public static float getEnvHeatCond(IHeatStorage tile, World world, BlockPos pos, byte sides) {
		float cond = 0;
		for (byte i = 0; i < 6; i++) 
			if ((sides >> i & 1) != 0) {
				Material m = world.getBlockState(pos.offset(EnumFacing.VALUES[i])).getBlock().getMaterial();
				Float f = blocks_env.get(m);
				cond += 1F / (tile.getHeatRes(i) + (f == null ? def_block + def_env : f.floatValue()));
			}
		return cond;
	}
	
	public static float getEnvironmentTemp(World world, BlockPos pos) {
		if (world.provider.getDimensionName() == "Nether") return 380F; //107°C
		else if (world.provider.getDimensionName() == "The End") return 250F; //-23°C
		else return 270F + world.getBiomeGenForCoords(pos).getFloatTemperature(pos) * 25F; 
		//coldTaiga= -18.1°C; icePlains= -3.1°C; plains= 11.9°C; jungle= 25.4°C; savanna= 32.9°C; desert= 56.9°C
	}
	
	/** Default Heat resistance for unlisted materials */
	public static float def_block;
	/** Default offset for environment heat resistance */
	public static float def_env;
	/** Default internal Heat resistance of machines for connected/disconnected sides */
	public static float def_con, def_discon;
	/** Heat resistance of block materials used as cover (no cover = air) */
	public static final HashMap<Material, Float> blocks = new HashMap<Material, Float>();
	/** Heat resistance of environment block materials */
	public static final HashMap<Material, Float> blocks_env = new HashMap<Material, Float>();
	static {
		def_block = 2.5F;
		def_env = 0.8F;
		blocks.put(Material.iron, 0.01F);
		blocks.put(Material.glass, 1F);
		blocks.put(Material.rock, 1.2F);
		blocks.put(Material.clay, 1F);
		blocks.put(Material.ground, 1.5F);
		blocks.put(Material.grass, 2F);
		blocks.put(Material.sand, 4F);
		blocks.put(Material.wood, 5F);
		blocks.put(Material.packedIce, 0.4F);
		blocks.put(Material.ice, 0.5F);
		blocks.put(Material.craftedSnow, 12F);
		blocks.put(Material.air, 25F);
		blocks.put(Material.cloth, 100F);
		blocks.put(Objects.M_thermIns, 1000F);
		for (Entry<Material, Float> e : blocks.entrySet()) blocks_env.put(e.getKey(), e.getValue() + def_env);
		blocks_env.put(Material.air, 10F);
		blocks_env.put(Material.snow, 15F);
		blocks_env.put(Material.lava, 2.0F);
		blocks_env.put(Material.water, 1.2F);
		def_con = 0.004F;//heat conductivity of 1m³ copper
		def_discon = blocks.get(Objects.M_thermIns);
	}
}
