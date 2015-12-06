/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cd4017be.automation.Entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

import cd4017be.api.automation.IMatterStorage;
import cd4017be.automation.TileEntity.AntimatterBomb;
import cd4017be.lib.BlockItemRegistry;
import cd4017be.lib.MovedBlock;
import cd4017be.lib.util.VecN;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidBlock;

/**
 *
 * @author CD4017BE
 */
public class EntityAntimatterExplosion1 extends Entity
{
    private float energy;
    public int size;
    
    public EntityAntimatterExplosion1(World world)
    {
        super(world);
    }
    
    public EntityAntimatterExplosion1(World world, int x, int y, int z, float energy)
    {
        this(world);
        this.energy = energy * PowerFactor * Density * Density * explMult; //Geometric energy correction (Cube-Surface > Sphere-Surface)
        this.size = -1;
        this.setPosition((double)x + 0.5D, (double)y + 0.5D, (double)z + 0.5D);
        this.chunkOffsX = (x - maxSize) >> 4;
        this.chunkOffsZ = (z - maxSize) >> 4;
        this.chunkSizeX = ((x + maxSize + 16) >> 4) - this.chunkOffsX;
        this.chunkSizeZ = ((z + maxSize + 16) >> 4) - this.chunkOffsZ;
        this.editedChunks = new BitSet(this.chunkSizeX * this.chunkSizeZ);
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
        if (size > 0 && run != 0)
        {
            IMatterStorage loot;
            TileEntity te = worldObj.getTileEntity((int)Math.floor(posX), (int)Math.floor(posY), (int)Math.floor(posZ));
            if (te != null && te instanceof IMatterStorage) loot = (IMatterStorage)te;
            else loot = null;
            explode(loot);
        } else if (size > 0) {
        	this.doChunkUpdates();
        	this.setDead();
        }
        this.dataWatcher.updateObject(2, new Integer(size));
    }
    
    private void explode(IMatterStorage loot)
    {
        run = 0;
        long t = System.nanoTime();
        for (int i = 0; i < 6; i++)
            explosionTick(i);
        destroyBlocks(loot);
        damageEntitys(loot);
        t = System.nanoTime() - t;
        if (t > 50000000) System.out.println(String.format("Layer %d took %.3f s", size, (double)t / 1000000000D));
        if (size >= maxSize) run = 0;
    }
    
    private void doChunkUpdates()
    {
    	Chunk chunk;
    	for (int x = 0; x < chunkSizeX; x++)
    		for (int z = 0; z < chunkSizeZ; z++)
    			if (editedChunks.get(x + z * chunkSizeX)) {
    				chunk = worldObj.getChunkFromChunkCoords(chunkOffsX + x, chunkOffsZ + z);
    				chunk.generateSkylightMap();
    			}
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
    private static final float AirLoss = 0.25F;
    private static final float FluidLoss = 1.0F;
    private static final float EntityDamage = 0.25F;
    private static final float EntityAcleration = 0.1F;
    public static final float PowerFactor = 5.625F;
    public static final int Density = 2;
    public static int maxSize = 256;
    public static float explMult = 1F;
    private final Item IdFluid = BlockItemRegistry.itemId("item.fluidDummy");
    private byte run;
    private BitSet editedChunks = new BitSet();
    private int chunkOffsX, chunkOffsZ, chunkSizeX, chunkSizeZ;
    private final ArrayList<BlockEntry> destroyedBlocks = new ArrayList<BlockEntry>();
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
        this.doGammaDamage();
        worldObj.setBlock((int)Math.floor(posX), (int)Math.floor(posY), (int)Math.floor(posZ), BlockItemRegistry.blockId("tile.antimatterBombE"), 0, 3);
        lastRayPower = new float[6][];
        float e = energy / 6F;
        for (int i = 0; i < 6; i++) lastRayPower[i] = new float[]{e};
        run = -1;
        
    }
    
