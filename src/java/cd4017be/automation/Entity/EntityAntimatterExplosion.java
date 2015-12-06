/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cd4017be.automation.Entity;

import java.util.ArrayList;
import java.util.List;

import cd4017be.api.automation.IMatterStorage;
import cd4017be.lib.BlockItemRegistry;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

/**
 *
 * @author CD4017BE
 */
public class EntityAntimatterExplosion extends Entity
{
    private float energy;
    public int size;
    
    public EntityAntimatterExplosion(World world)
    {
        super(world);
    }
    
    public EntityAntimatterExplosion(World world, int x, int y, int z, float energy)
    {
        this(world);
        this.energy = energy * 2F * (float)Math.PI;
        this.size = -200;
        this.setPosition((double)x + 0.5D, (double)y + 0.5D, (double)z + 0.5D);
        System.out.println("Antimatter Bomb will explode in 10s with power: " + energy);
    }
    
    @Override
    protected void entityInit() 
    {
        this.setSize(1, 1);
        this.dataWatcher.addObject(2, new Integer(size));
    }

    @Override
    public boolean isEntityInvulnerable() 
    {
        return true;
    }

    @Override
    public void onEntityUpdate() 
    {
        if (worldObj.isRemote)
        {
            size = dataWatcher.getWatchableObjectInt(2);
            return;
        }
        size++;
        if (size == 0)
        {
            initExplosion();
        } else
        if (size > 0 && run)
        {
            IMatterStorage loot;
            TileEntity te = worldObj.getTileEntity((int)Math.floor(posX), (int)Math.floor(posY), (int)Math.floor(posZ));
            if (te != null && te instanceof IMatterStorage) loot = (IMatterStorage)te;
            else loot = null;
            explode(loot);
        }
        this.dataWatcher.updateObject(2, new Integer(size));
        if (size > 0 && !run) this.setDead();
    }
    
    private void explode(IMatterStorage loot)
    {
        explosionTick();
        destroyBlocks(loot);
        damageEntitys(loot);
    }
    
    @Override
    public void addVelocity(double par1, double par3, double par5) 
    {}
    

    @Override
    protected void readEntityFromNBT(NBTTagCompound nbt) 
    {
        size = nbt.getInteger("Size");
        energy = nbt.getFloat("Energy");
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound nbt) 
    {
        nbt.setInteger("Size", size);
        nbt.setFloat("Energy", energy);
    }
    
    private float[][] lastRayPower;
    private int num;
    private float lastRad;
    private final float RAD = 2F * (float)Math.PI;
    private final float AirLoss = 0.25F;
    private final float EntityDamage = 0.5F;
    private final float EntityAcleration = 0.1F;
    private boolean run;
    private final List<BlockEntry> destroyedBlocks = new ArrayList();
    private class BlockEntry
    {
        public int x;
        public int y;
        public int z;
        public boolean drop;
        public BlockEntry(int x, int y, int z, boolean drop)
        {
            this.x = x;
            this.y = y;
            this.z = z;
            this.drop = drop;
        }
    }
    
    private void initExplosion()
    {
        worldObj.setBlock((int)Math.floor(posX), (int)Math.floor(posY), (int)Math.floor(posZ), BlockItemRegistry.blockId("tile.antimatterBombE"), 0, 3);
        lastRayPower = new float[1][1];
        lastRad = RAD;
        lastRayPower[0][0] = energy;
        run = true;
        num = 1;
    }
    
    private void explosionTick()
    {
        float a = 1F / (float)size;
        run = false;
        int n = Math.round((float)Math.ceil(RAD / a));
        float[][] rayPower = new float[n * 2][n];
        for (int i = 0; i < n * 2; i++)
        {
            for (int j = 0; j < n; j++)
            {
                float b = (float)i * a / 2;
                float c = (float)j * a;
                float p = getPower(b, c, a);
                if (p <= 0)
                {
                    rayPower[i][j] = 0;
                    continue;
                }
                int y = (int)Math.floor(posY + (float)size * Math.cos(c / 2));
                float xz = (float)size * (float)Math.sin(c / 2);
                int x = (int)Math.floor(posX + xz * Math.cos(b));
                int z = (int)Math.floor(posZ + xz * Math.sin(b));
                Block block = worldObj.getBlock(x, y, z);
                if (block == null)
                {
                    p -= AirLoss;
                } else
                {
                    float r = block.getExplosionResistance(this);
                    if (r < AirLoss) r = AirLoss;
                    if (p >= r) destroyedBlocks.add(new BlockEntry(x, y, z, true));
                    p -= r;
                }
                run |= p > 0;
                rayPower[i][j] = p > 0 ? p : 0;
            }
        }
        lastRad = a;
        lastRayPower = rayPower;
        num = n;
    }
    
