package cd4017be.automation.gas;

import net.minecraft.nbt.NBTTagCompound;

public class Mix {
	public final int e;
	public final float[] c;
	
	public static Mix read(NBTTagCompound nbt, String k, Mix alt) {
		int[] data = nbt.getIntArray(k);
		if (data.length == 0) return alt;
		int n = Integer.bitCount(data[0]);
		if (n == 1) return new Mix(data[0], null);
		if (n == 0 || data.length <= n) return alt;
		float[] c = new float[n];
		float cs = 0;
		for (int i = 0; i < n; i++) cs += (c[i] = Float.intBitsToFloat(data[i + 1]));
		if (Float.isNaN(cs) || Float.isInfinite(cs) || cs == 0) return alt;
		if (cs != 1) for (int i = 0; i < n; i++) c[i] /= cs;
		return new Mix(data[0], c);
	}
	
	public void write(NBTTagCompound nbt, String k) {
		int[] data = new int[c == null ? 1 : c.length + 1];
		data[0] = e;
		for (int i = 1; i < data.length; i++) data[i] = Float.floatToIntBits(c[i - 1]);
		nbt.setIntArray(k, data);
	}
	
	public Mix(int e, float[] c) {
		this.e = e;
		this.c = c;
	}
	
	public Mix(int id) {
		e = 1 << id;
		c = null;
	}
	
	public Mix add(int t, float r1) {
		int i = e >> t;
		t = 1 << t;
		float r0 = 1F - r1;
		if ((i & 1) != 0) {
			if (c == null) return this;
			i = c.length - Integer.bitCount(i);
			for (int j = 0; j < c.length; j++) c[j] *= r0;
			c[i] += r1;
			return this;
		}
		if (c == null) return new Mix(e | t, t > e ? new float[]{r0, r1} : new float[]{r1, r0});
		float[] arr = new float[c.length + 1];
		i = c.length - Integer.bitCount(i);
		for (int j = 0; j < arr.length; j++)
			arr[j] = j == i ? r1 : c[j < i ? j : j - 1] * r0;
		return new Mix(e | t, arr);
	}
	
	public Mix remove(int t, float r1) {
		int i = e >> t;
		if (c == null || (i & 1) == 0) return this;
		i = c.length - Integer.bitCount(i);
		if (c[i] > r1) {
			float r0 = 1F / (1F - r1);
			for (int j = 0; j < c.length; j++) c[j] *= r0;
			c[i] -= r1;
			return this;
		}
		float r0 = 1F / (1F - c[i]);
		float[] arr = new float[c.length - 1];
		for (int j = 0; j < arr.length; j++)
			arr[j] = c[j < i ? j : j + 1] * r0;
		return new Mix(e & ~(1 << t), arr);
	}
	
	public Mix merge(Mix mix, float r1) {
		if (mix == this) return this;
		float r0 = 1F - r1;
		if (e == mix.e) {
			for (int i = 0; i < c.length; i++) 
				c[i] = c[i] * r0 + mix.c[i] * r1;
			return this;
		} 
		int ng = e | mix.e, k0 = Integer.lowestOneBit(ng);
		if (e == ng) {
			for (int i = 0, j = 0, k = k0; i < c.length; k <<= 1) {
				if ((mix.e & k) != 0) {
					c[i] = c[i] * r0 + mix.c[j++] * r1;
					i++;
				} else if ((e & k) != 0) i++;
			}
			return this;
		}
		float[] arr = new float[Integer.bitCount(ng)];
		for (int i = 0, j = 0, l = 0, k = k0; l < arr.length; k <<= 1) {
			if ((ng & k) != 0) {
				if ((mix.e & k) != 0) arr[l] += mix.c[j++] * r1;
				if ((e & k) != 0) arr[l] += c[i++] * r0;
				l++;
			}
		}
		return new Mix(ng, arr);
	}
	
	public float c(int t) {
		int i = e >> t;
		if ((i & 1) == 0) return 0F;
		return c[c.length - Integer.bitCount(i)];
	}
}
