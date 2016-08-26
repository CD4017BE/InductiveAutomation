/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cd4017be.automation.TileEntity;

import net.minecraft.network.PacketBuffer;

import java.io.IOException;
import java.util.HashMap;

import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.Optional.Interface;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.Environment;
import li.cil.oc.api.network.Message;
import li.cil.oc.api.network.Node;
import cd4017be.api.automation.IEnergy;
import cd4017be.api.automation.PipeEnergy;
import cd4017be.api.circuits.IRedstone1bit;
import cd4017be.api.circuits.IRedstone8bit;
import cd4017be.api.computers.ComputerAPI;
import cd4017be.api.energy.EnergyAPI;
import cd4017be.api.energy.EnergyAPI.IEnergyAccess;
import cd4017be.automation.Objects;
import cd4017be.automation.Item.PipeUpgradeFluid;
import cd4017be.automation.Item.PipeUpgradeItem;
import cd4017be.lib.TileEntityData;
import cd4017be.lib.Gui.SlotItemType;
import cd4017be.lib.Gui.TileContainer;
import cd4017be.lib.templates.AutomatedTile;
import cd4017be.lib.templates.IAutomatedInv;
import cd4017be.lib.templates.Inventory;
import cd4017be.lib.util.Utils;
import cd4017be.lib.util.Utils.ItemType;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

/**
 *
 * @author CD4017BE
 */
@Optional.InterfaceList(value = {@Interface(iface = "dan200.computercraft.api.peripheral.IPeripheral", modid = "ComputerCraft"), @Interface(iface = "li.cil.oc.api.network.Environment", modid = "OpenComputers")})
public class Detector extends AutomatedTile implements IAutomatedInv, IRedstone8bit, IRedstone1bit, Environment //,IPeripheral //TODO reimplement
{
	private final Object[] filter = new Object[6];
	
    public Detector()
    {
        inventory = new Inventory(this, 6);
        /**
         * long: states, config
         * int: ref 0-5 +/-
         */
        netData = new TileEntityData(2, 12, 0, 0);
    }

    @Override
    public void update() 
    {
    	super.update();
        if (worldObj.isRemote) return;
        ComputerAPI.update(this, node, 0);
        if (netData.longs[1] == 0) return;
        int s = this.getOrientation();
        TileEntity te = Utils.getTileOnSide(this, (byte)s);
        IInventory inv = te != null && te instanceof IInventory ? (IInventory)te : null;
        IFluidHandler tank = te != null && te instanceof IFluidHandler ? (IFluidHandler)te : null;
        for (int i = 0; i < 6; i++) {
            byte m = this.getConfig(i);
            byte d = this.getConfig(i + 8);
            byte os = this.getState(i);
            byte ns;
            float v;
            m &= 3;
            int high = netData.ints[i * 2], low = netData.ints[i * 2 + 1], max = Math.max(low, high);
            if (d >= 6) v = 0;
            else if (m == 1) {
                v = this.checkVoltage(te, d);
            } else if (m == 2) {
                v = this.checkInventory(inv, d, max, filter[i] instanceof PipeUpgradeItem ? (PipeUpgradeItem)filter[i] : null);
            } else if (m == 3) {
                v = this.checkTank(tank, d, max, filter[i] instanceof PipeUpgradeFluid ? (PipeUpgradeFluid)filter[i] : null);
            } else v = Float.NaN;
            
            if (Float.isNaN(v)) ns = 0; 
            else if (high == low) ns = v <= low ? 0 : (byte)255;
            else {
            	v -= low;
            	v *= 255F / (float)(high - low);
            	ns = v > 255F ? (byte)255 : v < 0 ? 0 : (byte)Math.round(v);
            }
            
            this.setState(i, ns);
            if (os != ns)
                worldObj.notifyBlockOfStateChange(pos.offset(EnumFacing.VALUES[i]), this.getBlockType());
        }
    }
    
    private float checkVoltage(TileEntity te, int s)
    {
        if (te == null) return 0;
        IEnergyAccess energy = EnergyAPI.get(te);
        return (float)(energy.getStorage(s) / 1000D);
    }
    
    private float checkInventory(IInventory inv, int s, int max, PipeUpgradeItem type)
    {
        if (inv == null) return 0;
        ItemType filter = type == null ? new ItemType() : type.getFilter();
        boolean neg = type == null || (type.mode & 1) != 0;
        int[] slots = Inventory.getSlots(inv, s);
        int n = 0;
        for (int slot : slots) {
            ItemStack item = inv.getStackInSlot(slot);
            if (filter.types == null || filter.types.length == 0) {
            	n += item == null ? neg ? 0 : 1 : neg ? item.stackSize : 0;
            } else if (item != null && (filter.matches(item) ^ neg)) {
            	n += item.stackSize;
            }
            if (n >= max) break;
        }
        return n;
    }
    
