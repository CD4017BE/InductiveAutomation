package cd4017be.automation.Item;

import cd4017be.api.Capabilities;
import cd4017be.api.automation.PipeEnergy;
import cd4017be.api.circuits.ItemBlockSensor;
import cd4017be.automation.Automation;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

/**
 *
 * @author CD4017BE
 */
public class ItemVoltMeter extends ItemBlockSensor {

	public ItemVoltMeter(String id) {
		super(id, 20F);
		this.setCreativeTab(Automation.tabAutomation);
		this.setMaxStackSize(1);
	}

	@Override
	protected float measure(ItemStack sensor, NBTTagCompound nbt, World world, BlockPos pos, EnumFacing side) {
		TileEntity te = world.getTileEntity(pos);
		PipeEnergy energy = te != null ? te.getCapability(Capabilities.ELECTRIC_CAPABILITY, side) : null;
		return energy != null ? energy.Ucap : 0F;
	}

}
