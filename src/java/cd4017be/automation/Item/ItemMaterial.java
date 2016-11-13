package cd4017be.automation.Item;

import cd4017be.lib.BlockItemRegistry;
import cd4017be.lib.DefaultItem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/**
 *
 * @author CD4017BE
 */
@Deprecated //TODO remove entirely
public class ItemMaterial extends DefaultItem {

	private static final String[] transform = {
		"m.ingotCopper", "m.ingotSilver", "m.ingotElectrum", "m.ingotSteel", "m.ingotRedstAlloy", "m.ingotConductive", "m.ingotGraphite", "m.ingotSilicon", "m.ingotHydrogen", 
		"m.dustElectrum", "m.dustConductive",
		"m.WoodC", "m.GlassC", "m.IronC", "m.StoneC", "m.SteelH", "m.GDPlate", "m.unbrC",
		"tile.electricHeater", "tile.electricCoilC", "tile.electricCoilA", "tile.electricCoilH",
		"m.Motor", "m.Turbine", "m.Circuit", "m.EMatrix",
		"m.Vent", "m.JetTurbine", "m.Control", "m.Strap",
		"m.LCAlgae", "m.LCBiomass", "m.LCNitrogen", "m.LCOxygen", "m.LCHydrogen", "m.LCHelium",
		"m.Biomass",
		"m.Acelerator", "m.AnihilationC", "m.QSGen", "m.BHGen", "m.ingotQAlloy", "m.QMatrix", "m.Breaker", "m.Placer", "m.AreaFrame",
		"m.mAccelerator", "m.amAcelerator", "m.Focus",
		"m.oxideIron", "m.oxideGold", "m.oxideCopper", "m.oxideSilver", "m.dustIron", "m.dustGold", "m.dustCopper", "m.dustSilver",
		"m.DenseM", "m.Neutron", "m.BlackHole"
	};

	public ItemMaterial(String id) {
		super(id);
	}

	@Override
	protected void init() {}

	@Override
	public void onUpdate(ItemStack stack, World worldIn, Entity entity, int itemSlot, boolean isSelected) {
		if (entity instanceof EntityPlayer)
			((EntityPlayer)entity).inventory.setInventorySlotContents(itemSlot, transf(stack));
	}

	@Override
	public boolean onEntityItemUpdate(EntityItem entityItem) {
		entityItem.setEntityItemStack(transf(entityItem.getEntityItem()));
		return true;
	}

	private ItemStack transf(ItemStack item) {
		int dmg = item.getItemDamage();
		if (dmg < 0 || dmg >= transform.length) return null;
		else return BlockItemRegistry.stack(transform[dmg], item.stackSize);
	}

}
