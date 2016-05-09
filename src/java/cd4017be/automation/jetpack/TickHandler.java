/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cd4017be.automation.jetpack;

import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import cd4017be.automation.Item.ItemJetpack;
import cd4017be.automation.jetpack.JetPackConfig.Mode;
import cd4017be.lib.util.Vec2;
import cd4017be.lib.util.Vec3;

/**
 *
 * @author CD4017BE
 */
public class TickHandler
{
    private static final Minecraft mc = Minecraft.getMinecraft();
    private Vec2 dirVec = Vec2.Def(0, 0);
    public int power = 0;
    public static TickHandler instance = new TickHandler();
    private static final KeyBinding keyOn = new KeyBinding("Jetpack On/Off", Keyboard.KEY_F, "Automation");
    private static final KeyBinding keyMode = new KeyBinding("Jetpack control-mode", Keyboard.KEY_Y, "Automation");
    private static boolean lastKeyOnState = false;
    private static boolean lastKeyModeState = false;
    
    public static void init()
    {
    	ClientRegistry.registerKeyBinding(keyMode);
        ClientRegistry.registerKeyBinding(keyOn);
        FMLCommonHandler.instance().bus().register(instance);
    }
    
    @SubscribeEvent
    public void tick(TickEvent.ClientTickEvent event) 
    {
    	if (event.phase == TickEvent.Phase.START && mc.thePlayer != null && mc.gameSettings != null) {
            this.checkJetpack();
        }
    }
    
    private void checkJetpack()
    {
    	ItemStack item = mc.thePlayer.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
    	if (item == null || !(item.getItem() instanceof ItemJetpack)) return;
    	this.updateKeyStates(item);
        NBTTagCompound nbt = item.getTagCompound();
        if (nbt == null || !nbt.getBoolean("On")) return;
        if (GameSettings.isKeyDown(mc.gameSettings.keyBindJump)) power++;
        if (GameSettings.isKeyDown(mc.gameSettings.keyBindSneak)) power--;
        if (power < 0) power = 0;
        if (power > ItemJetpack.maxPower) power = ItemJetpack.maxPower;
        Vec3 dir;
        Mode mode = JetPackConfig.getMode();
        if (mode.vertAngleFaktor == 0) {
            dir = Vec3.Def(0, mode.verticalComp, 0);
        } else {
            float a = mode.vertAngleOffset - mode.vertAngleFaktor * mc.thePlayer.rotationPitch;
            float f1 = MathHelper.cos(-mc.thePlayer.rotationYaw * 0.017453292F);// - (float)Math.PI
            float f2 = MathHelper.sin(-mc.thePlayer.rotationYaw * 0.017453292F);// - (float)Math.PI
            float f3 = MathHelper.cos(a * 0.017453292F);
            float f4 = MathHelper.sin(a * 0.017453292F);
            dir = Vec3.Def((double)(f2 * f3), (double)f4, (double)(f1 * f3)).scale(mode.verticalComp);
        }
        if (mode.moveStrength > 0) {
            Vec2 ctr = Vec2.Def(0, 0);
            if (GameSettings.isKeyDown(mc.gameSettings.keyBindForward)) ctr.z--;
            if (GameSettings.isKeyDown(mc.gameSettings.keyBindBack)) ctr.z++;
            if (GameSettings.isKeyDown(mc.gameSettings.keyBindRight)) ctr.x++;
            if (GameSettings.isKeyDown(mc.gameSettings.keyBindLeft)) ctr.x--;
            dirVec = dirVec.scale(1 - mode.moveStrength).add(ctr.scale(mode.moveStrength));
            float cos = MathHelper.cos(-mc.thePlayer.rotationYaw * 0.017453292F - (float)Math.PI);
            float sin = MathHelper.sin(-mc.thePlayer.rotationYaw * 0.017453292F - (float)Math.PI);
            dir = dir.add(new Vec3(dirVec.rotate(cos, sin), 0));
        }
        dir = dir.norm();
        if (nbt.getInteger("power") < 0) return;
        ItemJetpack.updateMovement(mc.thePlayer, dir, power);
        PacketBuffer dos = new PacketBuffer(Unpooled.buffer());
		//command-ID
		dos.writeByte(0);
		dos.writeInt(power);
		dos.writeFloat((float)dir.x);
		dos.writeFloat((float)dir.y);
		dos.writeFloat((float)dir.z);
		/*
		dos.writeFloat((float)mc.thePlayer.posX);
		dos.writeFloat((float)mc.thePlayer.posY);
		dos.writeFloat((float)mc.thePlayer.posZ);
		dos.writeFloat((float)mc.thePlayer.motionX);
		dos.writeFloat((float)mc.thePlayer.motionY);
		dos.writeFloat((float)mc.thePlayer.motionZ);
		*/
		PacketHandler.eventChannel.sendToServer(new FMLProxyPacket(dos, PacketHandler.channel));
    }

