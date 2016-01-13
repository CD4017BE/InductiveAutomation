package cd4017be.automation;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.IWorldGenerator;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;

import java.util.ArrayList;
import java.util.Random;

import org.apache.logging.log4j.Level;

import cd4017be.api.automation.AreaProtect;
import cd4017be.api.automation.AutomationRecipes;
import cd4017be.api.computers.ComputerAPI;
import cd4017be.automation.Block.BlockItemPipe;
import cd4017be.automation.Block.BlockLiquidPipe;
import cd4017be.automation.Block.BlockOre;
import cd4017be.automation.Block.BlockSkyLight;
import cd4017be.automation.Block.BlockUnbreakable;
import cd4017be.automation.Block.GhostBlock;
import cd4017be.automation.Block.GlassUnbreakable;
import cd4017be.automation.Entity.EntityAntimatterExplosion1;
import cd4017be.automation.Item.ItemAlgaeFood;
import cd4017be.automation.Item.ItemAntimatterLaser;
import cd4017be.automation.Item.ItemAntimatterTank;
import cd4017be.automation.Item.ItemBuilderAirType;
import cd4017be.automation.Item.ItemBuilderTexture;
import cd4017be.automation.Item.ItemCutter;
import cd4017be.automation.Item.ItemESU;
import cd4017be.automation.Item.ItemEnergyCell;
import cd4017be.automation.Item.ItemEnergyTool;
import cd4017be.automation.Item.ItemFluidDummy;
import cd4017be.automation.Item.ItemFluidUpgrade;
import cd4017be.automation.Item.ItemFurnace;
import cd4017be.automation.Item.ItemHugeTank;
import cd4017be.automation.Item.ItemInterdimHole;
import cd4017be.automation.Item.ItemInvEnergy;
import cd4017be.automation.Item.ItemInventory;
import cd4017be.automation.Item.ItemItemUpgrade;
import cd4017be.automation.Item.ItemJetpack;
import cd4017be.automation.Item.ItemJetpackFuel;
import cd4017be.automation.Item.ItemLiquidAir;
import cd4017be.automation.Item.ItemMachineSynchronizer;
import cd4017be.automation.Item.ItemMaterial;
import cd4017be.automation.Item.ItemMatterCannon;
import cd4017be.automation.Item.ItemMatterInterface;
import cd4017be.automation.Item.ItemMatterOrb;
import cd4017be.automation.Item.ItemMinerDrill;
import cd4017be.automation.Item.ItemPlacement;
import cd4017be.automation.Item.ItemPortableCrafter;
import cd4017be.automation.Item.ItemPortableGenerator;
import cd4017be.automation.Item.ItemPortableMagnet;
import cd4017be.automation.Item.ItemPortablePump;
import cd4017be.automation.Item.ItemPortableTeleporter;
import cd4017be.automation.Item.ItemPortableTesla;
import cd4017be.automation.Item.ItemQuantumTank;
import cd4017be.automation.Item.ItemRemoteInv;
import cd4017be.automation.Item.ItemSelectionTool;
import cd4017be.automation.Item.ItemTank;
import cd4017be.automation.Item.ItemTeleporterCoords;
import cd4017be.automation.Item.ItemTranslocator;
import cd4017be.automation.Item.ItemVertexSel;
import cd4017be.automation.Item.ItemVoltMeter;
import cd4017be.automation.TileEntity.AdvancedFurnace;
import cd4017be.automation.TileEntity.AlgaePool;
import cd4017be.automation.TileEntity.AntimatterAnihilator;
import cd4017be.automation.TileEntity.AntimatterBomb;
import cd4017be.automation.TileEntity.AntimatterFabricator;
import cd4017be.automation.TileEntity.AntimatterTank;
import cd4017be.automation.TileEntity.AutoCrafting;
import cd4017be.automation.TileEntity.Builder;
import cd4017be.automation.TileEntity.Collector;
import cd4017be.automation.TileEntity.DecompCooler;
import cd4017be.automation.TileEntity.Detector;
import cd4017be.automation.TileEntity.ELink;
import cd4017be.automation.TileEntity.ESU;
import cd4017be.automation.TileEntity.ElectricCompressor;
import cd4017be.automation.TileEntity.Electrolyser;
import cd4017be.automation.TileEntity.EnergyFurnace;
import cd4017be.automation.TileEntity.Farm;
import cd4017be.automation.TileEntity.FluidPacker;
import cd4017be.automation.TileEntity.FluidVent;
import cd4017be.automation.TileEntity.FuelCell;
import cd4017be.automation.TileEntity.GeothermalFurnace;
import cd4017be.automation.TileEntity.GraviCond;
import cd4017be.automation.TileEntity.HPSolarpanel;
import cd4017be.automation.TileEntity.HugeTank;
import cd4017be.automation.TileEntity.InterdimHole;
import cd4017be.automation.TileEntity.ItemBuffer;
import cd4017be.automation.TileEntity.ItemPipe;
import cd4017be.automation.TileEntity.ItemSorter;
import cd4017be.automation.TileEntity.ItemWarpPipe;
import cd4017be.automation.TileEntity.LavaCooler;
import cd4017be.automation.TileEntity.LightShaft;
import cd4017be.automation.TileEntity.LiquidPipe;
import cd4017be.automation.TileEntity.LiquidWarpPipe;
import cd4017be.automation.TileEntity.Magnet;
import cd4017be.automation.TileEntity.MassstorageChest;
import cd4017be.automation.TileEntity.MatterInterface;
import cd4017be.automation.TileEntity.MatterOrb;
import cd4017be.automation.TileEntity.Miner;
import cd4017be.automation.TileEntity.Pump;
import cd4017be.automation.TileEntity.QuantumTank;
import cd4017be.automation.TileEntity.SecuritySys;
import cd4017be.automation.TileEntity.Solarpanel;
import cd4017be.automation.TileEntity.SteamBoiler;
import cd4017be.automation.TileEntity.SteamCompressor;
import cd4017be.automation.TileEntity.SteamEngine;
import cd4017be.automation.TileEntity.SteamGenerator;
import cd4017be.automation.TileEntity.SteamTurbine;
import cd4017be.automation.TileEntity.Tank;
import cd4017be.automation.TileEntity.Teleporter;
import cd4017be.automation.TileEntity.TeslaTransmitter;
import cd4017be.automation.TileEntity.TeslaTransmitterLV;
import cd4017be.automation.TileEntity.TextureMaker;
import cd4017be.automation.TileEntity.Trash;
import cd4017be.automation.TileEntity.VertexShematicGen;
import cd4017be.automation.TileEntity.VoltageTransformer;
import cd4017be.automation.TileEntity.Wire;
import cd4017be.automation.jetpack.JetPackConfig;
import cd4017be.automation.jetpack.PacketHandler;
import cd4017be.lib.BlockGuiHandler;
import cd4017be.lib.BlockItemRegistry;
import cd4017be.lib.DefaultItemBlock;
import cd4017be.lib.TileBlockRegistry;
import cd4017be.lib.TileContainer;
import static cd4017be.lib.BlockItemRegistry.stack;
import cd4017be.lib.ModFluid;
import cd4017be.lib.TileBlock;
import cd4017be.lib.templates.BlockPipe;
import cd4017be.lib.templates.BlockSuperfluid;
import net.minecraft.block.Block;
import net.minecraft.block.BlockMushroom;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemSeeds;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.feature.WorldGenMinable;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidContainerRegistry.FluidContainerData;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;

