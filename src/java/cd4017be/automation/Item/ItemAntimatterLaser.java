/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cd4017be.automation.Item;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import cd4017be.api.automation.AntimatterItemHandler;
import cd4017be.api.automation.AntimatterItemHandler.IAntimatterItem;
import cd4017be.api.automation.AreaProtect;
import cd4017be.api.automation.EnergyItemHandler;
import cd4017be.api.automation.MatterOrbItemHandler;
import cd4017be.api.automation.MatterOrbItemHandler.IMatterOrb;
import cd4017be.automation.Automation;
import cd4017be.automation.Config;
import cd4017be.automation.Gui.ContainerAMLEnchant;
import cd4017be.automation.Gui.GuiAMLEnchant;
import cd4017be.lib.ClientInputHandler.IScrollHandlerItem;
import cd4017be.lib.BlockGuiHandler;
import cd4017be.lib.IGuiItem;
import net.minecraft.block.Block;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;

/**
 *
 * @author CD4017BE
 */
public class ItemAntimatterLaser extends ItemEnergyCell implements IAntimatterItem, IMatterOrb, IFluidContainerItem, IGuiItem, IScrollHandlerItem
{
    public static int EnergyUsage = 16;
    public static float AmUsage = 1F;
    public static float BaseDamage = 12F;
    public static float MaxDamage = 60F;
    public static float DamageMult = 1.0F;
    public static float AMDamage = 1.0F;
    public static float AMDmgExp = 1.0F;
    
    public ItemAntimatterLaser(String id, String tex)
    {
        super(id, tex, Config.Ecap[1]);
        this.setMaxStackSize(1);
    }

    @Override
	public EnumRarity getRarity(ItemStack item) 
    {
		return EnumRarity.epic;
	}

	@Override
    public int getChargeSpeed(ItemStack item) 
    {
        return 1000;
    }

    private static final String[] modes = {" 1x1", " 3x3", " 5x5", " 7x7", " 9x9", " vein", " off"};
    @Override
    public String getItemStackDisplayName(ItemStack item) 
    {
    	if (item.stackTagCompound == null) return super.getItemStackDisplayName(item);
        int am = AntimatterItemHandler.getAntimatter(item);
        int mode = item.stackTagCompound.getByte("mode") & 0xff;
        return super.getItemStackDisplayName(item) + modes[mode % modes.length] + " (" + (am > 0 ? am + " ng)" : "Empty)");
    }
    
    @Override
    public void addInformation(ItemStack item, EntityPlayer player, List list, boolean f) {
        AntimatterItemHandler.addInformation(item, list);
        MatterOrbItemHandler.addInformation(item, list);
        super.addInformation(item, player, list, f);
    }

    @Override
    public ItemStack onItemRightClick(ItemStack item, World world, EntityPlayer player) 
    {
    	int mode = item.stackTagCompound.getByte("mode");
        Vec3 pos = Vec3.createVectorHelper(player.posX, player.posY + 1.62D - player.yOffset, player.posZ);
        Vec3 dir = player.getLookVec();
        Vec3 pos1 = pos.addVector(dir.xCoord * 128, dir.yCoord * 128, dir.zCoord * 128);
        double x0 = pos.xCoord, y0 = pos.yCoord, z0 = pos.zCoord, 
        		x1 = pos1.xCoord, y1 = pos1.yCoord, z1 = pos1.zCoord;
        MovingObjectPosition obj = mode == 6 || player.isSneaking() ? null : world.rayTraceBlocks(pos, pos1, false);
        pos.xCoord = x0; pos.yCoord = y0; pos.zCoord = z0;
        pos1.xCoord = x1; pos1.yCoord = y1; pos1.zCoord = z1;
        if (obj != null) {
        	x1 = obj.hitVec.xCoord;
        	y1 = obj.hitVec.yCoord;
        	z1 = obj.hitVec.zCoord;
        }
        if (x1 < x0) {double a = x0; x0 = x1; x1 = a;}
        if (y1 < y0) {double a = y0; y0 = y1; y1 = a;}
        if (z1 < z0) {double a = z0; z0 = z1; z1 = a;}
        List<Entity> list = world.getEntitiesWithinAABBExcludingEntity(player, AxisAlignedBB.getBoundingBox(x0, y0, z0, x1, y1, z1));
        MovingObjectPosition obj1;
        for (Entity e : list) {
        	obj1 = e.boundingBox.calculateIntercept(pos, pos1);
        	if (obj1 != null && (obj == null || obj1.hitVec.squareDistanceTo(pos) < obj.hitVec.squareDistanceTo(pos))) 
        		obj = new MovingObjectPosition(e);
        }
        if (obj == null) return item;
        else if (obj.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) this.onItemUse(item, player, world, obj.blockX, obj.blockY, obj.blockZ, obj.sideHit, (float)obj.hitVec.xCoord, (float)obj.hitVec.yCoord, (float)obj.hitVec.zCoord);
        else if (obj.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY) this.useOnEntity(item, player, obj.entityHit);
        return item;
    }
    
