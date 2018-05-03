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
package wasp.data;

/**
 * A node wrapper that re-implements the equality test based on equivalence of symbols.
 * 
 * @author ywwong
 *
 */
public class Tree {

	public Node node;
	
	public Tree(Node node) {
		this.node = node;
	}
	
	/**
	 * Indicates if this tree is equal to the specified tree.  Two trees are considered equal if they 
	 * have the same tree structure and have the same node labels.
	 */
	public boolean equals(Object o) {
		if (o instanceof Tree) {
			Node x = node;
			Node y = ((Tree) o).node;
			return x == y || equal(x, y);
		}
		return false;
	}
	
	/**
	 * Indicates if the trees rooted at the specified nodes are equal.  Two trees are considered equal
	 * if they have the same tree structure and have the same node labels.
	 * 
	 * @param x the root of a tree.
	 * @param y the root of the tree with which to compare.
	 */
	public static boolean equal(Node x, Node y) {
		if (x.getSymbol().equals(y.getSymbol())) {
			short xn = x.countChildren();
			short yn = y.countChildren();
			if (xn == yn) {
				Node[] xc = x.getChildren();
				Node[] yc = y.getChildren();
				for (short i = 0; i < xn; ++i)
					if (!equal(xc[i], yc[i]))
						return false;
				return true;
			}
		}
		return false;
	}
	
	public int hashCode() {
		return node.getHash();
	}
	
}
