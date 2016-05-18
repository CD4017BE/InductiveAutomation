/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cd4017be.automation.TileEntity;

import net.minecraft.network.PacketBuffer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.Optional.Interface;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.Environment;
import li.cil.oc.api.network.Message;
import li.cil.oc.api.network.Node;
import cd4017be.api.automation.IEnergy;
import cd4017be.api.automation.IOperatingArea;
import cd4017be.api.automation.PipeEnergy;
import cd4017be.api.computers.ComputerAPI;
import cd4017be.automation.Config;
import cd4017be.automation.Item.ItemMachineSynchronizer;
import cd4017be.automation.Item.ItemMinerDrill;
import cd4017be.lib.TileContainer;
import cd4017be.lib.TileEntityData;
import cd4017be.lib.templates.AutomatedTile;
import cd4017be.lib.templates.Inventory;
import cd4017be.lib.templates.Inventory.Component;
import cd4017be.lib.util.CachedChunkProtection;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.IFluidBlock;

/**
 *
 * @author CD4017BE
 */
@Optional.InterfaceList(value = {@Interface(iface = "dan200.computercraft.api.peripheral.IPeripheral", modid = "ComputerCraft"), @Interface(iface = "li.cil.oc.api.network.Environment", modid = "OpenComputers")})
public class Miner extends AutomatedTile implements ISidedInventory, IEnergy, IOperatingArea, Environment //,IPeripheral //TODO reimplement
{
    public static float Energy = 40000F;
    private static final float resistor = 25F;
    private static final float eScale = (float)Math.sqrt(1D - 1D / resistor);
    private int[] area = new int[6];
    private int px;
    private int py;
    private int pz;
    private float storage;
    private float neededE;
    private IOperatingArea slave = null;
    
    private String lastUser = "";
	private CachedChunkProtection prot;
    
    public Miner()
    {
        inventory = new Inventory(this, 29, new Component(0, 6, -1), new Component(6, 24, 1));
        energy = new PipeEnergy(Config.Umax[1], Config.Rcond[1]);
        netData = new TileEntityData(1, 1, 0, 0);
    }
    
    @Override
    public void onPlaced(EntityLivingBase entity, ItemStack item)  
    {
        lastUser = entity.getName();
        prot = null;
    }
    
    @Override
    public boolean onActivated(EntityPlayer player, EnumFacing s, float X, float Y, float Z) 
    {
        lastUser = player.getName();
        prot = null;
        return super.onActivated(player, s, X, Y, Z);
    }
    
    @Override
    public void update() 
    {
    	super.update();
        if (worldObj.isRemote) return;
        if (slave == null || ((TileEntity)slave).isInvalid()) slave = ItemMachineSynchronizer.getLink(inventory.items[28], this);
        double e = energy.getEnergy(0, resistor);
        storage += e;
        storage -= ComputerAPI.update(this, node, storage * 0.5D);
        if (active())
        	for (int i = 0; i < Config.taskQueueSize; i++) {
        		if (breakBlock(new BlockPos(px, py, pz)))
        		{
        			py--;
        			if (py < area[1] || py >= area[4]) {
        				py = area[4] - 1;
        				px++;
        			}
        			if (px >= area[3] || px < area[0]) {
        				px = area[0];
        				pz++;
        			}
        			if (pz >= area[5] || pz < area[2]) {
        				pz = area[2];
        				px = area[0];
        				py = area[1];
        				netData.ints[0] = 0;
        			}
        		} else break;
        	}
        else if (worldObj.isBlockIndirectlyGettingPowered(getPos()) >= 8) netData.ints[0] = -1;
        else {
        	MineTask task;
        	synchronized(this.sheduledTasks) {
	        	while (!this.sheduledTasks.isEmpty()) {
	            	task = this.sheduledTasks.get(0);
	        		if (task.execute(this)) {
	            		this.sheduledTasks.remove(0);
	            	} else break;
	            }
        	}
        }
        if (storage < e + neededE) energy.Ucap *= eScale;
        else storage -= e;
        neededE = 0;
    }
    
