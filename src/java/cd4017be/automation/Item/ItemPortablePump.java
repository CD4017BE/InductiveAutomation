package cd4017be.automation.Item;

import java.util.List;

import cd4017be.automation.Config;
import cd4017be.lib.util.Obj2;
import cd4017be.lib.util.Utils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;
import cd4017be.api.energy.EnergyAutomation.EnergyItem;

public class ItemPortablePump extends ItemEnergyCell implements IFluidContainerItem {

	public static int energyUse = 8;
	
	public ItemPortablePump(String id) 
	{
		super(id, Config.Ecap[0]);
		this.setMaxStackSize(1);
	}

	@Override
    public void addInformation(ItemStack item, EntityPlayer player, List list, boolean par4) 
    {
        FluidStack fluid = this.getFluid(item);
        if (fluid != null) {
            list.add(Utils.formatNumber((float)fluid.amount / 1000F, 3) + "/" + Utils.formatNumber((float)this.getCapacity(item) / 1000F, 3) + " m³ " +  fluid.getLocalizedName());
        }
        super.addInformation(item, player, list, par4);
    }

    @Override
    public FluidStack getFluid(ItemStack item) 
    {
        if (item.stackTagCompound == null) return null;
        else return FluidStack.loadFluidStackFromNBT(item.stackTagCompound.getCompoundTag("fluid"));
    }

    @Override
    public int getCapacity(ItemStack item) 
    {
        return 16000;
    }

    @Override
    public int fill(ItemStack item, FluidStack resource, boolean doFill) 
    {
        if (resource == null) return 0;
        if (doFill && item.stackTagCompound == null) item.stackTagCompound = new NBTTagCompound();
        int n;
        FluidStack fluid = this.getFluid(item);
        if (fluid != null) {
            if (!fluid.isFluidEqual(resource)) return 0;
            n = Math.min(this.getCapacity(item) - fluid.amount, resource.amount);
            if (doFill){
                fluid.amount += n;
                NBTTagCompound tag = new NBTTagCompound();
                fluid.writeToNBT(tag);
                item.stackTagCompound.setTag("fluid", tag);
            }
            return n;
        } else {
            n = Math.min(this.getCapacity(item), resource.amount);
            if (doFill) {
                fluid = resource.copy();
                fluid.amount = n;
                NBTTagCompound tag = new NBTTagCompound();
                fluid.writeToNBT(tag);
                item.stackTagCompound.setTag("fluid", tag);
            }
            return n;
        }
    }

    @Override
    public FluidStack drain(ItemStack item, int maxDrain, boolean doDrain) 
    {
        FluidStack fluid = this.getFluid(item);
        if (fluid == null) return null;
        FluidStack ret = fluid.copy();
        ret.amount = Math.min(fluid.amount, maxDrain);
        if (doDrain) {
            fluid.amount -= ret.amount;
            if (fluid.amount <= 0) item.stackTagCompound.removeTag("fluid");
            else {
            	NBTTagCompound tag = new NBTTagCompound();
                fluid.writeToNBT(tag);
                item.stackTagCompound.setTag("fluid", tag);
            }
        }
        return ret;
    }

	@Override
	public ItemStack onItemRightClick(ItemStack item, World world, EntityPlayer player) 
	{
		if (world.isRemote) return item;
		EnergyItem energy = new EnergyItem(item, this);
		if (energy.getStorageI() < energyUse) return item;
		Vec3 pos = new Vec3(player.posX, player.posY + 1.62D - player.getYOffset(), player.posZ);
        Vec3 dir = player.getLookVec();
        MovingObjectPosition obj = world.rayTraceBlocks(pos, pos.addVector(dir.xCoord * 16, dir.yCoord * 16, dir.zCoord * 16), true, false, false);
        if (obj == null) return item;
        FluidStack fluid = Utils.getFluid(world, obj.getBlockPos(), true);
        if (fluid != null && this.fill(item, fluid, false) == fluid.amount) {
        	this.fill(item, fluid, true);
        	energy.addEnergyI(-energyUse, -1);
        	world.setBlockToAir(obj.getBlockPos());
        }
        return item;
	}

	@Override
	public void onUpdate(ItemStack item, World world, Entity entity, int s, boolean b) 
	{
		if (entity instanceof EntityPlayer && !world.isRemote) {
			if (item.stackTagCompound == null) item.stackTagCompound = new NBTTagCompound();
			int t = item.stackTagCompound.getByte("t") + 1;
			if (t >= 20) {
				t = 0;
				if (item.stackTagCompound.hasKey("fluid")) {
					this.fillFluid(item, ((EntityPlayer)entity).inventory);
				}
			}
			item.stackTagCompound.setByte("t", (byte)t);
		}
	}
	
	private void fillFluid(ItemStack item, InventoryPlayer inv)
	{
		FluidStack type = this.getFluid(item);
		if (type == null) {
			item.stackTagCompound.removeTag("fluid");
			return;
		}
		int[] list = new int[inv.mainInventory.length];
		int n = 0;
		for (int i = 0; i < inv.mainInventory.length && type.amount > 0; i++) {
			ItemStack stack = inv.mainInventory[i];
			if (stack != null && stack.getItem() instanceof IFluidContainerItem && !(stack.getItem() instanceof ItemPortablePump)) {
				IFluidContainerItem cont = (IFluidContainerItem)stack.getItem();
				FluidStack fluid = cont.getFluid(stack);
				if (fluid == null) list[n++] = i;
				else if (fluid.isFluidEqual(type)) {
					Obj2<ItemStack, Integer> obj = Utils.fillFluid(stack, type);
					type.amount -= obj.objB;
					inv.mainInventory[i] = obj.objA;
				}
			}
		}
		for (int i = 0; i < n && type.amount > 0; i++) {
			Obj2<ItemStack, Integer> obj = Utils.fillFluid(inv.mainInventory[list[i]], type);
			type.amount -= obj.objB;
			inv.mainInventory[list[i]] = obj.objA;
		}
		if (type.amount <= 0) {
			item.stackTagCompound.removeTag("fluid");
		} else {
			NBTTagCompound tag = new NBTTagCompound();
			type.writeToNBT(tag);
			item.stackTagCompound.setTag("fluid", tag);
		}
	}
}
