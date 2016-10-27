package cd4017be.automation.TileEntity;

import net.minecraft.network.PacketBuffer;

import java.io.IOException;

import cd4017be.api.automation.PipeEnergy;
import cd4017be.api.recipes.AutomationRecipes;
import cd4017be.api.recipes.AutomationRecipes.LFRecipe;
import cd4017be.automation.Config;
import cd4017be.lib.Gui.DataContainer;
import cd4017be.lib.Gui.DataContainer.IGuiData;
import cd4017be.lib.Gui.SlotTank;
import cd4017be.lib.Gui.TileContainer;
import cd4017be.lib.Gui.TileContainer.ISlotClickHandler;
import cd4017be.lib.Gui.TileContainer.TankSlot;
import cd4017be.lib.templates.AutomatedTile;
import cd4017be.lib.templates.Inventory;
import cd4017be.lib.templates.TankContainer;
import cd4017be.lib.util.ItemFluidUtil;
import cd4017be.lib.util.Utils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraft.entity.player.EntityPlayerMP;

/**
 *
 * @author CD4017BE
 */
public class AdvancedFurnace extends AutomatedTile implements IGuiData, ISlotClickHandler{

	private float powerScale;
	private LFRecipe recipe;
	public int Rw;
	public float Estor, Eneed, Uc;

	public AdvancedFurnace() {
		inventory = new Inventory(8, 2, null).group(0, 2, 5, Utils.IN).group(1, 5, 8, Utils.OUT);
		tanks = new TankContainer(2, 2).tank(0, Config.tankCap[1], Utils.IN, 0, -1).tank(1, Config.tankCap[1], Utils.OUT, -1, 1);
		energy = new PipeEnergy(Config.Umax[1], Config.Rcond[1]);
		Rw = Config.Rmin;
		powerScale = Config.Pscale;
	}

	@Override
	public void update() {
		super.update();
		if (this.worldObj.isRemote) return;
		if (recipe == null && (tanks.getAmount(0) > 0 || inventory.items[2] != null || inventory.items[3] != null || inventory.items[4] != null)) {
			ItemStack[] items = new ItemStack[3];
			System.arraycopy(inventory.items, 2, items, 0, items.length);
			recipe = AutomationRecipes.getRecipeFor(tanks.fluids[0], items);
			if (recipe != null) {
				if (recipe.Linput != null) tanks.drain(0, recipe.Linput.amount, true);
				if (recipe.Iinput != null)
				for (Object item : recipe.Iinput) {
					if (item == null) continue;
					int n = AutomationRecipes.getStacksize(item);
					for (int i = 2; i < 5 && n > 0; i++) {
						if (!AutomationRecipes.isItemEqual(inventory.items[i], item)) continue;
						else if (inventory.items[i].stackSize <= n) {
							n -= inventory.items[i].stackSize;
							inventory.items[i] = null;
						} else {
							inventory.items[i].stackSize -= n;
							n = 0;
						}
					}
				}
				Eneed = recipe.energy;
			}
		}
		Uc = (float)energy.Ucap;
		double power = energy.getEnergy(0, Rw) / 1000F;
		if (recipe != null) {
			if (Estor < recipe.energy) {
				Estor += power;
				energy.Ucap *= powerScale;
			} if (Estor >= recipe.energy) {
				boolean r = false;
				if (recipe.Loutput != null) {
					recipe.Loutput.amount -= tanks.fill(1, recipe.Loutput, true);
					if (recipe.Loutput.amount > 0) r = true;
					else recipe.Loutput = null;
				}
				if (recipe.Ioutput != null)
					for (int i = 0; i < recipe.Ioutput.length; i++)
						if (recipe.Ioutput[i] != null) {
							recipe.Ioutput[i] = ItemFluidUtil.putInSlots(inventory, recipe.Ioutput[i], 5, 6, 7);
							r |= recipe.Ioutput[i] != null;
						}
				if (!r) {
					Estor -= recipe.energy;
					recipe = null;
				}
			}
		}
	}

	@Override
	protected void customPlayerCommand(byte cmd, PacketBuffer dis, EntityPlayerMP player) throws IOException {
		if (cmd == 0) {
			Rw = dis.readInt();
			if (Rw < Config.Rmin) Rw = Config.Rmin;
			powerScale = (float)Math.sqrt(1.0D - 1.0D / (double)Rw);
		} else if (cmd == 1) {
			FluidStack ls = tanks.fluids[0];
			tanks.setFluid(0, tanks.fluids[1]);
			tanks.setFluid(1, ls);
		}
	}

	public float getProgress() {
		return Eneed <= 0 ? 0F : Estor / Eneed;
	}

	public float getPower() {
		return Uc * Uc / (float)Rw / 200000F;
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) 
	{
		if (recipe != null) nbt.setTag("recipe", AutomationRecipes.writeToNBT(recipe));
		nbt.setFloat("progress", Estor);
		nbt.setInteger("resistor", Rw);
		nbt.setFloat("pScale", powerScale);
		return super.writeToNBT(nbt);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) 
	{
		super.readFromNBT(nbt);
		if (nbt.hasKey("recipe")) recipe = AutomationRecipes.readFromNBT(nbt.getCompoundTag("recipe"));
		else recipe = null;
		Estor = nbt.getFloat("progress");
		Rw = nbt.getInteger("resistor");
		powerScale = nbt.getFloat("pScale");
	}

	@Override
	public void initContainer(DataContainer container) {
		TileContainer cont = (TileContainer)container;
		cont.refInts = new int[4];
		cont.addItemSlot(new SlotTank(inventory, 0, 62, 34));
		cont.addItemSlot(new SlotTank(inventory, 1, 152, 34));
		for (int i = 0; i < 3; i++)
			cont.addItemSlot(new SlotItemHandler(inventory, 2 + i, 80, 16 + 18 * i));
		for (int i = 0; i < 3; i++)
			cont.addItemSlot(new SlotItemHandler(inventory, 5 + i, 116, 16 + 18 * i));
		cont.addPlayerInventory(8, 86);
		cont.addTankSlot(new TankSlot(tanks, 0, 44, 16, (byte)0x23));
		cont.addTankSlot(new TankSlot(tanks, 1, 134, 16, (byte)0x23));
	}

	@Override
	public int[] getSyncVariables() {
		return new int[]{Rw, Float.floatToIntBits(Estor), Float.floatToIntBits(Eneed), Float.floatToIntBits(Uc)};
	}

	@Override
	public void setSyncVariable(int i, int v) {
		switch(i) {
		case 0: Rw = v; break;
		case 1: Estor = Float.intBitsToFloat(v); break;
		case 2: Eneed = Float.intBitsToFloat(v); break;
		case 3: Uc = Float.intBitsToFloat(v); break;
		}
	}

	@Override
	public boolean transferStack(ItemStack item, int s, TileContainer container) {
		if (s < container.invPlayerS) container.mergeItemStack(item, container.invPlayerS, container.invPlayerE, false);
		else container.mergeItemStack(item, 2, 5, false);
		return true;
	}

}
