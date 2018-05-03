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

import java.util.ArrayList;

import wasp.data.Node;
import wasp.data.Symbol;
import wasp.data.Terminal;
import wasp.util.Arrays;
import wasp.util.Combine;
import wasp.util.Mask;
import wasp.util.Matrices;

/**
 * Word alignments that allow many-to-many links.
 * 
 * @author ywwong
 *
 */
public class NToNWordAlign extends WordAlign {

	private boolean[][] a;
	private Link[] headsE;
	private short[] fertsE;
	private Link[] headsF;
	private short[] fertsF;

	public NToNWordAlign(Symbol[] E, Node[] F, double score) {
		super(E, F, score);
		a = new boolean[E.length][F.length];
		headsE = new Link[E.length];
		fertsE = new short[E.length];
		headsF = new Link[F.length];
		fertsF = new short[F.length];
	}
	
	public NToNWordAlign(Symbol[] E, Node[] F) {
		this(E, F, 0);
	}
	
	public boolean equals(Object o) {
		if (o instanceof NToNWordAlign) {
			NToNWordAlign align = (NToNWordAlign) o;
			return E == align.E && F == align.F && Matrices.equal(a, align.a);
		}
		return false;
	}
	
	public int hashCode() {
		return Matrices.hashCode(a);
	}
	
	public boolean isLinked(short e, short f) {
		return a[e][f];
	}
	
	public Link getFirstLinkFromE(short e) {
		return headsE[e];
	}
	
	public short countLinksFromE(short e) {
		return fertsE[e];
	}
	
	public Link getFirstLinkFromF(short f) {
		return headsF[f];
	}
	
	public short countLinksFromF(short f) {
		return fertsF[f];
	}
	
	protected void addNewLink(Link link) {
		if (a[link.e][link.f])
			return;
		a[link.e][link.f] = true;
		++fertsE[link.e];
		++fertsF[link.f];
		if (headsE[link.e] == null) {
			link.back = null;
			link.next = null;
			headsE[link.e] = link;
		} else if (headsE[link.e].f > link.f) {
			link.back = null;
			link.next = headsE[link.e];
			headsE[link.e].back = link;
			headsE[link.e] = link;
		} else {
			Link back = headsE[link.e];
			while (back.next != null && back.next.f < link.f)
				back = back.next;
			link.back = back;
			link.next = back.next;
			back.next = link;
			if (link.next != null)
				link.next.back = link;
		}
		link = (Link) link.clone();
		if (headsF[link.f] == null) {
			link.back = null;
			link.next = null;
			headsF[link.f] = link;
		} else if (headsF[link.f].e > link.e) {
			link.back = null;
			link.next = headsF[link.f];
			headsF[link.f].back = link;
			headsF[link.f] = link;
		} else {
			Link back = headsF[link.f];
			while (back.next != null && back.next.e < link.e)
				back = back.next;
			link.back = back;
			link.next = back.next;
			back.next = link;
			if (link.next != null)
				link.next.back = link;
		}
	}
	
	public void removeLink(Link link) {
		if (!a[link.e][link.f])
			return;
		a[link.e][link.f] = false;
		--fertsE[link.e];
		--fertsF[link.f];
		if (headsE[link.e].f == link.f) {
			headsE[link.e] = headsE[link.e].next;
			if (headsE[link.e] != null)
				headsE[link.e].back = null;
		} else {
			Link back = headsE[link.e];
			while (back.next.f != link.f)
				back = back.next;
			Link next = back.next.next;
			if (next != null)
				next.back = back;
			back.next = next;
		}
		if (headsF[link.f].e == link.e) {
			headsF[link.f] = headsF[link.f].next;
			if (headsF[link.f] != null)
				headsF[link.f].back = null;
		} else {
			Link back = headsF[link.f];
			while (back.next.e != link.e)
				back = back.next;
			Link next = back.next.next;
			if (next != null)
				next.back = back;
			back.next = next;
		}
	}
	
	public void removeLinksFromE(short e) {
		Link link = headsE[e];
		while (link != null) {
			removeLink(link);
			link = link.next;
		}
	}
	
	public void removeLinksFromF(short f) {
		Link link = headsF[f];
		while (link != null) {
			removeLink(link);
			link = link.next;
		}
	}
	
	/**
	 * Restores the MR parse nodes in a word alignment that have been removed.
	 * 
	 * @param F the linearized MR parse with all removed nodes restored.
	 * @param Fmask the mask that has been applied to the linearized MR parse.
	 * @return a new word alignment with all removed MR parse nodes restored.
	 */
	public NToNWordAlign unmaskF(Node[] F, Mask Fmask) {
		NToNWordAlign align = new NToNWordAlign(E, F, score);
		for (short i = 0; i < headsE.length; ++i) {
			Link link = headsE[i];
			while (link != null) {
				align.addLink(link.e, Fmask.toLong(link.f), link.strength);
				link = link.next;
			}
		}
		return align;
	}
	
	/**
	 * Combines the specified words in the NL sentence.  All links to the original phrase are removed.
	 * This method returns a new word alignment in which the specified words are combined.  A new
	 * <code>Combine</code> object is appended to the given list so that the combination of words can
	 * be undone later.
	 * 
	 * @param from the index of the first word to combine.
	 * @param n the number of words to combine.
	 * @param Ecomb an <i>output</i> list of <code>Combine</code> objects for keeping track of the words
	 * that have been combined.
	 * @return a new word alignment in which the specified words are combined.
	 */
	public NToNWordAlign combineE(short from, short n, ArrayList Ecomb) {
		Terminal[] E = (Terminal[]) Arrays.replace(this.E, from, from+n, combineTerms(from, n));
		NToNWordAlign align = new NToNWordAlign(E, F, score);
		Combine comb = new Combine(from, n);
		Ecomb.add(comb);
		for (short i = 0; i < headsE.length; ++i) {
			Link link = headsE[i];
			while (link != null) {
				if (i < from || i >= from+n)
					align.addLink(comb.toShort(i), link.f, link.strength);
				link = link.next;
			}
		}
		return align;
	}
	
	private Terminal combineTerms(short from, short n) {
		StringBuffer sb = new StringBuffer();
		for (short i = 0; i < n; ++i) {
			sb.append('_');
			sb.append(E[i+from]);
		}
		return new Terminal(sb.toString(), true);
	}
	
}
