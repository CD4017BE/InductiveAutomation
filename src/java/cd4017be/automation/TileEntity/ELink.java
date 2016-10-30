package cd4017be.automation.TileEntity;

import net.minecraft.network.PacketBuffer;

import java.io.IOException;

import cofh.api.energy.IEnergyProvider;
import cofh.api.energy.IEnergyReceiver;
import cd4017be.api.automation.PipeEnergy;
import cd4017be.api.energy.EnergyAPI;
import cd4017be.api.energy.EnergyAPI.IEnergyAccess;
import cd4017be.automation.Config;
import cd4017be.automation.Objects;
import cd4017be.lib.Gui.DataContainer;
import cd4017be.lib.Gui.DataContainer.IGuiData;
import cd4017be.lib.Gui.TileContainer;
import cd4017be.lib.templates.AutomatedTile;
import cd4017be.lib.util.Utils;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import static cd4017be.api.energy.EnergyAPI.RF_value;

/**
 *
 * @author CD4017BE
 */
public class ELink extends AutomatedTile implements IGuiData, IEnergyReceiver, IEnergyProvider {//TODO better TESLA support

	public int type = 0;
	public float energyDemand = 0, energyMoved = 0;
	public int Umin, Umax, rstCtr;
	public float Uref, netF1, netF2, netF3;
	
	public ELink() {
		energy = new PipeEnergy(0, 0);
	}
	
	protected void setWireType() {
		Block id = this.getBlockType();
		type = id == Objects.link ? 1 : 2;
	}

	@Override
	public void update() {
		if (energy.Umax == 0) {
			this.setWireType();
			byte con = energy.sideCfg;
			energy = new PipeEnergy(Config.Umax[type], Config.Rcond[type]);
			energy.sideCfg = (byte)(con & ~(1 << getOrientation()));
		}
		super.update();
		if (this.worldObj.isRemote) return;
		boolean active = ((rstCtr & 2) != 0 ? worldObj.isBlockPowered(getPos()) : false) ^ (rstCtr & 1) != 0;
		int s = this.getOrientation();
		IEnergyAccess storage = EnergyAPI.get(Utils.getTileOnSide(this, (byte)s), EnumFacing.VALUES[this.getOrientation()^1]);
		Uref = (float)energy.Ucap;
		netF1 = (float)storage.getStorage();
		netF2 = (float)storage.getCapacity();
		float Uref = netF2 > 0 ? Umin + (Umax - Umin) * (netF1 / netF2) : (Umin + Umax) / 2F;
		if (active) {
			float e = energy.getEnergy(Uref, 1);
			energyMoved = storage.addEnergy(e);
			energyDemand = energyMoved - e;
			if (energyDemand == 0) energy.Ucap = Uref;
			else if (energyMoved != 0) energy.addEnergy(-energyMoved);
		} else {
			energyDemand = energyMoved = 0;
		}
		netF3 = (float)energyMoved; energyMoved = 0;
	}

	public float getStorage() {
		return netF1 / netF2;
	}

	public float getVoltage() {
		if (Umin < Umax) return Uref < Umin ? 0 : (Uref - Umin) / (Umax - Umin);
		else if (Umin > Umax) return Uref > Umax ? 0 : (Uref - Umax) / (Umax - Umin);
		else return Uref > Umax ? -0.5F : 0.5F;
	}

	@Override
	protected void customPlayerCommand(byte cmd, PacketBuffer dis, EntityPlayerMP player) throws IOException {
		if (cmd == 0) Umin = dis.readInt();
		else if (cmd == 1) Umax = dis.readInt();
		else if (cmd == 2) rstCtr = dis.readInt();
	}

	@Override
	public void onPlayerCommand(PacketBuffer dis, EntityPlayerMP player) throws IOException {
		super.onPlayerCommand(dis, player);
		energy.sideCfg &= ~(1 << getOrientation());
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		nbt.setByte("mode", (byte)rstCtr);
		nbt.setInteger("Umin", Umin);
		nbt.setInteger("Umax", Umax);
		return super.writeToNBT(nbt);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		rstCtr = nbt.getByte("mode");
		Umin = nbt.getInteger("Umin");
		Umax = nbt.getInteger("Umax");
	}
	
	@Override
	public void initContainer(DataContainer container) {
		container.refInts = new int[7];
		((TileContainer)container).addPlayerInventory(8, 86);
	}

	//RedstoneFlux
	
	@Override
	public int receiveEnergy(EnumFacing dir, int e, boolean sim) {
		if (energy == null || energyDemand <= 0) return 0;
		float x = energy.getEnergy(energy.Umax, 1);
		if (energyDemand + x > 0) energyDemand = -x;
		if ((x = (float)e * RF_value) > energyDemand) e = (int)Math.floor((x = energyDemand) / RF_value);
		if (!sim) {
			energy.addEnergy(x);
			energyDemand -= x;
			energyMoved += x;
		}
		return e;
	}

	@Override
	public int extractEnergy(EnumFacing dir, int e, boolean sim) {
		if (energy == null || energyDemand >= 0) return 0;
		float x = energy.getEnergy(0, 1);
		if (energyDemand + x < 0) energyDemand = -x;
		if ((x = -(float)e * RF_value) < energyDemand) e = (int)Math.floor(-(x = energyDemand) / RF_value);
		if (!sim) {
			energy.addEnergy(x);
			energyDemand -= x;
			energyMoved += x;
		}
		return e;
	}

	@Override
	public int getEnergyStored(EnumFacing dir) {
		return energy == null ? 0 : (int)(energy.getEnergy(0, 1) / RF_value);
	}

	@Override
	public int getMaxEnergyStored(EnumFacing dir) {
		return energy == null ? 0 : (int)Math.floor(energy.Umax * energy.Umax / RF_value);
	}
	
	@Override
	public boolean canConnectEnergy(EnumFacing dir) {
		return dir.getIndex() == this.getOrientation();
	}

	@Override
	public int[] getSyncVariables() {
		return new int[]{Umin, Umax, rstCtr, Float.floatToIntBits(Uref), Float.floatToIntBits(netF1), Float.floatToIntBits(netF2), Float.floatToIntBits(netF3)};
	}

	@Override
	public void setSyncVariable(int i, int v) {
		switch(i) {
		case 0: Umin = v; break;
		case 1: Umax = v; break;
		case 2: rstCtr = v; break;
		case 3: Uref = Float.intBitsToFloat(v); break;
		case 4: netF1 = Float.intBitsToFloat(v); break;
		case 5: netF2 = Float.intBitsToFloat(v); break;
		case 6: netF3 = Float.intBitsToFloat(v); break;
		}
	}

}
