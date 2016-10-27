package cd4017be.automation.TileEntity;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

import cd4017be.api.automation.AreaProtect;
import cd4017be.api.automation.PipeEnergy;
import cd4017be.automation.AreaConfig;
import cd4017be.automation.Config;
import cd4017be.lib.Gui.DataContainer;
import cd4017be.lib.Gui.DataContainer.IGuiData;
import cd4017be.lib.templates.AutomatedTile;
import cd4017be.lib.util.ItemFluidUtil;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.world.WorldServer;

/**
 *
 * @author CD4017BE
 */
public class SecuritySys extends AutomatedTile implements IGuiData {

	public AreaConfig prot = new AreaConfig(this);
	public HashMap<String, ItemStack[]> itemStorage = new HashMap<String, ItemStack[]>();
	public boolean enabled = false;
	public boolean enabledC = false;
	private byte perm = 0;
	public int netI0, netI1, netI2, netI3;
	public float netF0;
	public String mainOwner = "";
	
	public SecuritySys()
	{
		energy = new PipeEnergy(Config.Umax[1], Config.Rcond[1]);
		/**
		 * long:
		 * int: voltage, energyUse, rstMode{1:ProtSrc, 2:ProtInv, 4:LoadSrc, 8:LoadInv}, energyUseC
		 * float: storage
		 */
	}
	
	@Override
	public void update() 
	{
		super.update();
		if (worldObj.isRemote) return;
		float e = (energy.Ucap * energy.Ucap - (float)(netI0 * netI0)) * 0.001F;
		if (e > 0) {
			energy.Ucap = netI0;
			if (netF0 + e < Config.Ecap[0])
			{
				netF0 += e;
			} else
			{
				e -= Config.Ecap[0] - netF0;
				netF0 = Config.Ecap[0];
				energy.addEnergy(e * 1000F);
			}
		}
		if (perm == 1) netI2 &= 12;
		else if (perm == 2) netI2 &= 3;
		else if (perm == 3) netI2 = 0;
		boolean rst = worldObj.isBlockPowered(getPos());
		if ((netI2 & 2) == 0) enabled = (netI2 & 1) != 0;
		else enabled = (netI2 & 1) != 0 ^ rst;
		if (netF0 < netI1) enabled = false;
		else if (enabled) netF0 -= netI1;
		boolean enabledCl = enabledC;
		if ((netI2 & 8) == 0) enabledC = (netI2 & 4) != 0;
		else enabledC = (netI2 & 4) != 0 ^ rst;
		if (netF0 < netI3 || (!enabledCl && netF0 < Config.Ecap[0] / 2)) enabledC = false;
		else if (enabledC) netF0 -= netI3;
		prot.update |= enabledC ^ enabledCl;
		prot.tick();
	}
	
