package cd4017be.automation.Gui;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import cd4017be.automation.Item.ItemAntimatterLaser;
import cd4017be.automation.Item.ItemAntimatterLaser.Enchantments;
import cd4017be.lib.Gui.GuiMachine;
import cd4017be.lib.Gui.TileContainer;

public class GuiAMLEnchant extends GuiMachine {

	private int Euse = ItemAntimatterLaser.EnergyUsage;
	private float AMuse = 1;
	private final InventoryPlayer inv;

	public GuiAMLEnchant(TileContainer container) {
		super(container);
		this.MAIN_TEX = new ResourceLocation("automation", "textures/gui/amlEnchant.png");
		this.inv = container.player.inventory;
	}

	@Override
	public void initGui() {
		this.xSize = 176;
		this.ySize = 132;
		super.initGui();
		guiComps.add(new Text(0, 62, 16, 88, 16, "amlE.use").setTooltip("amlE.info"));//
	}

	@Override
	protected Object getDisplVar(int id) {
		return new Object[]{AMuse * 100F, Euse};
	}

	@Override
	public void updateScreen() {
		super.updateScreen();
		Enchantments ench = new Enchantments(inv.mainInventory[inv.currentItem]);
		AMuse = ench.amMult * 8;
		Euse = ench.Euse;
	}

}
