package cd4017be.automation.Item;

import java.util.List;

import cd4017be.api.automation.InventoryItemHandler;
import cd4017be.api.automation.InventoryItemHandler.IItemStorage;
import cd4017be.api.energy.EnergyAutomation.EnergyItem;
import cd4017be.api.energy.EnergyAutomation.IEnergyItem;
import cd4017be.automation.Config;
import cd4017be.automation.Gui.ContainerPortableFurnace;
import cd4017be.automation.Gui.GuiPortableFurnace;
import cd4017be.lib.BlockGuiHandler;
import cd4017be.lib.IGuiItem;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class ItemFurnace extends ItemFilteredSubInventory implements IEnergyItem, IItemStorage, IGuiItem
{
	public static int energyUse = 200;
	public ItemFurnace(String id)
	{
		super(id);
		this.setMaxDamage(16);
	}
	
	@Override
	public int getSizeInventory(ItemStack item) 
	{
		return 1;
	}

	@Override
	public void addInformation(ItemStack item, EntityPlayer player, List list, boolean b) 
	{
		new EnergyItem(item, this).addInformation(list);
		super.addInformation(item, player, list, b);
	}
	
    @Override
	public boolean showDurabilityBar(ItemStack stack) {
		return true;
	}

	@Override
	public double getDurabilityForDisplay(ItemStack stack) {
    	return 1D - (double)new EnergyItem(stack, this).getStorageI() / (double)this.getEnergyCap(stack);
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(ItemStack item, World world, EntityPlayer player, EnumHand hand) 
    {
        BlockGuiHandler.openItemGui(player, world, 0, -1, 0);
        return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, item);
    }
	
	@Override
	public Container getContainer(World world, EntityPlayer player, int x, int y, int z) 
	{
		return new ContainerPortableFurnace(player);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public GuiContainer getGui(World world, EntityPlayer player, int x, int y, int z) 
	{
		return new GuiPortableFurnace(new ContainerPortableFurnace(player));
	}

	@Override
	public int getEnergyCap(ItemStack item) 
	{
		return Config.Ecap[0];
	}

	@Override
	public int getChargeSpeed(ItemStack item) {
		return 160;
	}

	@Override
	public String getEnergyTag() {
		return "energy";
	}

	@Override
	protected void updateItem(ItemStack item, EntityPlayer player, InventoryPlayer inv, int s, PipeUpgradeItem in, PipeUpgradeItem out) 
	{
		EnergyItem energy = new EnergyItem(item, this);
		if (energy.getStorageI() >= energyUse) {
			ItemStack[] cont = InventoryItemHandler.getItemList(item);
			if (cont.length > 0) {
				ItemStack stack = FurnaceRecipes.instance().getSmeltingResult(cont[0]);
				if (stack != null) {
					stack = stack.copy();
					energy.addEnergyI(-energyUse, -1);
					cont[0].stackSize = 1;
					InventoryItemHandler.extractItemStack(item, cont[0]);
					if (!inv.addItemStackToInventory(stack)) {
						player.dropPlayerItemWithRandomChoice(stack, true);
					}
				}
			}
		}
		if (InventoryItemHandler.hasEmptySlot(item)) super.updateItem(item, player, inv, s, in, out);
	}

}
