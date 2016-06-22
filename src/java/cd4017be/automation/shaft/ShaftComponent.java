package cd4017be.automation.shaft;

import java.util.Arrays;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import cd4017be.api.automation.PipeEnergy;
import cd4017be.automation.Config;
import cd4017be.automation.Objects;
import cd4017be.automation.shaft.ShaftPhysics.IKineticComp;
import cd4017be.lib.templates.AutomatedTile;
import cd4017be.lib.templates.IComponent;
import cd4017be.lib.templates.SharedNetwork;
import cd4017be.lib.util.Obj2;

public class ShaftComponent implements IComponent<ShaftComponent, ShaftPhysics> {

	public ShaftPhysics network;
	public final AutomatedTile shaft;
	protected long Uid;
	public byte con = 0;
	public float m;
	public boolean updateCon;
	public byte type = 0;
	
	public ShaftComponent(AutomatedTile shaft, float m) {
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
	
	public static ShaftComponent readFromNBT(AutomatedTile tile, NBTTagCompound nbt) {
		ShaftComponent pipe = new ShaftComponent(tile, nbt.getFloat("mass"));
		pipe.type = nbt.getByte("type");
		if (pipe.type >= 2 && pipe.type < 5) tile.energy = new PipeEnergy(Config.Umax[pipe.type - 2], Config.Rcond[pipe.type - 2]);
		ShaftPhysics physics = new ShaftPhysics(pipe);
		physics.v = nbt.getFloat("rotVel");
		physics.s = nbt.getFloat("rotPos");
		return pipe;
	}
	
	public void writeToNBT(NBTTagCompound nbt) {
		nbt.setFloat("mass", m);
		nbt.setByte("type", type);
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
		float mass = m;
		if (player == null || (player.isSneaking() && item == null)) {
			switch (type) {
			case 1: shaft.dropStack(new ItemStack(Items.redstone)); break;
			case 2: shaft.dropStack(new ItemStack(Objects.electricCoilC)); break;
			case 3: shaft.dropStack(new ItemStack(Objects.electricCoilA)); break;
			case 4: shaft.dropStack(new ItemStack(Objects.electricCoilH)); break;
			case 5: shaft.dropStack(new ItemStack(Blocks.iron_block)); break;
			default: return false;
			}
			type = 0;
			mass = 1000F;
		} else if (type == 0 && !player.isSneaking() && item != null) {
			Item i = item.getItem();
			if (i == Items.redstone) {type = 1; mass = 2000F;}
			else if (i == Item.getItemFromBlock(Objects.electricCoilC)) {type = 2; mass = 2000F;}
			else if (i == Item.getItemFromBlock(Objects.electricCoilA)) {type = 3; mass = 2000F;}
			else if (i == Item.getItemFromBlock(Objects.electricCoilH)) {type = 4; mass = 2000F;}
			else if (i == Item.getItemFromBlock(Blocks.iron_block)) {type = 5; mass = 16000F;}
			else return false;
			player.setCurrentItemOrArmor(0, --item.stackSize <= 0 ? null : item);
		} else return false;
		if (type >= 2 && type < 5 && shaft.energy == null) shaft.energy = new PipeEnergy(Config.Umax[type - 2], Config.Rcond[type - 2]);
		else if (type == 0 && shaft.energy != null) shaft.energy = null;
		if (mass != m) network.changeMass(this, mass);
		shaft.markUpdate();
		return true;
	}
	
	private static final double[] loss = {Math.sqrt(0.99F), Math.sqrt(0.999F), 1};
	
	public double getCoilLoss() {
		return loss[type >= 2 && type < 4 ? type - 2 : 2];
	}

}
