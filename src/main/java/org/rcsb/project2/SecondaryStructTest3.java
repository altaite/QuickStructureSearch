package org.rcsb.project2;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.vecmath.Point3d;

import org.apache.hadoop.io.ArrayWritable;
import org.apache.hadoop.io.Text;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.rcsb.hadoop.io.HadoopToSimpleChainMapper;
import org.rcsb.hadoop.io.SimplePolymerChain;

import scala.Tuple2;

public class SecondaryStructTest3 {
	private static final int NUM_THREADS = 4;
	private static final int NUM_TASKS_PER_THREAD = 3;

	public static void main(String[] args) throws IOException {
		Set<String> needed = new HashSet<>();
		Map<String, Point3d[]> pts = new HashMap<>();
		ChainPair[] pairs = new ChainPair[0];
		int N = 0;
		String date;
		date = new SimpleDateFormat("yyyy_MM_dd__hh-mm-ss").format(new Date());
		PrintWriter out = new PrintWriter(new FileWriter("output_" + date + ".csv"));
		try (BufferedReader br = new BufferedReader(new FileReader("testsethuge.csv"))) {
			N = Integer.parseInt(br.readLine());
			pairs = new ChainPair[N];
			for (int i = 0; i < N; i++) {
				String[] spl = br.readLine().split(",");
				pairs[i] = new ChainPair();
				pairs[i].setN1(spl[0]);
				pairs[i].setN2(spl[1]);
				pairs[i].setTm(Double.parseDouble(spl[2]));
				System.out.println(pairs[i]);
				needed.add(spl[0]);
				needed.add(spl[1]);
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		addAllIntoMap(needed, pts, args[0]);
		for (int i = 0; i < N; i++) {
			System.out.println((i + 1) + " / " + N);
			if (pts.containsKey(pairs[i].getN1()) && pts.containsKey(pairs[i].getN2()))
				out.printf("%s,%s,%.5f,%.5f" + System.lineSeparator(), pairs[i].getN1(), pairs[i].getN2(),
						pairs[i].getTm(),
						SecondaryStructTools.calculateScore(pts.get(pairs[i].getN1()), pts.get(pairs[i].getN2())));
		}
		out.close();
	}

	public static void addAllIntoMap(Set<String> needed, Map<String, Point3d[]> pts, String path) {
		SparkConf conf = new SparkConf().setMaster("local[" + NUM_THREADS + "]")
				.setAppName(FingerprintMapperTest2.class.getSimpleName())
				.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer");
		JavaSparkContext sc = new JavaSparkContext(conf);
		List<Tuple2<String, SimplePolymerChain>> m = sc
				.sequenceFile(path, Text.class, ArrayWritable.class, NUM_THREADS * NUM_TASKS_PER_THREAD)
				.mapToPair(new HadoopToSimpleChainMapper())
				//
				.filter(a -> a._2.isProtein())
				//
				.filter(a -> a._2.getCoordinates().length > 50)
				.filter(new Function<Tuple2<String, SimplePolymerChain>, Boolean>() {
					@Override
					public Boolean call(Tuple2<String, SimplePolymerChain> v1) throws Exception {
						return needed.contains(v1._1);
					}
				}).collect();
		for (Tuple2<String, SimplePolymerChain> t : m)
			pts.put(t._1, t._2.getCoordinates());
		System.out.println("Needed: " + needed.size());
		System.out.println("Got: " + pts.size());
		sc.close();
	}
}
