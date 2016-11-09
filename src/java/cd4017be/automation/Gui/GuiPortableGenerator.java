package cd4017be.automation.Gui;

import cd4017be.automation.Item.ItemFilteredSubInventory;
import cd4017be.lib.BlockGuiHandler;
import cd4017be.lib.Gui.GuiMachine;
import cd4017be.lib.Gui.TileContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

public class GuiPortableGenerator extends GuiMachine {

	private final InventoryPlayer inv;

	public GuiPortableGenerator(TileContainer container) {
		super(container);
		this.inv = container.player.inventory;
		this.MAIN_TEX = new ResourceLocation("automation", "textures/gui/portableGenerator.png");
	}

	@Override
	public void initGui() {
		this.xSize = 176;
		this.ySize = 132;
		super.initGui();
		guiComps.add(new Button(0, 61, 19, 18, 10, 0).texture(176, 16).setTooltip("inputFilter"));
		guiComps.add(new ProgressBar(1, 99, 17, 14, 14, 176, 0, (byte)1));
		guiComps.add(new Text(2, 142, 20, 0, 0, "Estor1").center());
	}

	@Override
	protected Object getDisplVar(int id) {
		ItemStack item = inv.mainInventory[inv.currentItem];
		if (id == 0) return ItemFilteredSubInventory.isFilterOn(item, true) ? 0 : 1;
		else if (id < 3) {
			float e = item != null && item.hasTagCompound() ? item.getTagCompound().getInteger("buff") / 1000F : 0;
			return id == 1 ? (float)Math.min(e, 80) / 80F : e;
		} else return null;
	}

	@Override
	protected void setDisplVar(int id, Object obj, boolean send) {
		PacketBuffer dos = BlockGuiHandler.getPacketTargetData(((TileContainer)inventorySlots).data.pos());
		if (id == 0) dos.writeByte(0);
		else return;
		if (send) BlockGuiHandler.sendPacketToServer(dos);
	}

}
