/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cd4017be.automation;

import cd4017be.api.automation.SelectionRenderer;
import cd4017be.automation.Entity.EntityAntimatterExplosion1;
import cd4017be.automation.Gui.GuiAdvancedFurnace;
import cd4017be.automation.Gui.GuiAlgaePool;
import cd4017be.automation.Gui.GuiAntimatterAnihilator;
import cd4017be.automation.Gui.GuiAntimatterFabricator;
import cd4017be.automation.Gui.GuiAntimatterTank;
import cd4017be.automation.Gui.GuiAutoCrafting;
import cd4017be.automation.Gui.GuiBuilder;
import cd4017be.automation.Gui.GuiCollector;
import cd4017be.automation.Gui.GuiDecompCooler;
import cd4017be.automation.Gui.GuiDetector;
import cd4017be.automation.Gui.GuiELink;
import cd4017be.automation.Gui.GuiESU;
import cd4017be.automation.Gui.GuiElectricCompressor;
import cd4017be.automation.Gui.GuiElectrolyser;
import cd4017be.automation.Gui.GuiEnergyFurnace;
import cd4017be.automation.Gui.GuiFarm;
import cd4017be.automation.Gui.GuiFluidPacker;
import cd4017be.automation.Gui.GuiFluidVent;
import cd4017be.automation.Gui.GuiFuelCell;
import cd4017be.automation.Gui.GuiGeothermalFurnace;
import cd4017be.automation.Gui.GuiGraviCond;
import cd4017be.automation.Gui.GuiHugeTank;
import cd4017be.automation.Gui.GuiItemBuffer;
import cd4017be.automation.Gui.GuiItemSorter;
import cd4017be.automation.Gui.GuiLavaCooler;
import cd4017be.automation.Gui.GuiMassstorageChest;
import cd4017be.automation.Gui.GuiMatterInterface;
import cd4017be.automation.Gui.GuiMiner;
import cd4017be.automation.Gui.GuiPump;
import cd4017be.automation.Gui.GuiQuantumTank;
import cd4017be.automation.Gui.GuiSecuritySys;
import cd4017be.automation.Gui.GuiSteamBoiler;
import cd4017be.automation.Gui.GuiSteamCompressor;
import cd4017be.automation.Gui.GuiSteamEngine;
import cd4017be.automation.Gui.GuiSteamGenerator;
import cd4017be.automation.Gui.GuiSteamTurbine;
import cd4017be.automation.Gui.GuiTank;
import cd4017be.automation.Gui.GuiTeleporter;
import cd4017be.automation.Gui.GuiTeslaTransmitter;
import cd4017be.automation.Gui.GuiTeslaTransmitterLV;
import cd4017be.automation.Gui.GuiTextureMaker;
import cd4017be.automation.Gui.GuiTrash;
import cd4017be.automation.Gui.GuiVertexShematicGen;
import cd4017be.automation.Gui.GuiVoltageTransformer;
import cd4017be.automation.TileEntity.AntimatterBomb;
import cd4017be.automation.TileEntity.Builder;
import cd4017be.automation.TileEntity.Farm;
import cd4017be.automation.TileEntity.Miner;
import cd4017be.automation.TileEntity.Pump;
import cd4017be.automation.TileEntity.Tank;
import cd4017be.automation.TileEntity.Teleporter;
import cd4017be.automation.TileEntity.VertexShematicGen;
import cd4017be.automation.jetpack.TickHandler;
import cd4017be.automation.render.Render3DVertexShem;
import cd4017be.automation.render.RenderAntimatterBomb;
import cd4017be.automation.render.TileEntityAntimatterBombRenderer;
import cd4017be.automation.render.TileEntityTankRenderer;
import cd4017be.lib.BlockItemRegistry;
import cd4017be.lib.ClientInputHandler;
import cd4017be.lib.TileBlock;
import cd4017be.lib.TileBlockRegistry;
import cd4017be.lib.TooltipInfo;
import static cd4017be.lib.BlockItemRegistry.blockId;
import cd4017be.lib.templates.PipeRenderer;
import cpw.mods.fml.client.registry.RenderingRegistry;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;


