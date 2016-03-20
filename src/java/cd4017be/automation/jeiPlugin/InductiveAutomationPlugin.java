/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cd4017be.automation.jeiPlugin;

import cd4017be.api.automation.AutomationRecipes;
import cd4017be.automation.Gui.GuiAdvancedFurnace;
import cd4017be.automation.Gui.GuiDecompCooler;
import cd4017be.automation.Gui.GuiElectricCompressor;
import cd4017be.automation.Gui.GuiElectrolyser;
import cd4017be.automation.Gui.GuiEnergyFurnace;
import cd4017be.automation.Gui.GuiGeothermalFurnace;
import cd4017be.automation.Gui.GuiGraviCond;
import cd4017be.automation.Gui.GuiSteamBoiler;
import cd4017be.automation.Gui.GuiSteamCompressor;
import mezz.jei.api.BlankModPlugin;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;

/**
 *
 * @author CD4017BE
 */
@JEIPlugin
public class InductiveAutomationPlugin extends BlankModPlugin
{

	@Override
	public void register(IModRegistry registry) {
		IGuiHelper guiHelper = registry.getJeiHelpers().getGuiHelper();
		//IRecipeTransferRegistry recipeTransferRegistry = registry.getRecipeTransferRegistry();
		
		registry.addRecipeCategories(
				new AssemblerRecipeCategory(guiHelper), 
				new AdvFurnaceRecipeCategory(guiHelper),
				new DecompCoolerRecipeCategory(guiHelper),
				new ElectrolyserRecipeCategory(guiHelper),
				new GravCondenserRecipeCategory(guiHelper));
		
		registry.addRecipeHandlers(
				new AssemblerRecipeHandler(), 
				new AdvFurnaceRecipeHandler(),
				new DecompCoolerRecipeHandler(),
				new ElectrolyserRecipeHandler(),
				new GravCondenserRecipeHandler());
		
		registry.addRecipeClickArea(GuiGeothermalFurnace.class, 117, 37, 32, 10, VanillaRecipeCategoryUid.SMELTING);
		registry.addRecipeClickArea(GuiEnergyFurnace.class, 81, 37, 32, 10, VanillaRecipeCategoryUid.SMELTING);
		registry.addRecipeClickArea(GuiGeothermalFurnace.class, 63, 35, 14, 14, VanillaRecipeCategoryUid.FUEL);
		registry.addRecipeClickArea(GuiSteamBoiler.class, 27, 35, 14, 14, VanillaRecipeCategoryUid.FUEL);
		registry.addRecipeClickArea(GuiSteamCompressor.class, 108, 37, 32, 10, "automation.assembler");
		registry.addRecipeClickArea(GuiElectricCompressor.class, 99, 37, 32, 10, "automation.assembler");
		registry.addRecipeClickArea(GuiAdvancedFurnace.class, 97, 37, 18, 10, "automation.advFurnace");
		registry.addRecipeClickArea(GuiDecompCooler.class, 98, 16, 16, 52, "automation.decompCool");
		registry.addRecipeClickArea(GuiElectrolyser.class, 0, 0, 0, 0, "automation.electrolyser");
		registry.addRecipeClickArea(GuiGraviCond.class, 98, 16, 16, 52, "automation.gravCond");
		
		registry.addRecipes(AutomationRecipes.getCompressorRecipes());
		registry.addRecipes(AutomationRecipes.getAdvancedFurnaceRecipes());
		registry.addRecipes(AutomationRecipes.getCoolerRecipes());
		registry.addRecipes(AutomationRecipes.getElectrolyserRecipes());
		registry.addRecipes(AutomationRecipes.getGraviCondRecipes());
		
		//recipeTransferRegistry.addRecipeTransferHandler(ContainerFurnace.class, VanillaRecipeCategoryUid.SMELTING, 0, 1, 3, 36);
	}
}
