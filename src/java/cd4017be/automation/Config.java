package cd4017be.automation;

import java.io.File;
import java.io.IOException;

import org.apache.logging.log4j.Level;

import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraft.init.Blocks;
import cd4017be.api.automation.AreaProtect;
import cd4017be.api.automation.ProtectLvl;
import cd4017be.api.energy.EnergyAPI;
import cd4017be.api.recipes.AutomationRecipes;
import cd4017be.automation.Item.ItemAntimatterLaser;
import cd4017be.automation.Item.ItemAntimatterTank;
import cd4017be.automation.Item.ItemFurnace;
import cd4017be.automation.Item.ItemJetpackFuel;
import cd4017be.automation.Item.ItemMatterCannon;
import cd4017be.automation.Item.ItemPortablePump;
import cd4017be.automation.Item.ItemPortableTeleporter;
import cd4017be.automation.TileEntity.AdvancedFurnace;
import cd4017be.automation.TileEntity.AntimatterAnihilator;
import cd4017be.automation.TileEntity.AntimatterBomb;
import cd4017be.automation.TileEntity.Builder;
import cd4017be.automation.TileEntity.DecompCooler;
import cd4017be.automation.TileEntity.ElectricCompressor;
import cd4017be.automation.TileEntity.Electrolyser;
import cd4017be.automation.TileEntity.EnergyFurnace;
import cd4017be.automation.TileEntity.Farm;
import cd4017be.automation.TileEntity.GeothermalFurnace;
import cd4017be.automation.TileEntity.GraviCond;
import cd4017be.automation.TileEntity.ItemPipe;
import cd4017be.automation.TileEntity.LiquidPipe;
import cd4017be.automation.TileEntity.Magnet;
import cd4017be.automation.TileEntity.Miner;
import cd4017be.automation.TileEntity.Pump;
import cd4017be.automation.TileEntity.SecuritySys;
import cd4017be.automation.TileEntity.SteamCompressor;
import cd4017be.automation.TileEntity.Teleporter;
import cd4017be.lib.ConfigurationFile;
import cd4017be.lib.templates.Inventory;

/**
 *
 * @author CD4017BE
 */
public class Config {

	public static ConfigurationFile data = new ConfigurationFile();
	public static int[] Umax = {240, 1200, 8000, 24000, 120000};//LV, MV, HV, teslaLV, teslaHV
	public static float[] Rcond = {0.01F, 0.001F, 0.0F};//Basic, Conductive, SuperConductive
	public static int[] Ecap = {16000, 128000, 1024000};//Single, Octa, Crystal
	public static int[] tankCap = {1000, 8000, 64000, 4096000, 160000000, 2097152000};//Pipe, Internal, Tank, HugeTank, AntimatterTank, QuantumTank
	//energy
	public static float E_Steam = 0.2F; // kJ/L
	//Power
	public static float[] Pgenerator = {4.0F, 24.0F, 120.0F, 0.5F, 4.0F, 160.0F, 14400.0F};
	public static int[] Ugenerator = {240, 1200, 1200, 120, 240, 4000, 8000};
	//steamBoiler, LavaCooler
	public static int steam_water = 200;
	public static int steam_biomassIn = 125;
	public static int maxK_steamBoiler = 6400;
	public static int K_Cooking_steamBoiler = 50;
	public static int steam_biomass_burning = 25;
	public static int Lsteam_Kfuel_burning = 1;
	//BioReactor
	public static float algaeGrowing = 0.00009F;
	public static float algaeDecaying = 0.0002F;
	public static int m_interdimDist = 10000;
	public static int taskQueueSize = 16;
	public static int Rmin = 5;
	public static float Pscale = 0.894427191F;

	public static int get_LSteam_Cooking() {
		return (int)(K_Cooking_steamBoiler / E_Steam);
	}

	public static int get_LWater_Cooking() {
		return get_LSteam_Cooking() / steam_water;
	}

	public static int get_LBiomass_Cooking() {
		return get_LSteam_Cooking() / steam_biomassIn;
	}

