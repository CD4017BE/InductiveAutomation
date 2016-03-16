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
    	anvilHandler.enchantments.add(Enchantment.unbreaking.effectId);
    	anvilHandler.enchantments.add(Enchantment.efficiency.effectId);
    	anvilHandler.enchantments.add(Enchantment.fortune.effectId);
    	anvilHandler.enchantments.add(Enchantment.silkTouch.effectId);
    	MinecraftForge.EVENT_BUS.register(anvilHandler);
    }
    
    public static class AnvilHandler {
    	public HashMap<Item, ItemStack> repairMaterials = new HashMap<Item, ItemStack>();
    	public ArrayList<Integer> enchantments = new ArrayList<Integer>();
    	@SubscribeEvent
    	public void onAnvilUpdate(AnvilUpdateEvent e) {
    		if (!(e.left.getItem() instanceof ItemMinerDrill)) return;
    		Map<Integer, Integer> enchantL = EnchantmentHelper.getEnchantments(e.left);
    		Map<Integer, Integer> enchantR = EnchantmentHelper.getEnchantments(e.right);
    		ItemStack out = new ItemStack(e.left.getItem(), 1, e.left.getItemDamage());
    		e.materialCost = 0;
    		int n, m, cost = 0;
    		for (int id : enchantR.keySet()) {
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
    			e.materialCost = 1;
    		}
    		EnchantmentHelper.setEnchantments(enchantL, out);
    		n = out.getItemDamage();
    		if (n > 0) {
    			if (e.right.getItem() == e.left.getItem()) {
    				m = n - e.right.getMaxDamage() + e.right.getItemDamage();
    				e.materialCost = 1;
    			} else {
    				ItemStack rm = repairMaterials.get(out.getItem());
    				if (rm != null && rm.isItemEqual(e.right)) {
    					m = Math.min(e.right.stackSize, (int)Math.ceil((float)n * (float)rm.stackSize / (float)out.getMaxDamage()));
    					e.materialCost = m;
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
    		if (e.materialCost > 0) {
    			e.output = out;
    			e.cost = cost;
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
        return register && (state.getBlock().getMaterial() == Material.rock || state.getBlock().getMaterial() == Material.iron || state.getBlock().getMaterial() == Material.anvil);
    }
    
}
