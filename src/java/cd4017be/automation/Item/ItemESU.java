package cd4017be.automation.Item;

import java.util.List;

import cd4017be.api.energy.EnergyAutomation.EnergyItem;
import cd4017be.api.energy.EnergyAutomation.IEnergyItem;
import cd4017be.automation.Config;
import cd4017be.automation.Objects;
import cd4017be.lib.DefaultItemBlock;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 *
 * @author CD4017BE
 */
public class ItemESU extends DefaultItemBlock implements IEnergyItem {

	public ItemESU(Block id) {
		super(id);
	}

	@Override
	public void addInformation(ItemStack item, EntityPlayer player, List<String> list, boolean f) {
		new EnergyItem(item, this, -1).addInformation(list);
		super.addInformation(item, player, list, f);
	}

	@Override
	public boolean showDurabilityBar(ItemStack stack) {
		return true;
	}

	@Override
	public double getDurabilityForDisplay(ItemStack stack) {
		return 1D - (double)new EnergyItem(stack, this, -1).getStorageI() / (double)this.getEnergyCap(stack);
	}

	@Override
	public void getSubItems(Item item, CreativeTabs tab, List<ItemStack> list) {
		super.getSubItems(item, tab, list);
		ItemStack stack = new ItemStack(item);
		new EnergyItem(stack, this, -1).addEnergyI(getEnergyCap(stack));
		list.add(stack);
	}

	@Override
	public int getEnergyCap(ItemStack item) {
		return item.getItem() == Item.getItemFromBlock(Objects.SCSU) ? Config.Ecap[0] : item.getItem() == Item.getItemFromBlock(Objects.OCSU) ? Config.Ecap[1] : Config.Ecap[2];
	}

	@Override
	public int getChargeSpeed(ItemStack item) {
		return item.getItem() == Item.getItemFromBlock(Objects.SCSU) ? 160 : item.getItem() == Item.getItemFromBlock(Objects.OCSU) ? 400 : 1000;
	}

	@Override
	public String getEnergyTag() {
		return "energy";
	}

}
