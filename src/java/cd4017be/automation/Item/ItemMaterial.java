/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cd4017be.automation.Item;

import java.util.List;

import cd4017be.automation.Automation;
import cd4017be.lib.BlockItemRegistry;
import cd4017be.lib.DefaultItem;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 *
 * @author CD4017BE
 */
public class ItemMaterial extends DefaultItem 
{
    public static final String[] types = {
        "CopperI", "SilverI", "ElectrumI", "SteelI", "RstMetall", "CondIA", "Graphite", "SiliconI", "HydrogenI", 
        "ElectrumR", "CondRA",
        "WoodC", "GlassC", "IronC", "StoneC", "SteelH", "GDPlate", "unbrC",
        "CoilH", "CoilCp", "CoilC", "CoilSC",
        "Motor", "Turbine", "Circut", "EMatrix",
        "Vent", "JetTurbine", "Control", "Strap",
        "LCAlgae", "LCBiomass", "LCNitrogen", "LCOxygen", "LCHydrogen", "LCHelium",
        "Biomass",
        "Acelerator", "AnihilationC", "QSGen", "BHGen", "QAlloyI", "QMatrix", "Breaker", "Placer", "AreaFrame",
        "mAccelerator", "amAcelerator", "Fokus",
        "IronOD", "GoldOD", "CopperOD", "SilverOD", "IronD", "GoldD", "CopperD", "SilverD",
        "DenseM", "Neutron", "BlackHole"
    };
    
    public ItemMaterial(String id) 
    {
        super(id);
        this.setCreativeTab(Automation.tabAutomation);
        this.setHasSubtypes(true);
        for (int i = 0; i < types.length; i++) {
            BlockItemRegistry.registerItemStack(new ItemStack(this, 1, i), types[i]);
        }
    }

    @Override
    protected void init() {}

    @Override
	public String getUnlocalizedName(ItemStack item) 
    {
    	int n = item.getItemDamage();
        if (n >= types.length || n < 0) return super.getUnlocalizedName(item);
    	return super.getUnlocalizedName(item).replaceFirst(":" + n, ":" + types[n]);
	}

    @Override
    public void getSubItems(Item id, CreativeTabs par2CreativeTabs, List<ItemStack> list) 
    {
        for (int i = 0; i < types.length; i++) list.add(new ItemStack(id, 1, i));
    }
    
}
