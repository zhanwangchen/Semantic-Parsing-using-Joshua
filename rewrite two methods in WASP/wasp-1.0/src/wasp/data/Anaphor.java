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
 * Special symbols for dealing with anaphora.  These symbols can be useful in situations like this:
 * <blockquote>
 * <p>
 * <i>If player 5 has the ball then <b><u>it</u></b> should pass to players 8 or 11.</i>
 * <p>
 * <code>((bowner (player our {5})) (do <b><u>(player our {5})</u></b> (pass (player our {8
 * 11}))))</code>
 * </blockquote>
 * <p>
 * Here the meaning of the pronoun <i>it</i> depends on the meaning of the previous NP <i>player 5</i>.
 * Both of them refer to player 5 in our team.
 * <p>
 * The <code>Anaphor</code> symbols allow WASP to handle such kind of coreferences.  Like terminal
 * symbols, <code>Anaphor</code> symbols appear on the RHS of a rule.  When an <code>Anaphor</code>
 * symbol appears, it must be the only symbol on the RHS.  All <code>Anaphor</code> symbols are typed,
 * and the types correspond to nonterminals in the MRL grammar.  If there is an <code>Anaphor</code>
 * symbol of type <i>X</i> in the output MR when a sentence is parsed, then the meaning of the symbol
 * will be the same as the meaning of the previous NL phrase whose MR is of type <i>X</i> (an MR is of
 * type <i>X</i> if an <i>X</i> nonterminal is at the top of its parse tree).  If there is no such NL
 * phrase, then the <code>Anaphor</code> symbol is said to be <i>unresolved</i>, and the output MR will
 * be considered invalid.
 * <p>
 * For training, the above MR would be transformed into:
 * <blockquote>
 * <p>
 * <code>((bowner (player our {5})) (do <b><u>*^:Player</u></b> (pass (player our {8 11}))))</code>
 * </blockquote>
 * <p>
 * Here <code>*^:Player</code> is an <code>Anaphor</code> symbol of type <code>Player</code>.
 * <p>
 * There is code that automatically replaces certain MR expressions with <code>Anaphor</code> symbols
 * for training.  See <tt>GIZAPlusPlus.createAnaphora()</tt> for more detail.
 *  
 * @see wasp.align.GIZAPlusPlus#createAnaphora()
 * @author ywwong
 *
 */
public class Anaphor extends Symbol {

	public Anaphor(int nonterm) {
		super(nonterm);
	}
	
	public boolean equals(Object o) {
		return o instanceof Anaphor && id == ((Anaphor) o).id;
	}

	public int hashCode() {
		return id+1279;
	}

	public Object copy() {
		return new Anaphor(id);
	}

	public boolean matches(Symbol sym) {
		return equals(sym);
	}

	///
	/// Textual representations
	///
	
	public String toString() {
		return "*^:"+Dictionary.nonterm(id);
	}

	public static Symbol read(String token) {
		if (!token.startsWith("*^:"))
			return null;
		return new Anaphor(Dictionary.nonterm(token.substring(3)));
	}
	
}
