package org.rcsb.project3;

import org.apache.commons.lang.ArrayUtils;

public class AngleSequenceFeature implements SequenceFeatureInterface<Double> {

	private double[] AngleSequence;
	// Some setting for calculate similarity score 
	private double diff = 3.14/72;
	private double match = 1;
    private double mismatch = -1;
    private double gap = -1;
	
    /**
     * Constructor that will store a double array of angle
     * @param AngleSequence
     */
	public AngleSequenceFeature(double[] AngleSequence) {
		this.AngleSequence = AngleSequence;
	}
	
	/**
	 * Constructor that will store a double array of angle and update the settings
	 * @param AngleSequence
	 * @param diff
	 * @param gap
	 * @param match
	 * @param mismatch
	 */
	public AngleSequenceFeature(double[] AngleSequence, double diff, double gap, double match, double mismatch) {
		this.AngleSequence = AngleSequence;
		this.diff = diff;
		this.gap = gap;
		this.match = match;
		this.mismatch = mismatch;
	}
		
	@Override
	public double similarity(SequenceFeatureInterface<?> sequence2, int i, int j) {
		// check NaN as gap
		if (Double.isNaN(this.get(i)) || Double.isNaN((Double)sequence2.get(j))){
			return gap;
		}
		// check similarity
		else if (((this.get(i)+diff) > (double)sequence2.get(j)) && ((this.get(i)-diff) < (double)sequence2.get(j)))
			return match;
		else 
			return mismatch;
	}

	@Override
	public boolean identity(SequenceFeatureInterface<?> sequence2, int i, int j) {
		// check NaN as gap
		if (Double.isNaN(this.get(i)) || Double.isNaN((double)sequence2.get(j))){
			return false;
		}
		// check identity
		else if (((this.get(i)+diff) > (double)sequence2.get(j)) && ((this.get(i)-diff) < (double)sequence2.get(j)))
			return true;
		else 
			return false;
	}

	@Override
	public Double get(int index) {
		return AngleSequence[index];
	}

	@Override
	public int length() {
		return AngleSequence.length;
	}

	@Override
	public Double[] getSequence() {
		return ArrayUtils.toObject(AngleSequence);
	}

}