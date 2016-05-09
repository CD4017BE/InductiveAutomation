/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cd4017be.automation.TileEntity;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

import cd4017be.api.automation.AreaProtect;
import cd4017be.api.automation.IAreaConfig;
import cd4017be.automation.Objects;
import cd4017be.automation.Item.ItemFluidDummy;
import cd4017be.lib.ModTileEntity;
import cd4017be.lib.MovedBlock;
import cd4017be.lib.util.Obj2;
import cd4017be.lib.util.VecN;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidBlock;

/**
 *
 * @author CD4017BE
 */
public class AntimatterBomb extends ModTileEntity implements ITickable
{
    
    public int antimatter;
    public int timer = -1;
    public byte state = 0; //0 = idle, 1 = countdown, 2 = exploding
    private AntimatterExplosion explosion = null;
    private MatterOrb loot = null;
    
    @Override
    public void update() 
    {
        if (worldObj.isRemote) return;
        if (state != 0) {
            timer--;
            this.markUpdate();
        }
        if (state == 1 && timer == 0) {
            if (this.isProtected()) {
                state = 0;
                return;
            }
            loot = new MatterOrb();
            explosion = new AntimatterExplosion();
            explosion.initExplosion();
            state = 2;
        } else if (state == 2) {
        	if (explosion == null) {
        		
        	}
        	if (!explosion.explode()) explosion = null;
        }
    }
    
    private boolean isProtected()
    {
        int x = pos.getX() >> 4;
        int z = pos.getZ() >> 4;
        int dx, dz;
        ArrayList<IAreaConfig> list = AreaProtect.instance.loadedSS.get(this.worldObj.provider.getDimension());
        if (list == null) return false;
        for (IAreaConfig area : list) {
            for (int[] e : area.getProtectedChunks("#antimatterBomb")) {
            	dx = e[0] - x; dz = e[1] - z;
                if (dx * dx + dz * dz < 70000) return true;
            }
        }
        return false;
    }
    
    @Override
    public void onNeighborBlockChange(Block b) 
    {
        if (state == 0 && worldObj.isBlockPowered(pos)) {
        	state = 1;
        	timer = 200;
        }
    }

