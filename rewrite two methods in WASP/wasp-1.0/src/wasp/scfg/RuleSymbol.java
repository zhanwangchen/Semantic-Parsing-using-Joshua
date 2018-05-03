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
package wasp.scfg;

import wasp.data.Symbol;
import wasp.util.Arrays;
import wasp.util.Int;

/**
 * SCFG rules encapsulated in symbols.  These symbols are used in parse trees.
 * 
 * @author ywwong
 *
 */
public class RuleSymbol extends Symbol {

	private Rule rule;
	
	/**
	 * Creates a new symbol that encapsulates the specified SCFG rule.
	 * 
	 * @param rule an SCFG rule.
	 */
	public RuleSymbol(Rule rule) {
		this.rule = rule;
	}
	
	public boolean equals(Object o) {
		return o instanceof RuleSymbol && rule.equals(((RuleSymbol) o).rule);
	}

	public boolean matches(Symbol sym) {
		return false;
	}
	
	public int hashCode() {
		return rule.hashCode();
	}

	public Object copy() {
		return new RuleSymbol(rule);
	}

	/**
	 * Returns the rule encapsulated in this symbol.
	 * 
	 * @return the rule encapsulated in this symbol.
	 */
	public Rule getRule() {
		return rule;
	}

	///
	/// Textual representations
	///
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("*r:");
		sb.append(rule.toString().replaceAll("\\\\", "\\\\\\\\").replaceAll("_", "\\\\_").replace(' ', '_'));
		return sb.toString();
	}
	
	public static Symbol read(String token) {
		if (!token.startsWith("*r:"))
			return null;
		Rule.readInit = false;
		StringBuffer sb = new StringBuffer();
		for (int i = 3; i < token.length(); ++i) {
			char c = token.charAt(i);
			if (c == '\\')
				sb.append(token.charAt(++i));
			else if (c == '_')
				sb.append(' ');
			else
				sb.append(c);
		}
		return new RuleSymbol(Rule.read(Arrays.tokenize(sb.toString()), new Int(0)));
	}

}
