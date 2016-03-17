package cd4017be.automation.Item;

import cd4017be.api.automation.InventoryItemHandler;
import cd4017be.api.automation.InventoryItemHandler.IItemStorage;
import cd4017be.api.energy.EnergyAPI;
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
		float e = item.stackTagCompound.getFloat("buff");
		if (e > 0) {
			float d0 = e, d1 = e;
			if (d0 > maxPower * 1000) d0 = d1 = maxPower * 1000;
            for (int j = 0; j < inv.mainInventory.length && d0 > 0; j++)
                d0 -= EnergyAPI.get(inv.mainInventory[j]).addEnergy(d0, 0);
            e += d0 - d1;
		}
		if (e < maxPower) {
			ItemStack[] cont = InventoryItemHandler.getItemList(item);
			if (cont.length > 0) {
				int h = TileEntityFurnace.getItemBurnTime(cont[0]);
				if (h > 0) {
					e += (float)h * 1000F;
					cont[0].stackSize = 1;
					InventoryItemHandler.extractItemStack(item, cont[0]);
				}
			}
		}
		item.stackTagCompound.setFloat("buff", e);
		if (InventoryItemHandler.hasEmptySlot(item)) super.updateItem(item, player, inv, s, in, out);
	}

}
