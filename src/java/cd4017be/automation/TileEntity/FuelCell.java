package cd4017be.automation.TileEntity;

import net.minecraft.network.PacketBuffer;

import java.io.IOException;

import cd4017be.api.automation.PipeEnergy;
import cd4017be.api.energy.EnergyAPI.IEnergyAccess;
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
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;

public class FuelCell extends AutomatedTile implements IEnergyAccess, IGuiData {

	public int netI0, netI1, netI2, netI3;
	public float netF0;
	
	public FuelCell() {
		energy = new PipeEnergy(Config.Umax[2], Config.Rcond[2]);
		inventory = new Inventory(3, 0, null);
		tanks = new TankContainer(3, 3).tank(0, Config.tankCap[1], Utils.IN, 0, -1, Objects.L_hydrogenG).tank(1, Config.tankCap[1], Utils.IN, 1, -1, Objects.L_oxygenG).tank(2, Config.tankCap[1], Utils.OUT, -1, 2, Objects.L_waterG);
		/**
		 * long: tanks
		 * int: voltage, power, mlHydrogen, mlOxygen
		 * float: storage
		 * fluid: Red, Ox
		 */	
	}
	
	@Override
	public void update() 
	{
		super.update();
		if (worldObj.isRemote) return;
		float e = energy.getEnergy(netI0, 1);
		float e1 = this.addEnergy(e);
		if (e1 != e) energy.addEnergy(-e1);
		else energy.Ucap = netI0;
		int p = (int)Math.ceil((1F - netF0 / (Config.Ecap[0] * 1000F) * 2F) * Config.PfuelCell * 0.5F);
		if (p * 2 > tanks.getAmount(0)) p = tanks.getAmount(0) / 2;
		if (p > tanks.getAmount(1)) p = tanks.getAmount(1);
		if (p > 0) {
			tanks.drain(0, p * 2, true);
			tanks.drain(1, p, true);
			tanks.fill(2, new FluidStack(Objects.L_waterG, p * 3), true);
			netF0 += p * 2000F;
		} else p = 0;
		netI1 = p * 2;
	}
	
	public int getStorageScaled(int s)
	{
		return (int)(netF0 * s / (Config.Ecap[0] * 1000));
	}
	
	public int getPowerScaled(int s)
	{
		return netI1 * s / Config.PfuelCell;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) 
	{
		super.readFromNBT(nbt);
	netF0 = nbt.getFloat("storage");
	netI0 = nbt.getInteger("voltage");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) 
	{
		nbt.setFloat("storage", netF0);
		nbt.setInteger("voltage", netI0);
		return super.writeToNBT(nbt);
	}

	@Override
	protected void customPlayerCommand(byte cmd, PacketBuffer dis, EntityPlayerMP player) throws IOException 
	{
		if (cmd == 0) {
			netI0 = dis.readShort();
		}
	}

	@Override
	public void initContainer(DataContainer cont) {
		TileContainer container = (TileContainer)cont;
		cont.refInts = new int[5];
		container.addItemSlot(new SlotTank(inventory, 0, 8, 34));
		container.addItemSlot(new SlotTank(inventory, 1, 26, 34));
		container.addItemSlot(new SlotTank(inventory, 2, 62, 34));
		
		container.addPlayerInventory(8, 86);
		
		container.addTankSlot(new TankSlot(tanks, 0, 8, 16, (byte)0x13));
		container.addTankSlot(new TankSlot(tanks, 1, 26, 16, (byte)0x13));
		container.addTankSlot(new TankSlot(tanks, 2, 62, 16, (byte)0x13));
	}

	@Override
	public float getStorage() 
	{
		return netF0;
	}

	@Override
	public float getCapacity() {
		return Config.Ecap[0] * 1000F;
	}

	@Override
	public float addEnergy(float e) 
	{
		netF0 += e;
		if (netF0 < 0) {
			e -= netF0;
			netF0 = 0;
		} else if (netF0 > Config.Ecap[0] * 1000) {
			e -= netF0 - Config.Ecap[0] * 1000;
			netF0 = Config.Ecap[0] * 1000;
		}
		return e;
	}

	@Override
	public int[] getSyncVariables() {
		return new int[]{netI0, netI1, netI2, netI3, Float.floatToIntBits(netF0)};
	}

	@Override
	public void setSyncVariable(int i, int v) {
		switch(i) {
		case 0: netI0 = v; break;
		case 1: netI1 = v; break;
		case 2: netI2 = v; break;
		case 3: netI3 = v; break;
		case 4: netF0 = Float.intBitsToFloat(v); break;
		}
	}

}
