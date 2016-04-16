/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cd4017be.automation;

import java.util.ArrayList;
import java.util.List;

import cd4017be.api.automation.AreaProtect;
import cd4017be.api.automation.IAreaConfig;
import cd4017be.api.automation.ProtectLvl;
import cd4017be.automation.TileEntity.SecuritySys;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;

/**
 *
 * @author CD4017BE
 */
public class AreaConfig implements IAreaConfig
{
    public static float ChunkLoadCost = 2.4F;
	
	public static class Group {
        public byte[] area = new byte[64];
        public ArrayList<String> members = new ArrayList<String>();
        
        public float getEnergyCost()
        {
            float e = 0;
            for (int i = 0; i < area.length; i++)
            {
                e += ProtectLvl.getLvl(area[i]).energyCost;
            }
            return e;
        }
    }
    
    public AreaConfig(SecuritySys tile)
    {
        this.tile = tile;
        for (int i = 0; i < groups.length; i++)
        {
            groups[i] = new Group();
        }
    }
    
    public SecuritySys tile;
    public int px;
    public int pz;
    public Group[] groups = new Group[3];
    public Ticket chunkTicket;
    private int timer = 0;
    
    public byte getProtectLvlFor(String obj, int x, int z)
    {
        if (!active()) return 0;
        int dx = x - px;
        int dz = z - pz;
        if (dx < 0 || dz < 0 || dx >= 8 || dz >= 8) return 0;
        if (this.isPlayerOwner(obj)) return 0;
        for (int i = 1; i < groups.length; i++)
        if (groups[i].members.contains(obj))
        {
            if (i == 0) return 0;
            else return groups[i].area[dx + dz * 8];
        }
        return groups[0].area[dx + dz * 8];
    }
    
    public void tick()
    {
        if (update || (!tile.enabledC && chunksLoaded)) {
        	this.updateChunkLoading();
        }
    	if (timer++ < 5) return;
        timer = 0;
        if (tile.getWorld() instanceof WorldServer) {
            ServerConfigurationManager manager = MinecraftServer.getServer().getConfigurationManager();
            String[] rem = new String[tile.itemStorage.size()];
            int n = 0;
            for (Object obj : manager.playerEntityList) {
                EntityPlayerMP player = (EntityPlayerMP)obj;
                ItemStack[] stor = tile.itemStorage.get(player.getName());
                if (player.worldObj.provider.getDimension() == this.tile.getWorld().provider.getDimension() && this.getProtectLvlFor(player.getName(), player.chunkCoordX, player.chunkCoordZ) == 3) continue;
                if (stor != null) {
                    for (ItemStack item : stor) {
                        if (item != null && !player.inventory.addItemStackToInventory(item)) {
                            EntityItem ei = new EntityItem(player.worldObj, player.posX, player.posY, player.posZ, item);
                            tile.getWorld().spawnEntityInWorld(ei);
                        }
                    }
                    rem[n++] = player.getName();
                }
            }
            for (int i = 0; i < n; i++) tile.itemStorage.remove(rem[i]);
        }
        if (!tile.enabled) return;
        List<EntityPlayer> list = tile.getWorld().getEntitiesWithinAABB(EntityPlayer.class, new AxisAlignedBB(px << 4, 0, pz << 4, (px + 8) << 4, 256, (pz + 8) << 4));
        for (EntityPlayer e : list) {
            if (this.getProtectLvlFor(e.getName(), (int)Math.floor(e.posX) >> 4, (int)Math.floor(e.posZ) >> 4) == ProtectLvl.NoInventory.ordinal()) {
                ItemStack[] inv = tile.itemStorage.get(e.getName());
                if (inv == null) inv = new ItemStack[40];
                for (int i = 0; i < inv.length; i++) {
                    ItemStack item = e.inventory.getStackInSlot(i);
                    if (inv[i] == null){
                        inv[i] = item;
                        item = null;
                    } else for (int j = 0; j < inv.length && item != null; j++) {
                        if (inv[j] == null){
                            inv[j] = item;
                            item = null;
                        } else if (inv[j].isItemEqual(item)) {
                            if (inv[j].stackSize + item.stackSize <= item.getMaxStackSize()) {
                                inv[j].stackSize += item.stackSize;
                                item = null;
                            } else {
                                item.stackSize = item.getMaxStackSize() - inv[j].stackSize;
                                inv[j].stackSize = item.getMaxStackSize();
                            }
                        }
                    }
                    e.inventory.setInventorySlotContents(i, item);
                }
                e.inventory.dropAllItems();
                tile.itemStorage.put(e.getName(), inv);
            } else {
                ItemStack[] inv = tile.itemStorage.remove(e.getName());
                if (inv != null) {
                    for (ItemStack item : inv) {
                        if (item != null && !e.inventory.addItemStackToInventory(item)) {
                            EntityItem ei = new EntityItem(tile.getWorld(), e.posX, e.posY, e.posZ, item);
                            tile.getWorld().spawnEntityInWorld(ei);
                        }
                    }
                }
            }
        }
    }
    