    @Override
	public int redstoneLevel(int s, boolean str) 
    {
		return netData.ints[0] != 0 ? 0 : 7;
	}

	private boolean slaveOp(BlockPos pos)
    {
    	if (slave != null && !((TileEntity)slave).isInvalid() && (slave instanceof Pump || slave instanceof Builder) && (slave.getSlave() == null || !(slave.getSlave() instanceof Miner || slave.getSlave().getSlave() != null))) {
        	return slave.remoteOperation(pos);
        } else return true;
    }
    
    private boolean breakBlock(BlockPos pos)
    {
        if (!worldObj.isBlockLoaded(pos)) return false;
        if (prot == null || !prot.equalPos(pos)) prot = CachedChunkProtection.get(lastUser, worldObj, pos);
    	if (!prot.allow) return true;
        IBlockState state = worldObj.getBlockState(pos);
        Block block = state.getBlock();
        int m = block.getMetaFromState(state);
        if (!this.isHarvestable(block, pos)) return this.slaveOp(pos);
        float hardness = block.getBlockHardness(worldObj, pos);
        float eff = 0;
        int tool = -1;
        if (block.getMaterial().isToolNotRequired()) eff = 1F;
        else for (int i = 0; i < 6; i++)
        {
            if (inventory.items[i] == null) continue;
            Item item = inventory.items[i].getItem();
            if (item instanceof ItemMinerDrill && ((ItemMinerDrill)item).canHarvestBlock(state, m)) {
                if (eff == 0 || ((ItemMinerDrill)item).efficiency < eff)  {
                    eff = ((ItemMinerDrill)item).efficiency;
                    tool = i;
                }
            }
        }
        if (eff == 0) return true;
        Map<Integer, Integer> enchant = tool < 0 ? new LinkedHashMap<Integer, Integer>() : EnchantmentHelper.getEnchantments(inventory.items[tool]);
        Integer n = enchant.get(Enchantment.efficiency.effectId);
        if (n != null) eff *= 1F + 0.3F * (float)n;
        float e = hardness * Energy / eff;
        boolean silk;
        try {silk = enchant.containsKey(Enchantment.silkTouch.effectId) && block.canSilkHarvest(worldObj, pos, state, null);} catch (NullPointerException ex) {silk = false;}
        if (silk) e *= 2.5F;
        else {
        	n = enchant.get(Enchantment.fortune.effectId);
        	if (n != null) e *= 1F + (float)n * 0.6F;
        }
        if (storage < e) {
        	neededE = e;
        	return false;
        }
        List<ItemStack> list;
        if (silk) {
        	list = new ArrayList<ItemStack>();
        	list.add(new ItemStack(block, 1, block.getDamageValue(worldObj, pos)));
        } else {
        	list = block.getDrops(worldObj, pos, state, n == null ? 0 : n);
        }
        if (invFull(list.size())) return false;
        storage -= e;
        if (tool >= 0) {
        	n = enchant.get(Enchantment.unbreaking.effectId);
        	if (n == null || randomDamage(n, pos)) {
        		int d = inventory.items[tool].getItemDamage() + 1;
                if (d >= inventory.items[tool].getMaxDamage()) inventory.items[tool] = null;
                else inventory.items[tool].setItemDamage(d);
        	}
        }
        for (ItemStack item : list) add(item);
        worldObj.setBlockState(pos, Blocks.air.getDefaultState(), 2);
        return this.slaveOp(pos);
    }
    
    private boolean randomDamage(int unbr, BlockPos pos)
    {
    	if (area[3] == area[0] || area[4] == area[1] || area[5] == area[2]) return true;
    	int i = pos.getZ() % (area[5] - area[2]);
    	i *= 31; 
    	i += pos.getX() % (area[3] - area[0]);
    	i *= 31; 
    	i += pos.getY() % (area[4] - area[1]);
    	i *= 31;
    	return i % (unbr + 3) < 3;
    }
    
