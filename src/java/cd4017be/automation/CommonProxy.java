/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cd4017be.automation;


import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.IFuelHandler;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.Level;

import cd4017be.api.automation.AreaProtect;
import cd4017be.api.automation.AutomationRecipes;
import cd4017be.api.automation.AutomationRecipes.LFRecipe;
import cd4017be.api.automation.AutomationRecipes.CoolRecipe;
import cd4017be.api.automation.AutomationRecipes.ElRecipe;
import cd4017be.api.automation.AutomationRecipes.GCRecipe;
import cd4017be.automation.Item.ItemMinerDrill;
import cd4017be.automation.TileEntity.*;
import static cd4017be.lib.BlockItemRegistry.stack;
import cd4017be.lib.NBTRecipe;
import cd4017be.lib.TileBlockRegistry;
import cd4017be.lib.TileContainer;
import cd4017be.lib.util.OreDictStack;
import net.minecraft.block.Block;
import net.minecraft.block.BlockMushroom;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemSeeds;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import static cd4017be.automation.Objects.*;

/**
 *
 * @author CD4017BE
 */
public class CommonProxy implements IFuelHandler
{
    
    private List<BioEntry> bioList = new ArrayList<BioEntry>();
    public static final String[] dyes = {"dyeBlack", "dyeRed", "dyeGreen", "dyeBrown", "dyeBlue", "dyePurple", "dyeCyan", "dyeLightGray", "dyeGray", "dyePink", "dyeLime", "dyeYellow", "dyeLightBlue", "dyeMagenta", "dyeOrange", "dyeWhite"};
    
    public void registerBlocks() 
    {
		TileBlockRegistry.register(lightShaft, LightShaft.class, null);
		TileBlockRegistry.register(wireC, Wire.class, null);
		TileBlockRegistry.register(wireA, Wire.class, null);
		TileBlockRegistry.register(wireH, Wire.class, null);
		TileBlockRegistry.register(liquidPipe, LiquidPipe.class, null);
		TileBlockRegistry.register(itemPipe, ItemPipe.class, null);
		TileBlockRegistry.register(warpPipe, WarpPipe.class, null);
		TileBlockRegistry.register(voltageTransformer, VoltageTransformer.class, TileContainer.class);
		TileBlockRegistry.register(SCSU, ESU.class, TileContainer.class);
		TileBlockRegistry.register(OCSU, ESU.class, TileContainer.class);
		TileBlockRegistry.register(CCSU, ESU.class, TileContainer.class);
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
		TileBlockRegistry.register(detector, Detector.class, TileContainer.class);
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
    	TileBlockRegistry.register(shaft, Shaft.class, null);
    	TileBlockRegistry.register(electricCoilC, ElectricCoil.class, TileContainer.class);
    	TileBlockRegistry.register(electricCoilA, ElectricCoil.class, TileContainer.class);
    	TileBlockRegistry.register(electricCoilH, ElectricCoil.class, TileContainer.class);
    }
    
    public void registerRenderers() {
        
    }

    @Override
    public int getBurnTime(ItemStack fuel) 
    {
        if (stack("Biomass", 1).isItemEqual(fuel)) return Config.K_dryBiomass;
        else if (stack("Graphite", 1).isItemEqual(fuel))return 6400;
        else if ((new ItemStack(Block.getBlockFromName("fire"))).isItemEqual(fuel)) return 800;
        //else if (stack("JetFuel", 1).isItemEqual(fuel)) return 160000;
        else return 0;
    }
    
    public void registerBioFuel(Object item, int n)
    {
        if (n > 0) bioList.add(new BioEntry(item, n));
    }
    
    public void registerBioFuel(Object item, int n, int a)
    {
    	if (n > 0 || a > 0) bioList.add(new BioEntry(item, n, a));
    }
    
    public List<BioEntry> getBioFuels() {
    	return bioList;
    }
    
    public void registerBioFuels()
    {
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
    	this.registerBioFuel(new ItemStack(Blocks.cactus), 75);
    	this.registerBioFuel(new ItemStack(Items.sugar), 75);
    	this.registerBioFuel(new ItemStack(Items.egg), 240);
    	this.registerBioFuel(BlockMushroom.class, 40);
    	this.registerBioFuel(new ItemStack(Blocks.waterlily), 20, 10);
    }
    
