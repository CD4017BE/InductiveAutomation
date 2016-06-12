package cd4017be.automation.TileEntity;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidHandler;
import cd4017be.api.recipes.AutomationRecipes;
import cd4017be.automation.Config;
import cd4017be.lib.TileContainer;
import cd4017be.lib.TileEntityData;
import cd4017be.lib.TileContainer.TankSlot;
import cd4017be.lib.templates.AutomatedTile;
import cd4017be.lib.templates.Inventory;
import cd4017be.lib.templates.SlotTank;
import cd4017be.lib.templates.TankContainer;
import cd4017be.lib.templates.TankContainer.Tank;
import cd4017be.lib.util.Obj2;

public class HeatRadiator extends AutomatedTile implements IFluidHandler {

	public HeatRadiator()
    {
        netData = new TileEntityData(1, 0, 0, 2);
        inventory = new Inventory(this, 2);
        tanks = new TankContainer(this, new Tank(Config.tankCap[1], -1).setIn(0), new Tank(Config.tankCap[1], 1).setOut(1));
    }

    @Override
    public void update() 
    {
    	super.update();
        if (this.worldObj.isRemote) return;
        int n = tanks.getAmount(0);
        int m = tanks.getSpace(1);
        if (n > 0 && m > 0) {
        	Obj2<Integer, FluidStack> recipe = AutomationRecipes.radiatorRecipes.get(netData.fluids[0].getFluid());
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
    public void initContainer(TileContainer container)
    {
    	container.addEntitySlot(new SlotTank(this, 0, 17, 34));
        container.addEntitySlot(new SlotTank(this, 1, 143, 34));
        
        container.addPlayerInventory(8, 86);
        
        container.addTankSlot(new TankSlot(tanks, 0, 8, 16, true));
        container.addTankSlot(new TankSlot(tanks, 1, 134, 16, true));
    }

}
