package cd4017be.automation;

import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.IWorldGenerator;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.util.ArrayList;
import java.util.Random;

import org.apache.logging.log4j.Level;

import cd4017be.api.automation.AreaProtect;
import cd4017be.api.computers.ComputerAPI;
import cd4017be.api.recipes.AutomationRecipes;
import cd4017be.api.recipes.RecipeAPI;
import cd4017be.automation.Block.BlockOre.Ore;
import cd4017be.automation.Entity.EntityAntimatterExplosion1;
import cd4017be.automation.jetpack.JetPackConfig;
import cd4017be.automation.jetpack.PacketHandler;
import cd4017be.lib.BlockGuiHandler;
import cd4017be.lib.BlockItemRegistry;
import cd4017be.lib.ConfigurationFile;
import cd4017be.lib.DefaultItemBlock;
import cd4017be.lib.ModFluid;
import cd4017be.lib.TileBlock;
import cd4017be.lib.templates.BlockPipe;
import cd4017be.lib.templates.BlockSuperfluid;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
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
import cd4017be.automation.Item.*;
import cd4017be.automation.Block.*;
import static cd4017be.automation.Objects.*;
import static cd4017be.lib.BlockItemRegistry.stack;

/**
 *
 * @author CD4017BE
 */
@Mod(modid="Automation", name="Inductive Automation", version="4.1.0")
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
    private static final String recipeFile = "inductiveAutomation.rcp";
    
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) 
    {
    	Config.loadConfig(ConfigurationFile.init(event, "inductiveAutomation.cfg", "/assets/automation/config/preset.cfg"));
    	ConfigurationFile.init(event, recipeFile, "/assets/automation/config/recipes.rcp");
    	BlockItemRegistry.setMod("automation");
        tabAutomation = new CreativeTabAutomation("automation");
        tabFluids = new CreativeTabFluids("fluids");
        initFluids();
        initItems();
        initBlocks();
        initOres();
        RecipeAPI.loadRecipes(recipeFile, RecipeAPI.PRE_INIT);
        if (event.getSide().isClient()) JetPackConfig.loadData();
    }
    
    @Mod.EventHandler
    public void load(FMLInitializationEvent event) 
    {
    	BlockItemRegistry.setMod("automation");
    	BlockGuiHandler.registerMod(this);
    	PacketHandler.register();
        AreaProtect.register(this);
    	GameRegistry.registerWorldGenerator(this, 0);
        
        FluidContainerRegistry.registerFluidContainer(new FluidContainerData(new FluidStack(L_biomass, FluidContainerRegistry.BUCKET_VOLUME), stack("LCBiomass", 1), FluidContainerRegistry.EMPTY_BOTTLE));
        FluidContainerRegistry.registerFluidContainer(new FluidContainerData(new FluidStack(L_nitrogenL, 100), stack("LCNitrogen", 1), FluidContainerRegistry.EMPTY_BOTTLE));
        FluidContainerRegistry.registerFluidContainer(new FluidContainerData(new FluidStack(L_hydrogenL, 100), stack("LCHydrogen", 1), FluidContainerRegistry.EMPTY_BOTTLE));
        FluidContainerRegistry.registerFluidContainer(new FluidContainerData(new FluidStack(L_heliumL, 100), stack("LCHelium", 1), FluidContainerRegistry.EMPTY_BOTTLE));
        FluidContainerRegistry.registerFluidContainer(new FluidContainerData(new FluidStack(L_oxygenL, 100), stack("LCOxygen", 1), FluidContainerRegistry.EMPTY_BOTTLE));
        
        EntityRegistry.registerModEntity(EntityAntimatterExplosion1.class, "AntimatterExplosion", 0, this, 32, 1, false);
        System.out.println("Automation: Set Explosion-Resistance of Bedrock to: " + Block.getBlockFromName("bedrock").setResistance(2000000.0F).getExplosionResistance(null));
        
        proxy.registerRenderers();
        
        proxy.registerBioFuels();
        RecipeAPI.loadRecipes(recipeFile, RecipeAPI.INIT);
    }
    
    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
    	ComputerAPI.register();
    	for (String s : Config.data.getStringArray("rcp.Pulverize.oreIn")) AutomationRecipes.addItemCrushingRecipes(s);
    	this.cleanConfig();
    	
    	byte gen = Config.data.getByte("oreGen.Copper", (byte)0);
        if (gen == -1 || (gen == 0 && OreDictionary.getOres("oreCopper").size() > 1)) {
            FMLLog.log("Automation", Level.INFO, "Copper ore world generation disabled. " + (gen == 0 ? "(provided by other mods)" : "(turned off in config)"));
            copperGen = null;
        }
        gen = Config.data.getByte("oreGen.Silver", (byte)0);
        if (gen == -1 || (gen == 0 && OreDictionary.getOres("oreSilver").size() > 1)) {
            FMLLog.log("Automation", Level.INFO, "Silver ore world generation disabled. " + (gen == 0 ? "(provided by other mods)" : "(turned off in config)"));
            silverGen = null;
        }
        RecipeAPI.loadRecipes(recipeFile, RecipeAPI.POST_INIT);
    }
    
    private void initItems()
    {
        material = new ItemMaterial("material");
        selectionTool = new ItemSelectionTool("selectionTool");
        voltMeter = new ItemVoltMeter("voltMeter");
        energyCell = new ItemEnergyCell("energyCell", Config.Ecap[0]);
        chisle = new ItemEnergyTool("chisle", Config.Ecap[0], Config.data.getInt("Tool.Chisle.Euse", 25), Config.data.getFloat("Tool.Chisle.digSpeed", 16F), 4).setToolClass(new String[]{"pickaxe", "shovel"}, Config.data.getShort("item.chisle.harvestLvl", (short)3));
        cutter = new ItemCutter("cutter", Config.Ecap[0], Config.data.getInt("Tool.Cutter.Euse", 25), 7);
        portableMagnet = new ItemPortableMagnet("portableMagnet", Config.Ecap[0]);
        builderTexture = new ItemBuilderTexture("builderTexture");
        teleporterCoords = new ItemTeleporterCoords("teleporterCoords");
        short[] dur = Config.data.getShortArray("minerDrill.durability");
        byte[] harvst = Config.data.getByteArray("minerDrill.harvestLvl");
        float[] eff = Config.data.getFloatArray("minerDrill.efficiency");
        stoneDrill = new ItemMinerDrill("stoneDrill", dur.length > 0 ? dur[0] : 4096, harvst.length > 0 ? harvst[0] : 1, eff.length > 0 ? eff[0] : 1.5F, ItemMinerDrill.defaultClass);
        ironDrill = new ItemMinerDrill("ironDrill", dur.length > 1 ? dur[1] : 8192, harvst.length > 1 ? harvst[1] : 2, eff.length > 1 ? eff[1] : 2.0F, ItemMinerDrill.defaultClass);
        diamondDrill = new ItemMinerDrill("diamondDrill", dur.length > 2 ? dur[2] : 16384, harvst.length > 2 ? harvst[2] : 3, eff.length > 2 ? eff[2] : 4.0F, ItemMinerDrill.defaultClass);
        amLaser = new ItemAntimatterLaser("amLaser");
        mCannon = new ItemMatterCannon("mCannon");
        contLiquidAir = new ItemLiquidAir("contLiquidAir");
        contAlgaeFood = new ItemAlgaeFood("contAlgaeFood");
        contInvEnergy = new ItemInvEnergy("contInvEnergy", Config.Ecap[2]);
        contJetFuel = new ItemJetpackFuel("contJetFuel");
        jetpack = new ItemJetpack("jetpack", 0);
        jetpackIron = new ItemJetpack("jetpackIron", 1);
        jetpackSteel = new ItemJetpack("jetpackSteel", 2);
        jetpackGraphite = new ItemJetpack("jetpackGraphite", 3);
        jetpackUnbr = new ItemJetpack("jetpackUnbr", 4);
        matterInterface = new ItemMatterInterface("matterInterface");
        fluidDummy = new ItemFluidDummy("fluidDummy");
        fluidUpgrade = new ItemFluidUpgrade("fluidUpgrade");
        itemUpgrade = new ItemItemUpgrade("itemUpgrade");
        portableFurnace = new ItemFurnace("portableFurnace");
        portableInventory = new ItemInventory("portableInventory");
        portableCrafter = new ItemPortableCrafter("portableCrafter");
        portableGenerator = new ItemPortableGenerator("portableGenerator");
        portableRemoteInv = new ItemRemoteInv("portableRemoteInv");
        portableTeleporter = new ItemPortableTeleporter("portableTeleporter");
        portablePump = new ItemPortablePump("portablePump");
        translocator = new ItemTranslocator("translocator");
        portableTesla = new ItemPortableTesla("portableTesla");
        placement = new ItemPlacement("placement");
        synchronizer = new ItemMachineSynchronizer("synchronizer");
        remBlockType = new ItemBuilderAirType("remBlockType");
        vertexSel = new ItemVertexSel("vertexSel");
    }
    
    private void initBlocks()
    {
    	(lightShaft = TileBlock.create("lightShaft", Material.glass, DefaultItemBlock.class, 0)).setCreativeTab(tabAutomation).setHardness(1.0F).setResistance(10F).setStepSound(Block.soundTypeGlass);
    	(wireC = new BlockPipe("wireC", Material.iron, DefaultItemBlock.class, 0x20)).setCreativeTab(tabAutomation).setHardness(0.5F).setResistance(10F).setStepSound(Block.soundTypeMetal);
    	(wireA = new BlockPipe("wireA", Material.iron, DefaultItemBlock.class, 0x20)).setCreativeTab(tabAutomation).setHardness(0.5F).setResistance(10F).setStepSound(Block.soundTypeMetal);
    	(wireH = new BlockPipe("wireH", Material.iron, DefaultItemBlock.class, 0x20)).setCreativeTab(tabAutomation).setHardness(0.5F).setResistance(10F).setStepSound(Block.soundTypeMetal);
    	(liquidPipe = new BlockLiquidPipe("liquidPipe", Material.glass, 0x20)).setHardness(0.5F).setCreativeTab(tabAutomation).setResistance(10F).setStepSound(Block.soundTypeGlass);
    	(itemPipe = new BlockItemPipe("itemPipe", Material.wood, 0x20)).setCreativeTab(tabAutomation).setHardness(0.5F).setResistance(10F).setStepSound(Block.soundTypeWood);
    	(warpPipe = new BlockPipe("warpPipe", Material.iron, DefaultItemBlock.class, 0x20)).setCreativeTab(tabAutomation).setHardness(1.0F).setResistance(20F).setStepSound(Block.soundTypeMetal);
    	(voltageTransformer = TileBlock.create("voltageTransformer", Material.iron, DefaultItemBlock.class, 2)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F).setStepSound(Block.soundTypeMetal);
    	(SCSU = TileBlock.create("SCSU", Material.iron, ItemESU.class, 0x40)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F).setStepSound(Block.soundTypeMetal);
    	(OCSU = TileBlock.create("OCSU", Material.iron, ItemESU.class, 0x40)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F).setStepSound(Block.soundTypeMetal);
    	(CCSU = TileBlock.create("CCSU", Material.iron, ItemESU.class, 0x40)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F).setStepSound(Block.soundTypeMetal);
    	(steamEngine = TileBlock.create("steamEngine", Material.iron, DefaultItemBlock.class, 0)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F).setStepSound(Block.soundTypeStone);
    	(steamTurbine = TileBlock.create("steamTurbine", Material.iron, DefaultItemBlock.class, 0)).setCreativeTab(tabAutomation).setHardness(2.5F).setResistance(10F).setStepSound(Block.soundTypeMetal);
    	(steamGenerator = TileBlock.create("steamGenerator", Material.iron, DefaultItemBlock.class, 0)).setCreativeTab(tabAutomation).setHardness(2.0F).setResistance(10F).setStepSound(Block.soundTypeMetal);
    	(steamBoiler = TileBlock.create("steamBoiler", Material.iron, DefaultItemBlock.class, 1)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F).setStepSound(Block.soundTypeStone);
    	(lavaCooler = TileBlock.create("lavaCooler", Material.rock, DefaultItemBlock.class, 0)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F).setStepSound(Block.soundTypeStone);
    	(energyFurnace = TileBlock.create("energyFurnace", Material.iron, DefaultItemBlock.class, 1)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F).setStepSound(Block.soundTypeStone);
    	(farm = TileBlock.create("farm", Material.iron, DefaultItemBlock.class, 0)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F).setStepSound(Block.soundTypeStone);
    	(miner = TileBlock.create("miner", Material.iron, DefaultItemBlock.class, 0x10)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F).setStepSound(Block.soundTypeStone);
    	(magnet = TileBlock.create("magnet", Material.iron, DefaultItemBlock.class, 0)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F).setStepSound(Block.soundTypeMetal);
    	(link = TileBlock.create("link", Material.iron, DefaultItemBlock.class, 2)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F).setStepSound(Block.soundTypeMetal);
    	(linkHV = TileBlock.create("linkHV", Material.iron, DefaultItemBlock.class, 2)).setCreativeTab(tabAutomation).setHardness(2.0F).setResistance(10F).setStepSound(Block.soundTypeMetal);
    	(texMaker = TileBlock.create("texMaker", Material.wood, DefaultItemBlock.class, 0)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F).setStepSound(Block.soundTypeWood);
    	(builder = TileBlock.create("builder", Material.iron, DefaultItemBlock.class, 0)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F).setStepSound(Block.soundTypeMetal);
    	(algaePool = TileBlock.create("algaePool", Material.glass, DefaultItemBlock.class, 1)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F).setStepSound(Block.soundTypeGlass);
    	(teleporter = TileBlock.create("teleporter", Material.iron, DefaultItemBlock.class, 0)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F).setStepSound(Block.soundTypeMetal);
    	(advancedFurnace = TileBlock.create("advancedFurnace", Material.iron, DefaultItemBlock.class, 1)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F).setStepSound(Block.soundTypeMetal);
    	(pump = TileBlock.create("pump", Material.iron, DefaultItemBlock.class, 0)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F).setStepSound(Block.soundTypeMetal);
    	(massstorageChest = TileBlock.create("massstorageChest", Material.wood, DefaultItemBlock.class, 0x41)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(50F).setStepSound(Block.soundTypeWood);
    	(matterOrb = TileBlock.create("matterOrb", Material.iron, ItemMatterOrb.class, 0x40)).setCreativeTab(tabAutomation).setHardness(1.0F).setResistance(10F).setStepSound(Block.soundTypeStone);
    	(antimatterBombE = TileBlock.create("antimatterBombE", Material.tnt, ItemMatterOrb.class, 0x40)).setCreativeTab(tabAutomation).setHardness(1.0F).setResistance(10F).setStepSound(Block.soundTypeStone);
    	(antimatterBombF = TileBlock.create("antimatterBombF", Material.tnt, ItemAntimatterTank.class, 0x41)).setCreativeTab(tabAutomation).setHardness(1.0F).setResistance(10F).setStepSound(Block.soundTypeStone);
    	(antimatterTank = TileBlock.create("antimatterTank", Material.iron, ItemAntimatterTank.class, 0x40)).setCreativeTab(tabAutomation).setHardness(2.5F).setResistance(40F).setStepSound(Block.soundTypeMetal);
    	(antimatterFabricator = TileBlock.create("antimatterFabricator", Material.iron, DefaultItemBlock.class, 1)).setCreativeTab(tabAutomation).setHardness(2.5F).setResistance(40F).setStepSound(Block.soundTypeMetal);
    	(antimatterAnihilator = TileBlock.create("antimatterAnihilator", Material.iron, DefaultItemBlock.class, 1)).setCreativeTab(tabAutomation).setHardness(2.5F).setResistance(40F).setStepSound(Block.soundTypeMetal);
    	(hpSolarpanel = TileBlock.create("hpSolarpanel", Material.glass, DefaultItemBlock.class, 0)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F).setStepSound(Block.soundTypeGlass);
    	(autoCrafting = TileBlock.create("autoCrafting", Material.wood, DefaultItemBlock.class, 0)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F).setStepSound(Block.soundTypeWood);
    	(geothermalFurnace = TileBlock.create("geothermalFurnace", Material.rock, DefaultItemBlock.class, 1)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F).setStepSound(Block.soundTypeStone);
    	(steamCompressor = TileBlock.create("steamCompressor", Material.iron, DefaultItemBlock.class, 1)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F).setStepSound(Block.soundTypeMetal);
    	(electricCompressor = TileBlock.create("electricCompressor", Material.iron, DefaultItemBlock.class, 1)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F).setStepSound(Block.soundTypeMetal);
    	(tank = TileBlock.create("tank", Material.glass, ItemTank.class, 0x60)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F).setStepSound(Block.soundTypeStone);
    	(security = TileBlock.create("security", Material.iron, DefaultItemBlock.class, 0x1)).setCreativeTab(tabAutomation).setBlockUnbreakable().setResistance(1000000F).setStepSound(Block.soundTypeStone);
    	(decompCooler = TileBlock.create("decompCooler", Material.iron, DefaultItemBlock.class, 0)).setCreativeTab(tabAutomation).setHardness(2.0F).setResistance(10F).setStepSound(Block.soundTypeMetal);
    	(collector = TileBlock.create("collector", Material.iron, DefaultItemBlock.class, 2)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F).setStepSound(Block.soundTypeMetal);
    	(trash = TileBlock.create("trash", Material.rock, DefaultItemBlock.class, 0)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F).setStepSound(Block.soundTypeStone);
    	(electrolyser = TileBlock.create("electrolyser", Material.iron, DefaultItemBlock.class, 0)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F).setStepSound(Block.soundTypeMetal);
    	(fuelCell = TileBlock.create("fuelCell", Material.iron, DefaultItemBlock.class, 0)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F).setStepSound(Block.soundTypeMetal);
    	(detector = TileBlock.create("detector", Material.rock, DefaultItemBlock.class, 0x12)).setCreativeTab(tabAutomation).setHardness(0.5F).setResistance(10F).setStepSound(Block.soundTypeStone);
    	(itemSorter = TileBlock.create("itemSorter", Material.wood, DefaultItemBlock.class, 0)).setCreativeTab(tabAutomation).setHardness(0.5F).setResistance(10F).setStepSound(Block.soundTypeWood);
    	(matterInterfaceB = TileBlock.create("matterInterfaceB", Material.iron, DefaultItemBlock.class, 0)).setCreativeTab(tabAutomation).setHardness(2.0F).setResistance(10F).setStepSound(Block.soundTypeMetal);
    	(fluidPacker = TileBlock.create("fluidPacker", Material.iron, DefaultItemBlock.class, 1)).setCreativeTab(tabAutomation).setHardness(2.0F).setResistance(10F).setStepSound(Block.soundTypeMetal);
    	(hugeTank = TileBlock.create("hugeTank", Material.glass, ItemHugeTank.class, 0x60)).setCreativeTab(tabAutomation).setHardness(2.0F).setResistance(15F).setStepSound(Block.soundTypeGlass);
    	(fluidVent = TileBlock.create("fluidVent", Material.iron, DefaultItemBlock.class, 2)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F).setStepSound(Block.soundTypeMetal);
    	(gravCond = TileBlock.create("gravCond", Material.iron, DefaultItemBlock.class, 0x10)).setCreativeTab(tabAutomation).setHardness(2.5F).setResistance(20F).setStepSound(Block.soundTypeMetal);
    	(itemBuffer = TileBlock.create("itemBuffer", Material.wood, DefaultItemBlock.class, 0)).setCreativeTab(tabAutomation).setHardness(0.5F).setResistance(10F).setStepSound(Block.soundTypeWood);
    	(quantumTank = TileBlock.create("quantumTank", Material.glass, ItemQuantumTank.class, 0x60)).setCreativeTab(tabAutomation).setHardness(2.5F).setResistance(20F).setStepSound(Block.soundTypeGlass);
    	(vertShemGen = TileBlock.create("vertShemGen", Material.rock, DefaultItemBlock.class, 0)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F).setStepSound(Block.soundTypeWood);
    	(heatRadiator = TileBlock.create("heatRadiator", Material.iron, DefaultItemBlock.class, 0)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F).setStepSound(Block.soundTypeMetal);
    	(ore = new BlockOre("ore")).setHardness(2.0F).setResistance(10F).setStepSound(Block.soundTypeStone);
        (pool = TileBlock.create("pool", Material.glass, DefaultItemBlock.class, 0x20)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F).setStepSound(Block.soundTypeStone).setLightOpacity(5);
        (unbrStone = new BlockUnbreakable("unbrStone")).setStepSound(Block.soundTypeStone);
        (unbrGlass = new GlassUnbreakable("unbrGlass")).setStepSound(Block.soundTypeGlass);
        light = new BlockSkyLight("light");
        placementHelper = new GhostBlock("placementHelper");
        (solarpanel = TileBlock.create("solarpanel", Material.glass, DefaultItemBlock.class, 0x20)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F).setStepSound(Block.soundTypeGlass).setBlockBounds(0F, 0F, 0F, 1F, 0.125F, 1F);
        (teslaTransmitterLV = TileBlock.create("teslaTransmitterLV", Material.iron, DefaultItemBlock.class, 0x60)).setCreativeTab(tabAutomation).setHardness(2.0F).setResistance(15F).setStepSound(Block.soundTypeMetal).setBlockBounds(0.1875F, 0F, 0.1875F, 0.8125F, 1F, 0.8125F);
        (teslaTransmitter = TileBlock.create("teslaTransmitter", Material.iron, DefaultItemBlock.class, 0x60)).setCreativeTab(tabAutomation).setHardness(2.5F).setResistance(20F).setStepSound(Block.soundTypeMetal).setBlockBounds(0.25F, 0F, 0.25F, 0.75F, 1F, 0.75F);
        (wormhole = TileBlock.create("wormhole", Material.portal, ItemInterdimHole.class, 0x60)).setCreativeTab(tabAutomation).setHardness(3.0F).setResistance(100F).setStepSound(Block.soundTypeGlass).setBlockBounds(0.0625F, 0.0625F, 0.0625F, 0.9375F, 0.9375F, 0.9375F);
        proxy.registerBlocks();
    }
    
    private void initFluids()
    {
    	String p = "automation:blocks/fluids/";
        L_steam = registerFluid(new ModFluid("steam", p+"steam").setDensity(0).setGaseous(true).setTemperature(523).setViscosity(10));
        L_waterG = registerFluid(new ModFluid("waterG", p+"waterG").setDensity(-1).setGaseous(true).setTemperature(373).setViscosity(10));
        L_biomass = registerFluid(new ModFluid("biomass", p+"biomass").setTemperature(310).setViscosity(1500));
        L_antimatter = registerFluid(new ModFluid("antimatter", p+"antimatter").setDensity(-1000).setGaseous(true).setTemperature(10000000).setViscosity(1));
        L_nitrogenG = registerFluid(new ModFluid("nitrogenG", p+"nitrogenG").setDensity(0).setGaseous(true).setTemperature(273).setViscosity(10));
        L_nitrogenL = registerFluid(new ModFluid("nitrogenL", p+"nitrogenL").setDensity(800).setTemperature(77));
        L_heliumG = registerFluid(new ModFluid("heliumG", p+"heliumG").setDensity(-1).setGaseous(true).setTemperature(273).setViscosity(5));
        L_heliumL = registerFluid(new ModFluid("heliumL", p+"heliumL").setDensity(144).setTemperature(4).setViscosity(1));
        L_hydrogenG = registerFluid(new ModFluid("hydrogenG", p+"hydrogenG").setDensity(-1).setGaseous(true).setTemperature(273).setViscosity(5));
        L_hydrogenL = registerFluid(new ModFluid("hydrogenL", p+"hydrogenL").setDensity(72).setTemperature(21).setViscosity(500));
        L_oxygenG = registerFluid(new ModFluid("oxygenG", p+"oxygenG").setDensity(0).setGaseous(true).setTemperature(273).setViscosity(10));
        L_oxygenL = registerFluid(new ModFluid("oxygenL", p+"oxygenL").setDensity(1160).setTemperature(90).setViscosity(800));
        L_water = FluidRegistry.WATER;
        L_lava = FluidRegistry.LAVA;
        /* TODO reimplement
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
        */
        if (L_hydrogenG.canBePlacedInWorld()) Blocks.fire.setFireInfo(L_hydrogenG.getBlock(), 500, 20);
    }
    
    private void initOres()
    {
        ore.setHarvestLevel("pickaxe", 1, ore.getStateFromMeta(Ore.Copper.ordinal()));
        ore.setHarvestLevel("pickaxe", 2, ore.getStateFromMeta(Ore.Silver.ordinal()));
        copperGen = new WorldGenMinable(ore.getStateFromMeta(Ore.Copper.ordinal()), 9);
        silverGen = new WorldGenMinable(ore.getStateFromMeta(Ore.Silver.ordinal()), 6);
    }
    
    /**
     * clears configuration data that is not used anymore after initialization to improve performance and save RAM
     */
    private void cleanConfig()
    {
    	ArrayList<String> rem = Config.data.getVariables("B.recipe", "AT.rcp", "AF.rcp", "I.rcp");
    	Config.data.removeEntry(rem.toArray(new String[rem.size()]));
    }
    
    private Fluid registerFluid(Fluid fluid)
    {
        Fluid ret = FluidRegistry.getFluid(fluid.getName());
        if (ret == null) {
            FluidRegistry.registerFluid(fluid);
            fluid.setBlock(new BlockSuperfluid(fluid.getName(), (ModFluid)fluid));
            return fluid;
        } else return ret;
    }
    
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
            gen.generate(world, rand, new BlockPos(x, y, z));
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
            gen.generate(world, rand, new BlockPos(x, y, z));
        }
    }
   
}
