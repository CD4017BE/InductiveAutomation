/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cd4017be.automation.TileEntity;

import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

import com.mojang.authlib.GameProfile;

import cd4017be.api.automation.AreaProtect;
import cd4017be.api.automation.IEnergy;
import cd4017be.api.automation.IOperatingArea;
import cd4017be.api.automation.PipeEnergy;
import cd4017be.automation.Config;
import cd4017be.automation.Gui.InventoryPlacement;
import cd4017be.automation.Item.ItemItemUpgrade;
import cd4017be.automation.Item.ItemMachineSynchronizer;
import cd4017be.automation.Item.ItemPlacement;
import cd4017be.automation.Item.PipeUpgradeItem;
import cd4017be.lib.BlockItemRegistry;
import cd4017be.lib.TileContainer;
import cd4017be.lib.TileEntityData;
import cd4017be.lib.templates.AutomatedTile;
import cd4017be.lib.templates.Inventory.Component;
import cd4017be.lib.templates.SlotHolo;
import cd4017be.lib.templates.SlotItemType;
import cd4017be.lib.util.Utils;
import cd4017be.lib.util.Utils.ItemType;
import cd4017be.lib.templates.Inventory;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.common.util.ForgeDirection;

/**
 *
 * @author CD4017BE
 */
public class Farm extends AutomatedTile implements ISidedInventory, IOperatingArea, IEnergy
{
    public static float Energy = 25000F;
    private static final float resistor = 20F;
    private static final float eScale = (float)Math.sqrt(1D - 1D / resistor);
    private static final GameProfile defaultUser = new GameProfile(new UUID(0, 0), "Automation-Farm");
    private int[] area = new int[6];
    private int px;
    private int py;
    private int pz;
    private float storage;
    private final Random random = new Random();
    private boolean invFull;
    private PipeUpgradeItem harvestFilter;
    private IOperatingArea slave = null;
    private GameProfile lastUser = defaultUser;

    public Farm()
    {
        inventory = new Inventory(this, 38, new Component(20, 32, 1), new Component(8, 20, 0)).setInvName("Automatic Farm");
        energy = new PipeEnergy(Config.Umax[1], Config.Rcond[1]);
        netData = new TileEntityData(1, 0, 0, 0);
    }
    
    @Override
    public void onPlaced(EntityLivingBase entity, ItemStack item)  
    {
        lastUser = entity instanceof EntityPlayer ? ((EntityPlayer)entity).getGameProfile() : new GameProfile(new UUID(0, 0), entity.getCommandSenderName());
    }
    
    @Override
    public boolean onActivated(EntityPlayer player, int s, float X, float Y, float Z) 
    {
        lastUser = player.getGameProfile();
        return super.onActivated(player, s, X, Y, Z);
    }
    
    @Override
    public void updateEntity() 
    {
        super.updateEntity();
        if (worldObj.isRemote) return;
        if (slave == null || ((TileEntity)slave).isInvalid()) slave = ItemMachineSynchronizer.getLink(inventory.items[37], this);
        if (storage < Energy)
        {
            storage += energy.getEnergy(0, resistor);
            energy.Ucap *= eScale;
        }
        if (checkBlock())
        {
            py--;
            if (py < area[1] || py >= area[4]) {
                py = area[4] - 1;
                px++;
            }
            if (px >= area[3] || px < area[0])
            {
                px = area[0];
                pz++;
            }
            if (pz >= area[5] || pz < area[2])
            {
                pz = area[2];
            }
        }
    }
    
