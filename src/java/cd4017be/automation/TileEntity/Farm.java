/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cd4017be.automation.TileEntity;


import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import com.mojang.authlib.GameProfile;

import cd4017be.api.automation.AreaProtect;
import cd4017be.api.automation.IEnergy;
import cd4017be.api.automation.IOperatingArea;
import cd4017be.api.automation.PipeEnergy;
import cd4017be.automation.Config;
import cd4017be.automation.Objects;
import cd4017be.automation.Gui.InventoryPlacement;
import cd4017be.automation.Item.ItemItemUpgrade;
import cd4017be.automation.Item.ItemMachineSynchronizer;
import cd4017be.automation.Item.ItemPlacement;
import cd4017be.automation.Item.PipeUpgradeItem;
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
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.util.FakePlayerFactory;

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
        inventory = new Inventory(this, 38, new Component(20, 32, 1), new Component(8, 20, 0));
        energy = new PipeEnergy(Config.Umax[1], Config.Rcond[1]);
        netData = new TileEntityData(1, 0, 0, 0);
    }
    
    @Override
    public void onPlaced(EntityLivingBase entity, ItemStack item)  
    {
        lastUser = entity instanceof EntityPlayer ? ((EntityPlayer)entity).getGameProfile() : new GameProfile(new UUID(0, 0), entity.getName());
    }
    
    @Override
    public boolean onActivated(EntityPlayer player, EnumFacing s, float X, float Y, float Z) 
    {
        lastUser = player.getGameProfile();
        return super.onActivated(player, s, X, Y, Z);
    }
    
    @Override
    public void update() 
    {
    	super.update();
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
        BlockPos pos;
    	while (!worldObj.isBlockLoaded(pos = new BlockPos(px, py, pz)) || worldObj.isAirBlock(pos)) {
            if (py <= area[1]) return true;
            py--;
        }
        IBlockState state = worldObj.getBlockState(pos);
        Block block = state.getBlock();
        int m = block.getMetaFromState(state);
        byte Z;
        //harvesting
        if ((Z = this.doHarvest(pos, state, block, m)) >= 0) return Z != 0;
        //planting
        return this.doPlant(pos, state, block, m) != 0;
    }
    
    /** @return -1 = nothing, 0 = block, 1 = next */
    private byte doHarvest(BlockPos pos, IBlockState state, Block block, int m) {
    	boolean hasSlave = this.checkSlave();
        if (!(block.getMaterial().isToolNotRequired() || hasSlave) || harvestFilter == null) return -1;
        if (!this.ckeckFilter(harvestFilter, state, block, m)) return -1;
        if (hasSlave) return slave.remoteOperation(pos) ? (byte)1 : 0;
        if (invFull && invFull()) return 0;
        invFull = false;
        if (storage < Energy) return 0;
        if (!AreaProtect.instance.isOperationAllowed(lastUser.getName(), worldObj, px >> 4, pz >> 4)) return 1;
        storage -= Energy;
        worldObj.setBlockToAir(pos);
        List<ItemStack> list = block.getDrops(worldObj, pos, state, 0);
        for (ItemStack list1 : list) add(list1);
        return 1;
    }
    
    /** @return -1 = nothing, 0 = block, 1 = next */
    private byte doPlant(BlockPos pos, IBlockState state, Block block, int m) {
    	boolean air = worldObj.isAirBlock(pos.up());
        ArrayList<Object[]> list = new ArrayList<Object[]>();
        int n = 0;
        for (int i = 0; i < 8; i++) {
        	if (inventory.items[i] == null) continue;
        	Object plant = inventory.items[i].getItem() instanceof ItemBlock ? ((ItemBlock)inventory.items[i].getItem()).block : inventory.items[i].getItem();
        	if (air && plant instanceof IPlantable && block.canSustainPlant(worldObj, pos, EnumFacing.UP, (IPlantable)plant)) {
        		list.add(new Object[]{i, plant, n, n += inventory.items[i].stackSize});
        	} else if (plant instanceof ItemPlacement) {
        		InventoryPlacement inv = new InventoryPlacement(inventory.items[i]);
        		if (this.equalsBlock(inv.inventory[0], state, pos, m, !inv.useDamage(0))) {
        			list.add(new Object[]{i, inv, n, n += inventory.items[i].stackSize});
        		}
        	}
        }
        if (n == 0) return -1;
        if (storage < Energy) return 0;
        if (!AreaProtect.instance.isOperationAllowed(lastUser.getName(), worldObj, px >> 4, pz >> 4)) return 1;
        int o = random.nextInt(n);
        for (Object[] obj : list) {
        	if (o >= (Integer)obj[2] && o < (Integer)obj[3]) {
        		return this.plantItem(inventory.items[(Integer)obj[0]], obj[1], pos) ? (byte)1 : 0;
        	}
        }
    	return -1;
    }
    
    private boolean checkSlave()
    {
    	return slave != null && !((TileEntity)slave).isInvalid() && slave instanceof Miner && 
        		(slave.getSlave() == null || slave.getSlave().getSlave() == null);
    }
    
    private boolean plantItem(ItemStack item, Object plant, BlockPos pos)
    {
    	if (plant instanceof IPlantable) {
        	if (!remove(item, 1)) return true;
            worldObj.setBlockState(pos.up(), ((IPlantable)plant).getPlant(worldObj, pos.up()), 0x3);
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
            		items[i] = ItemPlacement.doPlacement(worldObj, player, items[i], pos, inv.getDir(i + 1), inv.Vxy[i + 1], inv.Vxy[i + 9], inv.sneak(i + 1), inv.useBlock);
        	}
        	int[] slots = inventory.componets[1].slots();
        	for (int i = 0; i < n; i++) 
        		if (items[i] != null)
        			this.putItemStack(items[i], inventory, -1, slots);
        	return true;
        }
    }
    
    private boolean ckeckFilter(PipeUpgradeItem blockFilter, IBlockState state, Block block, int m) {
    	if ((blockFilter.mode&64) != 0 && (worldObj.isBlockIndirectlyGettingPowered(getPos()) > 0 ^ (blockFilter.mode&128) == 0)) return false;
        ItemType filter = blockFilter.getFilter();
        List<ItemStack> list = block.getDrops(worldObj, pos, state, 0);
        if ((blockFilter.mode&2) != 0) list.add(new ItemStack(block));
        boolean harvest = false;
        int match;
        for (ItemStack item : list) {
            if ((match = filter.getMatch(item)) >= 0) {
            	if ((blockFilter.mode&32) != 0 && blockFilter.list[match].stackSize <= 16 && (blockFilter.list[match].stackSize & 15) != m)
            		continue;
            	harvest = true;
            	break;
            }
        }
        return harvest ^ (blockFilter.mode&1) != 0;
    }
    
    private boolean equalsBlock(ItemStack item, IBlockState state, BlockPos pos, int m, boolean drop)
    {
    	if (item == null) return false;
    	else if (item.getItem() instanceof ItemItemUpgrade) {
    		return this.ckeckFilter(PipeUpgradeItem.load(item.getTagCompound()), state, state.getBlock(), m);
    	} else if (drop) {
    		List<ItemStack> list = state.getBlock().getDrops(worldObj, pos, state, 0);
    		for (ItemStack stack : list)
    			if (Utils.itemsEqual(item, stack)) return true;
    		return false;
    	} else if (item.getItem() instanceof ItemBlock) {
    		ItemBlock ib = (ItemBlock)item.getItem();
    		return ib.block == state.getBlock() && ib.getMetadata(item.getItemDamage()) == m;
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
            worldObj.spawnEntityInWorld(new EntityItem(worldObj, pos.getX() + 0.5D, pos.getY() + 1.5D, pos.getZ() + 0.5D, item));
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
        this.worldObj.markBlockForUpdate(getPos());
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
        
        container.addEntitySlot(new SlotItemType(this, 36, 152, 16, new ItemStack(Objects.itemUpgrade)));
        
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
    public ItemStack removeStackFromSlot(int i) 
    {
        if (i < 8) return null;
        else return super.removeStackFromSlot(i);
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
			harvestFilter = item == null || !(item.getItem() instanceof ItemItemUpgrade) || item.getTagCompound() == null ? null : PipeUpgradeItem.load(item.getTagCompound());
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
			this.worldObj.markBlockForUpdate(getPos());
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
	public boolean remoteOperation(BlockPos pos) 
	{
		return true;
	}

	@Override
	public IOperatingArea getSlave() 
	{
		return slave;
	}
    
}
