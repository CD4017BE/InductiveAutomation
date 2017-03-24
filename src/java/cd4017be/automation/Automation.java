package cd4017be.automation;

import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.util.ArrayList;

import cd4017be.api.Capabilities.*;
import cd4017be.api.automation.AreaProtect;
import cd4017be.api.recipes.RecipeAPI;
import cd4017be.api.recipes.RecipeScriptContext;
import cd4017be.automation.Block.BlockOre.Ore;
import cd4017be.automation.jetpack.JetPackConfig;
import cd4017be.automation.jetpack.PacketHandler;
import cd4017be.automation.pipes.BasicWarpPipe;
import cd4017be.automation.shaft.GasContainer;
import cd4017be.automation.shaft.ShaftComponent;
import cd4017be.lib.BlockGuiHandler;
import cd4017be.lib.ConfigurationFile;
import cd4017be.lib.DefaultBlock;
import cd4017be.lib.DefaultItem;
import cd4017be.lib.DefaultItemBlock;
import cd4017be.lib.ModFluid;
import cd4017be.lib.TileBlock;
import cd4017be.lib.script.ScriptFiles.Version;
import cd4017be.lib.templates.BlockPipe;
import cd4017be.lib.templates.BlockSuperfluid;
import net.minecraft.block.Block;
import net.minecraft.block.BlockMushroom;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemSeeds;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import cd4017be.automation.Item.*;
import cd4017be.automation.Block.*;
import static cd4017be.automation.Objects.*;

/**
 *
 * @author CD4017BE
 */
@Mod(modid="Automation", useMetadata=true)
public class Automation {

	@Instance("Automation")
	public static Automation instance;

	@SidedProxy(clientSide="cd4017be.automation.ClientProxy", serverSide="cd4017be.automation.CommonProxy")
	public static CommonProxy proxy;

	public static CreativeTabs tabAutomation;
	public static CreativeTabs tabFluids;

