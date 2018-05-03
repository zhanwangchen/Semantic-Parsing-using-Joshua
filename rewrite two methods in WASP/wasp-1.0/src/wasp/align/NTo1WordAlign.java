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
import wasp.data.Nonterminal;
import wasp.data.Symbol;
import wasp.data.Terminal;
import wasp.mrl.Production;
import wasp.mrl.ProductionSymbol;
import wasp.util.Arrays;
import wasp.util.Combine;
import wasp.util.Mask;

/**
 * Word alignments that allow at most one link from each NL word.  Synchronous grammar rules are
 * extracted from these alignments.
 * 
 * @author ywwong
 *
 */
public class NTo1WordAlign extends WordAlign {

	private Link[] a;
	private Link[] heads;
	private short[] ferts;

	public NTo1WordAlign(Symbol[] E, Node[] F, double score) {
		super(E, F, score);
		a = new Link[E.length];
		heads = new Link[F.length];
		ferts = new short[F.length];
	}
	
	public NTo1WordAlign(Symbol[] E, Node[] F) {
		this(E, F, 0);
	}
	
	public boolean equals(Object o) {
		if (o instanceof NTo1WordAlign) {
			NTo1WordAlign align = (NTo1WordAlign) o;
			return E == align.E && F == align.F && Arrays.equal(a, align.a);
		}
		return false;
	}
	
	public int hashCode() {
		return Arrays.hashCode(a);
	}
	
	public boolean isLinked(short e, short f) {
		return a[e] != null && a[e].f == f;
	}
	
	public Link getFirstLinkFromE(short e) {
		return a[e];
	}
	
	public short countLinksFromE(short e) {
		return (short) ((a[e]==null) ? 0 : 1);
	}
	
	public Link getFirstLinkFromF(short f) {
		return heads[f];
	}
	
	public short countLinksFromF(short f) {
		return ferts[f];
	}
	
