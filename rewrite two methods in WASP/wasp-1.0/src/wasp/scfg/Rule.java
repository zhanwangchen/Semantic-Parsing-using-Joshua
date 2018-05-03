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

import java.util.ArrayList;

import wasp.data.Nonterminal;
import wasp.data.Symbol;
import wasp.data.Terminal;
import wasp.math.Math;
import wasp.mrl.Production;
import wasp.util.Arrays;
import wasp.util.Double;
import wasp.util.Int;
import wasp.util.Short;

/**
 * Production rules in a synchronous context-free grammar.
 * 
 * @author ywwong
 *
 */
public class Rule {

	/** The LHS nonterminal. */
	private int lhs;
	/** The NL string on the RHS. */
	private Symbol[] E;
	/** Size of word gaps to the right of each RHS NL symbol. */
	private short[] gaps;
	/** The MRL string on the RHS. */
	private Symbol[] F;
	/** The corresponding production in the MRL grammar. */
	private Production prod;
	/** Indicates if this rule is an initial rule. */
	private boolean init;
	/** Indicates if this rule is active. */
	private boolean active;
	/** The weight of this rule in a log-linear model. */
	private double weight;
	/** The outer score of this rule. */
	private double outer;

	/** The number of non-zero word gaps in this rule. */
	private short ngaps;
	/** Hash code of this rule. */
	private int hash;
	
	/**
	 * Creates a new SCFG rule with the specified arguments, assuming the corresponding MRL production
	 * already exists in the MRL grammar.
	 * 
	 * @param lhs the ID of the LHS nonterminal.
	 * @param E the NL string on the RHS.
	 * @param gaps size of word gaps to the right of each NL symbol.
	 * @param F the MRL string on the RHS.
	 * @param init indicates if this rule is an initial rule.
	 */
	public Rule(int lhs, Symbol[] E, short[] gaps, Symbol[] F, boolean init) {
		this.lhs = lhs;
		this.E = E;
		this.gaps = gaps;
		this.F = F;
		Symbol[] rhs = (Symbol[]) Arrays.copy(F);
		for (short i = 0; i < rhs.length; ++i)
			rhs[i].setIndex((short) 0);
		// assuming that an interned copy of the new production already exists...
		prod = new Production(lhs, rhs).intern();
		this.init = init;
		active = true;
		weight = 0;
		outer = Double.NEGATIVE_INFINITY;
		init();
	}

	/**
	 * Creates a new SCFG rule with the specified arguments.
	 * 
	 * @param prod an interned copy of the corresponding MRL production.
	 * @param E the NL string on the RHS.
	 * @param gaps size of word gaps to the right of each NL symbol.
	 * @param init indicates if this rule is an initial rule.
	 */
	public Rule(Production prod, Symbol[] E, short[] gaps, boolean init) {
		lhs = prod.getLhs();
		this.E = E;
		this.gaps = gaps;
		F = (Symbol[]) Arrays.copy(prod.getRhs());
		for (short i = 0, j = 1; i < F.length; ++i)
			if (F[i].isIndexable())
				F[i].setIndex(j++);
		this.prod = prod;
		this.init = init;
		active = true;
		weight = 0;
		outer = Double.NEGATIVE_INFINITY;
		init();
	}
	
	/**
	 * Creates a new dummy rule with the specified RHS nonterminal.
	 *   
	 * @param rhs the ID of the RHS nonterminal.
	 */
	public Rule(int rhs) {
		lhs = -1;
		E = new Symbol[1];
		E[0] = new Nonterminal(rhs);
		E[0].setIndex((short) 1);
		gaps = new short[1];
		F = new Symbol[1];
		F[0] = new Nonterminal(rhs);
		F[0].setIndex((short) 1);
		prod = new Production(rhs);
		init = false;
		active = true;
		weight = 0;
		outer = Double.NEGATIVE_INFINITY;
		init();
	}

