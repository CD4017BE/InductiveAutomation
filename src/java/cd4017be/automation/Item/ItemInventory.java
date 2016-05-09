package cd4017be.automation.Item;

import cd4017be.api.automation.InventoryItemHandler.IItemStorage;
import cd4017be.automation.Gui.ContainerPortableInventory;
import cd4017be.automation.Gui.GuiPortableInventory;
import cd4017be.lib.BlockGuiHandler;
import cd4017be.lib.IGuiItem;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class ItemInventory extends ItemFilteredSubInventory implements IItemStorage, IGuiItem
{

	public ItemInventory(String id) 
	{
		super(id);
	}

	@Override
	public int getSizeInventory(ItemStack item) 
	{
		return 24;
	}

	@Override
	public Container getContainer(World world, EntityPlayer player, int x, int y, int z) 
	{
		return new ContainerPortableInventory(player);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public GuiContainer getGui(World world, EntityPlayer player, int x, int y, int z) 
	{
		return new GuiPortableInventory(new ContainerPortableInventory(player));
	}
	
	@Override
    public ActionResult<ItemStack> onItemRightClick(ItemStack item, World world, EntityPlayer player, EnumHand hand) 
    {
        BlockGuiHandler.openItemGui(player, world, 0, -1, 0);
        return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, item);
    }

}