    private boolean checkBlock()
    {
        while (!worldObj.blockExists(px, py, pz) || worldObj.isAirBlock(px, py, pz)) {
            if (py <= area[1]) return true;
            py--;
        }
        Block b = worldObj.getBlock(px, py, pz);
        int m = worldObj.getBlockMetadata(px, py, pz);
        Block block = b;
        if (block == null) return true;
        if (worldObj.isAirBlock(px, py + 1, pz))
        {
            ArrayList<Object[]> list = new ArrayList<Object[]>();
            int n = 0;
            for (int i = 0; i < 8; i++)
            {
                if (inventory.items[i] == null) continue;
                Object plant = inventory.items[i].getItem() instanceof ItemBlock ? ((ItemBlock)inventory.items[i].getItem()).field_150939_a : inventory.items[i].getItem();
                if (plant instanceof IPlantable && block.canSustainPlant(worldObj, px, py, pz, ForgeDirection.UP, (IPlantable)plant)) {
                    list.add(new Object[]{i, plant, n, n += inventory.items[i].stackSize});
                } else if (plant instanceof ItemPlacement) {
                	InventoryPlacement inv = new InventoryPlacement(inventory.items[i]);
                	if (this.equalsBlock(inv.inventory[0], b, px, py, pz, m, !inv.useDamage(0))) {
                		list.add(new Object[]{i, inv, n, n += inventory.items[i].stackSize});
                	}
                }
            }
            if (n > 0) {
            	if (storage < Energy) return false;
            	if (!AreaProtect.instance.isOperationAllowed(lastUser.getName(), worldObj, px >> 4, pz >> 4)) return true;
            	int o = random.nextInt(n);
                for (Object[] obj : list) {
                    if (o >= (Integer)obj[2] && o < (Integer)obj[3]) {
                        return this.plantItem(inventory.items[(Integer)obj[0]], obj[1], px, py, pz);
                    }
                }
            }
        }
        boolean hasSlave = this.checkSlave();
        if (!(block.getMaterial().isToolNotRequired() || hasSlave) || harvestFilter == null) return true;
        if ((harvestFilter.mode&64) != 0 && (worldObj.getBlockPowerInput(xCoord, yCoord, zCoord) > 0 ^ (harvestFilter.mode&128) == 0)) return true;
        ItemType filter = harvestFilter.getFilter();
        ArrayList<ItemStack> list = block.getDrops(worldObj, px, py, pz, m, 0);
        if ((harvestFilter.mode&2) != 0) list.add(new ItemStack(block));
        boolean harvest = false;
        int match;
        for (ItemStack item : list) {
            if ((match = filter.getMatch(item)) >= 0) {
            	if ((harvestFilter.mode&32) != 0 && harvestFilter.list[match].stackSize <= 16 && (harvestFilter.list[match].stackSize & 15) != m)
            		continue;
            	harvest = true;
            	break;
            }
        }
        if (harvest ^ (harvestFilter.mode&1) != 0) {
            if (hasSlave) return slave.remoteOperation(px, py, pz);
        	if (invFull && invFull()) return false;
            invFull = false;
            if (storage >= Energy) {
            	if (!AreaProtect.instance.isOperationAllowed(lastUser.getName(), worldObj, px >> 4, pz >> 4)) return true;
                storage -= Energy;
                worldObj.setBlockToAir(px, py, pz);
                list = block.getDrops(worldObj, px, py, pz, m, 0);
                for (ItemStack list1 : list) add(list1);
                return true;
            } else return false;
        }
        return true;
    }
    
    private boolean checkSlave()
    {
    	return slave != null && !((TileEntity)slave).isInvalid() && slave instanceof Miner && 
        		(slave.getSlave() == null || slave.getSlave().getSlave() == null);
    }
    
