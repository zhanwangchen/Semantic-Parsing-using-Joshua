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
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * An implementation of heaps. Values are all implicit in the comparator passed
 * in on construction.
 * 
 * @author Dan Klein
 * @author Christopher Manning
 * @author ywwong
 * 
 */
public class Heap {

	private class Itr implements Iterator {
		private int next;
		public Itr() {
			next = 0;
		}
		public boolean hasNext() {
			return next < nobjs;
		}
		public Object next() {
			return objs[next++];
		}
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	private Object[] objs;
	private int nobjs;
	private Comparator cmp;
	private int inc;

	public Heap(Comparator cmp, int inc) {
		objs = new Object[inc];
		nobjs = 0;
		this.cmp = cmp;
		this.inc = inc;
	}

	public Heap(int inc) {
		this(null, inc);
	}

	public Heap() {
		this(null, 8);
	}

	private Heap(Object[] objs, int nobjs, Comparator cmp, int inc) {
		this.objs = objs;
		this.nobjs = nobjs;
		this.cmp = cmp;
		this.inc = inc;
	}

	public Object clone() {
		return new Heap((Object[]) objs.clone(), nobjs, cmp, inc);
	}

	// Primitive Heap Operations

	private int parent(int index) {
		return (index - 1) / 2;
	}

	private int leftChild(int index) {
		return index * 2 + 1;
	}

	private int rightChild(int index) {
		return index * 2 + 2;
	}

	private int compare(Object o1, Object o2) {
		if (cmp == null)
			return ((Comparable) o1).compareTo(o2);
		else
			return cmp.compare(o1, o2);
	}

	/**
	 * On the assumption that leftChild(entry) and rightChild(entry) satisfy the
	 * heap property, make sure that the heap at entry satisfies this property
	 * by possibly percolating the element o downwards. I've replaced the
	 * obvious recursive formulation with an iterative one to gain (marginal)
	 * speed.
	 */
	private void heapify(int index) {
		while (true) {
			int min = index;

			int left = leftChild(index);
			if (left < nobjs && compare(objs[min], objs[left]) > 0)
				min = left;

			int right = rightChild(index);
			if (right < nobjs && compare(objs[min], objs[right]) > 0)
				min = right;

			if (min == index)
				break;
			else {
				// Swap min and index
				Object o = objs[min];
				objs[min] = objs[index];
				objs[index] = o;
				index = min;
			}
		}
	}

	/**
	 * Finds the object with the minimum key, removes it from the heap, and
	 * returns it.
	 * 
	 * @return the object with minimum key
	 */
	public Object extractMin() {
		if (nobjs == 0)
			throw new NoSuchElementException();

		Object minObj = objs[0];
		objs[0] = null;
		--nobjs;
		if (nobjs > 0) {
			objs[0] = objs[nobjs];
			objs[nobjs] = null;
			heapify(0);
		}
		return minObj;
	}

	/**
	 * Finds the object with the minimum key and returns it, without modifying
	 * the heap.
	 * 
	 * @return the object with minimum key
	 */
	public Object min() {
		if (nobjs == 0)
			throw new NoSuchElementException();
		return objs[0];
	}

	/**
	 * Adds an object to the heap.
	 * 
	 * @param o
	 *            an <code>Object</code> value
	 */
	public boolean add(Object o) {
		if (nobjs == objs.length) {
			Object[] copy = new Object[nobjs + inc];
			for (int i = 0; i < nobjs; ++i)
				copy[i] = objs[i];
			objs = copy;
		}
		int index = nobjs++;
		int parent = parent(index);
		while (index > 0 && compare(o, objs[parent]) < 0) {
			objs[index] = objs[parent];
			index = parent;
			parent = parent(index);
		}
		objs[index] = o;
		return true;
	}

	/**
	 * Checks if the heap is empty.
	 */
	public boolean isEmpty() {
		return nobjs == 0;
	}

	/**
	 * Get the number of elements in the heap.
	 */
	public int size() {
		return nobjs;
	}

	public void clear() {
		for (int i = 0; i < nobjs; ++i)
			objs[i] = null;
		nobjs = 0;
	}

	public Iterator iterator() {
		return new Itr();
	}

	/**
	 * Returns an array containing all of the elements in this heap in the
	 * sorted order; the runtime type of the returned array is that of the
	 * specified array. If the heap fits in the specified array, it is returned
	 * therein. Otherwise, a new array is allocated with the runtime type of the
	 * specified array and the size of this heap.
	 * 
	 * If the heap fits in the specified array with room to spare (i.e., the
	 * array has more elements than the heap), the element in the array
	 * immediately following the end of the collection is set to
	 * <code>null</code>.
	 * 
	 * This operation is <b>destructive</b>; elements are removed from this
	 * heap before they are stored in the returned array.
	 * 
	 * @param a
	 *            the array into which the elements of the heap are to be
	 *            stored, if it is big enough; otherwise, a new array of the
	 *            same runtime type is allocated for this purpose.
	 * @return an array containing the elements of the heap.
	 * @throws ArrayStoreException
	 *             if the runtime type of a is not a supertype of the runtime
	 *             type of every element in this heap.
	 */
	public Object[] toArray(Object[] a) {
		if (a.length < nobjs)
			a = (Object[]) Array.newInstance(a.getClass().getComponentType(),
					nobjs);
		else if (a.length > nobjs)
			a[nobjs] = null;
		for (int i = 0; !isEmpty();)
			a[i++] = extractMin();
		return a;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append('[');
		for (int i = 0; i < nobjs; ++i) {
			if (i > 0)
				sb.append(", ");
			sb.append(objs[i]);
		}
		sb.append(']');
		return sb.toString();
	}

}
