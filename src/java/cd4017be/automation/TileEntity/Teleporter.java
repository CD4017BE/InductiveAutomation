/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cd4017be.automation.TileEntity;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import cpw.mods.fml.common.Optional;
import cpw.mods.fml.common.Optional.Interface;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.Environment;
import li.cil.oc.api.network.Message;
import li.cil.oc.api.network.Node;
import cd4017be.api.automation.AreaProtect;
import cd4017be.api.automation.IEnergy;
import cd4017be.api.automation.IOperatingArea;
import cd4017be.api.automation.PipeEnergy;
import cd4017be.api.computers.ComputerAPI;
import cd4017be.automation.Config;
import cd4017be.automation.Item.ItemTeleporterCoords;
import cd4017be.lib.BlockItemRegistry;
import cd4017be.lib.MovedBlock;
import cd4017be.lib.TileContainer;
import cd4017be.lib.TileEntityData;
import cd4017be.lib.templates.AutomatedTile;
import cd4017be.lib.templates.IAutomatedInv;
import cd4017be.lib.templates.Inventory;
import cd4017be.lib.templates.Inventory.Component;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

/**
 *
 * @author CD4017BE
 */
@Optional.InterfaceList(value = {@Interface(iface = "dan200.computercraft.api.peripheral.IPeripheral", modid = "ComputerCraft"), @Interface(iface = "li.cil.oc.api.network.Environment", modid = "OpenComputers")})
public class Teleporter extends AutomatedTile implements IOperatingArea, IAutomatedInv, IEnergy, IPeripheral, Environment
{
	private static final float resistor = 50F;
	private static final float eScale = (float)Math.sqrt(1D - 1D / resistor);
	
    public static float Energy = 2000F;
    private int[] area = new int[6];
    private int[] target = new int[4];
    
    private String lastUser = "";
    
    public Teleporter()
    {
        inventory = new Inventory(this, 16, new Component(1, 2, 0));
        energy = new PipeEnergy(Config.Umax[2], Config.Rcond[2]);
        //mode[int 3]: 0x1 = use rst, 0x2 = rel coord, 0x4 = last rst, 0x8 = teleport, 0x10 = copy
        netData = new TileEntityData(1, 4, 2, 0);
    }
    
    @Override
    public void onPlaced(EntityLivingBase entity, ItemStack item)  
    {
        lastUser = entity.getCommandSenderName();
    }
    
    @Override
    public boolean onActivated(EntityPlayer player, int s, float X, float Y, float Z) 
    {
        return super.onActivated(player, s, X, Y, Z);
    }
    
    @Override
    public void updateEntity() 
    {
        super.updateEntity();
        if (worldObj.isRemote) return;
        if ((netData.ints[3] & 1) == 1)
        {
            boolean r = worldObj.getBlockPowerInput(xCoord, yCoord, zCoord) > 0;
            if (r && (netData.ints[3] & 4) == 0)
            {
            	netData.ints[3] ^= 4;
                initTeleportation();
            } else
            if (!r && (netData.ints[3] & 4) == 4)
            {
            	netData.ints[3] ^= 4;
            	netData.ints[3] &= ~8;
            }
        }
        if ((netData.ints[3] & 8) != 0) {
            if (netData.floats[0] < netData.floats[1]) {
                netData.floats[0] += energy.getEnergy(0, resistor);
                energy.Ucap *= eScale;
            } else {
                netData.floats[0] -= netData.floats[1];
                netData.floats[1] = 0;
                teleport();
            }
        } else if (node != null && netData.floats[0] < 1000F) {
        	netData.floats[0] += energy.getEnergy(0, resistor);
        	energy.Ucap *= eScale;
        }
        netData.floats[0] -= ComputerAPI.update(this, node, (netData.ints[3] & 8) == 0 ? netData.floats[0] : 0);
    }
    
