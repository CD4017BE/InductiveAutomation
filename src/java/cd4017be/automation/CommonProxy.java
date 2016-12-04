package cd4017be.automation;

import net.minecraftforge.fml.common.FMLLog;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.Level;

import cd4017be.automation.TileEntity.*;
import cd4017be.lib.TileBlockRegistry;
import cd4017be.lib.Gui.TileContainer;
import cd4017be.lib.util.OreDictStack;
import net.minecraft.block.BlockMushroom;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemSeeds;
import net.minecraft.item.ItemStack;
import static cd4017be.automation.Objects.*;

/**
 *
 * @author CD4017BE
 */
public class CommonProxy {

	private List<BioEntry> bioList = new ArrayList<BioEntry>();
	public static final String[] dyes = {"dyeBlack", "dyeRed", "dyeGreen", "dyeBrown", "dyeBlue", "dyePurple", "dyeCyan", "dyeLightGray", "dyeGray", "dyePink", "dyeLime", "dyeYellow", "dyeLightBlue", "dyeMagenta", "dyeOrange", "dyeWhite"};

	public void registerBlocks() {
		TileBlockRegistry.register(lightShaft, LightShaft.class, null);
		TileBlockRegistry.register(wireC, Wire.class, null);
		TileBlockRegistry.register(wireA, Wire.class, null);
		TileBlockRegistry.register(wireH, Wire.class, null);
		TileBlockRegistry.register(liquidPipe, LiquidPipe.class, null);
		TileBlockRegistry.register(itemPipe, ItemPipe.class, null);
		TileBlockRegistry.register(warpPipe, WarpPipe.class, null);
		TileBlockRegistry.register(SCSU, ESU.class, TileContainer.class);
		TileBlockRegistry.register(OCSU, ESU.class, TileContainer.class);
		TileBlockRegistry.register(CCSU, ESU.class, TileContainer.class);
		TileBlockRegistry.register(CEU, CEU.class, TileContainer.class);
		TileBlockRegistry.register(steamEngine, SteamEngine.class, TileContainer.class);
		TileBlockRegistry.register(steamTurbine, SteamTurbine.class, TileContainer.class);
		TileBlockRegistry.register(steamGenerator, SteamGenerator.class, TileContainer.class);
		TileBlockRegistry.register(steamBoiler, SteamBoiler.class, TileContainer.class);
		TileBlockRegistry.register(lavaCooler, LavaCooler.class, TileContainer.class);
		TileBlockRegistry.register(energyFurnace, EnergyFurnace.class, TileContainer.class);
		TileBlockRegistry.register(farm, Farm.class, TileContainer.class);
		TileBlockRegistry.register(miner, Miner.class, TileContainer.class);
		TileBlockRegistry.register(magnet, Magnet.class, null);
		TileBlockRegistry.register(link, ELink.class, TileContainer.class);
		TileBlockRegistry.register(linkHV, ELink.class, TileContainer.class);
		TileBlockRegistry.register(texMaker, TextureMaker.class, TileContainer.class);
		TileBlockRegistry.register(builder, Builder.class, TileContainer.class);
		TileBlockRegistry.register(algaePool, AlgaePool.class, TileContainer.class);
		TileBlockRegistry.register(teleporter, Teleporter.class, TileContainer.class);
		TileBlockRegistry.register(advancedFurnace, AdvancedFurnace.class, TileContainer.class);
		TileBlockRegistry.register(pump, Pump.class, TileContainer.class);
		TileBlockRegistry.register(massstorageChest, MassstorageChest.class, TileContainer.class);
		TileBlockRegistry.register(matterOrb, MatterOrb.class, null);
		TileBlockRegistry.register(antimatterBombE, MatterOrb.class, null);
		TileBlockRegistry.register(antimatterBombF, AntimatterBomb.class, null);
		TileBlockRegistry.register(antimatterTank, AntimatterTank.class, TileContainer.class);
		TileBlockRegistry.register(antimatterFabricator, AntimatterFabricator.class, TileContainer.class);
		TileBlockRegistry.register(antimatterAnihilator, AntimatterAnihilator.class, TileContainer.class);
		TileBlockRegistry.register(hpSolarpanel, HPSolarpanel.class, null);
		TileBlockRegistry.register(autoCrafting, AutoCrafting.class, TileContainer.class);
		TileBlockRegistry.register(geothermalFurnace, GeothermalFurnace.class, TileContainer.class);
		TileBlockRegistry.register(steamCompressor, SteamCompressor.class, TileContainer.class);
		TileBlockRegistry.register(electricCompressor, ElectricCompressor.class, TileContainer.class);
		TileBlockRegistry.register(tank, Tank.class, TileContainer.class);
		TileBlockRegistry.register(security, SecuritySys.class, TileContainer.class);
		TileBlockRegistry.register(decompCooler, DecompCooler.class, TileContainer.class);
		TileBlockRegistry.register(collector, Collector.class, TileContainer.class);
		TileBlockRegistry.register(trash, Trash.class, TileContainer.class);
		TileBlockRegistry.register(electrolyser, Electrolyser.class, TileContainer.class);
		TileBlockRegistry.register(fuelCell, FuelCell.class, TileContainer.class);
		TileBlockRegistry.register(itemSorter, ItemSorter.class, TileContainer.class);
		TileBlockRegistry.register(matterInterfaceB, MatterInterface.class, TileContainer.class);
		TileBlockRegistry.register(fluidPacker, FluidPacker.class, TileContainer.class);
		TileBlockRegistry.register(hugeTank, HugeTank.class, TileContainer.class);
		TileBlockRegistry.register(fluidVent, FluidVent.class, TileContainer.class);
		TileBlockRegistry.register(gravCond, GraviCond.class, TileContainer.class);
		TileBlockRegistry.register(itemBuffer, ItemBuffer.class, TileContainer.class);
		TileBlockRegistry.register(quantumTank, QuantumTank.class, TileContainer.class);
		TileBlockRegistry.register(vertShemGen, VertexShematicGen.class, TileContainer.class);
		TileBlockRegistry.register(solarpanel, Solarpanel.class, null);
		TileBlockRegistry.register(teslaTransmitterLV, TeslaTransmitterLV.class, TileContainer.class);
		TileBlockRegistry.register(teslaTransmitter, TeslaTransmitter.class, TileContainer.class);
		TileBlockRegistry.register(wormhole, InterdimHole.class, null);
		TileBlockRegistry.register(heatRadiator, HeatRadiator.class, TileContainer.class);
		TileBlockRegistry.register(shaft, Shaft.class, null);
		TileBlockRegistry.register(electricCoilC, ElectricCoil.class, TileContainer.class);
		TileBlockRegistry.register(electricCoilA, ElectricCoil.class, TileContainer.class);
		TileBlockRegistry.register(electricCoilH, ElectricCoil.class, TileContainer.class);
		TileBlockRegistry.register(electricHeater, HeatingCoil.class, TileContainer.class);
		TileBlockRegistry.register(pneumaticPiston, PneumaticPiston.class, TileContainer.class);
		TileBlockRegistry.register(gasPipe, GasPipe.class, TileContainer.class);
		TileBlockRegistry.register(solidFuelHeater, SolidFuelHeater.class, TileContainer.class);
		TileBlockRegistry.register(gasVent, GasVent.class, null);
		TileBlockRegistry.register(heatedFurnace, HeatedFurnace.class, TileContainer.class);
	}

