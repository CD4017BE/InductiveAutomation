package cd4017be.automation;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class CreativeTabFluids extends CreativeTabs
{
    
    public CreativeTabFluids(String name)
    {
        super(CreativeTabs.getNextID(), name);
    }

    @Override
    public ItemStack getIconItemStack() 
    {
        return new ItemStack(Blocks.water);
    }

    @Override
    public String getTranslatedTabLabel() 
    {
        return "Liquids";
    }

	@Override
	public Item getTabIconItem() 
	{
		return Item.getItemFromBlock(Blocks.water);
	}
	
}