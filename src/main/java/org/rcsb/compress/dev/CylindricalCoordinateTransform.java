package org.rcsb.compress.dev;

import java.io.Serializable;

import org.rcsb.compress.IntegerTransform;

/**
 * https://en.wikipedia.org/wiki/Cylindrical_coordinate_system
 * @author peter
 *
 */
public class CylindricalCoordinateTransform implements IntegerTransform, Serializable {
	private static final long serialVersionUID = 1L;
	private static final double scale = 2048;

	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}
	
	@Override
	public int[] forward(int[] data) {
		int[] out = new int[data.length];
		System.arraycopy(data, 0, out, 0, data.length);
		int len = data.length/3;

		for (int i = 0; i < len; i++) { 
			int x = out[i];
			int y = out[i+len];
			double r = Math.sqrt(x*x + y*y);
			double theta = Math.atan2(y, x);
			out[i] = (int) Math.round(r);
			out[i+len] = (int) Math.round(theta*scale);
			System.out.println(out[i] + " " + out[i+len] + " " + out[i+len+len]);
		}
		
		return out;
	}

	@Override
	public int[] reverse(int[] data) {
		int[] out = new int[data.length];
		System.arraycopy(data, 0, out, 0, data.length);
	//	int r = 0;
		int len = data.length/3;
		for (int i = 0; i < len; i++) { 
//			r += out[i];
			int r = out[i];
			int theta = out[i+len];
			double x = r * Math.cos(theta/scale);
			double y = r * Math.sin(theta/scale);
			out[i] = (int) Math.round(x);
			out[i+len] = (int) Math.round(y);
		}
		
		return out;
	}
}
