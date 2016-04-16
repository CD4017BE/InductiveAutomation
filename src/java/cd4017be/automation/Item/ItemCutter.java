/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cd4017be.automation.Item;

import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraftforge.common.IShearable;
import cd4017be.api.energy.EnergyAutomation.EnergyItem;

/**
 *
 * @author CD4017BE
 */
public class ItemCutter extends ItemEnergyCell
{
    private final int energyUsage;
    private final int damageVsEntity;
    
    public ItemCutter(String id, int es, int eu, int ed)
    {
        super(id, es);
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
        return block.getMaterial().isToolNotRequired() && new EnergyItem(item, this).getStorageI() >= this.energyUsage;
    }
    
    @Override
    public boolean onBlockDestroyed(ItemStack item, World world, Block b, BlockPos pos, EntityLivingBase entityLiving) 
    {
    	new EnergyItem(item, this).addEnergyI(-this.energyUsage, -1);
        return true;
    }

    @Override
    public float getDigSpeed(ItemStack item, IBlockState state) 
    {
        float str = this.canHarvestBlock(state.getBlock(), item) ? 16F : 1F;
        return str;
    }
    
    @Override
    public boolean hitEntity(ItemStack item, EntityLivingBase entityLivingHit, EntityLivingBase par3EntityLiving) 
    {
        EnergyItem energy = new EnergyItem(item, this);
    	if (energy.getStorageI() >= this.energyUsage)
        {
            entityLivingHit.attackEntityFrom(DamageSource.causeMobDamage(par3EntityLiving), this.damageVsEntity);
            entityLivingHit.hurtResistantTime = (entityLivingHit.maxHurtResistantTime + 1) / 2;
            energy.addEnergyI(-this.energyUsage, -1);
        }
        return true;
    }
    
    @Override
    public boolean itemInteractionForEntity(ItemStack item, EntityPlayer player, EntityLivingBase entity)
    {
        EnergyItem energy = new EnergyItem(item, this);
    	if (entity.worldObj.isRemote || energy.getStorageI() < this.energyUsage)
        {
            return false;
        }
        if (entity instanceof IShearable)
        {
            IShearable target = (IShearable)entity;
            if (target.isShearable(item, entity.worldObj, new BlockPos(entity.posX, entity.posY, entity.posZ)))
            {
                List<ItemStack> drops = target.onSheared(item, entity.worldObj, new BlockPos(entity.posX, entity.posY, entity.posZ), EnchantmentHelper.getEnchantmentLevel(Enchantment.fortune.effectId, item));

                Random rand = new Random();
                for(ItemStack stack : drops)
                {
                    EntityItem ent = entity.entityDropItem(stack, 1.0F);
                    ent.motionY += rand.nextFloat() * 0.05F;
                    ent.motionX += (rand.nextFloat() - rand.nextFloat()) * 0.1F;
                    ent.motionZ += (rand.nextFloat() - rand.nextFloat()) * 0.1F;
                }
                energy.addEnergyI(-this.energyUsage, -1);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean onBlockStartBreak(ItemStack item, BlockPos pos, EntityPlayer player)
    {
        if (player.worldObj.isRemote)
        {
            return false;
        }
        IBlockState state = player.worldObj.getBlockState(pos);
        if (state.getBlock() instanceof IShearable && !state.getBlock().canSilkHarvest(player.worldObj, pos, state, player))
        {
            IShearable target = (IShearable)state.getBlock();
            if (target.isShearable(item, player.worldObj, pos))
            {
                List<ItemStack> drops = target.onSheared(item, player.worldObj, pos, EnchantmentHelper.getEnchantmentLevel(Enchantment.fortune.effectId, item));
                Random rand = new Random();

                for(ItemStack stack : drops)
                {
                    float f = 0.7F;
                    double d  = (double)(rand.nextFloat() * f) + (double)(1.0F - f) * 0.5D;
                    double d1 = (double)(rand.nextFloat() * f) + (double)(1.0F - f) * 0.5D;
                    double d2 = (double)(rand.nextFloat() * f) + (double)(1.0F - f) * 0.5D;
                    EntityItem entityitem = new EntityItem(player.worldObj, (double)pos.getX() + d, (double)pos.getY() + d1, (double)pos.getZ() + d2, stack);
                    entityitem.setNoPickupDelay();
                    player.worldObj.spawnEntityInWorld(entityitem);
                }

                new EnergyItem(item, this).addEnergyI(-this.energyUsage, -1);
            }
        }
        return false;
    }
    
}