    private void useOnEntity(ItemStack item, EntityPlayer player, Entity entity)
    {
    	if (player.worldObj.isRemote) {
    		EnergyItemHandler.addEnergy(item, 0, false);
    		return;
    	}
    	if (entity.isDead) return;
    	else if (!(entity instanceof EntityItem || entity instanceof EntityXPOrb)) {
    		if (EnergyItemHandler.getEnergy(item) < EnergyUsage) {
        		player.addChatMessage(new ChatComponentText("Out of Energy!"));
                return;
        	}
    		float dmg = BaseDamage;
    		if (entity instanceof EntityLivingBase) {
    			dmg = Math.min(MaxDamage, Math.max(dmg, ((EntityLivingBase)entity).getMaxHealth()));
    		}
    		float am = AntimatterItemHandler.getAntimatter(item) + item.stackTagCompound.getFloat("buff");
            dmg = Math.min(dmg, (float)Math.pow(am / AMDamage, 1D / AMDmgExp));
            if (dmg < 1F) return;
            EnergyItemHandler.addEnergy(item, -EnergyUsage, false);
            float r = (float)Math.pow(dmg, AMDmgExp) * AMDamage - item.stackTagCompound.getFloat("buff");
            int n = (int)Math.ceil(r);
            if (n != 0) item.stackTagCompound.setInteger("antimatter", item.stackTagCompound.getInteger("antimatter") - n);
            item.stackTagCompound.setFloat("buff", n - r);
    		entity.attackEntityFrom(DamageSource.causePlayerDamage(player), dmg);
    		player.worldObj.playSoundEffect(entity.posX, entity.posY, entity.posZ, "random.explosion", 2.0F, 2.7F);
    	}
    	for (EntityItem ei : (List<EntityItem>)player.worldObj.getEntitiesWithinAABB(EntityItem.class, AxisAlignedBB.getBoundingBox(entity.posX - 2.5D, entity.posY - 2.5D, entity.posZ - 2.5D, entity.posX + 2.5D, entity.posY + 2.5D, entity.posZ + 2.5D))) {
			if (!ei.isDead && MatterOrbItemHandler.canInsert(item, ei.getEntityItem())) {
				MatterOrbItemHandler.addItemStacks(item, ei.getEntityItem());
				ei.setDead();
			}
		}
    	for (EntityXPOrb ei : (List<EntityXPOrb>)player.worldObj.getEntitiesWithinAABB(EntityXPOrb.class, AxisAlignedBB.getBoundingBox(entity.posX - 5D, entity.posY - 5D, entity.posZ - 5D, entity.posX + 5D, entity.posY + 5D, entity.posZ + 5D))) {
    		ei.setPosition(player.posX, player.posY - 0.5D, player.posZ);
    	}
    }
    
