/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cd4017be.automation.Item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import cd4017be.automation.Automation;
import cd4017be.lib.DefaultItem;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AnvilUpdateEvent;

/**
 *
 * @author CD4017BE
 */
public class ItemMinerDrill extends DefaultItem
{
    public static final String[] defaultClass = {"pickaxe", "axe", "shovel"};
    public final int miningLevel;
    public final float efficiency;
    public final String[] harvestClass;
    public static AnvilHandler anvilHandler = new AnvilHandler();
    
    static {
    	anvilHandler.enchantments.add(Enchantments.unbreaking);
    	anvilHandler.enchantments.add(Enchantments.efficiency);
    	anvilHandler.enchantments.add(Enchantments.fortune);
    	anvilHandler.enchantments.add(Enchantments.silkTouch);
    	MinecraftForge.EVENT_BUS.register(anvilHandler);
    }
    
    public static class AnvilHandler {
    	public HashMap<Item, ItemStack> repairMaterials = new HashMap<Item, ItemStack>();
    	public ArrayList<Enchantment> enchantments = new ArrayList<Enchantment>();
    	@SubscribeEvent
    	public void onAnvilUpdate(AnvilUpdateEvent e) {
    		if (!(e.getLeft().getItem() instanceof ItemMinerDrill)) return;
    		Map<Enchantment, Integer> enchantL = EnchantmentHelper.getEnchantments(e.getLeft());
    		Map<Enchantment, Integer> enchantR = EnchantmentHelper.getEnchantments(e.getRight());
    		ItemStack out = new ItemStack(e.getLeft().getItem(), 1, e.getLeft().getItemDamage());
    		e.setMaterialCost(0);
    		int n, m, cost = 0;
    		for (Enchantment id : enchantR.keySet()) {
    			if (!enchantments.contains(id)) {
    				e.setCanceled(true);
    				return;
    			}
    			n = enchantR.get(id);
    			Integer i = enchantL.get(id);
    			if (i == null) {
    				enchantL.put(id, n);
    				cost += n + 7;
    			} else if (i == n) {
    				enchantL.put(id, n + 1);
    				cost += n + 1;
    			} else if (n > i) {
    				enchantL.put(id, n);
    				cost += n;
    			} else {
    				e.setCanceled(true);
    				return;
    			}
    			e.setMaterialCost(1);
    		}
    		EnchantmentHelper.setEnchantments(enchantL, out);
    		n = out.getItemDamage();
    		if (n > 0) {
    			if (e.getRight().getItem() == e.getLeft().getItem()) {
    				m = n - e.getRight().getMaxDamage() + e.getRight().getItemDamage();
    				e.setMaterialCost(1);
    			} else {
    				ItemStack rm = repairMaterials.get(out.getItem());
    				if (rm != null && rm.isItemEqual(e.getRight())) {
    					m = Math.min(e.getRight().stackSize, (int)Math.ceil((float)n * (float)rm.stackSize / (float)out.getMaxDamage()));
    					e.setMaterialCost(m);
    					m = n - out.getMaxDamage() * m / rm.stackSize;
    				} else m = n;
    			}
    			if (m >= 0) n -= m;
				else {
					n = n * n / (n - m);
					m = 0;
				}
				out.setItemDamage(m);
				cost += Math.ceil((float)n / (float)out.getMaxDamage() * (float)(30 + 10 * enchantL.size()));
    		}
    		if (e.getMaterialCost() > 0) {
    			e.setOutput(out);
    			e.setCost(cost);
    		} else {
    			e.setCanceled(true);
    		}
    	}
    }
    
    public ItemMinerDrill(String id, int durability, int miningLvl, float efficiency, String... harvestClass)
    {
        super(id);
        this.setCreativeTab(Automation.tabAutomation);
        this.setMaxStackSize(1);
        this.setMaxDamage(durability);
        this.miningLevel = miningLvl;
        this.efficiency = efficiency;
        this.harvestClass = harvestClass;
    }
    
    public boolean canHarvestBlock(IBlockState state, int meta)
    {
        boolean register = true;
        for (String hc : this.harvestClass) {
            int l = state.getBlock().getHarvestLevel(state);
            if (l != -1 && l <= this.miningLevel) return true;
            register &= l == -1;
        }
        return register && (state.getMaterial() == Material.rock || state.getMaterial() == Material.iron || state.getMaterial() == Material.anvil);
    }
    
}
