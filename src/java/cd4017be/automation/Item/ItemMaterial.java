/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cd4017be.automation.Item;

import java.util.List;

import cd4017be.automation.Automation;
import cd4017be.lib.BlockItemRegistry;
import cd4017be.lib.DefaultItem;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

/**
 *
 * @author CD4017BE
 */
public class ItemMaterial extends DefaultItem 
{
    private static final String[] types = {
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
    
    private static final String[] names = {
        "Copper Ingot", "Silver Ingot", "Electrum Ingot", "Steel Ingot", "Redstone Metall Ingot", "Conductive Alloy Ingot", "Graphite Ingot", "Silicon Ingot", "Superconductive Hydrogen Ingot",
        "Electrum Dust", "Conductive Alloy Dust",
        "Wood Casing", "Glass Casing", "Iron Casing", "Stone Casing", "Steel Casing", "Graphite Casing", "unbreakable Casing",
        "Heating Coil", "Copper Coil", "Conductive Coil", "Superconductive Coil",
        "Motor", "Electric Turbine", "Redstone Circuit", "Ender Matrix",
        "Liquid Heat Exchanger", "Jetpack Turbine", "Control Interface", "Chest straps",
        "Algae Bottle", "Biomass Bottle", "Liquid Nitrogen Bottle", "Liquid Oxygen Bottle", "Liquid Hydrogen Bottle", "Liquid Helium Bottle",
        "Dry Biomass",
        "Particle accelerator", "Anihilation Chamber", "Quantum entangler", "Hyperspace Wormhole", "Quantum integrated Ingot", "Quantum Matrix", "Electric Block Harvester", "Electric Block Placer", "Tool Motion Frame",
        "Matter accelerator", "Antimatter accelerator", "Particle Fokus-module",
        "Iron Ore Dust", "Gold Ore Dust", "Copper Ore Dust", "Silver Ore Dust", "Iron Dust", "Gold Dust", "Copper Dust", "Silver Dust",
        "Ultra dense Material", "Neutronium-10^42", "Black Hole"
    };
    
    private IIcon[] textures = new IIcon[types.length];
    
    public ItemMaterial(String id, String tex) 
    {
        super(id);
        this.setCreativeTab(Automation.tabAutomation);
        this.setHasSubtypes(true);
        this.setTextureName(tex);
        for (int i = 0; i < types.length; i++) {
            BlockItemRegistry.registerItemStack(new ItemStack(this, 1, i), types[i]);
        }
    }

    @Override
    protected void init() {}

    @Override
    public void registerIcons(IIconRegister register) 
    {
        for (int i = 0; i < types.length; i++)
        {
            textures[i] = register.registerIcon(this.iconString + types[i]);
        }
        this.itemIcon = register.registerIcon(iconString + "invalid");
    }
    
    @Override
    public IIcon getIconFromDamage(int m) 
    {
        return m >= 0 && m < textures.length ? textures[m] : this.itemIcon;
    }

    @Override
    public String getItemStackDisplayName(ItemStack itemStack) 
    {
        int n = itemStack.getItemDamage();
        if (n >= names.length || n < 0) return "Invalid Item";
        return names[n];
    }

    @Override
    public void getSubItems(Item id, CreativeTabs par2CreativeTabs, List list) 
    {
        for (int i = 0; i < types.length; i++)
        {
            list.add(new ItemStack(id, 1, i));
        }
    }
    
}
