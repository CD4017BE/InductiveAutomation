/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cd4017be.automation.Item;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.common.ISpecialArmor;

import org.lwjgl.input.Keyboard;

import cd4017be.automation.Automation;
import cd4017be.automation.Objects;
import cd4017be.lib.BlockItemRegistry;
import cd4017be.lib.TooltipInfo;
import cd4017be.lib.util.Vec3;

/**
 *
 * @author CD4017BE
 */
public class ItemJetpack extends ItemArmor implements ISpecialArmor
{
    
    private static final ItemArmor.ArmorMaterial defaultType = ItemArmor.ArmorMaterial.IRON;
    private static final double[] absorption = {0, 0.24D, 0.28D, 0.32D, 0.32D};
    private static final int[] displayArmor = {0, 6, 7, 8, 8};
    private static final int[] durability = {0, 240, 360, 480, 0};
    private static final String[] textures = {"standart", "iron", "steel", "graphite", "unbreak"};
    public static final int slotPos = 2;
    private final int armor;
    
    
    public ItemJetpack(String id, int armor)
    {
        super(defaultType, 0, EntityEquipmentSlot.CHEST);
        this.armor = armor;
        this.setUnlocalizedName(id);
        this.setMaxDamage(durability[armor]);
        this.setCreativeTab(Automation.tabAutomation);
        BlockItemRegistry.registerItem(this, id);
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type) 
    {
        return "automation:textures/armor/" + textures[this.armor] + ".png";
    }

    @Override
    public boolean getIsRepairable(ItemStack item, ItemStack material) 
    {
        return false;
    }

    @Override
    public int getItemEnchantability() 
    {
        return 0;
    }

    @Override
    public ArmorProperties getProperties(EntityLivingBase player, ItemStack armor, DamageSource source, double damage, int slot) 
    {
        return new ArmorProperties(0, absorption[this.armor], this.getMaxDamage() - armor.getItemDamage());
    }

    @Override
    public int getArmorDisplay(EntityPlayer player, ItemStack armor, int slot) 
    {
        return displayArmor[this.armor];
    }

    @Override
    public void damageArmor(EntityLivingBase entity, ItemStack stack, DamageSource source, int damage, int slot) 
    {
        if (this.armor == 0 || this.armor == 4) return;
        int d = stack.getItemDamage() + damage;
        if (d >= stack.getMaxDamage()) {
            ItemStack item = new ItemStack(Objects.jetpack);
            item.setTagCompound((NBTTagCompound)stack.getTagCompound().copy());
            entity.setItemStackToSlot(EntityEquipmentSlot.CHEST, item);
        } else {
            stack.setItemDamage(d);
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
    public void addInformation(ItemStack item, EntityPlayer player, List list, boolean b) 
    {
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
        	String s = this.getUnlocalizedName(item) + ".tip";
        	String s1 = TooltipInfo.getLocFormat(s);
            if (!s1.equals(s)) list.addAll(Arrays.asList(s1.split("\n")));
        } else list.add("<SHIFT for info>");
        super.addInformation(item, player, list, b);
    }

    @Override
    public void onArmorTick(World world, EntityPlayer player, ItemStack item) 
    {
    	NBTTagCompound nbt = item.getTagCompound();
    	if (nbt == null) {
            nbt = new NBTTagCompound();
            nbt.setBoolean("On", false);
            nbt.setInteger("power", 0);
            nbt.setFloat("vecX", 0);
            nbt.setFloat("vecY", 1);
            nbt.setFloat("vecZ", 0);
            item.setTagCompound(nbt);
        }
        if (!nbt.getBoolean("On")) return;
        if (!world.isRemote) {
            int power = this.updateEnergy(nbt, player.inventory);
            Vec3 dir = Vec3.Def(nbt.getFloat("vecX"), nbt.getFloat("vecY"), nbt.getFloat("vecZ"));
            if (dir.isNaN() || dir.sq() < 0.9D) {
                dir = Vec3.Def(0, 1, 0);
                nbt.setFloat("vecX", (float)dir.x);
                nbt.setFloat("vecY", (float)dir.y);
                nbt.setFloat("vecZ", (float)dir.z);
            }
            updateMovement(player, dir, power);
        }
        this.spawnParticles(nbt, world, player);
    }
    
    public static final int maxPower = 20;
    private static final double energyFaktor = 0.0128D;
    private static final float energyUse = 0.05F / (float)maxPower;
    
    private int updateEnergy(NBTTagCompound data, InventoryPlayer inv)
    {
        float energy = 0;
        for (ItemStack item : inv.mainInventory)
            energy += ItemJetpackFuel.getFuel(item);
        int power = (int)Math.floor(energy / energyUse);
        int lpow = data.getInteger("power");
        if (lpow < 0 && power > 0) {
            data.setInteger("power", 0);
            return 0;
        }
        energy = lpow * energyUse;
        for (int i = 0; energy > 0 && i < inv.mainInventory.length; i++)
        	energy -= ItemJetpackFuel.useFuel(energy, inv, i);
        if (power < lpow) {
            data.setInteger("power", -1);
            return 0;
        } else if (lpow < power) return lpow; 
        else return power;
    }
    
    public static void updateMovement(EntityPlayer player, Vec3 dir, int pow)
    {
        double power = (double)pow / (double)maxPower;
        if (power <= 0) return;
        dir = dir.scale(Math.sqrt(power * energyFaktor));
        player.addVelocity(dir.x, dir.y, dir.z);
        if (!player.onGround) {
            player.moveForward = 0;
            player.moveStrafing = 0;
        }
        if (player instanceof EntityPlayerMP) {
        	((EntityPlayerMP)player).playerNetServerHandler.floatingTickCount = 0;
        }
        updateFallDamage(player);
    }
    
    private static final double fallDstFaktor = 6.25D;
    
    private static void updateFallDamage(EntityPlayer player)
    {
        if (player.motionY < 0) {
            player.fallDistance = (float)(player.motionY * player.motionY * fallDstFaktor);
        } else player.fallDistance = 0;
        
    }
    
    private static final Random random = new Random();
    
    private void spawnParticles(NBTTagCompound data, World world, EntityPlayer player)
    {
        if (!data.getBoolean("On")) return;
        int power = data.getInteger("power");
        if (random.nextInt(maxPower) < power) {
            Vec3 mov = Vec3.Def(data.getFloat("vecX"), data.getFloat("vecY"), data.getFloat("vecZ")).scale((double)power / (double)maxPower * -1F);
            mov = mov.add(player.motionX, player.motionY, player.motionZ);
            world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, player.posX, player.posY - 0.8D, player.posZ, mov.x, mov.y, mov.z);
        }
    }

	@Override
	public String getUnlocalizedName(ItemStack item) 
	{
		return super.getUnlocalizedName().replaceFirst("item.", "item.cd4017be.");
	}

	@Override
	public String getItemStackDisplayName(ItemStack item) 
	{
		return I18n.translateToLocal(this.getUnlocalizedName(item) + ".name");
	}
    
}
