package cd4017be.automation.TileEntity;

import java.io.IOException;

import cd4017be.api.automation.PipeEnergy;
import cd4017be.automation.Config;
import cd4017be.lib.Gui.DataContainer;
import cd4017be.lib.Gui.TileContainer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.items.SlotItemHandler;

public class CEU extends ESU {

	public int timer = 0;
	public float Pmin = 0, Pmax = 0;

	public CEU() {
		super();
		energy = new PipeEnergy(Integer.MAX_VALUE, 0F);
	}

	@Override
	public void update() {
		if (worldObj.isRemote) return;
		super.update();
		timer++;
		if(power > Pmax) Pmax = power;
		else if(power < Pmin) Pmin = power;
	}

	@Override
	public float addEnergy(float e) {
		Estor += e;
		return e;
	}

	@Override
	public float getStorage() {
		return (float)Integer.MAX_VALUE * 0.5F;
	}

	@Override
	public float getCapacity() {
		return (float)Integer.MAX_VALUE;
	}

	@Override
	protected void customPlayerCommand(byte cmd, PacketBuffer dis, EntityPlayerMP player) throws IOException {
		if(cmd == 0) Uref = dis.readInt();
		else if(cmd == 1) {
			Estor = 0;
			Pmin = 0;
			Pmax = 0;
			timer = 0;
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		nbt.setFloat("Pmin", Pmin);
		nbt.setFloat("Pmax", Pmax);
		nbt.setInteger("timer", timer);
		return super.writeToNBT(nbt);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		float u = energy.Ucap;
		energy = new PipeEnergy(Integer.MAX_VALUE, 0F);
		energy.Ucap = u;
		Pmin = nbt.getFloat("Pmin");
		Pmax = nbt.getFloat("Pmax");
		timer = nbt.getInteger("timer");
	}

	@Override
	public float getDiff() {
		return power / Math.max(-Pmin, Pmax);
	}

	@Override
	public int[] getSyncVariables() {
		return new int[]{Uref, timer, Float.floatToIntBits(Estor), Float.floatToIntBits(power), Float.floatToIntBits(Pmin), Float.floatToIntBits(Pmax)};
	}

	@Override
	public void setSyncVariable(int i, int v) {
		switch(i) {
		case 0: Uref = v; break;
		case 1: timer = v; break;
		case 2: Estor = Float.intBitsToFloat(v); break;
		case 3: power = Float.intBitsToFloat(v); break;
		case 4: Pmin = Float.intBitsToFloat(v); break;
		case 5: Pmax = Float.intBitsToFloat(v); break;
		}
	}

	@Override
	public void initContainer(DataContainer cont) {
		TileContainer container = (TileContainer)cont;
		container.addItemSlot(new SlotItemHandler(inventory, 0, 98, 16));
		container.addItemSlot(new SlotItemHandler(inventory, 1, 134, 16));
		
		container.addPlayerInventory(8, 94);
	}

}
