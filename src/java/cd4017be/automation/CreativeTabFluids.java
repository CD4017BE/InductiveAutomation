package cd4017be.automation;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidRegistry;

public class CreativeTabFluids extends CreativeTabs
{
    
    public CreativeTabFluids(String name)
    {
        super(CreativeTabs.getNextID(), name);
    }

    @Override
    public ItemStack getIconItemStack() 
    {
        return new ItemStack(Objects.fluidDummy, 1, FluidRegistry.WATER.getID());
    }

    @Override
    public String getTranslatedTabLabel() 
    {
        return "Liquids";
    }

	@Override
	public Item getTabIconItem() 
	{
		return Objects.fluidDummy;
	}
	
}