package cd4017be.automation.TileEntity;

import java.io.IOException;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import cd4017be.automation.gas.GasState;
import cd4017be.automation.shaft.GasContainer;
import cd4017be.automation.shaft.GasPhysics.IGasCon;
import cd4017be.automation.shaft.GasPhysics.IGasStorage;
import cd4017be.automation.shaft.ShaftComponent;
import cd4017be.automation.shaft.ShaftPhysics.IKineticComp;
import cd4017be.lib.TileEntityData;
import cd4017be.lib.templates.AutomatedTile;
import cd4017be.lib.util.Utils;

/**
 * 
 * @author CD4017BE
 *
 */
public class PneumaticPiston extends AutomatedTile implements IKineticComp, IGasCon {

	private ShaftComponent link;
	private GasContainer input, output;
	public static final float Amax = 0.1F;
	private boolean updateCon = false, run = false;
	
	public PneumaticPiston() {
		netData = new TileEntityData(0, 1, 3, 0);
	}

	@Override
	public void update() {
		if (worldObj.isRemote) return;
		if (updateCon) {
			input = this.getConContainer(netData.ints[0] & 0xf);
			output = this.getConContainer(netData.ints[0] >> 4 & 0xf);
			updateCon = false;
		}
	}
	
	private GasContainer getConContainer(int side) {
		if (side == 0) return null;
		side += this.getOrientation(); side %= 6;
		TileEntity te = Utils.getTileOnSide(this, (byte)side);
		return te != null && te instanceof IGasStorage ? ((IGasStorage)te).getGas() : null;
	}

	@Override
	public byte getConSide() {
		return this.getOrientation();
	}

	@Override
	public ShaftComponent getShaft() {
		return link;
	}

	@Override
	public void setShaft(ShaftComponent shaft) {
		link = shaft;
	}

	@Override
	public boolean valid() {
		return !this.tileEntityInvalid;
	}

	private static final float LIMIT = 1e-18F;
	
	@Override
	public float estimatedForce(float ds) {
		if (input != null && input.tile.isInvalid()) {input = null; updateCon = true;}
		if (output != null && output.tile.isInvalid()) {output = null; updateCon = true;}
		if (input == null || output == null || netData.floats[0] == 0 || input.network.gas.P() < LIMIT || ((netData.ints[0] & 0x100) == 0 ^ ((netData.ints[0] & 0x200) != 0 && worldObj.getStrongPower(pos) > 0))) {
			run = false;
			return 0;
		}
		run = true;
		if (netData.floats[1] == 0) netData.floats[1] = netData.floats[0];
		GasState in = input.network.gas, out = output.network.gas;
		float x0 = netData.floats[0] / in.V;
		x0 *= 2F - netData.floats[0] / netData.floats[1];
		return in.E() * x0 - out.E() * netData.floats[1] / out.V;
		/*
		float x0;
		if (ds <= 0) {
			x0 = Ain / input.V;
			x0 *= 2F - Ain / Aout;
			return input.E() * x0 - output.E() * Aout / output.V;
		}
		float x1, x2, dV = ds * Vin;
		x0 = Vin / (input.V + dV);
		x1 = output.nR / (output.nR + input.nR * x0 * ds);
		x2 = 1F - Vin / Vout + x1 * dV / output.V;
		x2 *= input.V / (input.V + dV);
		x1 *= Vout / output.V;
		x0 *= 1F + x2;
		 */
	}

	@Override
	public float work(float ds, float v) {
		if (!run) return netData.floats[2] = 0;
		GasState in = input.network.gas, out = output.network.gas;
		float E = in.E() + out.E();
		GasState s = in.extract(netData.floats[0] * ds);
		float sqA = s.T * s.nR * netData.floats[0] / out.P() / ds;
		if (sqA > Amax * Amax || sqA < 0 || Float.isNaN(sqA)) netData.floats[1] = Amax;
		else netData.floats[1] = (float)Math.sqrt(sqA);
		s.adiabat(netData.floats[1] * ds);
		out.inject(s);
		return netData.floats[2] = E - in.E() - out.E();
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		netData.floats[0] = nbt.getFloat("Ain");
		netData.floats[1] = nbt.getFloat("Aout");
		netData.ints[0] = nbt.getInteger("cfg");
		updateCon = true;
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		nbt.setFloat("Ain", netData.floats[0]);
		nbt.setFloat("Aout", netData.floats[1]);
		nbt.setInteger("cfg", netData.ints[0]);
        return super.writeToNBT(nbt);
	}

	@Override
	public void onNeighborBlockChange(Block b) {
		updateCon = true;
	}

	@Override
	public void onNeighborTileChange(BlockPos pos) {
		updateCon = true;
	}

	@Override
	protected void customPlayerCommand(byte cmd, PacketBuffer dis, EntityPlayerMP player) throws IOException {
		if (cmd == 0) {
			netData.floats[0] = dis.readFloat();
			if (netData.floats[0] < 0) netData.floats[0] = 0;
			else if (netData.floats[0] > Amax) netData.floats[0] = Amax;
		} else if (cmd == 1) {
			netData.ints[0] = dis.readInt();
			updateCon = true;
		}
	}

	public int getVscaled(int i) {
		return (int)(netData.floats[0] * (float)i / Amax);
	}

	@Override
	public boolean conGas(byte side) {
		side = (byte)((side + 6 - this.getOrientation()) % 6);
		return (netData.ints[0] & 0xf) == side || (netData.ints[0] >> 4 & 0xf) == side;
	}

}
