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
import java.util.TreeSet;

import wasp.math.Math;

/**
 * This evaluator implements the F-measure metric.  F-measure is the harmonic mean of precision and
 * recall.
 * 
 * @author ywwong
 *
 */
public class FMeasure extends ParserEvaluator {

	protected void summarize(PrintWriter out, TreeSet[] entries) {
		out.println("begin f-measure");
		double[] f = new double[entries.length];
		for (int i = 0; i < entries.length; ++i)
			f[i] = f(entries[i]);
		out.println("mean "+Math.mean(f));
		double[] interval = Math.confInterval95(f);
		out.println("95%-confidence-interval "+interval[0]+" "+interval[1]);
		out.println("end f-measure");
	}

	private double f(TreeSet entries) {
		double precision = new Precision().precision(entries);
		double recall = new Recall().recall(entries);
		return (precision==0 && recall==0) ? 0 : (2*precision*recall) / (precision+recall);
	}
	
}