    private void explosionTick(int d)
    {
        if (lastRayPower[d] == null) return;
        int l = size * 2 * Density + 1;
        float[] power = new float[l * l];
        boolean run = false;
        int n = 0;
        int j, i, x, y, z;
        float p, r;
        for (j = 0; j < l; j++)
            for (i = 0; i < l; i++) {
                p = getPower(i, j, d);
                if (p <= 0) {n++; continue;}
                VecN v = this.getPosition(i, j, d, size).norm().scale(size).add(posX, posY, posZ);
                x = (int)Math.floor(v.x[0]); y = (int)Math.floor(v.x[1]); z = (int)Math.floor(v.x[2]);
                if (y < -1 || y > 256) {power[n++] = 0; continue;}
                Block block = worldObj.getBlock(x, y, z);
                if (block == null) {
                    p -= AirLoss;
                } else if (block.getMaterial().isLiquid()) {
                    p -= FluidLoss;
                    if (p >= 0) destroyedBlocks.add(new BlockEntry(x, y, z, false));
                } else {
                    r = block.getExplosionResistance(this, worldObj, x, y, z, posX, posY, posZ);
                    if (r < AirLoss) r = AirLoss;
                    if (p >= r) destroyedBlocks.add(new BlockEntry(x, y, z, true));
                    p -= r;
                }
                run |= p > 0;
                power[n++] = p > 0 ? p : 0;
            }
        if (run) {
            this.run |= 1 << d;
            lastRayPower[d] = power;
        } else lastRayPower[d] = null;
    }
    
    private VecN getPosition(int i, int j, int d, int s)
    {
        int s1 = s * Density;
        double a = (double)(s * 2) / (double)(s1 * 2 + 1);
        switch (d) {
            case 0: return new VecN((double)(i-s1) * a, -s, (double)(j-s1) * a);
            case 1: return new VecN((double)(i-s1) * a, s, (double)(j-s1) * a);
            case 2: return new VecN((double)(i-s1) * a, (double)(j-s1) * a, -s);
            case 3: return new VecN((double)(i-s1) * a, (double)(j-s1) * a, s);
            case 4: return new VecN(-s, (double)(i-s1) * a, (double)(j-s1) * a);
            case 5: return new VecN(s, (double)(i-s1) * a, (double)(j-s1) * a);
            default: return new VecN(0, 0, 0);
        }
    }
    
    private float getPower(int x, int y, int d)
    {
        int s = (size - 1) * 2 * Density + 1;
        float f = (float)s / (float)(size * 2 * Density + 1);
        float f0 = (float)x * f, f1 = (float)y * f, f2 = (float)(x + 1) * f, f3 = (float)(y + 1) * f;
        int x0 = (int)Math.floor(f0), y0 = (int)Math.floor(f1), x1 = (int)Math.floor(f2), y1 = (int)Math.floor(f3);
        if (x0 < 0) x0 = 0;
        if (y0 < 0) y0 = 0;
        if (x1 >= s) x1 = s - 1;
        if (y1 >= s) y1 = s - 1;
        boolean b0 = x0 != x1, b1 = y0 != y1;
        if (b0 && b1) {
            return lastRayPower[d][x0 + y0 * s] * ((float)x1 - f0) * ((float)y1 - f1) +
                   lastRayPower[d][x1 + y0 * s] * (f2 - (float)x1) * ((float)y1 - f1) +
                   lastRayPower[d][x0 + y1 * s] * ((float)x1 - f0) * (f3 - (float)y1) +
                   lastRayPower[d][x1 + y1 * s] * (f2 - (float)x1) * (f3 - (float)y1);
        } else if (b0) {
            return lastRayPower[d][x0 + y0 * s] * f * ((float)y1 - f1) +
                   lastRayPower[d][x0 + y1 * s] * f * (f3 - (float)y1);
        } else if (b1) {
            return lastRayPower[d][x0 + y0 * s] * ((float)x1 - f0) * f + 
                   lastRayPower[d][x1 + y0 * s] * (f2 - (float)x1) * f;
        } else {
            return lastRayPower[d][x0 + y0 * s] * f * f;
        }
    }
    
