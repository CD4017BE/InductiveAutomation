package cd4017be.automation.Item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import cd4017be.api.automation.ITeslaTransmitter;

public class TeslaTransmitterItem implements ITeslaTransmitter
{
	public final short frequency;
	public final int Umax;
	private final EntityPlayer player;
	private final int slot;
	private final Item type;
	
	public TeslaTransmitterItem(EntityPlayer player, int slot, ItemStack item, int Umax) 
	{
		this.player = player;
		this.slot = slot;
		this.type = item.getItem();
		this.Umax = Umax;
		this.frequency = item.stackTagCompound == null ? 0 : item.stackTagCompound.getShort("freq");
	}

	@Override
	public short getFrequency() 
	{
		return frequency;
	}

	@Override
	public boolean checkAlive() 
	{
		ItemStack item = player.inventory.mainInventory[slot];
		return !player.isDead && item != null && item.getItem() == type && item.stackTagCompound != null && item.stackTagCompound.getShort("freq") == this.frequency;
	}

	@Override
	public double getSqDistance(ITeslaTransmitter t) 
	{
		int[] p = t.getLocation();
    	int[] p1 = this.getLocation();
		double d;
        if (p[3] == player.worldObj.provider.getDimensionId()) {
            int dx = p1[0] - p[0];
            int dy = p1[1] - p[1];
            int dz = p1[2] - p[2];
            d = (double)(dx*dx + dy*dy + dz*dz);
        } else d = Double.POSITIVE_INFINITY;
        return d;
	}

	@Override
	public double getVoltage() 
	{
		ItemStack item = player.inventory.mainInventory[slot];
		return item.stackTagCompound == null ? 0 : item.stackTagCompound.getDouble("voltage");
	}

	@Override
	public double getPower(double R, double U) 
	{
		if (R < 16) R = 16;
        double u = this.getVoltage();
        u *= u;
		double e = (u - U * U) / R;
		if (e > 0) e = Math.min(e, u);
		else e = Math.max(e, u - (double)Umax * (double)Umax);
        return e;
	}

	@Override
	public double addEnergy(double E) 
	{
		ItemStack item = player.inventory.mainInventory[slot];
		if (item.stackTagCompound == null) return 0;
		double u = item.stackTagCompound.getDouble("voltage");
		u *= u;
		if (E < 0) E = Math.max(E, -u);
		else E = Math.min(E, (double)Umax * (double)Umax - u);
		if (E != 0) {
			u = Math.sqrt(u + E);
	        if (Double.isNaN(u)) u = 0F;
	        item.stackTagCompound.setDouble("voltage", u);
		}
        return E;
	}

	@Override
	public int[] getLocation() 
	{
		return new int[]{(int)Math.floor(player.posX), (int)Math.floor(player.posY), (int)Math.floor(player.posZ), player.dimension};
	}

	@Override
	public boolean equals(Object obj) 
	{
		if (obj == null || !(obj instanceof TeslaTransmitterItem)) return false;
		TeslaTransmitterItem i = (TeslaTransmitterItem)obj;
		return i.frequency == this.frequency && i.slot == this.slot && i.type == this.type && i.player.getName() == this.player.getName();
	}

}
