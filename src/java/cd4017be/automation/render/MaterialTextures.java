package cd4017be.automation.render;

import cd4017be.automation.Objects;
import cd4017be.automation.Item.ItemMaterial;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class MaterialTextures implements ItemMeshDefinition {
	private final String path;
	public MaterialTextures(String path) {
		this.path = path;
		ResourceLocation[] locs = new ResourceLocation[ItemMaterial.types.length];
		for (int i = 0; i < locs.length; i++) {
			locs[i] = new ResourceLocation(path + ItemMaterial.types[i]);
		}
		ModelBakery.registerItemVariants(Objects.material, locs);
	}
	
	@Override
	public ModelResourceLocation getModelLocation(ItemStack stack) {
		int m = stack.getItemDamage();
		if (m >= 0 && m < ItemMaterial.types.length) return new ModelResourceLocation(path + ItemMaterial.types[m], "inventory");
		return null;
	}

}
