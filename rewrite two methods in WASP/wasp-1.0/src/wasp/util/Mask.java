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

import java.lang.reflect.Array;

/**
 * Masks for creating subsequences of arrays.
 * 
 * @author ywwong
 *
 */
public class Mask {

	private boolean[] mask;
	private short[] toShort;
	private short[] toLong;
	
	public Mask(short len) {
		mask = new boolean[len];
		toShort = null;
		toLong = null;
	}

	/**
	 * Sets the value of this mask to true at the specified position.
	 *  
	 * @param i a position index.
	 */
	public void set(short i) {
		mask[i] = true;
		toShort = null;
		toLong = null;
	}
	
	/**
	 * Extracts a subsequence of the specified array based on this mask.
	 *  
	 * @param array the array from which a subsequence is extracted.
	 * @return a subsequence of the <code>array</code> argument based on this mask.
	 * @throws RuntimeException if the array has a different length from this mask.
	 */
	public Object[] apply(Object[] array) {
		if (array.length != mask.length)
			throw new RuntimeException();
		if (toShort == null)
			init();
		Class type = array.getClass().getComponentType();
		Object[] a = (Object[]) Array.newInstance(type, toLong.length);
		for (short i = 0, j = 0; i < mask.length; ++i)
			if (mask[i])
				a[j++] = array[i];
		return a;
	}
	
	private void init() {
		short l = 0;
		for (short i = 0; i < mask.length; ++i)
			if (mask[i])
				++l;
		toShort = new short[mask.length];
		toLong = new short[l];
		for (short i = 0, j = 0; i < mask.length; ++i)
			if (mask[i]) {
				toShort[i] = j;
				toLong[j] = i;
				++j;
			} else
				toShort[i] = -1;
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
		if (toShort == null)
			init();
		return toShort[i];
	}
	
	/**
	 * Returns the position index of the complete array that corresponds to the specified position index
	 * of the subsequence.
	 * 
	 * @param i a position index of the subsequence.
	 * @return the position index of the complete array that corresponds to the <code>i</code>-th
	 * element of the subsequence.
	 */
	public short toLong(short i) {
		if (toLong == null)
			init();
		return toLong[i];
	}
	
}
