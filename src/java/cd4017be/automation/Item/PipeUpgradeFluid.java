/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cd4017be.automation.Item;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;

/**
 *
 * @author CD4017BE
 */
public class PipeUpgradeFluid 
{
    public int maxAmount;
    public FluidStack[] list = new FluidStack[0];
    public byte mode;//1=invert; 2=force; 4=redstone; 8=invertRS
    
    public int getMaxFillAmount(FluidStack stack, FluidTankInfo info, boolean rs)
    {
        if ((mode & 4) != 0 && (rs ^ (mode & 8) == 0)) return 0;
        if (stack == null) return 0;
        if (info.fluid != null && !stack.isFluidEqual(info.fluid)) return 0;
        boolean listed = false;
        for (FluidStack fluid : list) {
            if (fluid.isFluidEqual(stack)){
                listed = true;
                break;
            }
        }
        if (listed ^ (mode & 1) == 0) return 0;
        if (maxAmount == 0) return stack.amount;
        int am = maxAmount - (info.fluid == null ? 0 : info.fluid.amount);
        if (am < 0) return 0;
        else if (am >= stack.amount) return stack.amount;
        else return am;
    }
    
    public int getMaxDrainAmount(FluidStack stack, FluidTankInfo info, boolean rs)
    {
        if ((mode & 4) != 0 && (rs ^ (mode & 8) == 0)) return 0;
        if (info.fluid == null) return 0;
        if (stack != null && !stack.isFluidEqual(info.fluid)) return 0;
        boolean listed = false;
        for (FluidStack fluid : list) {
            if (fluid.isFluidEqual(info.fluid)){
                listed = true;
                break;
            }
        }
        if (listed ^ (mode & 1) == 0) return 0;
        int am = info.fluid.amount - maxAmount;
        if (am < 0) return 0;
        else return am;
    }
    
    public boolean transferFluid(FluidStack stack)
    {
        if ((mode & 2) == 0) return true;
        else if (stack == null) return false;
        boolean listed = false;
        for (FluidStack fluid : list) {
            if (fluid.isFluidEqual(stack)){
                listed = true;
                break;
            }
        }
        return listed ^ (mode & 1) == 0;
    }
    
    public static PipeUpgradeFluid load(NBTTagCompound nbt)
    {
        PipeUpgradeFluid upgrade = new PipeUpgradeFluid();
        upgrade.mode = nbt.getByte("mode");
        upgrade.maxAmount = nbt.getInteger("maxAm");
        if (upgrade.maxAmount < 0) upgrade.maxAmount = 0;
        if (nbt.hasKey("list")) {
            NBTTagList list = nbt.getTagList("list", 10);
            upgrade.list = new FluidStack[list.tagCount()];
            for (int i = 0; i < list.tagCount(); i++) {
                upgrade.list[i] = FluidStack.loadFluidStackFromNBT(list.getCompoundTagAt(i));
            }
        }
        return upgrade;
    }
    
    public static NBTTagCompound save(PipeUpgradeFluid upgrade)
    {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setByte("mode", upgrade.mode);
        nbt.setInteger("maxAm", upgrade.maxAmount);
        if (upgrade.list.length > 0) {
            NBTTagList list = new NBTTagList();
            for (FluidStack fluid : upgrade.list) {
                NBTTagCompound tag = new NBTTagCompound();
                fluid.writeToNBT(tag);
                list.appendTag(tag);
            }
            nbt.setTag("list", list);
        }
        return nbt;
    }
    
}