    private void initTeleportation()
    {
        int sx = area[3] - area[0];
        int sy = area[4] - area[1];
        int sz = area[5] - area[2];
        int dx = netData.ints[0];
        int dy = netData.ints[1];
        int dz = netData.ints[2];
        if ((netData.ints[3] & 2) == 0)
        {
            dx -= xCoord;
            dy -= yCoord;
            dz -= zCoord;
        }
        World world = worldObj;
        int[] maxS = Handler.maxSize(this);
        if ((dx != 0 || dy != 0 || dz != 0) && (sx <= maxS[0] && sy <= maxS[1] && sz <= maxS[2]))
        {
        	netData.ints[3] |= 8;
            netData.floats[1] = (float)(sx * sy * sz) * (float)Math.sqrt(dx*dx + dy*dy + dz*dz) * Energy;
            TileEntity te = worldObj.getTileEntity(xCoord + dx, yCoord + dy, zCoord + dz);
            if (te != null && te instanceof InterdimHole) {
                InterdimHole idwh = (InterdimHole)te;
                netData.floats[1] += (float)(sx * sy * sz) * 256D * Energy;
                dx = idwh.linkX - idwh.xCoord;
                dy = idwh.linkY - idwh.yCoord;
                dz = idwh.linkZ - idwh.zCoord;
                world = DimensionManager.getWorld(idwh.linkD);
                if (world == null){
                	netData.ints[3] &= ~8;
                    return;
                }
            }
        } else return;
        for (int cx = area[0] >> 4; cx <= area[3] >> 4; cx++)
        for (int cz = area[2] >> 4; cz <= area[5] >> 4; cz++)
        if (!AreaProtect.instance.isOperationAllowed(lastUser, worldObj, cx, cz)) netData.ints[3] &= ~8;
        for (int cx = (area[0] + dx) >> 4; cx <= (area[3] + dx) >> 4; cx++)
        for (int cz = (area[2] + dz) >> 4; cz <= (area[5] + dz) >> 4; cz++)
        if (!AreaProtect.instance.isOperationAllowed(lastUser, world, cx, cz)) netData.ints[3] &= ~8;
        target = new int[]{area[0] + dx, area[1] + dy, area[2] + dz, world.provider.dimensionId};
    }
    
    private void teleport()
    {
    	netData.ints[3] &= ~8;
        int[] pos = {area[0], area[1], area[2], area[3] - area[0], area[4] - area[1], area[5] - area[2]};
        World world = DimensionManager.getWorld(target[3]);
        if (world == null) return;
        if (xCoord < area[0] || xCoord >= area[3] || yCoord < area[1] || yCoord >= area[4] || zCoord < area[2] || zCoord >= area[5]) {
            area[0] = target[0];
            area[1] = target[1];
            area[2] = target[2];
            area[3] = area[0] + pos[3];
            area[4] = area[1] + pos[4];
            area[5] = area[2] + pos[5];
        }
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        for (int cx = target[0] >> 4; cx <= (target[0] + pos[3]) >> 4; cx++)
        for (int cz = target[2] >> 4; cz <= (target[2] + pos[5]) >> 4; cz++)
        if (!world.getChunkProvider().chunkExists(cx, cz))
        {
            world.getChunkProvider().loadChunk(cx, cz);
        }
        List<Entity> e0 = worldObj.getEntitiesWithinAABB(Entity.class, AxisAlignedBB.getBoundingBox(pos[0], pos[1], pos[2], pos[0] + pos[3], pos[1] + pos[4], pos[2] + pos[5]));
        List<Entity> e1 = world.getEntitiesWithinAABB(Entity.class, AxisAlignedBB.getBoundingBox(target[0], target[1], target[2], target[0] + pos[3], target[1] + pos[4], target[2] + pos[5]));
        int dx = target[0] - pos[0];
        int dy = target[1] - pos[1];
        int dz = target[2] - pos[2];
        for (Iterator<Entity> i = e0.iterator(); i.hasNext();)
        {
            Entity e = i.next();
            MovedBlock.moveEntity(e, world.provider.dimensionId, e.posX + dx, e.posY + dy, e.posZ + dz);
        }
        for (Iterator<Entity> i = e1.iterator(); i.hasNext();)
        {
            Entity e = i.next();
            MovedBlock.moveEntity(e, worldObj.provider.dimensionId, e.posX - dx, e.posY - dy, e.posZ - dz);
        }
        boolean ox = dx < 0, oy = dy < 0, oz = dz < 0;
        for (int x = ox ? 0 : pos[3] - 1; ox ? x < pos[3] : x >= 0; x += ox ? 1 : -1)
        for (int y = oy ? 0 : pos[4] - 1; oy ? y < pos[4] : y >= 0; y += oy ? 1 : -1)
        for (int z = oz ? 0 : pos[5] - 1; oz ? z < pos[5] : z >= 0; z += oz ? 1 : -1)
        {
            MovedBlock b0 = MovedBlock.get(worldObj, pos[0] + x, pos[1] + y, pos[2] + z);
            MovedBlock b1 = MovedBlock.get(world, target[0] + x, target[1] + y, target[2] + z);
            if ((netData.ints[3] & 16) != 0) {
                b0.set(world, target[0] + x, target[1] + y, target[2] + z);
            } else {
                b0.set(world, target[0] + x, target[1] + y, target[2] + z);
                b1.set(worldObj, pos[0] + x, pos[1] + y, pos[2] + z);
            }
        }
    }
    
    public int getStorageScaled(int s)
    {
        return netData.floats[1] <= 0 ? 0 : (int)(netData.floats[0] * s / netData.floats[1]);
    }

