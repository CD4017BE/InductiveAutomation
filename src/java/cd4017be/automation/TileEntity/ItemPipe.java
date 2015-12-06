/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cd4017be.automation.TileEntity;

import java.util.ArrayList;

import cd4017be.api.automation.IItemPipeCon;
import cd4017be.automation.Block.BlockItemPipe;
import cd4017be.automation.Item.ItemItemUpgrade;
import cd4017be.automation.Item.PipeUpgradeItem;
import cd4017be.lib.BlockItemRegistry;
import cd4017be.lib.TileEntityData;
import cd4017be.lib.templates.AutomatedTile;
import cd4017be.lib.templates.IPipe;
import cd4017be.lib.templates.Inventory;
import cd4017be.lib.templates.Inventory.Component;
import cd4017be.lib.util.Utils;
import cd4017be.lib.util.Utils.ItemType;
import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

/**
 *
 * @author CD4017BE
 */
public class ItemPipe extends AutomatedTile implements IPipe, ISidedInventory
{
    //[1<<0 .. 1<<5]: dir[0..5] -> 1: out;
    //[1<<8 .. 1<<13]: dir[0..5] -> 1: in;
    //0x0040: has_out; 0x4000: has_in
    private short flow;
    private boolean updateCon = true;
    private PipeUpgradeItem filter = null;
    private Cover cover = null;
    
    public ItemPipe()
    {
        inventory = new Inventory(this, 1, new Component(0, 1, 0));
        netData = new TileEntityData(1, 0, 0, 1);
        netData.longs[0] = 0x555;
    }

    @Override
    public void updateEntity() 
    {
        if (worldObj.isRemote) return;
        int type = this.worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
        if (updateCon) this.updateConnections(type);
        if (getFlowBit(6) && getFlowBit(14)) this.transferItem(type);
    }
    
    private void updateConnections(int type) 
    {
        ForgeDirection dir;
        TileEntity te;
        boolean lHasOut = getFlowBit(6);
        boolean lHasIn = getFlowBit(14);
        boolean nHasOut = false;
        boolean nHasIn = false;
        boolean lDirOut;
        boolean lDirIn;
        ArrayList<ItemPipe> updateList = new ArrayList<ItemPipe>();
        short lFlow = flow;
        for (int i = 0; i < 6; i++) {
            lDirOut = this.getFlowBit(i);
            lDirIn = this.getFlowBit(i | 8);
            if (lDirOut && lDirIn) continue;
            dir = ForgeDirection.getOrientation(i);
            te = worldObj.getTileEntity(xCoord + dir.offsetX, yCoord + dir.offsetY, zCoord + dir.offsetZ);
            if (te == null) {
                setFlowBit(i, false);
                setFlowBit(i | 8, false);
            } else if (te instanceof ItemPipe) {
                ItemPipe pipe = (ItemPipe)te;
                boolean pHasOut = pipe.getFlowBit(6);
                boolean pHasIn = pipe.getFlowBit(14);
                boolean pDirOut = pipe.getFlowBit(i ^ 1);
                boolean pDirIn = pipe.getFlowBit((i ^ 1) | 8);
                boolean nDirOut = pHasOut && !pDirOut;
                boolean nDirIn = pHasIn && !pDirIn;
                if (pDirOut && pDirIn) {
                    nDirOut = true;
                    nDirIn = true;
                } else if (nDirOut && nDirIn) {
                    boolean s = lHasIn ^ lHasOut;
                    nDirOut = s && lHasIn && !lDirIn;
                    nDirIn = s && lHasOut && !lDirOut;
                }
                this.setFlowBit(i, nDirOut);
                this.setFlowBit(i | 8, nDirIn);
                nHasIn |= nDirIn && !nDirOut;
                nHasOut |= nDirOut && !nDirIn;
                updateList.add(pipe);
            } else if (te instanceof IItemPipeCon) {
            	byte c = ((IItemPipeCon)te).getItemConnectType(i^1);
            	if (c == 1) {
            		setFlowBit(i, false);
                    setFlowBit(i | 8, true);
                    nHasIn = true;
            	} else if (c == 2) {
            		setFlowBit(i, true);
                    setFlowBit(i | 8, false);
                    nHasOut = true;
            	} else {
            		setFlowBit(i, false);
                    setFlowBit(i | 8, false);
            	}
            } else if (te instanceof IInventory) {
                if (type == BlockItemPipe.ID_Transport) {
                    setFlowBit(i, false);
                    setFlowBit(i | 8, false);
                } else if (type == BlockItemPipe.ID_Extraction) {
                    setFlowBit(i, false);
                    setFlowBit(i | 8, true);
                    nHasIn = true;
                } else if (type == BlockItemPipe.ID_Injection) {
                    setFlowBit(i, true);
                    setFlowBit(i | 8, false);
                    nHasOut = true;
                }
            } else {
                setFlowBit(i, false);
                setFlowBit(i | 8, false);
            }
        }
        setFlowBit(6, nHasOut);
        setFlowBit(14, nHasIn);
        if (flow != lFlow) {
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
            for (ItemPipe pipe : updateList) {
                pipe.onNeighborBlockChange(this.getBlockType());
            }
        }
        updateCon = false;
    }
    
