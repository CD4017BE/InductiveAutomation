package cd4017be.automation.Item;

import java.io.IOException;

import cd4017be.automation.Automation;
import cd4017be.automation.Gui.ContainerPortableCrafting;
import cd4017be.automation.Gui.GuiPortableCrafting;
import cd4017be.lib.BlockGuiHandler;
import cd4017be.lib.DefaultItem;
import cd4017be.lib.IGuiItem;
import cd4017be.lib.util.Utils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class ItemPortableCrafter extends DefaultItem implements IGuiItem {

	public ItemPortableCrafter(String id) 
	{
		super(id);
        this.setCreativeTab(Automation.tabAutomation);
        this.setMaxStackSize(1);
	}

	@Override
	public Container getContainer(World world, EntityPlayer player, int x, int y, int z) 
	{
		return new ContainerPortableCrafting(player);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public GuiContainer getGui(World world, EntityPlayer player, int x, int y, int z) 
	{
		return new GuiPortableCrafting(new ContainerPortableCrafting(player));
	}

	@Override
	public void onPlayerCommand(World world, EntityPlayer player, PacketBuffer dis) throws IOException 
	{
		ItemStack item = player.getHeldItemMainhand();
		if (item == null || item.getItem() != this) return;
		if (item.getTagCompound() == null) item.setTagCompound(new NBTTagCompound());
		byte cmd = dis.readByte();
		if (cmd == 0) {
			item.getTagCompound().setBoolean("active", !item.getTagCompound().getBoolean("active"));
		} else if (cmd == 1) {
			item.getTagCompound().setBoolean("auto", !item.getTagCompound().getBoolean("auto"));
			item.getTagCompound().setBoolean("active", false);
		} else if (cmd == 2) {
			byte n = item.getTagCompound().getByte("amount");
			n += dis.readByte();
			if (n < 0) n = 0;
			if (n > 64) n = 64;
			item.getTagCompound().setByte("amount", n);
		} else if (cmd == 3) {
			byte n = dis.readByte();
			ItemStack[] recipe = loadRecipe(item.getTagCompound().getTagList("grid", 10), world);
			if (recipe[0] != null) this.craft(player.inventory, recipe, n > 0 ? n : Integer.MAX_VALUE, false);
			item.getTagCompound().setBoolean("active", false);
		} else if (cmd == 4) {// set crafting grid (used for JEI recipe transfer)
			NBTTagCompound nbt = dis.readNBTTagCompoundFromBuffer();
			item.getTagCompound().setTag("grid", nbt.getTagList("grid", 10));
			byte n = nbt.getByte("amount");
			if (n < 0) n = 0;
			if (n > 64) n = 64;
			item.getTagCompound().setByte("amount", n);
			item.getTagCompound().setBoolean("active", false);
			if (player.openContainer instanceof ContainerPortableCrafting) 
				((ContainerPortableCrafting)player.openContainer).load(item);
		}
	}
	
	@Override
    public ActionResult<ItemStack> onItemRightClick(ItemStack item, World world, EntityPlayer player, EnumHand hand) 
    {
        BlockGuiHandler.openItemGui(player, world, 0, -1, 0);
        return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, item);
    }

	@Override
	public void onUpdate(ItemStack item, World world, Entity entity, int s, boolean b) 
	{
		if (entity instanceof EntityPlayer) {
			if (item.getTagCompound() == null) item.setTagCompound(new NBTTagCompound());
			int t = item.getTagCompound().getByte("t") + 1;
			if (t >= 20) {
				t = 0;
				EntityPlayer player = (EntityPlayer)entity;
				InventoryPlayer inv = player.inventory;
				int n = item.getTagCompound().getByte("amount");
				boolean auto = item.getTagCompound().getBoolean("auto");
				boolean on = item.getTagCompound().getBoolean("active");
				if (on) {
					ItemStack[] recipe = loadRecipe(item.getTagCompound().getTagList("grid", 10), world);
					if (recipe[0] != null) {
						n -= this.craft(inv, recipe, n, auto);
						if (!auto) item.getTagCompound().setByte("amount", (byte)n);
						if (n <= 0) item.getTagCompound().setBoolean("active", false);
					} else item.getTagCompound().setBoolean("active", false);
				}
			}
			item.getTagCompound().setByte("t", (byte)t);
		}
	}
	
	public static ItemStack[] loadRecipe(NBTTagList data, World world)
	{
		InventoryCrafting icr = new InventoryCrafting(new Container() {
            @Override
            public boolean canInteractWith(EntityPlayer var1) {
                return true;
            }
            @Override
            public void onCraftMatrixChanged(IInventory par1IInventory) {}
        }, 3, 3);
		ItemStack[] recipe = new ItemStack[data.tagCount()];
		int n = 0;
		NBTTagCompound tag;
		for (int i = 0; i < data.tagCount(); i++) {
			tag = data.getCompoundTagAt(i);
			int s = tag.getByte("slot") & 0xff;
			ItemStack stack = ItemStack.loadItemStackFromNBT(tag);
			if (s < 9) icr.setInventorySlotContents(s, stack);
			for (int j = 0; j < n && stack != null; j++)
				if (Utils.itemsEqual(recipe[j], stack)) {
					recipe[j].stackSize++;
					stack = null;
				}
			if (stack != null) recipe[n++] = stack.splitStack(1);
		}
		ItemStack[] ret = new ItemStack[n + 1];
		ret[0] = CraftingManager.getInstance().findMatchingRecipe(icr, world);
		System.arraycopy(recipe, 0, ret, 1, n);
		return ret;
	}
	
	private int craft(InventoryPlayer inv, ItemStack[] recipe, int n, boolean auto)
	{
		if (auto) {
			n *= recipe[0].stackSize;
			for (int i = 0; i < inv.mainInventory.length && n > 0; i++) {
				if (Utils.itemsEqual(inv.mainInventory[i], recipe[0])) n -= inv.mainInventory[i].stackSize;
			}
			n /= recipe[0].stackSize;
		}
		if (n <= 0) return 0;
		int m, p;
		int[] slots = new int[inv.mainInventory.length];
		for (int i = 0; i < slots.length; i++) slots[i] = i;
		for (int i = 1; i < recipe.length; i++) {
			m = 0; p = 0;
			while((p = Utils.findStack(recipe[i], inv, slots, p)) >= 0) {
				m += inv.getStackInSlot(slots[p++]).stackSize;
			}
			m /= recipe[i].stackSize;
			if (m < n) n = m;
		}
		if (n <= 0) return 0;
		for (int i = 1; i < recipe.length; i++) {
			m = recipe[i].stackSize * n; p = 0;
			while((p = Utils.findStack(recipe[i], inv, slots, p)) >= 0 && m > 0) {
				m -= inv.decrStackSize(slots[p++], m).stackSize;
			}
			if (recipe[i].getItem().hasContainerItem(recipe[i])) {
                ItemStack iout = recipe[i].getItem().getContainerItem(recipe[i]);
                if (iout.isItemStackDamageable() && iout.getItemDamage() > iout.getMaxDamage()) iout = null;
                if (iout != null) {
                	iout.stackSize = recipe[i].stackSize * n;
                	if (!inv.addItemStackToInventory(iout)) {
            			inv.player.dropPlayerItemWithRandomChoice(iout, true);
            		}
                }
            }
		}
		recipe[0].stackSize *= n;
		if (!inv.addItemStackToInventory(recipe[0])) {
			inv.player.dropPlayerItemWithRandomChoice(recipe[0], true);
		}
		if (auto) return 0;
		else return n;
	}

}