	/**
	 * Adds a new link from an NL word to an MR parse node (<code>f</code>).  If the NL word is already 
	 * linked to another MR parse node (<code>f'</code>), then no new link will be added, unless
	 * <code>f</code> is an ancestor of <code>f'</code> in the MR parse tree, in which case the new link
	 * will replace the old link.
	 */
	protected void addNewLink(Link link) {
		if (a[link.e] != null)
			return;
		a[link.e] = link;
		++ferts[link.f];
		if (heads[link.f] == null) {
			link.back = null;
			link.next = null;
			heads[link.f] = link;
		} else if (heads[link.f].e > link.e) {
			link.back = null;
			link.next = heads[link.f];
			heads[link.f].back = link;
			heads[link.f] = link;
		} else {
			Link back = heads[link.f];
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
		if (a[link.e] == null || a[link.e].f != link.f)
			return;
		a[link.e] = null;
		--ferts[link.f];
		if (heads[link.f].e == link.e) {
			heads[link.f] = heads[link.f].next;
			if (heads[link.f] != null)
				heads[link.f].back = null;
		} else {
			Link back = heads[link.f];
			while (back.next.e != link.e)
				back = back.next;
			Link next = back.next.next;
			if (next != null)
				next.back = back;
			back.next = next;
		}
	}
	
	public void removeLinksFromE(short e) {
		if (a[e] != null)
			removeLink(a[e]);
	}
	
	public void removeLinksFromF(short f) {
		Link link = heads[f];
		while (link != null) {
			removeLink(link);
			link = link.next;
		}
	}
	
	/**
	 * Restores the words in the NL sentence that have been removed.
	 * 
	 * @param E the NL sentence with all removed words restored.
	 * @param Emask the mask that has been applied to the NL sentence.
	 * @return a new word alignment with all removed words restored.
	 */
	public NTo1WordAlign unmaskE(Terminal[] E, Mask Emask) {
		NTo1WordAlign align = new NTo1WordAlign(E, F, score);
		for (short i = 0; i < a.length; ++i) {
			if (a[i] != null)
				align.addLink(Emask.toLong(a[i].e), a[i].f, a[i].strength);
		}
		return align;
	}
	
	/**
	 * Restores the MR parse nodes in a word alignment that have been removed.
	 * 
	 * @param F the linearized MR parse with all removed nodes restored.
	 * @param Fmask the mask that has been applied to the linearized MR parse.
	 * @return a new word alignment with all removed MR parse nodes restored.
	 */
	public NTo1WordAlign unmaskF(Node[] F, Mask Fmask) {
		NTo1WordAlign align = new NTo1WordAlign(E, F, score);
		for (short i = 0; i < a.length; ++i)
			if (a[i] != null)
				align.addLink(a[i].e, Fmask.toLong(a[i].f), a[i].strength);
		return align;
	}
	
	/**
	 * Separates the NL words that have been combined using the <code>NToNWordAlign.combineE()</code>
	 * method.  All links to the combined words are replicated to cover all individual words.  This method 
	 * returns a new alignment in which the combined words are separated.
	 * 
	 * @param E the NL sentence with all combined words separated.
	 * @param Ecomb a list of <code>Combine</code> objects that keep track of the words that have been
	 * combined.
	 * @return a new word alignment in which the combined words are separated.
	 */
	public NTo1WordAlign separateE(Terminal[] E, ArrayList Ecomb) {
		if (Ecomb.isEmpty())
			return this;
		NTo1WordAlign align = new NTo1WordAlign(E, F, score);
		for (short i = 0; i < a.length; ++i)
			if (a[i] != null) {
				short[] e = new short[1];
				e[0] = i;
				for (short j = (short) (Ecomb.size()-1); j >= 0; --j) {
					Combine comb = (Combine) Ecomb.get(j);
					e = comb.toLong(e);
				}
				for (short j = 0; j < e.length; ++j)
					align.addLink(e[j], a[i].f, a[i].strength);
			}
		return align;
	}
	
	/**
	 * Replaces the specified phrase in the NL sentence with the given nonterminal.  All links to the 
	 * original phrase are removed.  This method returns a new alignment in which the specified phrase is 
	 * replaced.
	 * 
	 * @param from the beginning index of the phrase to replace.
	 * @param to the end index (exclusive) of the phrase to replace.
	 * @param replacement the replacement symbol.
	 * @return a new word alignment in which the specified phrase is replaced.
	 */
	public NTo1WordAlign replaceE(short from, short to, Nonterminal replacement) {
		Symbol[] E = this.E;
		if (E instanceof Terminal[])
			E = toSymbolArray(E);
		E = (Symbol[]) Arrays.replace(E, from, to, replacement);
		NTo1WordAlign align = new NTo1WordAlign(E, F, score);
		for (short i = 0; i < a.length; ++i)
			if (a[i] != null) {
				if (a[i].e < from)
					align.addLink(a[i].e, a[i].f, a[i].strength);
				else if (a[i].e >= to)
					align.addLink((short) (i-(to-from)+1), a[i].f, a[i].strength);
			}
		return align;
	}
	
	private Symbol[] toSymbolArray(Symbol[] array) {
		Symbol[] a = new Symbol[array.length];
		for (short i = 0; i < array.length; ++i)
			a[i] = array[i];
		return a;
	}
	
	/**
	 * Combines the specified node in the MR parse tree with its parent.  All links to the specified node 
	 * are transferred to its parent.  This method returns a new alignment in which the specified node is 
	 * combined with its parent.  If the specified node has no parent, then this alignment is returned
	 * instead. 
	 * 
	 * @param f the index of the MR parse node to combine with its parent.
	 * @return a new word alignment in which the specified node is combined with its parent. 
	 */
	public NTo1WordAlign combineF(short f) {
		if (!F[f].hasParent())
			return this;
		Node[] F = this.F[0].deepCopy().getDescends();
		short parent = (short) Arrays.indexOf(F, F[f].getParent());
		Production prod = ((ProductionSymbol) F[parent].getSymbol()).getProduction();
		Production arg = ((ProductionSymbol) F[f].getSymbol()).getProduction();
		short argIndex = F[parent].indexOf(F[f]);
		Node n = new Node(new ProductionSymbol(new Production(prod, arg, argIndex)));
		for (short i = 0; i < argIndex; ++i)
			n.addChild(F[parent].getChild(i).deepCopy());
		for (short i = 0; i < F[f].countChildren(); ++i)
			n.addChild(F[f].getChild(i).deepCopy());
		for (short i = (short) (argIndex+1); i < F[parent].countChildren(); ++i)
			n.addChild(F[parent].getChild(i).deepCopy());
		if (F[parent].hasParent()) {
			F[parent].getParent().replaceChild(F[parent], n);
			F = F[0].getDescends();
		} else
			F = n.getDescends();
		NTo1WordAlign align = new NTo1WordAlign(E, F, score);
		for (short i = 0; i < a.length; ++i)
			if (a[i] != null) {
				if (a[i].f < f)
					align.addLink(a[i].e, a[i].f, a[i].strength);
				else if (a[i].f == f)
					align.addLink(a[i].e, parent, a[i].strength);
				else
					align.addLink(a[i].e, (short) (a[i].f-1), a[i].strength);
			}
		return align;
	}
	
}