    private boolean plantItem(ItemStack item, Object plant, int x, int y, int z)
    {
    	if (plant instanceof IPlantable) {
        	if (!remove(item, 1)) return true;
            worldObj.setBlock(px, py + 1, pz, ((IPlantable)plant).getPlant(worldObj, x, y + 1, z), item.getItem().getMetadata(item.getItemDamage()), 0x3);
            storage -= Energy;
            return true;
        } else {
        	InventoryPlacement inv = (InventoryPlacement)plant;
        	ItemStack[] items = new ItemStack[7];
        	boolean canDo = true;
        	int n;
        	for (n = 0; n < inv.inventory.length - 1 && inv.inventory[n + 1] != null; n++) {
        		items[n] = this.remove(inv.inventory[n + 1], inv.useDamage(n + 1));
        		if (items[n] == null) {
        			canDo = false;
        			break;
        		}
        	}
        	if (n == 0) return true;
        	if (canDo) {
        		storage -= Energy;
        		EntityPlayer player = FakePlayerFactory.get((WorldServer)worldObj, lastUser);
            	for (int i = 0; i < n; i++)
            		items[i] = ItemPlacement.doPlacement(worldObj, player, items[i], x, y, z, inv.getDir(i + 1), inv.Vxy[i + 1], inv.Vxy[i + 9], inv.sneak(i + 1), inv.useBlock);
        	}
        	int[] slots = inventory.componets[1].slots();
        	for (int i = 0; i < n; i++) 
        		if (items[i] != null)
        			this.putItemStack(items[i], inventory, -1, slots);
        	return true;
        }
    }
    
    private boolean equalsBlock(ItemStack item, Block block, int x, int y, int z, int m, boolean drop)
    {
    	if (item == null) return false;
    	else if (drop) {
    		ArrayList<ItemStack> list = block.getDrops(worldObj, x, y, z, m, 0);
    		for (ItemStack stack : list)
    			if (Utils.itemsEqual(item, stack)) return true;
    		return false;
    	} else if (item.getItem() instanceof ItemBlock) {
    		ItemBlock ib = (ItemBlock)item.getItem();
    		return ib.field_150939_a == block && ib.getMetadata(item.getItemDamage()) == m;
    	} else return false;
    }
    
    private boolean invFull()
    {
        for (int i = 20; i < 32; i++)
            if (inventory.items[i] == null) return false;
        return true;
    }
    
    private ItemStack remove(ItemStack item, boolean meta)
    {
        for (int i = 8; i < 20; i++) {
        	if (inventory.items[i] != null && inventory.items[i].getItem() == item.getItem() && (!meta || inventory.items[i].getItemDamage() == item.getItemDamage()))
        		return this.decrStackSize(i, 1);
	    }
	    return null;
    }
    
    private boolean remove(ItemStack item, int n)
    {
        for (int i = 8; i < 20 && n > 0; i++)
        {
            if (inventory.items[i] != null && inventory.items[i].isItemEqual(item))
            {
                if (inventory.items[i].stackSize > n)
                {
                    inventory.items[i].stackSize -= n;
                    return true;
                } else
                {
                    n -= inventory.items[i].stackSize;
                    inventory.items[i] = null;
                }
            }
        }
        return n <= 0;
    }
    
    private void add(ItemStack item)
    {
        if (item == null) return;
        int p = 20;
        for (int i = 0; i < 4; i++)
        {
            if (inventory.items[i] != null && Utils.itemsEqual(inventory.items[i], item))
            {
                p = 8;
                break;
            }
        }
        int[] s = new int[32 - p];
        for (int i = 0; i < s.length; i++) s[i] = p + i;
        item = this.putItemStack(item, this, -1, s);
        if (item != null)
        {
            worldObj.spawnEntityInWorld(new EntityItem(worldObj, xCoord + 0.5D, yCoord + 1.5D, zCoord + 0.5D, item));
            invFull = true;
        }
    }

    @Override
    public int[] getOperatingArea() 
    {
        return area;
    }