	public static void loadConfig(File file) {
		if (file != null) {
			FMLLog.log("Automation", Level.INFO, "Loading config from File");
			try {
				data.load(file);
			} catch (IOException e) {
				FMLLog.log("Automation", Level.WARN, e, "Config file loading failed!");
			}
		} else {
			FMLLog.log("Automation", Level.WARN, "No config data loaded!");
		}
		
		//Tuning
		int[] ai = data.getIntArray("EnergyTiers.Umax");
		if (ai.length >= Umax.length) Umax = ai;
		float[] af = data.getFloatArray("EnergyTiers.Rcond");
		if (af.length >= Rcond.length) Rcond = af;
		ai = data.getIntArray("EnergyTiers.Ecap");
		if (ai.length >= Ecap.length) Ecap = ai;
		ai = data.getIntArray("FluidTiers.TankCap");
		if (ai.length >= tankCap.length) tankCap = ai;
		af = data.getFloatArray("Generators.Pmax");
		if (af.length == Pgenerator.length) Pgenerator = af;
		ai = data.getIntArray("Generators.Umax");
		if (ai.length == Ugenerator.length) Ugenerator = ai;
		AntimatterAnihilator.setP(Pgenerator[6]);
		AutomationRecipes.Lnutrients_healAmount = data.getInt("bioReact.FoodNutr", (int)AutomationRecipes.Lnutrients_healAmount);
		algaeGrowing *= data.getFloat("bioReact.growth", 1F);
		algaeDecaying *= data.getFloat("bioReact.decay", 1F);
		//Energy conversions
		E_Steam = data.getFloat("EnergyConv.Steam", E_Steam);
		EnergyAPI.RF_value = data.getFloat("EnergyConv.RF", (float)EnergyAPI.RF_value / 1000F) * 1000F;
		EnergyAPI.EU_value = data.getFloat("EnergyConv.EU", (float)EnergyAPI.EU_value / 1000F) * 1000F;
		EnergyAPI.OC_value = data.getFloat("EnergyConv.OC", (float)EnergyAPI.OC_value / 1000F) * 1000F;
		//Energy usage
		float f = 1000F;
		Inventory.ticks = data.getByte("inventory.ticks", (byte)Inventory.ticks);
		ItemPipe.ticks = data.getByte("itemPipe.ticks", (byte)ItemPipe.ticks);
		LiquidPipe.ticks = data.getByte("fluidPipe.ticks", (byte)LiquidPipe.ticks);
		Builder.Energy = f * data.getFloat("Builder.Euse", Builder.Energy / f);
		Builder.resistor = data.getFloat("Builder.Rwork", Builder.resistor);
		Builder.eScale = (float)Math.sqrt(1D - 1D / Builder.resistor);
		Builder.Umax = data.getInt("Builder.Umax", Builder.Umax);
		Builder.maxSpeed = data.getByte("Builder.maxSpeed", Builder.maxSpeed);
		ElectricCompressor.Energy = data.getFloat("ElCompr.Euse", ElectricCompressor.Energy);
		ElectricCompressor.Umax = data.getInt("ElCompr.Umax", ElectricCompressor.Umax);
		SteamCompressor.Euse = data.getInt("StCompr.Euse", SteamCompressor.Euse);
		EnergyFurnace.Euse = data.getFloat("Furnace.Euse", EnergyFurnace.Euse);
		EnergyFurnace.Umax = data.getInt("Furnace.Umax", EnergyFurnace.Umax);
		GeothermalFurnace.Euse = data.getInt("GeothFurn.Euse", GeothermalFurnace.Euse);
		AdvancedFurnace.Umax = data.getInt("AdvFurn.Umax", AdvancedFurnace.Umax);
		DecompCooler.Umax = data.getInt("Cooler.Umax", DecompCooler.Umax);
		Electrolyser.Umax = data.getInt("Electrolyser.Umax", Electrolyser.Umax);
		Magnet.Energy = f * data.getFloat("Magnet.Euse", Magnet.Energy / f);
		Magnet.Umax = data.getInt("Magnet.Umax", Magnet.Umax);
		Farm.Energy = f * data.getFloat("Farm.Euse", Farm.Energy / f);
		Farm.resistor = data.getFloat("Farm.Rwork", Farm.resistor);
		Farm.eScale = (float)Math.sqrt(1D - 1D / Farm.resistor);
		Farm.Umax = data.getInt("Farm.Umax", Farm.Umax);
		Miner.Energy = f * data.getFloat("Miner.Emult", Miner.Energy / f);
		Miner.resistor = data.getFloat("Miner.Rwork", Miner.resistor);
		Miner.eScale = (float)Math.sqrt(1D - 1D / Miner.resistor);
		Miner.Umax = data.getInt("Miner.Umax", Miner.Umax);
		Miner.maxSpeed = data.getByte("Miner.maxSpeed", Miner.maxSpeed);
		Pump.Energy = f * data.getFloat("Pump.Euse", Pump.Energy / f);
		Pump.resistor = data.getFloat("Pump.Rwork", Pump.resistor);
		Pump.eScale = (float)Math.sqrt(1D - 1D / Pump.resistor);
		Pump.Umax = data.getInt("Pump.Umax", Pump.Umax);
		Teleporter.Energy = f * data.getFloat("Teleport.Emult", Teleporter.Energy / f);
		Teleporter.resistor = data.getFloat("Teleport.Rwork", Teleporter.resistor);
		Teleporter.eScale = (float)Math.sqrt(1D - 1D / Teleporter.resistor);
		Teleporter.Umax = data.getInt("Teleporter.Umax", Teleporter.Umax);
		ItemAntimatterLaser.EnergyUsage = data.getInt("Tool.AMLaser.Euse", ItemAntimatterLaser.EnergyUsage);
		ItemFurnace.energyUse = data.getInt("Tool.Furnace.Euse", ItemFurnace.energyUse);
		ItemMatterCannon.EnergyUsage = data.getInt("Tool.MCannon.Euse", ItemMatterCannon.EnergyUsage);
		ItemPortablePump.energyUse = data.getInt("Tool.Pump.Euse", ItemPortablePump.energyUse);
		ItemPortableTeleporter.energyUse = data.getFloat("Tool.Teleport.Emult", ItemPortableTeleporter.energyUse);
		//Antimatter Bomb
		AntimatterBomb.maxSize = data.getInt("AmBomb.MaxRad", AntimatterBomb.maxSize);
		AntimatterBomb.explMult = data.getFloat("AmBomb.ExplMult", AntimatterBomb.explMult);
		ItemAntimatterLaser.AmUsage = data.getFloat("Tool.AmLaser.AMmult", ItemAntimatterLaser.AmUsage);
		ItemAntimatterLaser.AMDamage = data.getFloat("Tool.AmLaser.AMDmgMult", ItemAntimatterLaser.AMDamage);
		ItemAntimatterLaser.AMDmgExp = data.getFloat("Tool.AmLaser.AMDmgExp", ItemAntimatterLaser.AMDmgExp);
		ItemAntimatterLaser.BaseDamage = data.getFloat("Tool.AmLaser.MinDmg", ItemAntimatterLaser.BaseDamage);
		ItemAntimatterLaser.MaxDamage = data.getFloat("Tool.AmLaser.MaxDmg", ItemAntimatterLaser.MaxDamage);
		ItemAntimatterLaser.DamageMult = data.getFloat("Tool.AmLaser.AMmult", ItemAntimatterLaser.DamageMult);
		ItemAntimatterTank.BombMaxCap = data.getInt("AmBomb.MaxAM", ItemAntimatterTank.BombMaxCap);
		ItemAntimatterTank.explFaktor = AntimatterBomb.PowerFactor * AntimatterBomb.explMult / Blocks.STONE.getExplosionResistance(null) * 0.125D;
		ItemJetpackFuel.H2Mult = data.getFloat("Jetpack.H2.val", ItemJetpackFuel.H2Mult * 1000F) / 1000F;
		ItemJetpackFuel.O2Mult = ItemJetpackFuel.H2Mult * 2F;
		ItemJetpackFuel.electricEmult = data.getFloat("Jetpack.el.val", ItemJetpackFuel.electricEmult * 1000F) / 1000F;
		GraviCond.energyCost = data.getFloat("GravCond.Euse", GraviCond.energyCost);
		GraviCond.forceEnergy = data.getFloat("GravCond.Eforce", GraviCond.forceEnergy);
		GraviCond.Umax = data.getInt("GravCond.Umax", GraviCond.Umax);
		GraviCond.blockWeight = data.getInt("GravCond.mBlock", GraviCond.blockWeight);
		GraviCond.itemWeight = data.getInt("GravCond.mItem", GraviCond.itemWeight);
		taskQueueSize = data.getShort("computer.taskQueue.size", (short)taskQueueSize);
		Rmin = data.getInt("Rwork.min", Rmin);
		if (Rmin < 1) Rmin = 1;
		Pscale = (float)Math.sqrt(1D - 1D / (double)Rmin);
		AreaProtect.permissions = data.getByte("SecuritySys.permMode", AreaProtect.permissions);
		AreaProtect.chunkloadPerm = data.getByte("SecuritySys.chunkloadPerm", AreaProtect.chunkloadPerm);
		AreaProtect.maxChunksPBlock = data.getByte("SecuritySys.maxChunks", AreaProtect.maxChunksPBlock);
		if (AreaProtect.maxChunksPBlock < 0) {AreaProtect.maxChunksPBlock = 0; AreaProtect.chunkloadPerm = -1;}
		else if (AreaProtect.maxChunksPBlock > 64) AreaProtect.maxChunksPBlock = 64;
		if (AreaProtect.maxChunksPBlock > ForgeChunkManager.getMaxTicketLengthFor("Automation")) {
			FMLLog.log("Automation", Level.INFO, "set Forge config property \"%s\" to %d", "maximumChunksPerTicket", AreaProtect.maxChunksPBlock);
			ForgeChunkManager.addConfigProperty(Automation.instance, "maximumChunksPerTicket", "" + AreaProtect.maxChunksPBlock, Property.Type.INTEGER);
		}
		af = data.getFloatArray("SecuritySys.Euse");
		ProtectLvl[] lvls = ProtectLvl.values();
		for (int i = 0; i < lvls.length && i < af.length; i++) lvls[i].energyCost = af[i];
		if (af.length >= 5) AreaConfig.ChunkLoadCost = af[4];
		SecuritySys.Umax = data.getInt("SecuritySys.Umax", SecuritySys.Umax);
		SecuritySys.Ecap = data.getFloat("SecuritySys.Ecap", SecuritySys.Ecap);
	}

}
