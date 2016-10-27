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

/**
 *
 * @author CD4017BE
 */
public class AntimatterAnihilator extends AutomatedTile implements IGuiData, IEnergyAccess {

	public static final int AMEnergy = 90000;
	private static final int MaxPower = 160;
	private static final int AMHeat = 1;
	public static final int MaxTemp = 800;
	private static final int CoolRes = 5;
	
	public int Uref, power, fracHe, heat;
	public float Estor;
	
	public AntimatterAnihilator() {
		energy = new PipeEnergy(Config.Umax[2], Config.Rcond[2]);
		tanks = new TankContainer(3, 3).tank(0, Config.tankCap[1], Utils.IN, 0, -1, Objects.L_heliumL).tank(1, Config.tankCap[1], Utils.OUT, -1, 1, Objects.L_heliumG).tank(2, Config.tankCap[1], Utils.IN, 2, -1, Objects.L_antimatter);
		inventory = new Inventory(3, 0, null);
		/**
		 * long: tanks
		 * int: voltage, power, drop, heat
		 * float: storage
		 * fluid: Red, Ox
		 */
		heat = MaxTemp;
	}
	
	@Override
	public void update() 
	{
		super.update();
		if (worldObj.isRemote) return;
		//energy
			float e = energy.getEnergy(Uref, 1);
			float e1 = this.addEnergy(e);
			if (e != e1) energy.addEnergy(-e1);
			else energy.Ucap = Uref;
			//cooling
			if (heat > 0) {
				int c = (heat - 1) / CoolRes + 1;	
				if (fracHe < c && tanks.getAmount(0) > 0) {
					fracHe += 800;
					tanks.drain(0, 1, true);
				}
				if (c > fracHe) c = fracHe;
				heat -= c;
				fracHe -= c;
				tanks.fill(1, new FluidStack(Objects.L_heliumG, c), true);
			}
		//antimatter
		int p = Math.min((MaxTemp - heat) / AMHeat, (int)Math.ceil((1F - Estor / this.getCapacity() * 2F) * MaxPower));
		if (p > tanks.getAmount(2)) p = tanks.getAmount(2);
		if (p > 0) {
				tanks.drain(2, p, true);
				heat += p * AMHeat;
				Estor += p * AMEnergy;
		} else p = 0;
		power = p;
	}

	public float storage() {
		return Estor / this.getCapacity();
	}

	public float getHeat() {
		return (float)heat / (float)MaxTemp;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) 
	{
		super.readFromNBT(nbt);
		Estor = nbt.getFloat("storage");
		Uref = nbt.getInteger("voltage");
		heat = nbt.getInteger("heat");
		fracHe = nbt.getInteger("drop");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) 
	{
		nbt.setFloat("storage", Estor);
		nbt.setInteger("voltage", Uref);
		nbt.setInteger("heat", heat);
		nbt.setInteger("drop", fracHe);
		return super.writeToNBT(nbt);
	}

	@Override
	protected void customPlayerCommand(byte cmd, PacketBuffer dis, EntityPlayerMP player) throws IOException 
	{
		if (cmd == 0) {
			Uref = dis.readShort();
		}
	}
	
	@Override
	public void initContainer(DataContainer cont) {
		TileContainer container = (TileContainer)cont;
		cont.refInts = new int[5];
		container.addItemSlot(new SlotTank(inventory, 0, 8, 34));
		container.addItemSlot(new SlotTank(inventory, 1, 26, 34));
		container.addItemSlot(new SlotTank(inventory, 2, 69, 34));
		
		container.addPlayerInventory(8, 86);
		
		container.addTankSlot(new TankSlot(tanks, 0, 8, 16, (byte)0x13));
		container.addTankSlot(new TankSlot(tanks, 1, 26, 16, (byte)0x13));
		container.addTankSlot(new TankSlot(tanks, 2, 62, 16, (byte)0x23));
	}

	@Override
	public float getStorage() 
	{
		return Estor;
	}

	@Override
	public float getCapacity() {
		return Config.Ecap[1] * 1000F;
	}

	@Override
	public float addEnergy(float e) 
	{
		Estor += e;
		if (Estor < 0) {
			e -= Estor;
			Estor = 0;
		} else if (Estor > this.getCapacity()) {
			e -= Estor - this.getCapacity();
			Estor = (float)this.getCapacity();
		}
		return e;
	}

	@Override
	public int[] getSyncVariables() {
		return new int[]{Uref, power, fracHe, heat, Float.floatToIntBits(Estor)};
	}

	@Override
	public void setSyncVariable(int i, int v) {
		switch(i) {
		case 0: Uref = v; break;
		case 1: power = v; break;
		case 2: fracHe = v; break;
		case 3: heat = v; break;
		case 4: Estor = Float.intBitsToFloat(v); break;
		}
	}

}
