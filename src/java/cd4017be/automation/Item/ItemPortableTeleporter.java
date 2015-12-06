package cd4017be.automation.Item;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cd4017be.api.automation.AreaProtect;
import cd4017be.api.automation.EnergyItemHandler;
import cd4017be.api.automation.EnergyItemHandler.IEnergyItem;
import cd4017be.automation.Automation;
import cd4017be.automation.Config;
import cd4017be.automation.Gui.ContainerPortableTeleporter;
import cd4017be.automation.Gui.GuiPortableTeleporter;
import cd4017be.automation.TileEntity.ESU;
import cd4017be.lib.BlockGuiHandler;
import cd4017be.lib.IGuiItem;
import cd4017be.lib.MovedBlock;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.material.Material;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class ItemPortableTeleporter extends ItemEnergyCell implements IGuiItem, IEnergyItem 
{
	
	public static float energyUse = 8.0F;
	
	public ItemPortableTeleporter(String id, String tex) 
	{
		super(id, tex, Config.Ecap[1]);
		this.setMaxDamage(16);
		this.setMaxStackSize(1);
	}
	
	@Override
	public EnumRarity getRarity(ItemStack item) 
    {
		return EnumRarity.rare;
	}

	@Override
	public Container getContainer(World world, EntityPlayer player, int x, int y, int z) 
	{
		return new ContainerPortableTeleporter(player);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public GuiContainer getGui(World world, EntityPlayer player, int x, int y, int z) 
	{
		return new GuiPortableTeleporter(new ContainerPortableTeleporter(player));
	}

	@Override
	public void onPlayerCommand(World world, EntityPlayer player, DataInputStream dis) throws IOException 
	{
		ItemStack item = player.getCurrentEquippedItem();
		if (item.stackTagCompound == null) item.stackTagCompound = new NBTTagCompound();
		NBTTagList points = item.stackTagCompound.getTagList("points", 10);
		NBTTagCompound tag;
		byte cmd = dis.readByte();
		if (cmd == 0) {//teleport
			int i = dis.readByte();
			if (i < 0 || i >= points.tagCount()) return;
			tag = points.getCompoundTagAt(i);
			int x = tag.getInteger("x");
			int y = tag.getInteger("y");
			int z = tag.getInteger("z");
			if (y < 0 || y > 256) {
				player.addChatMessage(new ChatComponentText("Destination outside the world!"));
				return;
			} else if (world.getBlock(x, y, z).getMaterial().blocksMovement() || world.getBlock(x, y + 1, z).getMaterial().blocksMovement()) {
				player.addChatMessage(new ChatComponentText("Destination obscured!"));
				return;
			}
			this.teleportTo(item, world, x, y, z, player);
			player.setCurrentItemOrArmor(0, item);
			return;
		} else if (cmd == 1) {//add Point
			if (points.tagCount() >= 64) {
				player.addChatMessage(new ChatComponentText("64 waypoints max!"));
				return;
			}
			tag = new NBTTagCompound();
			tag.setInteger("x", MathHelper.floor_double(player.posX));
			tag.setInteger("y", MathHelper.floor_double(player.posY));
			tag.setInteger("z", MathHelper.floor_double(player.posZ));
			tag.setString("n", "P" + points.tagCount());
			points.appendTag(tag);
		} else if (cmd == 2) {//edit Point
			int i = dis.readByte();
			if (i < 0 || i >= points.tagCount()) return;
			tag = points.getCompoundTagAt(i);
			tag.setInteger("x", dis.readInt());
			tag.setInteger("y", dis.readInt());
			tag.setInteger("z", dis.readInt());
			tag.setString("n", dis.readUTF());
		} else if (cmd == 3) {//delete Point
			int i = dis.readByte();
			if (i >= 0 && i < points.tagCount()) points.removeTag(i);
		}
		item.stackTagCompound.setTag("points", points);
		player.setCurrentItemOrArmor(0, item);
	}

	@Override
	public int getChargeSpeed(ItemStack item) 
	{
		return 400;
	}

	@Override
	public ItemStack onItemRightClick(ItemStack item, World world, EntityPlayer player) 
	{
		if (player.isSneaking()) {
			if (world.isRemote) return item;
			Vec3 pos = Vec3.createVectorHelper(player.posX, player.posY + 1.62D - player.yOffset, player.posZ);
	        Vec3 dir = player.getLookVec();
	        MovingObjectPosition obj = world.func_147447_a(pos, pos.addVector(dir.xCoord * 256, dir.yCoord * 256, dir.zCoord * 256), false, true, false);
	        if (obj == null) return item;
	        int[] t = this.findTarget(obj, world);
	        if (t != null) {
	        	this.teleportTo(item, world, t[0], t[1], t[2], player);
	        } else {
	        	player.addChatMessage(new ChatComponentText("Destination obscured!"));
	        }
		} else {
			BlockGuiHandler.openItemGui(player, world, 0, -1, 0);
		}
		return item;
	}
	
	private int[] findTarget(MovingObjectPosition pos, World world)
	{
		if (!world.getBlock(pos.blockX, pos.blockY + 1, pos.blockZ).getMaterial().blocksMovement() && !world.getBlock(pos.blockX, pos.blockY + 2, pos.blockZ).getMaterial().blocksMovement())
			return new int[]{pos.blockX, pos.blockY + 1, pos.blockZ};
		ForgeDirection side = ForgeDirection.getOrientation(pos.sideHit);
		if (side == ForgeDirection.UP || side == ForgeDirection.UNKNOWN) return null;
		if (side == ForgeDirection.DOWN) {
			if (!world.getBlock(pos.blockX, pos.blockY - 1, pos.blockZ).getMaterial().blocksMovement() && !world.getBlock(pos.blockX, pos.blockY - 2, pos.blockZ).getMaterial().blocksMovement())
				return new int[]{pos.blockX, pos.blockY - 2, pos.blockZ};
			else return null;
		}
		int x = pos.blockX + side.offsetX, y = pos.blockY + side.offsetY, z = pos.blockZ + side.offsetZ;
		if (world.getBlock(x, y, z).getMaterial().blocksMovement()) return null;
		else if (!world.getBlock(x, y - 1, z).getMaterial().blocksMovement()) return new int[]{x, y - 1, z};
		else if (!world.getBlock(x, y + 1, z).getMaterial().blocksMovement()) return new int[]{x, y, z};
		else return null;
	}
	
	public void teleportTo(ItemStack item, World world, int x, int y, int z, EntityPlayer player)
	{
		int x0 = MathHelper.floor_double(player.posX),
			y0 = MathHelper.floor_double(player.posY) - 1,
			z0 = MathHelper.floor_double(player.posZ);
		int dx = x - x0, dy = y - y0, dz = z - z0;
		int e = MathHelper.floor_double(Math.sqrt(dx * dx + dy * dy + dz * dz) * energyUse);
		if (EnergyItemHandler.getEnergy(item) < e) {
			player.addChatMessage(new ChatComponentText("Not enough Energy: " + e + " kJ needed!"));
			return;
		}
		if (!AreaProtect.instance.isInteractingAllowed(player.getCommandSenderName(), world, x >> 4, z >> 4)) {
			player.addChatMessage(new ChatComponentText("Destination protected!"));
			return;
		}
		EnergyItemHandler.addEnergy(item, -e, false);
		int dim = player.dimension;
		List<Entity> entities = world.getEntitiesWithinAABB(Entity.class, AxisAlignedBB.getBoundingBox(x0, y0, z0, x0 + 1, y0 + 2, z0 + 1));
		for (Entity entity : entities) {
			MovedBlock.moveEntity(entity, dim, (double)x + 0.5D, (double)y + (double)(entity.ySize - entity.yOffset), (double)z + 0.5D);
		}
	}

}
