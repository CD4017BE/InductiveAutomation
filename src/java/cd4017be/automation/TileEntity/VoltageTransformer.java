/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cd4017be.automation.TileEntity;

import net.minecraft.util.ITickable;

import java.io.IOException;
import cd4017be.api.automation.IEnergy;
import cd4017be.api.automation.PipeEnergy;
import cd4017be.lib.ModTileEntity;
import cd4017be.lib.TileEntityData;
import cd4017be.lib.util.Utils;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;

/**
 *
 * @author CD4017BE
 */
public class VoltageTransformer extends ModTileEntity implements IEnergy, ITickable
{
	
	public VoltageTransformer() {
		netData = new TileEntityData(0, 2, 0, 0);
		netData.ints[0] = 10;
	}
    public double Eflow;
    
    @Override
    public void update() 
    {
        if(worldObj.isRemote) return;
        byte s = this.getOrientation();
        if((netData.ints[1] & 2) == 0 ^ ((netData.ints[1] & 1) == 0 ? false : worldObj.isBlockPowered(getPos()))) return;
        TileEntity te = Utils.getTileOnSide(this, (byte)(s ^ 1));
        PipeEnergy wireLV = te != null && te instanceof IEnergy ? ((IEnergy)te).getEnergy((byte)(s)) : null;
        te = Utils.getTileOnSide(this, (byte)(s));
        PipeEnergy wireHV = te != null && te instanceof IEnergy ? ((IEnergy)te).getEnergy((byte)(s ^ 1)) : null;
        if (wireLV != null && wireHV != null && wireLV.Umax > 0 && wireHV.Umax > 0)
        {
            float x = (float)(netData.ints[0] * netData.ints[0]) * 0.01F;
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
        netData.ints[0] = dis.readInt();
        netData.ints[1] = dis.readByte();
        if (netData.ints[0] < 10) netData.ints[0] = 10;
        if (netData.ints[0] > 200) netData.ints[0] = 200;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) 
    {
        nbt.setInteger("faktor", netData.ints[0]);
        nbt.setByte("mode", (byte)netData.ints[1]);
        nbt.setDouble("Eflow", Eflow);
        return super.writeToNBT(nbt);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) 
    {
        super.readFromNBT(nbt);
        netData.ints[0] = nbt.getInteger("faktor");
        netData.ints[1] = nbt.getByte("mode");
        Eflow = nbt.getDouble("Eflow");
    }
    
}
