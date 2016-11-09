package cd4017be.automation.TileEntity;

import net.minecraft.network.PacketBuffer;

import java.io.IOException;

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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.FluidStack;

/**
 *
 * @author CD4017BE
 */
public class Collector extends AutomatedTile implements IGuiData {

	private static final FluidStack[] Output = {null, new FluidStack(Objects.L_nitrogenG, 100), new FluidStack(Objects.L_oxygenG, 160)};
	public int mode, netI1, netI2;
	
	public Collector() {
		inventory = new Inventory(1, 0, null);
		tanks = new TankContainer(1, 1).tank(0, Config.tankCap[1], Utils.OUT, -1, 0);
	}

	@Override
	public void update() 
	{
		super.update();
		if (worldObj.isRemote) return;
		EnumFacing dir = EnumFacing.VALUES[this.getOrientation()];
		BlockPos pos = this.pos.offset(dir);
		if (mode == 0) {
			FluidStack fluid = Utils.getFluid(worldObj, pos, true);
			if (fluid != null && tanks.fill(0, fluid, false) == fluid.amount) {
				tanks.fill(0, fluid, true);
				worldObj.setBlockToAir(pos);
			}
		} else if (worldObj.isAirBlock(pos)) {
			FluidStack fluid = Output[mode];
			tanks.fill(0, fluid, true);
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) 
	{
		nbt.setInteger("type", mode);
		return super.writeToNBT(nbt);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) 
	{
		super.readFromNBT(nbt);
		mode = nbt.getInteger("type");
	}
	
	@Override
	protected void customPlayerCommand(byte cmd, PacketBuffer dis, EntityPlayerMP player) throws IOException 
	{
		if (cmd == 0) mode = (mode + 1) % Output.length;
	}

	@Override
	public void initContainer(DataContainer cont) {
		TileContainer container = (TileContainer)cont;
		container.addItemSlot(new SlotTank(inventory, 0, 202, 74));
		container.addPlayerInventory(8, 16);
		
		container.addTankSlot(new TankSlot(tanks, 0, 184, 16, (byte)0x23));
	}

	@Override
	public int[] getSyncVariables() {
		return new int[]{mode, netI1, netI2};
	}

	@Override
	public void setSyncVariable(int i, int v) {
		switch(i) {
		case 0: mode = v; break;
		case 1: netI1 = v; break;
		case 2: netI2 = v; break;
		}
	}

}
