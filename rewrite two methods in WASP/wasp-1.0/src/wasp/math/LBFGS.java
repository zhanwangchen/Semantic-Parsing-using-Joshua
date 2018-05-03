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

import java.util.logging.Logger;

import wasp.util.Double;

/**
 * Limited-memory BFGS (Nocedal, 1980).
 * 
 * @author ywwong
 *
 */
public class LBFGS {

	private static Logger logger = Logger.getLogger(LBFGS.class.getName());
	
	/**
	 * Objective functions to minimize using the LBFGS algorithm.
	 * 
	 * @author ywwong
	 *
	 */
	public static interface Objective extends Differentiable {
		/**
		 * Returns the current decision vector.
		 * 
		 * @param X an <i>output</i> vector that would be the current decision vector.
		 */
		public void getX(double[] X);
		/**
		 * This method is called after each iteration of the LBFGS algorithm.
		 * 
		 * @param lbfgs the <code>LBFGS</code> object that uses this objective function.
		 * @param iter the iteration number (which starts from <code>1</code>).
		 * @param isLastIter indicates if this is the end of the last iteration.
		 */
		public void check(LBFGS lbfgs, int iter, boolean isLastIter);
	}
	
	private static final int MAX_ITERATIONS = 1000;
	private static final int NUM_CORRECTIONS = 4;
    private static final double ABS_RESIDUAL = 1e-1;
    private static final double REL_RESIDUAL = 1e-3;
	
	private double[] lastX;
	private double[] lastGrad;
    private double[][] S;
    private double[][] Y;
    private double[] Rho;
    private double[] alpha;
    
    public LBFGS() {
    	lastX = null;
    	lastGrad = null;
    	S = null;
    	Y = null;
    	Rho = null;
    	alpha = null;
    }

    /**
     * Finds a decision vector that locally minimizes the value of the specified objective function.
     * 
     * @param obj the objective function to minimize.
     * @param X the initial decision vector; it is also the <i>output</i> decision vector.
     * @return <code>true</code> if the optimization algorithm converges; <code>false</code> otherwise.
     */
    public boolean minimize(Objective obj, double[] X) {
    	Double val = new Double();
    	double[] grad = new double[X.length];
    	obj.getValueAndGradient(X, val, grad);
    	double g0 = Vectors.twoNorm(grad);
    	if (g0 == 0)
    		return true;
    	reset();
    	for (int iter = 0; iter < MAX_ITERATIONS; ++iter) {
    		logger.info("LBFGS iteration "+iter);
    		double[] dir = null;
    		double[] s = null;
    		double[] y = null;
    		double sy = Double.NaN;
    		if (lastX != null) {
    			s = Vectors.addCopy(X, -1, lastX);
    			y = Vectors.addCopy(grad, -1, lastGrad);
    			sy = Vectors.dotProduct(s, y);
    			// Kelley (1999)
    			if (sy <= 0) {
    				logger.warning("being cautious, resetting correction vectors");
    				sy = Double.NaN;
    				reset();
    			}
    		}
    		if (Double.isNaN(sy)) {
    			lastX = (double[]) X.clone();
    			lastGrad = (double[]) grad.clone();
    			dir = (double[]) grad.clone();
    			Vectors.multiply(dir, -1);
    		} else {
    			double rho = 1/sy;
    			// Byrd, Nocedal & Schnabel (1992)
    			double gamma = sy/Vectors.dotProduct(y, y);
    			push(S, s);
    			push(Y, y);
    			push(Rho, rho);
    			lastX = (double[]) X.clone();
    			lastGrad = (double[]) grad.clone();
    			dir = (double[]) grad.clone();
    			int size = size(S);
    			for (int i = size-1; i >= 0; --i) {
    				alpha[i] = get(Rho,i,size) * Vectors.dotProduct(get(S,i,size), dir);
    				Vectors.add(dir, -alpha[i], get(Y,i,size));
    			}
    			Vectors.multiply(dir, gamma);
    			for (int i = 0; i < size; ++i) {
    				double beta = get(Rho,i,size) * Vectors.dotProduct(get(Y,i,size), dir);
    				Vectors.add(dir, alpha[i]-beta, get(S,i,size));
    			}
    			Vectors.multiply(dir, -1);
    		}
    		boolean lineMinimized = new LineSearch().decrease(obj, X, dir);
    		obj.getValueAndGradient(X, val, grad);
    		// Kelley (1999)
    		boolean converged = Vectors.twoNorm(grad) < REL_RESIDUAL*g0 + ABS_RESIDUAL;
    		obj.check(this, iter, !lineMinimized || converged);
    		if (lastX == null) {
    			obj.getX(X);
    			obj.getValueAndGradient(X, val, grad);
    			continue;
    		}
    		if (!lineMinimized)
    			return false;
    		if (converged)
    			return true;
    	}
    	logger.warning("LBFGS fails to converge");
    	return false;
    }
    
    /**
     * Resets the correction vectors (mainly <code>S</code> and <code>Y</code>) stored in this 
     * <code>LBFGS</code> object.  This is necessary when the objective function is changed (e.g. during
     * Viterbi approximation).
     */
    public void reset() {
    	lastX = null;
    	lastGrad = null;
    	S = new double[NUM_CORRECTIONS][];
    	Y = new double[NUM_CORRECTIONS][];
    	Rho = new double[NUM_CORRECTIONS];
    	alpha = new double[NUM_CORRECTIONS];
    }
        
    private void push(double[][] queue, double[] X) {
    	for (int i = 0; i < NUM_CORRECTIONS-1; ++i)
    		queue[i] = queue[i+1];
    	queue[NUM_CORRECTIONS-1] = X;
    }
    
    private void push(double[] queue, double a) {
    	for (int i = 0; i < NUM_CORRECTIONS-1; ++i)
    		queue[i] = queue[i+1];
    	queue[NUM_CORRECTIONS-1] = a;
    }
    
    private int size(double[][] queue) {
    	for (int i = 0; i < NUM_CORRECTIONS; ++i)
    		if (queue[i] != null)
    			return NUM_CORRECTIONS-i;
    	return 0;
    }
    
    private double[] get(double[][] queue, int i, int size) {
    	return queue[NUM_CORRECTIONS-size+i];
    }
    
    private double get(double[] queue, int i, int size) {
    	return queue[NUM_CORRECTIONS-size+i];
    }

}
