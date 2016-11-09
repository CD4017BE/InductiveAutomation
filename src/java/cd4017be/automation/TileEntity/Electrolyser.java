package cd4017be.automation.TileEntity;

import net.minecraft.network.PacketBuffer;

import java.io.IOException;

import cd4017be.api.automation.PipeEnergy;
import cd4017be.api.recipes.AutomationRecipes;
import cd4017be.api.recipes.AutomationRecipes.ElRecipe;
import cd4017be.automation.Config;
import cd4017be.lib.Gui.DataContainer;
import cd4017be.lib.Gui.DataContainer.IGuiData;
import cd4017be.lib.Gui.TileContainer;
import cd4017be.lib.Gui.TileContainer.TankSlot;
import cd4017be.lib.templates.AutomatedTile;
import cd4017be.lib.templates.TankContainer;
import cd4017be.lib.templates.Inventory;
import cd4017be.lib.util.Utils;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.items.SlotItemHandler;

public class Electrolyser extends AutomatedTile implements IGuiData {

	private float powerScale;
	private ElRecipe recipe;
	public int Rw;
	public float Estor, Eneed, Uc;
	
	public Electrolyser() {
		energy = new PipeEnergy(Config.Umax[1], Config.Rcond[1]);
		inventory = new Inventory(3, 3, null).group(0, 0, 1, Utils.IN).group(1, 1, 2, Utils.OUT).group(2, 2, 3, Utils.OUT);
		tanks = new TankContainer(3, 3).tank(0, Config.tankCap[1], Utils.IN, 0, -1).tank(1, Config.tankCap[1], Utils.OUT, -1, 1).tank(2, Config.tankCap[1], Utils.OUT, -1, 2);
		/**
		 * long: invcfg, tankcfg
		 * int: resistor
		 * float: progress, rcpEnergy, power
		 * fluid: input, output0, output1
		 */
		
		Rw = Config.Rmin;
		powerScale = Config.Pscale;
	}

	@Override
	public void update() 
	{
		super.update();
		if (worldObj.isRemote) return;
		if (recipe == null) {
			recipe = AutomationRecipes.getRecipeFor(inventory.items, 0, tanks.fluids[0]);
			if (recipe != null) {
				recipe.useRes(inventory, 0, tanks, 0);
				Eneed = recipe.energy;
			}
		}
		Uc = (float)energy.Ucap;
		double power = energy.getEnergy(0, Rw) / 1000D;
		if (recipe != null) {
			if (Estor < Eneed) {
				Estor += power;
				energy.Ucap *= powerScale;
			}
			if (Estor >= Eneed && recipe.output(inventory, 1, 2, tanks, 1, 2)) {
				recipe = null;
				Estor -= Eneed;
				Eneed = 0;
			}
		}
	}

	public float getProgress() {
		return Estor / Eneed;
	}

	public float getPower() {
		return Uc * Uc / (float)Rw / 200000F;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) 
	{
		super.readFromNBT(nbt);
		if (nbt.hasKey("recipe")) recipe = AutomationRecipes.readElRecipeFromNBT(nbt.getCompoundTag("recipe"));
		else recipe = null;
		Estor = nbt.getFloat("progress");
		Rw = nbt.getInteger("resistor");
		powerScale = nbt.getFloat("pScale");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) 
	{
		if (recipe != null) nbt.setTag("recipe", AutomationRecipes.writeElRecipeToNBT(recipe));
		nbt.setFloat("progress", Estor);
		nbt.setInteger("resistor", Rw);
		nbt.setFloat("pScale", powerScale);
		return super.writeToNBT(nbt);
	}

	@Override
	protected void customPlayerCommand(byte cmd, PacketBuffer dis, EntityPlayerMP player) throws IOException 
	{
		if (cmd == 0)
		{
			Rw = dis.readInt();
			if (Rw < Config.Rmin) Rw = Config.Rmin;
			powerScale = (float)Math.sqrt(1.0D - 1.0D / (double)Rw);
		}
	}

	@Override
	public void initContainer(DataContainer cont) {
		TileContainer container = (TileContainer)cont;
		container.addItemSlot(new SlotItemHandler(inventory, 0, 98, 34));
		container.addItemSlot(new SlotItemHandler(inventory, 1, 44, 34));
		container.addItemSlot(new SlotItemHandler(inventory, 2, 152, 34));
		
		container.addPlayerInventory(8, 86);
		
		container.addTankSlot(new TankSlot(tanks, 0, 98, 16, (byte)0x13));
		container.addTankSlot(new TankSlot(tanks, 1, 44, 16, (byte)0x13));
		container.addTankSlot(new TankSlot(tanks, 2, 152, 16, (byte)0x13));
	}

	@Override
	public boolean transferStack(ItemStack item, int s, TileContainer container) {
		if (s < container.invPlayerS) container.mergeItemStack(item, container.invPlayerS, container.invPlayerE, false);
		else container.mergeItemStack(item, 0, 1, false);
		return true;
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

}