    @Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
    	antimatter = pkt.getNbtCompound().getInteger("am");
    	timer = pkt.getNbtCompound().getShort("timer");
    	state = pkt.getNbtCompound().getByte("state");
	}

    @Override
    public Packet getDescriptionPacket() 
    {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setByte("state", state);
        if (state == 0) nbt.setInteger("am", antimatter);
        else nbt.setShort("timer", (short)timer);
        return new SPacketUpdateTileEntity(getPos(), -1, nbt);
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) 
    {
        super.writeToNBT(nbt);
        nbt.setShort("timer", (short)timer);
        nbt.setInteger("antimatter", antimatter);
        nbt.setByte("state", state);
        if (loot != null) nbt.setTag("loot", loot.writeItems());
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) 
    {
        super.readFromNBT(nbt);
        timer = nbt.getShort("timer");
        antimatter = nbt.getInteger("antimatter");
        state = nbt.getByte("state");
        if (nbt.hasKey("loot")) {
        	loot = new MatterOrb();
        	loot.readItems(nbt.getTagList("loot", 10));
        }
    }
    
    @Override
    public void onPlaced(EntityLivingBase entity, ItemStack item) 
    {
        if (item != null && item.getItem() == Item.getItemFromBlock(this.getBlockType())) {
            this.antimatter = 0;
            if (item.getTagCompound() != null) antimatter = item.getTagCompound().getInteger("antimatter");
        }
    }

    @Override
    public ArrayList<ItemStack> dropItem(IBlockState state, int fortune) 
    {
        ItemStack item;
        if (this.state == 2) {
        	if (loot != null) return loot.dropItem(Objects.antimatterBombE.getDefaultState(), fortune);
        	item = new ItemStack(Objects.antimatterBombE);
        } else {
        	item = new ItemStack(state.getBlock(), 1, 0);
        	if (antimatter >= 0) {
                item.setTagCompound(new NBTTagCompound());
                item.getTagCompound().setInteger("antimatter", antimatter);
            }
        }
        ArrayList<ItemStack> list = new ArrayList<ItemStack>();
        list.add(item);
        return list;
    }
    
    public static int maxSize = 256;
    public static float explMult = 1F;
    public static final float PowerFactor = 5.625F;
    public static final int Density = 2;
    
    private class AntimatterExplosion {
    	
    	public AntimatterExplosion()
        {
    		this.posX = pos.getX() + 0.5F;
    		this.posY = pos.getY() + 0.5F;
    		this.posZ = pos.getZ() + 0.5F;
            this.chunkOffsX = (pos.getX() - maxSize) >> 4;
            this.chunkOffsZ = (pos.getZ() - maxSize) >> 4;
            this.chunkSizeX = ((pos.getX() + maxSize + 16) >> 4) - this.chunkOffsX;
            this.chunkSizeZ = ((pos.getZ() + maxSize + 16) >> 4) - this.chunkOffsZ;
            this.editedChunks = new BitSet(this.chunkSizeX * this.chunkSizeZ);
        }
        
        private boolean explode()
        {
            int size = -timer;
        	boolean run = false;
            long t = System.nanoTime();
            for (int i = 0; i < 6; i++)
                run |= explosionTick(i, size);
            destroyBlocks();
            damageEntitys(size);
            t = System.nanoTime() - t;
            if (t > 50000000) System.out.println(String.format("Layer %d took %.3f s", size, (double)t / 1000000000D));
            return run && size < maxSize;
        }
        
        private float[][] lastRayPower;
        private static final float AirLoss = 0.25F;
        private static final float FluidLoss = 1.0F;
        private static final float EntityDamage = 0.25F;
        private static final float EntityAcleration = 0.1F;
        private BitSet editedChunks = new BitSet();
        private float posX, posY, posZ;
        private int chunkOffsX, chunkOffsZ, chunkSizeX, chunkSizeZ;
        private final ArrayList<Obj2<BlockPos, Boolean>> destroyedBlocks = new ArrayList<Obj2<BlockPos, Boolean>>();
        
        private void initExplosion()
        {
        	float energy = (float)antimatter * PowerFactor * Density * Density * explMult; //Geometric energy correction (Cube-Surface > Sphere-Surface)
            antimatter = -1;
        	this.doGammaDamage(energy);
            lastRayPower = new float[6][];
            energy /= 6F;
            for (int i = 0; i < 6; i++) lastRayPower[i] = new float[]{energy};
        }
        
        private boolean explosionTick(int d, int size)
        {
            if (lastRayPower[d] == null) return false;
            int l = size * 2 * Density + 1;
            float[] power = new float[l * l];
            boolean run = false;
            int n = 0;
            int j, i;
            BlockPos pos;
            float p, r;
            for (j = 0; j < l; j++)
                for (i = 0; i < l; i++) {
                    p = getPower(i, j, d, size);
                    if (p <= 0) {n++; continue;}
                    VecN v = this.getPosition(i, j, d, size).norm().scale(size);
                    pos = AntimatterBomb.this.pos.add(v.x[0] + 0.5D, v.x[1] + 0.5D, v.x[2] + 0.5D);
                    if (pos.getY() < -1 || pos.getY() > 256) {power[n++] = 0; continue;}
                    IBlockState state = worldObj.getBlockState(pos);
                    if (state.getBlock().isAir(state, worldObj, pos)) {
                        p -= AirLoss;
                    } else if (state.getMaterial().isLiquid()) {
                        p -= FluidLoss;
                        if (p >= 0) destroyedBlocks.add(new Obj2<BlockPos, Boolean>(pos, false));
                    } else {
                        r = state.getBlock().getExplosionResistance(worldObj, pos, null, null);
                        if (r < AirLoss) r = AirLoss;
                        if (p >= r) destroyedBlocks.add(new Obj2<BlockPos, Boolean>(pos, true));
                        p -= r;
                    }
                    run |= p > 0;
                    power[n++] = p > 0 ? p : 0;
                }
            if (run) {
                lastRayPower[d] = power;
                return true;
            } else {
            	lastRayPower[d] = null;
            	return false;
            }
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
        
        private float getPower(int x, int y, int d, int size)
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
        
    	private void damageEntitys(int size) {
    		float size0 = (float)size + 0.5F, size1 = (float)size - 3.5F;
    		size0 *= size0; size1 *= size1;
    		List<Entity> list = worldObj.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos).expandXyz(size));
    		for (Entity entity : list) {
    			double dist = entity.getDistanceSqToCenter(pos);
    			if (!entity.isDead && dist < size0) {
    				if (entity instanceof EntityItem && loot != null) {
    					ArrayList<ItemStack> item = new ArrayList<ItemStack>();
    					item.add(((EntityItem)entity).getEntityItem());
    					loot.addItems(item);
    					entity.setDead();
    					continue;
    				} else if (dist < size1) continue;
    				int[] i = this.getIndex(entity.posX, entity.posY, entity.posZ, size);
    				if (lastRayPower[i[0]] == null) continue;
    				float p = lastRayPower[i[0]][i[1]];
    				float dx = (float)(entity.posX - posX);
    				float dy = (float)(entity.posY - posY);
    				float dz = (float)(entity.posZ - posZ);
    				entity.attackEntityFrom(DamageSource.causeExplosionDamage(null), (int)(p * EntityDamage));
    				p *= EntityAcleration;
    				if (p > 1) p = 1F;
    				dist = Math.sqrt(dist);
    				entity.motionX += dx / dist * p;
    				entity.motionY += dy / dist * p;
    				entity.motionZ += dz / dist * p;
    			}
    		}
    	}

    	private void destroyBlocks() {
    		IBlockState state;
    		Block block;
    		BlockPos pos;
    		for (Obj2<BlockPos, Boolean> next : destroyedBlocks) {
    			pos = next.objA;
    			state = worldObj.getBlockState(pos);
    			block = state.getBlock();
    			editedChunks.set((pos.getX() >> 4) - chunkOffsX + ((pos.getZ() >> 4) - chunkOffsZ) * chunkSizeX);
    			if (state.getBlock() == Objects.antimatterBombF) {
    				TileEntity te = worldObj.getTileEntity(pos);
    				if (te != null && te instanceof AntimatterBomb) {
    					int[] i = this.getIndex(pos.getX() + 0.5D, pos.getY() + 0.5, pos.getZ() + 0.5, -timer);
    					lastRayPower[i[0]][i[1]] += ((AntimatterBomb)te).antimatter;
    					((AntimatterBomb)te).antimatter = 0;
    				}
    				loot.addItems(block.getDrops(worldObj, pos, state, 0));
    			}
    			else if (next.objB) loot.addItems(block.getDrops(worldObj, pos, state, 0));
    			else {
    				FluidStack fluid = null;
    				if (block instanceof IFluidBlock) fluid = ((IFluidBlock)block).drain(worldObj, pos, false);
    				else if (state == Blocks.water.getDefaultState() || state == Blocks.flowing_water.getDefaultState()) fluid = new FluidStack(FluidRegistry.WATER, 1000);
    				else if (state == Blocks.lava.getDefaultState() || state == Blocks.flowing_lava.getDefaultState()) fluid = new FluidStack(FluidRegistry.LAVA, 1000);
    				if (fluid != null && fluid.amount >= 1000) loot.addItems(new ArrayList<ItemStack>(Arrays.asList(ItemFluidDummy.item(fluid.getFluid(), fluid.amount / 1000))));
    			}
    			block.breakBlock(worldObj, pos, state);
    			MovedBlock.setBlock(worldObj, pos, Blocks.air.getDefaultState(), null);
    		}
    		destroyedBlocks.clear();
    	}

    	private void doGammaDamage(float energy) {
    		List<Entity> list = worldObj.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos).expandXyz(maxSize));
    		for (Entity entity : list) {
    			if (entity instanceof EntityLivingBase) {
    				if (worldObj.rayTraceBlocks(new Vec3d(posX, posY, posZ), new Vec3d(entity.posX, entity.posY, entity.posZ), true) == null) {
    					double d = Math.log(energy / (double)(Density * Density) / entity.getDistanceSq(posX, posY, posZ));
    					double o = d * entity.getLookVec().dotProduct(new Vec3d(posX - entity.posX, posY - entity.posY, posZ - entity.posZ).normalize());
    					entity.attackEntityFrom(DamageSource.inFire, (int)(d * 2.5));
    					if (o > 0) ((EntityLivingBase)entity).addPotionEffect(new PotionEffect(Potion.getPotionFromResourceLocation("blindness"), (int)(o * 50.0)));
    				}
    			}
    		}
    	}

        private int[] getIndex(double x, double y, double z, int size) 
        {
            double dx = x - posX, dy = y - posY, dz = z - posZ;
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
    
}
