package cd4017be.automation.render;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.google.common.base.Function;

import cd4017be.automation.Item.ItemFluidDummy;
import cd4017be.lib.render.SpecialModelLoader;
import cd4017be.lib.render.TESRBlockModel;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.fluids.Fluid;

public class FluidTextures implements IModel, IBakedModel {

	@Override
	public Collection<ResourceLocation> getDependencies() {
		return Collections.emptyList();
	}

	@Override
	public Collection<ResourceLocation> getTextures() {
		return Collections.emptyList();
	}

	@Override
	public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
		return this;
	}

	@Override
	public IModelState getDefaultState() {
		return ModelRotation.X0_Y0;
	}
	
	@Override
	public boolean isAmbientOcclusion() {
		return false;
	}

	@Override
	public boolean isGui3d() {
		return false;
	}

	@Override
	public boolean isBuiltInRenderer() {
		return false;
	}

	@Override
	public TextureAtlasSprite getParticleTexture() {
		return null;
	}

	@Override
	public ItemCameraTransforms getItemCameraTransforms() {
		return ItemCameraTransforms.DEFAULT;
	}

	@Override
	public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
		return Collections.emptyList();
	}

	@Override
	public ItemOverrideList getOverrides() {
		return itemList;
	}
	
	private static final ItemOverride itemList = new ItemOverride();

	static class ItemOverride extends ItemOverrideList {

		public ItemOverride() {
			super(Collections.<net.minecraft.client.renderer.block.model.ItemOverride> emptyList());
		}

		@Override
		public IBakedModel handleItemState(IBakedModel model, ItemStack stack, World world, EntityLivingBase entity) {
			Fluid fluid = ItemFluidDummy.fluid(stack);
			if (fluid == null) return model;
			ResourceLocation res = fluid.getStill();
			if (res == null) return model;
			TextureAtlasSprite tex = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(res.toString());
			if (tex == null) return model;
			return new TESRBlockModel(tex, true, fluid.getColor(), SpecialModelLoader.instance.tesrModelData.get(TileEntityTankRenderer.model));
		}

	}

}