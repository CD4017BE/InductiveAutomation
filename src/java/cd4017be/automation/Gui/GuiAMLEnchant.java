package cd4017be.automation.Gui;

import org.lwjgl.opengl.GL11;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;
import cd4017be.automation.Item.ItemAntimatterLaser;
import cd4017be.automation.Item.ItemAntimatterLaser.Enchantments;
import cd4017be.lib.TooltipInfo;
import cd4017be.lib.Gui.GuiMachine;

public class GuiAMLEnchant extends GuiMachine 
{

	private int Euse = ItemAntimatterLaser.EnergyUsage;
	private float AMuse = 1;
	private final ContainerAMLEnchant cont;
	
	public GuiAMLEnchant(ContainerAMLEnchant container) 
	{
		super(container);
		this.cont = container;
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mx, int my) 
	{
		super.drawGuiContainerForegroundLayer(mx, my);
		this.drawInfo(49, 20, 7, 8, "\\i", "amlE.info");
	}

	@Override
	public void initGui() 
	{
		this.xSize = 176;
        this.ySize = 132;
        super.initGui();
	}
	
	@Override
	public void updateScreen() 
	{
		super.updateScreen();
		Enchantments ench = new Enchantments(cont.player.getHeldItemMainhand());
		AMuse = ench.amMult * 8;
		Euse = ench.Euse;
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_) 
	{
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.renderEngine.bindTexture(new ResourceLocation("automation", "textures/gui/amlEnchant.png"));
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
        this.fontRendererObj.drawString("Euse = " + Euse + " " + TooltipInfo.getEnergyUnit(), this.guiLeft + 62, this.guiTop + 17, 0x404040);
        this.fontRendererObj.drawString("AMuse = " + (int)(AMuse * 100F) + " %", this.guiLeft + 62, this.guiTop + 26, 0x404040);
        this.drawStringCentered(I18n.translateToLocal("gui.cd4017be.amLaser.name"), this.guiLeft + this.xSize / 2, this.guiTop + 4, 0x404040);
        this.drawStringCentered(I18n.translateToLocal("container.inventory"), this.guiLeft + this.xSize / 2, this.guiTop + 36, 0x404040);
	}

}
