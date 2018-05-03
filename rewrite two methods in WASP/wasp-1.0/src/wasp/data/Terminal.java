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

import wasp.util.Double;
import wasp.util.Int;
import wasp.util.Short;

/**
 * The class for terminal symbols.  Words in a sentence, and predicates in a meaning representations, are
 * all terminal symbols.
 * 
 * @author ywwong
 *
 */
public class Terminal extends Symbol {

	public static final int WILDCARD_ANY = 0;
    public static final int WILDCARD_NUM = 1;
    public static final int WILDCARD_UNUM = 2;
    public static final int WILDCARD_IDENT = 3;
    public static final int BOUNDARY = 4;
    public static final int NUM_SPECIAL_TERMS = 5;
	
	protected int displayId;
	protected short index;

	public Terminal(String str, boolean isWord) {
		displayId = Dictionary.term(str, false);
		if (isWord)
			id = Dictionary.term(lower(normalize(str)), true);
		else
			id = Dictionary.term(normalize(str), false);
		index = 0;
	}
	
	public Terminal(int id) {
		displayId = this.id = id;
		index = 0;
	}
	
	private String normalize(String str) {
		try {
			double num = Double.parseDouble(str);
			if (num == (int) num)
				return Int.toString((int) num);
			else
				return Double.toString(num);
		} catch (NumberFormatException e) {
			return str;
		}
	}
	
	private String lower(String str) {
		return (Dictionary.isIdent(str)) ? str : str.toLowerCase();
	}
	
	private Terminal(int id, int displayId, short index) {
		super(id);
		this.displayId = displayId;
		this.index = index;
	}

	public boolean equals(Object o) {
		if (o instanceof Terminal) {
			Terminal t = (Terminal) o;
			return id == t.id && index == t.index;
		}
		return false;
	}

	public boolean matches(Symbol sym) {
		if (sym instanceof Terminal) {
			Terminal t = (Terminal) sym;
			return id == WILDCARD_ANY
			|| (id == WILDCARD_NUM && t.isNum())
			|| (id == WILDCARD_UNUM && t.isUnum())
			|| (id == WILDCARD_IDENT && t.isIdent())
			|| id == t.id;
		}
		return false;
	}
	
	public int hashCode() {
		return id;
	}
	
	public Object copy() {
		return new Terminal(id, displayId, index);
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
	 * rule.  Only wildcards can be assigned a non-zero index.
	 */
	public void setIndex(short index) {
		if (isWildcard())
			this.index = index;
	}
	
	/**
	 * Indicates if this symbol can associate with other symbols in an SCFG rule.  Only wildcards can
	 * associate with other symbols.
	 */
	public boolean isIndexable() {
		return isWildcard();
	}
	
	/**
	 * Indicates if this symbol is a real number.
	 * 
	 * @return <code>true</code> if this symbol is a real number; <code>false</code> otherwise.
	 */
	public boolean isNum() {
		return Dictionary.isNum(id);
	}
	
	/**
	 * Indicates if this symbol is a uniform number (RoboCup domain-specific).
	 * 
	 * @return <code>true</code> if this symbol is a uniform number; <code>false</code> otherwise.
	 */
	public boolean isUnum() {
		return Dictionary.isUnum(id);
	}
	
	/**
	 * Indicates if this symbol is a CLang identifier (RoboCup domain-specific).
	 * 
	 * @return <code>true</code> if this symbol is a CLang identifier; <code>false</code> otherwise.
	 */
	public boolean isIdent() {
		return Dictionary.isIdent(id);
	}

	/**
	 * Indicates if this symbol is a wildcard.
	 * 
	 * @return <code>true</code> if this symbol is a wildcard; <code>false</code> otherwise.
	 */
	public boolean isWildcard() {
		return id == WILDCARD_ANY
		|| id == WILDCARD_NUM
		|| id == WILDCARD_UNUM
		|| id == WILDCARD_IDENT;
	}
	
	/**
	 * Indicates if this symbol denotes a sentence boundary.
	 * 
	 * @return <code>true</code> if this symbol denotes a sentence boundary; <code>false</code> 
	 * otherwise.
	 */
	public boolean isBoundary() {
		return id == BOUNDARY;
	}
	
	/**
	 * Creates and returns a wildcard that matches any terminals.
	 * 
	 * @return a wildcard that matches any terminals.
	 */
	public static Terminal wildcardAny() {
		return new Terminal(WILDCARD_ANY, WILDCARD_ANY, (short) 0);
	}
	
	/**
	 * Creates and returns a wildcard that matches any terminals that are real numbers.
	 * 
	 * @return a wildcard that matches any terminals that are real numbers.
	 */
	public static Terminal wildcardNum() {
		return new Terminal(WILDCARD_NUM, WILDCARD_NUM, (short) 0);
	}
	
	/**
	 * Creates and returns a wildcard that matches any terminals that are uniform numbers (RoboCup 
	 * domain-specific).
	 * 
	 * @return a wildcard that matches any terminals that are uniform numbers.
	 */
	public static Terminal wildcardUnum() {
		return new Terminal(WILDCARD_UNUM, WILDCARD_UNUM, (short) 0);
	}
	
	/**
	 * Creates and returns a wildcard that matches any terminals that are CLang identifiers (RoboCup
	 * domain-specific).
	 * 
	 * @return a wildcard that matches any terminals that are CLang identifiers.
	 */
	public static Terminal wildcardIdent() {
		return new Terminal(WILDCARD_IDENT, WILDCARD_IDENT, (short) 0);
	}
	
	/**
	 * Creates and returns a terminal node that indicates a sentence boundary.
	 * 
	 * @return a terminal node that indicates a sentence boundary.
	 */
	public static Terminal boundary() {
		return new Terminal(BOUNDARY, BOUNDARY, (short) 0);
	}

	///
	/// Textual representations
	///
	
	public String toString() {
    	StringBuffer sb = new StringBuffer();
        if (id == WILDCARD_ANY)
            sb.append("*t:Any");
        else if (id == WILDCARD_NUM)
            sb.append("*t:Num");
        else if (id == WILDCARD_UNUM)
            sb.append("*t:Unum");
        else if (id == WILDCARD_IDENT)
            sb.append("*t:Ident");
        else if (id == BOUNDARY)
            sb.append("*t:Bound");
        else
            sb.append(Dictionary.term(displayId));
        if (index > 0) {
        	sb.append('#');
        	sb.append(index);
        }
        return sb.toString();
	}

	/** Indicates if all tokens subsequently read by the <code>read</code> method are NL words. */
	public static boolean readWords = false;

	public static Symbol read(String token) {
		if (token.startsWith("*t:")) {
			token = token.substring(3);
			short index = 0;
			int pound = token.indexOf('#');
			if (pound > 0) {
				index = Short.parseShort(token.substring(pound+1));
				token = token.substring(0, pound);
			}
			Terminal t = null;
			if (token.equals("Any"))
				t = wildcardAny();
			else if (token.equals("Num"))
				t = wildcardNum();
			else if (token.equals("Unum"))
				t = wildcardUnum();
			else if (token.equals("Ident"))
				t = wildcardIdent();
			else if (token.equals("Bound"))
				t = boundary();
			if (t != null && index > 0)
				t.setIndex(index);
			return t;
		}
		return new Terminal(token, readWords);
	}
	
}