    private void damageEntitys(IMatterStorage loot)
    {
        List<Entity> list = this.worldObj.getEntitiesWithinAABBExcludingEntity(this, AxisAlignedBB.getBoundingBox(
                posX - (float)size, posY - (float)size, posZ - (float)size,
                posX + (float)size, posY + (float)size, posZ + (float)size));
        for (Entity entity : list) {
            double dist = entity.getDistance(posX, posY, posZ);
            if (!entity.isDead && dist < size)
            {
                if (entity instanceof EntityItem && loot != null) {
                    ArrayList<ItemStack> item = new ArrayList();
                    item.add(((EntityItem)entity).getEntityItem());
                    loot.addItems(item);
                    entity.setDead();
                    continue;
                } else if (dist < size - 4F) continue;
                int[] i = this.getIndex(entity.posX, entity.posY, entity.posZ);
                if (lastRayPower[i[0]] == null) continue;
                float p = lastRayPower[i[0]][i[1]];
                float dx = (float)(entity.posX - posX);
                float dy = (float)(entity.posY - posY);
                float dz = (float)(entity.posZ - posZ);
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
        boolean hasLoot = loot != null;
        Block id = BlockItemRegistry.blockId("tile.antimatterBombF");
        Block block;
        int m;
        for (BlockEntry next : destroyedBlocks) {
            block = worldObj.getBlock(next.x, next.y, next.z);
            m = worldObj.getBlockMetadata(next.x, next.y, next.z);
            editedChunks.set((next.x >> 4) - chunkOffsX + ((next.z >> 4) - chunkOffsZ) * chunkSizeX);
            if (block != null)
            {
                if (!hasLoot) {}
                else if (block == id) {
                    TileEntity te = worldObj.getTileEntity(next.x, next.y, next.z);
                    if (te != null && te instanceof AntimatterBomb) {
                        int[] i = this.getIndex(next.x + 0.5, next.y + 0.5, next.z + 0.5);
                        lastRayPower[i[0]][i[1]] += ((AntimatterBomb)te).antimatter;
                        ((AntimatterBomb)te).antimatter = 0;
                    }
                    loot.addItems(block.getDrops(worldObj, next.x, next.y, next.z, m, 0));
                }
                else if (next.drop) loot.addItems(block.getDrops(worldObj, next.x, next.y, next.z, m, 0));
                else {
                    ItemStack item = null;
                    if ((block == Blocks.water || block == Blocks.flowing_water) && m == 0) item = new ItemStack(IdFluid, 1, 1);
                    else if ((block == Blocks.lava || block == Blocks.flowing_lava) && m == 0) item = new ItemStack(IdFluid, 1, 2);
                    else if (block instanceof IFluidBlock) {
                        FluidStack fluid = ((IFluidBlock)block).drain(worldObj, next.x, next.y, next.z, false);
                        if (fluid != null && fluid.amount >= 1000) item = new ItemStack(IdFluid, fluid.amount / 1000, fluid.fluidID);
                    }
                    if (item != null) loot.addItems(new ArrayList<ItemStack>(Arrays.asList(item)));
                }
                block.breakBlock(worldObj, next.x, next.y, next.z, block, m);
                MovedBlock.setBlock(worldObj, next.x, next.y, next.z, Blocks.air, 0, null);
            }
        }
        destroyedBlocks.clear();
    }
    
    private void doGammaDamage()
    {
        List<Entity> list = this.worldObj.getEntitiesWithinAABBExcludingEntity(this, AxisAlignedBB.getBoundingBox(
                posX - (float)maxSize, posY - (float)maxSize, posZ - (float)maxSize,
                posX + (float)maxSize, posY + (float)maxSize, posZ + (float)maxSize));
        for (Entity entity : list) {
            if (entity instanceof EntityLivingBase) {
                if (worldObj.rayTraceBlocks(Vec3.createVectorHelper(posX, posY, posZ), Vec3.createVectorHelper(entity.posX, entity.posY, entity.posZ), true) == null) {
                    double d = Math.log(energy / (double)(Density * Density) / entity.getDistanceSq(posX, posY, posZ));
                    double o = d * entity.getLookVec().dotProduct(Vec3.createVectorHelper(posX - entity.posX, posY - entity.posY, posZ - entity.posZ).normalize());
                    entity.attackEntityFrom(DamageSource.inFire, (int)(d * 2.5));
                    if (o > 0) ((EntityLivingBase)entity).addPotionEffect(new PotionEffect(Potion.blindness.id, (int)(o * 50.0)));
                }
            }
        }
    }
    
    private int[] getIndex(double x, double y, double z) 
    {
        double dx = x - this.posX, dy = y - this.posY, dz = z - this.posZ;
        int d, i, j;
        int s1 = size * Density;
        int s2 = s1 * 2 + 1;
        double a = (double)(size * 2) / (double)(s1 * 2 + 1);
        double f;
        if (dx * dx > dz * dz && dx * dx > dy * dy) {
            d = dx > 0 ? 5 : 4;
            f = (double)size / Math.abs(dx) / a;
            i = (int)Math.round(dy * f) + s1;
            j = (int)Math.round(dz * f) + s1;
        } else if (dz * dz > dy * dy) {
            d = dz > 0 ? 3 : 2;
            f = (double)size / Math.abs(dz) / a;
            i = (int)Math.round(dx * f) + s1;
            j = (int)Math.round(dy * f) + s1;
        } else {
            d = dy > 0 ? 1 : 0;
            f = (double)size / Math.abs(dy) / a;
            i = (int)Math.round(dx * f) + s1;
            j = (int)Math.round(dz * f) + s1;
        }
        if (i >= s2) i = s2 - 1;
        else if (i < 0) i = 0;
        if (j >= s2) j = s2 - 1;
        else if (j < 0) j = 0;
        return new int[]{d, i + j * s2};
    }
    
}
