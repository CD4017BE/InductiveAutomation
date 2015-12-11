/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cd4017be.automation.TileEntity;

import cd4017be.automation.Automation;
import cd4017be.automation.Config;
import cd4017be.lib.BlockItemRegistry;
import cd4017be.lib.TileContainer;
import cd4017be.lib.TileEntityData;
import cd4017be.lib.templates.AutomatedTile;
import cd4017be.lib.templates.Inventory;
import cd4017be.lib.templates.Inventory.Component;
import cd4017be.lib.templates.SlotOutput;
import cd4017be.lib.templates.SlotTank;
import cd4017be.lib.templates.TankContainer;
import cd4017be.lib.templates.TankContainer.Tank;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.IFluidHandler;

/**
 *
 * @author CD4017BE
 */
public class FluidPacker extends AutomatedTile implements ISidedInventory, IFluidHandler
{
    private static Item Id = BlockItemRegistry.itemId("item.fluidDummy");
    
    public FluidPacker()
    {
        netData = new TileEntityData(2, 0, 0, 4);
        inventory = new Inventory(this, 7, new Component(0, 1, 1), new Component(1, 2, 1), new Component(2, 3, 1));
        tanks = new TankContainer(this, new Tank(Config.tankCap[2], -1).setIn(3), new Tank(Config.tankCap[2], -1).setIn(4), new Tank(Config.tankCap[2], -1).setIn(5), new Tank(Config.tankCap[1], -1, Automation.L_antimatter).setIn(6)).setNetLong(1);
    }

    @Override
    public void updateEntity() 
    {
        super.updateEntity();
        for (int i = 0; i < 3; i++) {
            int n = Math.min(tanks.getAmount(3), tanks.getAmount(i) / 1000);
            if (n <= 0) continue;
            ItemStack item = new ItemStack(Id, n, tanks.getFluid(i).fluidID);
            if (inventory.items[i] == null) {
                inventory.items[i] = item;
            } else if (inventory.items[i].isItemEqual(item)){
                if (inventory.items[i].stackSize + item.stackSize <= 64) {
                    inventory.items[i].stackSize += item.stackSize;
                } else {
                    n = 64 - inventory.items[i].stackSize;
                    inventory.items[i].stackSize = 64;
                }
            }
            tanks.drain(3, n, true);
            tanks.drain(i, n * 1000, true);
        }
    }

    @Override
    public void initContainer(TileContainer container) 
    {
        for (int i = 0; i < 3; i++) {
            container.addEntitySlot(new SlotOutput(this, i, 80 + i * 36, 52));
        }
        for (int i = 0; i < 3; i++) {
            container.addEntitySlot(new SlotTank(this, i + 3, 62 + i * 36, 34));
        }
        container.addEntitySlot(new SlotTank(this, 6, 17, 34));
        
        container.addPlayerInventory(8, 86);
    }
    
}
