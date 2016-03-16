package cd4017be.automation.Item;

import java.io.IOException;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import cd4017be.api.automation.EnergyItemHandler;
import cd4017be.api.automation.TeslaNetwork;
import cd4017be.automation.Automation;
import cd4017be.automation.Config;
import cd4017be.automation.Gui.ContainerPortableTesla;
import cd4017be.automation.Gui.GuiPortableTesla;
import cd4017be.lib.BlockGuiHandler;
import cd4017be.lib.DefaultItem;
import cd4017be.lib.IGuiItem;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemPortableTesla extends DefaultItem implements IGuiItem
{
	
	public ItemPortableTesla(String id) 
	{
		super(id);
        this.setCreativeTab(Automation.tabAutomation);
        this.setMaxStackSize(1);
	}

	@Override
	public EnumRarity getRarity(ItemStack item) 
    {
		return EnumRarity.UNCOMMON;
	}
	
	@Override
	public Container getContainer(World world, EntityPlayer player, int x, int y, int z) 
	{
		return new ContainerPortableTesla(player);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public GuiContainer getGui(World world, EntityPlayer player, int x, int y, int z) 
	{
		return new GuiPortableTesla(new ContainerPortableTesla(player));
	}

	@Override
	public void onPlayerCommand(World world, EntityPlayer player, PacketBuffer dis) throws IOException 
	{
		ItemStack item = player.getCurrentEquippedItem();
		if (item.stackTagCompound == null) item.stackTagCompound = new NBTTagCompound();
		byte cmd = dis.readByte();
		if (cmd == 0) item.stackTagCompound.setShort("mode", dis.readShort());
		else if (cmd == 1) item.stackTagCompound.setShort("freq", dis.readShort());
	}

	@Override
	public ItemStack onItemRightClick(ItemStack item, World world, EntityPlayer player) 
	{
		BlockGuiHandler.openItemGui(player, world, 0, -1, 0);
		return item;
	}

	@Override
	public void onUpdate(ItemStack item, World world, Entity entity, int s, boolean b) 
	{
		if (!(entity instanceof EntityPlayer) || world.isRemote || item.stackTagCompound == null) return;
		EntityPlayer player = (EntityPlayer)entity;
		TeslaNetwork.instance.transmittEnergy(new TeslaTransmitterItem(player, s, item, Config.Umax[3]));
		double Emax = Config.Umax[3] * Config.Umax[3];
		short mode = item.stackTagCompound.getShort("mode");
		if ((mode & 0x7) != 0) 
		{
			InventoryPlayer inv = player.inventory;
            double E0 = item.stackTagCompound.getDouble("voltage");
			double E = (E0 *= E0);
			boolean kJ = (mode & 0x1) != 0, RF = (mode & 0x2) != 0, EU = (mode & 0x4) != 0;
			if ((mode & 0x0300) == 0x0100) {
				for (int i = 0; i < InventoryPlayer.getHotbarSize(); i++) {
					E -= this.chargeItem(inv.mainInventory[i], E, kJ, RF, EU);
				}
			} else if ((mode & 0x0300) == 0x0200) {
				for (int i = 0; i < InventoryPlayer.getHotbarSize(); i++) {
					E += this.dischargeItem(inv.mainInventory[i], Emax - E, kJ, RF, EU);
				}
			}
			
			if ((mode & 0x0c00) == 0x0400) {
				for (int i = InventoryPlayer.getHotbarSize(); i < inv.mainInventory.length; i++) {
					E -= this.chargeItem(inv.mainInventory[i], E, kJ, RF, EU);
				}
			} else if ((mode & 0x0c00) == 0x0800) {
				for (int i = InventoryPlayer.getHotbarSize(); i < inv.mainInventory.length; i++) {
					E += this.dischargeItem(inv.mainInventory[i], Emax - E, kJ, RF, EU);
				}
			}
			
			if ((mode & 0x3000) == 0x1000) {
				for (int i = 0; i < inv.armorInventory.length; i++) {
					E -= this.chargeItem(inv.armorInventory[i], E, kJ, RF, EU);
				}
			} else if ((mode & 0x3000) == 0x2000) {
				for (int i = 0; i < inv.armorInventory.length; i++) {
					E += this.dischargeItem(inv.armorInventory[i], Emax - E, kJ, RF, EU);
				}
			}
            if (E != E0) item.stackTagCompound.setDouble("voltage", Math.sqrt(E));
		}
	}
	
	private double chargeItem(ItemStack item, double e, boolean kJ, boolean RF, boolean EU)
	{
		if (item == null) return 0;
		else if (kJ && EnergyItemHandler.isEnergyItem(item)) return (double)EnergyItemHandler.addEnergy(item, (int)Math.floor(e * 0.001D), true) * 1000D;
		/*else if (RF && item.getItem() instanceof IEnergyContainerItem) {
			IEnergyContainerItem cont = (IEnergyContainerItem)item.getItem();
			return (double)cont.receiveEnergy(item, (int)Math.floor(e / EnergyThermalExpansion.E_Factor), false) * EnergyThermalExpansion.E_Factor;
		}*/ else return 0;//TODO reimplement
	}
	
	private double dischargeItem(ItemStack item, double e, boolean kJ, boolean RF, boolean EU)
	{
		if (item == null) return 0;
		if (kJ && EnergyItemHandler.isEnergyItem(item)) return -(double)EnergyItemHandler.addEnergy(item, -(int)Math.floor(e * 0.001D), true) * 1000D;
		/*else if (RF && item.getItem() instanceof IEnergyContainerItem) {
			IEnergyContainerItem cont = (IEnergyContainerItem)item.getItem();
			return (double)cont.extractEnergy(item, (int)Math.floor(e / EnergyThermalExpansion.E_Factor), false) * EnergyThermalExpansion.E_Factor;
		}*/ else return 0;//TODO reimplement
	}

}
