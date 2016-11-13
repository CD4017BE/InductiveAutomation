package cd4017be.automation.Gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import cd4017be.automation.TileEntity.MassstorageChest;
import cd4017be.lib.BlockGuiHandler;
import cd4017be.lib.Gui.GuiMachine;
import cd4017be.lib.Gui.TileContainer;
import cd4017be.lib.templates.AutomatedTile;

/**
 *
 * @author CD4017BE
 */
public class GuiMassstorageChest extends GuiMachine {

	private final MassstorageChest tileEntity;

	public GuiMassstorageChest(MassstorageChest tileEntity, EntityPlayer player) {
		super(new TileContainer(tileEntity, player));
		this.tileEntity = tileEntity;
		this.MAIN_TEX = new ResourceLocation("automation", "textures/gui/massstorage.png");
	}

	@Override
	public void initGui() {
		this.xSize = 206;
		this.ySize = 256;
		super.initGui();
		guiComps.add(new Button(1, 181, 209, 18, 18, -1).setTooltip("autoSort"));
		guiComps.add(new GuiComp(2, 182, 174, 16, 16).setTooltip("massstorage"));
		guiComps.add(new GuiComp(3, 186, 196, 7, 8).setTooltip("specialSlot"));
	}

	@Override
	protected void setDisplVar(int id, Object obj, boolean send) {
		if (!send || id != 1) return;
		PacketBuffer dis = tileEntity.getPacketTargetData();
		dis.writeByte(AutomatedTile.CmdOffset);
		BlockGuiHandler.sendPacketToServer(dis);
	}

}
