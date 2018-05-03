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
package wasp.mrl;

import java.util.ArrayList;

import wasp.data.Anaphor;
import wasp.data.Node;
import wasp.data.Nonterminal;
import wasp.data.Symbol;
import wasp.data.Terminal;
import wasp.main.Config;
import wasp.util.Arrays;
import wasp.util.Int;
import wasp.util.Short;

/**
 * Production rules of MRL grammars.
 * 
 * @author ywwong
 *
 */
public class Production {

	/** The LHS nonterminal. */
	private int lhs;
	/** The RHS string. */
	private Symbol[] rhs;
	/** Indicates if this production is in the original, unambiguous MRL grammar. */
	private boolean orig;
	
	/** Argument types in left-to-right order. */
	private int[] args;
	/** Parse of the RHS string based on the original, unambiguous MRL grammar. */
	private Node parse;
	/** Paths from the root of the parse tree of the RHS to all nonterminals on the frontier. */
	private short[][] paths;
	/** Hash code of this production. */
	private int hash;
	
	/**
	 * Creates a new production with the specified LHS and RHS.  The resulting production will be part of
	 * the original, unambiguous MRL grammar.
	 *   
	 * @param lhs the ID of the LHS nonterminal.
	 * @param rhs the RHS string.
	 */
	public Production(int lhs, Symbol[] rhs) {
		this.lhs = lhs;
		this.rhs = rhs;
		orig = true;
		
		ArrayList list = new ArrayList();
		for (short i = 0; i < rhs.length; ++i)
			if (rhs[i] instanceof Nonterminal)
				list.add(new Int(rhs[i].getId()));
		args = Arrays.toIntArray(list);
		parse = new Node(new ProductionSymbol(this));
		for (short i = 0; i < rhs.length; ++i)
			if (rhs[i] instanceof Nonterminal)
				parse.addChild(new Node(rhs[i]));
		init();
	}

	/**
	 * Creates a new dummy production with the specified RHS nonterminal.
	 *   
	 * @param rhs the ID of the RHS nonterminal.
	 */
	public Production(int rhs) {
		lhs = -1;
		this.rhs = new Symbol[1];
		this.rhs[0] = new Nonterminal(rhs);
		orig = false;
		
		args = new int[1];
		args[0] = rhs;
		parse = new Node(new ProductionSymbol(this));
		parse.addChild(new Node(this.rhs[0]));
		init();
	}

	/**
	 * Creates a specialization of a production by replacing the wildcard on the RHS with the specified
	 * terminal symbol.  The terminal symbol must be compatible with the wildcard.  Otherwise, a
	 * <code>RuntimeException</code> is thrown.
	 * 
	 * @param prod the production to specialize.
	 * @param term the replacement terminal symbol.
	 * @throws RuntimeException if specialization fails.
	 */
	public Production(Production prod, Terminal term) {
		if (!prod.isWildcard() || !prod.rhs[0].matches(term))
			throw new RuntimeException();
		
		lhs = prod.lhs;
		rhs = new Symbol[1];
		rhs[0] = (Symbol) term.copy();
		orig = false;
		
		args = new int[0];
		parse = new Node(new ProductionSymbol(this));
		init();
	}
	
	/**
	 * Creates a new production by combining a production with its argument at the specified argument
	 * index.  The LHS of the argument must match the required argument type.  Also, the argument must
	 * not be a wildcard production.  Otherwise, a <code>RuntimeException</code> is thrown.
	 *  
	 * @param prod a production.
	 * @param arg an argument.
	 * @param argIndex a non-negative argument index.
	 * @throws RuntimeException if the productions fail to combine.
	 */
	public Production(Production prod, Production arg, short argIndex) {
		if (arg.getLhs() != prod.args[argIndex] || arg.isWildcard())
			throw new RuntimeException();
		
		lhs = prod.lhs;
		rhs = new Symbol[prod.rhs.length+arg.rhs.length-1];
		short i = 0, j = 0, k = 0, l = -1;
		for (; ; ++i, ++j) {
			if (prod.rhs[j] instanceof Nonterminal)
				if (++l == argIndex)
					break;
			rhs[i] = (Symbol) prod.rhs[j].copy();
		}
		for (; k < arg.rhs.length; ++i, ++k)
			rhs[i] = (Symbol) arg.rhs[k].copy();
		for (++j; j < prod.rhs.length; ++i, ++j)
			rhs[i] = (Symbol) prod.rhs[j].copy();
		ArrayList list = new ArrayList();
		for (i = 0; i < rhs.length; ++i)
			if (rhs[i] instanceof Nonterminal)
				list.add(new Int(rhs[i].getId()));
		args = Arrays.toIntArray(list);
		parse = prod.parse.deepCopy();
		Node[] n = parse.getDescends(Nonterminal.class);
		n[argIndex].getParent().replaceChild(n[argIndex], arg.parse.deepCopy());
		init();
	}
	
