package cd4017be.automation.TileEntity;

import java.io.IOException;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import cd4017be.api.automation.PipeEnergy;
import cd4017be.automation.Config;
import cd4017be.automation.Objects;
import cd4017be.automation.shaft.ShaftComponent;
import cd4017be.automation.shaft.ShaftPhysics.IKineticComp;
import cd4017be.lib.Gui.DataContainer;
import cd4017be.lib.Gui.DataContainer.IGuiData;
import cd4017be.lib.templates.AutomatedTile;
import cd4017be.lib.util.Utils;

public class ElectricCoil extends AutomatedTile implements IKineticComp, IGuiData {

	public int type;
	private ShaftComponent link;
	public float Eflow;
	public int N, rstCfg;
	public float power, Uc;

	public ElectricCoil() {
		energy = new PipeEnergy(0, 0);
	}

	private void setWireType() {
		Block id = this.getBlockType();
		type = id == Objects.electricCoilC ? 0 : id == Objects.electricCoilA ? 1 : 2;
	}

	@Override
	public void update() {
		if (energy.Umax == 0) {
			this.setWireType();
			byte con = energy.sideCfg;
			energy = new PipeEnergy(Config.Umax[type], Config.Rcond[type]);
			energy.sideCfg = (byte)(con & ~(1 << getOrientation()));
			if (N == 0) N = Nmin[type];
		}
		super.update();
		if (worldObj.isRemote) return;
		Uc = energy.Ucap;
		if (link != null) return;
		if ((rstCfg & 1) == 0 ^ ((rstCfg & 2) != 0 && worldObj.getStrongPower(pos) > 0)) {
			power = 0;
			return;
		} 
		byte o = getOrientation();
		TileEntity te = Utils.getTileOnSide(this, o);
		ElectricCoil ec;
		if (te != null && te instanceof ElectricCoil && (ec = (ElectricCoil)te).getOrientation() == (o^1) && ec.energy.Umax != 0 && ((ec.rstCfg & 1) != 0 ^ ((ec.rstCfg & 2) != 0 && ec.worldObj.getStrongPower(ec.pos) > 0))) {
			if (o % 2 != 0) return;
			float x = (float)N, y = (float)ec.N;
			x *= x; y *= y;
			float Ex = energy.Ucap * energy.Ucap;
			float Ey = ec.energy.Ucap * ec.energy.Ucap;
			float dE0 = (Ey * x - Ex * y) / (x + y);
			float dE1 = dE0 + Eflow;
			Eflow = dE0 * 2F / (x / y + y / x);
			if (Ex + dE1 < 0) dE1 = -Ex;
			if (Ey - dE1 < 0) dE1 = Ey;
			energy.addEnergy(dE1);
			ec.energy.addEnergy(-dE1);
			ec.power = -(power = (float)dE1);
		} else power = 0;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		type = nbt.getByte("type");
		energy = new PipeEnergy(Config.Umax[type], Config.Rcond[type]);
		N = nbt.getInteger("N");
		rstCfg = nbt.getByte("cfg");
		Eflow = (float)nbt.getDouble("Eflow");
		super.readFromNBT(nbt);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		nbt.setByte("type", (byte)type);
		nbt.setInteger("N", N);
		nbt.setByte("cfg", (byte)rstCfg);
		nbt.setDouble("Eflow", Eflow);
		return super.writeToNBT(nbt);
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

	private static final float[] C = {20F, 10F, 4F};
	private static final float[] R = {0.1F, 0.2F, 0.5F};
	public static int[] Nmax = {200, 400, 1000};
	public static int[] Nmin = {5, 10, 25};
	public static final float Mag = 0.01F;

	private float F, v0;

	@Override
	public float estimatedForce(float ds) {
		if ((rstCfg & 1) == 0 ^ ((rstCfg & 2) != 0 && worldObj.getStrongPower(pos) > 0)) return F = 0;
		float B = this.getMagStr();
		if (B == 0) return F = 0;
		v0 = ds;
		float Ca = (float)N * C[type];
		float Uind = Ca * ds * B;
		float Ia = ((float)energy.Ucap - Uind) / (float)N / R[type];
		return F = Ca * Ia * B;
	}

	@Override
	public float work(float ds, float v) {
		if (F == 0) return power = 0;
		v -= v0;
		if (v * F > 0) {
			float Ca = (float)N * C[type] * this.getMagStr();
			float x = ((float)energy.Ucap / Ca - v0) / v;
			if (x > 0 && x < 1) F *= x;
		}
		float W = Math.min(F * ds, (float)(energy.Ucap * energy.Ucap));
		energy.addEnergy(power = -W);
		return W;
	}
	
	private float getMagStr() {
		if (link == null || link.type <= 0 || link.type > 4 || !(link.tile instanceof Shaft)) return 0F;
		if (((Shaft)link.tile).energy == null) return 1F;
		return (float)((Shaft)link.tile).energy.Ucap * Mag;
	}

	@Override
	protected void customPlayerCommand(byte cmd, PacketBuffer dis, EntityPlayerMP player) throws IOException {
		if (cmd == 0) N = Math.min(Nmax[type], Math.max(Nmin[type], dis.readInt()));
		else if (cmd == 1) rstCfg = dis.readByte();
	}

	@Override
	public void onPlayerCommand(PacketBuffer dis, EntityPlayerMP player) throws IOException {
		super.onPlayerCommand(dis, player);
		energy.sideCfg &= ~(1 << getOrientation());
	}

	@Override
	public void initContainer(DataContainer container) {
	}

	@Override
	public int[] getSyncVariables() {
		return new int[]{N, rstCfg, Float.floatToIntBits(power), Float.floatToIntBits(Uc)};
	}

	@Override
	public void setSyncVariable(int i, int v) {
		switch(i) {
		case 0: N = v; break;
		case 1: rstCfg = v; break;
		case 2: power = Float.intBitsToFloat(v); break;
		case 3: Uc = Float.intBitsToFloat(v); break;
		}
	}

}