    private void transferItem(int type)
    {
        boolean rs = worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord);
        Inventory[] flowList = new Inventory[5];
        int n = 0;
        ForgeDirection dir;
        boolean dirOut;
        boolean dirIn;
        if (type == BlockItemPipe.ID_Extraction) {
            for (int i = 0; i < 6; i++) {
                dirOut = this.getFlowBit(i);
                dirIn = this.getFlowBit(i | 8);
                if (!dirIn ^ dirOut) continue;
                dir = ForgeDirection.getOrientation(i);
                TileEntity te = worldObj.getTileEntity(xCoord + dir.offsetX, yCoord + dir.offsetY, zCoord + dir.offsetZ);
                if (te == null || !(te instanceof IInventory)) updateCon = true;
                else if (te instanceof ItemPipe) {
                	if (dirOut && n < 5) flowList[n++] = ((ItemPipe)te).inventory;
                } else {
                	if (dirOut) updateCon = true;
                    IInventory container = (IInventory)te;
                    if (filter == null) {
                    	if (inventory.items[0] == null) {
                    		inventory.items[0] = Utils.drainStack(container, i^1, Utils.accessibleSlots(container, i^1), new ItemType(), 64);
                        } else {
                            ItemStack item = Utils.drainStack(container, i^1, Utils.accessibleSlots(container, i^1), new ItemType(inventory.items[0]), inventory.items[0].getMaxStackSize() - inventory.items[0].stackSize);
                            if (item != null) {
                            	item.stackSize += inventory.items[0] == null ? 0 : inventory.items[0].stackSize;
                            	inventory.items[0] = item;
                            }
                        }
                    } else {
                        int[] s = Utils.accessibleSlots(container, i^1);
                        ItemStack item = filter.getExtractItem(inventory.items[0], container, s, rs);
                        if (item != null) {
                            if (inventory.items[0] == null) inventory.items[0] = Utils.drainStack(container, i^1, s, new ItemType(item), item.stackSize);
                            else {
                                item = Utils.drainStack(container, i^1, s, new ItemType(item), Math.min(item.stackSize, inventory.items[0].getMaxStackSize() - inventory.items[0].stackSize));
                                if (item != null) {
                                	item.stackSize += inventory.items[0] == null ? 0 : inventory.items[0].stackSize;
                                	inventory.items[0] = item;
                                }
                            }
                        }
                    }
                }
            }
        } else if (type == BlockItemPipe.ID_Injection && inventory.items[0] != null) {
            for (int i = 0; i < 6; i++) {
                dirOut = this.getFlowBit(i);
                dirIn = this.getFlowBit(i | 8);
                if (dirOut && !dirIn) {
                    dir = ForgeDirection.getOrientation(i);
                    TileEntity te = worldObj.getTileEntity(xCoord + dir.offsetX, yCoord + dir.offsetY, zCoord + dir.offsetZ);
                    if (te == null || !(te instanceof IInventory)) updateCon = true;
                    else if (te instanceof ItemPipe) {
                        if (n < 5) flowList[n++] = ((ItemPipe)te).inventory;
                    } else {
                        IInventory container = (IInventory)te;
                        if (filter == null) {
                            ItemStack[] item = Utils.fill(container, i^1, Utils.accessibleSlots(container, i^1), this.decrStackSize(0, 64));
                            if (item.length > 0) {
                            	if (inventory.items[0] == null) inventory.items[0] = item[0];
                                else inventory.items[0].stackSize += item[0].stackSize;
                            }
                        } else {
                            int[] s = Utils.accessibleSlots(container, i^1);
                            int m = filter.getInsertAmount(inventory.items[0], container, s, rs);
                            if (m > 0) {
                                ItemStack[] item = Utils.fill(container, i^1, Utils.accessibleSlots(container, i^1), inventory.decrStackSize(0, m));
                                if (item.length > 0) {
                                    if (inventory.items[0] == null) inventory.items[0] = item[0];
                                    else inventory.items[0].stackSize += item[0].stackSize;
                                }
                            }
                        }
                        if (inventory.items[0] == null) return;
                    }
                }
            }
        } else if (type == BlockItemPipe.ID_Transport && inventory.items[0] != null) {
            for (int i = 0; i < 6; i++) {
                dirOut = this.getFlowBit(i);
                dirIn = this.getFlowBit(i | 8);
                if (dirOut && !dirIn) {
                    dir = ForgeDirection.getOrientation(i);
                    TileEntity te = worldObj.getTileEntity(xCoord + dir.offsetX, yCoord + dir.offsetY, zCoord + dir.offsetZ);
                    if (te != null && te instanceof ItemPipe && n < 5) flowList[n++] = ((ItemPipe)te).inventory;
                    else updateCon = true;
                }
            }
        }
        if (n == 0 || inventory.items[0] == null || (type == BlockItemPipe.ID_Injection && filter != null && !filter.transferItem(inventory.items[0]))) return;
        for (int i = 0; i < n; i++) {
        	Inventory dst = flowList[i];
            if (dst.items[0] == null) {
            	dst.items[0] = inventory.items[0];
                inventory.items[0] = null;
                return;
            } else if (Utils.itemsEqual(dst.items[0], inventory.items[0])) {
                ItemStack item = inventory.decrStackSize(0, dst.items[0].getMaxStackSize() - dst.items[0].stackSize);
                if (item != null) dst.items[0].stackSize += item.stackSize;
                if (inventory.items[0] == null) return;
            }
        }
    }
    
    @Override
    public void onNeighborBlockChange(Block b) 
    {
        updateCon = true;
    }

    @Override
    public boolean onActivated(EntityPlayer player, int s, float X, float Y, float Z) 
    {
        ItemStack item = player.getCurrentEquippedItem();
        int type = this.worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
        boolean canF = type == BlockItemPipe.ID_Extraction || type == BlockItemPipe.ID_Injection;
        if (player.isSneaking() && item == null) {
            if (worldObj.isRemote) return true;
            if (cover != null) {
                player.setCurrentItemOrArmor(0, cover.item);
                cover = null;
                worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
                return true;
            }
            X -= 0.5F;
            Y -= 0.5F;
            Z -= 0.5F;
            float dx = Math.abs(X);
            float dy = Math.abs(Y);
            float dz = Math.abs(Z);
            if (dy > dz && dy > dx) s = Y < 0 ? 0 : 1;
            else if (dz > dx) s = Z < 0 ? 2 : 3;
            else s = X < 0 ? 4 : 5;
            boolean lock = !(this.getFlowBit(s) && this.getFlowBit(s | 8));
            this.setFlowBit(s, lock);
            this.setFlowBit(s | 8, lock);
            this.onNeighborBlockChange(this.getBlockType());
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
            TileEntity te = Utils.getTileOnSide(this, (byte)s);
            if (te != null && te instanceof ItemPipe) {
                ItemPipe pipe = (ItemPipe)te;
                pipe.setFlowBit(s^1, lock);
                pipe.setFlowBit(s^1 | 8, lock);
                pipe.onNeighborBlockChange(this.getBlockType());
                worldObj.markBlockForUpdate(pipe.xCoord, pipe.yCoord, pipe.zCoord);
            }
            return true;
        } else if (!player.isSneaking() && item == null && filter != null) {
            if (worldObj.isRemote) return true;
            item = BlockItemRegistry.stack("item.itemUpgrade", 1);
            item.stackTagCompound = PipeUpgradeItem.save(filter);
            filter = null;
            player.setCurrentItemOrArmor(0, item);
            return true;
        } else if (!player.isSneaking() && cover == null && item != null && (cover = Cover.create(item)) != null) {
            if (worldObj.isRemote) return true;
            item.stackSize--;
            if (item.stackSize <= 0) item = null;
            player.setCurrentItemOrArmor(0, item);
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
            return true;
        } else if (filter == null && canF && item != null && item.getItem() instanceof ItemItemUpgrade && item.stackTagCompound != null) {
            if (worldObj.isRemote) return true;
            filter = PipeUpgradeItem.load(item.stackTagCompound);
            item.stackSize--;
            if (item.stackSize <= 0) item = null;
            player.setCurrentItemOrArmor(0, item);
            return true;
        } else return false;
    }
    
    public boolean getFlowBit(int b)
    {
        return (flow & (1 << b)) != 0;
    }
    
    protected void setFlowBit(int b, boolean v)
    {
        if (v) flow |= 1 << b;
        else flow &= ~(1 << b);
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) 
    {
        super.writeToNBT(nbt);
        nbt.setShort("flow", flow);
        if (filter != null) nbt.setTag("filter", PipeUpgradeItem.save(filter));
        if (cover != null) cover.write(nbt, "cover");
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) 
    {
        super.readFromNBT(nbt);
        flow = nbt.getShort("flow");
        if (nbt.hasKey("filter")) filter = PipeUpgradeItem.load(nbt.getCompoundTag("filter"));
        cover = Cover.read(nbt, "cover");
        updateCon = true;
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) 
    {
        flow = pkt.func_148857_g().getShort("flow");
        cover = Cover.read(pkt.func_148857_g(), "cover");
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    @Override
    public Packet getDescriptionPacket() 
    {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setShort("flow", flow);
        if (cover != null) cover.write(nbt, "cover");
        return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, -1, nbt);
    }
    
    @Override
    public int textureForSide(byte s) 
    {
        TileEntity p = Utils.getTileOnSide(this, s);
        boolean b0 = getFlowBit(s);
        boolean b1 = getFlowBit(s | 8);
        if (!(b0 && b1) && p != null && p instanceof IInventory)
        {
            return (b0?1:0)|(b1?2:0);
        } else return -1;
    }

    @Override
    public void breakBlock() 
    {
        super.breakBlock();
        if (filter != null) {
            ItemStack item = BlockItemRegistry.stack("item.itemUpgrade", 1);
            item.stackTagCompound = PipeUpgradeItem.save(filter);
            filter = null;
            EntityItem entity = new EntityItem(worldObj, xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D, item);
            worldObj.spawnEntityInWorld(entity);
        }
        if (cover != null) {
            EntityItem entity = new EntityItem(worldObj, xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D, cover.item);
            cover = null;
            worldObj.spawnEntityInWorld(entity);
        }
    }

    @Override
    public Cover getCover() 
    {
        return cover;
    }
}
