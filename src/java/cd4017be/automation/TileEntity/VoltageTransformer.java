/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cd4017be.automation.TileEntity;

import net.minecraft.util.ITickable;

import java.io.IOException;
import java.util.List;

import cd4017be.api.automation.IEnergy;
import cd4017be.api.automation.PipeEnergy;
import cd4017be.lib.ModTileEntity;
import cd4017be.lib.TileContainer;
import cd4017be.lib.util.Utils;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.ICrafting;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;

/**
 *
 * @author CD4017BE
 */
public class VoltageTransformer extends ModTileEntity implements IEnergy, ITickable
{
    public int faktor = 10;
    public byte ctrMode;
    public double Eflow;
    
    @Override
    public void update() 
    {
        if(worldObj.isRemote) return;
        byte s = this.getOrientation();
        if((ctrMode & 2) == 0 ^ ((ctrMode & 1) == 0 ? false : worldObj.isBlockPowered(getPos()))) return;
        TileEntity te = Utils.getTileOnSide(this, (byte)(s ^ 1));
        PipeEnergy wireLV = te != null && te instanceof IEnergy ? ((IEnergy)te).getEnergy((byte)(s)) : null;
        te = Utils.getTileOnSide(this, (byte)(s));
        PipeEnergy wireHV = te != null && te instanceof IEnergy ? ((IEnergy)te).getEnergy((byte)(s ^ 1)) : null;
        if (wireLV != null && wireHV != null && wireLV.Umax > 0 && wireHV.Umax > 0)
        {
            float x = (float)(faktor * faktor) * 0.01F;
            float f = 1 / (1 + x);
            double E0 = wireLV.Ucap * wireLV.Ucap;
            double E1 = wireHV.Ucap * wireHV.Ucap;
            double dE0 = (E0 + E1) * f - E0;
            double dE1 = Eflow + dE0;
            Eflow = dE0 / x;
            if (E0 + dE1 < 0) dE1 = -E0;
            if (E1 - dE1 < 0) dE1 = E1;
            wireLV.addEnergy(dE1);
            wireHV.addEnergy(-dE1);
        }
    }

    @Override
    public PipeEnergy getEnergy(byte side) 
    {
        return (side & 0xe) == (this.getOrientation() & 0xe) ? PipeEnergy.empty : null;
    }
    
    @Override
    public void onPlayerCommand(PacketBuffer dis, EntityPlayerMP player) throws IOException 
    {
        faktor = dis.readInt();
        ctrMode = dis.readByte();
        if (faktor < 10) faktor = 10;
        if (faktor > 200) faktor = 200;
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) 
    {
        super.writeToNBT(nbt);
        nbt.setInteger("faktor", faktor);
        nbt.setByte("mode", ctrMode);
        nbt.setDouble("Eflow", Eflow);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) 
    {
        super.readFromNBT(nbt);
        faktor = nbt.getInteger("faktor");
        ctrMode = nbt.getByte("mode");
        Eflow = nbt.getDouble("Eflow");
    }
    
    private byte lastCtrMode;
    private int lastFaktor;
    
    @Override
    public void addCraftingToCrafters(TileContainer container, ICrafting crafting)
    {
        crafting.sendProgressBarUpdate(container, 0, this.ctrMode);
        crafting.sendProgressBarUpdate(container, 1, this.faktor);
    }
    
    @Override
    public void updateProgressBar(int var, int val)
    {
        if (var == 0) this.ctrMode = (byte)val;
        else if (var == 1) this.faktor = val;
    }
    
    @Override
    public boolean detectAndSendChanges(TileContainer container, List<ICrafting> crafters, PacketBuffer dis) 
    {
        for (int var1 = 0; var1 < crafters.size(); ++var1)
        {
            ICrafting var2 = crafters.get(var1);
            if (this.ctrMode != lastCtrMode)
            {
                var2.sendProgressBarUpdate(container, 0, this.ctrMode);
            }
            if (this.faktor != lastFaktor)
            {
                var2.sendProgressBarUpdate(container, 1, this.faktor);
            }
        }
        lastCtrMode = this.ctrMode;
        lastFaktor = this.faktor;
        return false;
    }
    
}
