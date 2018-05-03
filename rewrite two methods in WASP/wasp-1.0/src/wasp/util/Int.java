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
 * A simplified version of <code>java.lang.Integer</code> where the stored value is mutable.
 * 
 * @author ywwong
 * 
 */
public class Int implements Comparable {

	public static final int MAX_VALUE = 2147483647;
	public static final int MIN_VALUE = -2147483648;

	public int val;

	public Int(int val) {
		this.val = val;
	}

	public boolean equals(Object o) {
		return o instanceof Int && val == ((Int) o).val;
	}

	public int hashCode() {
		return val;
	}

	public int compareTo(Object o) {
		if (val < ((Int) o).val)
			return -1;
		else if (val > ((Int) o).val)
			return 1;
		else
			return 0;
	}

	public static int parseInt(String s) {
		return Integer.parseInt(s);
	}

	public static String toString(int val) {
		return Integer.toString(val);
	}
	
	public String toString() {
		return Integer.toString(val);
	}

}