    private void damageEntitys(IMatterStorage loot)
    {
        List<Entity> list = this.worldObj.getEntitiesWithinAABBExcludingEntity(this, AxisAlignedBB.getBoundingBox(
                posX - (float)size, posY - (float)size, posZ - (float)size,
                posX + (float)size, posY + (float)size, posZ + (float)size));
        for (Entity entity : list) {
            double dist = entity.getDistance(posX, posY, posZ);
            if (!entity.isDead && dist < size * size)
            {
                if (entity instanceof EntityItem && loot != null) {
                    ArrayList<ItemStack> item = new ArrayList();
                    item.add(((EntityItem)entity).getEntityItem());
                    loot.addItems(item);
                    entity.setDead();
                    continue;
                } else if (dist < size - 4F) continue;
                float dx = (float)(entity.posX - posX);
                float dy = (float)(entity.posY - posY);
                float dz = (float)(entity.posZ - posZ);
                float dxz = (float)Math.sqrt(dx * dx + dz * dz);
                float yaw = (float)Math.atan2(dz, dx);
                float pitch = (float)Math.atan2(dxz, dy);
                if (yaw < 0)
                {
                    yaw += RAD / 2F;
                    pitch += RAD / 2F;
                }
                if (pitch < 0) pitch += RAD;
                float p = getPower(yaw * 2F, pitch * 2F, lastRad) / lastRad / (float)dist;
                entity.attackEntityFrom(DamageSource.setExplosionSource(null), (int)(p * EntityDamage));
                p *= EntityAcleration;
                if (p > 1) p = 1F;
                entity.motionX += dx / dist * p;
                entity.motionY += dy / dist * p;
                entity.motionZ += dz / dist * p;
            }
        }
    }
    
    private void destroyBlocks(IMatterStorage loot)
    {
        for (BlockEntry next : destroyedBlocks) {
            Block block = worldObj.getBlock(next.x, next.y, next.z);
            if (block != null)
            {
                if (loot != null) loot.addItems(block.getDrops(worldObj, next.x, next.y, next.z, worldObj.getBlockMetadata(next.x, next.y, next.z), 0));
                worldObj.setBlockToAir(next.x, next.y, next.z);
            }
        }
        destroyedBlocks.clear();
    }
    
    private float getPower(float yaw, float pitch, float rad)
    {
        float q = rad / lastRad;
        float ry = yaw / lastRad;
        float rp = pitch / lastRad;
        int a = Math.round((float)Math.floor(ry));
        int b = Math.round((float)Math.floor(rp));
        int c = a + 1;
        int d = b + 1;
        float yawb = yaw + rad;
        float pitchb = pitch + rad;
        boolean bla = yawb > c * lastRad;
        boolean blb = pitchb > d * lastRad;
        if (bla && blb)
        {
            float rba = (float)c * lastRad;
            float rbb = (float)d * lastRad;
            q = lastRad * lastRad;
            return lastRayPower[a % num][b % num] / q * (rba - yaw) * (rbb - pitch) +
                   lastRayPower[c % num][b % num] / q * (yawb - rba) * (rbb - pitch) +
                   lastRayPower[a % num][d % num] / q * (rba - yaw) * (pitchb - rbb) +
                   lastRayPower[c % num][d % num] / q * (yawb - rba) * (pitchb - rbb);
        } else
        if (bla)
        {
            float rb = (float)c * lastRad;
            return (lastRayPower[a % num][b % num] / lastRad * (rb - yaw) + lastRayPower[c % num][b % num] / lastRad * (yawb - rb)) * q;
        } else
        if (blb)
        {
            float rb = (float)d * lastRad;
            return (lastRayPower[a % num][b % num] / lastRad * (rb - pitch) + lastRayPower[a % num][d % num] / lastRad * (pitchb - rb)) * q;
        } else
        {
            return lastRayPower[a % num][b % num] * q * q;
        }
    }
    
}
