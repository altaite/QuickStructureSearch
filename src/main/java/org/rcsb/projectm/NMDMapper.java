package org.rcsb.projectm;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.vecmath.Point3d;

import org.apache.spark.broadcast.Broadcast;
import org.rcsb.project3.*;

import scala.Tuple2;

/**
 * This class maps a pair of chains, specified by two indices into the broadcasted sequences list, to
 * a MeetMin Index. It calculates the MeetMin index for multi-sets.
 * 
 * Order of fragments are getting ignored for MeetMin index calculation.
 * 
 * @author  Peter Rose, Chris Li
 */
public class NMDMapper implements AlignmentAlgorithmInterface {
	private static final long serialVersionUID = 1L;
	private Broadcast<Map<String,SequenceFeatureInterface<?>>> sequences = null;

	public static void main(String[] args) {
		
	}
	public NMDMapper() {
	}

	public NMDMapper(Broadcast<Map<String,SequenceFeatureInterface<?>>> sequences) {
		this.sequences = sequences;
	}

	public String getName() {
		return "NormalizedManhattanDistance";
	}
	
	/**
	 * Returns <PdbId.Chain, MeetMin Index> pairs. This is an extension of the 
	 * MeetMin Index to multi-sets. The multi-sets are represented as vectors,
	 * where each vector element is a feature count.
	 */
	@SuppressWarnings("unchecked")
	public Tuple2<String, Float> call(Tuple2<String, String> tuple) throws Exception {
		SequenceFeatureInterface<Double> v1 = (SequenceFeatureInterface<Double>) this.sequences.getValue().get(tuple._1);
		SequenceFeatureInterface<Double> v2 = (SequenceFeatureInterface<Double>) this.sequences.getValue().get(tuple._2);
		
		if(v1 == null || v2 == null) {
			return null;
		}

		String key = tuple._1 + "," + tuple._2;
		float value = nMd(v1, v2);
	
        return new Tuple2<String, Float>(key, value);
    }

	@Override
	public void setSequence(Broadcast<Map<String, SequenceFeatureInterface<?>>> sequences) {
		this.sequences = sequences;
	}

	/**
	 * Not used in this algorithm
	 */
	@Override
	public void setCoords(Broadcast<List<Tuple2<String, Point3d[]>>> coords) {		
	}
	
	private <T> float nMd(SequenceFeatureInterface<T> s1, SequenceFeatureInterface<T> s2) {
		double[] x1 = new double[s1.length()];
		double[] x2 = new double[s2.length()];
		for(int i=0;i<s1.length();i++){
			x1[i] = (double) s1.get(i);
		}
		
		for(int i=0;i<s2.length();i++){
			x2[i] = (double) s2.get(i);
		}
		
		
		return (float) NormalizedManhattanDistance.distance(x1, x2);
	}
	
	/**
	 * Counts how many times each feature occurs in the sequence.
	 * @param sequence map where the key represents the feature and the value represents the feature count
	 * @return
	 */
	private <T> Map<T, Integer> calcFeatureCounts(SequenceFeatureInterface<T> sequence) {
		Map<T, Integer> featureCounts = new HashMap<T, Integer>(sequence.length());
		for (T key: sequence.getSequence()) {
			Integer count = featureCounts.getOrDefault(key, 0);
			featureCounts.put(key, count+1);
		}
		return featureCounts;
	}
}
