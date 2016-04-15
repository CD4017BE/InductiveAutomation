package cd4017be.automation.pipes;

import cd4017be.automation.Item.PipeUpgradeItem;
import cd4017be.lib.util.Utils;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

public class InventoryInterface {


	public static class InvAccess {
		public IInventory inv;
		public final byte side;
		public int[] slots;
		public final byte dir;
		public PipeUpgradeItem filter;
		
		public InvAccess(IInventory inv, int[] slots, byte side, byte dir) {
			this.inv = inv;
			this.side = side;
			this.dir = dir;
			this.slots = slots;
		}
		
		public boolean valid() {
			if (((TileEntity)inv).isInvalid()) {
				return false;
			}
			return true;
		}
		
		public static InvAccess create(TileEntity te, byte side, byte dir) {
			if (te == null || !(te instanceof IInventory)) return null;
			IInventory inv = (IInventory)te;
			int[] slots = Utils.accessibleSlots(inv, side);
			if (inv instanceof ISidedInventory && dir != 0) {
				try {//exclude slots that never allow extraction/insertion
					EnumFacing f = EnumFacing.VALUES[dir];
					ISidedInventory si = (ISidedInventory)inv;
					int p0, p1;
					if (dir < 0)
						for (p0 = 0, p1 = 0; p1 < slots.length;) {
							if (p0 != p1) slots[p0] = slots[p1];
							if (si.canExtractItem(slots[p1], null, f)) p0++;
							p1++;
						}
					else for (p0 = 0, p1 = 0; p1 < slots.length;) {
						if (p0 != p1) slots[p0] = slots[p1];
						if (si.canInsertItem(slots[p1], null, f)) p0++;
						p1++;
					}
					if (p0 != p1) {
						int[] buff = new int[p0];
						System.arraycopy(slots, 0, buff, 0, p0);
						slots = buff;
					}
				} catch (NullPointerException e) {}
			}
			if (slots.length == 0) return null;
			return new InvAccess(inv, slots, side, dir);
		}
	}
	
}