    private float checkTank(IFluidHandler tank, int s, int max, PipeUpgradeFluid type)
    {
        if (tank == null) return 0;
        FluidTankInfo[] data = tank.getTankInfo(EnumFacing.VALUES[s]);
        if (data == null) return 0;
        boolean neg = type == null || (type.mode & 1) != 0;
        if (type == null) type = new PipeUpgradeFluid();
        int n = 0;
        for (FluidTankInfo info : data) {
            if (type.list.length == 0) {
            	n += info.fluid == null ? neg ? 0 : info.capacity : neg ? info.fluid.amount : info.capacity - info.fluid.amount;
            } else if (info.fluid != null) {
            	boolean match = false;
            	for (FluidStack fluid : type.list)
            		if (info.fluid.isFluidEqual(fluid)) {
            			match = true;
            			break;
            		}
            	if (match ^ neg) n += info.fluid.amount;
            }
        	if (n >= max) break;
        }
        return n;
    }
    
    public byte getConfig(int s)
    {
        return (byte)(netData.longs[1] >> (s * 4) & 0xfL);
    }
    
    public void setConfig(int s, byte v)
    {
        netData.longs[1] &= ~(0xfL << (s * 4));
        netData.longs[1] |= (long)(v & 0xf) << (s * 4);
    }
    
    public byte getState(int s)
    {
        return (byte)(netData.longs[0] >> (s * 8) & 0xffL);
    }
    
    public void setState(int s, byte v)
    {
    	netData.longs[0] &= ~(0xffL << (s * 8));
        netData.longs[0] |= (long)(v & 0xff) << (s * 8);
    }
    
