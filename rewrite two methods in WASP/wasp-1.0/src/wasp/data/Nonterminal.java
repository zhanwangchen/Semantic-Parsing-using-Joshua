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

import wasp.util.Short;

/**
 * The class for nonterminal symbols.
 * 
 * @author ywwong
 *
 */
public class Nonterminal extends Symbol {

	protected short index;
	
	public Nonterminal(String str) {
		super(Dictionary.nonterm(str));
		index = 0;
	}
	
	public Nonterminal(int id) {
		super(id);
		index = 0;
	}
	
	private Nonterminal(int id, short index) {
		super(id);
		this.index = index;
	}

	public boolean equals(Object o) {
		if (o instanceof Nonterminal) {
			Nonterminal n = (Nonterminal) o;
			return id == n.id && index == n.index;
		}
		return false;
	}
	
	public boolean matches(Symbol sym) {
		return false;
	}
	
	public int hashCode() {
		return id+1259;
	}
	
	public Object copy() {
		return new Nonterminal(id, index);
	}

	/**
	 * Returns the index of this symbol which indicates its association with other symbols in an SCFG 
	 * rule.
	 */
	public short getIndex() {
		return index;
	}
	
	/**
	 * Assigns an index to this symbol which indicates its association with other symbols in an SCFG 
	 * rule.
	 */
	public void setIndex(short index) {
		this.index = index;
	}
	
	/**
	 * Indicates if this symbol can associate with other symbols in an SCFG rule.
	 */
	public boolean isIndexable() {
		return true;
	}

	///
	/// Textual representations
	///
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("*n:");
		sb.append(Dictionary.nonterm(id));
		if (index > 0) {
			sb.append('#');
			sb.append(index);
		}
		return sb.toString();
	}
	
	public static Symbol read(String token) {
		if (!token.startsWith("*n:"))
			return null;
		token = token.substring(3);
		short index = 0;
		int pound = token.indexOf('#');
		if (pound > 0) {
			index = Short.parseShort(token.substring(pound+1));
			token = token.substring(0, pound);
		}
		Nonterminal n = new Nonterminal(token);
		if (index > 0)
			n.setIndex(index);
		return n;
	}
	
}
