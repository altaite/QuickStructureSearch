package org.rcsb.compress;

import java.io.Serializable;

public class UnsignedTransform implements IntegerTransform, Serializable {
	private static final long serialVersionUID = 1L;

	@Override
	public int[] forward(int[] data) {
		int[] out = new int[data.length];
		System.arraycopy(data, 0, out, 0, data.length);
		for (int i = 0; i < out.length; i++) {
			out[i] = toUnsignedInt(out[i]);
		}
		
		return out;
	}

	@Override
	public int[] reverse(int[] data) {
		int[] out = new int[data.length];
		System.arraycopy(data, 0, out, 0, data.length);
		for (int i = 0; i < out.length; i++) {
			out[i] = toSignedInt(out[i]);
		}
		
		return out;
	}
	
	private static int toUnsignedInt(final int n) {
	    return (n << 1) ^ (n >> 31);
	}
	
	private static int toSignedInt(final int n) {
	    return (n >>> 1) ^ -(n & 1);
    }
}
