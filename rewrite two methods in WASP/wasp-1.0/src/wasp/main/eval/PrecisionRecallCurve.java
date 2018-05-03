/*
 * Copyright 2006 Yuk Wah Wong (The University of Texas at Austin).
 * 
 * This file is part of the WASP distribution.
 *
 * WASP is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * WASP is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with WASP; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package wasp.main.eval;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeSet;

import wasp.math.Math;

/**
 * This evaluator generates precision-recall curves.
 * 
 * @author ywwong
 *
 */
public class PrecisionRecallCurve extends ParserEvaluator {

	private static final int NUM_INTERVALS = 100;
	
	protected void summarize(PrintWriter out, TreeSet[] entries) {
		out.println("begin precision-recall-curve");
		double[][] curves = new double[NUM_INTERVALS+1][entries.length];
		for (int i = 0; i < entries.length; ++i)
			precisions(curves, i, entries[i]);
		out.println("begin mean");
		for (int i = 0; i <= NUM_INTERVALS; ++i) {
			double recall = ((double) i)/NUM_INTERVALS;
			double precision = Math.mean(curves[i]);
			out.println(recall+" "+precision);
		}
		out.println("end mean");
		out.println("begin 95%-confidence-interval");
		for (int i = 0; i <= NUM_INTERVALS; ++i) {
			double recall = ((double) i)/NUM_INTERVALS;
			double[] interval = Math.confInterval95(curves[i]);
			out.println(recall+" "+interval[0]+" "+interval[1]);
		}
		out.println("end 95%-confidence-interval");
		out.println("end precision-recall-curve");
	}

	private void precisions(double[][] curves, int trial, TreeSet entries) {
		ArrayList list = new ArrayList();
		int size = entries.size();
		int ntranslated = 0;
		int ncorrect = 0;
		for (Iterator it = entries.iterator(); it.hasNext();) {
			Entry entry = (Entry) it.next();
			if (entry.isTranslated) {
				++ntranslated;
				if (entry.isCorrect) {
					++ncorrect;
					double[] pair = new double[2];
					pair[0] = ((double) ncorrect)/size;         // recall
					pair[1] = ((double) ncorrect)/ntranslated;  // precision
					list.add(pair);
				}
			}
		}
		for (int i = NUM_INTERVALS; i >= 0; --i) {
			double level = ((double) i)/NUM_INTERVALS;
			double nextLevel = ((double) (i+1))/NUM_INTERVALS;
			curves[i][trial] = 0;
			for (Iterator jt = list.iterator(); jt.hasNext();) {
				double[] pair = (double[]) jt.next();
				if (level <= pair[0] && pair[0] <= nextLevel)
					curves[i][trial] = Math.max(curves[i][trial], pair[1]);
			}
			if (i < NUM_INTERVALS)
				curves[i][trial] = Math.max(curves[i][trial], curves[i+1][trial]);
		}
	}
	
}