    @Override
    protected void customPlayerCommand(byte cmd, DataInputStream dis, EntityPlayerMP player) throws IOException 
    {
        lastUser = player.getCommandSenderName();
        if (cmd == 0)
        {
            byte b = dis.readByte();
            if (b == 0)
            {
            	netData.ints[3] ^= 4;
                if ((netData.ints[3] & 4) == 4)
                {
                    initTeleportation();
                } else
                {
                	netData.ints[3] &= ~8;
                }
                return;
            } else
            if (b == 1)
            {
            	netData.ints[3] ^= 1;
            } else
            if (b == 2)
            {
            	netData.ints[3] ^= 2;
                if ((netData.ints[3] & 2) == 0) {
                	netData.ints[0] += xCoord;
                	netData.ints[1] += yCoord;
                	netData.ints[2] += zCoord;
                } else {
                	netData.ints[0] -= xCoord;
                	netData.ints[1] -= yCoord;
                	netData.ints[2] -= zCoord;
                }
            } else
            if (b == 3)
            {
                if (inventory.items[0] == null && inventory.items[1] == null)
                {
                    if ((netData.ints[3] & 2) == 0){
                    	netData.ints[0] = xCoord;
                    	netData.ints[1] = yCoord;
                    	netData.ints[2] = zCoord;
                    } else {
                    	netData.ints[0] = 0;
                    	netData.ints[1] = 0;
                    	netData.ints[2] = 0;
                    }
                }
                if (inventory.items[0] != null && (inventory.items[0].getItem() instanceof ItemTeleporterCoords || inventory.items[0].getItem() == Items.paper))
                {
                    String name = inventory.items[0].stackTagCompound == null ? "" : inventory.items[0].stackTagCompound.getString("name");
                    inventory.items[0] = BlockItemRegistry.stack("item.teleporterCoords", inventory.items[0].stackSize);
                    inventory.items[0].stackTagCompound = new NBTTagCompound();
                    inventory.items[0].stackTagCompound.setString("name", name);
                    inventory.items[0].stackTagCompound.setInteger("x", netData.ints[0]);
                    inventory.items[0].stackTagCompound.setInteger("y", netData.ints[1]);
                    inventory.items[0].stackTagCompound.setInteger("z", netData.ints[2]);
                }
            } else
            if (b == 4)
            {
                if (player.capabilities.isCreativeMode) netData.ints[3] ^= 16;
                else netData.ints[3] &= ~16;
            }
        } else
        if (cmd == 1)
        {
        	netData.ints[0] = dis.readInt();
        } else
        if (cmd == 2)
        {
        	netData.ints[1] = dis.readInt();
        } else
        if (cmd == 3)
        {
        	netData.ints[2] = dis.readInt();
        } else
        if (cmd == 4 && inventory.items[0] != null && inventory.items[0].getItem() instanceof ItemTeleporterCoords && inventory.items[0].stackTagCompound != null)
        {
            inventory.items[0].stackTagCompound.setString("name", dis.readUTF());
        }
        netData.ints[3] &= ~8;
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) 
    {
        super.writeToNBT(nbt);
        nbt.setByte("mode", (byte)netData.ints[3]);
        nbt.setInteger("px", netData.ints[0]);
        nbt.setInteger("py", netData.ints[1]);
        nbt.setInteger("pz", netData.ints[2]);
        nbt.setIntArray("area", area);
        nbt.setFloat("storage", netData.floats[0]);
        nbt.setString("lastUser", lastUser);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) 
    {
        super.readFromNBT(nbt);
        netData.ints[3] = nbt.getByte("mode");
        netData.ints[0] = nbt.getInteger("px");
        netData.ints[1] = nbt.getInteger("py");
        netData.ints[2] = nbt.getInteger("pz");
        area = nbt.getIntArray("area");
        if (area == null || area.length != 6) area = new int[6];
        netData.floats[0] = nbt.getFloat("storage");
        netData.ints[3] &= ~8;
        lastUser = nbt.getString("lastUser");
        this.onUpgradeChange(3);
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
            netData.ints[3] &= ~8;
        }
        this.worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    @Override
    public boolean canInsert(ItemStack item, int cmp, int i) 
    {
        return this.isValid(item, cmp, i);
    }

    @Override
    public boolean canExtract(ItemStack item, int cmp, int i) 
    {
        return true;
    }

    @Override
    public boolean isValid(ItemStack item, int cmp, int i) 
    {
        return item == null || item.getItem() instanceof ItemTeleporterCoords;
    }
    