    public int[] getLnutrients(ItemStack item)
    {
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
    
    public class BioEntry
    {
        public final Object item;
        public final int nutrients;
        public final int algae;
        
        public BioEntry(Object item, int n)
        {
            this(item, n, 0);
        }
        
		public BioEntry(Object item, int n, int a)
        {
        	this.item = item;
        	this.nutrients = n;
        	this.algae = a;
        }
        
        @SuppressWarnings("rawtypes")
		public boolean matches(ItemStack item)
        {
            if (this.item instanceof ItemStack) return ((ItemStack)this.item).isItemEqual(item);
            else if (this.item instanceof OreDictStack){
                return ((OreDictStack)this.item).isEqual(item);
            } else if (this.item instanceof Class) {
                if (item.getItem() instanceof ItemBlock) return ((Class)this.item).isInstance(((ItemBlock)item.getItem()).block);
                else return ((Class)this.item).isInstance(item.getItem());
            } else return false;
        }
    }
    
    public void configurableRecipe(String name, IRecipe recipe)
    {
    	if (Config.data.getBoolean("recipe." + name, true)) {
    		GameRegistry.addRecipe(recipe);
    	}
    }
    
    public static boolean enabled(String name)
    {
    	return Config.data.getBoolean(name, true);
    }
    
    public void registerRecipes()
    {
        OreDictionary.registerOre("AIcasingWood", stack("WoodC", 1));
        OreDictionary.registerOre("AIcasingWood", new ItemStack(Blocks.chest));
        OreDictionary.registerOre("AIcasingGlass", stack("GlassC", 1));
        OreDictionary.registerOre("AIcasingGlass", new ItemStack(Items.bucket));
        OreDictionary.registerOre("AIcasingIron", stack("IronC", 1));
        OreDictionary.registerOre("AIcasingIron", new ItemStack(Items.iron_chestplate));
        OreDictionary.registerOre("AIcasingSteel", stack("SteelH", 1));
        OreDictionary.registerOre("AIcasingStone", stack("StoneC", 1));
        OreDictionary.registerOre("AIcasingGraphite", stack("GDPlate", 1));
        OreDictionary.registerOre("AIcasingUnbr", stack("unbrC", 1));
        
        FurnaceRecipes.instance().addSmeltingRecipe(stack("oreSilver", 1), stack("SilverI", 1), 0F);
        FurnaceRecipes.instance().addSmeltingRecipe(stack("oreCopper", 1), stack("CopperI", 1), 0F);
        ItemStack item;
        item = stack("ElectrumR", 1);
        FurnaceRecipes.instance().addSmeltingRecipe(item, stack("ElectrumI", 1), 0F);
        item = stack("CondRA", 1);
        FurnaceRecipes.instance().addSmeltingRecipe(item, stack("CondIA", 1), 0F);
        item = stack("WoodC", 1);
        FurnaceRecipes.instance().addSmeltingRecipe(item, new ItemStack(Items.coal, 1, 1), 0F);
        item = stack("GlassC", 1);
        FurnaceRecipes.instance().addSmeltingRecipe(item, new ItemStack(Blocks.glass, 4), 0F);
        item = stack("IronC", 1);
        FurnaceRecipes.instance().addSmeltingRecipe(item, new ItemStack(Items.iron_ingot, 4), 0F);
        item = stack("IronOD", 1);
        FurnaceRecipes.instance().addSmeltingRecipe(item, new ItemStack(Items.iron_ingot), 0F);
        item = stack("IronD", 1);
        FurnaceRecipes.instance().addSmeltingRecipe(item, new ItemStack(Items.iron_ingot), 0F);
        item = stack("GoldOD", 1);
        FurnaceRecipes.instance().addSmeltingRecipe(item, new ItemStack(Items.gold_ingot), 0F);
        item = stack("GoldD", 1);
        FurnaceRecipes.instance().addSmeltingRecipe(item, new ItemStack(Items.gold_ingot), 0F);
        item = stack("CopperOD", 1);
        FurnaceRecipes.instance().addSmeltingRecipe(item, stack("CopperI", 1), 0F);
        item = stack("CopperD", 1);
        FurnaceRecipes.instance().addSmeltingRecipe(item, stack("CopperI", 1), 0F);
        item = stack("SilverOD", 1);
        FurnaceRecipes.instance().addSmeltingRecipe(item, stack("SilverI", 1), 0F);
        item = stack("SilverD", 1);
        FurnaceRecipes.instance().addSmeltingRecipe(item, stack("SilverI", 1), 0F);
        
        GameRegistry.addRecipe(new ShapedOreRecipe(stack("CoilH", 1), "000", "010", "000", '0', new ItemStack(Blocks.iron_bars), '1', "sandstone"));
        GameRegistry.addRecipe(new ShapedOreRecipe(stack("CoilCp", 1), "000", "010", "000", '0', stack("tile.wireC", 1), '1', "ingotIron"));
        GameRegistry.addRecipe(new ShapedOreRecipe(stack("CoilC", 1), "000", "010", "000", '0', stack("tile.wireA", 1), '1', "ingotIron"));
        GameRegistry.addRecipe(new ShapedOreRecipe(stack("CoilSC", 1), "000", "010", "000", '0', stack("tile.wireH", 1), '1', "ingotIron"));
        GameRegistry.addRecipe(new ShapedOreRecipe(stack("Motor", 1), " 0 ", "121", " 0 ", '0', "stickWood", '1', stack("CoilC", 1), '2', "ingotIron"));
        GameRegistry.addRecipe(new ShapedOreRecipe(stack("Vent", 1), "000", "111", "000", '0', "ingotCopper", '1', new ItemStack(Blocks.iron_bars)));
        GameRegistry.addRecipe(new ShapedOreRecipe(stack("Breaker", 1), "01", " 4", "23", '0', new ItemStack(Items.golden_pickaxe), '1', stack("Motor", 1), '2', new ItemStack(Blocks.hopper), '3', "AIcasingWood", '4', "AIcasingIron"));
        GameRegistry.addRecipe(new ShapedOreRecipe(stack("Placer", 1), "01", " 4", "23", '0', new ItemStack(Blocks.piston), '1', stack("CoilC", 1), '2', new ItemStack(Blocks.dispenser), '3', "AIcasingWood", '4', "AIcasingIron"));
        GameRegistry.addRecipe(new ShapedOreRecipe(stack("AreaFrame", 1), "000", "123", "000", '0', new ItemStack(Blocks.iron_bars), '1', stack("Circut", 1), '2', stack("Motor", 1), '3', "stickWood"));
        GameRegistry.addRecipe(new NBTRecipe(stack("item.energyCell", 1), "+energy", "0", '0', stack("tile.SCSU", 1)));
        GameRegistry.addRecipe(new ShapedOreRecipe(stack("Acelerator", 1), "010", "222", "010", '0', stack("HydrogenI", 1), '1', stack("Vent", 1), '2', "AIcasingSteel"));
        GameRegistry.addRecipe(new ShapedOreRecipe(stack("AnihilationC", 1), "010", "232", "010", '0', "ingotSteel", '1', "blockGlass", '2', stack("Vent", 1), '3', stack("tile.hpSolarpanel", 1)));
        GameRegistry.addRecipe(new ShapedOreRecipe(stack("QSGen", 1), "010", "232", "010", '0', stack("SiliconI", 1), '1', stack("Circut", 1), '2', "glowstone", '3', "AIcasingGraphite"));
        GameRegistry.addRecipe(new ShapedOreRecipe(stack("BHGen", 1), "030", "212", "030", '0', stack("Neutron", 1), '1', stack("BlackHole", 1), '2', "AIcasingGraphite", '3', stack("CoilSC", 1)));
        GameRegistry.addRecipe(new ShapedOreRecipe(stack("mAccelerator", 1), "010", "232", "010", '0', stack("tile.wireH", 1), '1', stack("CoilSC", 1), '2', stack("Acelerator", 1), '3', "AIcasingSteel"));
        GameRegistry.addRecipe(new ShapedOreRecipe(stack("amAcelerator", 1), "000", "010", "000", '0', "AIcasingUnbr", '1', stack("mAccelerator", 1)));
        GameRegistry.addRecipe(new ShapedOreRecipe(stack("Fokus", 1), "010", "121", "010", '0', stack("Circut", 1), '1', stack("CoilSC", 1), '2', stack("QAlloyI", 1)));
        configurableRecipe("item.contJetFuel", new ShapedOreRecipe(stack("item.contJetFuel", 1), "010", '0', stack("LCHydrogen", 1), '1', stack("LCOxygen", 1)));
        configurableRecipe("item.contLiquidAir", new ShapedOreRecipe(stack("item.contLiquidAir", 1), "00", "01", '0', stack("LCNitrogen", 1), '1', stack("LCOxygen", 1)));
        configurableRecipe("item.contAlgaeFood", new ShapedOreRecipe(stack("item.contAlgaeFood", 1), "00", "12", '0', stack("LCAlgae", 1), '1', new ItemStack(Items.potionitem), '2', new ItemStack(Items.apple)));
        configurableRecipe("item.contInvEnergy", new NBTRecipe(stack("item.contInvEnergy", 1), "+energy", "010", '0', stack("tile.wireH", 1), '1', stack("tile.CCSU", 1)));
        GameRegistry.addRecipe(new ShapedOreRecipe(stack("Strap", 1), "0 0", "000", "000", '0', new ItemStack(Items.string)));
        GameRegistry.addRecipe(new ShapedOreRecipe(stack("Control", 1), "012", "343", "656", '0', new ItemStack(Items.compass), '1', "dustGlowstone", '2', new ItemStack(Items.clock), '3', new ItemStack(Blocks.lever), '4', stack("Circut", 1), '5', "dustRedstone", '6', new ItemStack(Blocks.stone_button)));
        GameRegistry.addRecipe(new ShapedOreRecipe(stack("JetTurbine", 1), "010", "020", "343", '0', stack("liquidPipeT", 1), '1', stack("tile.collector", 1), '2', stack("Circut", 1), '3', "AIcasingSteel", '4', stack("Turbine", 1)));
        configurableRecipe("item.jetpack", new ShapedOreRecipe(stack("item.jetpack", 1), " 0 ", "121", "343", '0', stack("Strap", 1), '1', stack("liquidPipeT", 1), '2', stack("Control", 1), '3', stack("JetTurbine", 1), '4', "dustRedstone"));
        configurableRecipe("item.jetpackIron", new ShapedOreRecipe(stack("item.jetpackIron", 1), "010", '0', "AIcasingIron", '1', stack("item.jetpack", 1)));
        configurableRecipe("item.jetpackSteel", new ShapedOreRecipe(stack("item.jetpackSteel", 1), "010", '0', "AIcasingSteel", '1', stack("item.jetpack", 1)));
        configurableRecipe("item.jetpackGraphite", new ShapedOreRecipe(stack("item.jetpackGraphite", 1), "010", '0', "AIcasingGraphite", '1', stack("item.jetpack", 1)));
        configurableRecipe("item.jetpackUnbr", new ShapedOreRecipe(stack("item.jetpackUnbr", 1), "010", '0', "AIcasingUnbr", '1', stack("item.jetpack", 1)));
        GameRegistry.addRecipe(new ShapedOreRecipe(stack("item.selectionTool", 1), "0", "1", '0', new ItemStack(Items.compass), '1', stack("Circut", 1)));
        configurableRecipe("item.voltMeter", new ShapelessOreRecipe(stack("item.voltMeter", 1), new ItemStack(Items.compass), stack("CoilCp", 1)));
        configurableRecipe("item.chisle", new NBTRecipe(stack("item.chisle", 1), "+energy", " 00", "120", "31 ", '0', "gemDiamond", '1', stack("CoilC", 1), '2', "ingotIron", '3', stack("item.energyCell", 1, OreDictionary.WILDCARD_VALUE)));
        configurableRecipe("item.cutter", new NBTRecipe(stack("item.cutter", 1), "+energy", "20", "10", "3 ", '0', "gemDiamond", '1', stack("Motor", 1), '2', "ingotIron", '3', stack("item.energyCell", 1, OreDictionary.WILDCARD_VALUE)));
        configurableRecipe("item.portableMagnet", new NBTRecipe(stack("item.portableMagnet", 1), "+energy", "0", "1", '0', stack("tile.magnet", 1), '1', stack("item.energyCell", 1, OreDictionary.WILDCARD_VALUE)));
        GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(Items.paper), stack("item.builderTexture", 1)));
        GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(Items.paper), stack("item.teleporterCoords", 1)));
        configurableRecipe("item.stoneDrill", new ShapedOreRecipe(stack("item.stoneDrill", 1), " 0 ", "010", " 0 ", '0', new ItemStack(Items.stone_pickaxe), '1', "AIcasingStone"));
        ItemMinerDrill.anvilHandler.repairMaterials.put(stoneDrill, new ItemStack(Blocks.stone, 20));
        configurableRecipe("item.ironDrill", new ShapedOreRecipe(stack("item.ironDrill", 1), " 0 ", "010", " 0 ", '0', new ItemStack(Items.iron_pickaxe), '1', "AIcasingSteel"));
        ItemMinerDrill.anvilHandler.repairMaterials.put(ironDrill, new ItemStack(Items.iron_ingot, 16));
        configurableRecipe("item.diamondDrill", new ShapedOreRecipe(stack("item.diamondDrill", 1), " 0 ", "010", " 0 ", '0', new ItemStack(Items.diamond_pickaxe), '1', "AIcasingGraphite"));
        ItemMinerDrill.anvilHandler.repairMaterials.put(diamondDrill, new ItemStack(Items.diamond, 12));
        configurableRecipe("item.amLaser", new NBTRecipe(stack("item.amLaser", 1), "#energy, #antimatter, #matter", "012", "334", "056", '0', stack("Fokus", 1), '1', stack("tile.magnet", 1), '2', stack("tile.matterOrb", 1), '3', stack("amAcelerator", 1), '4', stack("tile.antimatterTank", 1), '5', stack("tile.voltageTransformer", 1), '6', stack("tile.OCSU", 1)));
        configurableRecipe("item.mCannon", new NBTRecipe(stack("item.mCannon", 1), "#energy, #matter", "001", "234", '0', stack("mAccelerator", 1), '1', stack("tile.matterOrb", 1), '2', stack("Fokus", 1), '3', stack("tile.voltageTransformer", 1), '4', stack("tile.OCSU", 1)));
        configurableRecipe("item.matterInterface", new ShapedOreRecipe(stack("item.matterInterface", 1), "000", "121", "333", '0', stack("itemPipeI", 1), '1', stack("QAlloyI", 1), '2', stack("Circut", 1), '3', stack("itemPipeE", 1)));
        configurableRecipe("item.fluidFilter", new ShapedOreRecipe(stack("item.fluidUpgrade", 8), "012", '0', new ItemStack(Items.comparator), '1', "AIcasingGlass", '2', stack("Circut", 1)));
        configurableRecipe("item.fluidFilter", new NBTRecipe(stack("item.fluidUpgrade", 2), "#mode, #list", "00", '0', stack("item.fluidUpgrade", 1)));
        configurableRecipe("item.itemFilter", new ShapedOreRecipe(stack("item.itemUpgrade", 8), "012", '0', new ItemStack(Items.comparator), '1', "AIcasingWood", '2', stack("Circut", 1)));
        configurableRecipe("item.itemFilter", new NBTRecipe(stack("item.itemUpgrade", 2), "#mode, #maxAm, #list", "00", '0', stack("item.itemUpgrade", 1)));
        configurableRecipe("item.portableFurnace", new NBTRecipe(stack("item.portableFurnace", 1), "#energy", " 0 ", "123", " 0 ", '0', new ItemStack(Items.string), '1', stack("itemPipeE", 1), '2', stack("tile.energyFurnace", 1), '3', stack("item.energyCell", 1)));
        configurableRecipe("item.portableInventory", new ShapedOreRecipe(stack("item.portableInventory", 1), " 0 ", "123", " 0 ", '0', new ItemStack(Items.string), '1', stack("itemPipeE", 1), '2', "AIcasingWood", '3', stack("itemPipeI", 1)));
        configurableRecipe("item.portableCrafter", new ShapedOreRecipe(stack("item.portableCrafter", 1), "0", "1", "0", '0', new ItemStack(Items.string), '1', stack("tile.autoCrafting", 1)));
        configurableRecipe("item.portableGenerator", new ShapedOreRecipe(stack("item.portableGenerator", 1), " 0 ", "123", " 0 ", '0', new ItemStack(Items.string), '1', stack("itemPipeE", 1), '2', stack("tile.steamBoiler", 1), '3', stack("tile.steamEngine", 1)));
        configurableRecipe("item.portableRemoteInv", new ShapedOreRecipe(stack("item.portableRemoteInv", 1), " 0 ", "123", " 0 ", '0', new ItemStack(Items.string), '1', stack("itemPipeE", 1), '2', stack("EMatrix", 1), '3', stack("itemPipeI", 1)));
        configurableRecipe("item.portableTeleporter", new NBTRecipe(stack("item.portableTeleporter", 1), "#energy", "010", "232", "456", '0', stack("EMatrix", 1), '1', stack("BHGen", 1), '2', new ItemStack(Items.string), '3', stack("QSGen", 1), '4', stack("tile.OCSU", 1), '5', "AIcasingSteel", '6', new ItemStack(Items.writable_book)));
        configurableRecipe("item.portablePump", new NBTRecipe(stack("item.portablePump", 1), "#energy", "01", "23", "45", '0', stack("tile.collector", 1), '1', stack("liquidPipeE", 1), '2', stack("Motor", 1), '3', stack("liquidPipeI", 1), '4', stack("item.energyCell", 1), '5', "AIcasingGlass"));
        configurableRecipe("item.translocator", new ShapedOreRecipe(stack("item.translocator", 1), "0 1", " 2 ", " 3 ", '0', new ItemStack(Blocks.sticky_piston), '1', new ItemStack(Blocks.piston), '2', stack("EMatrix", 1), '3', "AIcasingIron"));
        configurableRecipe("item.translocator", new ShapelessOreRecipe(stack("item.translocator", 1), stack("item.translocator", 1)));
        configurableRecipe("item.portableTesla", new NBTRecipe(stack("item.portableTesla", 1), "#voltage", " 0 ", "121", '0', stack("tile.teslaTransmitterLV", 1), '1', new ItemStack(Items.string), '2', stack("tile.linkHV", 1)));
        configurableRecipe("item.placement", new ShapedOreRecipe(stack("item.placement", 4), "012", '0', "AIcasingWood", '1', stack("Circut", 1), '2', new ItemStack(Blocks.dispenser)));
        configurableRecipe("item.synchronizer", new ShapedOreRecipe(stack("item.synchronizer", 1), "0", "1", '0', stack("AreaFrame", 1), '1', stack("EMatrix", 1)));
        configurableRecipe("item.remBlockType", new ShapedOreRecipe(stack("item.remBlockType", 1), "0", "1", '0', new ItemStack(Items.wooden_pickaxe, 1, OreDictionary.WILDCARD_VALUE), '1', new ItemStack(Items.paper)));
        configurableRecipe("item.vertexSel", new ShapedOreRecipe(stack("item.vertexSel", 1), "0", "1", '0', new ItemStack(Items.compass), '1', stack("RstMetall", 1)));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(Items.paper), " 0 ", '0', new ItemStack(Items.paper)));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(Items.book), " 0 ", '0', new ItemStack(Items.book)));
        
        GameRegistry.addRecipe(new ShapedOreRecipe(stack("tile.shaft", 3), "000", '0', "ingotIron"));
        
        GameRegistry.addRecipe(new ShapedOreRecipe(stack("tile.wireC", 8), "000", '0', "ingotCopper"));
        GameRegistry.addRecipe(new ShapedOreRecipe(stack("tile.wireA", 8), "000", '0', "ingotConductiveAlloy"));
        GameRegistry.addRecipe(new ShapedOreRecipe(stack("tile.wireH", 8), "010", "222", "010", '0', "ingotElectrum", '1', stack("SiliconI", 1), '2', stack("HydrogenI", 1)));
        GameRegistry.addRecipe(new ShapedOreRecipe(stack("liquidPipeT", 24), "000", '0', "AIcasingGlass"));
        GameRegistry.addRecipe(new ShapedOreRecipe(stack("liquidPipeI", 4), " 0 ", "010", " 0 ", '0', stack("liquidPipeT", 1), '1', new ItemStack(Blocks.piston)));
        GameRegistry.addRecipe(new ShapedOreRecipe(stack("liquidPipeE", 4), " 0 ", "010", " 0 ", '0', stack("liquidPipeT", 1), '1', new ItemStack(Blocks.sticky_piston)));
        GameRegistry.addRecipe(new ShapedOreRecipe(stack("itemPipeT", 24), "000", '0', "AIcasingWood"));
        GameRegistry.addRecipe(new ShapedOreRecipe(stack("itemPipeI", 4), " 0 ", "010", " 0 ", '0', stack("itemPipeT", 1), '1', new ItemStack(Blocks.piston)));
        GameRegistry.addRecipe(new ShapedOreRecipe(stack("itemPipeE", 4), " 0 ", "010", " 0 ", '0', stack("itemPipeT", 1), '1', new ItemStack(Blocks.sticky_piston)));
        configurableRecipe("tile.warpPipe", new ShapedOreRecipe(stack("tile.warpPipe", 24), "000", "232", "111", '0', "AIcasingWood", '1', "AIcasingGlass", '2', new ItemStack(Blocks.obsidian), '3', stack("EMatrix", 1)));
        GameRegistry.addRecipe(new NBTRecipe(stack("tile.SCSU", 1), "+energy", "0", "1", '0', stack("item.energyCell", 1, OreDictionary.WILDCARD_VALUE), '1', stack("tile.wireC", 1)));
        GameRegistry.addRecipe(new NBTRecipe(stack("tile.OCSU", 1), "+energy", "000", "010", "000", '0', stack("item.energyCell", 1, OreDictionary.WILDCARD_VALUE), '1', stack("tile.wireA", 1)));
        GameRegistry.addRecipe(new ShapedOreRecipe(stack("tile.CCSU", 1), "010", "232", "010", '0', stack("SiliconI", 1), '1', "AIcasingGraphite", '2', stack("CoilSC", 1), '3', stack("LCHelium", 1)));
        
        GameRegistry.addRecipe(new ShapedOreRecipe(stack("tile.electricCoilC", 1), "000", "010", "000", '0', stack("tile.wireC", 1), '1', "ingotIron"));
        GameRegistry.addRecipe(new ShapedOreRecipe(stack("tile.electricCoilA", 1), "000", "010", "000", '0', stack("tile.wireA", 1), '1', "ingotIron"));
        GameRegistry.addRecipe(new ShapedOreRecipe(stack("tile.electricCoilH", 1), "000", "010", "000", '0', stack("tile.wireH", 1), '1', "ingotIron"));
        
        configurableRecipe("tile.steamEngine", new ShapedOreRecipe(stack("tile.steamEngine", 1), "010", " 2 ", '0', "AIcasingGlass", '1', new ItemStack(Blocks.piston), '2', stack("CoilCp", 1)));
        configurableRecipe("tile.steamGenerator", new ShapedOreRecipe(stack("tile.steamGenerator", 1), "000", "121", "000", '0', new ItemStack(Blocks.piston), '1', "AIcasingGlass", '2', stack("Motor", 1)));
        configurableRecipe("tile.steamTurbine", new ShapedOreRecipe(stack("tile.steamTurbine", 1), "001", "222", "100", '0', "AIcasingSteel", '1', "AIcasingGlass", '2', stack("Turbine", 1)));
        configurableRecipe("tile.steamBoiler", new ShapedOreRecipe(stack("tile.steamBoiler", 1), "010", " 2 ", '0', "AIcasingGlass", '1', new ItemStack(Blocks.furnace), '2', "AIcasingWood"));
        configurableRecipe("tile.lavaCooler", new ShapedOreRecipe(stack("tile.lavaCooler", 1), "230", "010", '0', "AIcasingGlass", '1', "AIcasingStone", '2', "AIcasingWood", '3', stack("Vent", 1)));
        configurableRecipe("tile.energyFurnace", new ShapedOreRecipe(stack("tile.energyFurnace", 1), " 0 ", "121", " 3 ", '0', stack("CoilH", 1), '1', new ItemStack(Blocks.brick_block), '2', "AIcasingIron", '3', "AIcasingWood"));
        configurableRecipe("tile.magnet", new ShapedOreRecipe(stack("tile.magnet", 2), "010", "121", "010", '0', new ItemStack(Blocks.iron_bars), '1', stack("CoilC", 1), '2', "ingotIron"));
        configurableRecipe("tile.farm", new ShapedOreRecipe(stack("tile.farm", 1), "1 4", "023", '0', stack("Circut", 1), '1', "AIcasingWood", '2', stack("AreaFrame", 1), '3', stack("Breaker", 1), '4', stack("Placer", 1)));
        configurableRecipe("tile.miner", new ShapedOreRecipe(stack("tile.miner", 1), "1 3", "023", '0', "AIcasingIron", '1', "AIcasingWood", '2', stack("AreaFrame", 1), '3', stack("Breaker", 1)));
        configurableRecipe("tile.builder", new ShapedOreRecipe(stack("tile.builder", 1), "1 3", "023", '0', stack("Circut", 1), '1', "AIcasingWood", '2', stack("AreaFrame", 1), '3', stack("Placer", 1)));
        configurableRecipe("tile.link", new ShapedOreRecipe(stack("tile.link", 1), "003", "124", "003", '0', "ingotIron", '1', stack("tile.wireA", 1), '2', stack("tile.voltageTransformer", 1), '3', "ingotElectrum", '4', stack("Circut", 1)));
        configurableRecipe("tile.linkHV", new ShapedOreRecipe(stack("tile.linkHV", 1), "003", "124", "003", '0', "ingotSteel", '1', stack("tile.wireH", 1), '2', stack("tile.voltageTransformer", 1), '3', stack("HydrogenI", 1), '4', stack("EMatrix", 1)));
        configurableRecipe("tile.texMaker", new ShapedOreRecipe(stack("tile.texMaker", 1), "021", "333", '0', new ItemStack(Items.feather), '1', "dyeBlack", '2', "AIcasingWood", '3', new ItemStack(Blocks.stone_slab)));
        configurableRecipe("tile.algaePool", new ShapedOreRecipe(stack("tile.pool", 1), "1", "0", '0', "AIcasingGlass", '1', "paneGlass"));
        configurableRecipe("tile.algaePool", new ShapedOreRecipe(stack("tile.algaePool", 1), "000", "123", '0', "AIcasingGlass", '1', "AIcasingWood", '2', stack("Circut", 1), '3', "AIcasingIron"));
        GameRegistry.addRecipe(new ShapedOreRecipe(stack("tile.voltageTransformer", 1), "000", "121", "000", '0', new ItemStack(Blocks.iron_bars), '1', stack("CoilC", 1), '2', "ingotIron"));
        configurableRecipe("tile.teslaTransmitterHV", new ShapedOreRecipe(stack("tile.teslaTransmitter", 1), "000", "121", "131", '0', "ingotElectrum", '1', stack("CoilSC", 1), '2', stack("LCHelium", 1), '3', stack("tile.CCSU", 1)));
        configurableRecipe("tile.teslaTransmitterLV", new ShapedOreRecipe(stack("tile.teslaTransmitterLV", 1), "000", "121", "131", '0', "ingotGold", '1', stack("CoilC", 1), '2', stack("LCNitrogen", 1), '3', stack("tile.OCSU", 1)));
        configurableRecipe("tile.teleporter", new ShapedOreRecipe(stack("tile.teleporter", 1), "2  ", "016", "435", '0', stack("EMatrix", 1), '1', stack("QSGen", 1), '2', "AIcasingWood", '3', stack("BHGen", 1), '4', "AIcasingSteel", '5', stack("AreaFrame", 1), '6', stack("item.translocator", 1)));
        configurableRecipe("tile.pump", new ShapedOreRecipe(stack("tile.pump", 1), "0 4", "123", '0', stack("Circut", 1), '1', "AIcasingGlass", '2', stack("AreaFrame", 1), '3', stack("CoilC", 1), '4', stack("tile.collector", 1)));
        GameRegistry.addRecipe(new ShapedOreRecipe(stack("tile.advancedFurnace", 1), " 0 ", "121", "343", '0', stack("CoilH", 1), '1', "AIcasingWood", '2', "AIcasingIron", '3', "AIcasingGlass", '4', "AIcasingStone"));
        configurableRecipe("tile.massstorageChest", new ShapedOreRecipe(stack("tile.massstorageChest", 1), "000", "010", "000", '0', "AIcasingWood", '1', stack("EMatrix", 1)));
        configurableRecipe("tile.antimatterFabricator", new ShapedOreRecipe(stack("tile.antimatterFabricator", 1), "012", "343", "210", '0', stack("Vent", 1), '1', stack("CoilSC", 1), '2', stack("Circut", 1), '3', stack("mAccelerator", 1), '4', "AIcasingGraphite"));
        configurableRecipe("tile.antimatterAnihilator", new ShapedOreRecipe(stack("tile.antimatterAnihilator", 1), "210", "345", "210", '0', stack("Vent", 1), '1', stack("CoilSC", 1), '2', stack("Circut", 1), '3', stack("amAcelerator", 1), '4', stack("AnihilationC", 1), '5', stack("tile.OCSU", 1)));
        configurableRecipe("tile.antimatterTank", new ShapedOreRecipe(stack("tile.antimatterTank", 1), "010", "232", "010", '0', "ingotSilver", '1', stack("CoilSC", 1), '2', "AIcasingSteel", '3', stack("Circut", 1)));
        configurableRecipe("tile.matterOrb", new ShapedOreRecipe(stack("tile.matterOrb", 1), "000", "010", "000", '0', stack("tile.massstorageChest", 1), '1', stack("QMatrix", 1)));
        configurableRecipe("tile.matterInterfaceB", new ShapedOreRecipe(stack("tile.matterInterfaceB", 1), "010", '0', "AIcasingWood", '1', stack("item.matterInterface", 1)));
        configurableRecipe("tile.antimatterBomb", new ShapedOreRecipe(stack("tile.antimatterBombE", 1), "010", "232", "040", '0', stack("DenseM", 1), '1', stack("tile.antimatterTank", 1), '2', "AIcasingSteel", '3', "AIcasingUnbr", '4', stack("tile.matterOrb", 1)));
        configurableRecipe("tile.antimatterBomb", new ShapedOreRecipe(stack("tile.antimatterBombF", 1), "010", "121", "010", '0', new ItemStack(Items.gunpowder), '1', new ItemStack(Blocks.tnt), '2', stack("tile.antimatterBombE", 1)));
        configurableRecipe("tile.hpSolarpanel", new ShapedOreRecipe(stack("tile.hpSolarpanel", 1), "010", "020", "030", '0', stack("tile.solarpanel", 1), '1', "glowstone", '2', "AIcasingGraphite", '3', "blockLapis"));
        configurableRecipe("tile.geothermalFurnace", new ShapedOreRecipe(stack("tile.geothermalFurnace", 1), " 0 ", "121", " 3 ", '0', "AIcasingGlass", '1', new ItemStack(Blocks.furnace), '2', "AIcasingStone", '3', "AIcasingWood"));
        GameRegistry.addRecipe(new ShapedOreRecipe(stack("tile.steamCompressor", 1), " 0 ", "121", " 3 ", '0', "AIcasingGlass", '1', new ItemStack(Blocks.piston), '2', "AIcasingIron", '3', "AIcasingWood"));
        configurableRecipe("tile.electricCompressor", new ShapedOreRecipe(stack("tile.electricCompressor", 1), " 0 ", "121", " 3 ", '0', stack("CoilC", 1), '1', new ItemStack(Blocks.piston), '2', "AIcasingIron", '3', "AIcasingWood"));
        GameRegistry.addRecipe(new ShapedOreRecipe(stack("tile.tank", 1), "000", "0 0", "000", '0', "AIcasingGlass"));
        if (AreaProtect.permissions > 0 || AreaProtect.chunkloadPerm > 0) 
        	GameRegistry.addRecipe(new ShapedOreRecipe(stack("tile.security", 1), "010", "232", "040", '0', "AIcasingSteel", '1', stack("EMatrix", 1), '2', new ItemStack(Items.ender_pearl), '3', "AIcasingGraphite", '4', stack("item.energyCell", 1, OreDictionary.WILDCARD_VALUE)));
        configurableRecipe("tile.autoCrafting", new ShapedOreRecipe(stack("tile.autoCrafting", 1), "010", "020", '0', "AIcasingWood", '1', stack("Circut", 1), '2', new ItemStack(Blocks.crafting_table)));
        GameRegistry.addRecipe(new ShapedOreRecipe(stack("tile.decompCooler", 1), "010", "232", "242", '0', stack("Vent", 1), '1', "AIcasingSteel", '2', "AIcasingGlass", '3', new ItemStack(Blocks.piston), '4', stack("Motor", 1)));
        configurableRecipe("tile.collector", new ShapedOreRecipe(stack("tile.collector", 1), "111", "232", "040", '0', "ingotCopper", '1', new ItemStack(Blocks.iron_bars), '2', new ItemStack(Blocks.redstone_torch), '3', new ItemStack(Blocks.sticky_piston), '4', "AIcasingGlass"));
        GameRegistry.addRecipe(new ShapedOreRecipe(stack("tile.trash", 1), "000", "123", "000", '0', "cobblestone", '1', "AIcasingGlass", '2', new ItemStack(Items.lava_bucket), '3', "AIcasingWood"));
        GameRegistry.addRecipe(new ShapedOreRecipe(stack("tile.electrolyser", 1), "0 0", "121", "333", '0', "ingotConductiveAlloy", '1', stack("Graphite", 1), '2', "AIcasingSteel", '3', "AIcasingGlass"));
        configurableRecipe("tile.fuelCell", new ShapedOreRecipe(stack("tile.fuelCell", 1), "010", "020", "333", '0', "AIcasingGraphite", '1', stack("item.energyCell", 1), '2', "AIcasingSteel", '3', "AIcasingGlass"));
        configurableRecipe("tile.detector", new ShapedOreRecipe(stack("tile.detector", 4), "010", "234", "050", '0', "stone", '1', "AIcasingWood", '2', new ItemStack(Items.comparator), '3', stack("Circut", 1), '4', stack("CoilCp", 1), '5', "AIcasingGlass"));
        configurableRecipe("tile.itemSorter", new ShapedOreRecipe(stack("tile.itemSorter", 1), " 0 ", "010", " 0 ", '0', stack("itemPipeI", 1), '1', "AIcasingWood"));
        configurableRecipe("tile.wormhole", new ShapedOreRecipe(stack("tile.wormhole", 1), "010", "323", "010", '0', new ItemStack(Blocks.obsidian), '1', new ItemStack(Items.ender_eye), '2', stack("QSGen", 1), '3', stack("BlackHole", 1)));
        configurableRecipe("tile.wormhole", new ShapedOreRecipe(stack("tile.wormhole", 1), "00", '0', new ItemStack(Objects.wormhole, 1, 1)));
        configurableRecipe("tile.hugeTank", new ShapedOreRecipe(stack("tile.hugeTank", 1), "000", "010", "000", '0', stack("tile.tank", 1), '1', stack("EMatrix", 1)));
        configurableRecipe("tile.lightShaft", new ShapedOreRecipe(stack("tile.lightShaft", 4), "010", "232", "232", '0', stack("SiliconI", 1), '1', new ItemStack(Blocks.daylight_detector), '2', "dustGlowstone", '3', "blockGlass"));
        configurableRecipe("tile.fluidPacker", new ShapedOreRecipe(stack("tile.fluidPacker", 1), "000", "121", "343", '0', stack("tile.tank", 1), '1', stack("QAlloyI", 1), '2', "AIcasingGlass", '3', new ItemStack(Blocks.piston), '4', "AIcasingWood"));
        configurableRecipe("tile.fluidVent", new ShapedOreRecipe(stack("tile.fluidVent", 1), "111", "232", "040", '0', "ingotSilver", '1', new ItemStack(Blocks.iron_bars), '2', stack("SiliconI", 1), '3', new ItemStack(Blocks.piston), '4', "AIcasingGlass"));
        GameRegistry.addRecipe(new ShapedOreRecipe(stack("tile.gravCond", 1), "010", "234", "010", '0', stack("CoilSC", 1), '1', stack("tile.magnet", 1), '2', stack("tile.trash", 1), '3', "AIcasingGraphite", '4', "AIcasingWood"));
        configurableRecipe("tile.itemBuffer", new ShapedOreRecipe(stack("tile.itemBuffer", 1), " 0 ", "121", '0', stack("Circut", 1), '1', "AIcasingWood", '2', new ItemStack(Blocks.piston)));
        configurableRecipe("tile.quantumTank", new ShapedOreRecipe(stack("tile.quantumTank", 1), "202", "010", "202", '0', stack("tile.hugeTank", 1), '1', stack("QMatrix", 1), '2', stack("QAlloyI", 1)));
        configurableRecipe("tile.unbrGlass", new ShapedOreRecipe(stack("tile.unbrGlass", 32), "000", "010", "000", '0', "AIcasingGlass", '1', "AIcasingUnbr"));
        configurableRecipe("tile.vertShemGen", new ShapedOreRecipe(stack("tile.vertShemGen", 1), "010", "020", "333", '0', "dustGlowstone", '1', new ItemStack(Items.writable_book), '2', stack("Circut", 1), '3', new ItemStack(Blocks.stone_slab)));
        if (Config.data.getBoolean("recipe.tile.unbrStone", true)) this.UnbreakableStoneRecipes();
    }
    
    public void UnbreakableStoneRecipes()
    {
        for (int i = 0; i < 16; i++)
            OreDictionary.registerOre("blockUnbrStone", stack("unbrStone" + Integer.toHexString(i), 1));
        for (int i = 0; i < 16; i++)
        {
            GameRegistry.addRecipe(new ShapedOreRecipe(stack("unbrStone" + Integer.toHexString(i), 32), "010", "121", "010", '0', "AIcasingStone", '1', new ItemStack(Blocks.wool, 1, i), '2', "AIcasingUnbr"));
            GameRegistry.addRecipe(new ShapedOreRecipe(stack("unbrStone" + Integer.toHexString(i), 8), "000", "010", "000", '0', "blockUnbrStone", '1', dyes[~i & 15]));
        }
    }
    
    @SuppressWarnings("serial")
	private static class MissingNameEntry extends Exception {
    	final String entry;
    	public MissingNameEntry(String name) {
    		entry = name;
    	}
    }
    
    private FluidStack readFluidConfig(String cfg) throws MissingNameEntry
    {
    	int p = cfg.indexOf('*');
    	if (p <= 0) return null;
    	Fluid fluid = FluidRegistry.getFluid(cfg.substring(p + 1));
    	if (fluid == null) throw new MissingNameEntry(cfg.substring(p + 1));
    	try {return new FluidStack(fluid, Short.parseShort(cfg.substring(0, p)));
    	} catch (NumberFormatException e) {return null;}
    }
    
    private Object readItemConfig(String cfg, boolean asItem) throws MissingNameEntry
    {
    	int p = cfg.indexOf('*');
    	int n = 1;
    	if (p > 0) {
    		try {n = Short.parseShort(cfg.substring(0, p));} catch (NumberFormatException e){}
    		cfg = cfg.substring(p + 1);
    		if (n <= 0) n = 1;
    	}
    	if (cfg.isEmpty()) return null;
    	if ((p = cfg.indexOf(':')) > 0) {
    		String mod = cfg.substring(0, p);
    		cfg = cfg.substring(p + 1);
    		int dmg = -1;
    		if ((p = cfg.indexOf(':')) > 0) {
    			try {dmg = Short.parseShort(cfg.substring(p + 1));} catch (NumberFormatException e){}
        		cfg = cfg.substring(0, p);
    		}
    		ItemStack item = GameRegistry.makeItemStack(mod + ":" + cfg, dmg, n, null);
    		if (item == null) throw new MissingNameEntry(cfg);
    		if (dmg >= 0) item.setItemDamage(dmg);
    		return item;
    	} else if (asItem) {
    		List<ItemStack> list = OreDictionary.getOres(cfg);
    		if (list.isEmpty()) throw new MissingNameEntry(cfg);
        	ItemStack item = list.get(0).copy();
        	item.stackSize = n;
        	return item;
    	} else {
    		return new OreDictStack(cfg, n);
    	}
    }
    
    public void registerLiquidRecipes()
    {
    	OreDictionary.registerOre("gravel", Blocks.gravel);
        //blastFurnace
        if (enabled("recipe.steel")) AutomationRecipes.addRecipe(new LFRecipe(new FluidStack(Objects.L_lava, 40), new Object[]{"2*ingotIron", new ItemStack(Items.coal)}, null, new ItemStack[]{stack("SteelI", 2), new ItemStack(Blocks.stone)}, 800));
        if (enabled("recipe.lavaOreProcess")) {
        	AutomationRecipes.addRecipe(new LFRecipe(new FluidStack(Objects.L_lava, 20), new Object[]{"oreIron"}, null, new ItemStack[]{new ItemStack(Items.iron_ingot, 2), new ItemStack(Blocks.stone)}, 400));
        	AutomationRecipes.addRecipe(new LFRecipe(new FluidStack(Objects.L_lava, 20), new Object[]{"oreCopper"}, null, new ItemStack[]{stack("CopperI", 2), new ItemStack(Blocks.stone)}, 400));
            AutomationRecipes.addRecipe(new LFRecipe(new FluidStack(Objects.L_lava, 20), new Object[]{"oreGold"}, null, new ItemStack[]{new ItemStack(Items.gold_ingot, 2), new ItemStack(Blocks.stone)}, 400));
            AutomationRecipes.addRecipe(new LFRecipe(new FluidStack(Objects.L_lava, 20), new Object[]{"oreSilver"}, null, new ItemStack[]{stack("SilverI", 2), new ItemStack(Blocks.stone)}, 400));
        }
        AutomationRecipes.addRecipe(new LFRecipe(new FluidStack(Objects.L_lava, 40), new Object[]{"gemQuartz", new ItemStack(Items.coal)}, null, new ItemStack[]{stack("SiliconI", 1), new ItemStack(Blocks.stone)}, 800));
        AutomationRecipes.addRecipe(new LFRecipe(new FluidStack(Objects.L_lava, 40), new Object[]{"gemQuartz", new ItemStack(Items.coal, 1, 1)}, null, new ItemStack[]{stack("SiliconI", 1), new ItemStack(Blocks.stone)}, 800));
        AutomationRecipes.addRecipe(new LFRecipe(new FluidStack(Objects.L_lava, 125), new Object[]{new ItemStack(Items.coal, 4)}, null, new ItemStack[]{stack("Graphite", 1), new ItemStack(Blocks.stone, 2)}, 800));
        if (enabled("recipe.diamondBlock")) {
        	AutomationRecipes.addRecipe(new LFRecipe(new FluidStack(Objects.L_lava, 6000), new Object[]{stack("Graphite", 16), "8*gemDiamond"}, null, new ItemStack[]{new ItemStack(Blocks.diamond_block), new ItemStack(Blocks.stone, 96)}, 3200));
        	AutomationRecipes.addRecipe(new LFRecipe(new FluidStack(Objects.L_lava, 8000), new Object[]{stack("GDPlate", 6)}, null, new ItemStack[]{new ItemStack(Blocks.diamond_block), new ItemStack(Blocks.stone, 128)}, 20000));
        }
        //reaction
        AutomationRecipes.addRecipe(new LFRecipe(new FluidStack(Objects.L_antimatter, 2000), new Object[]{new ItemStack(Items.ender_pearl, 2), new ItemStack(Blocks.end_stone, 4), stack("HydrogenI", 1)}, null, new ItemStack[]{stack("QAlloyI", 1)}, 20000));
        if (enabled("recipe.charcoalToBonemeal")) AutomationRecipes.addRecipe(new LFRecipe(new FluidStack(Objects.L_oxygenG, 1000), new Object[]{new ItemStack(Items.coal, 1, 1)}, null, new ItemStack[]{new ItemStack(Items.dye, 1, 15)}, 50));
        //crushing
        if (enabled("recipe.cobblestone")) AutomationRecipes.addRecipe(new LFRecipe(new FluidStack(Objects.L_water, 10), new Object[]{"stone"}, new FluidStack(Objects.L_steam, 250), new ItemStack[]{new ItemStack(Blocks.cobblestone)}, 250));
        if (enabled("recipe.gravel")) AutomationRecipes.addRecipe(new LFRecipe(new FluidStack(Objects.L_water, 10), new Object[]{"cobblestone"}, new FluidStack(Objects.L_steam, 250), new ItemStack[]{new ItemStack(Blocks.gravel)}, 250));
        if (enabled("recipe.sand")) AutomationRecipes.addRecipe(new LFRecipe(new FluidStack(Objects.L_water, 10), new Object[]{"gravel"}, new FluidStack(Objects.L_steam, 250), new ItemStack[]{new ItemStack(Blocks.sand)}, 250));
        if (enabled("recipe.sandstone")) AutomationRecipes.addRecipe(new LFRecipe(new FluidStack(Objects.L_water, 20), new Object[]{"sandstone"}, new FluidStack(Objects.L_steam, 500), new ItemStack[]{new ItemStack(Blocks.sand, 4)}, 500));
        if (enabled("recipe.waterOreProcess")) {
        	AutomationRecipes.addRecipe(new LFRecipe(new FluidStack(Objects.L_water, 20), new Object[]{"oreCoal"}, new FluidStack(Objects.L_steam, 500), new ItemStack[]{new ItemStack(Items.coal, 2), new ItemStack(Blocks.gravel)}, 500));
            AutomationRecipes.addRecipe(new LFRecipe(new FluidStack(Objects.L_water, 40), new Object[]{"oreDiamond"}, new FluidStack(Objects.L_steam, 1000), new ItemStack[]{new ItemStack(Items.diamond, 2), new ItemStack(Blocks.gravel)}, 1000));
            AutomationRecipes.addRecipe(new LFRecipe(new FluidStack(Objects.L_water, 40), new Object[]{"oreEmerald"}, new FluidStack(Objects.L_steam, 1000), new ItemStack[]{new ItemStack(Items.emerald, 2), new ItemStack(Blocks.gravel)}, 1000));
            AutomationRecipes.addRecipe(new LFRecipe(new FluidStack(Objects.L_water, 40), new Object[]{"oreRedstone"}, new FluidStack(Objects.L_steam, 1000), new ItemStack[]{new ItemStack(Items.redstone, 8), new ItemStack(Blocks.gravel)}, 1000));
            AutomationRecipes.addRecipe(new LFRecipe(new FluidStack(Objects.L_water, 40), new Object[]{"oreLapis"}, new FluidStack(Objects.L_steam, 1000), new ItemStack[]{new ItemStack(Items.dye, 8, 4), new ItemStack(Blocks.gravel)}, 1000));
            AutomationRecipes.addRecipe(new LFRecipe(new FluidStack(Objects.L_water, 40), new Object[]{"oreQuartz"}, new FluidStack(Objects.L_steam, 1000), new ItemStack[]{new ItemStack(Items.quartz, 2), new ItemStack(Blocks.netherrack)}, 1000));
            AutomationRecipes.addRecipe(new LFRecipe(new FluidStack(Objects.L_water, 40), new Object[]{"oreIron"}, new FluidStack(Objects.L_steam, 1000), new ItemStack[]{stack("IronOD", 2), new ItemStack(Blocks.gravel)}, 1000));
            AutomationRecipes.addRecipe(new LFRecipe(new FluidStack(Objects.L_water, 40), new Object[]{"oreGold"}, new FluidStack(Objects.L_steam, 1000), new ItemStack[]{stack("GoldOD", 2), new ItemStack(Blocks.gravel)}, 1000));
            AutomationRecipes.addRecipe(new LFRecipe(new FluidStack(Objects.L_water, 40), new Object[]{"oreCopper"}, new FluidStack(Objects.L_steam, 1000), new ItemStack[]{stack("CopperOD", 2), new ItemStack(Blocks.gravel)}, 1000));
            AutomationRecipes.addRecipe(new LFRecipe(new FluidStack(Objects.L_water, 40), new Object[]{"oreSilver"}, new FluidStack(Objects.L_steam, 1000), new ItemStack[]{stack("SilverOD", 2), new ItemStack(Blocks.gravel)}, 1000));
            
        }
        //alloySmelter
        if (enabled("recipe.electrumIngot")) AutomationRecipes.addRecipe(new LFRecipe(null, new Object[]{"ingotSilver", "ingotGold"}, null, new ItemStack[]{stack("ElectrumI", 2)}, 250));
        AutomationRecipes.addRecipe(new LFRecipe(null, new Object[]{"ingotCopper", "dustRedstone"}, null, new ItemStack[]{stack("RstMetall", 2)}, 250));
        if (enabled("recipe.condAlloyIngot")) AutomationRecipes.addRecipe(new LFRecipe(null, new Object[]{"ingotSilver", "2*ingotIron", stack("RstMetall", 3)}, null, new ItemStack[]{stack("CondIA", 6)}, 1000));
        //cooking
        if (enabled("recipe.slimeball")) AutomationRecipes.addRecipe(new LFRecipe(new FluidStack(Objects.L_water, 10), new Object[]{new ItemStack(Items.sugar, 2), new ItemStack(Items.rotten_flesh), "dyeGreen"}, null, new ItemStack[]{new ItemStack(Items.slime_ball, 2)}, 320));
        //distillator
        AutomationRecipes.addRecipe(new LFRecipe(new FluidStack(Objects.L_biomass, 100), null, new FluidStack(Objects.L_steam, 2000), new ItemStack[]{stack("Biomass", 1)}, 500));
        if (enabled("recipe.stoneToLava")) AutomationRecipes.addRecipe(new LFRecipe(null, new Object[]{"stone"}, new FluidStack(Objects.L_lava, 100), null, 2000));
        if (enabled("recipe.blazedustToLava")) AutomationRecipes.addRecipe(new LFRecipe(null, new Object[]{new ItemStack(Items.blaze_powder)}, new FluidStack(Objects.L_lava, 100), null, 50));
        if (enabled("recipe.netherrackToLava")) AutomationRecipes.addRecipe(new LFRecipe(null, new Object[]{new ItemStack(Blocks.netherrack, 2)}, new FluidStack(Objects.L_lava, 100), null, 1500));
        //if (enabled("recipe.cheatedFire")) AutomationRecipes.addRecipe(new LFRecipe(null, new Object[]{new ItemStack(Items.fire_charge)}, null, new ItemStack[]{new ItemStack(Blocks.fire, 16)}, 1000));
        if (enabled("recipe.glowstoneToHelium")) AutomationRecipes.addRecipe(new LFRecipe(null, new Object[]{"glowstone"}, new FluidStack(Objects.L_heliumG, 4000), new ItemStack[]{new ItemStack(Items.redstone, 2), new ItemStack(Items.quartz, 2), new ItemStack(Items.gold_nugget, 3)}, 250));
        if (enabled("recipe.endstoneToHelium")) AutomationRecipes.addRecipe(new LFRecipe(null, new Object[]{new ItemStack(Blocks.end_stone, 4)}, new FluidStack(Objects.L_heliumG, 4000), new ItemStack[]{new ItemStack(Blocks.sand, 3), new ItemStack(Blocks.gravel, 1)}, 250));
        
        String[] inF = Config.data.getStringArray("rcp.AdvFurn.InF");
        String[] in1 = Config.data.getStringArray("rcp.AdvFurn.In1");
        String[] in2 = Config.data.getStringArray("rcp.AdvFurn.In2");
        String[] in3 = Config.data.getStringArray("rcp.AdvFurn.In3");
        String[] outF = Config.data.getStringArray("rcp.AdvFurn.OutF");
        String[] out1 = Config.data.getStringArray("rcp.AdvFurn.Out1");
        String[] out2 = Config.data.getStringArray("rcp.AdvFurn.Out2");
        String[] out3 = Config.data.getStringArray("rcp.AdvFurn.Out3");
        float[] energy = Config.data.getFloatArray("rcp.AdvFurn.Euse");
        if (inF.length != in1.length || in1.length != in2.length || in2.length != in3.length || in3.length != outF.length || outF.length != out1.length || out1.length != out2.length || out2.length != out3.length || out3.length != energy.length) {
        	FMLLog.log("Automation", Level.WARN, "Can not add any custom Advanced Furnace recipes: Different recipe amounts %d %d %d %d %d %d %d %d %d", inF.length, in1.length, in2.length, in3.length, outF.length, out1.length, out2.length, out3.length, energy.length);
        } else {
        	int n = inF.length;
        	int m1, m2;
        	Object item;
        	Object[] itemIn = new Object[3];
        	ItemStack[]	itemOut = new ItemStack[3];
        	for (int i = 0; i < n; i++) {
        		m1 = m2 = 0;
        		try {
	        		item = this.readItemConfig(in1[i], false);
	        		if (item != null) itemIn[m1++] = item;
	        		item = this.readItemConfig(in2[i], false);
	        		if (item != null) itemIn[m1++] = item;
	        		item = this.readItemConfig(in3[i], false);
	        		if (item != null) itemIn[m1++] = item;
	        		item = this.readItemConfig(out1[i], true);
	        		if (item != null) itemOut[m2++] = (ItemStack)item;
	        		item = this.readItemConfig(out2[i], true);
	        		if (item != null) itemOut[m2++] = (ItemStack)item;
	        		item = this.readItemConfig(out3[i], true);
	        		if (item != null) itemOut[m2++] = (ItemStack)item;
	        		AutomationRecipes.addRecipe(new LFRecipe(this.readFluidConfig(inF[i]), m1 == 0 ? null : Arrays.copyOf(itemIn, m1), this.readFluidConfig(outF[i]), m2 == 0 ? null : Arrays.copyOf(itemOut, m2), energy[i]));
        		} catch (MissingNameEntry e) {
        			FMLLog.log("Automation", Level.INFO, "Custom Advanced Furnace recipe %d not added: Object \"%s\" unavailable", i + 1, e.entry);
        		}
        	}
        }
    }
    
    public void registerCompressorRecipes()
    {
        //Casings
        if (enabled("recipe.WoodC")) AutomationRecipes.addCmpRecipe(stack("WoodC", 1), null, null, null, "4*plankWood");
        if (enabled("recipe.GlassC")) AutomationRecipes.addCmpRecipe(stack("GlassC", 1), null, null, "ingotCopper", "4*blockGlass");
        if (enabled("recipe.IronC")) AutomationRecipes.addCmpRecipe(stack("IronC", 1), null, null, null, "4*ingotIron");
        AutomationRecipes.addCmpRecipe(stack("SteelH", 1), null, null, null, "8*ingotSteel");
        AutomationRecipes.addCmpRecipe(stack("StoneC", 1), null, null, new ItemStack(Blocks.obsidian, 2), "6*stone");
        AutomationRecipes.addCmpRecipe(stack("GDPlate", 1), null, null, "gemDiamond", stack("Graphite", 8));
        AutomationRecipes.addCmpRecipe(stack("unbrC", 1), null, null, stack("DenseM", 1), new ItemStack(Blocks.bedrock));
        //Misc
        AutomationRecipes.addCmpRecipe(stack("Circut", 1), "4*dustRedstone", stack("SiliconI", 2), stack("RstMetall", 2), stack("Graphite", 1));
        AutomationRecipes.addCmpRecipe(stack("EMatrix", 1), stack("Circut", 1), "gemDiamond", new ItemStack(Items.ender_eye), "2*dustGlowstone");
        AutomationRecipes.addCmpRecipe(stack("QMatrix", 1), stack("EMatrix", 1), stack("QSGen", 1), stack("QAlloyI", 2), stack("LCHelium", 1));
        AutomationRecipes.addCmpRecipe(stack("Turbine", 1), null, "16*ingotSteel", null, stack("Motor", 1));
        AutomationRecipes.addCmpRecipe(stack("item.energyCell", 1), "2*gemLapis", "ingotElectrum", stack("RstMetall", 1), stack("Graphite", 1));
        if (enabled("recipe.tile.solarpanel")) AutomationRecipes.addCmpRecipe(stack("tile.solarpanel", 1), "4*paneGlass", "2*ingotElectrum", stack("RstMetall", 2), stack("SiliconI", 2));
        //Raw Material
        AutomationRecipes.addCmpRecipe(stack("RstMetall", 2), null, "ingotCopper", "2*dustRedstone", null);
        if (enabled("recipe.electrumDust")) AutomationRecipes.addCmpRecipe(stack("ElectrumR", 2), null, "ingotSilver", "ingotGold", null);
        if (enabled("recipe.condAlloyDust"))AutomationRecipes.addCmpRecipe(stack("CondRA", 6), "2*dustRedstone", "ingotElectrum", "2*ingotCopper", "2*ingotIron");
        //Recycling
        AutomationRecipes.addCmpRecipe(stack("CondIA", 3), stack("tile.wireA", 8));
        AutomationRecipes.addCmpRecipe(stack("GlassC", 1), stack("liquidPipeT", 8));
        AutomationRecipes.addCmpRecipe(stack("WoodC", 1), stack("itemPipeT", 8));
        AutomationRecipes.addCmpRecipe(stack("CopperI", 3), stack("tile.wireC", 8));
        if (enabled("recipe.ironbarsRecycling")) AutomationRecipes.addCmpRecipe(new ItemStack(Items.iron_ingot, 3), new ItemStack(Blocks.iron_bars, 8));
        if (enabled("recipe.glasspaneRecycling")) AutomationRecipes.addCmpRecipe(new ItemStack(Blocks.glass, 3), new ItemStack(Blocks.glass_pane, 8));
        if (enabled("recipe.glowstoneRecycling")) AutomationRecipes.addCmpRecipe(new ItemStack(Items.glowstone_dust, 4), "glowstone");
        if (enabled("recipe.quartzRecycling")) AutomationRecipes.addCmpRecipe(new ItemStack(Items.quartz, 4), "blockQuartz");
        if (enabled("recipe.boneCrushing")) AutomationRecipes.addCmpRecipe(new ItemStack(Items.dye, 6, 15), new ItemStack(Items.bone));
        if (enabled("recipe.blazeRodCrushing")) AutomationRecipes.addCmpRecipe(new ItemStack(Items.blaze_powder, 4), new ItemStack(Items.blaze_rod));
        if (enabled("recipe.gravelToFlint")) AutomationRecipes.addCmpRecipe(new ItemStack(Items.flint), "gravel");
        
        String[] in0 = Config.data.getStringArray("rcp.CmpAss.In0");
        String[] in1 = Config.data.getStringArray("rcp.CmpAss.In1");
        String[] in2 = Config.data.getStringArray("rcp.CmpAss.In2");
        String[] in3 = Config.data.getStringArray("rcp.CmpAss.In3");
        String[] out = Config.data.getStringArray("rcp.CmpAss.Out");
        if (in0.length != in1.length || in1.length != in2.length || in2.length != in3.length || in3.length != out.length) {
        	FMLLog.log("Automation", Level.WARN, "Can not add any custom Compression Assembler recipes: Different recipe amounts %d %d %d %d %d", in0.length, in1.length, in2.length, in3.length, out.length);
        } else {
        	int n = in0.length;
        	Object item;
        	for (int i = 0; i < n; i++) {
        		try {
        			item = this.readItemConfig(out[i], true);
	        		if (item != null) AutomationRecipes.addCmpRecipe((ItemStack)item, this.readItemConfig(in0[i], false), this.readItemConfig(in1[i], false), this.readItemConfig(in2[i], false), this.readItemConfig(in3[i], false));
        		} catch (MissingNameEntry e) {
        			FMLLog.log("Automation", Level.INFO, "Custom Compression Assembler recipe %d not added: Object \"%s\" unavailable", i + 1, e.entry);
        		}
        	}
        }
    }
    
    public void registerCoolerRecipes()
    {
        if (enabled("recipe.snowball")) AutomationRecipes.addRecipe(new CoolRecipe(new FluidStack(Objects.L_water, 40), new FluidStack(Objects.L_steam, 8000), new FluidStack(Objects.L_steam, 8000), new ItemStack(Items.snowball, 1), 20));
        if (enabled("recipe.iceblock")) AutomationRecipes.addRecipe(new CoolRecipe(new FluidStack(Objects.L_water, 40), new FluidStack(Objects.L_steam, 8000), new FluidStack(Objects.L_water, 1000), new ItemStack(Blocks.ice, 1), 1600));
        AutomationRecipes.addRecipe(new CoolRecipe(new FluidStack(Objects.L_water, 40), new FluidStack(Objects.L_steam, 8000), new FluidStack(Objects.L_nitrogenG, 6400), new FluidStack(Objects.L_nitrogenL, 10), 1800));
        AutomationRecipes.addRecipe(new CoolRecipe(new FluidStack(Objects.L_water, 40), new FluidStack(Objects.L_steam, 8000), new FluidStack(Objects.L_oxygenG, 8000), new FluidStack(Objects.L_oxygenL, 10), 1800));
        AutomationRecipes.addRecipe(new CoolRecipe(new FluidStack(Objects.L_nitrogenL, 10), new FluidStack(Objects.L_nitrogenG, 6400), new FluidStack(Objects.L_heliumG, 4000), new FluidStack(Objects.L_heliumL, 5), 200));
        AutomationRecipes.addRecipe(new CoolRecipe(new FluidStack(Objects.L_nitrogenL, 5), new FluidStack(Objects.L_nitrogenG, 3200), new FluidStack(Objects.L_hydrogenG, 4000), new FluidStack(Objects.L_hydrogenL, 5), 100));
        AutomationRecipes.addRecipe(new CoolRecipe(new FluidStack(Objects.L_heliumL, 10), new FluidStack(Objects.L_heliumG, 8000), new FluidStack(Objects.L_hydrogenL, 100), stack("HydrogenI", 1), 200));
        AutomationRecipes.addRecipe(new CoolRecipe(new FluidStack(Objects.L_hydrogenL, 5), new FluidStack(Objects.L_hydrogenG, 4000), new FluidStack(Objects.L_nitrogenG, 6400), new FluidStack(Objects.L_nitrogenL, 10), 100));
        
        String[] inC = Config.data.getStringArray("rcp.Cool.InC");
        String[] inM = Config.data.getStringArray("rcp.Cool.InM");
        String[] outC = Config.data.getStringArray("rcp.Cool.OutC");
        String[] outM = Config.data.getStringArray("rcp.Cool.OutM");
        float[] energy = Config.data.getFloatArray("rcp.Cool.Euse");
        if (inC.length != inM.length || inM.length != outC.length || outC.length != outM.length || outM.length != energy.length) {
        	FMLLog.log("Automation", Level.WARN, "Can not add any custom Decompression Cooler recipes: Different recipe amounts %d %d %d %d %d", inC.length, inM.length, outC.length, outM.length, energy.length);
        } else {
        	int n = inM.length;
        	Object[] items = new Object[4];
        	for (int i = 0; i < n; i++) {
        		try {
	        		try{items[0] = this.readFluidConfig(inC[i]);} catch (MissingNameEntry e) {items[0] = null;}
	        		if (items[0] == null) items[0] = this.readItemConfig(inC[i], true);
	        		try{items[1] = this.readFluidConfig(inM[i]);} catch (MissingNameEntry e) {items[1] = null;}
	        		if (items[1] == null) items[1] = this.readItemConfig(inM[i], true);
	        		try{items[2] = this.readFluidConfig(outC[i]);} catch (MissingNameEntry e) {items[2] = null;}
	        		if (items[2] == null) items[2] = this.readItemConfig(outC[i], true);
	        		try{items[3] = this.readFluidConfig(outM[i]);} catch (MissingNameEntry e) {items[3] = null;}
	        		if (items[3] == null) items[3] = this.readItemConfig(outM[i], true);
	        		AutomationRecipes.addRecipe(new CoolRecipe(items[0], items[2], items[1], items[3], energy[i]));
        		} catch (MissingNameEntry e) {
        			FMLLog.log("Automation", Level.INFO, "Custom Decompression Cooler recipe %d not added: Object \"%s\" unavailable", i + 1, e.entry);
        		}
        	}
        }
    }
    
    public void registerElectrolyserRecipes()
    {
    	if (enabled("recipe.waterToHydrogen")) AutomationRecipes.addRecipe(new ElRecipe(new FluidStack(Objects.L_water, 5), new FluidStack(Objects.L_hydrogenG, 2000), new FluidStack(Objects.L_oxygenG, 1000), 2400));
    	if (enabled("recipe.steamToHydrogen")) AutomationRecipes.addRecipe(new ElRecipe(new FluidStack(Objects.L_steam, 2000), new FluidStack(Objects.L_hydrogenG, 2000), new FluidStack(Objects.L_oxygenG, 1000), 1800));
    	if (enabled("recipe.biomassToHydrogen"))AutomationRecipes.addRecipe(new ElRecipe(stack("Biomass", 1), new FluidStack(Objects.L_hydrogenG, 8000), new ItemStack(Items.coal, 1, 1), 1200));
    	if (enabled("recipe.electrolyserOreProcess")) {
    		AutomationRecipes.addRecipe(new ElRecipe(stack("IronOD", 2), stack("IronD", 3), new FluidStack(Objects.L_oxygenG, 2000), 1000));
    		AutomationRecipes.addRecipe(new ElRecipe(stack("GoldOD", 2), stack("GoldD", 3), new FluidStack(Objects.L_oxygenG, 2000), 1000));
            AutomationRecipes.addRecipe(new ElRecipe(stack("CopperOD", 2), stack("CopperD", 3), new FluidStack(Objects.L_oxygenG, 2000), 1000));
        	AutomationRecipes.addRecipe(new ElRecipe(stack("SilverOD", 2), stack("SilverD", 3), new FluidStack(Objects.L_oxygenG, 2000), 1000));
    	}
    	
    	String[] in = Config.data.getStringArray("rcp.Electr.In");
        String[] out0 = Config.data.getStringArray("rcp.Electr.Out-");
        String[] out1 = Config.data.getStringArray("rcp.Electr.Out+");
        float[] energy = Config.data.getFloatArray("rcp.Electr.Euse");
        if (in.length != out0.length || out0.length != out1.length || out1.length != energy.length) {
        	FMLLog.log("Automation", Level.WARN, "Can not add any custom Electrolyser recipes: Different recipe amounts %d %d %d %d", in.length, out0.length, out1.length, energy.length);
        } else {
        	int n = in.length;
        	Object[] items = new Object[3];
        	for (int i = 0; i < n; i++) {
        		try {
	        		try{items[0] = this.readFluidConfig(in[i]);} catch (MissingNameEntry e) {items[0] = null;}
	        		if (items[0] == null) items[0] = this.readItemConfig(in[i], true);
	        		try{items[1] = this.readFluidConfig(out0[i]);} catch (MissingNameEntry e) {items[1] = null;}
	        		if (items[1] == null) items[1] = this.readItemConfig(out0[i], true);
	        		try{items[2] = this.readFluidConfig(out1[i]);} catch (MissingNameEntry e) {items[2] = null;}
	        		if (items[2] == null) items[2] = this.readItemConfig(out1[i], true);
	        		AutomationRecipes.addRecipe(new ElRecipe(items[0], items[1], items[2], energy[i]));
        		} catch (MissingNameEntry e) {
        			FMLLog.log("Automation", Level.INFO, "Custom Electrolyser recipe %d not added: Object \"%s\" unavailable", i + 1, e.entry);
        		}
        	}
        }
    }
    
    public void registerTrashRecipes()
    {
    	if (enabled("recipe.bedrock")) AutomationRecipes.addRecipe(new GCRecipe(new ItemStack(Blocks.bedrock), new ItemStack(Blocks.diamond_block, 1), Config.data.getInt("rcp.GravCond.mBedrock", 40960000)));
    	if (enabled("recipe.obsidian")) AutomationRecipes.addRecipe(new GCRecipe(new ItemStack(Blocks.obsidian, 1), new ItemStack(Blocks.netherrack, 4), Config.data.getInt("rcp.GravCond.mObsidian", 24000)));
    	AutomationRecipes.addRecipe(new GCRecipe(stack("DenseM", 1), new ItemStack(Blocks.obsidian, 16), Config.data.getInt("rcp.GravCond.mDenseM", 512000)));
    	AutomationRecipes.addRecipe(new GCRecipe(stack("Neutron", 1), stack("DenseM", 4), Config.data.getInt("rcp.GravCond.mNeutron", 6144000)));
    	AutomationRecipes.addRecipe(new GCRecipe(stack("BlackHole", 1), stack("Neutron", 1), Config.data.getInt("rcp.GravCond.mBHole", 24576000)));
    }
}
