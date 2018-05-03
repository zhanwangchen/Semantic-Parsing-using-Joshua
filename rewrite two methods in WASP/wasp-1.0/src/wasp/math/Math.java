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
 * Common math operations.
 * 
 * @author ywwong
 * @author Christopher Manning
 *
 */
public class Math {

	private Math() {}

	public static double abs(double x) {
		return java.lang.Math.abs(x);
	}
	
	public static double exp(double x) {
		return java.lang.Math.exp(x);
	}
	
	public static double log(double x) {
		return java.lang.Math.log(x);
	}
	
	public static double sqrt(double x) {
		return java.lang.Math.sqrt(x);
	}
	
	public static double max(double x1, double x2) {
		return (x1 > x2) ? x1 : x2;
	}
	
	public static double min(double x1, double x2) {
		return (x1 < x2) ? x1 : x2;
	}
	
	public static int min(int x1, int x2) {
		return (x1 < x2) ? x1 : x2;
	}
	
	public static double mean(double[] array) {
		double sum = 0;
		for (int i = 0; i < array.length; ++i)
			sum += array[i];
		return (array.length==0) ? 0 : sum/array.length;
	}
	
	public static double meanSq(double[] array) {
		double sumSq = 0;
		for (int i = 0; i < array.length; ++i)
			sumSq += array[i]*array[i];
		return (array.length==0) ? 0 : sumSq/array.length;
	}
	
	public static double stdDev(double[] array) {
		double mean = mean(array);
		double meanSq = meanSq(array);
		return java.lang.Math.sqrt(meanSq - mean*mean);
	}
	
	public static double[] confInterval95(double[] array) {
		double mean = mean(array);
		double stdErr = stdDev(array) / java.lang.Math.sqrt(array.length);
		double[] interval = new double[2];
		interval[0] = mean - 1.96*stdErr;
		interval[1] = mean + 1.96*stdErr;
		return interval;
	}
	
    /** If a difference is bigger than this in log terms, then the sum or
     *  difference of them will just be the larger (to 12 or so decimal
     *  places). 
     */
    private static final double LOG_TOLERANCE = 30.0;

    /** Returns the log of the sum of two numbers, which are
     *  themselves input in log form.  This uses natural logarithms.
     *  Reasonable care is taken to do this as efficiently as possible
     *  (under the assumption that the numbers might differ greatly in
     *  magnitude), with high accuracy, and without numerical overflow.
     *  Also, handle correctly the case of arguments being -Inf (e.g.,
     *  probability 0).
     *  
     *  @param lx First number, in log form
     *  @param ly Second number, in log form
     *  @return log(exp(lx) + exp(ly))
     */
    public static double logAdd(double lx, double ly) {
        double max, negDiff;
        if (lx > ly) {
            max = lx;
            negDiff = ly - lx;
        } else {
            max = ly;
            negDiff = lx - ly;
        }
        if (max == Double.NEGATIVE_INFINITY) {
            return max;
        } else if (negDiff < -LOG_TOLERANCE) {
            return max;
        } else {
            return max + log(1.0 + exp(negDiff));
        }
    }           

    /** Returns the log of the sum of an array of numbers, which are
     *  themselves input in log form.  This is all natural logarithms.
     *  Reasonable care is taken to do this as efficiently as possible
     *  (under the assumption that the numbers might differ greatly in
     *  magnitude), with high accuracy, and without numerical overflow.
     *  
     *  @param logInputs An array of numbers [log(x1), ..., log(xn)]
     *  @return log(x1 + ... + xn)
     */
    public static double logSum(double[] logInputs) {
        int leng = logInputs.length;
        if (leng == 0) {
            throw new IllegalArgumentException();
        }
        int maxIdx = 0;
        double max = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < leng; i++) {
            if (logInputs[i] > max) {
                maxIdx = i;
                max = logInputs[i];
            }
        }
        if (max == Double.NEGATIVE_INFINITY)
            return max;
        boolean haveTerms = false;
        double intermediate = 0.0;
        double cutoff = max - LOG_TOLERANCE;
        // we avoid rearranging the array and so test indices each time!
        for (int i = 0; i < leng; i++) {
            if (i != maxIdx && logInputs[i] >= cutoff) {
                haveTerms = true;
                intermediate += exp(logInputs[i] - max);
            }
        }
        if (haveTerms) {
            return max + log(1.0 + intermediate);
        } else {
            return max;
        }
    }           

}
