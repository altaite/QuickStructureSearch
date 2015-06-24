package org.rcsb.project4;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.vecmath.Point3d;

import org.apache.hadoop.io.ArrayWritable;
import org.apache.hadoop.io.Text;
import org.apache.spark.Accumulator;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.broadcast.Broadcast;
import org.rcsb.structuralSimilarity.ChainPairLengthFilter;
import org.rcsb.structuralSimilarity.GapFilter;
import org.rcsb.structuralSimilarity.LengthFilter;
import org.rcsb.structuralSimilarity.SeqToChainMapper;

import scala.Tuple2;

/**
 * This class creates structural alignments between random protein chain pairs 
 * using jFatCAT and scores the alignments with the TM score
 * 
 * @author  Peter Rose
 */
public class project4Tester { 
	private static int NUM_THREADS = 8;
	private static int NUM_TASKS_PER_THREAD = 3; // Spark recommends 2-3 tasks per thread
	private static int BATCH_SIZE = 50;

	public static void main(String[] args ) throws FileNotFoundException
	{
		String sequenceFileName = args[0]; 
		String outputFileName = args[1];
		int nPairs = Integer.parseInt(args[2]);
		int seed = Integer.parseInt(args[3]);
		
		long t1 = System.nanoTime();
		project4Tester creator = new project4Tester();
		creator.run(sequenceFileName, outputFileName, nPairs, seed);
		System.out.println("Running Time: " + ((System.nanoTime()-t1)/1E9) + " s");
	}

	private void run(String path, String outputFileName, int nPairs, int seed) throws FileNotFoundException {
		// setup spark
		SparkConf conf = new SparkConf()
				.setMaster("local[" + NUM_THREADS + "]")
				.setAppName("1" + this.getClass().getSimpleName())
				.set("spark.driver.maxResultSize", "2g");
//				.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer");

		JavaSparkContext sc = new JavaSparkContext(conf);
		
		// Step 1. calculate <pdbId.chainId, feature vector> pairs
        List<Tuple2<String, Point3d[]>> chains = sc
				.sequenceFile(path, Text.class, ArrayWritable.class, NUM_THREADS)  // read protein chains
			//	.sample(false, 0.1, 123456) // use only a random fraction, i.e., 10%
				.mapToPair(new SeqToChainMapper()) // convert input to <pdbId.chainId, CA coordinate> pairs
				.filter(new GapFilter(0, 0)) // keep protein chains with gap size <= 0 and 0 gaps
				.filter(new LengthFilter(50,500)) // keep protein chains with 50 - 500 residues
				.collect(); // return results to master node

		// Step 2.  broadcast feature vectors to all nodes
		final Broadcast<List<Tuple2<String,Point3d[]>>> chainsBc = sc.broadcast(chains);
		final Accumulator<Long> timer1 = sc.accumulator(new Long(0),new TimeAccumulator());
		final Accumulator<Long> timer2 = sc.accumulator(new Long(0),new TimeAccumulator());
		final Accumulator<Long> timer3 = sc.accumulator(new Long(0),new TimeAccumulator());
		final Accumulator<Long> timer4 = sc.accumulator(new Long(0),new TimeAccumulator());
		List<Accumulator<Long>> timers = new ArrayList<Accumulator<Long>>();
		timers.add(timer1);
		timers.add(timer2);
		timers.add(timer3);
		timers.add(timer4);
		int nChains = chains.size();
		
		Random r = new Random(seed);

		PrintWriter writer = new PrintWriter(outputFileName);
		
		for (int i = 0; i < nPairs; i+=BATCH_SIZE) {
			List<Tuple2<Integer,Integer>> pairs = randomPairs(nChains, BATCH_SIZE, r.nextLong());

			List<Tuple2<String, Float[]>> list = sc
					.parallelizePairs(pairs, NUM_THREADS*NUM_TASKS_PER_THREAD) // distribute data
					.filter(new ChainPairLengthFilter(chainsBc, 0.5, 1.0)) // restrict the difference in chain length
					.mapToPair(new ChainPairToTmMapperP4(chainsBc,timers)) // maps pairs of chain id indices to chain id, TM score pairs
					//				.filter(s -> s._2 > 0.9f) //
					.collect();	// copy result to master node

			// write results to .csv file
			writeToCsv(writer, list);
		}

		writer.close();
		
		System.out.println("Total time cost    	: " + timers.get(3).value()/1E9 + " s");
		System.out.println("Atom time cost     	: " + timers.get(0).value()/1E9 + " s");
		System.out.println("Algorithm time cost	: " + timers.get(1).value()/1E9 + " s");
		System.out.println("Get TM time cost   	: " + timers.get(2).value()/1E9 + " s");
		
		sc.stop();
		sc.close();

		System.out.println("protein chains     	: " + nChains);
		System.out.println("ramdom pairs       	: " + nPairs);
	}

	/**
	 * Writes pairs of chain ids and calculated similarity score to a csv file
	 * @param writer
	 * @param list
	 */
	private static void writeToCsv(PrintWriter writer, List<Tuple2<String, Float[]>> list) {
		for (Tuple2<String, Float[]> t : list) {
			writer.print(t._1);
			for (Float f: t._2) {
				writer.print(",");
			    writer.print(f);
			}
			writer.println();
		}
		writer.flush();
	}

	/**
	 * Returns random pairs of indices for the pairwise comparison.
	 * @param n number of feature vectors
	 * @return
	 */
	private List<Tuple2<Integer, Integer>> randomPairs(int n, int nPairs, long seed) {
		Random r = new Random(seed);
		Set<Tuple2<Integer,Integer>> set = new HashSet<>(nPairs);

		for (int i = 0; i < nPairs; i++) {
			int j = r.nextInt(n);
			int k = r.nextInt(n);
			if (j == k) {
				continue;
			}

			Tuple2<Integer,Integer> tuple = new Tuple2<>(j,k);
			if (! set.contains(tuple)) {
			    set.add(tuple);
			}
		}
		return new ArrayList<Tuple2<Integer,Integer>>(set);
	}
}

