package org.rcsb.project3;

import java.io.Serializable;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
/**
 * This class generate fingerprint that is a sequence of angles which is the angle 
 * between each three points.
 * @author Chris Li
 * 
 */
public class AngleSequenceFingerprint implements SequenceFingerprint, Serializable {

	private static final long serialVersionUID = 1L;

	/**
     * Returns a fingerprint as sequence for the given chain. 
     * @param coords coordinates of a macromolecule fragment
     * @return
     */
	public AngleSequenceFeature getFingerprint(Point3d[] coordinates) {
		// the angle feature array
		double[] features = new double[coordinates.length-1];
		// each two points generate a vector
		Vector3d v1 = new Vector3d();
		Vector3d v2 = new Vector3d();
		for (int i = 1; i < coordinates.length-1; i++) {
			if (coordinates[i-1] == null || coordinates[i] == null || coordinates[i+1] == null)
				continue;
			v1.set(coordinates[i-1]);
			v1.sub(coordinates[i]);
			v2.set(coordinates[i+1]);
			v2.sub(coordinates[i]);
			// the angle between two vectors
			features[i] = v2.angle(v1);
		}
		return new AngleSequenceFeature(features);
	}

	public String getName() {
		return this.getClass().getSimpleName();
	}
}