    @Override
    public void updateArea(int[] area) 
    {
        boolean b = false;
        for (int i = 0; i < this.area.length && !b;i++){b = area[i] != this.area[i];}
        if (b) {
            this.area = area;
        }
        this.worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) 
    {
        super.readFromNBT(nbt);
        area = nbt.getIntArray("area");
        if (area.length != 6) area = new int[6];
        px = nbt.getInteger("px");
        py = nbt.getInteger("py");
        pz = nbt.getInteger("pz");
        storage = nbt.getFloat("storage");
        invFull = invFull();
        try {lastUser = new GameProfile(new UUID(nbt.getLong("lastUserID0"), nbt.getLong("lastUserID1")), nbt.getString("lastUser"));
        } catch (Exception e) {lastUser = defaultUser;}
        this.setInventorySlotContents(36, inventory.items[36]);
        this.onUpgradeChange(3);
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) 
    {
        super.writeToNBT(nbt);
        nbt.setIntArray("area", area);
        nbt.setInteger("px", px);
        nbt.setInteger("py", py);
        nbt.setInteger("pz", pz);
        nbt.setFloat("storage", storage);
        nbt.setString("lastUser", lastUser.getName());
        nbt.setLong("lastUserID0", lastUser.getId().getMostSignificantBits());
        nbt.setLong("lastUserID1", lastUser.getId().getLeastSignificantBits());
    }
        
    @Override
    public void initContainer(TileContainer container)
    {
    	for (int i = 0; i < 8; i++)
    		container.addEntitySlot(new SlotHolo(this, i, 8 + 18 * i, 16, false, true));
        
        for (int k = 0; k < 2; k++)
            for (int j = 0; j < 3; j++)
                for (int i = 0; i < 4; i++)
                    container.addEntitySlot(new Slot(this, 8 + i + j * 4 + k * 12, 8 + 18 * (i + 5 * k), 34 + 18 * j));
        
        container.addEntitySlot(new SlotItemType(this, 36, 152, 16, BlockItemRegistry.stack("item.itemUpgrade", 1)));
        
        container.addPlayerInventory(8, 104);
    }
    
    @Override
    public int[] stackTransferTarget(ItemStack item, int s, TileContainer container) 
    {
        int[] inv = container.getPlayerInv();
        if (s < inv[0]) return inv;
        else if (item.getItem() instanceof ItemItemUpgrade) return new int[]{32, 33};
        else {
            int n = 20;
            for (int i = 0; i < 8; i++) {
                ItemStack st = this.getStackInSlot(i);
                if (st != null && st.isItemEqual(item)) {
                    n = 8;
                    break;
                }
            }
            return new int[]{n, 32};
        }
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int i) 
    {
        if (i < 8) return null;
        else return super.getStackInSlotOnClosing(i);
    }
    
	@Override
	public ItemStack decrStackSize(int i, int j) 
	{
		ItemStack item = super.decrStackSize(i, j);
		if (i == 36 && inventory.items[i] == null) harvestFilter = null;
		return item;
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack item) 
	{
		super.setInventorySlotContents(i, item);
		if (i == 36)
			harvestFilter = item == null || !(item.getItem() instanceof ItemItemUpgrade) || item.stackTagCompound == null ? null : PipeUpgradeItem.load(item.stackTagCompound);
	}

	@Override
	public int[] getUpgradeSlots() 
	{
		return new int[]{32, 33, 34, 35, 37};
	}
	
	@Override
	public int[] getBaseDimensions() 
	{
		return new int[]{12, 16, 12, 8, Config.Umax[1]};
	}

	@Override
	public void onUpgradeChange(int s) 
	{
		if (s == 0) {
			this.worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
			return;
		}
		if (s == 3) {
			double u = energy.Ucap;
			energy = new PipeEnergy(Handler.Umax(this), Config.Rcond[1]);
			energy.Ucap = u;
		} else if (s == 4) {
			slave = ItemMachineSynchronizer.getLink(inventory.items[37], this);
		} else {
			Handler.setCorrectArea(this, area, true);
		}
	}

	@Override
	public boolean remoteOperation(int x, int y, int z) 
	{
		return true;
	}

	@Override
	public IOperatingArea getSlave() 
	{
		return slave;
	}
    
}