/**
 *
 * @author CD4017BE
 */
public class ClientProxy extends CommonProxy
{
    
    
    @Override
    public void registerRenderers()
    {
        
        wireRenderer = new PipeRenderer();
        amBombRender = new RenderAntimatterBomb();
        RenderingRegistry.registerBlockHandler(wireRenderer);
        TileEntityRendererDispatcher.instance.mapSpecialRenderers.put(Miner.class, new SelectionRenderer());
        TileEntityRendererDispatcher.instance.mapSpecialRenderers.put(Builder.class, new SelectionRenderer());
        TileEntityRendererDispatcher.instance.mapSpecialRenderers.put(Farm.class, new SelectionRenderer());
        TileEntityRendererDispatcher.instance.mapSpecialRenderers.put(Pump.class, new SelectionRenderer());
        TileEntityRendererDispatcher.instance.mapSpecialRenderers.put(Teleporter.class, new SelectionRenderer());
        TileEntityRendererDispatcher.instance.mapSpecialRenderers.put(Tank.class, new TileEntityTankRenderer());
        TileEntityRendererDispatcher.instance.mapSpecialRenderers.put(AntimatterBomb.class, new TileEntityAntimatterBombRenderer());
        TileEntityRendererDispatcher.instance.mapSpecialRenderers.put(VertexShematicGen.class, new Render3DVertexShem());
        wireRenderer.setRenderMachine((TileBlock)BlockItemRegistry.getBlock("tile.wireC"));
        wireRenderer.setRenderMachine((TileBlock)BlockItemRegistry.getBlock("tile.wireA"));
        wireRenderer.setRenderMachine((TileBlock)BlockItemRegistry.getBlock("tile.wireH"));
        wireRenderer.setRenderMachine((TileBlock)BlockItemRegistry.getBlock("tile.liquidPipe"));
        wireRenderer.setRenderMachine((TileBlock)BlockItemRegistry.getBlock("tile.itemPipe"));
        wireRenderer.setRenderMachine((TileBlock)BlockItemRegistry.getBlock("tile.itemWarpPipe"));
        wireRenderer.setRenderMachine((TileBlock)BlockItemRegistry.getBlock("tile.liquidWarpPipe"));
        RenderingRegistry.registerEntityRenderingHandler(EntityAntimatterExplosion1.class, amBombRender);
        TickHandler.init();
        ClientInputHandler.init();
        TooltipInfo.addConfigReference(Config.data);
    }

