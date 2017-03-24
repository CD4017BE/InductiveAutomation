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

	public int Uref, power, fracH2, fracO2;
	public float Estor;
	
	public FuelCell() {
		energy = new PipeEnergy(Config.Ugenerator[5], Config.Rcond[1]);
		inventory = new Inventory(3, 0, null);
		tanks = new TankContainer(3, 3).tank(0, Config.tankCap[1], Utils.IN, 0, -1, Objects.L_hydrogenG).tank(1, Config.tankCap[1], Utils.IN, 1, -1, Objects.L_oxygenG).tank(2, Config.tankCap[1], Utils.OUT, -1, 2, Objects.L_waterG);
	}
	
	@Override
	public void update() 
	{
		super.update();
		if (worldObj.isRemote) return;
		float e = energy.getEnergy(Uref, 1);
		float e1 = this.addEnergy(e);
		if (e1 != e) energy.addEnergy(-e1);
		else energy.Ucap = Uref;
		int p = (int)Math.ceil((1F - Estor / (Config.Ecap[0] * 1000F) * 2F) * Config.Pgenerator[5] * 0.5F);
		if (p * 2 > tanks.getAmount(0)) p = tanks.getAmount(0) / 2;
		if (p > tanks.getAmount(1)) p = tanks.getAmount(1);
		if (p > 0) {
			tanks.drain(0, p * 2, true);
			tanks.drain(1, p, true);
			tanks.fill(2, new FluidStack(Objects.L_waterG, p * 3), true);
			Estor += p * 2000F;
		} else p = 0;
		power = p * 2;
	}

	public float storage() {
		return Estor / (float)Config.Ecap[0] / 1000F;
	}

	public float getPower() {
		return (float)power / Config.Pgenerator[5];
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) 
	{
		super.readFromNBT(nbt);
		Estor = nbt.getFloat("storage");
		Uref = nbt.getInteger("voltage");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) 
	{
		nbt.setFloat("storage", Estor);
		nbt.setInteger("voltage", Uref);
		return super.writeToNBT(nbt);
	}

	@Override
	protected void customPlayerCommand(byte cmd, PacketBuffer dis, EntityPlayerMP player) throws IOException 
	{
		if (cmd == 0) {
			Uref = dis.readShort();
			if (Uref > Config.Ugenerator[5]) Uref = Config.Ugenerator[5];
		}
	}

	@Override
	public void initContainer(DataContainer cont) {
		TileContainer container = (TileContainer)cont;
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
		return Estor;
	}

	@Override
	public float getCapacity() {
		return Config.Ecap[0] * 1000F;
	}

	@Override
	public float addEnergy(float e) 
	{
		Estor += e;
		if (Estor < 0) {
			e -= Estor;
			Estor = 0;
		} else if (Estor > Config.Ecap[0] * 1000) {
			e -= Estor - Config.Ecap[0] * 1000;
			Estor = Config.Ecap[0] * 1000;
		}
		return e;
	}

	@Override
	public int[] getSyncVariables() {
		return new int[]{Uref, power, Float.floatToIntBits(Estor)};
	}

	@Override
	public void setSyncVariable(int i, int v) {
		switch(i) {
		case 0: Uref = v; break;
		case 1: power = v; break;
		case 2: Estor = Float.intBitsToFloat(v); break;
		}
	}

}
