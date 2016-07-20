package cd4017be.automation.TileEntity;

import java.io.IOException;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import cd4017be.api.automation.IEnergy;
import cd4017be.api.automation.PipeEnergy;
import cd4017be.automation.Config;
import cd4017be.automation.shaft.HeatReservoir;
import cd4017be.automation.shaft.IHeatReservoir;
import cd4017be.automation.shaft.IHeatReservoir.IHeatStorage;
import cd4017be.lib.TileEntityData;
import cd4017be.lib.templates.AutomatedTile;

public class HeatingCoil extends AutomatedTile implements IHeatStorage, IEnergy {

	public HeatReservoir heat;
	private float powerScale = 0;
	
	public HeatingCoil() {
		netData = new TileEntityData(0, 2, 3, 0);
		heat = new HeatReservoir(10000F);
		energy = new PipeEnergy(Config.Umax[1], Config.Rcond[1]);
		netData.floats[0] = 300F;
		netData.ints[0] = Config.Rmin;
        powerScale = Config.Pscale;
	}
	
	@Override
	public void update() {
		super.update();
		if (worldObj.isRemote) return;
		heat.update(this);
		netData.floats[2] = heat.T;
		netData.floats[1] = (float)energy.Ucap;
        if (heat.T < netData.floats[0] && ((netData.ints[1] & 1) != 0 ^ ((netData.ints[1] & 2) != 0 && worldObj.getStrongPower(pos) > 0))) {
        	heat.addHeat((float)energy.getEnergy(0, netData.ints[0]));
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
			netData.ints[0] = dis.readShort();
            if (netData.ints[0] < Config.Rmin) netData.ints[0] = Config.Rmin;
            powerScale = (float)Math.sqrt(1.0D - 1.0D / (double)netData.ints[0]);
		} else if (cmd == 1) netData.floats[0] = dis.readFloat();
		else if (cmd == 2) netData.ints[1] = dis.readByte();
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
		return (int)(s * netData.floats[1] * netData.floats[1] / (float)netData.ints[0]) / 200000;
	}

}