    @Override
    protected void customPlayerCommand(byte cmd, PacketBuffer dis, EntityPlayerMP player) throws IOException 
    {
        if (cmd == 0) {
        	byte s = dis.readByte();
            if (s >= 0 && s < 12) netData.ints[s] = dis.readInt();
        } else if (cmd == 1) {
        	byte s = dis.readByte();
            if (s >= 0 && s < 6) this.setConfig(s, dis.readByte());
        } else if (cmd == 2) {
            byte s = dis.readByte();
            if (s >= 0 && s < 6) this.setConfig(s + 8, dis.readByte());
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) 
    {
        if (node != null) ComputerAPI.saveNode(node, nbt);
        nbt.setIntArray("data", netData.ints);
        nbt.setLong("state", netData.longs[0]);
        nbt.setLong("cfg", netData.longs[1]);
        return super.writeToNBT(nbt);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) 
    {
        super.readFromNBT(nbt);
        if (node != null) ComputerAPI.readNode(node, nbt);
        for (int i = 0; i < inventory.items.length; i++) this.slotChange(null, inventory.items[i], i);
        int[] data = nbt.getIntArray("data");
        System.arraycopy(data, 0, netData.ints, 0, Math.min(data.length, netData.ints.length));
        netData.longs[0] = nbt.getLong("state");
        netData.longs[1] = nbt.getLong("cfg");
    }
    
    @Override
    public void slotChange(ItemStack oldItem, ItemStack newItem, int i) 
    {
    	if (newItem != null && newItem.getItem() != Objects.itemUpgrade && newItem.getItem() != Objects.fluidUpgrade) newItem = null; //TODO remove later
    	if (newItem == null || newItem.getTagCompound() == null) filter[i] = null;
        else filter[i] = newItem.getItem() == Objects.itemUpgrade ? PipeUpgradeItem.load(newItem.getTagCompound()) : newItem.getItem() == Objects.fluidUpgrade ? PipeUpgradeFluid.load(newItem.getTagCompound()) : null;
    }
    
    @Override
	public boolean canInsert(ItemStack item, int cmp, int i) {return true;}

	@Override
	public boolean canExtract(ItemStack item, int cmp, int i) {return true;}

	@Override
	public boolean isValid(ItemStack item, int cmp, int i) {return true;}
    
	@Override
    public void initContainer(TileContainer container)
    {
        for (int i = 0; i < 6; i++) {
            container.addItemSlot(new SlotItemType(this, i, 44, 16 + i * 18, new ItemStack(Objects.itemUpgrade), new ItemStack(Objects.fluidUpgrade)));
        }
        
        container.addPlayerInventory(8, 140);
    }

    @Override
    public byte getValue(int s) 
    {
        return this.getState(s);
    }

    @Override
    public byte getDirection(int s) 
    {
        return 1;
    }

    @Override
    public void setValue(int s, byte v, int recursion) {}
    
	@Override
	public byte getBitValue(int s) {
		return (byte)(this.getState(s) >> 4 & 0xf);
	}

	@Override
	public byte getBitDirection(int s) {
		return 1;
	}

	@Override
	public void setBitValue(int s, byte state, int recursion) {}
	
	@Override
    public int redstoneLevel(int s, boolean str) 
    {
        return !str ? this.getState(s) >> 4 & 0xf : 0;
    }

    //ComputerCraft:
    /* TODO reimplement
    @Optional.Method(modid = "ComputerCraft")
    @Override
    public String getType() 
    {
        return "Automation-Detector";
    }

    @Optional.Method(modid = "ComputerCraft")
    @Override
    public String[] getMethodNames() 
    {
        return new String[]{"getVoltage", "getInventory", "getFluidTank", "getEnergy"};
    }

    @Optional.Method(modid = "ComputerCraft")
    @Override
    public Object[] callMethod(IComputerAccess computer, ILuaContext lua, int cmd, Object[] par) throws LuaException 
    {
        byte s = (byte)worldObj.getBlockMetadata(getPos());
        if (cmd == 0) {
            if (par.length != 1 || !(par[0] instanceof Double)) throw new LuaException("missing parameters [Number Ucur, Number Umax = getVoltage(Number side)]");
            byte d = ((Double)par[0]).byteValue();
            if (d < 0 || d >= 6) throw new LuaException("parameter <side> out of range [0-6]");
            TileEntity te = Utils.getTileOnSide(this, s);
            if (te != null && te instanceof IEnergy) {
                PipeEnergy e = ((IEnergy)te).getEnergy(d);
                if (e != null) return new Object[]{Double.valueOf(e.Ucap), Double.valueOf(e.Umax)};
            }
            return new Object[]{Double.valueOf(0), Double.valueOf(0)};
        } else if (cmd == 1) {
            if (par.length != 1 || !(par[0] instanceof Double)) throw new LuaException("missing parameters [Table slots = getInventory(Number side)]");
            byte d = ((Double)par[0]).byteValue();
            if (d < 0 || d >= 6) throw new LuaException("parameter <side> out of range [0-6]");
            TileEntity te = Utils.getTileOnSide(this, s);
            if (te != null && te instanceof IInventory) {
                IInventory inv = (IInventory)te;
                int[] slots;
                if (inv instanceof ISidedInventory) {
                    slots = ((ISidedInventory)inv).getSlotsForFace(d);
                } else {
                    slots = new int[inv.getSizeInventory()];
                    for (int i = 0; i < slots.length; i++) slots[i] = i;
                }
                HashMap<Integer, HashMap<String, Integer>> list = new HashMap<Integer, HashMap<String, Integer>>();
                for (int i : slots) {
                    ItemStack item = inv.getStackInSlot(i);
                    if (item != null) {
                        HashMap<String, Integer> obj = new HashMap<String, Integer>();
                        obj.put("id", Item.getIdFromItem(item.getItem()));
                        obj.put("dmg", item.getItemDamage());
                        obj.put("am", item.stackSize);
                        list.put(i, obj);
                    }
                }
                return new Object[]{list};
            } else return null;
        } else if (cmd == 2) {
            if (par.length != 1 || !(par[0] instanceof Double)) throw new LuaException("missing parameters [Table fluids = getFluidTank(Number side)]");
            byte d = ((Double)par[0]).byteValue();
            if (d < 0 || d >= 6) throw new LuaException("parameter <side> out of range [0-6]");
            TileEntity te = Utils.getTileOnSide(this, s);
            if (te instanceof IFluidHandler) {
                IFluidHandler flh = (IFluidHandler)te;
                FluidTankInfo[] info = flh.getTankInfo(EnumFacing.VALUES[d]);
                HashMap<Integer, HashMap<String, Integer>> list = new HashMap<Integer, HashMap<String, Integer>>();
                if (info != null)
                    for (int i = 0; i < info.length; i++) {
                        HashMap<String, Integer> obj = new HashMap<String, Integer>();
                        obj.put("cap", info[i].capacity);
                        FluidStack fluid = info[i].fluid;
                        if (fluid != null) {
                            obj.put("am", fluid.amount);
                            obj.put("id", fluid.fluidID);
                        } else {
                            obj.put("am", 0);
                            obj.put("id", 0);
                        }
                        list.put(i, obj);
                    }
                return new Object[]{list};
            } else return null;
        } else if (cmd == 3) {
            if (par.length != 1 || !(par[0] instanceof Double)) throw new LuaException("missing parameters [Number E, Number cap = getEnergy(Number side)]");
            byte d = ((Double)par[0]).byteValue();
            if (d < 0 || d >= 6) throw new LuaException("parameter <side> out of range [0-6]");
            TileEntity te = Utils.getTileOnSide(this, s);
            IEnergyAccess energy = EnergyAPI.getAccess(te);
            return new Object[]{Double.valueOf(energy.getStorage(d) / 1000F), Double.valueOf(energy.getCapacity(d) / 1000F)};
        } else return null;
    }

    @Optional.Method(modid = "ComputerCraft")
    @Override
    public void attach(IComputerAccess paramIComputerAccess) {}

    @Optional.Method(modid = "ComputerCraft")
    @Override
    public void detach(IComputerAccess paramIComputerAccess) {}

    @Optional.Method(modid = "ComputerCraft")
    @Override
    public boolean equals(IPeripheral peripheral) 
    {
        return this.hashCode() == peripheral.hashCode();
    }

*/

    //OpenComputers:
    
    private Object node = ComputerAPI.newOCnode(this, "Automation-Detector", false);
    
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
	@Callback(doc = "" ,direct = true)
	public Object[] getVoltage(Context cont, Arguments args) throws Exception
	{
		byte s = (byte)this.getOrientation();
		byte d = (byte)args.checkInteger(0);
        if (d < 0 || d >= 6) throw new Exception("parameter <side> out of range [0-6]");
        TileEntity te = Utils.getTileOnSide(this, s);
        if (te != null && te instanceof IEnergy) {
            PipeEnergy e = ((IEnergy)te).getEnergy(d);
            if (e != null) return new Object[]{e.Ucap, Double.valueOf(e.Umax)};
        }
        return new Object[]{Double.valueOf(0), Double.valueOf(0)};
	}
	
	@Optional.Method(modid = "OpenComputers")
	@Callback(doc = "" ,direct = true)
	public Object[] getInventory(Context cont, Arguments args) throws Exception
	{
		byte s = (byte)this.getOrientation();
		byte d = (byte)args.checkInteger(0);
        if (d < 0 || d >= 6) throw new Exception("parameter <side> out of range [0-6]");
        TileEntity te = Utils.getTileOnSide(this, s);
        if (te != null && te instanceof IInventory) {
            IInventory inv = (IInventory)te;
            int[] slots;
            if (inv instanceof ISidedInventory) {
                slots = ((ISidedInventory)inv).getSlotsForFace(EnumFacing.VALUES[d]);
            } else {
                slots = new int[inv.getSizeInventory()];
                for (int i = 0; i < slots.length; i++) slots[i] = i;
            }
            HashMap<Integer, HashMap<String, Integer>> list = new HashMap<Integer, HashMap<String, Integer>>();
            for (int i : slots) {
                ItemStack item = inv.getStackInSlot(i);
                if (item != null) {
                    HashMap<String, Integer> obj = new HashMap<String, Integer>();
                    obj.put("id", Item.getIdFromItem(item.getItem()));
                    obj.put("dmg", item.getItemDamage());
                    obj.put("am", item.stackSize);
                    list.put(i, obj);
                }
            }
            return new Object[]{list};
        } else return null;
	}
	
	@Optional.Method(modid = "OpenComputers")
	@Callback(doc = "" ,direct = true)
	public Object[] getFluidTank(Context cont, Arguments args) throws Exception
	{
		byte s = (byte)this.getOrientation();
		byte d = (byte)args.checkInteger(0);
        if (d < 0 || d >= 6) throw new Exception("parameter <side> out of range [0-6]");
        TileEntity te = Utils.getTileOnSide(this, s);
        if (te instanceof IFluidHandler) {
            IFluidHandler flh = (IFluidHandler)te;
            FluidTankInfo[] info = flh.getTankInfo(EnumFacing.VALUES[d]);
            HashMap<Integer, HashMap<String, Object>> list = new HashMap<Integer, HashMap<String, Object>>();
            if (info != null)
                for (int i = 0; i < info.length; i++) {
                    HashMap<String, Object> obj = new HashMap<String, Object>();
                    obj.put("cap", info[i].capacity);
                    FluidStack fluid = info[i].fluid;
                    if (fluid != null) {
                        obj.put("am", fluid.amount);
                        obj.put("id", fluid.getFluid().getName());
                    } else {
                        obj.put("am", 0);
                        obj.put("id", 0);
                    }
                    list.put(i, obj);
                }
            return new Object[]{list};
        } else return null;
	}
	
	@Optional.Method(modid = "OpenComputers")
	@Callback(doc = "" ,direct = true)
	public Object[] getEnergy(Context cont, Arguments args) throws Exception
	{
		byte s = (byte)this.getOrientation();
		byte d = (byte)args.checkInteger(0);
        if (d < 0 || d >= 6) throw new Exception("parameter <side> out of range [0-6]");
        TileEntity te = Utils.getTileOnSide(this, s);
        IEnergyAccess energy = EnergyAPI.get(te);
        return new Object[]{Double.valueOf(energy.getStorage(d) / 1000F), Double.valueOf(energy.getCapacity(d) / 1000F)};
	}
	
}
