package cd4017be.automation.Gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import cd4017be.api.automation.IOperatingArea;
import cd4017be.automation.Objects;
import cd4017be.lib.BlockItemRegistry;
import cd4017be.lib.ItemContainer;
import cd4017be.lib.templates.SlotItemType;

public class ContainerAreaUpgrade extends ItemContainer 
{
	public final int[] link;
	public final IOperatingArea machine;
	
	public ContainerAreaUpgrade(IOperatingArea machine, int[] link, EntityPlayer player) 
	{
		super(player);
		this.link = link;
		this.machine = machine;
		this.addPlayerInventory(8, 86);
		int[] s = machine.getUpgradeSlots();
		this.addSlotToContainer(new SlotItemType(machine, s[0], 152, 16, new ItemStack(Items.glowstone_dust)));
		this.addSlotToContainer(new SlotItemType(machine, s[1], 8, 16, BlockItemRegistry.stack("AreaFrame", 1)));
		this.addSlotToContainer(new SlotItemType(machine, s[2], 8, 34, BlockItemRegistry.stack("EMatrix", 1)));
		this.addSlotToContainer(new SlotItemType(machine, s[3], 8, 52, BlockItemRegistry.stack("CoilSC", 1)));
		this.addSlotToContainer(new SlotItemType(machine, s[4], 134, 16, new ItemStack(Objects.synchronizer, 1, 1), 
				new ItemStack(Objects.synchronizer, 1, 2), new ItemStack(Objects.synchronizer, 1, 3), new ItemStack(Objects.synchronizer, 1, 4)));
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) 
	{
		return super.canInteractWith(player) && player.worldObj.getTileEntity(new BlockPos(link[0], link[1], link[2])) == machine;
	}

	@Override
	public ItemStack slotClick(int s, int b, int m, EntityPlayer par4EntityPlayer) {
		ItemStack item = super.slotClick(s, b, m, par4EntityPlayer);
		if (s >= 36) this.machine.onUpgradeChange(s - 36);
		return item;
	}
	
	public int getDistance()
	{
		int[] area = machine.getOperatingArea();
		int dx = link[0] + 1 < area[0] ? area[0] - link[0] - 1 : link[0] > area[3] ? link[0] - area[3] : 0;
		int dy = link[1] + 1 < area[1] ? area[1] - link[1] - 1 : link[1] > area[4] ? link[1] - area[4] : 0;
		int dz = link[2] + 1 < area[2] ? area[2] - link[2] - 1 : link[2] > area[5] ? link[2] - area[5] : 0;
		return dx > dy ? (dz > dx ? dz : dx) : (dz > dy ? dz : dy);
	}
	

}
