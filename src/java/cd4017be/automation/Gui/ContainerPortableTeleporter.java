package cd4017be.automation.Gui;

import cd4017be.lib.ItemContainer;
import net.minecraft.entity.player.EntityPlayer;

public class ContainerPortableTeleporter extends ItemContainer 
{

	public ContainerPortableTeleporter(EntityPlayer player) 
	{
		super(player);
		this.addPlayerInventory(8, 134);
	}

}