	/**
	 * Creates a specialization of a rule by replacing the wildcards on the RHS with the specified
	 * terminal symbol.  The terminal symbol must be compatible with the wildcards.  Otherwise, a
	 * <code>RuntimeException</code> is thrown.
	 * 
	 * @param rule the rule to specialize.
	 * @param term the replacement terminal symbol.
	 * @throws RuntimeException if specialization fails.
	 */
	public Rule(Rule rule, Terminal term) {
		if (!rule.isWildcard() || !rule.E[0].matches(term))
			throw new RuntimeException();
		
		lhs = rule.lhs;
		E = new Symbol[1];
		E[0] = (Symbol) term.copy();
		gaps = new short[1];
		F = new Symbol[1];
		F[0] = (Symbol) term.copy();
		prod = new Production(rule.getProduction(), term);
		init = false;
		active = true;
		weight = 0;
		outer = Double.NEGATIVE_INFINITY;
		init();
	}
	
	private void init() {
		ngaps = 0;
		for (short i = 0; i < gaps.length; ++i)
			if (gaps[i] > 0)
				++ngaps;
		hash = 1;
		hash = 31*hash + lhs;
		hash = 31*hash + Arrays.hashCode(E);
		hash = 31*hash + Arrays.hashCode(gaps);
		hash = 31*hash + Arrays.hashCode(F);
	}
	
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (o instanceof Rule) {
			Rule r = (Rule) o;
			return lhs == r.lhs && Arrays.equal(E, r.E) && Arrays.equal(gaps, r.gaps) 
			&& Arrays.equal(F, r.F);
		}
		return false;
	}
	
	public int hashCode() {
		return hash;
	}
	
	/**
	 * Returns the ID of the LHS nonterminal of this rule.  <code>-1</code> is returned if this is a 
	 * dummy rule.
	 * 
	 * @return the ID of the LHS nonterminal of this rule.
	 */
	public int getLhs() {
		return lhs;
	}
	
	public Symbol[] getE() {
		return E;
	}
	
	public Symbol getE(short i) {
		return E[i];
	}
	
	/**
	 * Returns the size of word gap to the right of the specified NL symbol.
	 * 
	 * @param i a position index.
	 * @return the size of word gap to the right of the <code>i</code>-th NL symbol.
	 */
	public short getGap(short i) {
		return gaps[i];
	}
	
	public Symbol[] getF() {
		return F;
	}
	
	public Symbol getF(short i) {
		return F[i];
	}
	
	public short lengthE() {
		return (short) E.length;
	}
	
	public short lengthF() {
		return (short) F.length;
	}
	
	public Production getProduction() {
		return prod;
	}
	
	/**
	 * Indicates if this rule is an initial rule.
	 * 
	 * @return <code>true</code> if this rule is an initial rule; <code>false</code> otherwise.
	 */
	public boolean isInit() {
		return init;
	}
	
	/**
	 * Indicates if this rule is active.
	 * 
	 * @return <code>true</code> if this rule is active; <code>false</code> otherwise.
	 */
	public boolean isActive() {
		return active;
	}
	
	/**
	 * Deactivates this rule.  Only rules that are not initial rules can be deactivated.
	 */
	public void deactivate() {
		if (!init)
			active = false;
	}
	
	/**
	 * Indicates if this rule requires any arguments.
	 * 
	 * @return <code>true</code> if this rule requires some arguments; <code>false</code> otherwise.
	 */
	public boolean hasArgs() {
		return prod.hasArgs();
	}
	
	/**
	 * Returns the number of arguments that this rule requires.
	 * 
	 * @return the number of arguments that this rule requires.
	 */
	public short countArgs() {
		return prod.countArgs();
	}
	
	/**
	 * Returns the number of non-zero word gaps in this rule.
	 * 
	 * @return the number of non-zero word gaps in this rule.
	 */
	public short countGaps() {
		return ngaps;
	}
	
	/**
	 * Returns the weight of this rule in a log-linear model.
	 * 
	 * @return the weight of this rule.
	 */
	public double getWeight() {
		return weight;
	}
	
	/**
	 * Assigns a weight to this rule in a log-linear model.
	 * 
	 * @param weight the weight to assign.
	 */
	public void setWeight(double weight) {
		this.weight = weight;
	}
	
	/**
	 * Indicates if this rule is a unary rule (i.e. RHS consists of a single nonterminal).
	 * 
	 * @return <code>true</code> if this rule is unary; <code>false</code> otherwise.
	 */
	public boolean isUnary() {
		return E.length == 1 && E[0] instanceof Nonterminal && prod.isUnary();
	}
	
	/**
	 * Indicates if the RHS of this rule is only a wildcard terminal.
	 * 
	 * @return <code>true</code> if the RHS of this rule is only a wildcard terminal; <code>false</code> 
	 * otherwise.
	 */
	public boolean isWildcard() {
		return E.length == 1 && E[0] instanceof Terminal && ((Terminal) E[0]).isWildcard()
		&& prod.isWildcard();
	}
	
	/**
	 * Indicates if this rule is a dummy.
	 * 
	 * @return <code>true</code> if this rule is a dummy; <code>false</code> otherwise.
	 */
	public boolean isDummy() {
		return lhs < 0;
	}
	
	/**
	 * Returns the path from the root of the MR parse tree on the RHS to the specified nonterminal on 
	 * the frontier. 
	 * 
	 * @param index a positive index.
	 * @return the path from the root of the MR parse tree on the RHS to the nonterminal with the
	 * specified index.
	 */
	public short[] getPath(short index) {
		return prod.getPath((short) (index-1));
	}

	///
	/// Parameter estimation
	///
	
	public double getOuterScore() {
		return outer;
	}
	
	public void addOuterScore(double z) {
		outer = Math.logAdd(outer, z);
	}
	
	public void resetOuterScore() {
		outer = Double.NEGATIVE_INFINITY;
	}
	
	///
	/// Textual representations
	///
	
	/**
	 * Returns the textual representation of this rule.
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(new Nonterminal(lhs));
		sb.append(" -> ({");
		for (short i = 0; i < E.length; ++i) {
			sb.append(' ');
			sb.append(E[i]);
			if (gaps[i] > 0) {
				sb.append(" *g:");
				sb.append(gaps[i]);
			}
		}
		sb.append(" })({");
		for (short i = 0; i < F.length; ++i) {
			sb.append(' ');
			sb.append(F[i]);
		}
		sb.append(" })");
		return sb.toString();
	}
	
	/** Indicates if all rules subsequently read by the <code>read</code> method are initial rules. */
	public static boolean readInit = false;

	/**
	 * Returns an SCFG rule that part of the given line of text represents.  Beginning with the token 
	 * at the specified index, this method finds the shortest substring of the line that is the textual 
	 * representation of an SCFG rule.  If such a substring exists, then the rule that it represents is 
	 * returned, and the <code>index</code> argument is set to the token index immediately after the end 
	 * of the substring.  Otherwise, <code>null</code> is returned and the <code>index</code> argument 
	 * remains unchanged.
	 * 
	 * @param line a line of text containing the textual representation of an SCFG rule.
	 * @param index the beginning token index to consider; it is also an <i>output</i> variable for 
	 * the token index immediately after the end of the consumed substring.
	 * @return the SCFG rule that the substring <code>line[index, i)</code> represents, for the 
	 * smallest <code>i</code> possible; <code>null</code> if no such <code>i</code> exists.
	 */
	public static Rule read(String[] line, Int index) {
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
		Terminal.readWords = true;
		ArrayList list1 = new ArrayList();
		ArrayList list2 = new ArrayList();
		for (++i; i < line.length && !line[i].equals("})({"); ++i)
			if (line[i].startsWith("*g:")) {
				list2.remove(list2.size()-1);
				list2.add(new Short(Short.parseShort(line[i].substring(3))));
			} else {
				Symbol sym = Symbol.read(line[i]);
				if (sym == null)
					return null;
				list1.add(sym);
				list2.add(new Short(0));
			}
		Terminal.readWords = false;
		ArrayList list3 = new ArrayList();
		for (++i; i < line.length && !line[i].equals("})"); ++i) {
			Symbol sym = Symbol.read(line[i]);
			if (sym == null)
				return null;
			list3.add(sym);
		}
		if (i >= line.length)
			return null;
		Symbol[] E = (Symbol[]) list1.toArray(new Symbol[0]);
		short[] gaps = Arrays.toShortArray(list2);
		Symbol[] F = (Symbol[]) list3.toArray(new Symbol[0]);
		index.val = i+1;
		return new Rule(lhs.getId(), E, gaps, F, readInit);
	}
	
}
