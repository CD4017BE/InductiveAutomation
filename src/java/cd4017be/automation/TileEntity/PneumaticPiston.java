package cd4017be.automation.TileEntity;

import java.io.IOException;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import cd4017be.automation.Objects;
import cd4017be.automation.gas.GasState;
import cd4017be.automation.shaft.GasContainer;
import cd4017be.automation.shaft.GasPhysics.IGasCon;
import cd4017be.automation.shaft.ShaftComponent;
import cd4017be.automation.shaft.ShaftPhysics.IKineticComp;
import cd4017be.lib.Gui.DataContainer.IGuiData;
import cd4017be.lib.templates.AutomatedTile;
import cd4017be.lib.util.Utils;

/**
 * 
 * @author CD4017BE
 *
 */
public class PneumaticPiston extends AutomatedTile implements IKineticComp, IGasCon, IGuiData {

	private ShaftComponent link;
	private GasContainer input, output;
	public static final float Amax = 0.1F;
	private boolean updateCon = false, run = false;
	public int netI0;
	public float netF0, netF1, netF2;

	@Override
	public void update() {
		if (worldObj.isRemote) return;
		if (updateCon) {
			input = this.getConContainer(netI0 & 0xf);
			output = this.getConContainer(netI0 >> 4 & 0xf);
			updateCon = false;
		}
	}

	private GasContainer getConContainer(int side) {
		if (side == 0) return null;
		side += this.getOrientation(); side %= 6;
		TileEntity te = Utils.getTileOnSide(this, (byte)side);
		return te != null ? te.getCapability(Objects.GAS_CAP, EnumFacing.VALUES[side^1]) : null;
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
		if (input != null && ((TileEntity)input.tile).isInvalid()) {input = null; updateCon = true;}
		if (output != null && ((TileEntity)input.tile).isInvalid()) {output = null; updateCon = true;}
		if (input == null || output == null || netF0 == 0 || input.network.gas.P() < LIMIT || ((netI0 & 0x100) == 0 ^ ((netI0 & 0x200) != 0 && worldObj.getStrongPower(pos) > 0))) {
			run = false;
			return 0;
		}
		run = true;
		if (netF1 == 0) netF1 = netF0;
		GasState in = input.network.gas, out = output.network.gas;
		float x0 = netF0 / in.V;
		x0 *= 2F - netF0 / netF1;
		return in.E() * x0 - out.E() * netF1 / out.V;
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
		if (!run) return netF2 = 0;
		GasState in = input.network.gas, out = output.network.gas;
		float E = in.E() + out.E();
		GasState s = in.extract(netF0 * ds);
		float sqA = s.T * s.nR * netF0 / out.P() / ds;
		if (sqA > Amax * Amax || sqA < 0 || Float.isNaN(sqA)) netF1 = Amax;
		else netF1 = (float)Math.sqrt(sqA);
		s.adiabat(netF1 * ds);
		out.inject(s);
		return netF2 = E - in.E() - out.E();
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		netF0 = nbt.getFloat("Ain");
		netF1 = nbt.getFloat("Aout");
		netI0 = nbt.getInteger("cfg");
		updateCon = true;
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		nbt.setFloat("Ain", netF0);
		nbt.setFloat("Aout", netF1);
		nbt.setInteger("cfg", netI0);
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
			netF0 = dis.readFloat();
			if (netF0 < 0) netF0 = 0;
			else if (netF0 > Amax) netF0 = Amax;
		} else if (cmd == 1) {
			netI0 = dis.readInt();
			updateCon = true;
		}
	}

	public int getVscaled(int i) {
		return (int)(netF0 * (float)i / Amax);
	}

	@Override
	public boolean conGas(byte side) {
		side = (byte)((side + 6 - this.getOrientation()) % 6);
		return (netI0 & 0xf) == side || (netI0 >> 4 & 0xf) == side;
	}

	@Override
	public int[] getSyncVariables() {
		return new int[]{netI0, Float.floatToIntBits(netF0), Float.floatToIntBits(netF1), Float.floatToIntBits(netF2)};
	}

	@Override
	public void setSyncVariable(int i, int v) {
		switch(i) {
		case 0: netI0 = v; break;
		case 1: netF0 = Float.intBitsToFloat(v); break;
		case 2: netF1 = Float.intBitsToFloat(v); break;
		case 3: netF2 = Float.intBitsToFloat(v); break;
		}
	}

}
