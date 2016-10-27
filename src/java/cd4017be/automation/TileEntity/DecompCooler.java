package cd4017be.automation.TileEntity;

import net.minecraft.network.PacketBuffer;

import java.io.IOException;

import cd4017be.api.automation.PipeEnergy;
import cd4017be.api.recipes.AutomationRecipes;
import cd4017be.api.recipes.AutomationRecipes.CoolRecipe;
import cd4017be.automation.Config;
import cd4017be.lib.Gui.DataContainer;
import cd4017be.lib.Gui.DataContainer.IGuiData;
import cd4017be.lib.Gui.TileContainer;
import cd4017be.lib.Gui.TileContainer.TankSlot;
import cd4017be.lib.templates.AutomatedTile;
import cd4017be.lib.templates.Inventory;
import cd4017be.lib.templates.TankContainer;
import cd4017be.lib.util.Utils;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.items.SlotItemHandler;

/**
 *
 * @author CD4017BE
 */
public class DecompCooler extends AutomatedTile implements IGuiData
{
	private float powerScale; 
	private CoolRecipe recipe;
	public int Rw;
	public float netF0, netF1, Uc;
	
	public DecompCooler() {
		inventory = new Inventory(4, 4, null).group(0, 0, 1, Utils.IN).group(1, 1, 2, Utils.IN).group(2, 2, 3, Utils.OUT).group(3, 3, 4, Utils.OUT);
		energy = new PipeEnergy(Config.Umax[1], Config.Rcond[1]);
		tanks = new TankContainer(4, 4).tank(0, Config.tankCap[1], Utils.IN, 0, -1).tank(1, Config.tankCap[1], Utils.IN, 1, -1).tank(2, Config.tankCap[1], Utils.OUT, -1, 2).tank(3, Config.tankCap[1], Utils.OUT, -1, 3);
		Rw = Config.Rmin;
		powerScale = Config.Pscale;
	}

	@Override
	public void update() 
	{
		super.update();
		if (this.worldObj.isRemote) return;
		if (recipe == null) {
			recipe = AutomationRecipes.getRecipeFor(inventory.items, 0, tanks.fluids[0], tanks.fluids[1]);
			if (recipe != null) {
				recipe.useRes(inventory, 0, 1, tanks, 0, 1);
				netF1 = recipe.energy;
			}
		}
		Uc = (float)energy.Ucap;
		double power = energy.getEnergy(0, Rw) / 1000D;
		if (recipe != null) {
			if (netF0 < netF1) {
				netF0 += power;
				energy.Ucap *= powerScale;
			}
			if (netF0 >= netF1 && recipe.output(inventory, 2, 3, tanks, 2, 3)) {
				recipe = null;
				netF0 -= netF1;
				netF1 = 0;
			}
		}
	}
	
	public float getProgress() {
		return netF0 / netF1;
	}

	public float getPower() {
		return Uc * Uc / (float)Rw / 200000F;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) 
	{
		super.readFromNBT(nbt);
		if (nbt.hasKey("recipe")) recipe = AutomationRecipes.readCoolRecipeFromNBT(nbt.getCompoundTag("recipe"));
		else recipe = null;
		netF0 = nbt.getFloat("progress");
		Rw = nbt.getInteger("resistor");
		powerScale = nbt.getFloat("pScale");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) 
	{
		if (recipe != null) nbt.setTag("recipe", AutomationRecipes.writeCoolRecipeToNBT(recipe));
		nbt.setFloat("progress", netF0);
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
		cont.refInts = new int[4];
		container.addItemSlot(new SlotItemHandler(inventory, 0, 44, 34));
		container.addItemSlot(new SlotItemHandler(inventory, 1, 116, 34));
		container.addItemSlot(new SlotItemHandler(inventory, 2, 80, 34));
		container.addItemSlot(new SlotItemHandler(inventory, 3, 152, 34));
		
		container.addPlayerInventory(8, 86);
		
		container.addTankSlot(new TankSlot(tanks, 0, 44, 16, (byte)0x13));
		container.addTankSlot(new TankSlot(tanks, 2, 80, 16, (byte)0x13));
		container.addTankSlot(new TankSlot(tanks, 1, 116, 16, (byte)0x13));
		container.addTankSlot(new TankSlot(tanks, 3, 152, 16, (byte)0x13));
	}

	@Override
	public int[] getSyncVariables() {
		return new int[]{Rw, Float.floatToIntBits(netF0), Float.floatToIntBits(netF1), Float.floatToIntBits(Uc)};
	}

	@Override
	public void setSyncVariable(int i, int v) {
		switch(i) {
		case 0: Rw = v; break;
		case 1: netF0 = Float.intBitsToFloat(v); break;
		case 2: netF1 = Float.intBitsToFloat(v); break;
		case 3: Uc = Float.intBitsToFloat(v); break;
		}
	}

}
