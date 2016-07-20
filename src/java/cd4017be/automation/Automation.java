package cd4017be.automation;

import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import java.util.ArrayList;
import org.apache.logging.log4j.Level;

import cd4017be.api.automation.AreaProtect;
import cd4017be.api.computers.ComputerAPI;
import cd4017be.api.recipes.AutomationRecipes;
import cd4017be.api.recipes.RecipeAPI;
import cd4017be.automation.Block.BlockOre.Ore;
import cd4017be.automation.jetpack.JetPackConfig;
import cd4017be.automation.jetpack.PacketHandler;
import cd4017be.lib.BlockGuiHandler;
import cd4017be.lib.BlockItemRegistry;
import cd4017be.lib.ConfigurationFile;
import cd4017be.lib.DefaultBlock;
import cd4017be.lib.DefaultItemBlock;
import cd4017be.lib.ModFluid;
import cd4017be.lib.TileBlock;
import cd4017be.lib.templates.BlockPipe;
import cd4017be.lib.templates.BlockSuperfluid;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.AxisAlignedBB;
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
@Mod(modid="Automation", useMetadata=true)
public class Automation
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
    	Config.loadConfig(ConfigurationFile.init(event, "inductiveAutomation.cfg", "/assets/automation/config/preset.cfg", true));
    	ConfigurationFile.init(event, recipeFile, "/assets/automation/config/recipes.rcp", true);
    	BlockItemRegistry.setMod("automation");
        tabAutomation = new CreativeTabAutomation("automation");
        tabFluids = new CreativeTabFluids("fluids");
        initFluids();
        initItems();
        initBlocks();
        //initOres();
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
    	//GameRegistry.registerWorldGenerator(this, 0);
        
        FluidContainerRegistry.registerFluidContainer(new FluidContainerData(new FluidStack(L_biomass, FluidContainerRegistry.BUCKET_VOLUME), stack("LCBiomass", 1), FluidContainerRegistry.EMPTY_BOTTLE));
        FluidContainerRegistry.registerFluidContainer(new FluidContainerData(new FluidStack(L_nitrogenL, 100), stack("LCNitrogen", 1), FluidContainerRegistry.EMPTY_BOTTLE));
        FluidContainerRegistry.registerFluidContainer(new FluidContainerData(new FluidStack(L_hydrogenL, 100), stack("LCHydrogen", 1), FluidContainerRegistry.EMPTY_BOTTLE));
        FluidContainerRegistry.registerFluidContainer(new FluidContainerData(new FluidStack(L_heliumL, 100), stack("LCHelium", 1), FluidContainerRegistry.EMPTY_BOTTLE));
        FluidContainerRegistry.registerFluidContainer(new FluidContainerData(new FluidStack(L_oxygenL, 100), stack("LCOxygen", 1), FluidContainerRegistry.EMPTY_BOTTLE));
        
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
    	class TIMaterial extends Material {
			public TIMaterial() {
				super(MapColor.STONE);
				this.setRequiresTool();
			}
    	}
    	M_thermIns = new TIMaterial();
    	
    	(heatRadiator = TileBlock.create("heatRadiator", Material.IRON, SoundType.METAL , DefaultItemBlock.class, 0)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F);
    	(lightShaft = TileBlock.create("lightShaft", Material.GLASS, SoundType.GLASS, DefaultItemBlock.class, 0)).setCreativeTab(tabAutomation).setHardness(1.0F).setResistance(10F);
    	(wireC = new BlockPipe("wireC", Material.IRON, SoundType.METAL, DefaultItemBlock.class, 0x20)).setCreativeTab(tabAutomation).setHardness(0.5F).setResistance(10F);
    	(wireA = new BlockPipe("wireA", Material.IRON, SoundType.METAL, DefaultItemBlock.class, 0x20)).setCreativeTab(tabAutomation).setHardness(0.5F).setResistance(10F);
    	(wireH = new BlockPipe("wireH", Material.IRON, SoundType.METAL, DefaultItemBlock.class, 0x20)).setCreativeTab(tabAutomation).setHardness(0.5F).setResistance(10F);
    	(liquidPipe = new BlockLiquidPipe("liquidPipe", SoundType.GLASS, Material.GLASS, 0x20)).setHardness(0.5F).setCreativeTab(tabAutomation).setResistance(10F);
    	(itemPipe = new BlockItemPipe("itemPipe", Material.WOOD, SoundType.WOOD, 0x20)).setCreativeTab(tabAutomation).setHardness(0.5F).setResistance(10F);
    	(warpPipe = new BlockPipe("warpPipe", Material.IRON, SoundType.METAL, DefaultItemBlock.class, 0x20)).setCreativeTab(tabAutomation).setHardness(1.0F).setResistance(20F);
    	(voltageTransformer = TileBlock.create("voltageTransformer", Material.IRON, SoundType.METAL, DefaultItemBlock.class, 2)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F);
    	(SCSU = TileBlock.create("SCSU", Material.IRON, SoundType.METAL, ItemESU.class, 0x40)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F);
    	(OCSU = TileBlock.create("OCSU", Material.IRON, SoundType.METAL, ItemESU.class, 0x40)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F);
    	(CCSU = TileBlock.create("CCSU", Material.IRON, SoundType.METAL, ItemESU.class, 0x40)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F);
    	(steamEngine = TileBlock.create("steamEngine", Material.IRON, SoundType.METAL, DefaultItemBlock.class, 0)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F);
    	(steamTurbine = TileBlock.create("steamTurbine", Material.IRON, SoundType.METAL, DefaultItemBlock.class, 0)).setCreativeTab(tabAutomation).setHardness(2.5F).setResistance(10F);
    	(steamGenerator = TileBlock.create("steamGenerator", Material.IRON, SoundType.METAL, DefaultItemBlock.class, 0)).setCreativeTab(tabAutomation).setHardness(2.0F).setResistance(10F);
    	(steamBoiler = TileBlock.create("steamBoiler", Material.IRON, SoundType.STONE, DefaultItemBlock.class, 1)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F);
    	(lavaCooler = TileBlock.create("lavaCooler", Material.ROCK, SoundType.STONE, DefaultItemBlock.class, 0)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F);
    	(energyFurnace = TileBlock.create("energyFurnace", Material.IRON, SoundType.STONE, DefaultItemBlock.class, 1)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F);
    	(farm = TileBlock.create("farm", Material.IRON, SoundType.STONE, DefaultItemBlock.class, 0)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F);
    	(miner = TileBlock.create("miner", Material.IRON, SoundType.STONE, DefaultItemBlock.class, 0x10)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F);
    	(magnet = TileBlock.create("magnet", Material.IRON, SoundType.METAL, DefaultItemBlock.class, 0)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F);
    	(link = TileBlock.create("link", Material.IRON, SoundType.METAL, DefaultItemBlock.class, 2)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F);
    	(linkHV = TileBlock.create("linkHV", Material.IRON, SoundType.METAL, DefaultItemBlock.class, 2)).setCreativeTab(tabAutomation).setHardness(2.0F).setResistance(10F);
    	(texMaker = TileBlock.create("texMaker", Material.WOOD, SoundType.WOOD, DefaultItemBlock.class, 0)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F);
    	(builder = TileBlock.create("builder", Material.IRON, SoundType.METAL, DefaultItemBlock.class, 0)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F);
    	(algaePool = TileBlock.create("algaePool", Material.GLASS, SoundType.GLASS, DefaultItemBlock.class, 1)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F);
    	(teleporter = TileBlock.create("teleporter", Material.IRON, SoundType.METAL, DefaultItemBlock.class, 0)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F);
    	(advancedFurnace = TileBlock.create("advancedFurnace", Material.IRON, SoundType.METAL, DefaultItemBlock.class, 1)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F);
    	(pump = TileBlock.create("pump", Material.IRON, SoundType.METAL, DefaultItemBlock.class, 0)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F);
    	(massstorageChest = TileBlock.create("massstorageChest", Material.WOOD, SoundType.WOOD, DefaultItemBlock.class, 0x41)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(50F);
    	(matterOrb = TileBlock.create("matterOrb", Material.IRON, SoundType.STONE, ItemMatterOrb.class, 0x40)).setCreativeTab(tabAutomation).setHardness(1.0F).setResistance(10F);
    	(antimatterBombE = TileBlock.create("antimatterBombE", Material.TNT, SoundType.STONE, ItemMatterOrb.class, 0x40)).setCreativeTab(tabAutomation).setHardness(1.0F).setResistance(10F);
    	(antimatterBombF = TileBlock.create("antimatterBombF", Material.TNT, SoundType.STONE, ItemAntimatterTank.class, 0x41)).setCreativeTab(tabAutomation).setHardness(1.0F).setResistance(10F);
    	(antimatterTank = TileBlock.create("antimatterTank", Material.IRON, SoundType.METAL, ItemAntimatterTank.class, 0x40)).setCreativeTab(tabAutomation).setHardness(2.5F).setResistance(40F);
    	(antimatterFabricator = TileBlock.create("antimatterFabricator", Material.IRON, SoundType.METAL, DefaultItemBlock.class, 1)).setCreativeTab(tabAutomation).setHardness(2.5F).setResistance(40F);
    	(antimatterAnihilator = TileBlock.create("antimatterAnihilator", Material.IRON, SoundType.METAL, DefaultItemBlock.class, 1)).setCreativeTab(tabAutomation).setHardness(2.5F).setResistance(40F);
    	(hpSolarpanel = TileBlock.create("hpSolarpanel", Material.GLASS, SoundType.GLASS, DefaultItemBlock.class, 0)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F);
    	(autoCrafting = TileBlock.create("autoCrafting", Material.WOOD, SoundType.WOOD, DefaultItemBlock.class, 0)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F);
    	(geothermalFurnace = TileBlock.create("geothermalFurnace", Material.ROCK, SoundType.STONE, DefaultItemBlock.class, 1)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F);
    	(steamCompressor = TileBlock.create("steamCompressor", Material.IRON, SoundType.METAL, DefaultItemBlock.class, 1)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F);
    	(electricCompressor = TileBlock.create("electricCompressor", Material.IRON, SoundType.METAL, DefaultItemBlock.class, 1)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F);
    	(tank = TileBlock.create("tank", Material.GLASS, SoundType.GLASS, ItemTank.class, 0x60)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F);
    	(security = TileBlock.create("security", Material.IRON, SoundType.METAL, DefaultItemBlock.class, 0x1)).setCreativeTab(tabAutomation).setBlockUnbreakable().setResistance(1000000F);
    	(decompCooler = TileBlock.create("decompCooler", Material.IRON, SoundType.METAL, DefaultItemBlock.class, 0)).setCreativeTab(tabAutomation).setHardness(2.0F).setResistance(10F);
    	(collector = TileBlock.create("collector", Material.IRON, SoundType.METAL, DefaultItemBlock.class, 2)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F);
    	(trash = TileBlock.create("trash", Material.ROCK, SoundType.STONE, DefaultItemBlock.class, 0)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F);
    	(electrolyser = TileBlock.create("electrolyser", Material.IRON, SoundType.METAL, DefaultItemBlock.class, 0)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F);
    	(fuelCell = TileBlock.create("fuelCell", Material.IRON, SoundType.METAL, DefaultItemBlock.class, 0)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F);
    	(detector = TileBlock.create("detector", Material.ROCK, SoundType.STONE, DefaultItemBlock.class, 0x12)).setCreativeTab(tabAutomation).setHardness(0.5F).setResistance(10F);
    	(itemSorter = TileBlock.create("itemSorter", Material.WOOD, SoundType.WOOD, DefaultItemBlock.class, 0)).setCreativeTab(tabAutomation).setHardness(0.5F).setResistance(10F);
    	(matterInterfaceB = TileBlock.create("matterInterfaceB", Material.IRON, SoundType.METAL, DefaultItemBlock.class, 0)).setCreativeTab(tabAutomation).setHardness(2.0F).setResistance(10F);
    	(fluidPacker = TileBlock.create("fluidPacker", Material.IRON, SoundType.METAL, DefaultItemBlock.class, 1)).setCreativeTab(tabAutomation).setHardness(2.0F).setResistance(10F);
    	(hugeTank = TileBlock.create("hugeTank", Material.GLASS, SoundType.GLASS, ItemHugeTank.class, 0x60)).setCreativeTab(tabAutomation).setHardness(2.0F).setResistance(15F);
    	(fluidVent = TileBlock.create("fluidVent", Material.IRON, SoundType.METAL, DefaultItemBlock.class, 2)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F);
    	(gravCond = TileBlock.create("gravCond", Material.IRON, SoundType.METAL, DefaultItemBlock.class, 0x10)).setCreativeTab(tabAutomation).setHardness(2.5F).setResistance(20F);
    	(itemBuffer = TileBlock.create("itemBuffer", Material.WOOD, SoundType.WOOD, DefaultItemBlock.class, 0)).setCreativeTab(tabAutomation).setHardness(0.5F).setResistance(10F);
    	(quantumTank = TileBlock.create("quantumTank", Material.GLASS, SoundType.GLASS, ItemQuantumTank.class, 0x60)).setCreativeTab(tabAutomation).setHardness(2.5F).setResistance(20F);
    	(vertShemGen = TileBlock.create("vertShemGen", Material.ROCK, SoundType.WOOD, DefaultItemBlock.class, 0)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F);
    	(ore = new BlockOre("ore")).setHardness(2.0F).setResistance(10F) ;
        (pool = TileBlock.create("pool", Material.GLASS, SoundType.STONE, DefaultItemBlock.class, 0x20)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F).setLightOpacity(5);
        unbrStone = new BlockUnbreakable("unbrStone");
        unbrGlass = new GlassUnbreakable("unbrGlass");
        light = new BlockSkyLight("light");
        placementHelper = new GhostBlock("placementHelper");
        (solarpanel = TileBlock.create("solarpanel", Material.GLASS, SoundType.GLASS, DefaultItemBlock.class, 0x20)).setBlockBounds(new AxisAlignedBB(0, 0, 0, 1, 0.125, 1)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F);
        (teslaTransmitterLV = TileBlock.create("teslaTransmitterLV", Material.IRON, SoundType.METAL, DefaultItemBlock.class, 0x60)).setBlockBounds(new AxisAlignedBB(0.1875, 0, 0.1875, 0.8125, 1, 0.8125)).setCreativeTab(tabAutomation).setHardness(2.0F).setResistance(15F);
        (teslaTransmitter = TileBlock.create("teslaTransmitter", Material.IRON, SoundType.METAL, DefaultItemBlock.class, 0x60)).setBlockBounds(new AxisAlignedBB(0.25, 0, 0.25, 0.75, 1, 0.75)).setCreativeTab(tabAutomation).setHardness(2.5F).setResistance(20F);
        (wormhole = TileBlock.create("wormhole", Material.PORTAL, SoundType.GLASS, ItemInterdimHole.class, 0x60)).setBlockBounds(new AxisAlignedBB(0.0625, 0.0625, 0.0625, 0.9375, 0.9375, 0.9375)).setCreativeTab(tabAutomation).setHardness(2.5F).setResistance(20F);
        (shaft = new BlockShaft("shaft", Material.IRON, 0x20)).setCreativeTab(tabAutomation).setHardness(2.0F).setResistance(10F);
        (electricCoilC = TileBlock.create("electricCoilC", Material.IRON, SoundType.METAL, DefaultItemBlock.class, 0x22)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F);
        (electricCoilA = TileBlock.create("electricCoilA", Material.IRON, SoundType.METAL, DefaultItemBlock.class, 0x22)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F);
        (electricCoilH = TileBlock.create("electricCoilH", Material.IRON, SoundType.METAL, DefaultItemBlock.class, 0x22)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F);
        (electricHeater = TileBlock.create("electricHeater", M_thermIns, SoundType.METAL, DefaultItemBlock.class, 0x22)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F);
        (thermIns = new DefaultBlock("thermIns", M_thermIns, DefaultItemBlock.class)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F);
        (pneumaticPiston = TileBlock.create("pneumaticPiston", Material.IRON, SoundType.METAL, DefaultItemBlock.class, 0x22)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F);
        (gasPipe = new BlockPipe("gasPipe", Material.IRON, SoundType.METAL, DefaultItemBlock.class, 0x20)).setCreativeTab(tabAutomation).setHardness(1.0F).setResistance(20F);
        (solidFuelHeater = TileBlock.create("solidFuelHeater", Material.ROCK, SoundType.STONE, DefaultItemBlock.class, 1)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F);
        (gasVent = TileBlock.create("gasVent", Material.GLASS, SoundType.GLASS, DefaultItemBlock.class, 0x22)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F);
        (heatedFurnace = TileBlock.create("heatedFurnace", Material.ROCK, SoundType.STONE, DefaultItemBlock.class, 1)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F);
        gasPipe.size = 0.5F;
        ore.setHarvestLevel("pickaxe", 1, ore.getStateFromMeta(Ore.Copper.ordinal()));
        ore.setHarvestLevel("pickaxe", 2, ore.getStateFromMeta(Ore.Silver.ordinal()));
        ore.setHarvestLevel("pickaxe", 1, ore.getStateFromMeta(Ore.Aluminium.ordinal()));
        
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
        if (L_hydrogenG.canBePlacedInWorld()) Blocks.FIRE.setFireInfo(L_hydrogenG.getBlock(), 500, 20);
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
   
}
