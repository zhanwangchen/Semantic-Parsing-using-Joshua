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

import wasp.util.Double;

/**
 * Functions that are differentiable on R^n.
 * 
 * @author ywwong
 *
 */
public interface Differentiable {

	/**
	 * Returns the value and gradient of this function at <code>X</code>.
	 * 
	 * @param X a real-valued vector of length <code>n</code>.
	 * @param val an <i>output</i> variable for the value of this function at <code>X</code>.
	 * @param grad an <i>output</i> vector of length <code>n</code> that would be the gradient of this
	 * function at <code>X</code>.
	 */
	public void getValueAndGradient(double[] X, Double val, double[] grad);
	
}
