/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cd4017be.automation.TileEntity;

import net.minecraft.network.PacketBuffer;

import java.io.IOException;

import cd4017be.automation.Config;
import cd4017be.automation.Objects;
import cd4017be.lib.Gui.SlotHolo;
import cd4017be.lib.Gui.SlotTank;
import cd4017be.lib.Gui.TileContainer;
import cd4017be.lib.Gui.TileContainer.TankSlot;
import cd4017be.lib.TileEntityData;
import cd4017be.lib.templates.AutomatedTile;
import cd4017be.lib.templates.Inventory;
import cd4017be.lib.templates.Inventory.Group;
import cd4017be.lib.templates.TankContainer;
import cd4017be.lib.templates.TankContainer.Tank;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidHandler;

/**
 *
 * @author CD4017BE
 */
public class LavaCooler extends AutomatedTile implements ISidedInventory, IFluidHandler
{
	private static final int SteamOut = 20; 
    public static enum Mode {
    	nothing(null, 10, 10, 50), 
    	cobblestone(new ItemStack(Blocks.COBBLESTONE), 4, 4, 20), 
    	stone(new ItemStack(Blocks.STONE), 40, 80, 200), 
    	obsidian(new ItemStack(Blocks.OBSIDIAN), 1000, 500, 2500);
    	public int time;
    	public int lava;
    	public int water;
    	private ItemStack item;
    	
    	private Mode(ItemStack out, int l, int w, int t)
    	{
    		this.item = out;
    		this.lava = l;
    		this.water = w;
    		this.time = t;
    	}
    	
    	public ItemStack getOutput()
    	{
    		return item == null ? null : item.copy();
    	}
    	
    	public static Mode get(int id)
    	{
    		return id < 0 || id >= values().length ? null : values()[id];
    	}
    	
    }
	
    public LavaCooler()
    {
        netData = new TileEntityData(2, 2, 0, 3);
        inventory = new Inventory(this, 5, new Group(0, 2, 1));
        tanks = new TankContainer(this, new Tank(Config.tankCap[1], -1, Objects.L_water).setIn(3), new Tank(Config.tankCap[1], -1, Objects.L_lava).setIn(2), new Tank(Config.tankCap[1], 1, Objects.L_steam).setOut(4)).setNetLong(1);
    }

    @Override
    public void update() 
    {
    	super.update();
        if (this.worldObj.isRemote) return;
        Mode mode = Mode.get(netData.ints[0] & 0xf);
        if (mode == null) {
        	Mode m = Mode.get(netData.ints[0] >> 4 & 0x3);
        	if (tanks.getAmount(0) >= m.water && tanks.getAmount(1) >= m.lava) {
        		tanks.drain(0, m.water, true);
        		tanks.drain(1, m.lava, true);
        		mode = m;
        		netData.ints[0] = (netData.ints[0] & 0x30) | m.ordinal();
        		netData.ints[1] = m.time;
        	}
        }
        if (mode == null || worldObj.isBlockPowered(getPos())) return;
        if (netData.ints[1] > 0) {
        	tanks.fill(2, new FluidStack(Objects.L_steam, SteamOut), true);
        	netData.ints[1]--;
        }
        if (netData.ints[1] <= 0) {
        	if (this.putItemStack(mode.getOutput(), this, -1, 0, 1) == null) {
        		netData.ints[0] = (netData.ints[0] & 0x30) | 4;
        	}
        }
    }

    @Override
    public ItemStack removeStackFromSlot(int i) 
    {
        if (i < 5)return super.removeStackFromSlot(i);
        else return null;
    }
    
    @Override
	public ItemStack getStackInSlot(int i) 
    {
		if (i < 5)return super.getStackInSlot(i);
		else return Mode.get(i - 5).getOutput();
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack) 
	{
		if (i < 5)super.setInventorySlotContents(i, itemstack);
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack)
	{
		if (i < 5)return super.isItemValidForSlot(i, itemstack);
		else return false;
	}

	@Override
	public boolean canInsertItem(int i, ItemStack itemstack, EnumFacing j) {
		if (i < 5) return super.canInsertItem(i, itemstack, j);
		else return false;
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemstack, EnumFacing j) {
		if (i < 5) return super.canExtractItem(i, itemstack, j);
		else return false;
	}

	@Override
	public int getSizeInventory() {
		return 9;
	}
    
    public int getCoolScaled(int s)
    {
        Mode m = Mode.get(netData.ints[0] & 0xf);
    	return m == null ? 0 : netData.ints[1] * s / m.time;
    }
    
    public String getOutputName()
    {
    	Mode m = Mode.get(netData.ints[0] & 0xf);
    	if (m == null) return "Inactive";
    	ItemStack i = m.getOutput();
    	return (i == null ? "Default" : i.getDisplayName()).concat("\n" + String.format("%d", (m.time - netData.ints[1]) * 100 / m.time) + " %");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) 
    {
        nbt.setInteger("mode", netData.ints[0]);
        nbt.setInteger("cool", netData.ints[1]);
        return super.writeToNBT(nbt);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) 
    {
        super.readFromNBT(nbt);
        netData.ints[0] = nbt.getInteger("mode");
        netData.ints[1] = nbt.getInteger("cool");
    }
    
    @Override
	protected void customPlayerCommand(byte cmd, PacketBuffer dis, EntityPlayerMP player) throws IOException 
    {
		if (cmd >= 0 && cmd < 4) {
			netData.ints[0] = (netData.ints[0] & 0xf) | cmd << 4;
		}
	}

	@Override
    public void initContainer(TileContainer container)
    {
    	container.addItemSlot(new Slot(this, 0, 8, 52));
    	container.addItemSlot(new Slot(this, 1, 26, 52));
    	
        container.addItemSlot(new SlotTank(this, 2, 53, 34));
        container.addItemSlot(new SlotTank(this, 3, 107, 34));
        container.addItemSlot(new SlotTank(this, 4, 143, 34));
        
        container.addItemSlot(new SlotHolo(this, 5, 8, 16, true, false));
        container.addItemSlot(new SlotHolo(this, 6, 26, 16, true, false));
        container.addItemSlot(new SlotHolo(this, 7, 8, 34, true, false));
        container.addItemSlot(new SlotHolo(this, 8, 26, 34, true, false));
        
        container.addPlayerInventory(8, 86);
        
        container.addTankSlot(new TankSlot(tanks, 0, 98, 16, true));
        container.addTankSlot(new TankSlot(tanks, 1, 44, 16, true));
        container.addTankSlot(new TankSlot(tanks, 2, 134, 16, true));
    }
    
    @Override
    public int[] stackTransferTarget(ItemStack item, int s, TileContainer container) 
    {
        int[] pi = container.getPlayerInv();
        if (s < pi[0]) return pi;
        else return new int[]{0, 2};
    }
    
}
