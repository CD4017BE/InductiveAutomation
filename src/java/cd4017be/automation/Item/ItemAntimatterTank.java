package cd4017be.automation.Item;

import java.util.List;

import cd4017be.api.automation.AntimatterItemHandler;
import cd4017be.api.automation.AntimatterItemHandler.IAntimatterItem;
import cd4017be.automation.Config;
import cd4017be.automation.Objects;
import cd4017be.automation.TileEntity.AntimatterBomb;
import cd4017be.lib.DefaultItemBlock;
import cd4017be.lib.templates.SingleFluidItemHandler;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

/**
 *
 * @author CD4017BE
 */
public class ItemAntimatterTank extends DefaultItemBlock implements IAntimatterItem {

	public static double explFaktor;
	public static int BombMaxCap = 160000000;
	private final int cap;

	public ItemAntimatterTank(Block id){
		super(id);
		this.setMaxStackSize(1);
		cap = id == Objects.antimatterBombF ? BombMaxCap : Config.tankCap[4];
	}

	@Override
	public EnumRarity getRarity(ItemStack item) {
		return item.isItemEqual(new ItemStack(Objects.antimatterBombF)) ? EnumRarity.EPIC : EnumRarity.UNCOMMON;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void addInformation(ItemStack item, EntityPlayer player, List list, boolean par4) {
		AntimatterItemHandler.addInformation(item, list);
		if (item.getTagCompound() != null && item.isItemEqual(new ItemStack(Objects.antimatterBombF)))
		{
			int d = (int)Math.cbrt((double)item.getTagCompound().getInteger(getAntimatterTag(item)) * explFaktor);
			list.add("Crater radius (Stone) ~ " + d + " m");
			if (d > AntimatterBomb.maxSize) list.add(String.format("Explosion will stop at %d m !", AntimatterBomb.maxSize));
		}
		super.addInformation(item, player, list, par4);
	}

	@Override
	public void getSubItems(Item id, CreativeTabs tab, List<ItemStack> list) {
		super.getSubItems(id, tab, list);
		if (id == Item.getItemFromBlock(Objects.antimatterTank)) {
			ItemStack item = new ItemStack(id, 1, 0);
			item.setTagCompound(new NBTTagCompound());
			item.getTagCompound().setInteger("antimatter", Config.tankCap[4]);
			list.add(item);
		}
	}

	@Override
	public int getAmCapacity(ItemStack item) {
		return cap;
	}

	@Override
	public String getAntimatterTag(ItemStack item) {
		return "antimatter";
	}

	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound nbt) {
		return new SingleFluidItemHandler(stack, getAmCapacity(stack), Objects.L_antimatter, getAntimatterTag(stack));
	}

}
