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
 * A simplified version of <code>java.lang.Boolean</code> where the stored value is mutable.
 * 
 * @author ywwong
 * 
 */
public class Bool {

	public static final Bool TRUE = new Bool(true);
	public static final Bool FALSE = new Bool(false);

	public boolean val;

	public Bool(boolean val) {
		this.val = val;
	}

	public boolean equals(Object o) {
		return o instanceof Bool && val == ((Bool) o).val;
	}

	public int hashCode() {
		return (val) ? 1231 : 1237;
	}

	public static boolean parseBool(String s) {
		return s != null && s.equalsIgnoreCase("true");
	}

	public static String toString(boolean val) {
		return Boolean.toString(val);
	}
	
	public String toString() {
		return Boolean.toString(val);
	}

}
