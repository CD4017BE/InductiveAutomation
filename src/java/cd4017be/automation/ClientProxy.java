/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cd4017be.automation;

import cd4017be.automation.TileEntity.AntimatterBomb;
import cd4017be.automation.TileEntity.Builder;
import cd4017be.automation.TileEntity.Farm;
import cd4017be.automation.TileEntity.Miner;
import cd4017be.automation.TileEntity.Pump;
import cd4017be.automation.TileEntity.Shaft;
import cd4017be.automation.TileEntity.Tank;
import cd4017be.automation.TileEntity.Teleporter;
import cd4017be.automation.TileEntity.VertexShematicGen;
import cd4017be.automation.jetpack.TickHandler;
import cd4017be.automation.render.FluidTextures;
import cd4017be.automation.render.MaterialTextures;
import cd4017be.automation.render.Render3DVertexShem;
import cd4017be.automation.render.ShaftRenderer;
import cd4017be.automation.render.TileEntityAntimatterBombRenderer;
import cd4017be.automation.render.TileEntityTankRenderer;
import cd4017be.lib.BlockItemRegistry;
import cd4017be.lib.ClientInputHandler;
import cd4017be.lib.TileBlockRegistry;
import cd4017be.lib.TooltipInfo;
import cd4017be.lib.render.ModelPipe;
import cd4017be.lib.render.SpecialModelLoader;
import cd4017be.lib.render.SelectionRenderer;
import net.minecraft.util.BlockRenderLayer;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import cd4017be.automation.Gui.*;
import static cd4017be.automation.Objects.*;

/**
 *
 * @author CD4017BE
 */
public class ClientProxy extends CommonProxy
{
    
