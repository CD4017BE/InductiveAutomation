package cd4017be.automation.Gui;

import cd4017be.api.energy.EnergyAPI;
import cd4017be.api.energy.EnergyAutomation.IEnergyItem;
import cd4017be.automation.Item.ItemFilteredSubInventory;
import cd4017be.lib.BlockGuiHandler;
import cd4017be.lib.Gui.GuiMachine;
import cd4017be.lib.Gui.TileContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

public class GuiPortableFurnace extends GuiMachine {

	private final InventoryPlayer inv;
	private int capacity;

	public GuiPortableFurnace(TileContainer container) {
		super(container);
		this.inv = container.player.inventory;
		this.MAIN_TEX = new ResourceLocation("automation", "textures/gui/portableFurnace.png");
	}

	@Override
	public void initGui() {
		ItemStack item = inv.mainInventory[inv.currentItem];
		if (item.getItem() instanceof IEnergyItem)
			capacity = ((IEnergyItem)item.getItem()).getEnergyCap(item);
		this.xSize = 176;
		this.ySize = 132;
		super.initGui();
		guiComps.add(new Button(0, 61, 19, 18, 10, 0).texture(176, 16).setTooltip("inputFilter"));
		guiComps.add(new ProgressBar(1, 116, 16, 34, 16, 176, 0, (byte)0).setTooltip("x*" + capacity + "+0;Estor1"));
	}

	@Override
	protected Object getDisplVar(int id) {
		ItemStack item = inv.mainInventory[inv.currentItem];
		if (id == 0) return ItemFilteredSubInventory.isFilterOn(item, true) ? 1 : 0;
		else if (id == 1) return EnergyAPI.get(item, -1).getStorage() / 1000F / (float)capacity;
		else return null;
	}

	@Override
	protected void setDisplVar(int id, Object obj, boolean send) {
		PacketBuffer dos = BlockGuiHandler.getPacketTargetData(((TileContainer)inventorySlots).data.pos());
		if (id == 0) dos.writeByte(0);
		else return;
		if (send) BlockGuiHandler.sendPacketToServer(dos);
	}

}