    @Override
    public void registerBlocks()
    {
        TileBlockRegistry.registerGui(blockId("tile.steamEngine"), GuiSteamEngine.class);
        TileBlockRegistry.registerGui(blockId("tile.steamGenerator"), GuiSteamGenerator.class);
        TileBlockRegistry.registerGui(blockId("tile.steamBoiler"), GuiSteamBoiler.class);
        TileBlockRegistry.registerGui(blockId("tile.lavaCooler"), GuiLavaCooler.class);
        TileBlockRegistry.registerGui(blockId("tile.energyFurnace"), GuiEnergyFurnace.class);
        TileBlockRegistry.registerGui(blockId("tile.SCSU"), GuiESU.class);
        TileBlockRegistry.registerGui(blockId("tile.OCSU"), GuiESU.class);
        TileBlockRegistry.registerGui(blockId("tile.CCSU"), GuiESU.class);
        TileBlockRegistry.registerGui(blockId("tile.farm"), GuiFarm.class);
        TileBlockRegistry.registerGui(blockId("tile.miner"), GuiMiner.class);
        TileBlockRegistry.registerGui(blockId("tile.link"), GuiELink.class);
        TileBlockRegistry.registerGui(blockId("tile.linkHV"), GuiELink.class);
        TileBlockRegistry.registerGui(blockId("tile.texMaker"), GuiTextureMaker.class);
        TileBlockRegistry.registerGui(blockId("tile.builder"), GuiBuilder.class);
        TileBlockRegistry.registerGui(blockId("tile.algaePool"), GuiAlgaePool.class);
        TileBlockRegistry.registerGui(blockId("tile.voltageTransformer"), GuiVoltageTransformer.class);
        TileBlockRegistry.registerGui(blockId("tile.teslaTransmitter"), GuiTeslaTransmitter.class);
        TileBlockRegistry.registerGui(blockId("tile.teslaTransmitterLV"), GuiTeslaTransmitterLV.class);
        TileBlockRegistry.registerGui(blockId("tile.teleporter"), GuiTeleporter.class);
        TileBlockRegistry.registerGui(blockId("tile.advancedFurnace"), GuiAdvancedFurnace.class);
        TileBlockRegistry.registerGui(blockId("tile.pump"), GuiPump.class);
        TileBlockRegistry.registerGui(blockId("tile.steamTurbine"), GuiSteamTurbine.class);
        TileBlockRegistry.registerGui(blockId("tile.massstorageChest"), GuiMassstorageChest.class);
        TileBlockRegistry.registerGui(blockId("tile.antimatterTank"), GuiAntimatterTank.class);
        TileBlockRegistry.registerGui(blockId("tile.antimatterFabricator"), GuiAntimatterFabricator.class);
        TileBlockRegistry.registerGui(blockId("tile.antimatterAnihilator"), GuiAntimatterAnihilator.class);
        TileBlockRegistry.registerGui(blockId("tile.autoCrafting"), GuiAutoCrafting.class);
        TileBlockRegistry.registerGui(blockId("tile.geothermalFurnace"), GuiGeothermalFurnace.class);
        TileBlockRegistry.registerGui(blockId("tile.steamCompressor"), GuiSteamCompressor.class);
        TileBlockRegistry.registerGui(blockId("tile.electricCompressor"), GuiElectricCompressor.class);
        TileBlockRegistry.registerGui(blockId("tile.tank"), GuiTank.class);
        TileBlockRegistry.registerGui(blockId("tile.security"), GuiSecuritySys.class);
        TileBlockRegistry.registerGui(blockId("tile.decompCooler"), GuiDecompCooler.class);
        TileBlockRegistry.registerGui(blockId("tile.collector"), GuiCollector.class);
        TileBlockRegistry.registerGui(blockId("tile.trash"), GuiTrash.class);
        TileBlockRegistry.registerGui(blockId("tile.electrolyser"), GuiElectrolyser.class);
        TileBlockRegistry.registerGui(blockId("tile.fuelCell"), GuiFuelCell.class);
        TileBlockRegistry.registerGui(blockId("tile.detector"), GuiDetector.class);
        TileBlockRegistry.registerGui(blockId("tile.itemSorter"), GuiItemSorter.class);
        TileBlockRegistry.registerGui(blockId("tile.matterInterfaceB"), GuiMatterInterface.class);
        TileBlockRegistry.registerGui(blockId("tile.fluidPacker"), GuiFluidPacker.class);
        TileBlockRegistry.registerGui(blockId("tile.hugeTank"), GuiHugeTank.class);
        TileBlockRegistry.registerGui(blockId("tile.fluidVent"), GuiFluidVent.class);
        TileBlockRegistry.registerGui(blockId("tile.gravCond"), GuiGraviCond.class);
        TileBlockRegistry.registerGui(blockId("tile.itemBuffer"), GuiItemBuffer.class);
        TileBlockRegistry.registerGui(blockId("tile.quantumTank"), GuiQuantumTank.class);
        TileBlockRegistry.registerGui(blockId("tile.vertShemGen"), GuiVertexShematicGen.class);
    }
    
}
