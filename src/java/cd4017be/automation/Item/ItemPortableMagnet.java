/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cd4017be.automation.Item;

import java.util.ArrayList;
import java.util.List;

import cd4017be.automation.TileEntity.Magnet;
import cd4017be.lib.util.Vec3;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.I18n;
import net.minecraft.world.World;
import cd4017be.api.energy.EnergyAutomation.EnergyItem;

/**
 *
 * @author CD4017BE
 */
public class ItemPortableMagnet extends ItemEnergyCell
{
    
    public ItemPortableMagnet(String id, int es)
    {
        super(id, es);
        this.setMaxStackSize(1);
    }

    @Override
    public void onUpdate(ItemStack item, World world, Entity entity, int i, boolean b) 
    {
        if (world.isRemote || !(entity instanceof EntityPlayer) || item.getTagCompound() == null) return;
        EnergyItem energy = new EnergyItem(item, this);
        if (energy.getStorageI() < 1 || !item.getTagCompound().getBoolean("active") || i == 17) return;
        ArrayList<Entity> list = new ArrayList<Entity>();
        AxisAlignedBB area = new AxisAlignedBB(entity.posX - Magnet.rad, entity.posY - Magnet.rad, entity.posZ - Magnet.rad, entity.posX + Magnet.rad, entity.posY + Magnet.rad, entity.posZ + Magnet.rad);
        if (item.getTagCompound().getBoolean("auto") && world.getEntitiesWithinAABB(EntityPlayer.class, area).size() > 1) return;
        list.addAll(world.getEntitiesWithinAABB(EntityItem.class, area));
        list.addAll(world.getEntitiesWithinAABB(EntityXPOrb.class, area));
        Vec3 vec0 = Vec3.Def(entity.posX, entity.posY, entity.posZ), vec1;
        for (Entity e : list) {
            vec1 = vec0.add(-e.posX, -e.posY, -e.posZ);
            vec1 = vec1.scale(Magnet.accleration / (vec1.sq() + 2D));
            e.addVelocity(vec1.x, vec1.y, vec1.z);
        }
        if (!list.isEmpty()) energy.addEnergyI(-1, -1);
    }

    @Override
    public ItemStack onItemRightClick(ItemStack item, World world, EntityPlayer player) 
    {
        if (item.getTagCompound() == null) item.setTagCompound(new NBTTagCompound());
        if (player.isSneaking()) item.getTagCompound().setBoolean("auto", !item.getTagCompound().getBoolean("auto"));
        else item.getTagCompound().setBoolean("active", !item.getTagCompound().getBoolean("active"));
        return item;
    }

    @Override
    public String getItemStackDisplayName(ItemStack item) 
    {
        if (item.getTagCompound() != null && item.getTagCompound().getBoolean("active")) return super.getItemStackDisplayName(item) + " (ON)";
        else return super.getItemStackDisplayName(item);
    }

	@Override
	public void addInformation(ItemStack item, EntityPlayer player, List list, boolean f) {
		if (item.hasTagCompound()) list.add(I18n.translateToLocal("item.cd4017be.portableMagnet.auto" + (item.getTagCompound().getBoolean("auto") ? "1" : "0")));
		super.addInformation(item, player, list, f);
	}
    
}
