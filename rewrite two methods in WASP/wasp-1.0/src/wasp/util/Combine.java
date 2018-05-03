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
 * Code for keeping track of array elements that have been combined.
 * 
 * @author ywwong
 *
 */
public class Combine {

	private short from;
	private short n;
	
	/**
	 * Combines the specified elements.
	 *
	 * @param from the index of the first element to combine.
	 * @param n the number of elements to combine.
	 */
	public Combine(short from, short n) {
		this.from = from;
		this.n = n;
	}

	/**
	 * Returns the position index of the subsequence that corresponds to the specified position index of
	 * the complete array.
	 *  
	 * @param i a position index of the complete array.
	 * @return the position index of the subsequence that corresponds to the <code>i</code>-th element
	 * of the complete array.
	 */
	public short toShort(short i) {
		if (i < from)
			return i;
		else if (i < from+n)
			return from;
		else
			return (short) (i-n+1);
	}
	
	/**
	 * Returns the position indices of the complete array that corresponds to the specified position index
	 * of the subsequence.
	 * 
	 * @param i a position index of the subsequence.
	 * @return the position indices of the complete array that corresponds to the <code>i</code>-th
	 * element of the subsequence.
	 */
	public short[] toLong(short i) {
		short[] a;
		if (i < from) {
			a = new short[1];
			a[0] = i;
		} else if (i == from) {
			a = new short[n];
			for (short j = 0; j < n; ++j)
				a[j] = (short) (j+from);
		} else {
			a = new short[1];
			a[0] = (short) (i+n-1);
		}
		return a;
	}
	
	public short[] toLong(short[] array) {
		if (array.length == 1)
			return toLong(array[0]);
		else {
			// assuming combined elements do not combine again
			short[] a = new short[array.length];
			for (short i = 0; i < array.length; ++i)
				a[i] = toLong(array[i])[0];
			return a;
		}
	}
	
}
