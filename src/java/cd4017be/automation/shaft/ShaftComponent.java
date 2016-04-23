package cd4017be.automation.shaft;

import java.util.Arrays;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import cd4017be.automation.shaft.ShaftPhysics.IKineticComp;
import cd4017be.lib.ModTileEntity;
import cd4017be.lib.templates.IComponent;
import cd4017be.lib.templates.SharedNetwork;
import cd4017be.lib.util.Obj2;

public class ShaftComponent implements IComponent<ShaftComponent, ShaftPhysics> {

	public ShaftPhysics network;
	public final ModTileEntity shaft;
	protected long Uid;
	public byte con = 0;
	public float m;
	public boolean updateCon;
	
	public ShaftComponent(ModTileEntity shaft, float m) {
		this.shaft = shaft;
		this.Uid = 0;
		this.m = m;
	}
	
	public void initUID(long Uid) {
		if (this.Uid != 0) return;
		this.Uid = Uid;
		if (network == null) new ShaftPhysics(this);
		else {
			network.components.remove(0);
			network.components.put(Uid, this);
		}
		updateCon = true;
	}

	@Override
	public void setNetwork(ShaftPhysics network) {
		this.network = network;
	}

	@Override
	public ShaftPhysics getNetwork() {
		return network;
	}

	@Override
	public long getUID() {
		return Uid;
	}

	@Override
	public boolean canConnect(byte side) {
		return side / 2 == shaft.getBlockMetadata();
	}

	@Override
	public List<Obj2<Long, Byte>> getConnections() {
		int n = (shaft.getBlockMetadata() * 2) % 6;
		EnumFacing dir = EnumFacing.VALUES[n];
		return Arrays.asList(new Obj2<Long, Byte>(SharedNetwork.ExtPosUID(shaft.getPos().offset(dir), 0), (byte)n),
				new Obj2<Long, Byte>(SharedNetwork.ExtPosUID(shaft.getPos().offset(dir, -1), 0), (byte)(n + 1)));
	}
	
	public static ShaftComponent readFromNBT(ModTileEntity tile, NBTTagCompound nbt) {
		ShaftComponent pipe = new ShaftComponent(tile, nbt.getFloat("mass"));
		ShaftPhysics physics = new ShaftPhysics(pipe);
		physics.v = nbt.getFloat("rotVel");
		physics.s = nbt.getFloat("rotPos");
		return pipe;
	}
	
	public void writeToNBT(NBTTagCompound nbt) {
		nbt.setFloat("mass", m);
		if (network != null) {
			nbt.setFloat("rotVel", network.v);
			nbt.setFloat("rotPos", network.s);
		}
	}
	
	public void remove() {
		if (network != null) network.remove(this);
	}

	public boolean supports(IKineticComp con2, byte i) {
		return (con >> i & 1) == 0;
	}

	public boolean onClicked(EntityPlayer player, ItemStack item) {
		if (item == null && !player.isSneaking()) {
			network.v += 0.1F / network.m;//TODO remove later?
			return true;
		}
		return false;
	}

}