	public Automation() {
		RecipeScriptContext.scriptRegistry.add(new Version("inductiveAutomation", 1001, "/assets/automation/config/recipes.rcp"));
	}

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		Config.loadConfig(ConfigurationFile.init(event, "inductiveAutomation.cfg", "/assets/automation/config/preset.cfg", true));
		tabAutomation = new CreativeTabAutomation("automation");
		tabFluids = new CreativeTabFluids("fluids");
		initCapabilities();
		initFluids();
		initItems();
		initBlocks();
		RecipeScriptContext.instance.run("inductiveAutomation.PRE_INIT");
		if (event.getSide().isClient()) JetPackConfig.loadData();
	}

	@Mod.EventHandler
	public void load(FMLInitializationEvent event) {
		BlockGuiHandler.registerMod(this);
		PacketHandler.register();
		AreaProtect.register(this);
		
		System.out.println("Automation: Set Explosion-Resistance of Bedrock to: " + Block.getBlockFromName("bedrock").setResistance(2000000.0F).getExplosionResistance(null));
		
		RecipeAPI.createOreDictEntries(ItemSeeds.class, "itemSeeds");
		RecipeAPI.createOreDictEntries(BlockMushroom.class, "blockMushroom");
		
		proxy.registerRenderers();
	}

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		ArrayList<String> rem = Config.data.getVariables("B.recipe", "AT.rcp", "AF.rcp", "I.rcp");
		Config.data.removeEntry(rem.toArray(new String[rem.size()]));
	}

	private void initCapabilities() {
		CapabilityManager.INSTANCE.register(BasicWarpPipe.class, new EmptyStorage<BasicWarpPipe>(), new EmptyCallable<BasicWarpPipe>());
		CapabilityManager.INSTANCE.register(ShaftComponent.class, new EmptyStorage<ShaftComponent>(), new EmptyCallable<ShaftComponent>());
		CapabilityManager.INSTANCE.register(GasContainer.class, new EmptyStorage<GasContainer>(), new EmptyCallable<GasContainer>());
	}

	private void initItems() {
		selectionTool = new ItemSelectionTool("selectionTool");
		voltMeter = new ItemVoltMeter("voltMeter");
		rotationSensor = new ItemRotationSensor("rotationSensor");
		thermometer = new ItemThermometer("thermometer");
		manometer = new ItemManometer("manometer");
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
		(remBlockType = new DefaultItem("remBlockType")).setCreativeTab(tabAutomation);
		vertexSel = new ItemVertexSel("vertexSel");
	}

	private void initBlocks() {
		class TIMaterial extends Material {
			public TIMaterial() {
				super(MapColor.STONE);
				this.setRequiresTool();
			}
		}
		M_thermIns = new TIMaterial();
		new DefaultItemBlock((heatRadiator = TileBlock.create("heatRadiator", Material.IRON, SoundType.METAL, 0)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F));
		new DefaultItemBlock((lightShaft = TileBlock.create("lightShaft", Material.GLASS, SoundType.GLASS, 0)).setCreativeTab(tabAutomation).setHardness(1.0F).setResistance(10F));
		new DefaultItemBlock((wireC = new BlockPipe("wireC", Material.IRON, SoundType.METAL, 0x20)).setCreativeTab(tabAutomation).setHardness(0.5F).setResistance(10F));
		new DefaultItemBlock((wireA = new BlockPipe("wireA", Material.IRON, SoundType.METAL, 0x20)).setCreativeTab(tabAutomation).setHardness(0.5F).setResistance(10F));
		new DefaultItemBlock((wireH = new BlockPipe("wireH", Material.IRON, SoundType.METAL, 0x20)).setCreativeTab(tabAutomation).setHardness(0.5F).setResistance(10F));
		new ItemLiquidPipe((liquidPipe = new BlockLiquidPipe("liquidPipe", SoundType.GLASS, Material.GLASS, 0x20)).setHardness(0.5F).setCreativeTab(tabAutomation).setResistance(10F));
		new ItemItemPipe((itemPipe = new BlockItemPipe("itemPipe", Material.WOOD, SoundType.WOOD, 0x20)).setCreativeTab(tabAutomation).setHardness(0.5F).setResistance(10F));
		new DefaultItemBlock((warpPipe = new BlockPipe("warpPipe", Material.IRON, SoundType.METAL, 0x20)).setCreativeTab(tabAutomation).setHardness(1.0F).setResistance(20F));
		new ItemESU((SCSU = TileBlock.create("SCSU", Material.IRON, SoundType.METAL, 0x40)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F));
		new ItemESU((OCSU = TileBlock.create("OCSU", Material.IRON, SoundType.METAL, 0x40)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F));
		new ItemESU((CCSU = TileBlock.create("CCSU", Material.IRON, SoundType.METAL, 0x40)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F));
		new DefaultItemBlock((steamEngine = TileBlock.create("steamEngine", Material.IRON, SoundType.METAL, 0)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F));
		new DefaultItemBlock((steamTurbine = TileBlock.create("steamTurbine", Material.IRON, SoundType.METAL, 0)).setCreativeTab(tabAutomation).setHardness(2.5F).setResistance(10F));
		new DefaultItemBlock((steamGenerator = TileBlock.create("steamGenerator", Material.IRON, SoundType.METAL, 0)).setCreativeTab(tabAutomation).setHardness(2.0F).setResistance(10F));
		new DefaultItemBlock((steamBoiler = TileBlock.create("steamBoiler", Material.IRON, SoundType.STONE, 1)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F));
		new DefaultItemBlock((lavaCooler = TileBlock.create("lavaCooler", Material.ROCK, SoundType.STONE, 0)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F));
		new DefaultItemBlock((energyFurnace = TileBlock.create("energyFurnace", Material.IRON, SoundType.STONE, 1)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F));
		new DefaultItemBlock((farm = TileBlock.create("farm", Material.IRON, SoundType.STONE, 0)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F));
		new DefaultItemBlock((miner = TileBlock.create("miner", Material.IRON, SoundType.STONE, 0x10)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F));
		new DefaultItemBlock((magnet = TileBlock.create("magnet", Material.IRON, SoundType.METAL, 0)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F));
		new DefaultItemBlock((link = TileBlock.create("link", Material.IRON, SoundType.METAL, 2)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F));
		new DefaultItemBlock((linkHV = TileBlock.create("linkHV", Material.IRON, SoundType.METAL, 2)).setCreativeTab(tabAutomation).setHardness(2.0F).setResistance(10F));
		new DefaultItemBlock((texMaker = TileBlock.create("texMaker", Material.WOOD, SoundType.WOOD, 0)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F));
		new DefaultItemBlock((builder = TileBlock.create("builder", Material.IRON, SoundType.METAL, 0)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F));
		new DefaultItemBlock((algaePool = TileBlock.create("algaePool", Material.GLASS, SoundType.GLASS, 1)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F));
		new DefaultItemBlock((teleporter = TileBlock.create("teleporter", Material.IRON, SoundType.METAL, 0)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F));
		new DefaultItemBlock((advancedFurnace = TileBlock.create("advancedFurnace", Material.IRON, SoundType.METAL, 1)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F));
		new DefaultItemBlock((pump = TileBlock.create("pump", Material.IRON, SoundType.METAL, 0)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F));
		new DefaultItemBlock((massstorageChest = TileBlock.create("massstorageChest", Material.WOOD, SoundType.WOOD, 0x41)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(50F));
		new ItemMatterOrb((matterOrb = TileBlock.create("matterOrb", Material.IRON, SoundType.STONE, 0x40)).setCreativeTab(tabAutomation).setHardness(1.0F).setResistance(10F));
		new ItemMatterOrb((antimatterBombE = TileBlock.create("antimatterBombE", Material.TNT, SoundType.STONE, 0x40)).setCreativeTab(tabAutomation).setHardness(1.0F).setResistance(10F));
		new ItemAntimatterTank((antimatterBombF = TileBlock.create("antimatterBombF", Material.TNT, SoundType.STONE, 0x41)).setCreativeTab(tabAutomation).setHardness(1.0F).setResistance(10F));
		new ItemAntimatterTank((antimatterTank = TileBlock.create("antimatterTank", Material.IRON, SoundType.METAL, 0x40)).setCreativeTab(tabAutomation).setHardness(2.5F).setResistance(40F));
		new DefaultItemBlock((antimatterFabricator = TileBlock.create("antimatterFabricator", Material.IRON, SoundType.METAL, 1)).setCreativeTab(tabAutomation).setHardness(2.5F).setResistance(40F));
		new DefaultItemBlock((antimatterAnihilator = TileBlock.create("antimatterAnihilator", Material.IRON, SoundType.METAL, 1)).setCreativeTab(tabAutomation).setHardness(2.5F).setResistance(40F));
		new DefaultItemBlock((hpSolarpanel = TileBlock.create("hpSolarpanel", Material.GLASS, SoundType.GLASS, 0)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F));
		new DefaultItemBlock((autoCrafting = TileBlock.create("autoCrafting", Material.WOOD, SoundType.WOOD, 0)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F));
		new DefaultItemBlock((geothermalFurnace = TileBlock.create("geothermalFurnace", Material.ROCK, SoundType.STONE, 1)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F));
		new DefaultItemBlock((steamCompressor = TileBlock.create("steamCompressor", Material.IRON, SoundType.METAL, 1)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F));
		new DefaultItemBlock((electricCompressor = TileBlock.create("electricCompressor", Material.IRON, SoundType.METAL, 1)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F));
		new ItemTank((tank = TileBlock.create("tank", Material.GLASS, SoundType.GLASS, 0x60)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F));
		new DefaultItemBlock((security = TileBlock.create("security", Material.IRON, SoundType.METAL, 0x1)).setCreativeTab(tabAutomation).setBlockUnbreakable().setResistance(1000000F));
		new DefaultItemBlock((decompCooler = TileBlock.create("decompCooler", Material.IRON, SoundType.METAL, 0)).setCreativeTab(tabAutomation).setHardness(2.0F).setResistance(10F));
		new DefaultItemBlock((collector = TileBlock.create("collector", Material.IRON, SoundType.METAL, 2)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F));
		new DefaultItemBlock((trash = TileBlock.create("trash", Material.ROCK, SoundType.STONE, 0)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F));
		new DefaultItemBlock((electrolyser = TileBlock.create("electrolyser", Material.IRON, SoundType.METAL, 0)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F));
		new DefaultItemBlock((fuelCell = TileBlock.create("fuelCell", Material.IRON, SoundType.METAL, 0)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F));
		new DefaultItemBlock((itemSorter = TileBlock.create("itemSorter", Material.WOOD, SoundType.WOOD, 0)).setCreativeTab(tabAutomation).setHardness(0.5F).setResistance(10F));
		new DefaultItemBlock((matterInterfaceB = TileBlock.create("matterInterfaceB", Material.IRON, SoundType.METAL, 0)).setCreativeTab(tabAutomation).setHardness(2.0F).setResistance(10F));
		new DefaultItemBlock((fluidPacker = TileBlock.create("fluidPacker", Material.IRON, SoundType.METAL, 1)).setCreativeTab(tabAutomation).setHardness(2.0F).setResistance(10F));
		new ItemHugeTank((hugeTank = TileBlock.create("hugeTank", Material.GLASS, SoundType.GLASS, 0x60)).setCreativeTab(tabAutomation).setHardness(2.0F).setResistance(15F));
		new DefaultItemBlock((fluidVent = TileBlock.create("fluidVent", Material.IRON, SoundType.METAL, 2)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F));
		new DefaultItemBlock((gravCond = TileBlock.create("gravCond", Material.IRON, SoundType.METAL, 0x10)).setCreativeTab(tabAutomation).setHardness(2.5F).setResistance(20F));
		new DefaultItemBlock((itemBuffer = TileBlock.create("itemBuffer", Material.WOOD, SoundType.WOOD, 0)).setCreativeTab(tabAutomation).setHardness(0.5F).setResistance(10F));
		new ItemQuantumTank((quantumTank = TileBlock.create("quantumTank", Material.GLASS, SoundType.GLASS, 0x60)).setCreativeTab(tabAutomation).setHardness(2.5F).setResistance(20F));
		new DefaultItemBlock((vertShemGen = TileBlock.create("vertShemGen", Material.ROCK, SoundType.WOOD, 0)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F));
		new ItemOre((ore = new BlockOre("ore")).setHardness(2.0F).setResistance(10F));
		new DefaultItemBlock((pool = TileBlock.create("pool", Material.GLASS, SoundType.STONE, 0x20)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F).setLightOpacity(5));
		new ItemBlockUnbreakable(unbrStone = new BlockUnbreakable("unbrStone"));
		new DefaultItemBlock(unbrGlass = new GlassUnbreakable("unbrGlass"));
		new DefaultItemBlock((solarpanel = TileBlock.create("solarpanel", Material.GLASS, SoundType.GLASS, 0x20)).setBlockBounds(new AxisAlignedBB(0, 0, 0, 1, 0.125, 1)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F));
		new DefaultItemBlock((teslaTransmitterLV = TileBlock.create("teslaTransmitterLV", Material.IRON, SoundType.METAL, 0x60)).setBlockBounds(new AxisAlignedBB(0.1875, 0, 0.1875, 0.8125, 1, 0.8125)).setCreativeTab(tabAutomation).setHardness(2.0F).setResistance(15F));
		new DefaultItemBlock((teslaTransmitter = TileBlock.create("teslaTransmitter", Material.IRON, SoundType.METAL, 0x60)).setBlockBounds(new AxisAlignedBB(0.25, 0, 0.25, 0.75, 1, 0.75)).setCreativeTab(tabAutomation).setHardness(2.5F).setResistance(20F));
		new ItemInterdimHole((wormhole = TileBlock.create("wormhole", Material.PORTAL, SoundType.GLASS, 0x60)).setBlockBounds(new AxisAlignedBB(0.0625, 0.0625, 0.0625, 0.9375, 0.9375, 0.9375)).setCreativeTab(tabAutomation).setHardness(2.5F).setResistance(20F));
		new DefaultItemBlock((shaft = new BlockShaft("shaft", Material.IRON, 0x20)).setCreativeTab(tabAutomation).setHardness(2.0F).setResistance(10F));
		new DefaultItemBlock((electricCoilC = TileBlock.create("electricCoilC", Material.IRON, SoundType.METAL, 0x22)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F));
		new DefaultItemBlock((electricCoilA = TileBlock.create("electricCoilA", Material.IRON, SoundType.METAL, 0x22)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F));
		new DefaultItemBlock((electricCoilH = TileBlock.create("electricCoilH", Material.IRON, SoundType.METAL, 0x22)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F));
		new DefaultItemBlock((electricHeater = TileBlock.create("electricHeater", M_thermIns, SoundType.METAL, 0x22)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F));
		new DefaultItemBlock((thermIns = new DefaultBlock("thermIns", M_thermIns)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F));
		new DefaultItemBlock((pneumaticPiston = TileBlock.create("pneumaticPiston", Material.IRON, SoundType.METAL, 0x22)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F));
		new DefaultItemBlock((gasPipe = new BlockPipe("gasPipe", Material.IRON, SoundType.METAL, 0x20)).setCreativeTab(tabAutomation).setHardness(1.0F).setResistance(20F));
		new DefaultItemBlock((solidFuelHeater = TileBlock.create("solidFuelHeater", Material.ROCK, SoundType.STONE, 1)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F));
		new DefaultItemBlock((gasVent = TileBlock.create("gasVent", Material.GLASS, SoundType.GLASS, 0x22)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F));
		new DefaultItemBlock((heatedFurnace = TileBlock.create("heatedFurnace", Material.ROCK, SoundType.STONE, 1)).setCreativeTab(tabAutomation).setHardness(1.5F).setResistance(10F));
		new DefaultItemBlock((CEU = TileBlock.create("CEU", Material.IRON, SoundType.METAL, 1)).setCreativeTab(tabAutomation).setBlockUnbreakable().setResistance(Float.POSITIVE_INFINITY));
		gasPipe.size = 0.5F;
		ore.setHarvestLevel("pickaxe", 1, ore.getStateFromMeta(Ore.Copper.ordinal()));
		ore.setHarvestLevel("pickaxe", 2, ore.getStateFromMeta(Ore.Silver.ordinal()));
		ore.setHarvestLevel("pickaxe", 1, ore.getStateFromMeta(Ore.Aluminium.ordinal()));
		
		proxy.registerBlocks();
	}

	private void initFluids() {
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

	private Fluid registerFluid(Fluid fluid) {
		Fluid ret = FluidRegistry.getFluid(fluid.getName());
		if (ret == null) {
			FluidRegistry.registerFluid(fluid);
			fluid.setBlock(new BlockSuperfluid(fluid.getName(), (ModFluid)fluid));
			return fluid;
		} else return ret;
	}

}