    public boolean active()
    {
        return tile.enabled;
    }
    
    public float getEnergyCost()
    {
        float e = 0;
        for (Group group : groups) {
            e = Math.max(e, group.getEnergyCost());
        }
        return e;
    }
    
    public float getLoadEnergyCost()
    {
    	int n = 0;
    	for (int i = 0; i < 64; i++) n += loadedChunks >> i & 1;
    	return (float)n * ChunkLoadCost;
    }
    
    public boolean isChunkProtected(int cx, int cz)
    {
        if (!active()) return false;
        int dx = cx - px;
        int dz = cz - pz;
        if (dx < 0 || dz < 0 || dx >= 8 || dz >= 8) return false;
        for (int i = 0; i < groups.length; i++)
        if (groups[i].area[dx + dz * 8] != 0)
        return true;
        return false;
    }
    
    public void writeToNbt(NBTTagCompound nbt)
    {
        for (int i = 0; i < groups.length; i++)
        {
            nbt.setByteArray("Area" + i, groups[i].area);
            NBTTagList list = new NBTTagList();
            for (String name : groups[i].members)
            {
                list.appendTag(new NBTTagString(name));
            }
            nbt.setTag("Group" + i, list);
        }
        nbt.setLong("ChunkLoad", loadedChunks);
    }
    
    public void readFromNbt(NBTTagCompound nbt)
    {
        for (int i = 0; i < groups.length; i++)
        {
            groups[i].area = nbt.getByteArray("Area" + i);
            if (groups[i].area.length != 64) groups[i].area = new byte[64];
            NBTTagList list = nbt.getTagList("Group" + i, 8);
            groups[i].members.clear();
            for (int j = 0; j < list.tagCount(); j++) {
                groups[i].members.add(list.getStringTagAt(j));
            }
        }
        loadedChunks = nbt.getLong("ChunkLoad");
        update = true;
    }
    
    public void addPlayer(String name, int g)
    {
        groups[g].members.add(name);
    }
    
    public void removePLayer(int id, int g)
    {
        groups[g].members.remove(id);
        if (g == 0 && !groups[g].members.contains(tile.mainOwner)) this.addPlayer(tile.mainOwner, g); 
    }
    
    public String getPlayer(int id, int g)
    {
        if (id < 0 || id >= groups[g].members.size()) return "";
        else return groups[g].members.get(id);
    }
    
    public int getProtection(int i, int g)
    {
        return groups[g].area[i];
    }
    
    public void setProtection(int i, int g, int v)
    {
        int x = px + i % 8;
        int z = pz + i / 8;
        if (checkOccupied(x, z)) groups[g].area[i] = (byte)(v % 4);
        else groups[g].area[i] = (byte)0;
    }
    
