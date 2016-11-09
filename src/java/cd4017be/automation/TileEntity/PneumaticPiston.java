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
	public int cfg;
	public float Ain, Aout, power;

	@Override
	public void update() {
		if (worldObj.isRemote) return;
		if (updateCon) {
			input = this.getConContainer(cfg & 0xf);
			output = this.getConContainer(cfg >> 4 & 0xf);
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
		if (input == null || output == null || Ain == 0 || input.network.gas.P() < LIMIT || ((cfg & 0x100) == 0 ^ ((cfg & 0x200) != 0 && worldObj.getStrongPower(pos) > 0))) {
			run = false;
			return 0;
		}
		run = true;
		if (Aout == 0) Aout = Ain;
		GasState in = input.network.gas, out = output.network.gas;
		float x0 = Ain / in.V;
		x0 *= 2F - Ain / Aout;
		return in.E() * x0 - out.E() * Aout / out.V;
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
		if (!run) return power = 0;
		GasState in = input.network.gas, out = output.network.gas;
		float E = in.E() + out.E();
		GasState s = in.extract(Ain * ds);
		float sqA = s.T * s.nR * Ain / out.P() / ds;
		if (sqA > Amax * Amax || sqA < 0 || Float.isNaN(sqA)) Aout = Amax;
		else Aout = (float)Math.sqrt(sqA);
		s.adiabat(Aout * ds);
		out.inject(s);
		return power = E - in.E() - out.E();
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		Ain = nbt.getFloat("Ain");
		Aout = nbt.getFloat("Aout");
		cfg = nbt.getInteger("cfg");
		updateCon = true;
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		nbt.setFloat("Ain", Ain);
		nbt.setFloat("Aout", Aout);
		nbt.setInteger("cfg", cfg);
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
			Ain = dis.readFloat();
			if (Ain < 0) Ain = 0;
			else if (Ain > Amax) Ain = Amax;
		} else if (cmd == 1) {
			cfg = dis.readInt();
			updateCon = true;
		}
	}

	public int getVscaled(int i) {
		return (int)(Ain * (float)i / Amax);
	}

	@Override
	public boolean conGas(byte side) {
		side = (byte)((side + 6 - this.getOrientation()) % 6);
		return (cfg & 0xf) == side || (cfg >> 4 & 0xf) == side;
	}

	@Override
	public int[] getSyncVariables() {
		return new int[]{cfg, Float.floatToIntBits(Ain), Float.floatToIntBits(Aout), Float.floatToIntBits(power)};
	}

	@Override
	public void setSyncVariable(int i, int v) {
		switch(i) {
		case 0: cfg = v; break;
		case 1: Ain = Float.intBitsToFloat(v); break;
		case 2: Aout = Float.intBitsToFloat(v); break;
		case 3: power = Float.intBitsToFloat(v); break;
		}
	}

}
