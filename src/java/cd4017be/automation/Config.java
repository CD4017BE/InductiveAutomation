/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cd4017be.automation;

import java.io.File;
import java.io.IOException;

import org.apache.logging.log4j.Level;

import cpw.mods.fml.common.FMLLog;
import net.minecraft.init.Blocks;
import cd4017be.api.automation.AreaProtect;
import cd4017be.api.automation.AutomationRecipes;
import cd4017be.api.automation.ProtectLvl;
import cd4017be.api.energy.EnergyIndustrialCraft;
import cd4017be.api.energy.EnergyThermalExpansion;
import cd4017be.automation.Entity.EntityAntimatterExplosion1;
import cd4017be.automation.Item.ItemAntimatterLaser;
import cd4017be.automation.Item.ItemAntimatterTank;
import cd4017be.automation.Item.ItemFurnace;
import cd4017be.automation.Item.ItemJetpackFuel;
import cd4017be.automation.Item.ItemMatterCannon;
import cd4017be.automation.Item.ItemPortablePump;
import cd4017be.automation.Item.ItemPortableTeleporter;
import cd4017be.automation.TileEntity.Builder;
import cd4017be.automation.TileEntity.ElectricCompressor;
import cd4017be.automation.TileEntity.EnergyFurnace;
import cd4017be.automation.TileEntity.Farm;
import cd4017be.automation.TileEntity.GeothermalFurnace;
import cd4017be.automation.TileEntity.GraviCond;
import cd4017be.automation.TileEntity.Magnet;
import cd4017be.automation.TileEntity.Miner;
import cd4017be.automation.TileEntity.Pump;
import cd4017be.automation.TileEntity.SteamCompressor;
import cd4017be.automation.TileEntity.Teleporter;
import cd4017be.lib.ConfigurationFile;

/**
 *
 * @author CD4017BE
 */
public class Config 
{
    public static ConfigurationFile data = new ConfigurationFile();
    public static int[] Umax = {240, 1200, 8000, 24000, 120000};//LV, MV, HV, teslaLV, teslaHV
    public static float[] Rcond = {0.01F, 0.001F, 0.0F};//Basic, Conductive, SuperConductive
    public static int[] Ecap = {16000, 128000, 1024000};//Single, Octa, Crystal
    public static int[] tankCap = {1000, 8000, 64000, 4096000, 160000000, 2097152000};//Pipe, Internal, Tank, HugeTank, AntimatterTank, QuantumTank
    //energy
    public static float E_Steam = 0.2F; // kJ/L
    public static int K_dryBiomass = 6400;
    //Power
    public static float[] PsteamGen = {4.0F, 24.0F, 120.0F};
    public static int PfuelCell = 160;
    public static float[] Psolar = {0.5F, 4.0F};
    //steamBoiler, LavaCooler
    public static int steam_waterIn = 100;
    public static int steam_waterOut = 200;
    public static int steam_biomassIn = 125;
    public static int Lsteam_25T_lavaCooler = 500;
    public static int Lwater_25T_lavaCooler = 20;
    public static int Llava_25T_lavaCooler = 5;
    public static int maxK_steamBoiler = 6400;
    public static int K_Cooking_steamBoiler = 50;
    public static int steam_biomass_burning = 25;
    public static int Lsteam_Kfuel_burning = 1;
    //BioReactor
    public static int Lnutrients_healAmount = 20;
    public static float algaeGrowing = 0.00009F;
    public static float algaeDecaying = 0.0002F;
    //
    public static int m_interdimDist = 10000;
    
    public static int taskQueueSize = 16;
    
    public static int Rmin = 5;
    public static float Pscale = 0.894427191F;
    
    public static int get_LSteam_Cooking()
    {
        return (int)(K_Cooking_steamBoiler / E_Steam);
    }
    
    public static int get_LWater_Cooking()
    {
        return get_LSteam_Cooking() / steam_waterIn;
    }
    
