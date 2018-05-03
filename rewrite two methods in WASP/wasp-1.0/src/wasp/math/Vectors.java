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
package wasp.math;

/**
 * Common vector operations.
 * 
 * @author ywwong
 *
 */
public class Vectors {

	private Vectors() {}
	
	public static void add(double[] X, double a, double[] Y) {
		for (int i = 0; i < X.length; ++i)
			X[i] += a*Y[i];
	}
	
	public static double[] addCopy(double[] X, double a, double[] Y) {
		double[] Z = (double[]) X.clone();
		for (int i = 0; i < Z.length; ++i)
			Z[i] += a*Y[i];
		return Z;
	}

	public static void assign(double[] X, double[] Y) {
		for (int i = 0; i < X.length; ++i)
			X[i] = Y[i];
	}
	
	public static double dotProduct(double[] X, double[] Y) {
		double a = 0;
		for (int i = 0; i < X.length; ++i)
			a += X[i]*Y[i];
		return a;
	}
	
	public static void multiply(double[] X, double a) {
		for (int i = 0; i < X.length; ++i)
			X[i] *= a;
	}
	
	public static double twoNorm(double[] X) {
		double norm = 0;
		for (int i = 0; i < X.length; ++i)
			norm += X[i]*X[i];
		return Math.sqrt(norm);
	}
	
}
