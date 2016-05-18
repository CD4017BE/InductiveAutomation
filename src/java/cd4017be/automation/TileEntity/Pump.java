/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cd4017be.automation.TileEntity;

import net.minecraft.network.PacketBuffer;

import java.io.IOException;
import java.util.UUID;

import com.mojang.authlib.GameProfile;

import cd4017be.api.automation.IEnergy;
import cd4017be.api.automation.IOperatingArea;
import cd4017be.api.automation.PipeEnergy;
import cd4017be.automation.Config;
import cd4017be.automation.Item.ItemMachineSynchronizer;
import cd4017be.lib.TileContainer;
import cd4017be.lib.TileContainer.TankSlot;
import cd4017be.lib.TileEntityData;
import cd4017be.lib.templates.AutomatedTile;
import cd4017be.lib.templates.Inventory;
import cd4017be.lib.templates.SlotTank;
import cd4017be.lib.templates.TankContainer;
import cd4017be.lib.util.CachedChunkProtection;
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
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.Fluid;
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
    private static final GameProfile defaultUser = new GameProfile(new UUID(0, 0), "#Pump");
    private GameProfile lastUser = defaultUser;
    private boolean disabled = false;
	private CachedChunkProtection prot;

    @Override
    public void onPlaced(EntityLivingBase entity, ItemStack item)  
    {
    	if (entity instanceof EntityPlayer)lastUser = ((EntityPlayer)entity).getGameProfile();
        prot = null;
    }
    
    @Override
    public boolean onActivated(EntityPlayer player, EnumFacing s, float X, float Y, float Z) 
    {
        lastUser = player.getGameProfile();
        prot = null;
        return super.onActivated(player, s, X, Y, Z);
    }
    
    public Pump()
    {
        netData = new TileEntityData(2, 2, 0, 1);
        inventory = new Inventory(this, 6);
        tanks = new TankContainer(this, new Tank(Config.tankCap[1] * 2, 1).setOut(0)).setNetLong(1);
        energy = new PipeEnergy(Config.Umax[1], Config.Rcond[1]);
    }
    
    @Override
    public void update() 
    {
    	super.update();
        if (worldObj.isRemote) return;
        if (slave == null || ((TileEntity)slave).isInvalid()) slave = ItemMachineSynchronizer.getLink(inventory.items[5], this);
        if (storage < Energy)
        {
            storage += energy.getEnergy(0, resistor);
            energy.Ucap *= eScale;
        }
        if (!disabled && alternateCheckBlock(new BlockPos(px, py, pz)))
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
    
    private boolean slaveOP(BlockPos pos)
    {
    	if (slave != null && slave instanceof Builder && 
    			!((TileEntity)slave).isInvalid() && (slave.getSlave() == null || !(slave.getSlave() instanceof Pump || slave.getSlave().getSlave() != null))) {
        	return slave.remoteOperation(pos);
        } else return true;
    }
    
    private boolean checkBlock(BlockPos pos)
    {
    	if (prot == null || !prot.equalPos(pos)) prot = CachedChunkProtection.get(lastUser, worldObj, pos);
    	if (!prot.allow) return true;
    	FluidStack fluid = Utils.getFluid(worldObj, pos, this.blockNotify);
        if (fluid == null || (tanks.isLocked(0) && !fluid.isFluidEqual(tanks.getFluid(0)))) return slaveOP(pos);
        else if (storage < Energy) return false;
        else return this.drain(pos, fluid);
    }
    
    private int[] blocks = new int[0];
    private int dist = -1;
    private Fluid fluidId;
    private boolean flowDir;
    private static final EnumFacing[] SearchArray = {EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.EAST, EnumFacing.WEST};
    private final EnumFacing[] lastDir = new EnumFacing[2];
    
    private boolean alternateCheckBlock(BlockPos pos)
    {
        if (!worldObj.isBlockLoaded(pos)) return false;
        if (blocks.length == 0) return checkBlock(pos);
        if (dist < 0) {
            FluidStack fluid = Utils.getFluid(worldObj, pos, this.blockNotify);
            if (fluid != null && (!tanks.isLocked(0) || fluid.isFluidEqual(tanks.getFluid(0)))) {
                fluidId = fluid.getFluid();
                flowDir = fluid.getFluid().getDensity() <= 0;
                dist = 0;
                blocks[dist] = 0;
                lastDir[0] = lastDir[1] = null;
            } else return true;
        }
        if (storage < Energy) return false;
        int target = blocks[dist];
        netData.ints[1] = target;
        byte dx = (byte)target, dy = (byte)(target >> 8), dz = (byte)(target >> 16);
        BlockPos np = pos.add(dx, dy, dz);
        if (prot == null || !prot.equalPos(np)) prot = CachedChunkProtection.get(lastUser, worldObj, np);
    	if (!prot.allow) return true;
        if (dist >= blocks.length - 1) {
            this.moveBack(np);
            return false;
        }
        EnumFacing dir = this.findNextDir(np);
        if (dir == null) this.moveBack(np);
        else {
            if (dir == EnumFacing.UP || dir == EnumFacing.DOWN) lastDir[0] = lastDir[1] = null;
            else if (dir != lastDir[0]) {
                lastDir[1] = lastDir[0];
                lastDir[0] = dir;
            }
            blocks[dist] = (blocks[dist] & 0x00ffffff) | (dir.ordinal() << 24);
            blocks[++dist] = (dx + dir.getFrontOffsetX() & 0xff) | (dy + dir.getFrontOffsetY() & 0xff) << 8 | (dz + dir.getFrontOffsetZ() & 0xff) << 16;
        }
        return dist < 0;
    }
    
    private EnumFacing findNextDir(BlockPos pos)
    {
        if (this.isValidPos(pos.up(flowDir ? -1 : 1))) return flowDir ? EnumFacing.DOWN : EnumFacing.UP;
        else if (lastDir[0] != null && this.isValidPos(pos.add(lastDir[0].getFrontOffsetX(), 0, lastDir[0].getFrontOffsetZ()))) return lastDir[0];
        else if (lastDir[0] == null || lastDir[1] == null) {
            for (EnumFacing dir : SearchArray) {
                if (this.isValidPos(pos.add(dir.getFrontOffsetX(), 0, dir.getFrontOffsetZ()))) return dir;
            }
            return null;
        } else if (this.isValidPos(pos.add(lastDir[1].getFrontOffsetX(), 0, lastDir[1].getFrontOffsetZ()))) return lastDir[1];
        else if (this.isValidPos(pos.add(-lastDir[1].getFrontOffsetX(), 0, -lastDir[1].getFrontOffsetZ()))) {
            BlockPos pos1 = pos.add(-lastDir[1].getFrontOffsetX(), 0, -lastDir[1].getFrontOffsetZ()); int d1 = dist - 1;
            while (--d1 > 0) {
                pos1 = pos1.add(-lastDir[0].getFrontOffsetX(), 0, -lastDir[0].getFrontOffsetZ());
                if ((byte)blocks[d1] + px == pos1.getX() && (byte)(blocks[d1] >> 16) + pz == pos1.getZ()) return null;
                else if (!this.isValidPos(pos1)) return lastDir[1].getOpposite();
            }
            return null;
        } else return null;
    }
    
    private boolean isValidPos(BlockPos pos)
    {
        if (pos.getX() >= area[0] && pos.getX() < area[3] && pos.getY() >= area[1] && pos.getY() < area[4] && pos.getZ() >= area[2] && pos.getZ() < area[5]) return false;
        BlockPos pos1 = pos.add(-px, -py, -pz);
        int l = blocks.length / 3;
        if (pos.getY() < 0 || pos.getY() >= 256 || Math.abs(pos1.getY()) > l || Math.abs(pos1.getX()) > l || Math.abs(pos1.getZ()) > l) return false;
        FluidStack fluid = Utils.getFluid(worldObj, pos, this.blockNotify);
        if (fluid == null || fluid.getFluid() != fluidId) return false;
        int p = (pos1.getX() & 0xff) | (pos1.getY() & 0xff) << 8 | (pos1.getZ() & 0xff) << 16;
        for (int i = dist - 1; i >= 0; i -= 2) {
            if ((blocks[i] & 0xffffff) == p) return false;
        }
        return true;
    }
    
    private void moveBack(BlockPos pos)
    {
        FluidStack fluid = Utils.getFluid(worldObj, pos, this.blockNotify);
        if (fluid != null && fluid.getFluid() == fluidId) this.drain(pos, fluid);
        dist--;
        if (dist <= 0) {
            lastDir[0] = null;
            return;
        }
        byte d0 = (byte)(blocks[dist - 1] >> 24), d1;
        if (d0 <= 1) lastDir[0] = null;
        else if (EnumFacing.VALUES[d0] != lastDir[0]) {
            lastDir[0] = EnumFacing.VALUES[d0];
            for (int i = dist - 2; i >= 0; i--) {
                d1 = (byte)(blocks[i] >> 24);
                if (d1 <= 1) break;
                else if (d1 != d0) {
                    lastDir[1] = EnumFacing.VALUES[d1];
                    return;
                }
            }
            lastDir[1] = null;
        }
    }
    
    private boolean drain(BlockPos pos, FluidStack fluid)
    {
        if (this.tanks.fill(0, fluid, false) == fluid.amount) {
            this.tanks.fill(0, fluid, true);
            if (this.blockNotify) worldObj.setBlockToAir(pos);
            else worldObj.setBlockState(pos, Blocks.air.getDefaultState(), 2);
            storage -= fluid.amount > 0 ? Energy : Energy * 0.25F;
            return this.slaveOP(pos);
        } else return false;
    }
    
    @Override
    protected void customPlayerCommand(byte cmd, PacketBuffer dis, EntityPlayerMP player) throws IOException 
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
        try {lastUser = new GameProfile(new UUID(nbt.getLong("lastUserID0"), nbt.getLong("lastUserID1")), nbt.getString("lastUser"));
        } catch (Exception e) {lastUser = defaultUser;}
        prot = null;
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
        nbt.setString("lastUser", lastUser.getName());
        nbt.setLong("lastUserID0", lastUser.getId().getMostSignificantBits());
        nbt.setLong("lastUserID1", lastUser.getId().getLeastSignificantBits());
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
        this.worldObj.markBlockForUpdate(getPos());
    }
    
    @Override
    public void initContainer(TileContainer container)
    {
        container.addEntitySlot(new SlotTank(this, 0, 202, 34));
        
        container.addPlayerInventory(8, 16);
        
        container.addTankSlot(new TankSlot(tanks, 0, 184, 16, true));
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
			this.worldObj.markBlockForUpdate(getPos());
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
	public boolean remoteOperation(BlockPos pos) 
	{
		disabled = true;
		if (pos.getX() < area[0] || pos.getY() < area[1] || pos.getZ() < area[2] || pos.getX() >= area[3] || pos.getY() >= area[4] || pos.getZ() >= area[5]) return true;
    	return this.checkBlock(pos);
	}

	@Override
	public IOperatingArea getSlave() 
	{
		return slave;
	}
    
}
