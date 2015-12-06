/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cd4017be.automation.TileEntity;

import java.io.DataInputStream;
import java.io.IOException;

import cd4017be.api.automation.AreaProtect;
import cd4017be.api.automation.IEnergy;
import cd4017be.api.automation.IOperatingArea;
import cd4017be.api.automation.PipeEnergy;
import cd4017be.automation.Config;
import cd4017be.automation.Item.ItemMachineSynchronizer;
import cd4017be.lib.TileContainer;
import cd4017be.lib.TileEntityData;
import cd4017be.lib.templates.AutomatedTile;
import cd4017be.lib.templates.Inventory;
import cd4017be.lib.templates.SlotTank;
import cd4017be.lib.templates.TankContainer;
import cd4017be.lib.util.Utils;
import cd4017be.lib.templates.TankContainer.Tank;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidHandler;

/**
 *
 * @author CD4017BE
 */
public class Pump extends AutomatedTile implements IEnergy, IOperatingArea, IFluidHandler, ISidedInventory
{
    public static float Energy = 8000F;
    private static final float resistor = 25F;
    private static final float eScale = (float)Math.sqrt(1D - 1D / resistor);
    private int[] area = new int[6];
    private int px;
    private int py;
    private int pz; 
    private float storage;
    public boolean blockNotify;
    private IOperatingArea slave = null;
    private String lastUser = "";
    private boolean disabled = false;

    @Override
    public void onPlaced(EntityLivingBase entity, ItemStack item)  
    {
        lastUser = entity.getCommandSenderName();
    }
    
    @Override
    public boolean onActivated(EntityPlayer player, int s, float X, float Y, float Z) 
    {
        lastUser = player.getCommandSenderName();
        return super.onActivated(player, s, X, Y, Z);
    }
    
    public Pump()
    {
        netData = new TileEntityData(2, 1, 0, 1);
        inventory = new Inventory(this, 6).setInvName("Automatic Pump");
        tanks = new TankContainer(this, new Tank(Config.tankCap[1] * 2, 1).setOut(0)).setNetLong(1);
        energy = new PipeEnergy(Config.Umax[1], Config.Rcond[1]);
    }
    
    @Override
    public void updateEntity() 
    {
        super.updateEntity();
        if (worldObj.isRemote) return;
        if (slave == null || ((TileEntity)slave).isInvalid()) slave = ItemMachineSynchronizer.getLink(inventory.items[5], this);
        if (storage < Energy)
        {
            storage += energy.getEnergy(0, resistor);
            energy.Ucap *= eScale;
        }
        if (!disabled && alternateCheckBlock(px, py, pz))
        {
            px++;
            if (px >= area[3])
            {
                px = area[0];
                pz++;
            }
            if (pz >= area[5])
            {
                pz = area[2];
                py--;
            }
            if (py < area[1])
            {
                py = area[4] - 1;
            }
        }
        
    }
    
    private boolean slaveOP(int x, int y, int z)
    {
    	if (slave != null && slave instanceof Builder && 
    			!((TileEntity)slave).isInvalid() && (slave.getSlave() == null || !(slave.getSlave() instanceof Pump || slave.getSlave().getSlave() != null))) {
        	return slave.remoteOperation(x, y, z);
        } else return true;
    }
    
    private boolean checkBlock(int x, int y, int z)
    {
    	
    	FluidStack fluid = Utils.getFluid(worldObj, x, y, z, this.blockNotify);
        if (fluid == null || (tanks.isLocked(0) && !fluid.isFluidEqual(tanks.getFluid(0)))) return slaveOP(x, y, z);
        else if (!AreaProtect.instance.isOperationAllowed(lastUser, worldObj, x >> 4, z >> 4)) return true;
        else if (storage < Energy) return false;
        else return this.drain(x, y, z, fluid);
    }
    
    private int[] blocks = new int[0];
    private int dist = -1;
    private int fluidId;
    private boolean flowDir;
    private static final ForgeDirection[] SearchArray = {ForgeDirection.NORTH, ForgeDirection.SOUTH, ForgeDirection.EAST, ForgeDirection.WEST};
    private final ForgeDirection[] lastDir = new ForgeDirection[2];
    
