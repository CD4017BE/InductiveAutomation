/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cd4017be.automation.neiPlugin;

import cd4017be.automation.Gui.GuiPortableCrafting;
import codechicken.nei.api.API;
import codechicken.nei.api.IConfigureNEI;

/**
 *
 * @author CD4017BE
 */
public class NEIConfig implements IConfigureNEI
{

    @Override
    public void loadConfig() 
    {
        API.registerRecipeHandler(new CompressorRecipeHandler());
        API.registerUsageHandler(new CompressorRecipeHandler());
        
        API.registerRecipeHandler(new AdvFurnaceRecipeHandler());
        API.registerUsageHandler(new AdvFurnaceRecipeHandler());
        
        API.registerRecipeHandler(new DecompCoolerRecipeHandler());
        API.registerUsageHandler(new DecompCoolerRecipeHandler());
        
        API.registerRecipeHandler(new ElectrolyserRecipeHandler());
        API.registerUsageHandler(new ElectrolyserRecipeHandler());
        
        API.registerRecipeHandler(new BedrockRecipeHandler());
        API.registerUsageHandler(new BedrockRecipeHandler());
        
        API.registerRecipeHandler(new GraviCondenserRecipeHandler());
        API.registerUsageHandler(new GraviCondenserRecipeHandler());
        
        API.registerGuiOverlay(GuiPortableCrafting.class, "crafting", -8, 10);
        API.registerGuiOverlayHandler(GuiPortableCrafting.class, new HoloRecipeOverlayHandler(-8, 10), "crafting");
    }

    @Override
    public String getName() 
    {
        return "Automation";
    }

    @Override
    public String getVersion() 
    {
        return "3.2.1";
    }
    
}
