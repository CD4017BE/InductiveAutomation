package cd4017be.automation.TileEntity;

import cd4017be.automation.Config;
import cd4017be.automation.Objects;
import cd4017be.automation.Item.ItemFluidDummy;
import cd4017be.lib.Gui.DataContainer;
import cd4017be.lib.Gui.DataContainer.IGuiData;
import cd4017be.lib.Gui.SlotItemType;
import cd4017be.lib.Gui.SlotTank;
import cd4017be.lib.Gui.TileContainer;
import cd4017be.lib.Gui.TileContainer.TankSlot;
import cd4017be.lib.templates.AutomatedTile;
import cd4017be.lib.templates.Inventory;
import cd4017be.lib.templates.TankContainer;
import cd4017be.lib.util.Utils;
import net.minecraft.item.ItemStack;

/**
 *
 * @author CD4017BE
 */
public class FluidPacker extends AutomatedTile implements IGuiData {

	public FluidPacker() {
		inventory = new Inventory(7, 3, null).group(0, 0, 1, Utils.OUT).group(1, 1, 2, Utils.OUT).group(2, 2, 3, Utils.OUT);
		tanks = new TankContainer(4, 4).tank(0, Config.tankCap[2], Utils.IN, 3, -1).tank(1, Config.tankCap[2], Utils.IN, 4, -1).tank(2, Config.tankCap[2], Utils.IN, 5, -1).tank(3, Config.tankCap[1], Utils.IN, 6, -1, Objects.L_antimatter);
	}

	@Override
	public void update() 
	{
		super.update();
		for (int i = 0; i < 3; i++) {
			int n = Math.min(tanks.getAmount(3), tanks.getAmount(i) / 1000);
			if (n <= 0) continue;
			ItemStack item = ItemFluidDummy.item(tanks.fluids[i].getFluid(), n);
			if (inventory.items[i] == null) {
				inventory.items[i] = item;
			} else if (inventory.items[i].isItemEqual(item)){
				if (inventory.items[i].stackSize + item.stackSize <= 64) {
					inventory.items[i].stackSize += item.stackSize;
				} else {
					n = 64 - inventory.items[i].stackSize;
					inventory.items[i].stackSize = 64;
				}
			} else n = 0;
			tanks.drain(3, n, true);
			tanks.drain(i, n * 1000, true);
		}
	}

	@Override
	public void initContainer(DataContainer cont) {
		TileContainer container = (TileContainer)cont;
		for (int i = 0; i < 3; i++) {
			container.addItemSlot(new SlotItemType(inventory, i, 80 + i * 36, 52));
		}
		for (int i = 0; i < 3; i++) {
			container.addItemSlot(new SlotTank(inventory, i + 3, 62 + i * 36, 34));
		}
		container.addItemSlot(new SlotTank(inventory, 6, 17, 34));
		
		container.addPlayerInventory(8, 86);
		
		container.addTankSlot(new TankSlot(tanks, 3, 8, 16, (byte)0x23));
		container.addTankSlot(new TankSlot(tanks, 0, 62, 16, (byte)0x13));
		container.addTankSlot(new TankSlot(tanks, 1, 98, 16, (byte)0x13));
		container.addTankSlot(new TankSlot(tanks, 2, 134, 16, (byte)0x13));
	}
	
}
