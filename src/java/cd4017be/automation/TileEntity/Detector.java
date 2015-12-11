/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cd4017be.automation.TileEntity;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;

import cpw.mods.fml.common.Optional;
import cpw.mods.fml.common.Optional.Interface;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.Environment;
import li.cil.oc.api.network.Message;
import li.cil.oc.api.network.Node;
import cd4017be.api.automation.IEnergy;
import cd4017be.api.automation.PipeEnergy;
import cd4017be.api.circuits.IRedstone8bit;
import cd4017be.api.circuits.RedstoneHandler;
import cd4017be.api.computers.ComputerAPI;
import cd4017be.api.energy.EnergyAPI;
import cd4017be.api.energy.IEnergyAccess;
import cd4017be.lib.TileContainer;
import cd4017be.lib.TileEntityData;
import cd4017be.lib.templates.AutomatedTile;
import cd4017be.lib.templates.Inventory;
import cd4017be.lib.templates.SlotHolo;
import cd4017be.lib.util.Utils;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

/**
 *
 * @author CD4017BE
 */
@Optional.InterfaceList(value = {@Interface(iface = "dan200.computercraft.api.peripheral.IPeripheral", modid = "ComputerCraft"), @Interface(iface = "li.cil.oc.api.network.Environment", modid = "OpenComputers")})
public class Detector extends AutomatedTile implements IRedstone8bit, IPeripheral, Environment
{
    
    public Detector()
    {
        inventory = new Inventory(this, 6);
        /**
         * long:
         * int: modes, ref0, ref1, ref2, ref3, ref4, ref5, sides
         */
        netData = new TileEntityData(1, 8, 0, 0);
    }

    @Override
    public void updateEntity() 
    {
        super.updateEntity();
        if (worldObj.isRemote) return;
        ComputerAPI.update(this, node, 0);
        if (netData.ints[0] == 0) return;
        int s = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
        TileEntity te = Utils.getTileOnSide(this, (byte)s);
        IInventory inv = te != null && te instanceof IInventory ? (IInventory)te : null;
        IFluidHandler tank = te != null && te instanceof IFluidHandler ? (IFluidHandler)te : null;
        boolean change = false;
        for (int i = 0; i < 6; i++) {
            byte m = this.getMode(i);
            byte d = this.getSide(i);
            boolean op = (m & 4) != 0;
            boolean os = this.getState(i);
            boolean ns;
            m &= 3;
            if (d >= 6) ns = (netData.ints[i + 1] < 0) ^ op;
            else if (m == 1) {
                ns = this.checkVoltage(te, d, netData.ints[i + 1]) ^ op;
            } else if (m == 2) {
                ns = this.checkInventory(inv, d, netData.ints[i + 1], inventory.items[i]) ^ op;
            } else if (m == 3) {
                ns = this.checkTank(tank, d, netData.ints[i + 1], inventory.items[i]) ^ op;
            } else ns = false;
            this.setState(i, ns);
            if (os ^ ns) {
                ForgeDirection dir = ForgeDirection.getOrientation(i);
                change = true;
                worldObj.notifyBlockOfNeighborChange(xCoord + dir.offsetX, yCoord + dir.offsetY, zCoord + dir.offsetZ, worldObj.getBlock(xCoord, yCoord, zCoord));
            }
        }
        if (change) RedstoneHandler.notify8bitNeighbors(this, this.getValue(0), 1);
    }
    
    private boolean checkVoltage(TileEntity te, int s, int ref)
    {
        if (te == null) return ref < 0;
        IEnergyAccess energy = EnergyAPI.getAccess(te);
        return ref < energy.getStorage(s) / 1000F;
    }
    
    private boolean checkInventory(IInventory inv, int s, int ref, ItemStack type)
    {
        if (inv == null) return ref < 0;
        int[] slots = Inventory.getSlots(inv, s);
        int n = 0;
        for (int slot : slots) {
            ItemStack item = inv.getStackInSlot(slot);
            if (item != null && (type == null || item.isItemEqual(type)) && (n += item.stackSize) > ref)
                return true;
        }
        return false;
    }
    
    private boolean checkTank(IFluidHandler tank, int s, int ref, ItemStack type)
    {
        if (tank == null) return ref < 0;
        FluidTankInfo[] data = tank.getTankInfo(ForgeDirection.getOrientation(s));
        if (data == null) return ref < 0;
        int n = 0;
        for (FluidTankInfo info : data) {
            if (info.fluid != null && (type == null || info.fluid.isFluidEqual(type)) && (n += info.fluid.amount) > ref)
                return true;
        }
        return false;
    }
    
