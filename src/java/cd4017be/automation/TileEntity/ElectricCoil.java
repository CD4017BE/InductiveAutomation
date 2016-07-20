package cd4017be.automation.TileEntity;

import java.io.IOException;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import cd4017be.api.automation.IEnergy;
import cd4017be.api.automation.PipeEnergy;
import cd4017be.automation.Config;
import cd4017be.automation.Objects;
import cd4017be.automation.shaft.ShaftComponent;
import cd4017be.automation.shaft.ShaftPhysics.IKineticComp;
import cd4017be.lib.TileEntityData;
import cd4017be.lib.templates.AutomatedTile;
import cd4017be.lib.util.Utils;

public class ElectricCoil extends AutomatedTile implements IKineticComp, IEnergy {

	public int type;
	private ShaftComponent link;
	public double Eflow;
	
	public ElectricCoil() {
		energy = new PipeEnergy(0, 0);
		netData = new TileEntityData(0, 2, 2, 0);
	}

	private void setWireType()
    {
        Block id = this.getBlockType();
        type = id == Objects.electricCoilC ? 0 : id == Objects.electricCoilA ? 1 : 2;
    }
    
    @Override
    public void update() 
    {
        if (energy.Umax == 0) {
            this.setWireType();
            byte con = energy.con;
            energy = new PipeEnergy(Config.Umax[type], Config.Rcond[type]);
            energy.con = con;
            if (netData.ints[0] == 0) netData.ints[0] = Nmin[type];
        }
        super.update();
        if (worldObj.isRemote) return;
        netData.floats[1] = (float)energy.Ucap;
        if (link != null) return;
        if ((netData.ints[1] & 1) == 0 ^ ((netData.ints[1] & 2) != 0 && worldObj.getStrongPower(pos) > 0)) {
        	netData.floats[0] = 0;
        	return;
        } 
        byte o = getOrientation();
        TileEntity te = Utils.getTileOnSide(this, o);
        ElectricCoil ec;
        if (te != null && te instanceof ElectricCoil && (ec = (ElectricCoil)te).getOrientation() == (o^1) && ec.energy.Umax != 0 && ((ec.netData.ints[1] & 1) != 0 ^ ((ec.netData.ints[1] & 2) != 0 && ec.worldObj.getStrongPower(ec.pos) > 0))) {
        	if (o % 2 != 0) return;
        	double x = (double)netData.ints[0], y = (double)ec.netData.ints[0];
        	x *= x; y *= y;
            double Ex = energy.Ucap * energy.Ucap;
            double Ey = ec.energy.Ucap * ec.energy.Ucap;
            double dE0 = (Ey * x - Ex * y) / (x + y);
            double dE1 = dE0 + Eflow;
            Eflow = dE0 * 2D / (x / y + y / x);
            if (Ex + dE1 < 0) dE1 = -Ex;
            if (Ey - dE1 < 0) dE1 = Ey;
            energy.addEnergy(dE1);
            ec.energy.addEnergy(-dE1);
            ec.netData.floats[0] = -(netData.floats[0] = (float)dE1);
        } else netData.floats[0] = 0;
    }
    
    @Override
    public void readFromNBT(NBTTagCompound nbt) 
    {
        type = nbt.getByte("type");
        energy = new PipeEnergy(Config.Umax[type], Config.Rcond[type]);
        netData.ints[0] = nbt.getInteger("N");
        netData.ints[1] = nbt.getByte("cfg");
        Eflow = nbt.getDouble("Eflow");
        super.readFromNBT(nbt);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) 
    {
        nbt.setByte("type", (byte)type);
        nbt.setInteger("N", netData.ints[0]);
        nbt.setByte("cfg", (byte)netData.ints[1]);
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
	public static final int[] Nmax = {200, 400, 1000};
	public static final int[] Nmin = {5, 10, 25};
	public static final float Mag = 0.01F;
	
	private float F, v0;
	
	@Override
	public float estimatedForce(float ds) {
		if ((netData.ints[1] & 1) == 0 ^ ((netData.ints[1] & 2) != 0 && worldObj.getStrongPower(pos) > 0)) return F = 0;
		float B = this.getMagStr();
		if (B == 0) return F = 0;
		v0 = ds;
		float Ca = (float)netData.ints[0] * C[type];
		float Uind = Ca * ds * B;
		float Ia = ((float)energy.Ucap - Uind) / (float)netData.ints[0] / R[type];
		return F = Ca * Ia * B;
	}

	@Override
	public float work(float ds, float v) {
		if (F == 0) return netData.floats[0] = 0;
		v -= v0;
		if (v * F > 0) {
			float Ca = (float)netData.ints[0] * C[type] * this.getMagStr();
			float x = ((float)energy.Ucap / Ca - v0) / v;
			if (x > 0 && x < 1) F *= x;
		}
		float W = Math.min(F * ds, (float)(energy.Ucap * energy.Ucap));
		energy.addEnergy(netData.floats[0] = -W);
		return W;
	}
	
	private float getMagStr() {
		if (link == null || link.type <= 0 || link.type > 4) return 0F;
		if (link.shaft.energy == null) return 1F;
		return (float)link.shaft.energy.Ucap * Mag;
	}

	@Override
	protected void customPlayerCommand(byte cmd, PacketBuffer dis, EntityPlayerMP player) throws IOException {
		if (cmd == 0) netData.ints[0] = Math.min(Nmax[type], Math.max(Nmin[type], dis.readInt()));
		else if (cmd == 1) netData.ints[1] = dis.readByte();
	}

	@Override
	public PipeEnergy getEnergy(byte side) {
		return side == this.getOrientation() ? null : super.getEnergy(side);
	}
	
}
