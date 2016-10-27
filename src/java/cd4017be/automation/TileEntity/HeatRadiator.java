package cd4017be.automation.TileEntity;

import net.minecraftforge.fluids.FluidStack;
import cd4017be.api.recipes.AutomationRecipes;
import cd4017be.automation.Config;
import cd4017be.lib.Gui.DataContainer;
import cd4017be.lib.Gui.DataContainer.IGuiData;
import cd4017be.lib.Gui.SlotTank;
import cd4017be.lib.Gui.TileContainer;
import cd4017be.lib.Gui.TileContainer.TankSlot;
import cd4017be.lib.templates.AutomatedTile;
import cd4017be.lib.templates.Inventory;
import cd4017be.lib.templates.TankContainer;
import cd4017be.lib.util.Obj2;
import cd4017be.lib.util.Utils;

public class HeatRadiator extends AutomatedTile implements IGuiData {

	public HeatRadiator() {
		inventory = new Inventory(2, 0, null);
		tanks = new TankContainer(2, 2).tank(0, Config.tankCap[1], Utils.IN, 0, -1).tank(1, Config.tankCap[1], Utils.OUT, -1, 1);
	}

	@Override
	public void update() {
		super.update();
		if (this.worldObj.isRemote) return;
		int n = tanks.getAmount(0);
		int m = tanks.getSpace(1);
		if (n > 0 && m > 0) {
			Obj2<Integer, FluidStack> recipe = AutomationRecipes.radiatorRecipes.get(tanks.fluids[0].getFluid());
			if (recipe != null && n >= recipe.objA && m >= recipe.objB.amount) {
				n /= recipe.objA;
				m /= recipe.objB.amount;
				if (n >= m) {
					n = recipe.objA * m;
					m *= recipe.objB.amount;
				} else {
					m = recipe.objB.amount * n;
					n *= recipe.objA;
				}
				tanks.drain(0, n, true);
				tanks.fill(1, new FluidStack(recipe.objB, m), true);
			}
		}
	}

	@Override
	public void initContainer(DataContainer cont) {
		TileContainer container = (TileContainer)cont;
		container.addItemSlot(new SlotTank(inventory, 0, 17, 34));
		container.addItemSlot(new SlotTank(inventory, 1, 143, 34));
		
		container.addPlayerInventory(8, 86);
		
		container.addTankSlot(new TankSlot(tanks, 0, 8, 16, (byte)0x23));
		container.addTankSlot(new TankSlot(tanks, 1, 134, 16, (byte)0x23));
	}

}
