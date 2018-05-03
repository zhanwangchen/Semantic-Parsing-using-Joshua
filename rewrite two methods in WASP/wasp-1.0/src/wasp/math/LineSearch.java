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
 * Backtracking line search (Armijo, 1966; Kelley, 1999).
 * 
 * @author ywwong
 *
 */
public class LineSearch {

	private static Logger logger = Logger.getLogger(LineSearch.class.getName());
	
    private static final int MAX_ITERATIONS = 100;
    private static final double ALPHA = 1e-4;
    private static final double BETA_LOW = 0.1;
    private static final double BETA_HIGH = 0.5;
    private static final double TOLERANCE = 1e-7;

    /**
     * Finds a decision vector along the specified direction such that the value of the specified
     * objective function sufficiently decreases.
     * 
     * @param obj the objective function to consider.
     * @param X the initial decision vector; it is also the <i>output</i> decision vector.
     * @param dir the direction along which line search is done.
     * @return <code>true</code> if a decision vector is found such that the objective sufficiently
     * decreases; <code>false</code> otherwise.
     */
    public boolean decrease(Differentiable obj, double[] X, double[] dir) {
    	Double val = new Double();
    	double[] grad = new double[X.length];
    	obj.getValueAndGradient(X, val, grad);
    	double Gd0 = Vectors.dotProduct(grad, dir);
    	if (Gd0 >= 0) {
    		logger.severe("roundoff problem, Gd0 >= 0");
    		throw new RuntimeException();
    	}
    	// Press et al. (1992)
    	double minLambda = 0;
    	for (int i = 0; i < dir.length; ++i) {
    		double t = Math.abs(dir[i])/Math.max(Math.abs(X[i]), 1);
    		if (minLambda < t)
    			minLambda = t;
    	}
    	minLambda = TOLERANCE/minLambda;
    	double[] X0 = (double[]) X.clone();
    	double val0 = val.val;
    	double lambda = 1;
    	double lastVal = val.val;
    	double lastLambda = 1;
    	for (int iter = 0; iter < MAX_ITERATIONS; ++iter) {
    		if (lambda < minLambda) {
    	    	Vectors.assign(X, X0);
    	    	logger.warning("step size is too small");
    			return false;
    		}
    		Vectors.assign(X, Vectors.addCopy(X0, lambda, dir));
    		obj.getValueAndGradient(X, val, grad);
    		// sufficient decrease condition
    		if (val.val-val0 < ALPHA*lambda*Gd0)
    			return true;
    		double nextLambda;
    		if (lambda == 1)
    			nextLambda = -Gd0/(2*(val.val-val0-Gd0));
    		else {  // lambda < 1
    			double r1 = val.val-val0-Gd0*lambda;
    			double r2 = lastVal-val0-Gd0*lastLambda;
    			double a = (r1/(lambda*lambda)-r2/(lastLambda*lastLambda))/(lambda-lastLambda);
    			double b = (-lastLambda*r1/(lambda*lambda)+lambda*r2/(lastLambda*lastLambda))
    			/(lambda-lastLambda);
    			if (a == 0)
    				nextLambda = -Gd0/(2*b);
    			else {
    				double d = b*b-3*a*Gd0;
    				if (d < 0)
    					nextLambda = 0.5*lambda;
    				else if (b <= 0)
    					nextLambda = (-b+Math.sqrt(d))/(3*a);
    				else
    					nextLambda = -Gd0/(b+Math.sqrt(d));
    			}
    		}
    		lastVal = val.val;
    		lastLambda = lambda;
    		if (nextLambda < BETA_LOW*lambda)
    			lambda *= BETA_LOW;
    		else if (nextLambda > BETA_HIGH*lambda)
    			lambda *= BETA_HIGH;
    		else
    			lambda = nextLambda;
    	}
    	Vectors.assign(X, X0);
    	logger.warning("line search fails to converge");
    	return false;
    }

}