    private void updateKeyStates(ItemStack item)
    {
    	
    	boolean pressOn = !lastKeyOnState && keyOn.isPressed();
    	boolean pressMode = !lastKeyModeState && keyMode.isPressed();
    	lastKeyOnState = keyOn.isPressed();
    	lastKeyModeState = keyMode.isPressed();
    	if (mc.currentScreen == null) {
    		if (pressOn) {
    			PacketBuffer dos = new PacketBuffer(Unpooled.buffer());
				dos.writeByte(2);
				PacketHandler.eventChannel.sendToServer(new FMLProxyPacket(dos, PacketHandler.channel));
    		}
    		if (pressMode && item.getTagCompound() != null && item.getTagCompound().getBoolean("On")) {
    			JetPackConfig.mode++;
                return;
            } else if (pressMode) {
                FMLClientHandler.instance().showGuiScreen(new GuiJetpackConfig());
            }
        }
    }
    
    
    @SubscribeEvent
    public void renderTick(TickEvent.RenderTickEvent event) 
    {
        if (mc.currentScreen == null && event.phase == TickEvent.Phase.END) {
        	ItemStack item = mc.thePlayer.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
            if (item == null || !(item.getItem() instanceof ItemJetpack) || item.getTagCompound() == null) return;
            NBTTagCompound nbt = item.getTagCompound();
            if (!nbt.getBoolean("On")) return;
            mc.fontRendererObj.drawString("Jetpack Control Mode: " + JetPackConfig.getMode().name, 8, 8, 0x40ff40);
            GL11.glColor4f(1, 1, 1, 1);
            mc.renderEngine.bindTexture(new ResourceLocation("automation", "textures/gui/jetpack.png"));
            int hgt = this.getScreenHeight();
            mc.ingameGUI.drawTexturedModalRect(8, (hgt - 176) / 2, 0, 0, 16, 169);
            int n = power * 160 / ItemJetpack.maxPower;
            mc.ingameGUI.drawTexturedModalRect(9, (hgt - 176) / 2 + 161 - n, 16, 160 - n, 6, n);
            if (nbt.getInteger("power") < 0)
                mc.fontRendererObj.drawString("Out of Fuel!", 8, (hgt + 176) / 2 - 8, 0xff7f3f);
            Vec3 mov = Vec3.Def(mc.thePlayer.posX - mc.thePlayer.lastTickPosX, mc.thePlayer.posY - mc.thePlayer.lastTickPosY, mc.thePlayer.posZ - mc.thePlayer.lastTickPosZ).scale(20D);
            mc.fontRendererObj.drawString(String.format("Speed  = %5.1f m/s", mov.l()), 8, hgt - 48, 0xff3f00);
            mc.fontRendererObj.drawString(String.format("Ascent = %+5.1f m/s", mov.y), 8, hgt - 32, 0xff3f00);
            mc.fontRendererObj.drawString(String.format("Height = %5.1f m", mc.thePlayer.posY), 8, hgt - 16, 0xff3f00);
        }
    }
    
    private int getScreenHeight()
    {
        ScaledResolution scaledresolution = new ScaledResolution(mc);
        return scaledresolution.getScaledHeight();
    }
    
}
