package cd4017be.automation.TileEntity;

import net.minecraft.network.PacketBuffer;

import java.io.IOException;

import cd4017be.automation.Config;
import cd4017be.automation.Objects;
import cd4017be.lib.Gui.DataContainer;
import cd4017be.lib.Gui.DataContainer.IGuiData;
import cd4017be.lib.Gui.SlotHolo;
import cd4017be.lib.Gui.SlotTank;
import cd4017be.lib.Gui.TileContainer;
import cd4017be.lib.Gui.TileContainer.TankSlot;
import cd4017be.lib.templates.AutomatedTile;
import cd4017be.lib.templates.Inventory;
import cd4017be.lib.templates.TankContainer;
import cd4017be.lib.util.ItemFluidUtil;
import cd4017be.lib.util.Utils;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.SlotItemHandler;

/**
 *
 * @author CD4017BE
 */
public class LavaCooler extends AutomatedTile implements IGuiData {

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
	
	public int cfg, progress;
	
	public LavaCooler()
	{
		inventory = new Inventory(5, 1, null).group(0, 0, 2, Utils.OUT);
		tanks = new TankContainer(3, 3).tank(0, Config.tankCap[1], Utils.IN, 3, -1, Objects.L_water).tank(1, Config.tankCap[1], Utils.IN, 2, -1, Objects.L_lava).tank(2, Config.tankCap[1], Utils.OUT, -1, 4, Objects.L_steam);
	}

	@Override
	public void update() 
	{
		super.update();
		if (this.worldObj.isRemote) return;
		Mode mode = Mode.get(cfg & 0xf);
		if (mode == null) {
			Mode m = Mode.get(cfg >> 4 & 0x3);
			if (tanks.getAmount(0) >= m.water && tanks.getAmount(1) >= m.lava) {
				tanks.drain(0, m.water, true);
				tanks.drain(1, m.lava, true);
				mode = m;
				cfg = (cfg & 0x30) | m.ordinal();
				progress = m.time;
			}
		}
		if (mode == null || worldObj.isBlockPowered(getPos())) return;
		if (progress > 0) {
			tanks.fill(2, new FluidStack(Objects.L_steam, SteamOut), true);
			progress--;
		}
		if (progress <= 0) {
			if (ItemFluidUtil.putInSlots(inventory, mode.getOutput(), 0, 1) == null) {
				cfg = (cfg & 0x30) | 4;
			}
		}
	}

	public float getCool() {
		Mode m = Mode.get(cfg & 0xf);
		return m == null ? Float.NaN : (float)progress / (float)m.time;
	}
	
	public String getOutputName()
	{
		Mode m = Mode.get(cfg & 0xf);
		if (m == null) return "Inactive";
		ItemStack i = m.getOutput();
		return (i == null ? "Default" : i.getDisplayName()).concat("\n" + String.format("%d", (m.time - progress) * 100 / m.time) + " %");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) 
	{
		nbt.setInteger("mode", cfg);
		nbt.setInteger("cool", progress);
		return super.writeToNBT(nbt);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) 
	{
		super.readFromNBT(nbt);
		cfg = nbt.getInteger("mode");
		progress = nbt.getInteger("cool");
	}
	
	@Override
	protected void customPlayerCommand(byte cmd, PacketBuffer dis, EntityPlayerMP player) throws IOException 
	{
		if (cmd >= 0 && cmd < 4) {
			cfg = (cfg & 0xf) | cmd << 4;
		}
	}

	@Override
	public void initContainer(DataContainer cont) {
		TileContainer container = (TileContainer)cont;
		container.addItemSlot(new SlotItemHandler(inventory, 0, 8, 52));
		container.addItemSlot(new SlotItemHandler(inventory, 1, 26, 52));
		
		container.addItemSlot(new SlotTank(inventory, 2, 53, 34));
		container.addItemSlot(new SlotTank(inventory, 3, 107, 34));
		container.addItemSlot(new SlotTank(inventory, 4, 143, 34));
		
		container.addItemSlot(new SlotHolo(inventory, 5, 8, 16, true, false));
		container.addItemSlot(new SlotHolo(inventory, 6, 26, 16, true, false));
		container.addItemSlot(new SlotHolo(inventory, 7, 8, 34, true, false));
		container.addItemSlot(new SlotHolo(inventory, 8, 26, 34, true, false));
		
		container.addPlayerInventory(8, 86);
		
		container.addTankSlot(new TankSlot(tanks, 0, 98, 16, (byte)0x23));
		container.addTankSlot(new TankSlot(tanks, 1, 44, 16, (byte)0x23));
		container.addTankSlot(new TankSlot(tanks, 2, 134, 16, (byte)0x23));
	}

	@Override
	public boolean transferStack(ItemStack item, int s, TileContainer container) {
		if (s < container.invPlayerS) container.mergeItemStack(item, container.invPlayerS, container.invPlayerE, false);
		else container.mergeItemStack(item, 0, 2, false);
		return true;
	}

}
