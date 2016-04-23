package cd4017be.automation.shaft;

import java.util.HashMap;
import java.util.Iterator;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import cd4017be.lib.ModTileEntity;
import cd4017be.lib.templates.SharedNetwork;
import cd4017be.lib.util.Utils;

public class ShaftPhysics extends SharedNetwork<ShaftComponent, ShaftPhysics> {

	/**
	 * set of connected mechanical components
	 */
	public final HashMap<Long, IKineticComp> connectors = new HashMap<Long, IKineticComp>();
	/**
	 * the "handle" radius at which components interact. 
	 * Used for calculating the effective rotation mass of shaft parts.
	 */
	public static final float R = (float)(1D / Math.PI);
	/**
	 * @field m [kg] the total effective rotation mass of the whole shaft structure
	 * @field v [m/s] the speed of the shaft
	 * @field s [m] the shaft's current position (1m = one full rotation)
	 */
	public float m = 0, v = 0, s = 0;
	private float diff = 1;
	
	public ShaftPhysics(ShaftComponent core) {
		super(core);
		m = core.m;
	}
	
	public ShaftPhysics(HashMap<Long, ShaftComponent> comps) {
		super(comps);
	}

	@Override
	public ShaftPhysics onSplit(HashMap<Long, ShaftComponent> comps) {
		ShaftPhysics network = new ShaftPhysics(comps);
		for (ShaftComponent comp : comps.values()) {
			for (int i = 0; i < 6; i++)
				if ((comp.con >> i & 1) != 0) {
					long id = SharedNetwork.SidedPosUID(comp.getUID(), i);
					network.connectors.put(id, this.connectors.remove(id));
				}
			network.m += comp.m;
		}
		network.s = this.s;
		network.v = this.v;
		this.m -= network.m;
		return network;
	}
	
	@Override
	public void onMerged(ShaftPhysics network) {
		super.onMerged(network);
		this.connectors.putAll(network.connectors);
		this.v *= this.m;
		this.v += network.v * network.m;
		this.m += network.m;
		this.v /= this.m;
	}

	@Override
	public void remove(ShaftComponent comp) {
		if (this.components.containsKey(comp.getUID())) {
			this.m -= comp.m;
			for (int i = 0; i < 6; i++)
				if ((comp.con >> i & 1) != 0)
					this.connectors.remove(SharedNetwork.SidedPosUID(comp.getUID(), i));
		}
		super.remove(comp);
	}

	/**
	 * add a component to the shafts mechanical structure
	 * @param comp
	 */
	public void addCon(ShaftComponent comp, IKineticComp con) {
		byte side = con.getConSide();
		
		connectors.put(SharedNetwork.SidedPosUID(comp.getUID(), side), con);
		comp.con |= 1 << side;
	}
	
	public void updateLink(ShaftComponent shaft) {
		TileEntity te;
		IKineticComp con;
		for (byte i = 0; i < 6; i++) {
			te = Utils.getTileOnSide(shaft.shaft, i);
			if (te == null) continue;
			if (shaft.canConnect(i) && te instanceof IShaft) {
				ShaftComponent p = ((IShaft)te).getShaft();
				if (p.canConnect((byte)(i^1))) add(p);
			} else if (te instanceof IKineticComp && (con = (IKineticComp)te).getConSide() == (i^1) && shaft.supports(con, i)) {
				this.addCon(shaft, con);
			}
		}
	}
	
	@Override
	protected void updatePhysics() {
		float dt = 0.05F;
		float ds = v * dt, F = 0, a, E;
		if (core.shaft.getWorld().isRemote) {
			s += ds;//just simulate constant rotation speed
			if (s > 1F) s -= Math.floor(s);
			return;
		}
		Iterator<IKineticComp> it = connectors.values().iterator();
		while (it.hasNext()) {//get the estimated total force of all components on the shaft
			IKineticComp comp = it.next();
			if (!comp.valid()) {
				it.remove();
				comp.getShaft().con &= ~(1 << comp.getConSide());
			}
			else F += comp.estimatedForce(s, ds);
		}
		a = F / m; //convert to acceleration
		if (v == 0 && a <= 0) return;
		if (-a * dt > v) dt = -v / a; //if rotation would stop, only calculate till that point
		ds = 0.5F * a * dt * dt + v * dt; //now use the real distance moved
		E = 0.5F * m * v * v; //get the kinetic Energy of the shaft and add the work of all components to it
		for (IKineticComp comp : connectors.values()) {
			E += comp.work(s, ds);
		}
		v = (float)Math.sqrt(2F * E / m); //convert back to speed
		if (Float.isNaN(v)) v = 0;
		s += ds; //move the shaft forward
		if (s > 1F) s -= Math.floor(s);
		if (v == 0 || (diff += dt) >= 1F) updateClient();
	}
	
	private void updateClient() {
		diff = 0;
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setFloat("RotVel", v);
		nbt.setFloat("RotPos", s);
		ModTileEntity tile = core.shaft;
		BlockPos pos = tile.getPos();
		MinecraftServer.getServer().getConfigurationManager().sendToAllNear(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 64D, tile.dimensionId, new S35PacketUpdateTileEntity(pos, -1, nbt));
	}
	
	public boolean isCore(ModTileEntity te) {
		return core != null && core.shaft == te;
	}
	
	public BlockPos pos() {
		return core.shaft.getPos();
	}
	
	public byte ax() {
		return (byte)core.shaft.getBlockMetadata();
	}
	
	public AxisAlignedBB boundingBox(ModTileEntity te) {
		BlockPos pos = te.getPos();
		AxisAlignedBB box = new AxisAlignedBB(pos, pos);
		if (!this.isCore(te)) return box;
		for (ShaftComponent comp : components.values()) {
			pos = comp.shaft.getPos();
			box = box.addCoord(pos.getX(), pos.getY(), pos.getZ());
		}
		return box.offset(0.5D, 0.5D, 0.5D).expand(1D, 1D, 1D);
	}

	public interface IKineticComp {
		public byte getConSide();
		public ShaftComponent getShaft();
		public void setShaft(ShaftComponent shaft);
		public boolean valid();
		/**
		 * calculate the estimated force of this part on the shaft. 
		 * This value doesn't need to be exact, but it shouldn't be greater than the actual force added during work().
		 * Otherwise the shaft could probably get negative kinetic energy.
		 * @param s [m] the current shaft position
		 * @param ds [m] the distance the shaft will move during this calculation tick
		 * @return [kg*m/s²] the force on the shaft (positive values accelerate, negative slow down)
		 */
		public float estimatedForce(float s, float ds);
		/**
		 * update the mechanical physics of this connected part
		 * @param s [m] the current shaft position
		 * @param ds [m] the distance the shaft will move during this calculation tick
		 * @return [J] the amount of work added to the shaft
		 */
		public float work(float s, float ds);
	}
	
	public interface IShaft {
		public ShaftComponent getShaft();
	}

}
