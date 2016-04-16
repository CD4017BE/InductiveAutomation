package cd4017be.automation.pipes;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import cd4017be.automation.TileEntity.InterdimHole;
import cd4017be.lib.templates.SharedNetwork;
import cd4017be.lib.util.Obj2;

public class InterdimWarpPipe extends BasicWarpPipe {

	public InterdimWarpPipe(InterdimHole pipe) {
		super(pipe);
	}

	@Override
	public boolean canConnect(byte side) {
		return true;
	}

	@Override
	public List<Obj2<Long, Byte>> getConnections() {
		ArrayList<Obj2<Long, Byte>> list = new ArrayList<Obj2<Long, Byte>>();
		for (int i = 0; i < 6; i++)
			list.add(new Obj2<Long, Byte>(SharedNetwork.ExtPosUID(pipe.getPos().offset(EnumFacing.VALUES[i]), pipe.dimensionId), (byte)(i^1)));
		InterdimHole pipe = (InterdimHole)this.pipe;
		list.add(new Obj2<Long, Byte>(SharedNetwork.ExtPosUID(new BlockPos(pipe.linkX, pipe.linkY, pipe.linkZ), pipe.linkD), (byte)-1));
		return list;
	}

}
