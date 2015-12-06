/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cd4017be.automation.Item;

import java.util.List;

import cd4017be.api.automation.IEnergy;
import cd4017be.api.automation.PipeEnergy;
import cd4017be.automation.Automation;
import cd4017be.lib.DefaultItem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;

/**
 *
 * @author CD4017BE
 */
public class ItemVoltMeter extends DefaultItem
{
    public ItemVoltMeter(String id, String tex)
    {
        super(id);
        this.setTextureName(tex);
        this.setCreativeTab(Automation.tabAutomation);
        this.setMaxStackSize(1);
    }

    @Override
    public void addInformation(ItemStack item, EntityPlayer player, List list, boolean par4) 
    {
        if (item.stackTagCompound != null)
        {
            list.add("Link=(" + item.stackTagCompound.getInteger("lx") + ", " + item.stackTagCompound.getInteger("ly") + ", " + item.stackTagCompound.getInteger("lz") + ")");
        }
        super.addInformation(item, player, list, par4);
    }

    @Override
    public String getItemStackDisplayName(ItemStack item) 
    {
        if (item.stackTagCompound == null)
        {
            return super.getItemStackDisplayName(item);
        } else
        if (item.stackTagCompound.getInteger("ly") < 0)
        {
            return super.getItemStackDisplayName(item) + " (not linked)";
        } else
        {
            return super.getItemStackDisplayName(item) + " (" + String.format("%.2f", item.stackTagCompound.getFloat("Ucap")) + "V / " + item.stackTagCompound.getInteger("Umax") + "V)";
        }
    }

    @Override
    public void onUpdate(ItemStack item, World world, Entity entity, int i, boolean b) 
    {
        if (item.stackTagCompound == null) createNBT(item);
        if (world.isRemote || item.stackTagCompound.getInteger("ly") < 0) return;
        TileEntity te = world.getTileEntity(item.stackTagCompound.getInteger("lx"), item.stackTagCompound.getInteger("ly"), item.stackTagCompound.getInteger("lz"));
        PipeEnergy energy = te != null && te instanceof IEnergy ? ((IEnergy)te).getEnergy(item.stackTagCompound.getByte("ls")) : null;
        if (energy != null)
        {
            float e = item.stackTagCompound.getFloat("Ucap") * 19F + (float)energy.Ucap;
            item.stackTagCompound.setFloat("Ucap", e / 20F);
        } else
        {
            item.stackTagCompound.setInteger("ly", -1);
        }
    }
    
    private void createNBT(ItemStack item)
    {
        item.stackTagCompound = new NBTTagCompound();
        item.stackTagCompound.setFloat("Ucap", 0F);
        item.stackTagCompound.setInteger("lx", 0);
        item.stackTagCompound.setInteger("ly", -1);
        item.stackTagCompound.setInteger("lz", 0);
    }
    
    @Override
    public boolean onItemUse(ItemStack item, EntityPlayer player, World world, int x, int y, int z, int s, float X, float Y, float Z) 
    {
        if (world.isRemote) return true;
        if (item.stackTagCompound == null) createNBT(item);
        TileEntity te = world.getTileEntity(x, y, z);
        PipeEnergy energy = te != null && te instanceof IEnergy ? ((IEnergy)te).getEnergy((byte)s) : null;
        if (energy != null)
        {
            item.stackTagCompound.setInteger("lx", x);
            item.stackTagCompound.setInteger("ly", y);
            item.stackTagCompound.setInteger("lz", z);
            item.stackTagCompound.setFloat("Ucap", (float)energy.Ucap);
            item.stackTagCompound.setInteger("Umax", energy.Umax);
            item.stackTagCompound.setByte("ls", (byte)s);
            player.addChatMessage(new ChatComponentText("Energy linked"));
        } else
        {
            item.stackTagCompound.setInteger("ly", -1);
            player.addChatMessage(new ChatComponentText("Unlinked"));
        }
        return true;
    }
    
}