    private boolean isHarvestable(Block block, BlockPos pos)
    {
        if (block == null || block.getBlockHardness(worldObj, pos) < 0 || block instanceof BlockLiquid || block instanceof IFluidBlock) return false;
        else return block.getMaterial().isToolNotRequired() || block.getMaterial() == Material.rock || block.getMaterial() == Material.iron || block.getMaterial() == Material.anvil;
    }
    
    public boolean active()
    {
        return netData.ints[0] != 0;
    }
    
    private boolean invFull(int n)
    {
        if (n <= 0) return false;
        for (int i = 6; i < 24; i++)
            if (inventory.items[i] == null && --n <= 0) return false;
        return true;
    }
    
    private void add(ItemStack item)
    {
        int[] s = new int[18];
        for (int i = 0; i < s.length; i++) s[i] = i + 6;
        item = this.putItemStack(item, this, -1, s);
        if (item != null)
        {
            worldObj.spawnEntityInWorld(new EntityItem(worldObj, pos.getX() + 0.5D, pos.getY() + 1.5D, pos.getZ() + 0.5D, item));
        }
    }
    
    private void reset()
    {
        px = area[0];
        py = area[4] - 1;
        pz = area[2];
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
            reset();
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
        lastUser = nbt.getString("lastUser");
        prot = null;
        netData.ints[0] = nbt.getBoolean("run") ? -1 : 0;
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
        nbt.setString("lastUser", lastUser);
        nbt.setBoolean("run", netData.ints[0] != 0);
    }
        
    @Override
    public void initContainer(TileContainer container)
    {
        for (int i = 0; i < 2; i++)
        {
            for (int j = 0; j < 3; j++)
            {
                container.addEntitySlot(new Slot(this, i + j * 2, 134 + i * 18, 16 + j * 18));
            }
        }
        for (int i = 0; i < 6; i++)
        {
            for (int j = 0; j < 3; j++)
            {
                container.addEntitySlot(new Slot(this, 6 + i + j * 6, 8 + i * 18, 16 + j * 18));
            }
        }
        
        container.addPlayerInventory(8, 86);
    }
    
    @Override
    public int[] stackTransferTarget(ItemStack item, int s, TileContainer container) 
    {
        int[] pi = container.getPlayerInv();
        if (s < pi[0]) return pi;
        else {
            boolean b = item.getItem() instanceof ItemTool;
            return new int[]{b ? 0 : 6, b ? 6 : 24};
        }
    }

    @Override
    protected void customPlayerCommand(byte cmd, PacketBuffer dis, EntityPlayerMP player) throws IOException 
    {
        if (cmd == 0) netData.ints[0] = ~netData.ints[0];
        else if (cmd == 1) reset();
    }

    private static class MineTask {
    	public final int x, y, z;
    	public final Object src;
    	public final boolean evType;
    	public MineTask(Object computer, int x, int y, int z, boolean event) {
    		this.x = x;
    		this.y = y;
    		this.z = z;
    		this.src = computer;
    		this.evType = event;
    	}
    	
    	public boolean execute(Miner miner)
    	{
    		BlockPos pos = new BlockPos(x, y, z);
    		if (!miner.worldObj.isBlockLoaded(pos)) return false;
            Block block = miner.worldObj.getBlockState(pos).getBlock();
            boolean imp = block == null || block.isReplaceable(miner.worldObj, pos);
            if (imp || miner.breakBlock(pos)) {
            	if (evType) ComputerAPI.sendEvent(src, "mine_block", x - miner.px, y - miner.py, z - miner.pz, !imp);
        		else if (miner.sheduledTasks.size() == 1) ComputerAPI.sendEvent(src, "mine_done");
            	return true;
            } else return false;
    	}
    }
    
    @Override
	public int[] getUpgradeSlots() 
	{
		return new int[]{24, 25, 26, 27, 28};
	}
    
