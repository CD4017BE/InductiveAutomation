/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cd4017be.automation.Item;

import java.util.ArrayList;
import java.util.Random;

import cd4017be.api.automation.EnergyItemHandler;
import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraftforge.common.IShearable;

/**
 *
 * @author CD4017BE
 */
public class ItemCutter extends ItemEnergyCell
{
    private final int energyUsage;
    private final int damageVsEntity;
    
    public ItemCutter(String id, String tex, int es, int eu, int ed)
    {
        super(id, tex, es);
        this.energyUsage = eu;
        this.damageVsEntity = ed;
        this.setMaxStackSize(1);
    }

    @Override
    public void onCreated(ItemStack item, World world, EntityPlayer player) 
    {
        item.addEnchantment(Enchantment.silkTouch, 1);
    }
    
    @Override
    public boolean canHarvestBlock(Block block, ItemStack item) 
    {
        return block.getMaterial().isToolNotRequired() && EnergyItemHandler.getEnergy(item) >= this.energyUsage;
    }
    
    @Override
    public boolean onBlockDestroyed(ItemStack item, World world, Block b, int x, int y, int z, EntityLivingBase entityLiving) 
    {
        EnergyItemHandler.addEnergy(item, -this.energyUsage, false);
        return true;
    }

    @Override
    public float getDigSpeed(ItemStack item, Block block, int m) 
    {
        float str = this.canHarvestBlock(block, item) ? 16F : 1F;
        return str;
    }
    
    @Override
    public boolean hitEntity(ItemStack item, EntityLivingBase entityLivingHit, EntityLivingBase par3EntityLiving) 
    {
        if (EnergyItemHandler.getEnergy(item) >= this.energyUsage)
        {
            entityLivingHit.attackEntityFrom(DamageSource.causeMobDamage(par3EntityLiving), this.damageVsEntity);
            entityLivingHit.hurtResistantTime = (entityLivingHit.maxHurtResistantTime + 1) / 2;
            EnergyItemHandler.addEnergy(item, -this.energyUsage, false);
        }
        return true;
    }
    
    @Override
    public boolean itemInteractionForEntity(ItemStack item, EntityPlayer player, EntityLivingBase entity)
    {
        if (entity.worldObj.isRemote || EnergyItemHandler.getEnergy(item) < this.energyUsage)
        {
            return false;
        }
        if (entity instanceof IShearable)
        {
            IShearable target = (IShearable)entity;
            if (target.isShearable(item, entity.worldObj, (int)entity.posX, (int)entity.posY, (int)entity.posZ))
            {
                ArrayList<ItemStack> drops = target.onSheared(item, entity.worldObj, (int)entity.posX, (int)entity.posY, (int)entity.posZ,
                        EnchantmentHelper.getEnchantmentLevel(Enchantment.fortune.effectId, item));

                Random rand = new Random();
                for(ItemStack stack : drops)
                {
                    EntityItem ent = entity.entityDropItem(stack, 1.0F);
                    ent.motionY += rand.nextFloat() * 0.05F;
                    ent.motionX += (rand.nextFloat() - rand.nextFloat()) * 0.1F;
                    ent.motionZ += (rand.nextFloat() - rand.nextFloat()) * 0.1F;
                }
                EnergyItemHandler.addEnergy(item, -this.energyUsage, false);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean onBlockStartBreak(ItemStack item, int x, int y, int z, EntityPlayer player)
    {
        if (player.worldObj.isRemote)
        {
            return false;
        }
        Block id = player.worldObj.getBlock(x, y, z);
        if (id instanceof IShearable && !id.canSilkHarvest(player.worldObj, player, x, y, z, player.worldObj.getBlockMetadata(x, y, z)))
        {
            IShearable target = (IShearable)id;
            if (target.isShearable(item, player.worldObj, x, y, z))
            {
                ArrayList<ItemStack> drops = target.onSheared(item, player.worldObj, x, y, z,
                        EnchantmentHelper.getEnchantmentLevel(Enchantment.fortune.effectId, item));
                Random rand = new Random();

                for(ItemStack stack : drops)
                {
                    float f = 0.7F;
                    double d  = (double)(rand.nextFloat() * f) + (double)(1.0F - f) * 0.5D;
                    double d1 = (double)(rand.nextFloat() * f) + (double)(1.0F - f) * 0.5D;
                    double d2 = (double)(rand.nextFloat() * f) + (double)(1.0F - f) * 0.5D;
                    EntityItem entityitem = new EntityItem(player.worldObj, (double)x + d, (double)y + d1, (double)z + d2, stack);
                    entityitem.delayBeforeCanPickup = 10;
                    player.worldObj.spawnEntityInWorld(entityitem);
                }

                EnergyItemHandler.addEnergy(item, -this.energyUsage, false);
            }
        }
        return false;
    }
    
}