	public void registerRenderers() {}

	//TODO move bioreactor recipe stuff to recipe API
	public void registerBioFuel(Object item, int n) {
		if (n > 0) bioList.add(new BioEntry(item, n));
	}

	public void registerBioFuel(Object item, int n, int a) {
		if (n > 0 || a > 0) bioList.add(new BioEntry(item, n, a));
	}

	public List<BioEntry> getBioFuels() {
		return bioList;
	}

	public void registerBioFuels() {
		String[] ids = Config.data.getStringArray("rcp.bioReact.id");
		int[] nutr = Config.data.getIntArray("rcp.bioReact.nutr");
		int[] alg = Config.data.getIntArray("rcp.bioReact.alg");
		if (ids.length != nutr.length || nutr.length != alg.length) {
			FMLLog.log("Automation", Level.WARN, "Can not add any custom BioReactor fuels: Different recipe amounts %d %d %d", ids.length, nutr.length, alg.length);
		} else {
			int n = ids.length;
			for (int i = 0; i < n; i++) 
				this.registerBioFuel(new OreDictStack(ids[i], 1), nutr[i], alg[i]);
		}
		
		this.registerBioFuel(ItemSeeds.class, 80);
		this.registerBioFuel(new ItemStack(Blocks.CACTUS), 75);
		this.registerBioFuel(new ItemStack(Items.SUGAR), 75);
		this.registerBioFuel(new ItemStack(Items.EGG), 240);
		this.registerBioFuel(BlockMushroom.class, 40);
		this.registerBioFuel(new ItemStack(Blocks.WATERLILY), 20, 10);
	}

	public int[] getLnutrients(ItemStack item) {
		if (item == null) return null;
		if (item.getItem() instanceof ItemFood) {
			ItemFood food = (ItemFood)item.getItem();
			return new int[]{(int)Math.ceil((food.getSaturationModifier(item) + food.getHealAmount(item)) * (float)Config.Lnutrients_healAmount), 0};
		} else {
			Iterator<BioEntry> iterator = bioList.iterator();
			while (iterator.hasNext())
			{
				BioEntry entry = iterator.next();
				if (entry.matches(item)) return new int[]{entry.nutrients, entry.algae};
			}
			return null;
		}
	}

	public class BioEntry {
		public final Object item;
		public final int nutrients;
		public final int algae;

		public BioEntry(Object item, int n) {
			this(item, n, 0);
		}

		public BioEntry(Object item, int n, int a) {
			this.item = item;
			this.nutrients = n;
			this.algae = a;
		}

		@SuppressWarnings("rawtypes")
		public boolean matches(ItemStack item) {
			if (this.item instanceof ItemStack) return ((ItemStack)this.item).isItemEqual(item);
			else if (this.item instanceof OreDictStack){
				return ((OreDictStack)this.item).isEqual(item);
			} else if (this.item instanceof Class) {
				if (item.getItem() instanceof ItemBlock) return ((Class)this.item).isInstance(((ItemBlock)item.getItem()).block);
				else return ((Class)this.item).isInstance(item.getItem());
			} else return false;
		}
	}

}
