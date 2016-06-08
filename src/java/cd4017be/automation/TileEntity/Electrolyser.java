package cd4017be.automation.TileEntity;

import net.minecraft.network.PacketBuffer;

import java.io.IOException;

import cd4017be.api.automation.IEnergy;
import cd4017be.api.automation.PipeEnergy;
import cd4017be.api.recipes.AutomationRecipes;
import cd4017be.api.recipes.AutomationRecipes.ElRecipe;
import cd4017be.automation.Config;
import cd4017be.lib.TileContainer;
import cd4017be.lib.TileContainer.TankSlot;
import cd4017be.lib.TileEntityData;
import cd4017be.lib.templates.AutomatedTile;
import cd4017be.lib.templates.Inventory.Component;
import cd4017be.lib.templates.TankContainer;
import cd4017be.lib.templates.TankContainer.Tank;
import cd4017be.lib.templates.Inventory;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.IFluidHandler;

public class Electrolyser extends AutomatedTile implements ISidedInventory, IEnergy, IFluidHandler
{
    private float powerScale;
    private ElRecipe recipe;
	
	public Electrolyser()
	{
            netData = new TileEntityData(2, 1, 3, 3);
		energy = new PipeEnergy(Config.Umax[1], Config.Rcond[1]);
		inventory = new Inventory(this, 3, new Component(0, 1, -1), new Component(1, 2, 1), new Component(2, 3, 1));
		tanks = new TankContainer(this, new Tank(Config.tankCap[1], -1).setIn(0), new Tank(Config.tankCap[1], 1).setOut(1), new Tank(Config.tankCap[1], 1).setOut(2)).setNetLong(1);
		/**
		 * long: invcfg, tankcfg
		 * int: resistor
		 * float: progress, rcpEnergy, power
		 * fluid: input, output0, output1
		 */
		
		netData.ints[0] = Config.Rmin;
        powerScale = Config.Pscale;
	}

	@Override
	public void update() 
	{
		super.update();
		if (worldObj.isRemote) return;
		if (recipe == null) {
            recipe = AutomationRecipes.getRecipeFor(inventory.items, 0, tanks.getFluid(0));
            if (recipe != null) {
                recipe.useRes(inventory, 0, tanks, 0);
                netData.floats[1] = recipe.energy;
            }
        }
		netData.floats[2] = (float)energy.Ucap;
        double power = energy.getEnergy(0, netData.ints[0]) / 1000D;
        if (recipe != null) {
            if (netData.floats[0] < netData.floats[1]) {
                netData.floats[0] += power;
                energy.Ucap *= powerScale;
            }
            if (netData.floats[0] >= netData.floats[1] && recipe.output(inventory, 1, 2, tanks, 1, 2)) {
                recipe = null;
                netData.floats[0] -= netData.floats[1];
                netData.floats[1] = 0;
            }
        }
	}
	
	public int getProgressScaled(int s)
	{
            if (netData.floats[1] <= 0) return 0;
            int n = (int)(netData.floats[0] * s / netData.floats[1]);
            return n > s ? s : n;
	}
	
	public int getPowerScaled(int s)
	{
		return (int)(s * netData.floats[2] * netData.floats[2] / (float)netData.ints[0]) / 200000;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) 
	{
		super.readFromNBT(nbt);
		if (nbt.hasKey("recipe")) recipe = AutomationRecipes.readElRecipeFromNBT(nbt.getCompoundTag("recipe"));
        else recipe = null;
        netData.floats[0] = nbt.getFloat("progress");
        netData.ints[0] = nbt.getInteger("resistor");
        powerScale = nbt.getFloat("pScale");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) 
	{
		if (recipe != null) nbt.setTag("recipe", AutomationRecipes.writeElRecipeToNBT(recipe));
        nbt.setFloat("progress", netData.floats[0]);
        nbt.setInteger("resistor", netData.ints[0]);
        nbt.setFloat("pScale", powerScale);
        return super.writeToNBT(nbt);
	}

	@Override
	protected void customPlayerCommand(byte cmd, PacketBuffer dis, EntityPlayerMP player) throws IOException 
	{
		if (cmd == 0)
        {
            netData.ints[0] = dis.readInt();
            if (netData.ints[0] < Config.Rmin) netData.ints[0] = Config.Rmin;
            powerScale = (float)Math.sqrt(1.0D - 1.0D / (double)netData.ints[0]);
        }
	}

	@Override
	public void initContainer(TileContainer container) 
	{
		container.addEntitySlot(new Slot(this, 0, 98, 34));
		container.addEntitySlot(new Slot(this, 1, 44, 34));
		container.addEntitySlot(new Slot(this, 2, 152, 34));
		
		container.addPlayerInventory(8, 86);
		
		container.addTankSlot(new TankSlot(tanks, 0, 98, 16, false));
		container.addTankSlot(new TankSlot(tanks, 1, 44, 16, false));
		container.addTankSlot(new TankSlot(tanks, 2, 152, 16, false));
	}

	@Override
	public int[] stackTransferTarget(ItemStack item, int s, TileContainer container) 
	{
		int[] pi = container.getPlayerInv();
        if (s < pi[0]) return pi;
        else return new int[]{0, 1};
	}
	
}
