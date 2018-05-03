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

import wasp.data.Node;
import wasp.data.Symbol;
import wasp.mrl.ProductionSymbol;

/**
 * A sentence-MR pair along with their word-to-word alignment.  An MR is represented using a list of MRL 
 * productions used to generate it.  This way we ensure that the resulting synchronous grammar outputs 
 * only well-formed meaning representations.  
 *  
 * @author ywwong
 *
 */
public abstract class WordAlign implements Comparable {

	protected Symbol[] E;
	protected Node[] F;
	protected double score;
	
	/**
	 * Creates an empty alignment for the specified NL sentence and linearized MR parse.  The new 
	 * alignment will have the specified score.
	 * 
	 * @param E an NL sentence.
	 * @param F a linearized MR parse.
	 * @param score the alignment score.
	 */
	protected WordAlign(Symbol[] E, Node[] F, double score) {
		this.E = E;
		this.F = F;
		this.score = score;
	}

	public abstract boolean equals(Object o);
	
	public abstract int hashCode();
	
	/**
	 * Word alignments with higher scores are ranked higher.  This ordering is <i>not</i> consistent
	 * with equals because distinct word alignments with equal scores should not be treated as equal.
	 */
	public int compareTo(Object o) {
		if (score > ((WordAlign) o).score)
			return -1;
		else if (score < ((WordAlign) o).score)
			return 1;
		else
			return 0;
	}
	
	/**
	 * Returns the NL sentence.
	 * 
	 * @return the NL sentence.
	 */
	public Symbol[] getE() {
		return E;
	}
	
	/**
	 * Returns the specified word in the NL sentence.
	 * 
	 * @param i a word index.
	 * @return the <code>i</code>-th NL word.
	 */
	public Symbol getE(short i) {
		return E[i];
	}
	
	/**
	 * Returns the linearized MR parse.
	 * 
	 * @return the linearized MR parse.
	 */
	public Node[] getF() {
		return F;
	}
	
	/**
	 * Returns the specified node in the linearized MR parse.
	 * 
	 * @param i a node index.
	 * @return the <code>i</code>-th MR parse node.
	 */
	public Node getF(short i) {
		return F[i];
	}
	
	/**
	 * Returns the length of the NL sentence.
	 * 
	 * @return the length of the NL sentence.
	 */
	public short lengthE() {
		return (short) E.length;
	}
	
	/**
	 * Returns the length of the linearized MR parse.
	 * 
	 * @return the length of the linearized MR parse.
	 */
	public short lengthF() {
		return (short) F.length;
	}
	
	/**
	 * Returns the score of this alignment.
	 * 
	 * @return the score of this alignment.
	 */
	public double getScore() {
		return score;
	}
	
	/**
	 * Assign a score to this alignment.
	 * 
	 * @param score the score to assign.
	 */
	public void setScore(double score) {
		this.score = score;
	}
	
	/**
	 * Indicates if the specified NL word is linked to the specified node in the MR parse.
	 * 
	 * @param e a word index.
	 * @param f a node index.
	 * @return <code>true</code> if the <code>e</code>-th NL word is linked to the <code>f</code>-th 
	 * node in the MR parse; <code>false</code> otherwise.
	 */
	public abstract boolean isLinked(short e, short f);
	
	/**
	 * Returns the first link from the specified NL word in this alignment, if any.
	 * 
	 * @param e a word index.
	 * @return the first link from the <code>e</code>-th NL word in this alignment; <code>null</code> if
	 * none exists.
	 */
	public abstract Link getFirstLinkFromE(short e);
	
	/**
	 * Returns the number of links from the specified NL word in this alignment.
	 * 
	 * @param e a word index.
	 * @return the number of links from the <code>e</code>-th NL word in this alignment.
	 */
	public abstract short countLinksFromE(short e);
	
	/**
	 * Indicates if the specified NL word is linked in this alignment.
	 * 
	 * @param e a word index.
	 * @return <code>true</code> if the <code>e</code>-th NL word is linked in this alignment;
	 * <code>false</code> otherwise.
	 */
	public boolean isLinkedFromE(short e) {
		return getFirstLinkFromE(e) != null;
	}
	
	/**
	 * Returns the first link from the specified MR parse node in this alignment, if any.
	 * 
	 * @param f a node index.
	 * @return the link from the <code>f</code>-th MR parse node in this alignment; <code>null</code> if 
	 * none exists.
	 */
	public abstract Link getFirstLinkFromF(short f);
	
	/**
	 * Returns the number of links from the specified MR parse node in this alignment.
	 * 
	 * @param f a node index.
	 * @return the number of links from the <code>f</code>-th MR parse node in this alignment.
	 */
	public abstract short countLinksFromF(short f);
	
	/**
	 * Indicates if the specified MR parse node is linked in this alignment.
	 * 
	 * @param f a node index.
	 * @return <code>true</code> if the <code>f</code>-th MR parse node is linked in this alignment;
	 * <code>false</code> otherwise.
	 */
	public boolean isLinkedFromF(short f) {
		return getFirstLinkFromF(f) != null;
	}
	
	/**
	 * Adds a new link to this alignment.
	 * 
	 * @param e the index of the word to link.
	 * @param f the index of the MR parse node to link.
	 * @param strength the strength of the new link.
	 */
	public void addLink(short e, short f, double strength) {
		addNewLink(new Link(this, e, f, strength));
	}
	
	/**
	 * Adds a new link to this alignment, with the link strength equal to one. 
	 * 
	 * @param e the index of the word to link.
	 * @param f the index of the MR parse node to link.
	 */
	public void addLink(short e, short f) {
		addNewLink(new Link(this, e, f, 1));
	}
	
	/**
	 * Adds a copy of the specified link to this alignment.
	 * 
	 * @param link the link to add.
	 */
	public void addLink(Link link) {
		addNewLink(new Link(this, link.e, link.f, link.strength));
	}

	/**
	 * Adds the specified link to this alignment.
	 * 
	 * @param link the link to add.
	 */
	protected abstract void addNewLink(Link link);
	
	/**
	 * Removes the specified link from this alignment.  If the link does not exist, then no links will
	 * be removed.
	 * 
	 * @param link the link to remove.
	 */
	public abstract void removeLink(Link link);
	
	/**
	 * Removes all links from the specified NL word.
	 * 
	 * @param e a word index.
	 */
	public abstract void removeLinksFromE(short e);
	
	/**
	 * Removes all links from the specified MR parse node.
	 * 
	 * @param f a node index.
	 */
	public abstract void removeLinksFromF(short f);
	
	/**
	 * Returns the textual representation of this alignment.
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (short i = 0; i < E.length; ++i) {
			if (i > 0)
				sb.append(' ');
			sb.append('(');
			sb.append(i);
			sb.append(") ");
			sb.append(E[i]);
		}
		sb.append('\n');
		for (short i = 0; i < F.length; ++i) {
			if (i > 0)
				sb.append(' ');
			sb.append(((ProductionSymbol) F[i].getSymbol()).toConcise());
			sb.append(" ({");
			for (Link link = getFirstLinkFromF(i); link != null; link = link.next) {
				sb.append(' ');
				sb.append(link.e);
			}
			sb.append(" })");
		}
		return sb.toString();
	}
	
}
