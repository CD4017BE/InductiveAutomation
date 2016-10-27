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
	public int netI0, netI1;
	public float netF0, netF1, netF2;

	public HeatingCoil() {
		heat = new HeatReservoir(10000F);
		energy = new PipeEnergy(Config.Umax[1], Config.Rcond[1]);
		netF0 = 300F;
		netI0 = Config.Rmin;
		powerScale = Config.Pscale;
	}
	
	@Override
	public void update() {
		super.update();
		if (worldObj.isRemote) return;
		heat.update(this);
		netF2 = heat.T;
		netF1 = (float)energy.Ucap;
		if (heat.T < netF0 && ((netI1 & 1) != 0 ^ ((netI1 & 2) != 0 && worldObj.getStrongPower(pos) > 0))) {
			heat.addHeat((float)energy.getEnergy(0, netI0));
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
			netI0 = dis.readShort();
			if (netI0 < Config.Rmin) netI0 = Config.Rmin;
			powerScale = (float)Math.sqrt(1.0D - 1.0D / (double)netI0);
		} else if (cmd == 1) netF0 = dis.readFloat();
		else if (cmd == 2) netI1 = dis.readByte();
	}

	@Override
	public IHeatReservoir getHeat(byte side) {
		return heat;
	}

	@Override
	public float getHeatRes(byte side) {
		return side == this.getOrientation() ? HeatReservoir.def_con : HeatReservoir.def_discon;
	}

	public int getPowerScaled(int s) {
		return (int)(s * netF1 * netF1 / (float)netI0) / 200000;
	}

	@Override
	public void initContainer(DataContainer container) {
		container.refInts = new int[5];
	}

	@Override
	public int[] getSyncVariables() {
		return new int[]{netI0, netI1, Float.floatToIntBits(netF0), Float.floatToIntBits(netF1), Float.floatToIntBits(netF1)};
	}

	@Override
	public void setSyncVariable(int i, int v) {
		switch(i) {
		case 0: netI0 = v; break;
		case 1: netI1 = v; break;
		case 2: netF0 = Float.intBitsToFloat(v); break;
		case 3: netF1 = Float.intBitsToFloat(v); break;
		case 4: netF2 = Float.intBitsToFloat(v); break;
		}
	}

}
