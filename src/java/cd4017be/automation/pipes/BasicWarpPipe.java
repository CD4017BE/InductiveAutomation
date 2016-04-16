package cd4017be.automation.pipes;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import cd4017be.lib.ModTileEntity;
import cd4017be.lib.templates.IComponent;
import cd4017be.lib.templates.SharedNetwork;
import cd4017be.lib.util.Obj2;

public class BasicWarpPipe implements IComponent<BasicWarpPipe, WarpPipePhysics> {

	public WarpPipePhysics network;
	public final ModTileEntity pipe;
	protected long Uid;
	public final byte[] con = new byte[6];
	public boolean redstone = false;
	public boolean updateCon = true;
	
	public BasicWarpPipe(ModTileEntity pipe) {
		this.pipe = pipe;
		this.Uid = 0;
	}

	public void initUID(long Uid) {
		if (this.Uid != 0) return;
		this.Uid = Uid;
		if (network == null) new WarpPipePhysics(this);
		else {
			network.components.remove(0);
			network.components.put(Uid, this);
			ConComp comp;
			for (int i = 0; i < 6; i++)
				if (con[i] >= 2) {
					comp = network.connectors.remove(SharedNetwork.SidedPosUID(0, i));
					if (comp != null) network.connectors.put(SharedNetwork.SidedPosUID(Uid, i), comp);
				}
		}
	}
	
	@Override
	public void setNetwork(WarpPipePhysics network) {
		this.network = network;
	}

	@Override
	public WarpPipePhysics getNetwork() {
		return network;
	}

	@Override
	public long getUID() {
		return Uid;
	}
	
	@Override
	public boolean canConnect(byte side) {
		return con[side] == 0;
	}

	@Override
	public List<Obj2<Long, Byte>> getConnections() {
		ArrayList<Obj2<Long, Byte>> list = new ArrayList<Obj2<Long, Byte>>();
		int dim = pipe.getWorld().provider.getDimension();
		for (int i = 0; i < 6; i++)
			if (con[i] == 0)
				list.add(new Obj2<Long, Byte>(SharedNetwork.ExtPosUID(pipe.getPos().offset(EnumFacing.VALUES[i]), dim), (byte)(i^1)));
		return list;
	}
	
	public static BasicWarpPipe readFromNBT(ModTileEntity tile, NBTTagCompound nbt) {
		BasicWarpPipe pipe = new BasicWarpPipe(tile);
		WarpPipePhysics physics = new WarpPipePhysics(pipe);
		NBTTagList list = nbt.getTagList("connectors", 10);
		for (int i = 0; i < list.tagCount(); i++) {
			ConComp con = ConComp.readFromNBT(pipe, list.getCompoundTagAt(i));
			if (con != null) physics.addConnector(pipe, con);
		}
		return pipe;
	}
	
	public void writeToNBT(NBTTagCompound nbt) {
		NBTTagList list = new NBTTagList();
		NBTTagCompound tag;
		ConComp comp;
		for (int i = 0; i < con.length; i++)
			if (con[i] > 0) {
				tag = ConComp.writeToNBT(con[i], (byte)i);
				comp = network.connectors.get(SharedNetwork.SidedPosUID(Uid, i));
				if (comp != null) comp.save(tag);
				list.appendTag(tag);
			}
		if (!list.hasNoTags()) nbt.setTag("connectors", list);
	}
	
	public void remove() {
		if (network != null) network.remove(this);
	}

}