    private boolean alternateCheckBlock(int x, int y, int z)
    {
        if (!worldObj.blockExists(x, y, z)) return false;
        if (blocks.length == 0) return checkBlock(x, y, z);
        if (dist < 0) {
            FluidStack fluid = Utils.getFluid(worldObj, x, y, z, this.blockNotify);
            if (fluid != null && (!tanks.isLocked(0) || fluid.isFluidEqual(tanks.getFluid(0)))) {
                fluidId = fluid.fluidID;
                flowDir = fluid.getFluid().getDensity() <= 0;
                dist = 0;
                blocks[dist] = 0;
                lastDir[0] = lastDir[1] = ForgeDirection.UNKNOWN;
            } else return true;
        }
        if (storage < Energy) return false;
        int target = blocks[dist];
        byte dx = (byte)target, dy = (byte)(target >> 8), dz = (byte)(target >> 16);
        if (!AreaProtect.instance.isOperationAllowed(lastUser, worldObj, (x + dx) >> 4, (z + dz) >> 4)) return true;
        if (dist >= blocks.length - 1) {
            this.moveBack(x + dx, y + dy, z + dz);
            return false;
        }
        ForgeDirection dir = this.findNextDir(x + dx, y + dy, z + dz);
        if (dir == null) this.moveBack(x + dx, y + dy, z + dz);
        else {
            if (dir == ForgeDirection.UP || dir == ForgeDirection.DOWN) lastDir[0] = lastDir[1] = ForgeDirection.UNKNOWN;
            else if (dir != lastDir[0]) {
                lastDir[1] = lastDir[0];
                lastDir[0] = dir;
            }
            blocks[dist] = (blocks[dist] & 0x00ffffff) | (dir.ordinal() << 24);
            blocks[++dist] = (dx + dir.offsetX & 0xff) | (dy + dir.offsetY & 0xff) << 8 | (dz + dir.offsetZ & 0xff) << 16;
        }
        return dist < 0;
    }
    
    private ForgeDirection findNextDir(int x, int y, int z)
    {
        if (this.isValidPos(x, y + (flowDir ? -1 : 1), z)) return flowDir ? ForgeDirection.DOWN : ForgeDirection.UP;
        else if (lastDir[0] != ForgeDirection.UNKNOWN && this.isValidPos(x + lastDir[0].offsetX, y, z + lastDir[0].offsetZ)) return lastDir[0];
        else if (lastDir[0] == ForgeDirection.UNKNOWN || lastDir[1] == ForgeDirection.UNKNOWN) {
            for (ForgeDirection dir : SearchArray) {
                if (this.isValidPos(x + dir.offsetX, y, z + dir.offsetZ)) return dir;
            }
            return null;
        } else if (this.isValidPos(x + lastDir[1].offsetX, y, z + lastDir[1].offsetZ)) return lastDir[1];
        else if (this.isValidPos(x - lastDir[1].offsetX, y, z - lastDir[1].offsetZ)) {
            int x1 = x - lastDir[1].offsetX, z1 = z - lastDir[1].offsetZ, d1 = dist - 1;
            while (--d1 > 0) {
                x1 -= lastDir[0].offsetX; z1 -= lastDir[0].offsetZ;
                if ((byte)blocks[d1] + px == x1 && (byte)(blocks[d1] >> 16) + pz == z1) return null;
                else if (!this.isValidPos(x1, y, z1)) return lastDir[1].getOpposite();
            }
            return null;
        } else return null;
    }
    
    private boolean isValidPos(int x, int y, int z)
    {
        if (x >= area[0] && x < area[3] && y >= area[1] && y < area[4] && z >= area[2] && z < area[5]) return false;
        if (Math.max(Math.max(Math.abs(x - xCoord), Math.abs(z - zCoord)), Math.abs(y - yCoord)) > blocks.length / 3) return false;
        FluidStack fluid = Utils.getFluid(worldObj, x, y, z, this.blockNotify);
        if (fluid == null || fluid.fluidID != fluidId) return false;
        int p = (x - px & 0xff) | (y - py & 0xff) << 8 | (z - pz & 0xff) << 16;
        for (int i = dist - 1; i >= 0; i -= 2) {
            if ((blocks[i] & 0xffffff) == p) return false;
        }
        return true;
    }
    
