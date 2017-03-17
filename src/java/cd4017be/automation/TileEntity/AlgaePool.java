package cd4017be.automation.TileEntity;

import java.util.ArrayList;
import java.util.List;

import cd4017be.api.recipes.AutomationRecipes;
import cd4017be.automation.Config;
import cd4017be.automation.Objects;
import cd4017be.lib.BlockItemRegistry;
import cd4017be.lib.Gui.DataContainer;
import cd4017be.lib.Gui.DataContainer.IGuiData;
import cd4017be.lib.Gui.SlotTank;
import cd4017be.lib.Gui.TileContainer;
import cd4017be.lib.Gui.TileContainer.ISlotClickHandler;
import cd4017be.lib.Gui.TileContainer.TankSlot;
import cd4017be.lib.templates.AutomatedTile;
import cd4017be.lib.templates.Inventory;
import cd4017be.lib.templates.TankContainer;
import cd4017be.lib.util.Utils;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.EnumSkyBlock;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.SlotItemHandler;

/**
 *
 * @author CD4017BE
 */
public class AlgaePool extends AutomatedTile implements IGuiData, ISlotClickHandler {

	public static final short UpdateTime = 10;
	private float amountBiomass;
	private short updateCounter;
	private float midBrightness;
	public int cap;
	public float algae, nutrient;

	public AlgaePool() {
		inventory = new Inventory(5, 1, null).group(0, 4, 5, Utils.IN);
		tanks = new TankContainer(2, 2).tank(0, Config.tankCap[1], Utils.IN, 2, -1, Objects.L_water).tank(1, Config.tankCap[1], Utils.OUT, -1, 3, Objects.L_biomass);
	}

	@Override
	public void update() 
	{
		super.update();
		if (worldObj.isRemote) return;
		updateCounter++;
		if (updateCounter >= UpdateTime)
		{
			updateCounter = 0;
			midBrightness = 0;
			List<BlockPos> list = new ArrayList<BlockPos>();
			updateMultiblock(pos, list);
			if (list.size() > 0) midBrightness /= list.size();
			cap = list.size() * Config.tankCap[1];
		}
		if (inventory.items[0] != null && inventory.items[0].isItemEqual(BlockItemRegistry.stack("m.LCAlgae", 1))) {
			algae += inventory.items[0].stackSize * 1000;
			inventory.items[0] = new ItemStack(Items.GLASS_BOTTLE, inventory.items[0].stackSize);
		}
		if (inventory.items[1] != null && inventory.items[1].isItemEqual(new ItemStack(Items.GLASS_BOTTLE)) && algae >= inventory.items[1].stackSize * 1000) {
			algae -= inventory.items[1].stackSize * 1000;
			inventory.items[1] = BlockItemRegistry.stack("m.LCAlgae", inventory.items[1].stackSize);
		}
		if (algae > 0) updateAlgaeGrowing();
		int[] n = AutomationRecipes.getLnutrients(inventory.items[4]);
		if (n != null && nutrient + n[0] <= Config.tankCap[1] && tanks.getAmount(0) >= n[0])
		{
			tanks.drain(0, n[0], true);
			nutrient += n[0];
			algae += n[1];
			inventory.items[4].stackSize--;
			if (inventory.items[4].stackSize <= 0) inventory.items[4] = null;
		}
	}
	
	private void updateMultiblock(BlockPos p, List<BlockPos> list)
	{
		for (int i = 0; i < 6 && list.size() < 16; i++)
		{
			EnumFacing dir = EnumFacing.VALUES[i];
			BlockPos pos = p.offset(dir);
			if (worldObj.getBlockState(pos).getBlock() != Objects.pool) continue;
			boolean b = true;
			for (int j = 0; j < list.size(); j++)
			{
				if (list.get(j).equals(pos))
				{
					b = false;
					break;
				}
			}
			if (b)
			{
				list.add(pos);
				midBrightness += worldObj.getLightFor(EnumSkyBlock.SKY, pos.up());
				updateMultiblock(pos, list);
			}
		}
	}
	
	private void updateAlgaeGrowing()
	{
		if (algae > cap)
		{
			amountBiomass += algae - cap;
			algae = cap;
		}
		float Rf = algae * sq(getBrightness()) * Config.algaeGrowing;
		float Rd = algae * sq(algae / (float)cap / 2F) * Config.algaeDecaying;
		algae -= Rd;
		amountBiomass += Rd;
		if (nutrient < Rf) Rf = nutrient;
		algae += Rf;
		nutrient -= Rf;
		int b = (int)Math.floor(amountBiomass);
		tanks.fill(1, new FluidStack(Objects.L_biomass, b), true);
		amountBiomass -= b;
	}
	
	private float getBrightness()
	{
		float l = midBrightness - worldObj.getSkylightSubtracted();
		if (l < 0) l = 0;
		return l * (1F - algae / (float)cap / 4F) / 15F;
	}
	
	private float sq(float v)
	{
		return v * v;
	}

	public float getAlgae() {
		return cap == 0 ? 0 : algae / (float)cap;
	}

	public float getNutrients() {
		return nutrient / (float)Config.tankCap[1];
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) 
	{
		super.readFromNBT(nbt);
		nutrient = nbt.getFloat("nutrients");
		algae = nbt.getFloat("algae");
		amountBiomass = nbt.getFloat("ofBiomass");
		updateCounter = UpdateTime;
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) 
	{
		nbt.setFloat("nutrients", nutrient);
		nbt.setFloat("algae", algae);
		nbt.setFloat("ofBiomass", amountBiomass);
		return super.writeToNBT(nbt);
	}

	@Override
	public void initContainer(DataContainer container) {
		TileContainer cont = (TileContainer)container;
		cont.clickHandler = this;
		cont.addItemSlot(new SlotItemHandler(inventory, 0, 80, 34));
		cont.addItemSlot(new SlotItemHandler(inventory, 1, 98, 34));
		cont.addItemSlot(new SlotTank(inventory, 2, 17, 34));
		cont.addItemSlot(new SlotTank(inventory, 3, 143, 34));
		cont.addItemSlot(new SlotItemHandler(inventory, 4, 44, 34));
		
		cont.addPlayerInventory(8, 86);
		
		cont.addTankSlot(new TankSlot(tanks, 0, 8, 16, (byte)0x23));
		cont.addTankSlot(new TankSlot(tanks, 1, 134, 16, (byte)0x23));
	}

	@Override
	public boolean transferStack(ItemStack item, int s, TileContainer container) {
		if (s < container.invPlayerS) container.mergeItemStack(item, container.invPlayerS, container.invPlayerE, false);
		else container.mergeItemStack(item, 4, 5, false);
		return true;
	}

	@Override
	public int[] getSyncVariables() {
		return new int[]{cap, Float.floatToIntBits(algae), Float.floatToIntBits(nutrient)};
	}

	@Override
	public void setSyncVariable(int i, int v) {
		switch(i) {
		case 0: cap = v; break;
		case 1: algae = Float.intBitsToFloat(v); break;
		case 2: nutrient = Float.intBitsToFloat(v); break;
		}
	}

}