    public byte getMode(int s)
    {
        return (byte)(netData.ints[0] >> (s * 4) & 0xf);
    }
    
    public void setMode(int s, byte v)
    {
        netData.ints[0] &= ~(0xf << (s * 4));
        netData.ints[0] |= (v & 0xf) << (s * 4);
    }
    
    public boolean getState(int s)
    {
        return (byte)(netData.ints[0] >> (s + 24) & 1) != 0;
    }
    
    public void setState(int s, boolean v)
    {
        if (v) netData.ints[0] |= 1 << (24 + s);
        else netData.ints[0] &= ~(1 << (24 + s));
    }
    
    public byte getSide(int s)
    {
        return (byte)(netData.ints[7] >> (s * 4) & 0xf);
    }
    
    public void setSide(int s, byte v)
    {
        netData.ints[7] &= ~(0xf << (s * 4));
        netData.ints[7] |= (v & 0xf) << (s * 4);
    }

    @Override
    public int redstoneLevel(int s, boolean str) 
    {
        return !str && this.getState(s) ? 15 : 0;
    }
    
    @Override
    protected void customPlayerCommand(byte cmd, DataInputStream dis, EntityPlayerMP player) throws IOException 
    {
        if (cmd == 0) {
            byte s = dis.readByte();
            if (s >= 0 && s < 6)
            this.setMode(s, dis.readByte());
        } else if (cmd == 1) {
            byte s = dis.readByte();
            if (s >= 0 && s < 6)
            netData.ints[s + 1] = dis.readInt();
        } else if (cmd == 2) {
            byte s = dis.readByte();
            if (s >= 0 && s < 6)
            this.setSide(s, dis.readByte());
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) 
    {
        super.writeToNBT(nbt);
        nbt.setIntArray("data", netData.ints);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) 
    {
        super.readFromNBT(nbt);
        int[] data = nbt.getIntArray("data");
        System.arraycopy(data, 0, netData.ints, 0, Math.min(data.length, netData.ints.length));
    }
    
    @Override
    public void initContainer(TileContainer container) 
    {
        for (int i = 0; i < 6; i++) {
            container.addEntitySlot(new SlotHolo(this, i, 44, 16 + i * 18, false, false));
        }
        
        container.addPlayerInventory(8, 140);
    }
    
    @Override
    public ItemStack getStackInSlotOnClosing(int i) 
    {
        return null;
    }

    @Override
    public byte getValue(int s) 
    {
        return (byte)(netData.ints[0] >> 24);
    }

    @Override
    public byte getDirection(int s) 
    {
        return 1;
    }

    @Override
    public void setValue(int s, byte v, int recursion) 
    {
    }

    //ComputerCraft:
    
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
        byte s = (byte)worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
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
                    slots = ((ISidedInventory)inv).getAccessibleSlotsFromSide(d);
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
                FluidTankInfo[] info = flh.getTankInfo(ForgeDirection.getOrientation(d));
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
		byte s = (byte)worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
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
		byte s = (byte)worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
		byte d = (byte)args.checkInteger(0);
        if (d < 0 || d >= 6) throw new Exception("parameter <side> out of range [0-6]");
        TileEntity te = Utils.getTileOnSide(this, s);
        if (te != null && te instanceof IInventory) {
            IInventory inv = (IInventory)te;
            int[] slots;
            if (inv instanceof ISidedInventory) {
                slots = ((ISidedInventory)inv).getAccessibleSlotsFromSide(d);
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
		byte s = (byte)worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
		byte d = (byte)args.checkInteger(0);
        if (d < 0 || d >= 6) throw new Exception("parameter <side> out of range [0-6]");
        TileEntity te = Utils.getTileOnSide(this, s);
        if (te instanceof IFluidHandler) {
            IFluidHandler flh = (IFluidHandler)te;
            FluidTankInfo[] info = flh.getTankInfo(ForgeDirection.getOrientation(d));
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
	}
	
	@Optional.Method(modid = "OpenComputers")
	@Callback(doc = "" ,direct = true)
	public Object[] getEnergy(Context cont, Arguments args) throws Exception
	{
		byte s = (byte)worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
		byte d = (byte)args.checkInteger(0);
        if (d < 0 || d >= 6) throw new Exception("parameter <side> out of range [0-6]");
        TileEntity te = Utils.getTileOnSide(this, s);
        IEnergyAccess energy = EnergyAPI.getAccess(te);
        return new Object[]{Double.valueOf(energy.getStorage(d) / 1000F), Double.valueOf(energy.getCapacity(d) / 1000F)};
	}
	
}