    private void moveBack(int x, int y, int z)
    {
        FluidStack fluid = Utils.getFluid(worldObj, x, y, z, this.blockNotify);
        if (fluid != null && fluid.fluidID == fluidId) this.drain(x, y, z, fluid);
        dist--;
        if (dist <= 0) {
            lastDir[0] = ForgeDirection.UNKNOWN;
            return;
        }
        byte d0 = (byte)(blocks[dist - 1] >> 24), d1;
        if (d0 <= 1) lastDir[0] = ForgeDirection.UNKNOWN;
        else if (d0 != lastDir[0].ordinal()) {
            lastDir[0] = ForgeDirection.getOrientation(d0);
            for (int i = dist - 2; i >= 0; i--) {
                d1 = (byte)(blocks[i] >> 24);
                if (d1 <= 1) break;
                else if (d1 != d0) {
                    lastDir[1] = ForgeDirection.getOrientation(d1);
                    return;
                }
            }
            lastDir[1] = ForgeDirection.UNKNOWN;
        }
    }
    
    private boolean drain(int x, int y, int z, FluidStack fluid)
    {
        if (this.tanks.fill(0, fluid, false) == fluid.amount) {
            this.tanks.fill(0, fluid, true);
            if (this.blockNotify) worldObj.setBlockToAir(x, y, z);
            else worldObj.setBlock(x, y, z, Blocks.air, 0, 2);
            storage -= fluid.amount > 0 ? Energy : Energy * 0.25F;
            return this.slaveOP(x, y, z);
        } else return false;
    }
    
    @Override
    protected void customPlayerCommand(byte cmd, DataInputStream dis, EntityPlayerMP player) throws IOException 
    {
        if (cmd == 0) {
            blockNotify = !blockNotify;
            netData.ints[0] = (netData.ints[0] & 0xff) | (blockNotify ? 0x100 : 0);
        } else if (cmd == 1) {
            byte l = dis.readByte();
            if (l < 0) l = 0;
            else if (l > 127) l = 127;
            blocks = new int[l * 3];
            dist = -1;
            netData.ints[0] = (netData.ints[0] & 0x100) | (l & 0xff);
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) 
    {
        super.readFromNBT(nbt);
        area = nbt.getIntArray("area");
        if (area == null || area.length != 6) area = new int[6];
        px = nbt.getInteger("px");
        py = nbt.getInteger("py");
        pz = nbt.getInteger("pz");
        storage = nbt.getFloat("storage");
        netData.ints[0] = nbt.getInteger("mode");
        blockNotify = (netData.ints[0] & 0x100) != 0;
        blocks = new int[(netData.ints[0] & 0x7f) * 3];
        dist = -1;
        lastUser = nbt.getString("lastUser");
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
        nbt.setInteger("mode", netData.ints[0]);
        nbt.setString("lastUser", lastUser);
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
            py = area[4] - 1;
            px = area[0];
            pz = area[2];
        }
        this.worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }
    
    @Override
    public void initContainer(TileContainer container)
    {
        container.addEntitySlot(new SlotTank(this, 0, 202, 34));
        
        container.addPlayerInventory(8, 16);
    }
    
    @Override
	public int[] getUpgradeSlots() 
	{
		return new int[]{1, 2, 3, 4, 5};
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
			this.worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
			return;
		}
		if (s == 3) {
			double u = energy.Ucap;
			energy = new PipeEnergy(Handler.Umax(this), Config.Rcond[1]);
			energy.Ucap = u;
		} else if (s == 4) {
			slave = ItemMachineSynchronizer.getLink(inventory.items[5], this);
		} else {
			Handler.setCorrectArea(this, area, true);
		}
	}

	@Override
	public boolean remoteOperation(int x, int y, int z) 
	{
		disabled = true;
		if (x < area[0] || y < area[1] || z < area[2] || x >= area[3] || y >= area[4] || z >= area[5]) return true;
    	return this.checkBlock(x, y, z);
	}

	@Override
	public IOperatingArea getSlave() 
	{
		return slave;
	}
    
}
