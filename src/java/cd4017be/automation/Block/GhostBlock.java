package cd4017be.automation.Block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import cd4017be.lib.DefaultBlock;
import cd4017be.lib.DefaultItemBlock;

public class GhostBlock extends DefaultBlock 
{

	public static Block ID;
	
	public GhostBlock(String id) 
	{
		super(id, Material.rock, DefaultItemBlock.class);
		ID = this;
	}

}
