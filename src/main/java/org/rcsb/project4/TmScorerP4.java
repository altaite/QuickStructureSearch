package org.rcsb.project4;

import java.io.Serializable;
import java.util.List;

import javax.vecmath.Point3d;

import org.apache.spark.Accumulator;
import org.biojava.nbio.structure.AminoAcidImpl;
import org.biojava.nbio.structure.Atom;
import org.biojava.nbio.structure.AtomImpl;
import org.biojava.nbio.structure.Chain;
import org.biojava.nbio.structure.ChainImpl;
import org.biojava.nbio.structure.Group;
import org.biojava.nbio.structure.StructureException;
import org.biojava.nbio.structure.align.model.AFPChain;
import org.biojava.nbio.structure.align.util.AFPChainScorer;

public class TmScorerP4 implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final String CA_NAME = "CA";
	private static final String GROUP_NAME = "GLU";

	public static Float[] getFatCatTmScore(Point3d[] points1, Point3d[] points2, List<Accumulator<Long>> timers) {
		Float[] scores = new Float[6];
		
		Atom[] ca1 = getCAAtoms(points1);
		Atom[] ca2 = getCAAtoms(points2);
		
		AFPChain afp = null;
		try {
			FatCatRigidP4 fatCat  = new FatCatRigidP4();
			afp = fatCat.align(ca1,ca2,timers);
			double tmScore = AFPChainScorer.getTMScore(afp, ca1, ca2);
			afp.setTMScore(tmScore);
		} catch (StructureException e) {
			e.printStackTrace();
			return scores;
		}        

		scores[0] = (float) afp.getTMScore();
		scores[1] = (float) afp.getTotalRmsdOpt();
		scores[2] = (float) afp.getProbability();
		scores[3] = (float) afp.getOptLength();
		scores[4] = (float) afp.getCoverage1();
		scores[5] = (float) afp.getCoverage2();
		return scores;
	}

	private static Atom[] getCAAtoms(Point3d[] points) {
		int gaps = 0;
		for (Point3d p: points) {
			if (p == null) {
				gaps++;
			}
		}
		Chain c = new ChainImpl();
		c.setChainID("A");		

		Atom[] atoms = new Atom[points.length-gaps];

		for (int i = 0, j = 0; i < points.length; i++) {
			if (points[i] != null) {
				atoms[j] = new AtomImpl();
				atoms[j].setName(CA_NAME);
				Group g = new AminoAcidImpl();
				g.setPDBName(GROUP_NAME);
				g.addAtom(atoms[j]);
				c.addGroup(g);

				atoms[j].setX(points[i].x);
				atoms[j].setY(points[i].y);
				atoms[j].setZ(points[i].z);
				j++;
			}
		}

		return atoms;
	}
}