    public static int get_LBiomass_Cooking()
    {
        return get_LSteam_Cooking() / steam_biomassIn;
    }
    
    public static void loadConfig(File file) 
    {
    	file = new File(file, "inductiveAutomation.cfg");
    	boolean useFile = true;
    	if (!file.exists()) {
    		FMLLog.log("Automation", Level.INFO, "Config file does not exist: creating a new one using the Preset.");
    		try {
    			ConfigurationFile.copyData("/assets/automation/config/preset.cfg", file);
    		} catch(IOException e) {
    			FMLLog.log("Automation", Level.WARN, e, "Config file creation failed!");
    			useFile = false;
    		}
    	}	
    	
    	if (useFile) {
    		FMLLog.log("Automation", Level.INFO, "Loading config from File");
    		try {
        		data.load(file);
        	} catch (IOException e) {
        		FMLLog.log("Automation", Level.WARN, e, "Config file loading failed!");
        		useFile = false;
        	}
    	}
    	
    	if (!useFile) {
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
    	af = data.getFloatArray("SteamGen.Pmax");
    	if (af.length == PsteamGen.length) PsteamGen = af;
    	af = data.getFloatArray("SolarGen.Pmax");
    	if (af.length == Psolar.length) Psolar = af;
    	PfuelCell = data.getInt("FuelCell.maxHUse", PfuelCell);
    	Lnutrients_healAmount = data.getInt("Mach.bioReact.FoodNutr", Lnutrients_healAmount);
    	algaeGrowing *= data.getFloat("Mach.bioReact.growth", 1F);
    	algaeDecaying *= data.getFloat("Mach.bioReact.decay", 1F);
    	//Energy conversions
    	E_Steam = data.getFloat("EnergyConv.Steam", E_Steam);
    	EnergyThermalExpansion.E_Factor = data.getFloat("EnergyConv.RF", EnergyThermalExpansion.E_Factor) * 1000F;
    	EnergyIndustrialCraft.E_Factor = data.getFloat("EnergyConv.EU", EnergyIndustrialCraft.E_Factor) * 1000F;
    	//Energy usage
    	float f = 1000F;
    	Builder.Energy = f * data.getFloat("Mach.Builder.Euse", Builder.Energy / f);
    	ElectricCompressor.Energy = data.getFloat("Mach.ElCompr.Euse", ElectricCompressor.Energy);
    	SteamCompressor.Euse = data.getInt("Mach.StCompr.Euse", SteamCompressor.Euse);
    	EnergyFurnace.Euse = data.getFloat("Mach.Furnace.Euse", EnergyFurnace.Euse);
    	GeothermalFurnace.Euse = data.getInt("Mach.GeothFurn.Euse", GeothermalFurnace.Euse);
    	Farm.Energy = f * data.getFloat("Mach.Farm.Euse", Farm.Energy / f);
    	Magnet.Energy = f * data.getFloat("Mach.Magnet.Euse", Magnet.Energy / f);
    	Miner.Energy = f * data.getFloat("Mach.Miner.Emult", Miner.Energy / f);
    	Pump.Energy = f * data.getFloat("Mach.Pump.Euse", Pump.Energy / f);
    	Teleporter.Energy = f * data.getFloat("Mach.Teleport.Emult", Teleporter.Energy / f);
    	AutomationRecipes.LFEmult = data.getFloat("Mach.AdvFurn.Emult", AutomationRecipes.LFEmult);
    	AutomationRecipes.CoolEmult = data.getFloat("Mach.Cooler.Emult", AutomationRecipes.CoolEmult);
    	AutomationRecipes.ElEmult = data.getFloat("Mach.Electrolyser.Emult", AutomationRecipes.ElEmult);
    	ItemAntimatterLaser.EnergyUsage = data.getInt("Tool.AMLaser.Euse", ItemAntimatterLaser.EnergyUsage);
    	ItemFurnace.energyUse = data.getInt("Tool.Furnace.Euse", ItemFurnace.energyUse);
    	ItemMatterCannon.EnergyUsage = data.getInt("Tool.MCannon.Euse", ItemMatterCannon.EnergyUsage);
    	ItemPortablePump.energyUse = data.getInt("Tool.Pump.Euse", ItemPortablePump.energyUse);
    	ItemPortableTeleporter.energyUse = data.getFloat("Tool.Teleport.Emult", ItemPortableTeleporter.energyUse);
    	//Antimatter Bomb
    	EntityAntimatterExplosion1.maxSize = data.getInt("AmBomb.MaxRad", EntityAntimatterExplosion1.maxSize);
    	EntityAntimatterExplosion1.explMult = data.getFloat("AmBomb.ExplMult", EntityAntimatterExplosion1.explMult);
    	ItemAntimatterLaser.AmUsage = data.getFloat("Tool.AmLaser.AMmult", ItemAntimatterLaser.AmUsage);
    	ItemAntimatterLaser.AMDamage = data.getFloat("Tool.AmLaser.AMDmgMult", ItemAntimatterLaser.AMDamage);
    	ItemAntimatterLaser.AMDmgExp = data.getFloat("Tool.AmLaser.AMDmgExp", ItemAntimatterLaser.AMDmgExp);
    	ItemAntimatterLaser.BaseDamage = data.getFloat("Tool.AmLaser.MinDmg", ItemAntimatterLaser.BaseDamage);
    	ItemAntimatterLaser.MaxDamage = data.getFloat("Tool.AmLaser.MaxDmg", ItemAntimatterLaser.MaxDamage);
    	ItemAntimatterLaser.DamageMult = data.getFloat("Tool.AmLaser.AMmult", ItemAntimatterLaser.DamageMult);
    	ItemAntimatterTank.BombMaxCap = data.getInt("AmBomb.MaxAM", ItemAntimatterTank.BombMaxCap);
    	ItemAntimatterTank.explFaktor = EntityAntimatterExplosion1.PowerFactor * EntityAntimatterExplosion1.explMult / Blocks.stone.getExplosionResistance(null) * 0.125D;
    	ItemJetpackFuel.maxFuel = data.getFloat("Tool.JetFuel.dur", ItemJetpackFuel.maxFuel);
    	GraviCond.energyCost = data.getFloat("Mach.GravCond.Euse", GraviCond.energyCost);
    	GraviCond.forceEnergy = data.getFloat("Mach.GravCond.Eforce", GraviCond.forceEnergy);
    	GraviCond.maxVoltage = data.getInt("Mach.GravCond.Umax", GraviCond.maxVoltage);
    	GraviCond.blockWeight = data.getInt("Mach.GravCond.mBlock", GraviCond.blockWeight);
    	GraviCond.itemWeight = data.getInt("Mach.GravCond.mItem", GraviCond.itemWeight);
    	taskQueueSize = data.getShort("ComputercraftAPI.taskQueue.size", (short)taskQueueSize);
    	Rmin = data.getByte("Mach.Rwork.min", (byte)Rmin);
    	if (Rmin < 1) Rmin = 1;
    	Pscale = (float)Math.sqrt(1D - 1D / (double)Rmin);
    	AreaProtect.permissions = data.getByte("SecuritySys.permMode", AreaProtect.permissions);
    	AreaProtect.chunkloadPerm = data.getByte("SecuritySys.chunkloadPerm", AreaProtect.chunkloadPerm);
    	AreaProtect.maxChunksPBlock = data.getByte("SecuritySys.maxChunks", AreaProtect.maxChunksPBlock);
    	af = data.getFloatArray("SecuritySys.Euse");
    	ProtectLvl[] lvls = ProtectLvl.values();
    	for (int i = 0; i < lvls.length && i < af.length; i++) lvls[i].energyCost = af[i];
    	if (af.length >= 5) AreaConfig.ChunkLoadCost = af[4]; 
    }
    
    
}