    @Override
    public void slotChange(ItemStack oldItem, ItemStack newItem, int i) 
    {
        if (i >= inventory.componets[0].s && i < inventory.componets[0].e && newItem != null && newItem.getItem() instanceof ItemTeleporterCoords && newItem.stackTagCompound != null)
        {
        	netData.ints[0] = newItem.stackTagCompound.getInteger("x");
        	netData.ints[1] = newItem.stackTagCompound.getInteger("y");
            netData.ints[2] = newItem.stackTagCompound.getInteger("z");
        }
    }
    
    @Override
    public void initContainer(TileContainer container)
    {
        container.addEntitySlot(new Slot(this, 0, 116, 52));
        container.addEntitySlot(new Slot(this, 1, 152, 52));
        for (int i = 0; i < 9; i++)
        {
            container.addEntitySlot(new Slot(this, 2 + i, 8 + i * 18, 16));
        }
        
        container.addPlayerInventory(8, 86);
    }
    
    @Override
    public int[] stackTransferTarget(ItemStack item, int s, TileContainer container) 
    {
        if (s < 2) return new int[]{2, 11};
        else if (s >= 2 && s < 11) return new int[]{1, 2};
        else return new int[]{2, 11};
    }
    
    @Override
	public int[] getUpgradeSlots() 
	{
		return new int[]{11, 12, 13, 14, 15};
	}

    @Override
	public int[] getBaseDimensions() 
	{
		return new int[]{12, 12, 12, Integer.MAX_VALUE, Config.Umax[2]};
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
			energy = new PipeEnergy(Handler.Umax(this), Config.Rcond[2]);
			energy.Ucap = u;
		} else if (s != 4){
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
		return null;
	}

	//ComputerCraft:

	@Optional.Method(modid = "ComputerCraft")
    @Override
    public String getType() 
    {
        return "Automation-Teleporter";
    }

	@Optional.Method(modid = "ComputerCraft")
    @Override
    public String[] getMethodNames() 
    {
        return new String[]{"getPosition", "setCoords", "teleport"};
    }

	@Optional.Method(modid = "ComputerCraft")
    @Override
    public Object[] callMethod(IComputerAccess computer, ILuaContext lua, int cmd, Object[] par) throws LuaException 
    {
        if (cmd == 0) {
            return new Object[]{Double.valueOf(xCoord), Double.valueOf(yCoord), Double.valueOf(zCoord)};
        } else if (cmd == 1) {
            if (par == null || par.length != 4) throw new LuaException("4 parameters needed: (int x, int y, int z, bool relative_Coord)");
            netData.ints[3] &= ~8;
            netData.ints[0] = ((Double)par[0]).intValue();
            netData.ints[1] = ((Double)par[1]).intValue();
            netData.ints[2] = ((Double)par[2]).intValue();
            if (((Boolean)par[3]).booleanValue()) netData.ints[3] |= 2;
            else netData.ints[3] &= ~2;
            lastUser = "#Computer" + computer.getID();
            return new Object[0];
        } else if (cmd == 2) {
            if (par != null && par.length > 0) throw new LuaException("This method does not take any parameters");
            lastUser = "#Computer" + computer.getID();
            if ((netData.ints[3] & 8) == 0) initTeleportation();
            return new Object[]{Boolean.valueOf((netData.ints[3] & 8) != 0)};
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
        return this.hashCode() == peripheral.hashCode();
    }

	//OpenComputers:
	
    private Object node = ComputerAPI.newOCnode(this, "Automation-Teleporter", true);
    
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
	@Callback(doc = "function():int{xCoord, yCoord, zCoord} --returns the position of the teleporter block", direct = true)
	public Object[] getPosition(Context cont, Arguments args)
	{
		return new Object[]{xCoord, yCoord, zCoord};
	}
	
	@Optional.Method(modid = "OpenComputers")
	@Callback(doc = "function(x:int, y:int, z:int, rel:bool) --sets the teleporter target position in relative (rel=true) or absolute (rel=false) coordinates", direct = true)
	public Object[] setCoords(Context cont, Arguments args)
	{
		netData.ints[3] &= ~8;
        netData.ints[0] = args.checkInteger(0);
        netData.ints[1] = args.checkInteger(1);
        netData.ints[2] = args.checkInteger(2);
        if (args.checkBoolean(3)) netData.ints[3] |= 2;
        else netData.ints[3] &= ~2;
        return null;
	}
	
	@Optional.Method(modid = "OpenComputers")
	@Callback(doc = "function():bool --starts teleportation energy accumulation and returns true if succeed", direct = true)
	public Object[] teleport(Context cont, Arguments args)
	{
		if ((netData.ints[3] & 8) == 0) initTeleportation();
        return new Object[]{(netData.ints[3] & 8) != 0};
	}
	
}
