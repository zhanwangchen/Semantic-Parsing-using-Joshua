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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.StringTokenizer;

/**
 * Common array operations.
 * 
 * @author ywwong
 *
 */
public class Arrays {

	private Arrays() {}

	public static void addAll(Collection c, Object[] array) {
		for (int i = 0; i < array.length; ++i)
			c.add(array[i]);
	}
	
	public static int argmax(short[] array) {
		int argmax = -1;
		int max = Int.MIN_VALUE;
		for (int i = 0; i < array.length; ++i)
			if (max < array[i]) {
				argmax = i;
				max = array[i];
			}
		return argmax;
	}
	
	public static double[] concat(double[] array1, double[] array2) {
		double[] a = new double[array1.length+array2.length];
		for (int i = 0; i < array1.length; ++i)
			a[i] = array1[i];
		for (int i = 0; i < array2.length; ++i)
			a[i+array1.length] = array2[i];
		return a;
	}
	
	public static boolean contains(Object[] array, Class type) {
		for (int i = 0; i < array.length; ++i)
			if (array[i] != null && type.isInstance(array[i]))
				return true;
		return false;
	}
	
	/**
	 * Creates and returns a <i>deep</i> copy of the specified array, where copies of the array elements
	 * are made using the <code>Copyable.copy()</code> method.
	 * 
	 * @param array an array of <code>Copyable</code> objects.
	 * @return a deep copy of the <code>array</code> argument.
	 */
	public static Object[] copy(Copyable[] array) {
		Class type = array.getClass().getComponentType();
		Object[] a = (Object[]) Array.newInstance(type, array.length);
		for (int i = 0; i < array.length; ++i)
			a[i] = array[i].copy();
		return a;
	}
	
	public static int count(boolean[] array) {
		int sum = 0;
		for (int i = 0; i < array.length; ++i)
			if (array[i])
				++sum;
		return sum;
	}
	
	public static boolean equal(boolean[] array1, boolean[] array2) {
		if (array1.length == array2.length) {
			for (int i = 0; i < array1.length; ++i)
				if (array1[i] != array2[i])
					return false;
			return true;
		}
		return false;
	}
	
	public static boolean equal(byte[] array1, byte[] array2) {
		if (array1.length == array2.length) {
			for (int i = 0; i < array1.length; ++i)
				if (array1[i] != array2[i])
					return false;
			return true;
		}
		return false;
	}
	
	public static boolean equal(double[] array1, double[] array2) {
		if (array1.length == array2.length) {
			for (int i = 0; i < array1.length; ++i)
				if (array1[i] != array2[i])
					return false;
			return true;
		}
		return false;
	}
	
	public static boolean equal(short[] array1, short[] array2) {
		if (array1.length == array2.length) {
			for (int i = 0; i < array1.length; ++i)
				if (array1[i] != array2[i])
					return false;
			return true;
		}
		return false;
	}
	
	public static boolean equal(Object[] array1, Object[] array2) {
		if (array1.length == array2.length) {
			for (int i = 0; i < array1.length; ++i)
				if (array1[i] == null) {
					if (array2[i] != null)
						return false;
				} else {
					if (!array1[i].equals(array2[i]))
						return false;
				}
			return true;
		}
		return false;
	}
	
	public static void fill(int[] array, int val) {
		for (int i = 0; i < array.length; ++i)
			array[i] = val;
	}
	
	public static void fill(double[] array, double val) {
		for (int i = 0; i < array.length; ++i)
			array[i] = val;
	}
	
	public static int hashCode(boolean[] array) {
		int hash = 1;
		for (int i = 0; i < array.length; ++i)
			hash = 31*hash + ((array[i]) ? 1231 : 1237);
		return hash;
	}
	
	public static int hashCode(short[] array) {
		int hash = 1;
		for (int i = 0; i < array.length; ++i)
			hash = 31*hash + array[i];
		return hash;
	}
	
	public static int hashCode(Object[] array) {
		int hash = 1;
		for (int i = 0; i < array.length; ++i)
			hash = 31*hash + ((array[i]==null) ? 0 : array[i].hashCode());
		return hash;
	}
	
	public static int indexOf(Object[] array, int length, Object obj) {
		for (int i = 0; i < length; ++i)
			if (obj == null) {
				if (array[i] == null)
					return i;
			} else {
				if (array[i] != null && array[i].equals(obj))
					return i;
			}
		return -1;
	}
	
	public static int indexOf(Object[] array, Object obj) {
		return indexOf(array, array.length, obj);
	}
	
	public static Object[] insert(Object[] array, int index, Object obj) {
		Class type = array.getClass().getComponentType();
		Object[] a = (Object[]) Array.newInstance(type, array.length+1);
		for (int i = 0; i < index; ++i)
			a[i] = array[i];
		a[index] = obj;
		for (int i = index; i < array.length; ++i)
			a[i+1] = array[i];
		return a;
	}
	
