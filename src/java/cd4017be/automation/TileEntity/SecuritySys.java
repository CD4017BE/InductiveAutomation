/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cd4017be.automation.TileEntity;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

import cd4017be.api.automation.AreaProtect;
import cd4017be.api.automation.IEnergy;
import cd4017be.api.automation.PipeEnergy;
import cd4017be.automation.AreaConfig;
import cd4017be.automation.Config;
import cd4017be.lib.TileEntityData;
import cd4017be.lib.templates.AutomatedTile;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.WorldServer;

/**
 *
 * @author CD4017BE
 */
public class SecuritySys extends AutomatedTile implements IEnergy
{
    public AreaConfig prot = new AreaConfig(this);
    public HashMap<String, ItemStack[]> itemStorage = new HashMap<String, ItemStack[]>();
    public boolean enabled = false;
    public boolean enabledC = false;
    private byte perm = 0;
    
    public String mainOwner = "";
    
    public SecuritySys()
    {
        energy = new PipeEnergy(Config.Umax[1], Config.Rcond[1]);
        /**
         * long:
         * int: voltage, energyUse, rstMode{1:ProtSrc, 2:ProtInv, 4:LoadSrc, 8:LoadInv}, energyUseC
         * float: storage
         */
        netData = new TileEntityData(0, 4, 1, 0);
    }
    
    @Override
    public void update() 
    {
    	super.update();
        if (worldObj.isRemote) return;
        double e = (energy.Ucap * energy.Ucap - (float)(netData.ints[0] * netData.ints[0])) * 0.001F;
        if (e > 0) {
            energy.Ucap = netData.ints[0];
            if (netData.floats[0] + e < Config.Ecap[0])
            {
                netData.floats[0] += e;
            } else
            {
                e -= Config.Ecap[0] - netData.floats[0];
                netData.floats[0] = Config.Ecap[0];
                energy.addEnergy(e * 1000F);
            }
        }
        if (perm == 1) netData.ints[2] &= 12;
        else if (perm == 2) netData.ints[2] &= 3;
        else if (perm == 3) netData.ints[2] = 0;
        boolean rst = worldObj.isBlockPowered(getPos());
        if ((netData.ints[2] & 2) == 0) enabled = (netData.ints[2] & 1) != 0;
        else enabled = (netData.ints[2] & 1) != 0 ^ rst;
        if (netData.floats[0] < netData.ints[1]) enabled = false;
        else if (enabled) netData.floats[0] -= netData.ints[1];
        boolean enabledCl = enabledC;
        if ((netData.ints[2] & 8) == 0) enabledC = (netData.ints[2] & 4) != 0;
        else enabledC = (netData.ints[2] & 4) != 0 ^ rst;
        if (netData.floats[0] < netData.ints[3] || (!enabledCl && netData.floats[0] < Config.Ecap[0] / 2)) enabledC = false;
        else if (enabledC) netData.floats[0] -= netData.ints[3];
        prot.update |= enabledC ^ enabledCl;
        prot.tick();
    }
    
