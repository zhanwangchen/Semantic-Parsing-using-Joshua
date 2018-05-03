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
package wasp.align;

/**
 * Word-to-word links in a word alignment.
 *  
 * @author ywwong
 *
 */
public class Link {

	private WordAlign align;
	/** Index of a word in an NL sentence. */
	public short e;
	/** Index of a node in an MR parse. */
	public short f;
	/** Strength of this link; basically word translation probability. */
	public double strength;
	/** Last link in the same cept. */
	public Link back;
	/** Next link in the same cept. */
	public Link next;
	
	public Link(WordAlign align, short e, short f, double strength) {
		this.align = align;
		this.e = e;
		this.f = f;
		this.strength = strength;
		back = null;
		next = null;
	}
	
	public boolean equals(Object o) {
		if (o instanceof Link) {
			Link l = (Link) o;
			return align == l.align && e == l.e && f == l.f;
		}
		return false;
	}
	
	public int hashCode() {
		return 31*e+f;
	}
	
	public Object clone() {
		return new Link(align, e, f, strength);
	}
	
}