	public static Object[] insert(Object[] array, int index, Object[] objs) {
		Class type = array.getClass().getComponentType();
		Object[] a = (Object[]) Array.newInstance(type, array.length+objs.length);
		for (int i = 0; i < index; ++i)
			a[i] = array[i];
		for (int i = 0; i < objs.length; ++i)
			a[index+i] = objs[i];
		for (int i = index; i < array.length; ++i)
			a[i+objs.length] = array[i];
		return a;
	}
	
	public static short[] remove(short[] array, int index) {
		short[] a = new short[array.length-1];
		for (int i = 0; i < index; ++i)
			a[i] = array[i];
		for (int i = index+1; i < array.length; ++i)
			a[i-1] = array[i];
		return a;
	}
	
	public static Object[] replace(Object[] array, int from, int to, Object replacement) {
		Class type = array.getClass().getComponentType();
		Object[] a = (Object[]) Array.newInstance(type, array.length-(to-from)+1);
		for (int i = 0; i < from; ++i)
			a[i] = array[i];
		a[from] = replacement;
		for (int i = to; i < array.length; ++i)
			a[i-(to-from)+1] = array[i];
		return a;
	}
	
	public static Object[] replace(Object[] array, int index, Object[] replacement) {
		Class type = array.getClass().getComponentType();
		Object[] a = (Object[]) Array.newInstance(type, array.length+replacement.length-1);
		for (int i = 0; i < index; ++i)
			a[i] = array[i];
		for (int i = 0; i < replacement.length; ++i)
			a[i+index] = replacement[i];
		for (int i = index+1; i < array.length; ++i)
			a[i+replacement.length-1] = array[i];
		return a;
	}
	
	public static double[] resize(double[] array, int length) {
		double[] a = new double[length];
		for (int i = 0; i < array.length && i < length; ++i)
			a[i] = array[i];
		return a;
	}
	
	public static Object[] resize(Object[] array, int length) {
		Class type = array.getClass().getComponentType();
		Object[] a = (Object[]) Array.newInstance(type, length);
		for (int i = 0; i < array.length && i < length; ++i)
			a[i] = array[i];
		return a;
	}

	public static void reverse(short[] array) {
		for (int i = 0, j = array.length-1; i < array.length/2; ++i, --j) {
			short val = array[i];
			array[i] = array[j];
			array[j] = val;
		}
	}
	
	public static void sort(Object[] array) {
		java.util.Arrays.sort(array);
	}
	
	public static void sort(Object[] array, Comparator comp) {
		java.util.Arrays.sort(array, comp);
	}
	
	public static double[] subarray(double[] array, int from, int to) {
		double[] a = new double[to-from];
		for (int i = 0; i < to-from; ++i)
			a[i] = array[i+from];
		return a;
	}
	
	public static Object[] subarray(Object[] array, int from, int to) {
		Class type = array.getClass().getComponentType();
		Object[] a = (Object[]) Array.newInstance(type, to-from);
		for (int i = 0; i < to-from; ++i)
			a[i] = array[i+from];
		return a;
	}
	
	public static short sum(short[] array) {
		short sum = 0;
		for (int i = 0; i < array.length; ++i)
			sum += array[i];
		return sum;
	}
	
	public static int[] toIntArray(Collection c) {
		int size = c.size();
		int[] a = new int[size];
		Iterator it = c.iterator();
		for (int i = 0; i < size; ++i)
			a[i] = ((Int) it.next()).val;
		return a;
	}
	
	public static short[] toShortArray(Collection c) {
		int size = c.size();
		short[] a = new short[size];
		Iterator it = c.iterator();
		for (int i = 0; i < size; ++i)
			a[i] = ((Short) it.next()).val;
		return a;
	}
	
	public static String toString(short[] array) {
		StringBuffer sb = new StringBuffer();
		sb.append('[');
		for (int i = 0; i < array.length; ++i) {
			if (i > 0)
				sb.append(", ");
			sb.append(array[i]);
		}
		sb.append(']');
		return sb.toString();
	}
	
	public static String toString(Object[] array) {
		StringBuffer sb = new StringBuffer();
		sb.append('[');
		for (int i = 0; i < array.length; ++i) {
			if (i > 0)
				sb.append(", ");
			sb.append(array[i]);
		}
		sb.append(']');
		return sb.toString();
	}
	
	public static String[] tokenize(String str) {
		ArrayList list = new ArrayList();
		StringTokenizer tokenizer = new StringTokenizer(str);
		while (tokenizer.hasMoreTokens())
			list.add(tokenizer.nextToken());
		return (String[]) list.toArray(new String[0]);
	}
	
}