    public int getStorageScaled(int s)
    {
        return ((int)netData.floats[0] * s) / Config.Ecap[0];
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) 
    {
        super.readFromNBT(nbt);
        prot.readFromNbt(nbt);
        netData.floats[0] = nbt.getFloat("storage");
        netData.ints[0] = nbt.getInteger("voltage");
        netData.ints[2] = nbt.getByte("mode");
        perm = nbt.getByte("access");
        mainOwner = nbt.getString("owner");
        netData.ints[1] = (int)prot.getEnergyCost();
        netData.ints[3] = (int)prot.getLoadEnergyCost();
        prot.px = ((this.pos.getX() + 8) >> 4) - 4;
        prot.pz = ((this.pos.getZ() + 8) >> 4) - 4;
        itemStorage.clear();
        NBTTagCompound stor = nbt.getCompoundTag("itemBuff");
        for (String name : stor.getKeySet()) {
            itemStorage.put(name, this.readItemsFromNBT(stor, name, 40));
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) 
    {
        super.writeToNBT(nbt);
        energy.writeToNBT(nbt, "Wire");
        prot.writeToNbt(nbt);
        nbt.setFloat("storage", netData.floats[0]);
        nbt.setInteger("voltage", netData.ints[0]);
        nbt.setByte("mode", (byte)netData.ints[2]);
        nbt.setByte("access", perm);
        nbt.setString("owner", mainOwner);
        NBTTagCompound stor = new NBTTagCompound();
        for (Entry<String, ItemStack[]> e : itemStorage.entrySet()) {
            this.writeItemsToNBT(stor, e.getKey(), e.getValue());
        }
        nbt.setTag("itemBuff", stor);
    }

    @Override
    public boolean onActivated(EntityPlayer player, EnumFacing s, float X, float Y, float Z) 
    {
        if (!prot.isPlayerOwner(player.getName())){
            if (!worldObj.isRemote) player.addChatMessage(new ChatComponentText("You are not given the necessary rights to use this!"));
            return true;
        } else if (player.isSneaking() && player.getCurrentEquippedItem() == null) {
            if (!itemStorage.isEmpty()) {
            	//TODO drop confiscated items;
            }
        	this.getBlockType().dropBlockAsItem(worldObj, getPos(), worldObj.getBlockState(pos), 0);
            worldObj.setBlockToAir(getPos());
            return true;
        } else return super.onActivated(player, s, X, Y, Z);
    }

    @Override
    public Packet getDescriptionPacket() 
    {
        NBTTagCompound nbt = new NBTTagCompound();
        prot.writeToNbt(nbt);
        return new S35PacketUpdateTileEntity(getPos(), -1, nbt);
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) 
    {
        NBTTagCompound nbt = pkt.getNbtCompound();
        prot.readFromNbt(nbt);
        netData.ints[1] = (int)prot.getEnergyCost();
        netData.ints[3] = (int)prot.getLoadEnergyCost();
    }

    @Override
    public void onPlayerCommand(PacketBuffer dis, EntityPlayerMP player) throws IOException 
    {
        boolean update = false;
        if (!prot.isPlayerOwner(player.getName())) return;
        byte cmd = dis.readByte();
        if (cmd == 0) {
            byte btn = dis.readByte();
            if (btn >= 0 && btn < 4) {
                netData.ints[0] -= btn == 0 ? 1 : btn == 1 ? 10 : btn == 2 ? 100 : 1000;
                if (netData.ints[0] < 0) netData.ints[0] = 0;
            } else if (btn >= 4 && btn < 8) {
                netData.ints[0] += btn == 4 ? 1 : btn == 5 ? 10 : btn == 6 ? 100 : 1000;
                if (netData.ints[0] > energy.Umax) netData.ints[0] = energy.Umax;
            } else if (btn == 8) {
                netData.ints[2] = (netData.ints[2] & 12) | (netData.ints[2] + 1 & 3);
            } else if (btn == 9) {
            	netData.ints[2] = (netData.ints[2] + 4 & 12) | (netData.ints[2] & 3);
            }
        } else if (cmd == 1) {
            byte g = dis.readByte();
            byte p = dis.readByte();
            prot.removePLayer(p, g);
            update = true;
        } else if (cmd == 2) {
            byte g = dis.readByte();
            String name = dis.readStringFromBuffer(64);
            prot.addPlayer(name, g);
            update = true;
        } else if (cmd == 3) {
            int g = dis.readByte() % 4;
            int i = dis.readByte() % 64;
            if (g < 0) {
            	prot.loadedChunks ^= 1L << i;
            	netData.ints[3] = (int)prot.getLoadEnergyCost();
            	prot.update = true;
            } else {
            	prot.setProtection(i, g, prot.getProtection(i, g) + 1);
                netData.ints[1] = (int)prot.getEnergyCost();
            }
            update = true;
        }
        if (update) worldObj.markBlockForUpdate(getPos());
    }
    
    @Override
    public void onPlaced(EntityLivingBase entity, ItemStack item)  
    {
        this.mainOwner = entity.getName();
        prot.addPlayer(mainOwner, 0);
        if ((AreaProtect.permissions > 0 && AreaProtect.chunkloadPerm > 0) || !(worldObj instanceof WorldServer)) return;
        ServerConfigurationManager manager = MinecraftServer.getServer().getConfigurationManager();
        boolean admin = entity instanceof EntityPlayer && manager.canSendCommands(((EntityPlayer)entity).getGameProfile());
        if (AreaProtect.permissions < 0 || (!admin && AreaProtect.permissions == 0)) perm |= 1;
        if (AreaProtect.chunkloadPerm < 0 || (!admin && AreaProtect.chunkloadPerm == 0)) perm |= 2;
        if (perm == 0) return;
        if (perm == 3) worldObj.setBlockToAir(getPos());
        if (entity instanceof EntityPlayer) ((EntityPlayer)entity).addChatMessage(new ChatComponentText(perm == 3 ? "You are not allowed to use this device on this server !" : perm == 2 ? "Chunk loading functionality of this device disabled for you !" : "Chunk protection functionality of this device disabled for you !"));
    }
    
    @Override
    public void onChunkUnload() 
    {
        onUnload();
        super.onChunkUnload();
        if (!this.prot.isChunkLoaded(pos.getX() >> 4, pos.getZ() >> 4)) AreaProtect.instance.removeChunkLoader(prot);
    }

    @Override
    public void invalidate() 
    {
        onUnload();
        super.invalidate();
        AreaProtect.instance.removeChunkLoader(prot);
    }
    
    private void onUnload() 
    {
    	AreaProtect.instance.unloadSecuritySys(prot);
    }

    @Override
    public void validate() 
    {
        prot.px = ((this.pos.getX() + 8) >> 4) - 4;
        prot.pz = ((this.pos.getZ() + 8) >> 4) - 4;
        AreaProtect.instance.loadSecuritySys(prot);
        super.validate();
    }
    
}