    @Override
    public void registerRenderers()
    {
    	TickHandler.init();
        ClientInputHandler.init();
        TooltipInfo.addConfigReference(Config.data);
    	this.registerAdditionalModels();
    	//BlockItems
    	BlockItemRegistry.registerBlockRender("ore:0");
    	BlockItemRegistry.registerBlockRender("ore:1");
    	BlockItemRegistry.registerBlockRender("pool:0");
    	BlockItemRegistry.registerBlockRender("unbrStone:0");
    	BlockItemRegistry.registerBlockRender("unbrStone:1");
    	BlockItemRegistry.registerBlockRender("unbrStone:2");
    	BlockItemRegistry.registerBlockRender("unbrStone:3");
    	BlockItemRegistry.registerBlockRender("unbrStone:4");
    	BlockItemRegistry.registerBlockRender("unbrStone:5");
    	BlockItemRegistry.registerBlockRender("unbrStone:6");
    	BlockItemRegistry.registerBlockRender("unbrStone:7");
    	BlockItemRegistry.registerBlockRender("unbrStone:8");
    	BlockItemRegistry.registerBlockRender("unbrStone:9");
    	BlockItemRegistry.registerBlockRender("unbrStone:10");
    	BlockItemRegistry.registerBlockRender("unbrStone:11");
    	BlockItemRegistry.registerBlockRender("unbrStone:12");
    	BlockItemRegistry.registerBlockRender("unbrStone:13");
    	BlockItemRegistry.registerBlockRender("unbrStone:14");
    	BlockItemRegistry.registerBlockRender("unbrStone:15");
    	BlockItemRegistry.registerBlockRender("unbrGlass:0");
    	BlockItemRegistry.registerBlockRender("solarpanel:0");
    	BlockItemRegistry.registerBlockRender("teslaTransmitterLV:0");
    	BlockItemRegistry.registerBlockRender("teslaTransmitter:0");
    	BlockItemRegistry.registerBlockRender("wormhole:0");
    	BlockItemRegistry.registerBlockRender("wormhole:1");
        BlockItemRegistry.registerBlockRender("lightShaft:0");
		BlockItemRegistry.registerBlockRender("wireC:0");
		BlockItemRegistry.registerBlockRender("wireA:0");
		BlockItemRegistry.registerBlockRender("wireH:0");
		BlockItemRegistry.registerBlockRender("liquidPipe:0");
		BlockItemRegistry.registerBlockRender("liquidPipe:1");
		BlockItemRegistry.registerBlockRender("liquidPipe:2");
		BlockItemRegistry.registerBlockRender("itemPipe:0");
		BlockItemRegistry.registerBlockRender("itemPipe:1");
		BlockItemRegistry.registerBlockRender("itemPipe:2");
		BlockItemRegistry.registerBlockRender("warpPipe:0");
		BlockItemRegistry.registerBlockRender("voltageTransformer:0");
		BlockItemRegistry.registerBlockRender("SCSU:0");
		BlockItemRegistry.registerBlockRender("OCSU:0");
		BlockItemRegistry.registerBlockRender("CCSU:0");
		BlockItemRegistry.registerBlockRender("steamEngine:0");
		BlockItemRegistry.registerBlockRender("steamTurbine:0");
		BlockItemRegistry.registerBlockRender("steamGenerator:0");
		BlockItemRegistry.registerBlockRender("steamBoiler:0");
		BlockItemRegistry.registerBlockRender("lavaCooler:0");
		BlockItemRegistry.registerBlockRender("energyFurnace:0");
		BlockItemRegistry.registerBlockRender("farm:0");
		BlockItemRegistry.registerBlockRender("miner:0");
		BlockItemRegistry.registerBlockRender("magnet:0");
		BlockItemRegistry.registerBlockRender("link:0");
		BlockItemRegistry.registerBlockRender("linkHV:0");
		BlockItemRegistry.registerBlockRender("texMaker:0");
		BlockItemRegistry.registerBlockRender("builder:0");
		BlockItemRegistry.registerBlockRender("algaePool:0");
		BlockItemRegistry.registerBlockRender("teleporter:0");
		BlockItemRegistry.registerBlockRender("advancedFurnace:0");
		BlockItemRegistry.registerBlockRender("pump:0");
		BlockItemRegistry.registerBlockRender("massstorageChest:0");
		BlockItemRegistry.registerBlockRender("matterOrb:0");
		BlockItemRegistry.registerBlockRender("antimatterBombE:0");
		BlockItemRegistry.registerBlockRender("antimatterBombF:0");
		BlockItemRegistry.registerBlockRender("antimatterTank:0");
		BlockItemRegistry.registerBlockRender("antimatterFabricator:0");
		BlockItemRegistry.registerBlockRender("antimatterAnihilator:0");
		BlockItemRegistry.registerBlockRender("hpSolarpanel:0");
		BlockItemRegistry.registerBlockRender("autoCrafting:0");
		BlockItemRegistry.registerBlockRender("geothermalFurnace:0");
		BlockItemRegistry.registerBlockRender("steamCompressor:0");
		BlockItemRegistry.registerBlockRender("electricCompressor:0");
		BlockItemRegistry.registerBlockRender("tank:0");
		BlockItemRegistry.registerBlockRender("security:0");
		BlockItemRegistry.registerBlockRender("decompCooler:0");
		BlockItemRegistry.registerBlockRender("collector:0");
		BlockItemRegistry.registerBlockRender("trash:0");
		BlockItemRegistry.registerBlockRender("electrolyser:0");
		BlockItemRegistry.registerBlockRender("fuelCell:0");
		BlockItemRegistry.registerBlockRender("detector:0");
		BlockItemRegistry.registerBlockRender("itemSorter:0");
		BlockItemRegistry.registerBlockRender("matterInterfaceB:0");
		BlockItemRegistry.registerBlockRender("fluidPacker:0");
		BlockItemRegistry.registerBlockRender("hugeTank:0");
		BlockItemRegistry.registerBlockRender("fluidVent:0");
		BlockItemRegistry.registerBlockRender("gravCond:0");
		BlockItemRegistry.registerBlockRender("itemBuffer:0");
		BlockItemRegistry.registerBlockRender("quantumTank:0");
		BlockItemRegistry.registerBlockRender("vertShemGen:0");
		BlockItemRegistry.registerBlockRender("heatRadiator:0");
		BlockItemRegistry.registerBlockRender("shaft:0");
		BlockItemRegistry.registerBlockRender("electricCoilC:0");
		BlockItemRegistry.registerBlockRender("electricCoilA:0");
		BlockItemRegistry.registerBlockRender("electricCoilH:0");
		//Items
        BlockItemRegistry.registerItemRender(material, new MaterialTextures("automation:"));
        BlockItemRegistry.registerItemRender("fluidDummy");
        BlockItemRegistry.registerItemRender("selectionTool");
    	BlockItemRegistry.registerItemRender("voltMeter");
    	BlockItemRegistry.registerItemRender("energyCell");
    	BlockItemRegistry.registerItemRender("chisle");
    	BlockItemRegistry.registerItemRender("cutter");
    	BlockItemRegistry.registerItemRender("portableMagnet");
    	BlockItemRegistry.registerItemRender("builderTexture");
    	BlockItemRegistry.registerItemRender("teleporterCoords");
    	BlockItemRegistry.registerItemRender("stoneDrill");
    	BlockItemRegistry.registerItemRender("ironDrill");
    	BlockItemRegistry.registerItemRender("diamondDrill");
    	BlockItemRegistry.registerItemRender("amLaser");
    	BlockItemRegistry.registerItemRender("mCannon");
    	BlockItemRegistry.registerItemRender("contLiquidAir");
    	BlockItemRegistry.registerItemRender("contAlgaeFood");
    	BlockItemRegistry.registerItemRender("contInvEnergy");
    	BlockItemRegistry.registerItemRender("contJetFuel");
    	BlockItemRegistry.registerItemRender("jetpack");
    	BlockItemRegistry.registerItemRender("jetpackIron");
    	BlockItemRegistry.registerItemRender("jetpackSteel");
    	BlockItemRegistry.registerItemRender("jetpackGraphite");
    	BlockItemRegistry.registerItemRender("jetpackUnbr");
    	BlockItemRegistry.registerItemRender("matterInterface");
    	BlockItemRegistry.registerItemRender("fluidUpgrade");
    	BlockItemRegistry.registerItemRender("itemUpgrade");
    	BlockItemRegistry.registerItemRender("portableFurnace");
    	BlockItemRegistry.registerItemRender("portableInventory");
    	BlockItemRegistry.registerItemRender("portableCrafter");
    	BlockItemRegistry.registerItemRender("portableGenerator");
    	BlockItemRegistry.registerItemRender("portableRemoteInv");
    	BlockItemRegistry.registerItemRender("portableTeleporter");
    	BlockItemRegistry.registerItemRender("portablePump");
    	BlockItemRegistry.registerItemRender("translocator");
    	BlockItemRegistry.registerItemRender("portableTesla");
    	BlockItemRegistry.registerItemRender("placement");
    	BlockItemRegistry.registerItemRender("synchronizer");
    	BlockItemRegistry.registerItemRender("remBlockType");
    	BlockItemRegistry.registerItemRender("vertexSel");
    	//Tiles
    	ClientRegistry.bindTileEntitySpecialRenderer(Miner.class, new SelectionRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(Builder.class, new SelectionRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(Farm.class, new SelectionRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(Pump.class, new SelectionRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(Teleporter.class, new SelectionRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(Tank.class, new TileEntityTankRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(AntimatterBomb.class, new TileEntityAntimatterBombRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(VertexShematicGen.class, new Render3DVertexShem());
        ClientRegistry.bindTileEntitySpecialRenderer(Shaft.class, new ShaftRenderer());
        SpecialModelLoader.registerTESRModel("automation:models/tileEntity/shaft");
        SpecialModelLoader.registerTESRModel("automation:models/tileEntity/shaftPermMag");
        SpecialModelLoader.registerTESRModel("automation:models/tileEntity/shaftCoilC");
        SpecialModelLoader.registerTESRModel("automation:models/tileEntity/shaftCoilA");
        SpecialModelLoader.registerTESRModel("automation:models/tileEntity/shaftCoilH");
        SpecialModelLoader.registerTESRModel(TileEntityTankRenderer.model);
    }
    
    private void registerAdditionalModels()
    {
    	BlockItemRegistry.registerModels(itemPipe, "itemPipe", "itemPipe_1", "itemPipe_2");
    	BlockItemRegistry.registerModels(liquidPipe, "liquidPipe", "liquidPipe_1", "liquidPipe_2");
    	BlockItemRegistry.registerModels(ore, "ore", "ore_1");
    	String[] tex = new String[16];
    	for (int i = 0; i < tex.length; i++) tex[i] = "unbrStone" + (i == 0 ? "" : "_" + i);
    	BlockItemRegistry.registerModels(unbrStone, tex);
    	BlockItemRegistry.registerModels(wormhole, "wormhole", "wormhole_1");
    }

    @Override
    public void registerBlocks()
    {
    	super.registerBlocks();
    	//register GUIs
        TileBlockRegistry.registerGui(steamEngine, GuiSteamEngine.class);
        TileBlockRegistry.registerGui(steamGenerator, GuiSteamGenerator.class);
        TileBlockRegistry.registerGui(steamBoiler, GuiSteamBoiler.class);
        TileBlockRegistry.registerGui(lavaCooler, GuiLavaCooler.class);
        TileBlockRegistry.registerGui(energyFurnace, GuiEnergyFurnace.class);
        TileBlockRegistry.registerGui(SCSU, GuiESU.class);
        TileBlockRegistry.registerGui(OCSU, GuiESU.class);
        TileBlockRegistry.registerGui(CCSU, GuiESU.class);
        TileBlockRegistry.registerGui(farm, GuiFarm.class);
        TileBlockRegistry.registerGui(miner, GuiMiner.class);
        TileBlockRegistry.registerGui(link, GuiELink.class);
        TileBlockRegistry.registerGui(linkHV, GuiELink.class);
        TileBlockRegistry.registerGui(texMaker, GuiTextureMaker.class);
        TileBlockRegistry.registerGui(builder, GuiBuilder.class);
        TileBlockRegistry.registerGui(algaePool, GuiAlgaePool.class);
        TileBlockRegistry.registerGui(voltageTransformer, GuiVoltageTransformer.class);
        TileBlockRegistry.registerGui(teslaTransmitter, GuiTeslaTransmitter.class);
        TileBlockRegistry.registerGui(teslaTransmitterLV, GuiTeslaTransmitterLV.class);
        TileBlockRegistry.registerGui(teleporter, GuiTeleporter.class);
        TileBlockRegistry.registerGui(advancedFurnace, GuiAdvancedFurnace.class);
        TileBlockRegistry.registerGui(pump, GuiPump.class);
        TileBlockRegistry.registerGui(steamTurbine, GuiSteamTurbine.class);
        TileBlockRegistry.registerGui(massstorageChest, GuiMassstorageChest.class);
        TileBlockRegistry.registerGui(antimatterTank, GuiAntimatterTank.class);
        TileBlockRegistry.registerGui(antimatterFabricator, GuiAntimatterFabricator.class);
        TileBlockRegistry.registerGui(antimatterAnihilator, GuiAntimatterAnihilator.class);
        TileBlockRegistry.registerGui(autoCrafting, GuiAutoCrafting.class);
        TileBlockRegistry.registerGui(geothermalFurnace, GuiGeothermalFurnace.class);
        TileBlockRegistry.registerGui(steamCompressor, GuiSteamCompressor.class);
        TileBlockRegistry.registerGui(electricCompressor, GuiElectricCompressor.class);
        TileBlockRegistry.registerGui(tank, GuiTank.class);
        TileBlockRegistry.registerGui(security, GuiSecuritySys.class);
        TileBlockRegistry.registerGui(decompCooler, GuiDecompCooler.class);
        TileBlockRegistry.registerGui(collector, GuiCollector.class);
        TileBlockRegistry.registerGui(trash, GuiTrash.class);
        TileBlockRegistry.registerGui(electrolyser, GuiElectrolyser.class);
        TileBlockRegistry.registerGui(fuelCell, GuiFuelCell.class);
        TileBlockRegistry.registerGui(detector, GuiDetector.class);
        TileBlockRegistry.registerGui(itemSorter, GuiItemSorter.class);
        TileBlockRegistry.registerGui(matterInterfaceB, GuiMatterInterface.class);
        TileBlockRegistry.registerGui(fluidPacker, GuiFluidPacker.class);
        TileBlockRegistry.registerGui(hugeTank, GuiHugeTank.class);
        TileBlockRegistry.registerGui(fluidVent, GuiFluidVent.class);
        TileBlockRegistry.registerGui(gravCond, GuiGraviCond.class);
        TileBlockRegistry.registerGui(itemBuffer, GuiItemBuffer.class);
        TileBlockRegistry.registerGui(quantumTank, GuiQuantumTank.class);
        TileBlockRegistry.registerGui(vertShemGen, GuiVertexShematicGen.class);
        TileBlockRegistry.registerGui(heatRadiator, GuiHeatRadiator.class);
        TileBlockRegistry.registerGui(electricCoilC, GuiElectricCoil.class);
        TileBlockRegistry.registerGui(electricCoilA, GuiElectricCoil.class);
        TileBlockRegistry.registerGui(electricCoilH, GuiElectricCoil.class);
        //set block transparencies
        Objects.itemPipe.setBlockLayer(BlockRenderLayer.CUTOUT);
        Objects.liquidPipe.setBlockLayer(BlockRenderLayer.CUTOUT);
        Objects.warpPipe.setBlockLayer(BlockRenderLayer.CUTOUT);
        Objects.tank.setBlockLayer(BlockRenderLayer.CUTOUT);
        Objects.hugeTank.setBlockLayer(BlockRenderLayer.CUTOUT);
        Objects.quantumTank.setBlockLayer(BlockRenderLayer.CUTOUT);
        Objects.pool.setBlockLayer(BlockRenderLayer.CUTOUT);
        Objects.wormhole.setBlockLayer(BlockRenderLayer.TRANSLUCENT);
        //fluids
        SpecialModelLoader.setMod("automation");
        SpecialModelLoader.registerFluid(L_steam);
        SpecialModelLoader.registerFluid(L_waterG);
        SpecialModelLoader.registerFluid(L_biomass);
        SpecialModelLoader.registerFluid(L_antimatter);
        SpecialModelLoader.registerFluid(L_nitrogenG);
        SpecialModelLoader.registerFluid(L_nitrogenL);
        SpecialModelLoader.registerFluid(L_hydrogenG);
        SpecialModelLoader.registerFluid(L_hydrogenL);
        SpecialModelLoader.registerFluid(L_heliumG);
        SpecialModelLoader.registerFluid(L_heliumL);
        SpecialModelLoader.registerFluid(L_oxygenG);
        SpecialModelLoader.registerFluid(L_oxygenL);
        //pipe models
    	SpecialModelLoader.registerBlockModel(Objects.itemPipe, new ModelPipe("automation:itemPipe", 3, 3));
        SpecialModelLoader.registerBlockModel(Objects.liquidPipe, new ModelPipe("automation:liquidPipe", 3, 3));
        SpecialModelLoader.registerBlockModel(Objects.wireC, new ModelPipe("automation:wireC", 1, 1));
        SpecialModelLoader.registerBlockModel(Objects.wireA, new ModelPipe("automation:wireA", 1, 1));
        SpecialModelLoader.registerBlockModel(Objects.wireH, new ModelPipe("automation:wireH", 1, 1));
        SpecialModelLoader.registerBlockModel(Objects.warpPipe, new ModelPipe("automation:warpPipe", 1, 5));
        SpecialModelLoader.registerBlockModel(Objects.shaft, new ModelPipe("automation:shaft", 0, 1));
        SpecialModelLoader.registerItemModel(Objects.fluidDummy, new FluidTextures());
    }
    
}