	public int getStorageScaled(int s)
	{
		return ((int)netF0 * s) / Config.Ecap[0];
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) 
	{
		super.readFromNBT(nbt);
		prot.readFromNbt(nbt);
		netF0 = nbt.getFloat("storage");
		netI0 = nbt.getInteger("voltage");
		netI2 = nbt.getByte("mode");
		perm = nbt.getByte("access");
		mainOwner = nbt.getString("owner");
		netI1 = (int)prot.getEnergyCost();
		netI3 = (int)prot.getLoadEnergyCost();
		prot.px = ((this.pos.getX() + 8) >> 4) - 4;
		prot.pz = ((this.pos.getZ() + 8) >> 4) - 4;
		itemStorage.clear();
		NBTTagCompound stor = nbt.getCompoundTag("itemBuff");
		for (String name : stor.getKeySet()) {
			ItemStack[] items = new ItemStack[40];
			ItemFluidUtil.loadItems(stor.getTagList(name, 10), items);
			itemStorage.put(name, items);
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) 
	{
		energy.writeToNBT(nbt, "Wire");
		prot.writeToNbt(nbt);
		nbt.setFloat("storage", netF0);
		nbt.setInteger("voltage", netI0);
		nbt.setByte("mode", (byte)netI2);
		nbt.setByte("access", perm);
		nbt.setString("owner", mainOwner);
		NBTTagCompound stor = new NBTTagCompound();
		for (Entry<String, ItemStack[]> e : itemStorage.entrySet())
			stor.setTag(e.getKey(), ItemFluidUtil.saveItems(e.getValue()));
		nbt.setTag("itemBuff", stor);
		return super.writeToNBT(nbt);
	}

	@Override
	public boolean onActivated(EntityPlayer player, EnumHand hand, ItemStack item, EnumFacing s, float X, float Y, float Z) 
	{
		if (!prot.isPlayerOwner(player.getName())){
			if (!worldObj.isRemote) player.addChatMessage(new TextComponentString("You are not given the necessary rights to use this!"));
			return true;
		} else if (player.isSneaking() && player.getHeldItemMainhand() == null) {
			if (!itemStorage.isEmpty()) {
				//TODO drop confiscated items;
			}
			this.getBlockType().dropBlockAsItem(worldObj, getPos(), worldObj.getBlockState(pos), 0);
			worldObj.setBlockToAir(getPos());
			return true;
		} else return super.onActivated(player, hand, item, s, X, Y, Z);
	}

	@Override
	public SPacketUpdateTileEntity getUpdatePacket() 
	{
		NBTTagCompound nbt = new NBTTagCompound();
		prot.writeToNbt(nbt);
		return new SPacketUpdateTileEntity(getPos(), -1, nbt);
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) 
	{
		NBTTagCompound nbt = pkt.getNbtCompound();
		prot.readFromNbt(nbt);
		netI1 = (int)prot.getEnergyCost();
		netI3 = (int)prot.getLoadEnergyCost();
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
				netI0 -= btn == 0 ? 1 : btn == 1 ? 10 : btn == 2 ? 100 : 1000;
				if (netI0 < 0) netI0 = 0;
			} else if (btn >= 4 && btn < 8) {
				netI0 += btn == 4 ? 1 : btn == 5 ? 10 : btn == 6 ? 100 : 1000;
				if (netI0 > energy.Umax) netI0 = energy.Umax;
			} else if (btn == 8) {
				netI2 = (netI2 & 12) | (netI2 + 1 & 3);
			} else if (btn == 9) {
				netI2 = (netI2 + 4 & 12) | (netI2 & 3);
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
				netI3 = (int)prot.getLoadEnergyCost();
				prot.update = true;
			} else {
				prot.setProtection(i, g, prot.getProtection(i, g) + 1);
				netI1 = (int)prot.getEnergyCost();
			}
			update = true;
		}
		if (update) this.markUpdate();
	}
	
	@Override
	public void onPlaced(EntityLivingBase entity, ItemStack item)  
	{
		this.mainOwner = entity.getName();
		prot.addPlayer(mainOwner, 0);
		if ((AreaProtect.permissions > 0 && AreaProtect.chunkloadPerm > 0) || !(worldObj instanceof WorldServer)) return;
		PlayerList manager = ((WorldServer)worldObj).getMinecraftServer().getPlayerList();
		boolean admin = entity instanceof EntityPlayer && manager.canSendCommands(((EntityPlayer)entity).getGameProfile());
		if (AreaProtect.permissions < 0 || (!admin && AreaProtect.permissions == 0)) perm |= 1;
		if (AreaProtect.chunkloadPerm < 0 || (!admin && AreaProtect.chunkloadPerm == 0)) perm |= 2;
		if (perm == 0) return;
		if (perm == 3) worldObj.setBlockToAir(getPos());
		if (entity instanceof EntityPlayer) ((EntityPlayer)entity).addChatMessage(new TextComponentString(perm == 3 ? "You are not allowed to use this device on this server !" : perm == 2 ? "Chunk loading functionality of this device disabled for you !" : "Chunk protection functionality of this device disabled for you !"));
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

	@Override
	public void initContainer(DataContainer container) {
		container.refInts = new int[5];
	}

	@Override
	public int[] getSyncVariables() {
		return new int[]{netI0, netI1, netI2, netI3, Float.floatToIntBits(netF0)};
	}

	@Override
	public void setSyncVariable(int i, int v) {
		switch(i) {
		case 0: netI0 = v; break;
		case 1: netI1 = v; break;
		case 2: netI2 = v; break;
		case 3: netI3 = v; break;
		case 4: netF0 = Float.intBitsToFloat(v); break;
		}
	}

}
