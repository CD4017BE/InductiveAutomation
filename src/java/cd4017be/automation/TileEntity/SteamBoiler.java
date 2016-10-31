package cd4017be.automation.TileEntity;

import net.minecraft.network.PacketBuffer;

import java.io.IOException;

import cd4017be.automation.Config;
import cd4017be.automation.Objects;
import cd4017be.lib.Gui.DataContainer;
import cd4017be.lib.Gui.DataContainer.IGuiData;
import cd4017be.lib.Gui.SlotTank;
import cd4017be.lib.Gui.TileContainer;
import cd4017be.lib.Gui.TileContainer.TankSlot;
import cd4017be.lib.templates.AutomatedTile;
import cd4017be.lib.templates.Inventory;
import cd4017be.lib.templates.TankContainer;
import cd4017be.lib.util.Utils;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.SlotItemHandler;

/**
 *
 * @author CD4017BE
 */
public class SteamBoiler extends AutomatedTile implements IGuiData {

	public byte waterType;
	public int fuel, burn, temp, cook, blow;

	public SteamBoiler() {
		inventory = new Inventory(5, 1, null).group(0, 0, 3, Utils.IN);
		tanks = new TankContainer(2, 2).tank(0, Config.tankCap[1], Utils.IN, 3, -1, Objects.L_water, Objects.L_biomass).tank(1, Config.tankCap[1], Utils.OUT, -1, 4, Objects.L_steam);
		blow = 1;
	}

	@Override
	public void update() {
		super.update();
		if (this.worldObj.isRemote) return;
		int steamOut = 0;
		if (burn > 0)
		{
			int d = Math.min(blow, burn);
			burn -= d;
			temp += d;
			steamOut += Config.Lsteam_Kfuel_burning * blow;
			if (temp > Config.maxK_steamBoiler) temp = Config.maxK_steamBoiler;
		}
		if (burn <= 0 && temp < Config.maxK_steamBoiler)
		{
			burnItem();
		}
		int nw = tanks.fluids[0] == null || tanks.fluids[0].getFluid().equals(Objects.L_water) ? Config.get_LWater_Cooking() : Config.get_LBiomass_Cooking();
		if (temp >= Config.K_Cooking_steamBoiler && tanks.getAmount(0) >= nw)
		{
			cook++;
			if (cook >= getCookTime())
			{
				if (tanks.fluids[0].getFluid().equals(Objects.L_water))
				{
					temp -= Config.K_Cooking_steamBoiler;
				} else
				{
					steamOut += Config.steam_biomass_burning;
				}
				tanks.drain(0, nw, true);
				steamOut += Config.get_LSteam_Cooking();
				cook = 0;
			}
		}
		tanks.fill(1, new FluidStack(Objects.L_steam, steamOut), true);
	}
	
	private void burnItem()
	{
		if (worldObj.isBlockPowered(getPos())) return;
		for (int i = 0; i < inventory.items.length; i++)
		{
			if (inventory.items[i] == null) continue;
			int n = TileEntityFurnace.getItemBurnTime(inventory.items[i]);
			if (n != 0)
			{
				fuel = n;
				burn += fuel;
				inventory.extractItem(i, 1, false);
				return;
			}
		}
	}

	public float getBurn() {
		return (float)burn / (float)fuel;
	}

	public float getCook() {
		return (float)cook / (float)getCookTime();
	}

	private int getCookTime() {
		if (temp == 0) return Integer.MAX_VALUE;
		return 40000 / temp;
	}

	public float getTemp() {
		return (float)temp / (float)Config.maxK_steamBoiler;
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) 
	{
		nbt.setInteger("burn", burn);
		nbt.setInteger("cook", cook);
		nbt.setInteger("temp", temp);
		nbt.setInteger("blow", blow);
		return super.writeToNBT(nbt);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) 
	{
		super.readFromNBT(nbt);
		burn = nbt.getInteger("burn");
		cook = nbt.getInteger("cook");
		temp = nbt.getInteger("temp");
		blow = nbt.getInteger("blow");
		fuel = burn;
	}
	
	@Override
	protected void customPlayerCommand(byte cmd, PacketBuffer dis, EntityPlayerMP player) throws IOException 
	{
		if (cmd == 0) {
			blow--;
			if (blow < 1) blow = 1;
		} else if (cmd == 1) {
			blow++;
			if (blow > 8) blow = 8;
		}
	}

	@Override
	public void initContainer(DataContainer cont) {
		TileContainer container = (TileContainer)cont;
		cont.refInts = new int[5];
		for (int i = 0; i < 3; i++)
			container.addItemSlot(new SlotItemHandler(inventory, i, 8, 16 + 18 * i));
		container.addItemSlot(new SlotTank(inventory, 3, 71, 34));
		container.addItemSlot(new SlotTank(inventory, 4, 143, 34));
		container.addPlayerInventory(8, 86);
		
		container.addTankSlot(new TankSlot(tanks, 0, 62, 16, (byte)0x23));
		container.addTankSlot(new TankSlot(tanks, 1, 134, 16, (byte)0x23));
	}

	@Override
	public boolean transferStack(ItemStack item, int s, TileContainer container) {
		if (s < container.invPlayerS) container.mergeItemStack(item, container.invPlayerS, container.invPlayerE, false);
		else container.mergeItemStack(item, 0, 3, false);
		return true;
	}

	@Override
	public int[] getSyncVariables() {
		return new int[]{fuel, burn, temp, cook, blow};
	}

	@Override
	public void setSyncVariable(int i, int v) {
		switch(i) {
		case 0: fuel = v; break;
		case 1: burn = v; break;
		case 2: temp = v; break;
		case 3: cook = v; break;
		case 4: blow = v; break;
		}
	}

}
