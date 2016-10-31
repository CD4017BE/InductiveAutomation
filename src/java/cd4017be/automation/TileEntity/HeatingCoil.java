package cd4017be.automation.TileEntity;

import java.io.IOException;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import cd4017be.api.automation.PipeEnergy;
import cd4017be.automation.Config;
import cd4017be.automation.shaft.HeatReservoir;
import cd4017be.automation.shaft.IHeatReservoir;
import cd4017be.automation.shaft.IHeatReservoir.IHeatStorage;
import cd4017be.lib.Gui.DataContainer;
import cd4017be.lib.Gui.DataContainer.IGuiData;
import cd4017be.lib.templates.AutomatedTile;

public class HeatingCoil extends AutomatedTile implements IHeatStorage, IGuiData {

	public HeatReservoir heat;
	private float powerScale = 0;
	public int Rw, rstCtr;
	public float Tref, Uc, temp;

	public HeatingCoil() {
		heat = new HeatReservoir(10000F);
		energy = new PipeEnergy(Config.Umax[1], Config.Rcond[1]);
		Tref = 300F;
		Rw = Config.Rmin;
		powerScale = Config.Pscale;
	}
	
	@Override
	public void update() {
		super.update();
		if (worldObj.isRemote) return;
		heat.update(this);
		temp = heat.T;
		Uc = (float)energy.Ucap;
		if (heat.T < Tref && ((rstCtr & 1) != 0 ^ ((rstCtr & 2) != 0 && worldObj.getStrongPower(pos) > 0))) {
			heat.addHeat((float)energy.getEnergy(0, Rw));
			energy.Ucap *= powerScale;
		}
	}
	
	@Override
	public void onNeighborBlockChange(Block b) {
		heat.check = true;
	}

	@Override
	public void onNeighborTileChange(BlockPos pos) {
		heat.check = true;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		heat.load(nbt, "heat");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		heat.save(nbt, "heat");
		return super.writeToNBT(nbt);
	}

	@Override
	protected void customPlayerCommand(byte cmd, PacketBuffer dis, EntityPlayerMP player) throws IOException {
		if (cmd == 0) {
			Rw = dis.readShort();
			if (Rw < Config.Rmin) Rw = Config.Rmin;
			powerScale = (float)Math.sqrt(1.0D - 1.0D / (double)Rw);
		} else if (cmd == 1) Tref = dis.readFloat();
		else if (cmd == 2) rstCtr = dis.readByte();
	}

	@Override
	public IHeatReservoir getHeat(byte side) {
		return heat;
	}

	@Override
	public float getHeatRes(byte side) {
		return side == this.getOrientation() ? HeatReservoir.def_con : HeatReservoir.def_discon;
	}

	public float getPower() {
		return Uc * Uc / (float)Rw / 200000F;
	}

	@Override
	public void initContainer(DataContainer container) {
		container.refInts = new int[5];
	}

	@Override
	public int[] getSyncVariables() {
		return new int[]{Rw, rstCtr, Float.floatToIntBits(Tref), Float.floatToIntBits(Uc), Float.floatToIntBits(Uc)};
	}

	@Override
	public void setSyncVariable(int i, int v) {
		switch(i) {
		case 0: Rw = v; break;
		case 1: rstCtr = v; break;
		case 2: Tref = Float.intBitsToFloat(v); break;
		case 3: Uc = Float.intBitsToFloat(v); break;
		case 4: temp = Float.intBitsToFloat(v); break;
		}
	}

}
