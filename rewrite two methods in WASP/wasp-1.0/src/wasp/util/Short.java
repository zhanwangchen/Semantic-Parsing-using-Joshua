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
 * A simplified version of <code>java.lang.Short</code> where the stored value is mutable.
 * 
 * @author ywwong
 * 
 */
public class Short implements Comparable {

	public static final short MAX_VALUE = 32767;
	public static final short MIN_VALUE = -32768;

	public short val;

	public Short(short val) {
		this.val = val;
	}

	public Short(int val) {
		this.val = (short) val;
	}

	public Short() {
		this.val = 0;
	}

	public boolean equals(Object o) {
		return o instanceof Short && val == ((Short) o).val;
	}

	public int hashCode() {
		return val;
	}

	public int compareTo(Object o) {
		if (val < ((Short) o).val)
			return -1;
		else if (val > ((Short) o).val)
			return 1;
		else
			return 0;
	}

	public static short parseShort(String s) {
		return java.lang.Short.parseShort(s);
	}

	public static String toString(short val) {
		return java.lang.Short.toString(val);
	}
	
	public String toString() {
		return java.lang.Short.toString(val);
	}

}
