package cd4017be.automation.TileEntity;

import net.minecraft.network.PacketBuffer;

import java.io.IOException;

import cd4017be.api.automation.PipeEnergy;
import cd4017be.api.recipes.AutomationRecipes;
import cd4017be.api.recipes.AutomationRecipes.CmpRecipe;
import cd4017be.automation.Config;
import cd4017be.lib.Gui.DataContainer;
import cd4017be.lib.Gui.DataContainer.IGuiData;
import cd4017be.lib.Gui.SlotItemType;
import cd4017be.lib.Gui.TileContainer;
import cd4017be.lib.templates.AutomatedTile;
import cd4017be.lib.templates.Inventory;
import cd4017be.lib.templates.Inventory.IAccessHandler;
import cd4017be.lib.util.ItemFluidUtil;
import cd4017be.lib.util.Utils;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.items.SlotItemHandler;

/**
 *
 * @author CD4017BE
 */
public class ElectricCompressor extends AutomatedTile implements IGuiData, IAccessHandler {

	public static float Energy = 200F;
	private float powerScale = 0;
	private boolean craftInvChange = true;
	public int Rw;
	public float Estor, Uc;

	public ElectricCompressor() {
		inventory = new Inventory(6, 5, this).group(0, 0, 1, Utils.IN).group(1, 1, 2, Utils.IN).group(2, 2, 3, Utils.IN).group(3, 3, 4, Utils.IN).group(4, 4, 5, Utils.OUT);
		energy = new PipeEnergy(Config.Umax[1], Config.Rcond[1]);
		Rw = Config.Rmin;
		powerScale = Config.Pscale;
	}

	@Override
	public void update() {
		super.update();
		if(worldObj.isRemote) return;
		//power
		Uc = (float)energy.Ucap;
		double power = energy.getEnergy(0, Rw) / 1000F;
		//Item process
		if (inventory.items[5] == null && craftInvChange) {
			CmpRecipe recipe = AutomationRecipes.getRecipeFor(inventory.items, 0, 4);
			if (recipe != null) {
				int n;
				for (int i = 0; i < recipe.input.length; i++)
					if ((n = AutomationRecipes.getStacksize(recipe.input[i])) > 0) inventory.extractItem(i, n, false);
				inventory.items[5] = recipe.output.copy();
			} else craftInvChange = false;
		}
		if (inventory.items[5] != null) {
			if (Estor < Energy) {
				Estor += power;
				energy.Ucap *= powerScale;
			}
			if (Estor >= Energy) {
				inventory.items[5] = ItemFluidUtil.putInSlots(inventory, inventory.items[5], 4);
				if (inventory.items[5] == null) Estor -= Energy;
			}
		}
	}

	public float getProgress() {
		return Estor / Energy;
	}

	public float getPower() {
		return Uc * Uc / (float)Rw / 200000F;
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		nbt.setFloat("progress", Estor);
		nbt.setShort("resistor", (short)Rw);
		nbt.setFloat("pScale", powerScale);
		return super.writeToNBT(nbt);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		Estor = nbt.getFloat("progress");
		Rw = nbt.getShort("resistor");
		powerScale = nbt.getFloat("pScale");
		craftInvChange = true;
	}

	@Override
	protected void customPlayerCommand(byte cmd, PacketBuffer dis, EntityPlayerMP player) throws IOException {
		if (cmd == 0) {
			Rw = dis.readShort();
			if (Rw < Config.Rmin) Rw = Config.Rmin;
			powerScale = (float)Math.sqrt(1.0D - 1.0D / (double)Rw);
		}
	}

	@Override
	public void initContainer(DataContainer cont) {
		TileContainer container = (TileContainer)cont;
		for (int j = 0; j < 2; j++)
			for (int i = 0; i < 2; i++)
				container.addItemSlot(new SlotItemHandler(inventory, i + 2 * j, 62 + 18 * i, 25 + 18 * j));
		container.addItemSlot(new SlotItemType(inventory, 4, 134, 34));
		
		container.addPlayerInventory(8, 86);
	}

	@Override
	public boolean transferStack(ItemStack item, int s, TileContainer container) {
		if (s < container.invPlayerS) container.mergeItemStack(item, container.invPlayerS, container.invPlayerE, false);
		else container.mergeItemStack(item, 0, 4, false);
		return true;
	}

	@Override
	public int[] getSyncVariables() {
		return new int[]{Rw, Float.floatToIntBits(Estor), Float.floatToIntBits(Uc)};
	}

	@Override
	public void setSyncVariable(int i, int v) {
		switch(i) {
		case 0: Rw = v; break;
		case 1: Estor = Float.intBitsToFloat(v); break;
		case 2: Uc = Float.intBitsToFloat(v); break;
		}
	}

	@Override
	public void setSlot(int g, int s, ItemStack item) {
		inventory.items[s] = item;
		if (s < 4) craftInvChange = true;
	}

}