    @Override
    public boolean onItemUse(ItemStack item, EntityPlayer player, World world, int x, int y, int z, int s, float X, float Y, float Z) 
    {
    	int mode = item.stackTagCompound.getByte("mode");
    	if (player.isSneaking()) return false;
        if (world.isRemote) {
        	EnergyItemHandler.addEnergy(item, 0, false);
            return true;
        }
        if (mode < 0) return true;
        else if (mode < 5) {
        	if (!AreaProtect.instance.isOperationAllowed(player.getCommandSenderName(), world, x - mode, x + mode + 1, z - mode, z + mode + 1)) {
                player.addChatMessage(new ChatComponentText("Block is Protected"));
                return true;
            }
        	byte ax = (byte)(s / 2);
        	for (int i = -mode; i <= mode; i++)
        		for (int j = -mode; j <= mode; j++)
        			if (!this.breakBlock(item, player, world, x + (ax==2?0:i), y + (ax==0?0:j), z + (ax==1?0: ax==2?i:j)))
        				return true;
        } else if (mode == 5) {
        	if (!AreaProtect.instance.isOperationAllowed(player.getCommandSenderName(), world, x - 7, x + 8, z - 7, z + 8)) {
                player.addChatMessage(new ChatComponentText("Block is Protected"));
                return true;
            }
        	Block id = world.getBlock(x, y, z);
        	int m = world.getBlockMetadata(x, y, z);
        	for (int i = -7; i <= 7; i++)
        		for (int j = -7; j <= 7; j++)
        			for (int k = -7; k <= 7; k++)
        				if (world.getBlock(x + i, y + j, z + k) == id && world.getBlockMetadata(x + i, y + j, z + k) == m)
        					if (!this.breakBlock(item, player, world, x + i, y + j, z + k))
        						return true;
        }
        return true;
    }
    
    private boolean breakBlock(ItemStack item, EntityPlayer player, World world, int x, int y, int z)
    {
    	Enchantments ench = new Enchantments(item);
    	if (EnergyItemHandler.getEnergy(item) < ench.Euse) {
    		player.addChatMessage(new ChatComponentText("Out of Energy!"));
            return false;
    	}
    	Block block = world.getBlock(x, y, z);
        int m = world.getBlockMetadata(x, y, z);
        if (block == null || block.isAir(world, x, y, z)) return true;
        float r = block.getExplosionResistance(player, world, x, y, z, player.posX, player.posY, player.posZ) * ench.amMult;
        float am = AntimatterItemHandler.getAntimatter(item) + item.stackTagCompound.getFloat("buff");
        if (am < r) {
            player.addChatMessage(new ChatComponentText("Not enough Antimatter, Needed: " + r + " pg"));
            return false;
        }
        ItemStack[] drop;
        if (ench.silktouch && block.canSilkHarvest(world, player, x, y, z, m)) {
        	drop = new ItemStack[]{new ItemStack(block, 1, block.getDamageValue(world, x, y, z))};
        } else {
            ArrayList<ItemStack> list = block.getDrops(world, x, y, z, m, ench.fortune);
            drop = list == null ? null : list.toArray(new ItemStack[list.size()]);
        }
        if (!MatterOrbItemHandler.canInsert(item, drop)) {
            player.addChatMessage(new ChatComponentText("Matter Orb is full!"));
            return false;
        }
        EnergyItemHandler.addEnergy(item, -ench.Euse, false);
        r -= item.stackTagCompound.getFloat("buff");
        int n = (int)Math.ceil(r);
        if (n != 0) item.stackTagCompound.setInteger("antimatter", item.stackTagCompound.getInteger("antimatter") - n);
        item.stackTagCompound.setFloat("buff", n - r);
        world.setBlockToAir(x, y, z);
        MatterOrbItemHandler.addItemStacks(item, drop);
        return true;
    }
    
