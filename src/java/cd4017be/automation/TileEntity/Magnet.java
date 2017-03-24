package cd4017be.automation.TileEntity;

import net.minecraft.util.ITickable;

import java.util.Iterator;

import cd4017be.api.Capabilities;
import cd4017be.api.automation.PipeEnergy;
import cd4017be.automation.Config;
import cd4017be.lib.ModTileEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

/**
 *
 * @author CD4017BE
 */
public class Magnet extends ModTileEntity implements ITickable {

	public static int Umax = 8000;
	public static float Energy = 0.1F;
	public static final float rad = 8;
	public static final float accleration = 1;
	private PipeEnergy energy = new PipeEnergy(Umax, Config.Rcond[1]);
	
	@Override
	public void update() 
	{
		if (worldObj.isRemote) return;
		energy.update(this);
		if (worldObj.isBlockPowered(getPos())) return;
		float e = energy.Ucap * energy.Ucap;
		float rad = (float)energy.Ucap / 64F;
		Iterator<Entity> list = worldObj.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos.getX() + 0.5D - rad, pos.getY() + 0.5D - rad, pos.getZ() + 0.5D - rad, pos.getX() + 0.5D + rad, pos.getY() + 0.5D + rad, pos.getZ() + 0.5D + rad)).iterator();
		Vec3d vec0 = new Vec3d(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
		while (list.hasNext())
		{
			Entity entity = list.next();
			if (entity instanceof EntityPlayer) continue;
			Vec3d vec1 = vec0.addVector(-entity.posX, -entity.posY, -entity.posZ);
			double d = vec1.xCoord * vec1.xCoord + vec1.yCoord * vec1.yCoord + vec1.zCoord * vec1.zCoord;
			if (e < Energy) continue;
			e -= Energy;
			if (entity instanceof EntityItem && entity.isEntityAlive())
			{
				ItemStack item = putItem(((EntityItem)entity).getEntityItem());
				if (item == null) worldObj.removeEntity(entity);
				else ((EntityItem)entity).setEntityItemStack(item);
			} else
			{
				d = accleration / d;
				entity.addVelocity(vec1.xCoord * d, vec1.yCoord * d, vec1.zCoord * d);
			}
		}
		energy.Ucap = 0;
		energy.addEnergy(e);
	}
	
	private ItemStack putItem(ItemStack item) {
		for (EnumFacing dir : EnumFacing.VALUES) {
			TileEntity te = getLoadedTile(pos.offset(dir));
			IItemHandler acc = te != null ? te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, dir.getOpposite()) : null;
			if (acc != null) item = ItemHandlerHelper.insertItemStacked(acc, item, false);
		}
		return item;
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		if (capability == Capabilities.ELECTRIC_CAPABILITY) return true;
		return super.hasCapability(capability, facing);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		if (capability == Capabilities.ELECTRIC_CAPABILITY) return (T)energy;
		return super.getCapability(capability, facing);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) 
	{
		energy.writeToNBT(nbt, "wire");
		return super.writeToNBT(nbt);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) 
	{
		super.readFromNBT(nbt);
		energy.readFromNBT(nbt, "wire");
	}

}
