package org.rcsb.projectm;

import java.io.ByteArrayOutputStream;

import java.util.zip.GZIPOutputStream;

/**
 * This class calculates the Normalized Manhattan Distance between two arrays containing the 12 moments describing the protein.
 * 
 * @author  Michael Wang
 */

public class NormalizedManhattanDistance {
	
	public static double distance(double[] s, double[] t) {
		
		double manhattanDis = 0;
		for(int i = 0; i < s.length; i++)
		{
			manhattanDis += Math.abs(s[i] - t[i]);
		}
		
		return (1.0)/(1.0 + (manhattanDis / 12));
	}
}