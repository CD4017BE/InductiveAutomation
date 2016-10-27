package cd4017be.automation.Item;

import java.util.List;

import cd4017be.api.Capabilities;
import cd4017be.api.automation.PipeEnergy;
import cd4017be.automation.Automation;
import cd4017be.lib.DefaultItem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

/**
 *
 * @author CD4017BE
 */
public class ItemVoltMeter extends DefaultItem {

	public ItemVoltMeter(String id) {
		super(id);
		this.setCreativeTab(Automation.tabAutomation);
		this.setMaxStackSize(1);
	}

	@Override
	public void addInformation(ItemStack item, EntityPlayer player, List<String> list, boolean par4) {
		if (item.getTagCompound() != null) {
			list.add("Link=(" + item.getTagCompound().getInteger("lx") + ", " + item.getTagCompound().getInteger("ly") + ", " + item.getTagCompound().getInteger("lz") + ")");
		}
		super.addInformation(item, player, list, par4);
	}

	@Override
	public String getItemStackDisplayName(ItemStack item) {
		if (item.getTagCompound() == null) {
			return super.getItemStackDisplayName(item);
		} else if (item.getTagCompound().getInteger("ly") < 0) {
			return super.getItemStackDisplayName(item) + " (not linked)";
		} else {
			return super.getItemStackDisplayName(item) + " (" + String.format("%.2f", item.getTagCompound().getFloat("Ucap")) + "V / " + item.getTagCompound().getInteger("Umax") + "V)";
		}
	}

	@Override
	public void onUpdate(ItemStack item, World world, Entity entity, int i, boolean b) {
		if (item.getTagCompound() == null) createNBT(item);
		if (world.isRemote || item.getTagCompound().getInteger("ly") < 0) return;
		TileEntity te = world.getTileEntity(new BlockPos(item.getTagCompound().getInteger("lx"), item.getTagCompound().getInteger("ly"), item.getTagCompound().getInteger("lz")));
		PipeEnergy energy = te != null ? te.getCapability(Capabilities.ELECTRIC_CAPABILITY, EnumFacing.VALUES[(item.getTagCompound().getByte("ls") & 0xff) % 6]) : null;
		if (energy != null) {
			float e = item.getTagCompound().getFloat("Ucap") * 19F + (float)energy.Ucap;
			item.getTagCompound().setFloat("Ucap", e / 20F);
		} else {
			item.getTagCompound().setInteger("ly", -1);
		}
	}

	private void createNBT(ItemStack item) {
		item.setTagCompound(new NBTTagCompound());
		item.getTagCompound().setFloat("Ucap", 0F);
		item.getTagCompound().setInteger("lx", 0);
		item.getTagCompound().setInteger("ly", -1);
		item.getTagCompound().setInteger("lz", 0);
	}

	@Override
	public EnumActionResult onItemUse(ItemStack item, EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing s, float X, float Y, float Z) {
		if (world.isRemote) return EnumActionResult.SUCCESS;
		if (item.getTagCompound() == null) createNBT(item);
		TileEntity te = world.getTileEntity(pos);
		PipeEnergy energy = te != null ? te.getCapability(Capabilities.ELECTRIC_CAPABILITY, EnumFacing.VALUES[(item.getTagCompound().getByte("ls") & 0xff) % 6]) : null;
		if (energy != null) {
			item.getTagCompound().setInteger("lx", pos.getX());
			item.getTagCompound().setInteger("ly", pos.getY());
			item.getTagCompound().setInteger("lz", pos.getZ());
			item.getTagCompound().setFloat("Ucap", (float)energy.Ucap);
			item.getTagCompound().setInteger("Umax", energy.Umax);
			item.getTagCompound().setByte("ls", (byte)s.getIndex());
			player.addChatMessage(new TextComponentString("Energy linked"));
		} else {
			item.getTagCompound().setInteger("ly", -1);
			player.addChatMessage(new TextComponentString("Unlinked"));
		}
		return EnumActionResult.SUCCESS;
	}

}