/**
 *
 * @author CD4017BE
 */
@Mod(modid="Automation", name="Inductive Automation", version="3.7.2")
public class Automation implements IWorldGenerator
{
    
    // The instance of your mod that Forge uses.
    @Instance("Automation")
    public static Automation instance;
    
    // Says where the client and server 'proxy' code is loaded.
    @SidedProxy(clientSide="cd4017be.automation.ClientProxy", serverSide="cd4017be.automation.CommonProxy")
    public static CommonProxy proxy;
    
    public static CreativeTabs tabAutomation;
    public static CreativeTabs tabFluids;
    
    public static Fluid L_water; //1000g/l
    public static Fluid L_lava; //??
    public static Fluid L_steam; //5g/l (8.3xComp) :x200
    public static Fluid L_biomass;
    public static Fluid L_antimatter;
    public static Fluid L_nitrogenG; //1.25g/l, 273K :x640
    public static Fluid L_nitrogenL; //800g/l, 77K
    public static Fluid L_hydrogenG; //.09g/l, 273K :x800 
    public static Fluid L_hydrogenL; //72g/l, 21K
    public static Fluid L_heliumG; //.18g/l, 273K :x800
    public static Fluid L_heliumL; //144g/l, 4K
    public static Fluid L_oxygenG; //1.62g/l, 273K :x800
    public static Fluid L_oxygenL; //1296g/l, 90K bläulich
    
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) 
    {
        Config.loadConfig(event.getModConfigurationDirectory());
    	BlockItemRegistry.setMod("automation");
        tabAutomation = new CreativeTabAutomation("automation");
        tabFluids = new CreativeTabFluids("fluids");
        initBlocks();
        initLiquids();
        initItems();
        Block ore = BlockItemRegistry.getBlock("tile.ore");
        ore.setHarvestLevel("pickaxe", 1, BlockOre.ID_Copper);
        ore.setHarvestLevel("pickaxe", 2, BlockOre.ID_Silver);
        copperGen = new WorldGenMinable(ore, BlockOre.ID_Copper, 9, Blocks.stone);
        silverGen = new WorldGenMinable(ore, BlockOre.ID_Silver, 6, Blocks.stone);
        OreDictionary.registerOre("oreCopper", new ItemStack(ore, 1, BlockOre.ID_Copper));
        OreDictionary.registerOre("oreSilver", new ItemStack(ore, 1, BlockOre.ID_Silver));
        OreDictionary.registerOre("ingotCopper", stack("CopperI", 1));
        OreDictionary.registerOre("ingotSilver", stack("SilverI", 1));
        OreDictionary.registerOre("ingotElectrum", stack("ElectrumI", 1));
        OreDictionary.registerOre("ingotConductiveAlloy", stack("CondIA", 1));
        OreDictionary.registerOre("ingotSteel", stack("SteelI", 1));
        OreDictionary.registerOre("dustIron", stack("IronD", 1));
        OreDictionary.registerOre("dustGold", stack("GoldD", 1));
        OreDictionary.registerOre("dustCopper", stack("CopperD", 1));
        OreDictionary.registerOre("dustSilver", stack("SilverD", 1));
        OreDictionary.registerOre("dustElectrum", stack("ElectrumR", 1));
        OreDictionary.registerOre("dustConductiveAlloy", stack("CondRA", 1));
        if (event.getSide().isClient()) {
            JetPackConfig.loadData(event.getModConfigurationDirectory());
        }
    }
    
    @Mod.EventHandler
    public void load(FMLInitializationEvent event) 
    {
        GameRegistry.registerWorldGenerator(this, 0);
        BlockGuiHandler.registerMod(this);
        PacketHandler.register();
        GameRegistry.registerFuelHandler(proxy);
        AreaProtect.register(this);
        
        proxy.registerRenderers();
        
        EntityRegistry.registerModEntity(EntityAntimatterExplosion1.class, "AntimatterExplosion", 0, this, 32, 1, false);
        System.out.println("Automation: Set Explosion-Resistance of Bedrock to: " + Block.getBlockFromName("bedrock").setResistance(2000000.0F).getExplosionResistance(null));
        
        proxy.registerBioFuels();
        
        FluidContainerRegistry.registerFluidContainer(new FluidContainerData(getLiquid(L_biomass, FluidContainerRegistry.BUCKET_VOLUME), stack("LCBiomass", 1), FluidContainerRegistry.EMPTY_BOTTLE));
        FluidContainerRegistry.registerFluidContainer(new FluidContainerData(getLiquid(L_nitrogenL, 100), stack("LCNitrogen", 1), FluidContainerRegistry.EMPTY_BOTTLE));
        FluidContainerRegistry.registerFluidContainer(new FluidContainerData(getLiquid(L_hydrogenL, 100), stack("LCHydrogen", 1), FluidContainerRegistry.EMPTY_BOTTLE));
        FluidContainerRegistry.registerFluidContainer(new FluidContainerData(getLiquid(L_heliumL, 100), stack("LCHelium", 1), FluidContainerRegistry.EMPTY_BOTTLE));
        FluidContainerRegistry.registerFluidContainer(new FluidContainerData(getLiquid(L_oxygenL, 100), stack("LCOxygen", 1), FluidContainerRegistry.EMPTY_BOTTLE));
        proxy.registerRecipes();
        proxy.registerLiquidRecipes();
        proxy.registerCompressorRecipes();
        proxy.registerCoolerRecipes();
        proxy.registerElectrolyserRecipes();
        proxy.registerTrashRecipes();
        ComputerAPI.register();
    }
    
    /**
     * clears configuration data that is not used anymore after initialization to improve performance
     */
    private void cleanConfig()
    {
    	ArrayList<String> rem = Config.data.getVariables("B.recipe", "AT.rcp", "AF.rcp", "I.rcp");
    	Config.data.removeEntry(rem.toArray(new String[rem.size()]));
    }
    
    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
    	for (String s : Config.data.getStringArray("rcp.Pulverize.oreIn")) AutomationRecipes.addItemCrushingRecipes(s);
    	this.cleanConfig();
    	
    	byte gen = Config.data.getByte("oreGen.Copper", (byte)0);
        if (gen == -1 || (gen == 0 && OreDictionary.getOres("oreCopper").size() > 1)) {
            FMLLog.log("Automation", Level.INFO, "Copper ore world generation disabled. " + (gen == 0 ? "(provided by other mods)" : "(turned off in config)"));
            this.copperGen = null;
        }
        gen = Config.data.getByte("oreGen.Silver", (byte)0);
        if (gen == -1 || (gen == 0 && OreDictionary.getOres("oreSilver").size() > 1)) {
            FMLLog.log("Automation", Level.INFO, "Silver ore world generation disabled. " + (gen == 0 ? "(provided by other mods)" : "(turned off in config)"));
            this.silverGen = null;
        }
    }
    
    private void initItems()
    {
        String path = BlockItemRegistry.texPath().concat("tools/");
        
        new ItemMaterial("material", BlockItemRegistry.texPath());
        
        BlockItemRegistry.registerName((new ItemSelectionTool("selectionTool", path+"selection")), "Area configuration Tool");
        BlockItemRegistry.registerName((new ItemVoltMeter("voltMeter", path+"voltMeter")), "Volt Meter");
        BlockItemRegistry.registerName((new ItemEnergyCell("energyCell", path+"energyCell", Config.Ecap[0])), "Energy Cell");
        BlockItemRegistry.registerName((new ItemEnergyTool("chisle", path+"chisle", Config.Ecap[0], Config.data.getInt("Tool.Chisle.Euse", 25), Config.data.getFloat("Tool.Chisle.digSpeed", 16F), 4)).setToolClass(new String[]{"pickaxe", "shovel"}, Config.data.getShort("item.chisle.harvestLvl", (short)3)), "Electric Chisel");
        BlockItemRegistry.registerName((new ItemCutter("cutter", path+"cutter", Config.Ecap[0], Config.data.getInt("Tool.Cutter.Euse", 25), 7)), "Electric Cutter");
        BlockItemRegistry.registerName((new ItemPortableMagnet("portableMagnet", path+"magnet", Config.Ecap[0])), "Portable Magnet");
        BlockItemRegistry.registerName((new ItemBuilderTexture("builderTexture", path+"builder")), "Building Texture");
        BlockItemRegistry.registerName((new ItemTeleporterCoords("teleporterCoords", path+"teleporter")), "Teleport to");
        short[] dur = Config.data.getShortArray("minerDrill.durability");
        byte[] harvst = Config.data.getByteArray("minerDrill.harvestLvl");
        float[] eff = Config.data.getFloatArray("minerDrill.efficiency");
        BlockItemRegistry.registerName((new ItemMinerDrill("stoneDrill", path+"stoneDrill", dur.length > 0 ? dur[0] : 4096, harvst.length > 0 ? harvst[0] : 1, eff.length > 0 ? eff[0] : 1.5F, ItemMinerDrill.defaultClass)), "Miner Stone-Drill");
        BlockItemRegistry.registerName((new ItemMinerDrill("ironDrill", path+"ironDrill",  dur.length > 1 ? dur[1] : 8192, harvst.length > 1 ? harvst[1] : 2, eff.length > 1 ? eff[1] : 2.0F, ItemMinerDrill.defaultClass)), "Miner Iron-Drill");
        BlockItemRegistry.registerName((new ItemMinerDrill("diamondDrill", path+"diamondDrill",  dur.length > 2 ? dur[2] : 16384, harvst.length > 2 ? harvst[2] : 3, eff.length > 2 ? eff[2] : 4.0F, ItemMinerDrill.defaultClass)), "Miner Diamond-Drill");
        BlockItemRegistry.registerName((new ItemAntimatterLaser("amLaser", path+"amLaser")), "Antimatter Laser");
        BlockItemRegistry.registerName((new ItemMatterCannon("mCannon", path+"mCannon")), "Matter Cannon");
        BlockItemRegistry.registerName((new ItemLiquidAir("contLiquidAir", path+"liquidAir")), "Liquid Air Container");
        BlockItemRegistry.registerName((new ItemAlgaeFood("contAlgaeFood", path+"algaeFood")), "Algae Food Container");
        BlockItemRegistry.registerName((new ItemInvEnergy("contInvEnergy", path+"invEnergy", Config.Ecap[2])), "Inventory Energy Container");
        BlockItemRegistry.registerName((new ItemJetpackFuel("contJetFuel", path+"jetFuel")), "Jetpack Fuel Container");
        BlockItemRegistry.registerName((new ItemJetpack("jetpack", path+"jetpack", 0)), "Jetpack");
        BlockItemRegistry.registerName((new ItemJetpack("jetpackIron", path+"jetpackIron", 1)), "Iron armored Jetpack");
        BlockItemRegistry.registerName((new ItemJetpack("jetpackSteel", path+"jetpackSteel", 2)), "Steel armored Jetpack");
        BlockItemRegistry.registerName((new ItemJetpack("jetpackGraphite", path+"jetpackGraphite", 3)), "Carbon armored Jetpack");
        BlockItemRegistry.registerName((new ItemJetpack("jetpackUnbr", path+"jetpackUnbr", 4)), "Unbreakable armored Jetpack");
        BlockItemRegistry.registerName((new ItemMatterInterface("matterInterface", path+"matterInterface")), "Portable Matter Interface");
        BlockItemRegistry.registerName((new ItemFluidDummy("fluidDummy")), "Stabilized Fluid");
        BlockItemRegistry.registerName((new ItemFluidUpgrade("fluidUpgrade", path+"fluidUpgrade")), "Pipe Liquid-Filter");
        BlockItemRegistry.registerName((new ItemItemUpgrade("itemUpgrade", path+"itemUpgrade")), "Pipe Item-Filter");
        BlockItemRegistry.registerName((new ItemFurnace("portableFurnace", path+"portableFurnace")), "Portable Electric Furnace");
        BlockItemRegistry.registerName((new ItemInventory("portableInventory", path+"portableInventory")), "Portable Item Storage");
        BlockItemRegistry.registerName((new ItemPortableCrafter("portableCrafter", path+"portableCrafter")), "Portable Automatic Crafter");
        BlockItemRegistry.registerName((new ItemPortableGenerator("portableGenerator", path+"portableGenerator")), "Portable Generator");
        BlockItemRegistry.registerName((new ItemRemoteInv("portableRemoteInv", path+"portableRemoteInv")), "Portable Inventory Remote");
        BlockItemRegistry.registerName((new ItemPortableTeleporter("portableTeleporter", path+"portableTeleporter")), "Portable Teleporter");
        BlockItemRegistry.registerName((new ItemPortablePump("portablePump", path+"portablePump")), "Portable Pump");
        BlockItemRegistry.registerName((new ItemTranslocator("translocator", path+"translocator")), "Block Translocator");
        BlockItemRegistry.registerName((new ItemPortableTesla("portableTesla", path+"portableTesla")), "Portable Tesla Transmitter");
        BlockItemRegistry.registerName((new ItemPlacement("placement", path+"placement")), "Block placement controller");
        BlockItemRegistry.registerName(new ItemMachineSynchronizer("synchronizer", path+"synchronizer"), "Machine co-op synchronizer");
        BlockItemRegistry.registerName(new ItemBuilderAirType("remBlockType", path+"remBlockType"), "Remove block pattern");
        BlockItemRegistry.registerName(new ItemVertexSel("vertexSel", path+"vertexSel"), "Vertex Selection Tool");
    }
    
    private void initBlocks()
    {
        Block block;
        block = (new BlockOre("ore")).setHardness(2.0F).setResistance(10F).setStepSound(Block.soundTypeStone);
        block = (new TileBlock("pool", Material.glass, DefaultItemBlock.class, 0x20)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F).setStepSound(Block.soundTypeStone);
        block.setLightOpacity(5);
        BlockItemRegistry.registerName(block, "Algae Pool");
        block = (new BlockUnbreakable("unbrStone", 0x7e)).setStepSound(Block.soundTypeStone);
        BlockItemRegistry.registerName((new GlassUnbreakable("unbrGlass", 0x7f)).setStepSound(Block.soundTypeGlass), "unbreakable Glass");
        TileBlockRegistry.register((TileBlock)(new TileBlock("lightShaft", Material.glass, DefaultItemBlock.class, 2)).setCreativeTab(tabAutomation).setHardness(1.0F).setResistance(10F).setStepSound(Block.soundTypeGlass), LightShaft.class, null, "Light Shaft");
        new BlockSkyLight("light");
        new GhostBlock("placementHelper");
        TileBlockRegistry.register((TileBlock)(new BlockPipe("wireC", Material.iron, DefaultItemBlock.class, 0x20, "pipes/copper")).setCreativeTab(tabAutomation).setHardness(0.5F).setResistance(10F).setStepSound(Block.soundTypeMetal), Wire.class, null, "Copper Wire");
        TileBlockRegistry.register((TileBlock)(new BlockPipe("wireA", Material.iron, DefaultItemBlock.class, 0x20, "pipes/condAlloy")).setCreativeTab(tabAutomation).setHardness(0.5F).setResistance(10F).setStepSound(Block.soundTypeMetal), Wire.class, null, "Conductive Alloy Wire");
        TileBlockRegistry.register((TileBlock)(new BlockPipe("wireH", Material.iron, DefaultItemBlock.class, 0x20, "pipes/hydrogen")).setCreativeTab(tabAutomation).setHardness(0.5F).setResistance(10F).setStepSound(Block.soundTypeMetal), Wire.class, null, "Superconductive Hydrogen Wire");
        TileBlockRegistry.register((TileBlock)(new BlockLiquidPipe("liquidPipe", Material.glass, 0x20)).setHardness(0.5F).setCreativeTab(tabAutomation).setResistance(10F).setStepSound(Block.soundTypeGlass), LiquidPipe.class, null, "Liquid Pipe");
        TileBlockRegistry.register((TileBlock)(new BlockItemPipe("itemPipe", Material.wood, 0x20)).setCreativeTab(tabAutomation).setHardness(0.5F).setResistance(10F).setStepSound(Block.soundTypeWood), ItemPipe.class, null, "Item Pipe");
        TileBlockRegistry.register((TileBlock)(new BlockPipe("itemWarpPipe", Material.iron, DefaultItemBlock.class, 0x20, "pipes/itemWp", "pipes/itemIn", "pipes/itemEx").setCreativeTab(tabAutomation).setHardness(1.0F).setResistance(20F).setStepSound(Block.soundTypeMetal)), ItemWarpPipe.class, null, "Item Warp Pipe");
        TileBlockRegistry.register((TileBlock)(new BlockPipe("liquidWarpPipe", Material.iron, DefaultItemBlock.class, 0x20, "pipes/liquidWp", "pipes/liquidIn", "pipes/liquidEx").setCreativeTab(tabAutomation).setHardness(1.0F).setResistance(20F).setStepSound(Block.soundTypeMetal)), LiquidWarpPipe.class, null, "Liquid Warp Pipe");
        TileBlockRegistry.register((TileBlock)(new TileBlock("voltageTransformer", Material.iron, DefaultItemBlock.class, 6)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F).setStepSound(Block.soundTypeMetal), VoltageTransformer.class, TileContainer.class, "Voltage Transformer");
        TileBlockRegistry.register((TileBlock)(new TileBlock("SCSU", Material.iron, ItemESU.class, 0x41)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F).setStepSound(Block.soundTypeMetal), ESU.class, TileContainer.class, "Single-Cell Storage Unit");
        TileBlockRegistry.register((TileBlock)(new TileBlock("OCSU", Material.iron, ItemESU.class, 0x41)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F).setStepSound(Block.soundTypeMetal), ESU.class, TileContainer.class, "Octa-Cell Storage Unit");
        TileBlockRegistry.register((TileBlock)(new TileBlock("CCSU", Material.iron, ItemESU.class, 0x41)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F).setStepSound(Block.soundTypeMetal), ESU.class, TileContainer.class, "Crystall-Cell Storage Unit");
        TileBlockRegistry.register((TileBlock)(new TileBlock("steamEngine", Material.iron, DefaultItemBlock.class, 2)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F).setStepSound(Block.soundTypeStone), SteamEngine.class, TileContainer.class, "Steam Engine");
        TileBlockRegistry.register((TileBlock)(new TileBlock("steamTurbine", Material.iron, DefaultItemBlock.class, 1)).setCreativeTab(tabAutomation).setHardness(2.5F).setResistance(10F).setStepSound(Block.soundTypeMetal), SteamTurbine.class, TileContainer.class, "Steam Turbine");
        TileBlockRegistry.register((TileBlock)(new TileBlock("steamGenerator", Material.iron, DefaultItemBlock.class, 2)).setCreativeTab(tabAutomation).setHardness(2.0F).setResistance(10F).setStepSound(Block.soundTypeMetal), SteamGenerator.class, TileContainer.class, "Steam Generator");
        TileBlockRegistry.register((TileBlock)(new TileBlock("steamBoiler", Material.iron, DefaultItemBlock.class, 3)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F).setStepSound(Block.soundTypeStone), SteamBoiler.class, TileContainer.class, "Steam Boiler");
        TileBlockRegistry.register((TileBlock)(new TileBlock("lavaCooler", Material.rock, DefaultItemBlock.class, 2)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F).setStepSound(Block.soundTypeStone), LavaCooler.class, TileContainer.class, "Geothermal Heat Exchanger");
        TileBlockRegistry.register((TileBlock)(new TileBlock("energyFurnace", Material.iron, DefaultItemBlock.class, 3)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F).setStepSound(Block.soundTypeStone), EnergyFurnace.class, TileContainer.class, "Electric Heated Furnace");
        block = (new TileBlock("solarpanel", Material.glass, DefaultItemBlock.class, 0x20)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F).setStepSound(Block.soundTypeGlass);
        block.setBlockBounds(0F, 0F, 0F, 1F, 0.125F, 1F);
        TileBlockRegistry.register((TileBlock)block, Solarpanel.class, null, "Solarpanel");
        TileBlockRegistry.register((TileBlock)(new TileBlock("farm", Material.iron, DefaultItemBlock.class, 2)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F).setStepSound(Block.soundTypeStone), Farm.class, TileContainer.class, "Automatic Farm");
        TileBlockRegistry.register((TileBlock)(new TileBlock("miner", Material.iron, DefaultItemBlock.class, 0x12)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F).setStepSound(Block.soundTypeStone), Miner.class, TileContainer.class, "Automatic Miner");
        TileBlockRegistry.register((TileBlock)(new TileBlock("magnet", Material.iron, DefaultItemBlock.class, 1)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F).setStepSound(Block.soundTypeMetal), Magnet.class, null, "Magnet");
        TileBlockRegistry.register((TileBlock)(new TileBlock("link", Material.iron, DefaultItemBlock.class, 5)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F).setStepSound(Block.soundTypeMetal), ELink.class, TileContainer.class, "Energy Link");
        TileBlockRegistry.register((TileBlock)(new TileBlock("linkHV", Material.iron, DefaultItemBlock.class, 5)).setCreativeTab(tabAutomation).setHardness(2.0F).setResistance(10F).setStepSound(Block.soundTypeMetal), ELink.class, TileContainer.class, "HV Energy Link");
        TileBlockRegistry.register((TileBlock)(new TileBlock("texMaker", Material.wood, DefaultItemBlock.class, 2)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F).setStepSound(Block.soundTypeWood), TextureMaker.class, TileContainer.class, "Texture drawing Table");
        TileBlockRegistry.register((TileBlock)(new TileBlock("builder", Material.iron, DefaultItemBlock.class, 2)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F).setStepSound(Block.soundTypeMetal), Builder.class, TileContainer.class, "Automatic Builder");
        TileBlockRegistry.register((TileBlock)(new TileBlock("algaePool", Material.glass, DefaultItemBlock.class, 3)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F).setStepSound(Block.soundTypeGlass), AlgaePool.class, TileContainer.class, "Bio Reactor");
        block = (new TileBlock("teslaTransmitterLV", Material.iron, DefaultItemBlock.class, 0x62)).setCreativeTab(tabAutomation).setHardness(2.0F).setResistance(15F).setStepSound(Block.soundTypeMetal);
        block.setBlockBounds(0.1875F, 0F, 0.1875F, 0.8125F, 1F, 0.8125F);
        TileBlockRegistry.register((TileBlock)block, TeslaTransmitterLV.class, TileContainer.class, "LV Tesla Transmitter");
        block = (new TileBlock("teslaTransmitter", Material.iron, DefaultItemBlock.class, 0x62)).setCreativeTab(tabAutomation).setHardness(2.5F).setResistance(20F).setStepSound(Block.soundTypeMetal);
        block.setBlockBounds(0.25F, 0F, 0.25F, 0.75F, 1F, 0.75F);
        TileBlockRegistry.register((TileBlock)block, TeslaTransmitter.class, TileContainer.class, "HV Tesla Transmitter");
        TileBlockRegistry.register((TileBlock)(new TileBlock("teleporter", Material.iron, DefaultItemBlock.class, 2)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F).setStepSound(Block.soundTypeMetal), Teleporter.class, TileContainer.class, "Teleporter");
        TileBlockRegistry.register((TileBlock)(new TileBlock("advancedFurnace", Material.iron, DefaultItemBlock.class, 3)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F).setStepSound(Block.soundTypeMetal), AdvancedFurnace.class, TileContainer.class, "Advanced Furnace");
        TileBlockRegistry.register((TileBlock)(new TileBlock("pump", Material.iron, DefaultItemBlock.class, 2)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F).setStepSound(Block.soundTypeMetal), Pump.class, TileContainer.class, "Automatic Pump");
        TileBlockRegistry.register((TileBlock)(new TileBlock("massstorageChest", Material.wood, DefaultItemBlock.class, 0x43)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(50F).setStepSound(Block.soundTypeWood), MassstorageChest.class, TileContainer.class, "Massstorage Chest");
        TileBlockRegistry.register((TileBlock)(new TileBlock("matterOrb", Material.iron, ItemMatterOrb.class, 0x40)).setCreativeTab(tabAutomation).setHardness(1.0F).setResistance(10F).setStepSound(Block.soundTypeStone), MatterOrb.class, null, "Matter Orb");
        TileBlockRegistry.register((TileBlock)(new TileBlock("antimatterBombE", Material.tnt, ItemMatterOrb.class, 0x42)).setCreativeTab(tabAutomation).setHardness(1.0F).setResistance(10F).setStepSound(Block.soundTypeStone), MatterOrb.class, null, "Raw Antimatter Bomb");
        TileBlockRegistry.register((TileBlock)(new TileBlock("antimatterBombF", Material.tnt, ItemAntimatterTank.class, 0x43)).setCreativeTab(tabAutomation).setHardness(1.0F).setResistance(10F).setStepSound(Block.soundTypeStone), AntimatterBomb.class, null, "Fusable Antimatter Bomb");
        TileBlockRegistry.register((TileBlock)(new TileBlock("antimatterTank", Material.iron, ItemAntimatterTank.class, 0x41)).setCreativeTab(tabAutomation).setHardness(2.5F).setResistance(40F).setStepSound(Block.soundTypeMetal), AntimatterTank.class, TileContainer.class, "Antimatter Tank");
        TileBlockRegistry.register((TileBlock)(new TileBlock("antimatterFabricator", Material.iron, DefaultItemBlock.class, 3)).setCreativeTab(tabAutomation).setHardness(2.5F).setResistance(40F).setStepSound(Block.soundTypeMetal), AntimatterFabricator.class, TileContainer.class, "Antimatter Fabricator");
        TileBlockRegistry.register((TileBlock)(new TileBlock("antimatterAnihilator", Material.iron, DefaultItemBlock.class, 3)).setCreativeTab(tabAutomation).setHardness(2.5F).setResistance(40F).setStepSound(Block.soundTypeMetal), AntimatterAnihilator.class, TileContainer.class, "Antimatter Annihilator");
        TileBlockRegistry.register((TileBlock)(new TileBlock("hpSolarpanel", Material.glass, DefaultItemBlock.class, 2)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F).setStepSound(Block.soundTypeGlass), HPSolarpanel.class, null, "High efficiency Solarpanel");
        TileBlockRegistry.register((TileBlock)(new TileBlock("autoCrafting", Material.wood, DefaultItemBlock.class, 2)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F).setStepSound(Block.soundTypeWood), AutoCrafting.class, TileContainer.class, "Automatic Crafter");
        TileBlockRegistry.register((TileBlock)(new TileBlock("geothermalFurnace", Material.rock, DefaultItemBlock.class, 3)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F).setStepSound(Block.soundTypeStone), GeothermalFurnace.class, TileContainer.class, "Geothermal Furnace");
        TileBlockRegistry.register((TileBlock)(new TileBlock("steamCompressor", Material.iron, DefaultItemBlock.class, 3)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F).setStepSound(Block.soundTypeMetal), SteamCompressor.class, TileContainer.class, "Steam-Pneumatic Compression Assembler");
        TileBlockRegistry.register((TileBlock)(new TileBlock("electricCompressor", Material.iron, DefaultItemBlock.class, 3)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F).setStepSound(Block.soundTypeMetal), ElectricCompressor.class, TileContainer.class, "Electromagnetic Compression Assembler");
        TileBlockRegistry.register((TileBlock)(new TileBlock("tank", Material.glass, ItemTank.class, 0x60)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F).setStepSound(Block.soundTypeStone), Tank.class, TileContainer.class, "Liquid Tank");
        TileBlockRegistry.register((TileBlock)(new TileBlock("security", Material.iron, DefaultItemBlock.class, 0x3)).setCreativeTab(tabAutomation).setBlockUnbreakable().setResistance(1000000F).setStepSound(Block.soundTypeStone), SecuritySys.class, TileContainer.class, "Chunk loading & protection Manager");
        TileBlockRegistry.register((TileBlock)(new TileBlock("decompCooler", Material.iron, DefaultItemBlock.class, 2)).setCreativeTab(tabAutomation).setHardness(2.0F).setResistance(10F).setStepSound(Block.soundTypeMetal), DecompCooler.class, TileContainer.class, "Decompression Cooler");
        TileBlockRegistry.register((TileBlock)(new TileBlock("collector", Material.iron, DefaultItemBlock.class, 5)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F).setStepSound(Block.soundTypeMetal), Collector.class, TileContainer.class, "Fluid Collector");
        TileBlockRegistry.register((TileBlock)(new TileBlock("trash", Material.rock, DefaultItemBlock.class, 2)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F).setStepSound(Block.soundTypeStone), Trash.class, TileContainer.class, "Trash");
        TileBlockRegistry.register((TileBlock)(new TileBlock("electrolyser", Material.iron, DefaultItemBlock.class, 2)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F).setStepSound(Block.soundTypeMetal), Electrolyser.class, TileContainer.class, "Electrolyser");
        TileBlockRegistry.register((TileBlock)(new TileBlock("fuelCell", Material.iron, DefaultItemBlock.class, 2)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F).setStepSound(Block.soundTypeMetal), FuelCell.class, TileContainer.class, "Hydrogen Fuel Cell");
        TileBlockRegistry.register((TileBlock)(new TileBlock("detector", Material.rock, DefaultItemBlock.class, 0x15)).setCreativeTab(tabAutomation).setHardness(0.5F).setResistance(10F).setStepSound(Block.soundTypeStone), Detector.class, TileContainer.class, "Detector");
        TileBlockRegistry.register((TileBlock)(new TileBlock("itemSorter", Material.wood, DefaultItemBlock.class, 0)).setCreativeTab(tabAutomation).setHardness(0.5F).setResistance(10F).setStepSound(Block.soundTypeWood), ItemSorter.class, TileContainer.class, "Item Sorter");
        block = new TileBlock("wormhole", Material.portal, ItemInterdimHole.class, 0x60).setCreativeTab(tabAutomation).setHardness(3.0F).setResistance(100F).setStepSound(Block.soundTypeGlass).setBlockTextureName("portal");
        block.setBlockBounds(0.0625F, 0.0625F, 0.0625F, 0.9375F, 0.9375F, 0.9375F);
        TileBlockRegistry.register((TileBlock)block, InterdimHole.class, null, "Interdimensional Wormhole");
        TileBlockRegistry.register((TileBlock)(new TileBlock("matterInterfaceB", Material.iron, DefaultItemBlock.class, 2)).setCreativeTab(tabAutomation).setHardness(2.0F).setResistance(10F).setStepSound(Block.soundTypeMetal), MatterInterface.class, TileContainer.class, "Matter Interface");
        TileBlockRegistry.register((TileBlock)(new TileBlock("fluidPacker", Material.iron, DefaultItemBlock.class, 3)).setCreativeTab(tabAutomation).setHardness(2.0F).setResistance(10F).setStepSound(Block.soundTypeMetal), FluidPacker.class, TileContainer.class, "Fluid Stabilizer");
        TileBlockRegistry.register((TileBlock)(new TileBlock("hugeTank", Material.glass, ItemHugeTank.class, 0x60)).setCreativeTab(tabAutomation).setHardness(2.0F).setResistance(15F).setStepSound(Block.soundTypeGlass), HugeTank.class, TileContainer.class, "Massstorage Tank");
        TileBlockRegistry.register((TileBlock)(new TileBlock("fluidVent", Material.iron, DefaultItemBlock.class, 5)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F).setStepSound(Block.soundTypeMetal), FluidVent.class, TileContainer.class, "Fluid Exhaust");
        TileBlockRegistry.register((TileBlock)(new TileBlock("gravCond", Material.iron, DefaultItemBlock.class, 0x10)).setCreativeTab(tabAutomation).setHardness(2.5F).setResistance(20F).setStepSound(Block.soundTypeMetal), GraviCond.class, TileContainer.class, "Gravitational Condenser");
        TileBlockRegistry.register((TileBlock)(new TileBlock("itemBuffer", Material.wood, DefaultItemBlock.class, 0)).setCreativeTab(tabAutomation).setHardness(0.5F).setResistance(10F).setStepSound(Block.soundTypeWood), ItemBuffer.class, TileContainer.class, "Item Flow Regulator");
        TileBlockRegistry.register((TileBlock)(new TileBlock("quantumTank", Material.glass, ItemQuantumTank.class, 0x60)).setCreativeTab(tabAutomation).setHardness(2.5F).setResistance(20F).setStepSound(Block.soundTypeGlass), QuantumTank.class, TileContainer.class, "Quantum Tank");
        TileBlockRegistry.register((TileBlock)(new TileBlock("vertShemGen", Material.water, DefaultItemBlock.class, 7)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F).setStepSound(Block.soundTypeWood), VertexShematicGen.class, TileContainer.class, "3D-Vertex Schematic Printer");
        proxy.registerBlocks();
    }
    
    private void initLiquids()
    {
        L_steam = registerFluid(new ModFluid("steam", "Steam").setDensity(0).setGaseous(true).setTemperature(523).setViscosity(10), "lSteam");
        L_biomass = registerFluid(new ModFluid("biomass", "Biomass").setTemperature(310).setViscosity(1500), "lBiomass");
        L_antimatter = registerFluid(new ModFluid("antimatter", "Antimatter").setDensity(-1000).setGaseous(true).setTemperature(10000000).setViscosity(1), "lAntimatter");
        L_nitrogenG = registerFluid(new ModFluid("nitrogenG", "Gaseous Nitrogen").setDensity(0).setGaseous(true).setTemperature(273).setViscosity(10), "lNitrogenG");
        L_nitrogenL = registerFluid(new ModFluid("nitrogenL", "Liquid Nitrogen").setDensity(800).setTemperature(77), "lNitrogenL");
        L_heliumG = registerFluid(new ModFluid("heliumG", "Gaseous Helium").setDensity(-1).setGaseous(true).setTemperature(273).setViscosity(5), "lHeliumG");
        L_heliumL = registerFluid(new ModFluid("heliumL", "Liquid Helium").setDensity(144).setTemperature(4).setViscosity(1), "lHeliumL");
        L_hydrogenG = registerFluid(new ModFluid("hydrogenG", "Gaseous Hydrogen").setDensity(-1).setGaseous(true).setTemperature(273).setViscosity(5), "lHydrogenG");
        L_hydrogenL = registerFluid(new ModFluid("hydrogenL", "Liquid Hydrogen").setDensity(72).setTemperature(21).setViscosity(500), "lHydrogenL");
        L_oxygenG = registerFluid(new ModFluid("oxygenG", "Gaseous Oxygen").setDensity(0).setGaseous(true).setTemperature(273).setViscosity(10), "lOxygenG");
        L_oxygenL = registerFluid(new ModFluid("oxygenL", "Liquid Oxygen").setDensity(1160).setTemperature(90).setViscosity(800), "lOxygenL");
        L_water = FluidRegistry.WATER;
        L_lava = FluidRegistry.LAVA;
        
        BlockSuperfluid.reactConversions.put(L_biomass, L_steam);
        BlockSuperfluid.reactConversions.put(L_nitrogenL, L_nitrogenG);
        BlockSuperfluid.reactConversions.put(L_heliumL, L_heliumG);
        BlockSuperfluid.reactConversions.put(L_hydrogenL, L_hydrogenG);
        BlockSuperfluid.reactConversions.put(L_oxygenL, L_oxygenG);
        BlockSuperfluid.effects.put(L_oxygenG, new PotionEffect[]{new PotionEffect(Potion.moveSpeed.id, 5), new PotionEffect(Potion.digSpeed.id, 5), new PotionEffect(Potion.damageBoost.id, 5)});
        BlockSuperfluid.effects.put(L_nitrogenG, new PotionEffect[]{new PotionEffect(Potion.moveSlowdown.id, 5), new PotionEffect(Potion.digSlowdown.id, 5), new PotionEffect(Potion.weakness.id, 5)});
        BlockSuperfluid.effects.put(L_heliumG, new PotionEffect[]{new PotionEffect(Potion.fireResistance.id, 5), new PotionEffect(Potion.moveSlowdown.id, 5), new PotionEffect(Potion.digSlowdown.id, 5)});
        BlockSuperfluid.effects.put(L_hydrogenG, new PotionEffect[]{new PotionEffect(Potion.confusion.id, 5), new PotionEffect(Potion.moveSlowdown.id, 5), new PotionEffect(Potion.digSlowdown.id, 5)});
        BlockSuperfluid.effects.put(L_antimatter, new PotionEffect[]{new PotionEffect(Potion.wither.id, 50), new PotionEffect(Potion.blindness.id, 50), new PotionEffect(Potion.hunger.id, 50)});
        if (L_hydrogenG.canBePlacedInWorld()) Blocks.fire.setFireInfo(L_hydrogenG.getBlock(), 500, 20);
    }
    
    private Fluid registerFluid(Fluid fluid, String blockId)
    {
        Fluid ret = FluidRegistry.getFluid(fluid.getName());
        if (ret == null) {
            FluidRegistry.registerFluid(fluid);
            fluid.setBlock(new BlockSuperfluid(blockId, (ModFluid)fluid));
            return fluid;
        } else return ret;
    }
    
    private WorldGenerator copperGen;
    private WorldGenerator silverGen;
    
    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider) 
    {
        int x = chunkX << 4;
        int z = chunkZ << 4;
        if (silverGen != null) this.genOreDensed(4, world, silverGen, random, x, z, 5, 40);
        if (copperGen != null) this.genOreDensed(8, world, copperGen, random, x, z, 48, 5);
    }
    
    private void genOreHomogen(int n, World world, WorldGenerator gen, Random rand, int cx, int cz, int min, int max)
    {
        if (max < min) {
            int a = min; min = max; max = a;
        }
        for (int i = 0; i < n; i++)
        {
            int x = cx + rand.nextInt(16);
            int y = rand.nextInt(max - min) + min;
            int z = cz + rand.nextInt(16);
            gen.generate(world, rand, x, y, z);
        }
    }
    
    private void genOreDensed(int n, World world, WorldGenerator gen, Random rand, int cx, int cz, int y0, int y1)
    {
        for (int i = 0; i < n; i++)
        {
            float offset = rand.nextFloat();
            int x = cx + rand.nextInt(16);
            int y = y0 + (int)((float)(y1 - y0) * offset * offset);
            int z = cz + rand.nextInt(16);
            gen.generate(world, rand, x, y, z);
        }
    }
    
    public static FluidStack getLiquid(Fluid type, int amount)
    {
        if (type == null) return null;
        FluidStack liquid = new FluidStack(type, amount);
        liquid.amount = amount;
        return liquid;
    }
   
}
