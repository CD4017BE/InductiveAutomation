/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cd4017be.automation.jetpack;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLEventChannel;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import cd4017be.automation.Item.ItemJetpack;
import cd4017be.lib.util.Vec3;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;

/**
 *
 * @author CD4017BE
 */
public class PacketHandler
{
    public static final String channel = "Autom_jetpack";
    public static final PacketHandler instance = new PacketHandler();
    
    public static FMLEventChannel eventChannel;
    
    public static void register()
    {
    	eventChannel = NetworkRegistry.INSTANCE.newEventDrivenChannel(channel);
        eventChannel.register(instance);
    }
    
    @SubscribeEvent
    public void onPacketData(FMLNetworkEvent.ServerCustomPacketEvent event) 
    {
        if (event.getPacket().channel().equals(channel) && event.getHandler() instanceof NetHandlerPlayServer) {
                EntityPlayer player = ((NetHandlerPlayServer)event.getHandler()).playerEntity;
                PacketBuffer dis = (PacketBuffer)event.getPacket().payload();
                ItemStack item = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
                if (item == null || !(item.getItem() instanceof ItemJetpack) || item.getTagCompound() == null) return;
                byte cmd = dis.readByte();
                if (cmd == 0) {
                    int power = dis.readInt();
                    Vec3 vec = Vec3.Def(dis.readFloat(), dis.readFloat(), dis.readFloat());
                    double l = vec.sq();
                    if (l > 1.015625D || l < 0.984375D) vec = vec.norm();
                    if (vec.isNaN()) vec = Vec3.Def(0, 1, 0);
                    if (power < 0) power = 0;
                    else if (power > ItemJetpack.maxPower) power = ItemJetpack.maxPower;
                    item.getTagCompound().setInteger("power", power);
                    item.getTagCompound().setFloat("vecX", (float)vec.x);
                    item.getTagCompound().setFloat("vecY", (float)vec.y);
                    item.getTagCompound().setFloat("vecZ", (float)vec.z);
                } else if (cmd == 1) {
                } else if (cmd == 2) {
                    boolean state = !item.getTagCompound().getBoolean("On");
                    item.getTagCompound().setBoolean("On", state);
                }
        }
    }
    
}
