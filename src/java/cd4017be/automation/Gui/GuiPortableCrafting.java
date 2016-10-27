package cd4017be.automation.Gui;

import org.lwjgl.input.Keyboard;
import cd4017be.lib.BlockGuiHandler;
import cd4017be.lib.Gui.GuiMachine;
import cd4017be.lib.Gui.TileContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

public class GuiPortableCrafting extends GuiMachine {

	private final InventoryPlayer inv;

	public GuiPortableCrafting(TileContainer container) {
		super(container);
		this.inv = container.player.inventory;
		this.MAIN_TEX = new ResourceLocation("automation", "textures/gui/portableCrafting.png");
	}

	@Override
	public void initGui() {
		this.xSize = 176;
		this.ySize = 168;
		super.initGui();
		guiComps.add(new Button(0, 89, 34, 16, 16, -1));
		guiComps.add(new NumberSel(1, 106, 33, 18, 18, "%d", 0, 64, 8));
		guiComps.add(new Button(2, 124, 33, 18, 18, 0).setTooltip("startCraft"));
		guiComps.add(new Button(3, 142, 33, 18, 18, 0).setTooltip("autoCraft#"));
		guiComps.add(new Button(4, 74, 55, 10, 10, -1).setTooltip("clearRecipe"));
		guiComps.add(new GuiComp(5, 92, 55, 10, 10).setTooltip("directCraft"));
		guiComps.add(new Text(6, 0, 4, xSize, 0, "item.cd4017be.portableCrafter.name").center());
	}

	@Override
	protected Object getDisplVar(int id) {
		ItemStack item = inv.mainInventory[inv.currentItem];
		NBTTagCompound nbt = item != null && item.hasTagCompound() ? item.getTagCompound() : new NBTTagCompound();
		switch(id) {
		case 1: return (int)nbt.getByte("amount");
		case 2: return nbt.getBoolean("active") ? 1 : 0;
		case 3: return nbt.getBoolean("auto") ? 1 : 0;
		default: return null;
		}
	}

	@Override
	protected void setDisplVar(int id, Object obj, boolean send) {
		PacketBuffer dos = BlockGuiHandler.getPacketTargetData(((TileContainer)inventorySlots).data.pos());
		switch(id) {
		case 0: dos.writeByte(3); dos.writeByte(((Integer)obj == 0 ? 1 : 8) * (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) ? 64 : 1)); break;
		case 1: dos.writeByte(2); dos.writeByte((Integer)obj); break;
		case 2: dos.writeByte(0); break;
		case 3: dos.writeByte(1); break;
		case 4: 
			for (int i = 0; i < 9; i++) 
				this.mc.playerController.windowClick(this.inventorySlots.windowId, i + 36, 0, ClickType.PICKUP, this.mc.thePlayer);
		default: return;
		}
		if (send) BlockGuiHandler.sendPacketToServer(dos);
	}

}
