package org.rcsb.structuralSimilarity;

import java.util.List;

import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.broadcast.Broadcast;
import org.apache.spark.mllib.linalg.Vector;

import scala.Tuple2;

public class PairSimilarityCalculator implements PairFunction<Tuple2<Integer,Integer>,String,Float> {
	private static final long serialVersionUID = 1L;
	private Broadcast<List<Tuple2<String,Vector>>> data = null;


	public PairSimilarityCalculator(Broadcast<List<Tuple2<String,Vector>>> data) {
		this.data = data;
	}

	public Tuple2<String, Float> call(Tuple2<Integer, Integer> tuple) throws Exception {
		Tuple2<String,Vector> t1 = this.data.getValue().get(tuple._1);
		Tuple2<String,Vector> t2 = this.data.getValue().get(tuple._2);
		
		StringBuilder key = new StringBuilder();
		key.append(t1._1);
		key.append(",");
		key.append(t2._1);
		
		double[] v1 = t1._2.toArray();
		double[] v2 = t2._2.toArray();

		float union = 0;
		float intersection = 0;
		for (int i = 0, n = v1.length; i < n; i++) {
			if (v1[i] < v2[i]) {
				intersection += v1[i];
				union += v2[i];
			} else {
				intersection += v2[i];
				union += v1[i];
			}
		}

		float value = 0;
		if (union > 0) {
		    value = intersection/union;
		}
		
        return new Tuple2<String, Float>(key.toString(), value);
    }
}
