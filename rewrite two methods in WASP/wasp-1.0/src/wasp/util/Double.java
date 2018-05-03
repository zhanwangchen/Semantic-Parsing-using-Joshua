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
package wasp.util;

/**
 * A simplified version of <code>java.lang.Double</code> where the stored value is mutable.
 * 
 * @author ywwong
 * 
 */
public class Double {

	public static final double POSITIVE_INFINITY = java.lang.Double.POSITIVE_INFINITY;
	public static final double NEGATIVE_INFINITY = java.lang.Double.NEGATIVE_INFINITY;
	public static final double NaN = java.lang.Double.NaN;
	
	public double val;

	public Double(double val) {
		this.val = val;
	}

	public Double() {
		this.val = 0;
	}

	public boolean equals(Object o) {
		return o instanceof Double && val == ((Double) o).val;
	}

	public int hashCode() {
		long v = java.lang.Double.doubleToLongBits(val);
		return (int) (v ^ (v >>> 32));
	}

	public int compareTo(Object o) {
		if (val < ((Double) o).val)
			return -1;
		else if (val > ((Double) o).val)
			return 1;
		else
			return 0;
	}

	public static boolean isInfinite(double val) {
		return java.lang.Double.isInfinite(val);
	}
	
	public static boolean isNaN(double val) {
		return java.lang.Double.isNaN(val);
	}
	
	public static double parseDouble(String s) {
		return java.lang.Double.parseDouble(s);
	}
	
	public static String toString(double val) {
		return java.lang.Double.toString(val);
	}
	
	public String toString() {
		return java.lang.Double.toString(val);
	}

}