    public boolean checkOccupied(int x, int z)
    {
        ArrayList<IAreaConfig> list = AreaProtect.instance.loadedSS.get(tile.getWorld().provider.getDimension());
    	if (list == null) return true;
        for (IAreaConfig cfg : list)
        	if (cfg != this && cfg.isChunkProtected(x, z)) return false;
        return true;
    }
    
    public int getGroupSize(int g)
    {
        return groups[g].members.size();
    }
    
    public boolean isPlayerOwner(String name)
    {
        return tile.mainOwner.equals(name) || groups[0].members.contains(name);
    }

	@Override
	public int[][] getProtectedChunks(String obj) 
	{
		if (!tile.enabledC) return new int[0][];
        if (this.isPlayerOwner(obj)) return new int[0][];
        Group group = groups[0];
        for (int i = 1; i < groups.length; i++)
	        if (groups[i].members.contains(obj)) {
	            group = groups[i];
	            break;
	        }
        int[][] buff = new int[64][];
        int n = 0;
        byte v;
        for (int i = 0; i < 64; i++)
        	if ((v = group.area[i]) != 0) 
        		buff[n++] = new int[]{px + i % 8, pz + i / 8, v};
        int[][] ret = new int[n][];
        System.arraycopy(buff, 0, ret, 0, n);
        return ret;
	}

	public void updateChunkLoading()
	{
		ArrayList<ChunkCoordIntPair> toAdd;
		ArrayList<ChunkCoordIntPair> toRemove;
		if (tile.enabledC) {
			toAdd = new ArrayList<ChunkCoordIntPair>();
			toRemove = new ArrayList<ChunkCoordIntPair>();
			for (int i = 0; i < 64; i++) {
				if ((loadedChunks >> i & 1) != 0)
					if (toAdd.size() < AreaProtect.maxChunksPBlock) toAdd.add(new ChunkCoordIntPair(i % 8 + px, i / 8 + pz));
					else loadedChunks &= (~1L) << i;
			}
			if (chunkTicket == null) {
				if (!toAdd.isEmpty()) AreaProtect.instance.supplyTicket(this, tile.getWorld());
				if (chunkTicket == null) {
					update = false;
					chunksLoaded = false;
					return;
				}
			}
			chunksLoaded = !toAdd.isEmpty();
			for (ChunkCoordIntPair c : chunkTicket.getChunkList()) {
				if (this.isChunkLoaded(c.chunkXPos, c.chunkZPos)) chunksLoaded |= !toAdd.remove(c);
				else toRemove.add(c);
			}
			for (ChunkCoordIntPair c : toRemove) ForgeChunkManager.unforceChunk(chunkTicket, c);
			for (ChunkCoordIntPair c : toAdd) ForgeChunkManager.forceChunk(chunkTicket, c);
		} else {
			if (this.chunkTicket != null) {
				toRemove = new ArrayList<ChunkCoordIntPair>(this.chunkTicket.getChunkList());
				for (ChunkCoordIntPair c : toRemove) ForgeChunkManager.unforceChunk(chunkTicket, c);
			}
			chunksLoaded = false;
		}
		update = false;
	}
	
	@Override
	public int[] getPosition() {
		return new int[]{tile.getPos().getX(), tile.getPos().getY(), tile.getPos().getZ(), tile.getWorld().provider.getDimension()};
	}

	public long loadedChunks;
	public boolean update;
	private boolean chunksLoaded;
	
	@Override
	public void setTicket(Ticket t) {
		this.chunkTicket = t;
		this.update = true;
	}

	@Override
	public Ticket getTicket() 
	{
		return this.chunkTicket;
	}

	@Override
	public boolean isChunkLoaded(int cx, int cz) 
	{
		int x = cx - px, z = cz - pz;
		return tile.enabledC && x >= 0 && z >= 0 && x < 8 && z < 8 && (loadedChunks >> (x + 8 * z) & 1) != 0;
	}
    
}
