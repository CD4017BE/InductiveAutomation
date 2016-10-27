package cd4017be.automation.TileEntity;

import net.minecraft.network.PacketBuffer;

import java.io.IOException;
import java.util.ArrayList;

import cd4017be.api.automation.PipeEnergy;
import cd4017be.api.energy.EnergyAPI;
import cd4017be.api.energy.EnergyAPI.IEnergyAccess;
import cd4017be.automation.Config;
import cd4017be.automation.Objects;
import cd4017be.lib.Gui.DataContainer;
import cd4017be.lib.Gui.DataContainer.IGuiData;
import cd4017be.lib.Gui.TileContainer;
import cd4017be.lib.templates.AutomatedTile;
import cd4017be.lib.templates.Inventory;
import cd4017be.lib.util.Utils;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.items.SlotItemHandler;

/**
 *
 * @author CD4017BE
 */
public class ESU extends AutomatedTile implements IGuiData, IEnergyAccess {

	public int type = 0;
	public int netI0;
	public float netF0, netF1;

	public ESU() {
		energy = new PipeEnergy(0, 0);
		inventory = new Inventory(2, 2, null).group(0, 0, 1, Utils.ACC).group(1, 1, 2, Utils.ACC);
	}

	protected void setWireType() {
		Block id = this.getBlockType();
		type = id == Objects.SCSU ? 0 : id == Objects.OCSU ? 1 : 2;
	}

	@Override
	public void update() {
		if (energy.Umax == 0) {
			this.setWireType();
			byte con = energy.sideCfg;
			energy = new PipeEnergy(Config.Umax[type], Config.Rcond[type]);
			energy.sideCfg = con;
		}
		super.update();
		if (worldObj.isRemote) return;
		netF0 -= EnergyAPI.get(inventory.items[0], 0).addEnergy(getStorage() - getCapacity());
		netF0 -= EnergyAPI.get(inventory.items[1], 0).addEnergy(getStorage());
		float d = energy.getEnergy(netI0, 1);
		float d1 = addEnergy(d);
		if (d1 == d) energy.Ucap = netI0;
		else if (d1 != 0) energy.addEnergy(-d1);
		netF1 = (float)d1;
	}

	public int getMaxStorage() {
		return Config.Ecap[type];
	}

	@Override
	public float addEnergy(float e) {
		if (energy.Umax == 0) return 0;
		if (netF0 + e < 0) {
			e = -netF0;
			netF0 = 0;
		} else if (netF0 + e > getCapacity()) {
			e = getCapacity() - netF0;
			netF0 = getCapacity();
		} else {
			netF0 += e;
		}
		return e;
	}

	@Override
	public void onPlaced(EntityLivingBase entity, ItemStack item) {
		netF0 = (float)EnergyAPI.get(item, -1).getStorage();
	}

	@Override
	public ArrayList<ItemStack> dropItem(IBlockState state, int fortune) {
		ItemStack item = new ItemStack(this.getBlockType());
		netF0 -= EnergyAPI.get(item, -1).addEnergy(netF0);
		ArrayList<ItemStack> list = new ArrayList<ItemStack>();
		list.add(item);
		return list;
	}

	@Override
	protected void customPlayerCommand(byte cmd, PacketBuffer dis, EntityPlayerMP player) throws IOException {
		if (cmd == 0) {
			netI0 = dis.readInt();
			if (netI0 < 0) netI0 = 0;
			if (netI0 > Config.Umax[type]) netI0 = Config.Umax[type];
		}
	}

	public int getStorageScaled(int s) {
		return (int)(netF0 / getCapacity() * (float)s);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		nbt.setByte("type", (byte)type);
		nbt.setFloat("storage", netF0);
		nbt.setInteger("voltage", netI0);
		return super.writeToNBT(nbt);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		type = nbt.getByte("type");
		energy = new PipeEnergy(Config.Umax[type], Config.Rcond[type]);
		super.readFromNBT(nbt);
		netF0 = nbt.getFloat("storage");
		netI0 = nbt.getInteger("voltage");
	}

	public int getDiff(int max) {
		int d = (int)(netF1 / this.getCapacity() * (float)max * 400F);
		if (d < -max) d = -max;
		if (d > max) d = max;
		return d;
	}

	@Override
	public void initContainer(DataContainer cont) {
		TileContainer container = (TileContainer)cont;
		cont.refInts = new int[3];
		container.addItemSlot(new SlotItemHandler(inventory, 0, 98, 16));
		container.addItemSlot(new SlotItemHandler(inventory, 1, 134, 16));
		
		container.addPlayerInventory(8, 86);
	}

	@Override
	public float getStorage() {
		return netF0;
	}

	@Override
	public float getCapacity() {
		return this.getMaxStorage() * 1000F;
	}

	@Override
	public int[] getSyncVariables() {
		return new int[]{netI0, Float.floatToIntBits(netF0), Float.floatToIntBits(netF1)};
	}

	@Override
	public void setSyncVariable(int i, int v) {
		switch(i) {
		case 0: netI0 = v; break;
		case 1: netF0 = Float.intBitsToFloat(v); break;
		case 2: netF1 = Float.intBitsToFloat(v); break;
		}
	}

}
