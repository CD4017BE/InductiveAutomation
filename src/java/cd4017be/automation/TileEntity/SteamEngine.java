package cd4017be.automation.TileEntity;

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
import net.minecraftforge.fluids.FluidStack;

/**
 *
 * @author CD4017BE
 */
public class SteamEngine extends AutomatedTile implements IGuiData {

	protected int getTier() {return 0;}
	public float getPower() {return Config.PsteamGen[0];}
	protected float getDissipation() {return 0.1F;}
	public float Estor;

	public SteamEngine() {
		energy = new PipeEnergy(Config.Umax[this.getTier()], Config.Rcond[this.getTier()]);
		inventory = new Inventory(2, 0, null);
		tanks = new TankContainer(2, 2).tank(0, Config.tankCap[1], Utils.IN, 1, -1, Objects.L_steam).tank(1, Config.tankCap[1], Utils.OUT, -1, 0, Objects.L_waterG);
	}

	@Override
	public void update() 
	{
		super.update();
		if (this.worldObj.isRemote) return;
		float e = this.getEnergyOut();
		if (e > 0) {
			energy.addEnergy(e * 1000F);
			Estor -= e;
		}
		int p = (int)Math.ceil(Math.min((float)tanks.getAmount(0) * 2F / (float)tanks.tanks[0].cap, 1F) * this.getPower() / Config.E_Steam);
		if (p > tanks.getAmount(0)) p = tanks.getAmount(0);
		if (p > 0)
		{
			tanks.drain(0, p, true);
			tanks.fill(1, new FluidStack(Objects.L_waterG, p * 8), true);
			Estor += (float)p * (float)Config.E_Steam;
		}
	}

	public float power() {
		return this.getEnergyOut() / this.getPower();
	}

	public float getEnergyOut() {
		return Estor > 1F ? Estor * this.getDissipation() : Estor;
	}

	@Override
	public void initContainer(DataContainer cont) {
		TileContainer container = (TileContainer)cont;
		container.addItemSlot(new SlotTank(inventory, 0, 89, 34));
		container.addItemSlot(new SlotTank(inventory, 1, 17, 34));
		
		container.addPlayerInventory(8, 86);
		
		container.addTankSlot(new TankSlot(tanks, 0, 8, 16, (byte)0x23));
		container.addTankSlot(new TankSlot(tanks, 1, 80, 16, (byte)0x23));
	}
	@Override
	public int[] getSyncVariables() {
		return new int[]{Float.floatToIntBits(Estor)};
	}
	@Override
	public void setSyncVariable(int i, int v) {
		Estor = Float.intBitsToFloat(v);
	}

}
