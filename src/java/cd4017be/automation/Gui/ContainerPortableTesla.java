package cd4017be.automation.Gui;

import net.minecraft.entity.player.EntityPlayer;
import cd4017be.lib.Gui.ItemContainer;

public class ContainerPortableTesla extends ItemContainer 
{

	public ContainerPortableTesla(EntityPlayer player) 
	{
		super(player);
		this.addPlayerInventory(8, 50);
	}

}
