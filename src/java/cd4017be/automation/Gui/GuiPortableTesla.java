package cd4017be.automation.Gui;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import cd4017be.lib.BlockGuiHandler;
import cd4017be.lib.Gui.GuiMachine;
import cd4017be.lib.Gui.TileContainer;

public class GuiPortableTesla extends GuiMachine {

	private final InventoryPlayer inv;

	public GuiPortableTesla(TileContainer container) {
		super(container);
		this.inv = container.player.inventory;
		this.MAIN_TEX = new ResourceLocation("automation", "textures/gui/portableTesla.png");
	}

	@Override
	public void initGui() {
		this.xSize = 194;
		this.ySize = 132;
		super.initGui();
		guiComps.add(new TextField(0, 134, 16, 34, 7, 5).setTooltip("tesla.set"));
		guiComps.add(new Button(1, 169, 15, 18, 9, 0).texture(194, 27).setTooltip("teslaP.on"));
		guiComps.add(new Button(2, 133, 24, 18, 9, 0).texture(194, 0).setTooltip("teslaP.invH"));
		guiComps.add(new Button(3, 151, 24, 18, 9, 0).texture(212, 0).setTooltip("teslaP.invM"));
		guiComps.add(new Button(4, 169, 24, 18, 9, 0).texture(230, 0).setTooltip("teslaP.invA"));
		guiComps.add(new Text(5, 8, 16, 120, 10, "tesla.stor").center());
		guiComps.add(new Text(6, 0, 4, xSize, 0, "gui.cd4017be.portableTesla.name").center());
	}

	@Override
	protected Object getDisplVar(int id) {
		ItemStack item = inv.mainInventory[inv.currentItem];
		NBTTagCompound nbt = item != null && item.hasTagCompound() ? item.getTagCompound() : new NBTTagCompound();
		switch(id) {
		case 0: return "" + nbt.getShort("freq");
		case 1: return nbt.getShort("mode") & 1;
		case 2: return nbt.getShort("mode") >> 8 & 3;
		case 3: return nbt.getShort("mode") >> 10 & 3;
		case 4: return nbt.getShort("mode") >> 12 & 3;
		case 5: {double u = nbt.getDouble("voltage"); return (int)(u * u * 0.001D);}
		default: return null;
		}
	}

	@Override
	protected void setDisplVar(int id, Object obj, boolean send) {
		PacketBuffer dos = BlockGuiHandler.getPacketTargetData(((TileContainer)inventorySlots).data.pos());
		if (id == 0) try {
				dos.writeByte(1).writeShort(Integer.parseInt((String)obj));
			} catch(NumberFormatException e) {return;}
		else if (id < 5){
			ItemStack item = inv.mainInventory[inv.currentItem];
			short mode = item != null && item.hasTagCompound() ? item.getTagCompound().getShort("mode") : 0;
			if (id == 1) mode ^= 1;
			else mode ^= 1 << (id * 2 + 4);
			dos.writeByte(0).writeShort(mode);
		} else return;
		if (send) BlockGuiHandler.sendPacketToServer(dos);
	}

}
