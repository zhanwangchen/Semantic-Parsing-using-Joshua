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

import wasp.util.Numberer;
import wasp.util.RadixMap;

/**
 * Mappings from strings to their IDs.  Terminal strings and nonterminal strings have their own separate
 * mappings.
 * 
 * @author ywwong
 *
 */
public class Dictionary {

	private static class StringProperties {
		public boolean isWord;
		public boolean isNum;
		public boolean isUnum;
		public boolean isIdent;
		public StringProperties(String str, boolean isWord) {
			this.isWord = isWord;
			isNum = isNum(str);
			isUnum = isUnum(str);
			isIdent = isIdent(str);
		}
		public static boolean isNum(String str) {
			try {
				Double.parseDouble(str);
				return true;
			} catch (NumberFormatException e) {
				return false;
			}
		}
		public static boolean isUnum(String str) {
			try {
				double num = Double.parseDouble(str);
				return num == (int) num && 1 <= num && num <= 11;
			} catch (NumberFormatException e) {
				return false;
			}
		}
		public static boolean isIdent(String str) {
	        int under = 0;
	        int cap = 0;
	        int lower = 0;
	        int digit = 0;
	        for (int i = str.length()-1; i >= 0; --i) {
	            char c = str.charAt(i);
	            if (c == '_')
	                ++under;
	            else if ('A' <= c && c <= 'Z')
	                ++cap;
	            else if ('a' <= c && c <= 'z')
	                ++lower;
	            else if ('0' <= c && c <= '9')
	                ++digit;
	        }
	        return (under > 0 && cap > 0) || (cap > 1 && (lower > 0 || digit > 0));
		}
	}
	
	private static Numberer terms = new Numberer(Terminal.NUM_SPECIAL_TERMS);
	private static Numberer nonterms = new Numberer();
	private static RadixMap properties = new RadixMap();
	private static int nwords = 0;
	
	private Dictionary() {}
	
	public static String term(int id) {
		return (String) terms.getObj(id);
	}
	
	public static int term(String str, boolean isWord, boolean add) {
		int id = terms.getId(str, add);
		if (id < 0)
			return id;
		StringProperties p = (StringProperties) properties.get(id);
		if (p == null) {
			p = new StringProperties(str, false);
			properties.put(id, p);
		}
		if (isWord && !p.isWord) {
			p.isWord = true;
			++nwords;
		}
		return id;
	}
	
	public static int term(String str, boolean isWord) {
		return term(str, isWord, true);
	}
	
	public static int countTerms() {
		return terms.getNextId();
	}
	
	public static int countWords() {
		return nwords;
	}
	
	public static boolean isWord(int id) {
		StringProperties p = (StringProperties) properties.get(id);
		return (p==null) ? false : p.isWord;
	}

	public static boolean isNum(int id) {
		StringProperties p = (StringProperties) properties.get(id);
		return (p==null) ? false : p.isNum;
	}

	public static boolean isNum(String str) {
		return StringProperties.isNum(str);
	}
	
	public static boolean isUnum(int id) {
		StringProperties p = (StringProperties) properties.get(id);
		return (p==null) ? false : p.isUnum;
	}

	public static boolean isUnum(String str) {
		return StringProperties.isUnum(str);
	}
	
	public static boolean isIdent(int id) {
		StringProperties p = (StringProperties) properties.get(id);
		return (p==null) ? false : p.isIdent;
	}

	public static boolean isIdent(String str) {
		return StringProperties.isIdent(str);
	}
	
	public static String nonterm(int id) {
		return (String) nonterms.getObj(id);
	}
	
	public static int nonterm(String str) {
		return nonterms.getId(str, true);
	}
	
	public static int countNonterms() {
		return nonterms.getNextId();
	}
	
}
