package cd4017be.automation.Block;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import cd4017be.lib.DefaultBlock;

public class GhostBlock extends DefaultBlock 
{

	public static Block ID;
	
	public GhostBlock(String id) 
	{
		super(id, Material.rock, null);
		ID = this;
	}

	@Override
	public int quantityDropped(Random random) {
		return 0;
	}

	@Override
	public int getRenderType() {
		return -1;
	}

}
