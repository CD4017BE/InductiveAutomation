package cd4017be.automation.render;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ISmartItemModel;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

@SuppressWarnings("deprecation")
public class FluidTextures implements ISmartItemModel {
	
	private final IBakedModel model;
	private final BakedQuad quad;
	public FluidTextures(IBakedModel model) {
		this.model = model;
		this.quad = model.getGeneralQuads().get(0);
	}
	
	private FluidTextures(IBakedModel model, BakedQuad quad) {
		this.model = model;
		this.quad = quad;
	}
	
	@Override
	public List<BakedQuad> getFaceQuads(EnumFacing p_177551_1_) {
		return Collections.emptyList();
	}

	@Override
	public List<BakedQuad> getGeneralQuads() {
		return Arrays.asList(quad);
	}

	@Override
	public boolean isAmbientOcclusion() {
		return model.isAmbientOcclusion();
	}

	@Override
	public boolean isGui3d() {
		return model.isGui3d();
	}

	@Override
	public boolean isBuiltInRenderer() {
		return model.isBuiltInRenderer();
	}

	@Override
	public TextureAtlasSprite getParticleTexture() {
		return model.getParticleTexture();
	}

	@Override
	public ItemCameraTransforms getItemCameraTransforms() {
		return model.getItemCameraTransforms();
	}

	@Override
	public IBakedModel handleItemState(ItemStack stack) {
		Fluid fluid = FluidRegistry.getFluid(stack.getItemDamage());
		if (fluid == null) return this;
		ResourceLocation res = fluid.getStill();
		if (res == null) return this;
		TextureAtlasSprite tex = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(res.toString());
		if (tex == null) return this;
		int[] data = new int[28];
		data[0] = data[21] = Float.floatToIntBits(0F);
		data[1] = data[8] = Float.floatToIntBits(0F);
		data[7] = data[14] = Float.floatToIntBits(1F);
		data[15] = data[22] = Float.floatToIntBits(1F);
		data[2] = data[9] = data[16] = data[23] = Float.floatToIntBits(0F);
		data[3] = data[10] = data[17] = data[24] = fluid.getColor();
		data[4] = data[25] = Float.floatToIntBits(tex.getMinU());
		data[5] = data[12] = Float.floatToIntBits(tex.getMinV());
		data[11] = data[18] = Float.floatToIntBits(tex.getMaxU());
		data[19] = data[26] = Float.floatToIntBits(tex.getMaxV());
		return new FluidTextures(model, new BakedQuad(data, quad.getTintIndex(), quad.getFace()));
	}
	
}