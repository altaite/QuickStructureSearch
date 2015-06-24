package org.rcsb.structuralSimilarity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.BitSet;
import java.util.zip.GZIPOutputStream;


public class NormalizedCompressionDistance {
	// http://en.wikipedia.org/wiki/Levenshtein_distance

	
	public static void main(String[] args) {
		int[] v1 = {1,2,3,4,5,6,7,8,9};
		int[] v2 = {2,3,4,6};
	}
	
	public static double distance(int[] s, int[] t)
	{
	    if (s.length == 0) return t.length;
	    if (t.length == 0) return s.length;

        int slen = length(s);
        int tlen = length(t);
        int clen = length(combine(s,t));
        int clen2 = length(combine(t,s));
        clen = Math.min(clen, clen2);
 //       System.out.println("s,t,c; " + slen + "," + tlen + "," + clen + ","  + clen2);
//        System.out.println((clen - Math.min(slen, tlen))/(double)Math.max(slen,  tlen));
//        System.out.println((clen2 - Math.min(slen, tlen))/(double)Math.max(slen,  tlen));
        
		return 1 - (clen - Math.min(slen, tlen))/(double)Math.max(slen,  tlen);
	}
	
	private static int[] combine(int[] s, int[] t) {
		int[] v = new int[s.length+t.length];
		for (int i = 0; i < s.length; i++) {
			v[i] = s[i];
		}
		for (int i = 0; i < t.length; i++) {
			v[s.length+i] = t[i];
		}
		return v;
	}
	
	private static int length(int[] values) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		GZIPOutputStream zos;
		try {
			zos = new GZIPOutputStream(baos);

			for (int v: values) {
				zos.write(v);
			}
			zos.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return baos.size();
	}
}