    public static class Enchantments {
    	public boolean silktouch;
    	public short fortune;
    	public short efficiency;
    	public short unbreaking;
    	public float amMult;
    	public int Euse;
    	public Enchantments(ItemStack item)
    	{
    		NBTTagList list = item.getEnchantmentTagList();
    		if (list != null)
    			for (int i = 0; i < list.tagCount(); i++) {
    				NBTTagCompound nbt = list.getCompoundTagAt(i);
    				short id = nbt.getShort("id");
    				short lvl = nbt.getShort("lvl");
    				silktouch |= id == Enchantment.silkTouch.effectId;
    				if (id == Enchantment.fortune.effectId) fortune = lvl;
    				else if (id == Enchantment.efficiency.effectId) efficiency = lvl;
    				else if (id == Enchantment.unbreaking.effectId) unbreaking = lvl;
    			}
    		Euse = EnergyUsage * 3 / (3 + unbreaking);
    		amMult = AmUsage * 0.5F / (float)(4 + efficiency) * (silktouch ? 2.0F : 1F + 0.5F * (float)fortune);
    	}
    }

    @Override
    public int getAmCapacity(ItemStack item) 
    {
        return Config.tankCap[4];
    }

    @Override
    public String getAntimatterTag(ItemStack item) 
    {
        return "antimatter";
    }

    @Override
    public int getMaxTypes(ItemStack item) 
    {
        return ItemMatterOrb.MaxTypes;
    }

    @Override
    public String getMatterTag(ItemStack item) 
    {
        return "matter";
    }
    
    @Override
	public FluidStack getFluid(ItemStack item) 
	{
		return new FluidStack(Automation.L_antimatter, AntimatterItemHandler.getAntimatter(item));
	}

	@Override
	public int getCapacity(ItemStack item) 
	{
		return getAmCapacity(item);
	}

	@Override
	public int fill(ItemStack item, FluidStack resource, boolean doFill) 
	{
		if (resource == null || resource.getFluid() != Automation.L_antimatter) return 0;
		if (doFill) return AntimatterItemHandler.addAntimatter(item, resource.amount);
		else return Math.min(resource.amount, getAmCapacity(item) - AntimatterItemHandler.getAntimatter(item));
	}

	@Override
	public FluidStack drain(ItemStack item, int maxDrain, boolean doDrain) {
		if (doDrain) return new FluidStack(Automation.L_antimatter, -AntimatterItemHandler.addAntimatter(item, -maxDrain));
		else return new FluidStack(Automation.L_antimatter, Math.min(maxDrain, AntimatterItemHandler.getAntimatter(item)));
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void onSneakScroll(ItemStack item, EntityPlayer player, int scroll) 
	{
		byte cmd = 2;
		if (scroll < 0) cmd = 0;
		else if (scroll > 0) cmd = 1;
		try {
			ByteArrayOutputStream bos = BlockGuiHandler.getPacketTargetData(0, -1, 0);
			bos.write(cmd);
			BlockGuiHandler.sendPacketToServer(bos);
		} catch (IOException e) {}
	}

	@Override
	public Container getContainer(World world, EntityPlayer player, int x, int y, int z) 
	{
		return new ContainerAMLEnchant(player);
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public GuiContainer getGui(World world, EntityPlayer player, int x, int y, int z) 
	{
		return new GuiAMLEnchant(new ContainerAMLEnchant(player));
	}

	@Override
	public void onPlayerCommand(World world, EntityPlayer player, DataInputStream dis) throws IOException 
	{
		ItemStack item = player.getCurrentEquippedItem();
		byte cmd = dis.readByte();
		if (cmd == 0) {
			item.stackTagCompound.setByte("mode", (byte)((item.stackTagCompound.getByte("mode") - 1 + modes.length) % modes.length));
		} else if (cmd == 1) {
			item.stackTagCompound.setByte("mode", (byte)((item.stackTagCompound.getByte("mode") + 1) % modes.length));
		} else if (cmd == 2) {
			BlockGuiHandler.openItemGui(player, world, 0, -1, 0);
		}
	}
    
}
