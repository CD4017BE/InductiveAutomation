package cd4017be.automation.Item;

import cd4017be.api.automation.EnergyItemHandler;
import cd4017be.api.automation.InventoryItemHandler;
import cd4017be.api.automation.InventoryItemHandler.IItemStorage;
import cd4017be.automation.Gui.ContainerPortableGenerator;
import cd4017be.automation.Gui.GuiPortableGenerator;
import cd4017be.lib.BlockGuiHandler;
import cd4017be.lib.IGuiItem;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.world.World;

public class ItemPortableGenerator extends ItemFilteredSubInventory implements IGuiItem, IItemStorage 
{
	
	private static final int maxPower = 80;
	
	public ItemPortableGenerator(String id) 
	{
		super(id);
	}

	@Override
	public int getSizeInventory(ItemStack item) 
	{
		return 1;
	}

	@Override
	public Container getContainer(World world, EntityPlayer player, int x, int y, int z) 
	{
		return new ContainerPortableGenerator(player);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public GuiContainer getGui(World world, EntityPlayer player, int x, int y, int z) 
	{
		return new GuiPortableGenerator(new ContainerPortableGenerator(player));
	}
	
	@Override
    public ItemStack onItemRightClick(ItemStack item, World world, EntityPlayer player) 
    {
        BlockGuiHandler.openItemGui(player, world, 0, -1, 0);
        return item;
    }

	@Override
	protected void updateItem(ItemStack item, EntityPlayer player, InventoryPlayer inv, int s, PipeUpgradeItem in, PipeUpgradeItem out) 
	{
		int e = item.stackTagCompound.getInteger("buff");
		if (e > 0) {
			int d0 = e, d1 = e;
			if (d0 > maxPower) d0 = d1 = maxPower;
            for (int j = 0; j < inv.mainInventory.length && d0 > 0; j++)
            {
                ItemStack it = inv.mainInventory[j];
                if (EnergyItemHandler.isEnergyItem(it)) {
                	d0 -= EnergyItemHandler.addEnergy(it, d0, true);
                }
            }
            item.stackTagCompound.setInteger("buff", e - d1 + d0);
		}
		if (item.stackTagCompound.getInteger("buff") <= 0) {
			ItemStack[] cont = InventoryItemHandler.getItemList(item);
			if (cont.length > 0) {
				e = TileEntityFurnace.getItemBurnTime(cont[0]);
				if (e > 0) {
					item.stackTagCompound.setInteger("buff", e);
					cont[0].stackSize = 1;
					InventoryItemHandler.extractItemStack(item, cont[0]);
				}
			}
		}
		if (InventoryItemHandler.hasEmptySlot(item)) super.updateItem(item, player, inv, s, in, out);
	}

}
