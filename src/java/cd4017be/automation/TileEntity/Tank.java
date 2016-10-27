package cd4017be.automation.TileEntity;

import java.util.ArrayList;

import cd4017be.automation.Config;
import cd4017be.lib.Gui.DataContainer;
import cd4017be.lib.Gui.DataContainer.IGuiData;
import cd4017be.lib.Gui.SlotTank;
import cd4017be.lib.Gui.TileContainer;
import cd4017be.lib.Gui.TileContainer.TankSlot;
import cd4017be.lib.templates.AutomatedTile;
import cd4017be.lib.templates.Inventory;
import cd4017be.lib.templates.TankContainer;
import cd4017be.lib.util.Utils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraftforge.fluids.FluidStack;

/**
 *
 * @author CD4017BE
 */
public class Tank extends AutomatedTile implements IGuiData {

	private int lastAmount;
	
	public Tank() {
		inventory = new Inventory(2, 2, null).group(0, 0, 1, Utils.ACC).group(1, 1, 2, Utils.ACC);
		tanks = new TankContainer(1, 1).tank(0, this.capacity(), Utils.OUT, 0, 1);
	}

	protected int capacity() {return Config.tankCap[2];}
	
	@Override
	public void update() 
	{
		super.update();
		if (worldObj.isRemote) return;
		int d = tanks.getAmount(0) - lastAmount;
		if ((d != 0 && (tanks.getAmount(0) == 0 || lastAmount == 0)) || d * d > 250000){
			lastAmount = tanks.getAmount(0);
			this.markUpdate();
		} 
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) 
	{
		tanks.readFromNBT(pkt.getNbtCompound(), "tank");
	}

	@Override
	public SPacketUpdateTileEntity getUpdatePacket() 
	{
		NBTTagCompound nbt = new NBTTagCompound();
		tanks.writeToNBT(nbt, "tank");
		return new SPacketUpdateTileEntity(getPos(), -1, nbt);
	}
	
	@Override
	public void initContainer(DataContainer cont) {
		TileContainer container = (TileContainer)cont;
		container.addItemSlot(new SlotTank(inventory, 0, 184, 74));
		container.addItemSlot(new SlotTank(inventory, 1, 202, 74));
		
		container.addPlayerInventory(8, 16);
		
		container.addTankSlot(new TankSlot(tanks, 0, 184, 16, (byte)0x23));
	}

	@Override
	public void onPlaced(EntityLivingBase entity, ItemStack item) 
	{
		tanks.setFluid(0, FluidStack.loadFluidStackFromNBT(item.getTagCompound()));
	}

	@Override
	public ArrayList<ItemStack> dropItem(IBlockState state, int fortune) 
	{
		ItemStack item = new ItemStack(this.getBlockType());
		if (tanks.getAmount(0) > 0) {
			item.setTagCompound(new NBTTagCompound());
			tanks.fluids[0].writeToNBT(item.getTagCompound());
		}
		ArrayList<ItemStack> list = new ArrayList<ItemStack>();
		list.add(item);
		return list;
	}
	
}
