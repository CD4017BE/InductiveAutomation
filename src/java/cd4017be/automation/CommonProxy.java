package cd4017be.automation;

import cd4017be.automation.TileEntity.*;
import cd4017be.lib.TileBlockRegistry;
import cd4017be.lib.Gui.TileContainer;
import static cd4017be.automation.Objects.*;

/**
 *
 * @author CD4017BE
 */
public class CommonProxy {

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

}