	private void init() {
		Node[] a = parse.getDescends(Nonterminal.class);
		paths = new short[a.length][];
		for (short i = 0; i < a.length; ++i) {
			ArrayList list = new ArrayList();
			Node n = a[i];
			while (n.getParent() != null) {
				list.add(new Short(n.getParentIndex()));
				n = n.getParent();
			}
			paths[i] = Arrays.toShortArray(list);
			Arrays.reverse(paths[i]);
		}
		hash = 1;
		hash = 31*hash + lhs;
		hash = 31*hash + Arrays.hashCode(rhs);
	}
	
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (o instanceof Production) {
			Production p = (Production) o;
			return lhs == p.lhs && Arrays.equal(rhs, p.rhs);
		}
		return false;
	}
	
	public int hashCode() {
		return hash;
	}

	/**
	 * Returns the ID of the LHS nonterminal of this production.  <code>-1</code> is returned if this
	 * is a dummy production.
	 * 
	 * @return the ID of the LHS nonterminal of this production.
	 */
	public int getLhs() {
		return lhs;
	}
	
	public Symbol[] getRhs() {
		return rhs;
	}
	
	public Symbol getRhs(short i) {
		return rhs[i];
	}
	
	public short length() {
		return (short) rhs.length;
	}
	
	/**
	 * Indicates if this production is in the original, unambiguous MRL grammar.
	 * 
	 * @return <code>true</code> if this production is in the original, unambiguous MRL grammar; 
	 * <code>false</code> otherwise.
	 */
	public boolean isOrig() {
		return orig;
	}
	
	/**
	 * Returns an array containing all argument types of this production, listed in left-to-right order.
	 * An empty array is returned if this production does not require any arguments.
	 * 
	 * @return an array of nonterminal IDs that specify the argument types of this production.
	 */
	public int[] getArgs() {
		return args;
	}
	
	/**
	 * Returns the number of arguments that this production requires.
	 * 
	 * @return the number of arguments that this production requires.
	 */
	public short countArgs() {
		return (short) args.length;
	}
	
	/**
	 * Indicates if this production requires any arguments.
	 * 
	 * @return <code>true</code> if this production requires some arguments; <code>false</code> 
	 * otherwise.
	 */
	public boolean hasArgs() {
		return args.length > 0;
	}
	
	/**
	 * Returns a parse of this production based on the original, unambiguous MRL grammar.  Wherever a 
	 * nonterminal symbol appears in the parse tree, an argument is required.
	 * 
	 * @return a parse of this production based on the original, unambiguous MRL grammar.
	 */
	public Node getParse() {
		return parse;
	}
	
	/**
	 * Returns the path from the root of the parse tree of the RHS to the specified nonterminal on 
	 * the frontier. 
	 * 
	 * @param i a non-negative number.
	 * @return the path from the root of the parse tree of the RHS to the <code>i</code>-th nontemrinal
	 * on the frontier.
	 */
	public short[] getPath(short i) {
		return paths[i];
	}
	
	/**
	 * Indicates if this production is a unary production (i.e. RHS consists of a single nonterminal).
	 * 
	 * @return <code>true</code> if this production is unary; <code>false</code> otherwise.
	 */
	public boolean isUnary() {
		return rhs.length == 1 && rhs[0] instanceof Nonterminal;
	}
	
	/**
	 * Indicates if the RHS of this production is only a wildcard terminal.
	 * 
	 * @return <code>true</code> if the RHS of this production is only a wildcard terminal;
	 * <code>false</code> otherwise.
	 */
	public boolean isWildcard() {
		return rhs.length == 1 && rhs[0] instanceof Terminal && ((Terminal) rhs[0]).isWildcard();
	}
	
	/**
	 * Indicates if the RHS of this production is only an <code>Anaphor</code> symbol.
	 * 
	 * @see wasp.data.Anaphor
	 * @return <code>true</code> if the RHS of this production is only an <code>Anaphor</code> symbol;
	 * <code>false</code> otherwise.
	 */
	public boolean isAnaphor() {
		return rhs.length == 1 && rhs[0] instanceof Anaphor;
	}
	
	/**
	 * Indicates if this production is a dummy.
	 * 
	 * @return <code>true</code> if this production is a dummy; <code>false</code> otherwise.
	 */
	public boolean isDummy() {
		return lhs < 0;
	}

	/**
	 * Returns an interned copy of this production.  If none exists, then this production is returned
	 * instead.
	 * 
	 * @return an interned copy of this production.
	 */
	public Production intern() {
		return Config.getMRLGrammar().intern(this);
	}
	
	/**
	 * Returns an interned copy of the production that this production is tied to (e.g. <code>State -> 
	 * stateid('virginia')</code> could be tied to <code>State -> stateid('texas')</code>).
	 * 
	 * @return an interned copy of the production that this production is tied to.
	 */
	public Production tied() {
		return Config.getMRLGrammar().tied(this);
	}
	
	///
	/// Textual representations
	///
	
	/**
	 * Returns the textual representation of this production.
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(new Nonterminal(lhs));
		sb.append(" -> ({");
		for (short i = 0; i < rhs.length; ++i) {
			sb.append(' ');
			sb.append(rhs[i]);
		}
		sb.append(" })");
		return sb.toString();
	}

	/**
	 * Returns a production that part of the given line of text represents.  Beginning with the token at 
	 * the specified index, this method finds the shortest substring of the line that is the textual 
	 * representation of a production.  If such a substring exists, then the production that it 
	 * represents is returned, and the <code>index</code> argument is set to the token index immediately 
	 * after the end of the substring.  Otherwise, <code>null</code> is returned and the 
	 * <code>index</code> argument remains unchanged.
	 * <p>
	 * This method is for reading productions in the original, unambiguous MRL grammar.  For other
	 * productions, use the <code>readParse</code> method.
	 * 
	 * @param line a line of text containing the textual representation of a production.
	 * @param index the beginning token index to consider; it is also an <i>output</i> variable for 
	 * the token index immediately after the end of the consumed substring.
	 * @return the production that the substring <code>line[index, i)</code> represents, for the 
	 * smallest <code>i</code> possible; <code>null</code> if no such <code>i</code> exists.
	 */
	public static Production read(String[] line, Int index) {
		int i = index.val;
		Nonterminal lhs;
		if (i >= line.length || (lhs = (Nonterminal) Nonterminal.read(line[i])) == null)
			return null;
		++i;
		if (i == line.length || !line[i].equals("->"))
			return null;
		++i;
		if (i == line.length || !line[i].equals("({"))
			return null;
		Terminal.readWords = false;
		ArrayList list = new ArrayList();
		for (++i; i < line.length && !line[i].equals("})"); ++i) {
			Symbol sym = Symbol.read(line[i]);
			if (sym == null)
				return null;
			list.add(sym);
		}
		if (i == line.length)
			return null;
		Symbol[] rhs = (Symbol[]) list.toArray(new Symbol[0]);
		index.val = i+1;
		return new Production(lhs.getId(), rhs);
	}
	
	/**
	 * Returns a production given the textual representation of a parse tree.  It is similar to the
	 * <code>read</code> method, except that the input is the textual representation of the parse tree
	 * of the production's RHS string, based on the original, unambiguous MRL grammar.
	 * 
	 * @param line a line of text containing the textual representation of a parse tree.
	 * @param index the beginning token index to consider; it is also an <i>output</i> variable for 
	 * the token index immediately after the end of the consumed substring.
	 * @return the production that corresponds to the textual representation of a parse tree; 
	 * <code>null</code> if none exists.
	 */
	public static Production readParse(String[] line, Int index) {
		Node.readSyn = false;
		Node parse = Node.read(line, index);
		if (parse == null)
			return null;
		return readParse(parse);
	}
	
	private static Production readParse(Node parse) {
		Production prod = ((ProductionSymbol) parse.getSymbol()).getProduction();
		short nc = parse.countChildren();
		for (short i = (short) (nc-1); i >= 0; --i) {
			Node child = parse.getChild(i);
			if (child.getSymbol() instanceof ProductionSymbol)
				prod = new Production(prod, readParse(child), i);
		}
		return prod;
	}
	
}
