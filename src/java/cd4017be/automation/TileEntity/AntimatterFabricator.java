package cd4017be.automation.TileEntity;

import net.minecraft.network.PacketBuffer;

import java.io.IOException;

import cd4017be.api.automation.PipeEnergy;
import cd4017be.automation.Config;
import cd4017be.automation.Objects;
import cd4017be.lib.Gui.DataContainer;
import cd4017be.lib.Gui.DataContainer.IGuiData;
import cd4017be.lib.Gui.SlotTank;
import cd4017be.lib.Gui.TileContainer;
import cd4017be.lib.Gui.TileContainer.TankSlot;
import cd4017be.lib.templates.AutomatedTile;
import cd4017be.lib.templates.Inventory;
import cd4017be.lib.templates.TankContainer;
import cd4017be.lib.util.Utils;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;

/**
 *
 * @author CD4017BE
 */
public class AntimatterFabricator extends AutomatedTile implements IGuiData
{
	public static final int AMEnergy = 14400000;
	public static final int AMamount = 160;
	public static final int AMHeat = 160;
	
	public int Uref, fracHe, rs;
	public float Estor, power;
	
	public AntimatterFabricator()
	{
		energy = new PipeEnergy(Config.Umax[2], Config.Rcond[2]);
		inventory = new Inventory(3, 0, null);
		tanks = new TankContainer(3, 3).tank(0, Config.tankCap[1], Utils.IN, 0, -1, Objects.L_heliumL).tank(1, Config.tankCap[1], Utils.OUT, -1, 1, Objects.L_heliumG).tank(2, Config.tankCap[1], Utils.OUT, -1, 2, Objects.L_antimatter);
	}
	
	@Override
	public void update() 
	{
		super.update();
		if (this.worldObj.isRemote) return;
		//cooling
		if (fracHe < AMHeat && tanks.getAmount(0) > 0) {
			fracHe += 800;
			tanks.drain(0, 1, true);
		}
		//energy
		if (Estor < AMEnergy && ((rs & 1) != 0 ^ ((rs & 2) != 0 && worldObj.isBlockPowered(pos)))) {
			double e = energy.getEnergy(Uref, 1);
			if (e < 0) e = 0;
			power = (float)e / 1000F;
			Estor += e;
			energy.Ucap = Uref;
		} else power = 0;
		//antimatter
		if (Estor >= AMEnergy && fracHe >= AMHeat && tanks.getSpace(2) >= AMamount) {
			Estor -= AMEnergy;
			fracHe -= AMHeat;
			tanks.fill(1, new FluidStack(Objects.L_heliumG, AMHeat), true);
			tanks.fill(2, new FluidStack(Objects.L_antimatter, AMamount), true);
		}
	}
	
	public float getPower() {
		return Estor / AMEnergy;
	}

	@Override
	protected void customPlayerCommand(byte cmd, PacketBuffer dis, EntityPlayerMP player) throws IOException 
	{
		if (cmd == 0) {
			Uref = dis.readInt();
		} else if (cmd == 1) {
			rs = dis.readByte();
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) 
	{
		nbt.setInteger("voltage", Uref);
		nbt.setInteger("drop", fracHe);
		nbt.setByte("mode", (byte)rs);
		nbt.setFloat("storage", Estor);
		return super.writeToNBT(nbt);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) 
	{
		super.readFromNBT(nbt);
		Uref = nbt.getInteger("voltage");
		fracHe = nbt.getInteger("drop");
		rs = nbt.getByte("mode");
		Estor = nbt.getFloat("storage");
	}
	
	@Override
	public void initContainer(DataContainer cont) {
		TileContainer container = (TileContainer)cont;
		cont.refInts = new int[5];
		container.addItemSlot(new SlotTank(inventory, 0, 8, 34));
		container.addItemSlot(new SlotTank(inventory, 1, 26, 34));
		container.addItemSlot(new SlotTank(inventory, 2, 143, 34));
		
		container.addPlayerInventory(8, 86);
		
		container.addTankSlot(new TankSlot(tanks, 0, 8, 16, (byte)0x13));
		container.addTankSlot(new TankSlot(tanks, 1, 26, 16, (byte)0x13));
		container.addTankSlot(new TankSlot(tanks, 2, 134, 16, (byte)0x23));
	}

	@Override
	public int[] getSyncVariables() {
		return new int[]{Uref, fracHe, rs, Float.floatToIntBits(Estor), Float.floatToIntBits(power)};
	}

	@Override
	public void setSyncVariable(int i, int v) {
		switch(i) {
		case 0: Uref = v; break;
		case 1: fracHe = v; break;
		case 2: rs = v; break;
		case 3: Estor = Float.intBitsToFloat(v); break;
		case 4: power = Float.intBitsToFloat(v); break;
		}
	}

}
