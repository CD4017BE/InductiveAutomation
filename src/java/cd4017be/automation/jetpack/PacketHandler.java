/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cd4017be.automation.jetpack;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.FMLEventChannel;
import cpw.mods.fml.common.network.FMLNetworkEvent;
import cpw.mods.fml.common.network.NetworkRegistry;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import cd4017be.automation.Item.ItemJetpack;
import cd4017be.lib.util.Vec3;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetHandlerPlayServer;

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
        if (event.packet.channel().equals(channel) && event.handler instanceof NetHandlerPlayServer) {
            try {
                EntityPlayer player = ((NetHandlerPlayServer)event.handler).playerEntity;
                DataInputStream dis = new DataInputStream(new ByteArrayInputStream(event.packet.payload().array()));
                ItemStack item = player.getCurrentArmor(ItemJetpack.slotPos);
                if (item == null || !(item.getItem() instanceof ItemJetpack) || item.stackTagCompound == null) return;
                byte cmd = dis.readByte();
                if (cmd == 0) {
                    int power = dis.readInt();
                    Vec3 vec = Vec3.Def(dis.readFloat(), dis.readFloat(), dis.readFloat());
                    double l = vec.sq();
                    if (l > 1.015625D || l < 0.984375D) vec = vec.norm();
                    if (vec.isNaN()) vec = Vec3.Def(0, 1, 0);
                    if (power < 0) power = 0;
                    else if (power > ItemJetpack.maxPower) power = ItemJetpack.maxPower;
                    item.stackTagCompound.setInteger("power", power);
                    item.stackTagCompound.setFloat("vecX", (float)vec.x);
                    item.stackTagCompound.setFloat("vecY", (float)vec.y);
                    item.stackTagCompound.setFloat("vecZ", (float)vec.z);
                } else if (cmd == 1) {
                } else if (cmd == 2) {
                    boolean state = !item.stackTagCompound.getBoolean("On");
                    item.stackTagCompound.setBoolean("On", state);
                }
            } catch (IOException e) {}
        }
    }
    
}
