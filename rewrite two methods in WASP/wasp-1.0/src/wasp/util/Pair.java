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
 * Pairs of objects.
 * 
 * @author ywwong
 *
 */
public class Pair {

	public Object first;
	public Object second;
	
	public Pair(Object first, Object second) {
		this.first = first;
		this.second = second;
	}
	
	public boolean equals(Object o) {
		if (o instanceof Pair) {
			Pair p = (Pair) o;
			if ((first==null) ? p.first != null : !first.equals(p.first))
				return false;
			if ((second==null) ? p.second != null : !second.equals(p.second))
				return false;
			return true;
		}
		return false;
	}
	
	public int hashCode() {
		int hash = 1;
		hash = 31*hash + ((first==null) ? 0 : first.hashCode());
		hash = 31*hash + ((second==null) ? 0 : second.hashCode());
		return hash;
	}
	
}