    @Override
	public int[] getBaseDimensions() 
	{
		return new int[]{16, 64, 16, 8, Config.Umax[1]};
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
			slave = ItemMachineSynchronizer.getLink(inventory.items[28], this);
		} else {
			Handler.setCorrectArea(this, area, true);
		}
	}

	@Override
	public boolean remoteOperation(BlockPos pos) 
	{
		if (pos.getX() < area[0] || pos.getY() < area[1] || pos.getZ() < area[2] || pos.getX() >= area[3] || pos.getY() >= area[4] || pos.getZ() >= area[5]) return true;
		return this.breakBlock(pos);
	}

	@Override
	public IOperatingArea getSlave() 
	{
		return slave;
	}
	
    private ArrayList<MineTask> sheduledTasks = new ArrayList<MineTask>();
    
	//ComputerCraft:
    /* TODO reimplement
    @Optional.Method(modid = "ComputerCraft")
    @Override
    public String getType() 
    {
        return "Automation-Miner";
    }

    @Optional.Method(modid = "ComputerCraft")
    @Override
    public String[] getMethodNames() 
    {
        return new String[]{"getAreaSize", "mineBlock", "stopMining", "isBlockAt", "clearQueue"};
    }

    @Optional.Method(modid = "ComputerCraft")
    @Override
    public Object[] callMethod(IComputerAccess computer, ILuaContext lua, int cmd, Object[] par) throws LuaException 
    {
        if (cmd == 0) {
            if (par != null && par.length > 0) throw new LuaException("This method does not take any parameters");
            return new Object[]{Double.valueOf(area[3] - area[0]), Double.valueOf(area[4] - area[1]), Double.valueOf(area[5] - area[2])};
        } else if (cmd == 1) {
            lastUser = "#Computer" + computer.getID();
            if (par == null || par.length < 3) throw new LuaException("min 3 parameters needed: (int x, int y, int z, bool event) event is optional");
            if (this.active()) throw new LuaException("Mining operation progress, must be stopped first!");
            int x = ((Double)par[0]).intValue() + area[0];
            int y = ((Double)par[1]).intValue() + area[1];
            int z = ((Double)par[2]).intValue() + area[2];
            if (x < area[0] || x >= area[3] || y < area[1] || y >= area[4] || z < area[2] || z >= area[5]) throw new LuaException("Position outside of operating area!");
            if (this.sheduledTasks.size() < Config.taskQueueSize) {
            	synchronized (this.sheduledTasks) {
            		this.sheduledTasks.add(new MineTask(par.length >= 4 ? computer : null, x, y, z, par[3] == Boolean.TRUE));
            	}
            	return new Object[]{Boolean.TRUE};
            } else return new Object[]{Boolean.FALSE};
        } else if (cmd == 2) {
            if (par != null && par.length > 0) throw new LuaException("This method does not take any parameters");
            netData.ints[0] = 0;
            return null;
        } else if (cmd == 3) {
            if (par == null || par.length != 3) throw new LuaException("3 parameters needed: (int x, int y, int z)");
            int x = ((Double)par[0]).intValue() + area[0];
            int y = ((Double)par[1]).intValue() + area[1];
            int z = ((Double)par[2]).intValue() + area[2];
            Block block = worldObj.getBlock(x, y, z);
            if (block != null && !block.isReplaceable(worldObj, x, y, z)) return new Object[]{Boolean.TRUE};
            else return new Object[]{Boolean.FALSE};
        } else if (cmd == 4) {
        	int n = this.sheduledTasks.size();
        	this.sheduledTasks.clear();
        	return new Object[]{Double.valueOf(n)};
        } else return null;
    }

    @Optional.Method(modid = "ComputerCraft")
    @Override
    public void attach(IComputerAccess computer) {}

    @Optional.Method(modid = "ComputerCraft")
    @Override
    public void detach(IComputerAccess computer) {}

    @Optional.Method(modid = "ComputerCraft")
    @Override
    public boolean equals(IPeripheral peripheral) 
    {
        return peripheral.hashCode() == this.hashCode();
    }

*/

	//OpenComputers:
	
    private Object node = ComputerAPI.newOCnode(this, "Automation-Miner", true);
    
    @Optional.Method(modid = "OpenComputers")
	@Override
	public Node node() 
	{
		return (Node)node;
	}

	@Override
	public void invalidate() 
	{
		super.invalidate();
		ComputerAPI.removeOCnode(node);
	}

	@Override
	public void onChunkUnload() 
	{
		super.onChunkUnload();
		ComputerAPI.removeOCnode(node);
	}

	@Optional.Method(modid = "OpenComputers")
	@Override
	public void onConnect(Node node) {}

	@Optional.Method(modid = "OpenComputers")
	@Override
	public void onDisconnect(Node node) {}

	@Optional.Method(modid = "OpenComputers")
	@Override
	public void onMessage(Message message) {}
    
	@Optional.Method(modid = "OpenComputers")
	@Callback(doc = "function():int{sizeX, sizeY, sizeZ, baseX, baseY, baseZ} --returns the operating area size and position", direct = true)
	public Object[] getArea(Context cont, Arguments args) 
	{
		return new Object[]{area[3] - area[0], area[4] - area[1], area[5] - area[2], area[0], area[1], area[2]};
	}
	
	@Optional.Method(modid = "OpenComputers")
	@Callback(doc = "function():bool --returns true if a user initiated mining process is running, in this case computer controlled mining is not allowed", direct = true)
	public Object[] isMining(Context cont, Arguments args)
	{
		return new Object[]{netData.ints[0] != 0};
	}
	
	@Optional.Method(modid = "OpenComputers")
	@Callback(doc = "function():int --returns the amount of sheduled unfinished block break tasks and cancels them all", direct = true)
	public Object[] clearQueue(Context cont, Arguments args)
	{
		int n;
		synchronized(this.sheduledTasks) {
			n = this.sheduledTasks.size();
			this.sheduledTasks.clear();
		}
		return new Object[]{n};
	}
	
	@Optional.Method(modid = "OpenComputers")
	@Callback(doc = "function(x:int, y:int, z:int):bool --returns true if there is a not replaceable block at given operating-area-relative position", direct = true)
	public Object[] isBlockAt(Context cont, Arguments args)
	{
		BlockPos pos = new BlockPos(args.checkInteger(0) + area[0], args.checkInteger(1) + area[1], args.checkInteger(2) + area[2]);
        Block block = worldObj.getBlockState(pos).getBlock();
        if (block != null && !block.isReplaceable(worldObj, pos)) return new Object[]{Boolean.TRUE};
        else return new Object[]{Boolean.FALSE};
	}
	
	@Optional.Method(modid = "OpenComputers")
	@Callback(doc = "function(x:int, y:int, z:int[, notify:bool]):bool --will add the block at given position to the processing queue or returns false if queue is full. if notify is given, the caller will receive a \"mine_block\"{x:int, y:int, z:int, success:bool} event when executed with true or a \"mine_done\" event when processing queue finished with false", direct = true)
	public Object[] mineBlock(Context cont, Arguments args) throws Exception
	{
		if (this.active()) throw new Exception("Manual mining operation in progress, must be stopped first!");
        int x = args.checkInteger(0) + area[0];
        int y = args.checkInteger(1) + area[1];
        int z = args.checkInteger(2) + area[2];
        if (x < area[0] || x >= area[3] || y < area[1] || y >= area[4] || z < area[2] || z >= area[5]) throw new Exception("Position outside of operating area!");
        if (this.sheduledTasks.size() < Config.taskQueueSize) {
        	synchronized (this.sheduledTasks) {
        		this.sheduledTasks.add(new MineTask(args.isBoolean(3) ? cont : null, x, y, z, args.checkBoolean(3)));
        	}
        	return new Object[]{Boolean.TRUE};
        } else return new Object[]{Boolean.FALSE};
	}
    
}
