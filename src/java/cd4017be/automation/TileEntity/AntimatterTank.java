package cd4017be.automation.TileEntity;

import java.util.ArrayList;

import cd4017be.automation.Config;
import cd4017be.automation.Objects;
import cd4017be.lib.Gui.DataContainer;
import cd4017be.lib.Gui.DataContainer.IGuiData;
import cd4017be.lib.Gui.TileContainer;
import cd4017be.lib.templates.AutomatedTile;
import cd4017be.lib.templates.Inventory;
import cd4017be.lib.templates.TankContainer;
import cd4017be.lib.util.ItemFluidUtil.StackedFluidAccess;
import cd4017be.lib.util.Utils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.SlotItemHandler;

/**
 *
 * @author CD4017BE
 */
public class AntimatterTank extends AutomatedTile implements IGuiData
{
	public static final int MaxCap = 160000000;// =0.16g
	private int fillRate = 0;
	
	public AntimatterTank()
	{
		tanks = new TankContainer(1, 1).tank(0, Config.tankCap[4], Utils.ACC, -1, -1, Objects.L_antimatter);
		inventory = new Inventory(2, 2, null).group(0, 0, 1, Utils.ACC).group(1, 1, 2, Utils.ACC);
	}
	
	@Override
	public void update() {
		super.update();
		if (worldObj.isRemote) return;
		boolean f = false;
		StackedFluidAccess acc;
		int am;
		if ((am = tanks.getAmount(0)) > 0 && (acc = new StackedFluidAccess(inventory.items[0])).valid()) {
			if (fillRate < 5) fillRate = 5;
			if (am - fillRate < 0) fillRate = am;
			f |= (am = acc.fill(new FluidStack(Objects.L_antimatter, fillRate), true)) > 0;
			tanks.drain(0, am, true);
			inventory.items[0] = acc.result();
		}
		if ((am = tanks.getSpace(0)) > 0 && (acc = new StackedFluidAccess(inventory.items[1])).valid()) {
			if (fillRate < 5) fillRate = 5;
			if (am - fillRate < 0) fillRate = am;
			f |= tanks.fill(0, acc.drain(new FluidStack(Objects.L_antimatter, fillRate), true), true) > 0;
			inventory.items[1] = acc.result();
		}
		if (!f) fillRate = 0;
		else fillRate += 5;
	}
	
	@Override
	public void onPlaced(EntityLivingBase entity, ItemStack item)  
	{
		if (item != null && item.getItem() == Item.getItemFromBlock(this.getBlockType()));
		{
			if (item.getTagCompound() != null) tanks.setFluid(0, new FluidStack(Objects.L_antimatter, item.getTagCompound().getInteger("antimatter")));
			else this.tanks.setFluid(0, new FluidStack(Objects.L_antimatter, 0));
		}
	}

	@Override
	public ArrayList<ItemStack> dropItem(IBlockState state, int fortune) 
	{
		ArrayList<ItemStack> list = new ArrayList<ItemStack>();
		ItemStack item = new ItemStack(this.getBlockType(), 1, 0);
		if (tanks.getAmount(0) >= 0) {
			item.setTagCompound(new NBTTagCompound());
			item.getTagCompound().setInteger("antimatter", tanks.getAmount(0));
		}
		list.add(item);
		return list;
	}

	public float getStorage() {
		return (float)tanks.getAmount(0) / (float)tanks.tanks[0].cap;
	}

	@Override
	public void initContainer(DataContainer cont) {
		TileContainer container = (TileContainer)cont;
		container.addItemSlot(new SlotItemHandler(inventory, 0, 152, 24));
		container.addItemSlot(new SlotItemHandler(inventory, 1, 152, 42));
		
		container.addPlayerInventory(8, 84);
	}

	@Override
	public int[] getSyncVariables() {
		return new int[]{tanks.getAmount(0)};
	}

	@Override
	public void setSyncVariable(int i, int v) {
		tanks.fluids[0].amount = v;
	}

}
