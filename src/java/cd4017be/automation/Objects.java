package cd4017be.automation;

import cd4017be.automation.Block.*;
import cd4017be.automation.Item.*;
import cd4017be.automation.pipes.BasicWarpPipe;
import cd4017be.automation.shaft.GasContainer;
import cd4017be.automation.shaft.ShaftComponent;
import cd4017be.lib.DefaultBlock;
import cd4017be.lib.DefaultItem;
import cd4017be.lib.TileBlock;
import cd4017be.lib.templates.BlockPipe;
import net.minecraft.block.material.Material;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.fluids.Fluid;

public class Objects {

	//Fluids
	public static Fluid L_water; //1000g/l
	public static Fluid L_lava; //??
	public static Fluid L_steam; //5g/l (8xComp) :x200
	public static Fluid L_waterG; //0.625g/l :x1600
	public static Fluid L_biomass;
	public static Fluid L_antimatter;
	public static Fluid L_nitrogenG; //1.25g/l, 273K :x640
	public static Fluid L_nitrogenL; //800g/l, 77K
	public static Fluid L_hydrogenG; //.09g/l, 273K :x800 
	public static Fluid L_hydrogenL; //72g/l, 21K
	public static Fluid L_heliumG; //.18g/l, 273K :x800
	public static Fluid L_heliumL; //144g/l, 4K
	public static Fluid L_oxygenG; //1.62g/l, 273K :x800
	public static Fluid L_oxygenL; //1296g/l, 90K

	//Items
	public static ItemMaterial material;
	public static ItemSelectionTool selectionTool;
	public static ItemVoltMeter voltMeter;
	public static ItemEnergyCell energyCell;
	public static ItemEnergyTool chisle;
	public static ItemCutter cutter;
	public static ItemPortableMagnet portableMagnet;
	public static ItemBuilderTexture builderTexture;
	public static ItemTeleporterCoords teleporterCoords;
	public static ItemMinerDrill stoneDrill;
	public static ItemMinerDrill ironDrill;
	public static ItemMinerDrill diamondDrill;
	public static ItemAntimatterLaser amLaser;
	public static ItemMatterCannon mCannon;
	public static ItemLiquidAir contLiquidAir;
	public static ItemAlgaeFood contAlgaeFood;
	public static ItemInvEnergy contInvEnergy;
	public static ItemJetpackFuel contJetFuel;
	public static ItemJetpack jetpack;
	public static ItemJetpack jetpackIron;
	public static ItemJetpack jetpackSteel;
	public static ItemJetpack jetpackGraphite;
	public static ItemJetpack jetpackUnbr;
	public static ItemMatterInterface matterInterface;
	public static ItemFluidDummy fluidDummy;
	public static ItemFluidUpgrade fluidUpgrade;
	public static ItemItemUpgrade itemUpgrade;
	public static ItemFurnace portableFurnace;
	public static ItemInventory portableInventory;
	public static ItemPortableCrafter portableCrafter;
	public static ItemPortableGenerator portableGenerator;
	public static ItemRemoteInv portableRemoteInv;
	public static ItemPortableTeleporter portableTeleporter;
	public static ItemPortablePump portablePump;
	public static ItemTranslocator translocator;
	public static ItemPortableTesla portableTesla;
	public static ItemPlacement placement;
	public static ItemMachineSynchronizer synchronizer;
	public static DefaultItem remBlockType;
	public static ItemVertexSel vertexSel;

	//Blocks
	public static BlockOre ore;
	public static TileBlock pool;
	public static BlockUnbreakable unbrStone;
	public static GlassUnbreakable unbrGlass;
	public static BlockSkyLight light;
	public static GhostBlock placementHelper;
	public static TileBlock solarpanel;
	public static TileBlock teslaTransmitterLV;
	public static TileBlock teslaTransmitter;
	public static TileBlock wormhole;
	public static TileBlock lightShaft;
	public static BlockPipe wireC;
	public static BlockPipe wireA;
	public static BlockPipe wireH;
	public static BlockPipe liquidPipe;
	public static BlockPipe itemPipe;
	public static BlockPipe warpPipe;
	public static TileBlock voltageTransformer;
	public static TileBlock SCSU;
	public static TileBlock OCSU;
	public static TileBlock CCSU;
	public static TileBlock steamEngine;
	public static TileBlock steamTurbine;
	public static TileBlock steamGenerator;
	public static TileBlock steamBoiler;
	public static TileBlock lavaCooler;
	public static TileBlock energyFurnace;
	public static TileBlock farm;
	public static TileBlock miner;
	public static TileBlock magnet;
	public static TileBlock link;
	public static TileBlock linkHV;
	public static TileBlock texMaker;
	public static TileBlock builder;
	public static TileBlock algaePool;
	public static TileBlock teleporter;
	public static TileBlock advancedFurnace;
	public static TileBlock pump;
	public static TileBlock massstorageChest;
	public static TileBlock matterOrb;
	public static TileBlock antimatterBombE;
	public static TileBlock antimatterBombF;
	public static TileBlock antimatterTank;
	public static TileBlock antimatterFabricator;
	public static TileBlock antimatterAnihilator;
	public static TileBlock hpSolarpanel;
	public static TileBlock autoCrafting;
	public static TileBlock geothermalFurnace;
	public static TileBlock steamCompressor;
	public static TileBlock electricCompressor;
	public static TileBlock tank;
	public static TileBlock security;
	public static TileBlock decompCooler;
	public static TileBlock collector;
	public static TileBlock trash;
	public static TileBlock electrolyser;
	public static TileBlock fuelCell;
	public static TileBlock detector;
	public static TileBlock itemSorter;
	public static TileBlock matterInterfaceB;
	public static TileBlock fluidPacker;
	public static TileBlock hugeTank;
	public static TileBlock fluidVent;
	public static TileBlock gravCond;
	public static TileBlock itemBuffer;
	public static TileBlock quantumTank;
	public static TileBlock vertShemGen;
	public static TileBlock heatRadiator;
	public static BlockShaft shaft;
	public static TileBlock electricCoilC;
	public static TileBlock electricCoilA;
	public static TileBlock electricCoilH;
	public static TileBlock electricHeater;
	public static DefaultBlock thermIns;
	public static TileBlock pneumaticPiston;
	public static BlockPipe gasPipe;
	public static TileBlock solidFuelHeater;
	public static TileBlock gasVent;
	public static TileBlock heatedFurnace;

	//Materials
	public static Material M_thermIns;

	//TODO init Capabilities
	@CapabilityInject(BasicWarpPipe.class)
	public static Capability<BasicWarpPipe> WARP_PIPE_CAP;
	@CapabilityInject(ShaftComponent.class)
	public static Capability<ShaftComponent> SHAFT_CAP;
	@CapabilityInject(GasContainer.class)
	public static Capability<GasContainer> GAS_CAP;

}
