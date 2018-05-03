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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * An iterator over a collection of objects sorted in their natural ordering.
 *  
 * @author ywwong
 *
 */
public class SortIterator implements Iterator {

	private Comparable[] objs;
	private Comparable threshold;
	private int next;
	
	/**
	 * Creates an iterator over a collection of objects sorted in their natural ordering.  The objects
	 * must be <code>Comparable</code> so that their natural ordering is well-defined.  Otherwise, an
	 * <code>ArrayStoreException</code> is thrown.
	 * 
	 * @param it an iterator over a collection of <code>Comparable</code> objects.
	 * @param k if <code>k > 0</code>, then the iterator will be over the top <code>k</code> of the 
	 * collection of objects according to the natural ordering; the actual number can be more if there
	 * are ties.
	 * @throws ArrayStoreException if the specified objects are not all <code>Comparable</code>.
	 */
	public SortIterator(Iterator it, int k) {
		ArrayList list = new ArrayList();
		while (it.hasNext())
			list.add(it.next());
		objs = (Comparable[]) list.toArray(new Comparable[0]);
		Arrays.sort(objs);
		threshold = (k == 0 || k > objs.length) ? null : objs[k-1];
		next = 0;
		verifyNext();
	}
	
	/**
	 * Creates an iterator over a collection of objects sorted in their natural ordering.  The objects
	 * must be <code>Comparable</code> so that their natural ordering is well-defined.  Otherwise, an
	 * <code>ArrayStoreException</code> is thrown.
	 * 
	 * @param it an iterator over a collection of <code>Comparable</code> objects.
	 * @throws ArrayStoreException if the specified objects are not all <code>Comparable</code>.
	 */
	public SortIterator(Iterator it) {
		this(it, 0);
	}
	
	private void verifyNext() {
		if (next == objs.length || (threshold != null && objs[next].compareTo(threshold) > 0))
			next = -1;
	}
	
	public boolean hasNext() {
		return next >= 0;
	}
	
	public Object next() {
		if (next < 0)
			throw new NoSuchElementException();
		Object obj = objs[next++];
		verifyNext();
		return obj;
	}
	
	public void remove() {
		throw new UnsupportedOperationException();
	}

}
