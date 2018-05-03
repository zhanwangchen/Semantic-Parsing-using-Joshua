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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

import wasp.data.Nonterminal;
import wasp.data.Symbol;
import wasp.data.Terminal;
import wasp.main.Config;
import wasp.mrl.Production;
import wasp.util.Double;
import wasp.util.FileWriter;
import wasp.util.Int;
import wasp.util.Matrices;
import wasp.util.Numberer;
import wasp.util.TokenReader;

/**
 * Synchronous context-free grammars.
 * 
 * @author ywwong
 *
 */
public class SCFG {

	private ArrayList[] byLhs;
	private Rule[][] _byLhs;
	private Numberer numberer;
	private HashMap ties;
	private int ninit;
	
	private boolean[][] Elc;
	private boolean[][] _ElcTrans;
	private boolean[][] Flc;
	private boolean[][] _FlcTrans;
	
	public SCFG() {
		int nlhs = countNonterms();
		byLhs = new ArrayList[nlhs];
		_byLhs = new Rule[nlhs][];
		for (int i = 0; i < byLhs.length; ++i)
			byLhs[i] = new ArrayList();
		numberer = new Numberer();
		ties = new HashMap();
		ninit = 0;

		Elc = new boolean[nlhs][nlhs];
		_ElcTrans = null;
		Flc = new boolean[nlhs][nlhs];
		_FlcTrans = null;
	}

	/**
	 * Returns the start symbol of this grammar.
	 * 
	 * @return the start symbol of this grammar.
	 */
	public int getStart() {
		return Config.getMRLGrammar().getStart();
	}
	
	/**
	 * Returns the number of nonterminal symbols in this grammar.
	 * 
	 * @return the number of nonterminal symbols in this grammar.
	 */
	public int countNonterms() {
		return Config.getMRLGrammar().countNonterms();
	}
	
	/**
	 * Returns an array containing all rules in this grammar with the specified LHS nonterminal.  
	 * An empty array is returned if there are no such rules in this grammar.
	 * 
	 * @param lhs the ID of an LHS nonterminal.
	 * @return an array containing all rules in this grammar with the specified LHS nonterminal.
	 */
	public Rule[] getRules(int lhs) {
		if (_byLhs[lhs] == null)
			_byLhs[lhs] = (Rule[]) byLhs[lhs].toArray(new Rule[0]);
		return _byLhs[lhs];
	}
	
	/**
	 * Returns an array containing all rules in this grammar.  An empty array is returned if there
	 * are no rules in this grammar.
	 * 
	 * @return an array containing all rules in this grammar.
	 */
	public Rule[] getRules() {
		int nr = numberer.getNextId();
		Rule[] a = new Rule[nr];
		for (int i = 0; i < nr; ++i)
			a[i] = (Rule) numberer.getObj(i);
		return a;
	}
	
	/**
	 * Returns the number of rules in this grammar.
	 * 
	 * @return the number of rules in this grammar.
	 */
	public int countRules() {
		return numberer.getNextId();
	}

	/**
	 * Returns the number of initial rules in this grammar.
	 * 
	 * @return the number of initial rules in this grammar.
	 */
	public int countInitRules() {
		return ninit;
	}
	
	/**
	 * Returns the ID of the specified rule.  <code>-1</code> is returned if there is no such rule in
	 * this grammar.
	 *  
	 * @param rule the rule to look for.
	 * @return the ID of the <code>rule</code> argument.
	 */
	public int getId(Rule rule) {
		return numberer.getId(rule, false);
	}
	
	/**
	 * Returns the rule with the specified ID.  <code>null</code> is returned if there is no such rule
	 * in this grammar.
	 * 
	 * @param id a rule ID.
	 * @return the rule with the specified ID.
	 */
	public Rule getRule(int id) {
		return (Rule) numberer.getObj(id);
	}
	
	/**
	 * Indicates if the specified rule is in this grammar.
	 * 
	 * @param rule the rule to look for.
	 * @return <code>true</code> if the <code>rule</code> argument is in this grammar; <code>false</code>
	 * otherwise.
	 */
	public boolean containsRule(Rule rule) {
		return numberer.getId(rule, false) >= 0;
	}
	
	/**
	 * Returns an interned copy of the specified rule.  If none exists, then the given rule is returned
	 * instead.
	 * 
	 * @param rule the rule to look for.
	 * @return an interned copy of the <code>rule</code> argument.
	 */
	public Rule intern(Rule rule) {
		int id = getId(rule);
		return (id < 0) ? rule : getRule(id);
	}

	/**
	 * Returns an interned copy of the rule that the specified rule is tied to (e.g. <code>State -&gt; 
	 * Virginia, stateid('virginia')</code> could be tied to <code>State -&gt; Texas, 
	 * stateid('texas')</code>).
	 * 
	 * @param rule a rule.
	 * @return an interned copy of the rule that the <code>rule</code> argument is tied to.
	 */
	public Rule tied(Rule rule) {
		Int tid;
		if ((tid = (Int) ties.get(rule)) != null)
			return getRule(tid.val);
		// wildcard rules
		if (rule.lengthE() == 1 && rule.lengthF() == 1) {
			Symbol e = rule.getE((short) 0);
			Symbol f = rule.getF((short) 0);
			if (e instanceof Terminal && e.equals(f)) {
				Terminal t = (Terminal) e;
				if (t.isNum()) {
					Rule r = createWildcardRule(rule.getLhs(), Terminal.wildcardNum());
					int rid = getId(r);
					if (rid >= 0)
						return getRule(rid);
				}
				if (t.isUnum()) {
					Rule r = createWildcardRule(rule.getLhs(), Terminal.wildcardUnum());
					int rid = getId(r);
					if (rid >= 0)
						return getRule(rid);
				}
				if (t.isIdent()) {
					Rule r = createWildcardRule(rule.getLhs(), Terminal.wildcardIdent());
					int rid = getId(r);
					if (rid >= 0)
						return getRule(rid);
				}
			}
		}
		return intern(rule);
	}

	private static Rule createWildcardRule(int lhs, Terminal wildcard) {
		Symbol[] E = new Symbol[1];
		E[0] = (Symbol) wildcard.copy();
		E[0].setIndex((short) 1);
		Symbol[] F = new Symbol[1];
		F[0] = (Symbol) wildcard.copy();
		F[0].setIndex((short) 1);
		return new Rule(lhs, E, new short[1], F, false);
	}

	/**
	 * Specifies the rule that the specified rule is tied to  (e.g. <code>State -&gt; Virginia, 
	 * stateid('virginia')</code> could be tied to <code>State -&gt; Texas, stateid('texas')</code>).
	 * 
	 * @param rule a rule.
	 * @param tied the rule that the <code>rule</code> argument is tied to.
	 */
	public void addTie(Rule rule, Rule tied) {
		addRule(rule);
		addRule(tied);
		ties.put(rule, new Int(getId(tied)));
	}
	
	/**
	 * Adds a new rule to this grammar.  This method returns <code>true</code> if the specified rule is
	 * successfully added to this grammar.  If the rule is already in the grammar, then 
	 * <code>false</code> is returned.
	 * 
	 * @param rule the rule to add.
	 * @return <code>true</code> if the specified rule is successfully added to this grammar; 
	 * <code>false</code> otherwise.
	 */
	public boolean addRule(Rule rule) {
		if (numberer.addObj(rule)) {
			byLhs[rule.getLhs()].add(rule);
			_byLhs[rule.getLhs()] = null;
			if (rule.isInit())
				++ninit;
			if (rule.getE((short) 0) instanceof Nonterminal) {
				Elc[rule.getLhs()][rule.getE((short) 0).getId()] = true;
				_ElcTrans = null;
			}
			if (rule.getF((short) 0) instanceof Nonterminal) {
				Flc[rule.getLhs()][rule.getF((short) 0).getId()] = true;
				_FlcTrans = null;
			}
			return true;
		}
		return false;
	}

	/**
	 * Indicates if the nonterminal <code>n2</code> is a <i>left corner</i> of the nonterminal
	 * <code>n1</code>, with respect to the NL grammar.  If a nonterminal is a left corner of another,
	 * then it is possible for the latter nonterminal to generate the former on the left fringe of some 
	 * parse tree.
	 * 
	 * @param n1 a nonterminal ID.
	 * @param n2 a nonterminal ID.
	 * @return <code>true</code> if the nonterminal <code>n2</code> is a left corner of the nonterminal
	 * <code>n1</code> with respect to the NL grammar; <code>false</code> otherwise.
	 */
	public boolean isLeftCornerForE(int n1, int n2) {
		if (_ElcTrans == null)
			_ElcTrans = Matrices.reflexiveTransitive(Elc);
		return _ElcTrans[n1][n2];
	}
	
	/**
	 * Indicates if the nonterminal <code>n2</code> is a <i>left corner</i> of the nonterminal
	 * <code>n1</code>, with respect to the MRL grammar.  If a nonterminal is a left corner of another, 
	 * then it is possible for the latter nonterminal to generate the former on the left fringe of some 
	 * parse tree.
	 * 
	 * @param n1 a nonterminal ID.
	 * @param n2 a nonterminal ID.
	 * @return <code>true</code> if the nonterminal <code>n2</code> is a left corner of the nonterminal
	 * <code>n1</code> with respect to the MRL grammar; <code>false</code> otherwise.
	 */
	public boolean isLeftCornerForF(int n1, int n2) {
		if (_FlcTrans == null)
			_FlcTrans = Matrices.reflexiveTransitive(Flc);
		return _FlcTrans[n1][n2];
	}
	
	///
	/// Parameter estimation
	///
	
	public void resetOuterScores() {
		int nr = numberer.getNextId();
		for (int i = 0; i < nr; ++i)
			((Rule) numberer.getObj(i)).resetOuterScore();
	}
	
	///
	/// File I/O
	///
	
	private static final String SCFG_RULES = "scfg-rules";
	
	/**
	 * Adds initial rules to this grammar.  Some of these initial rules are automatically created based on
	 * the MRL grammar (e.g. the unary rules).  Others are read from the file specified in the
	 * configuration file (via the key <code>Config.SCFG_INIT</code>).  If the specified file contains 
	 * something that is not a valid textual representation of a rule, then a 
	 * <code>RuntimeException</code> is thrown.  All initial rules have zero weights.
	 * 
	 * @throws IOException if an I/O error occurs.
	 * @throws RuntimeException if the file contains something that is not a valid textual representation
	 * of a rule.
	 */
	public void readInit() throws IOException {
		TokenReader in = new TokenReader(new BufferedReader(new FileReader(Config.get(Config.SCFG_INIT))));
		Rule.readInit = true;
		String[] line;
		while ((line = in.readLine()) != null) {
			Int index = new Int(0);
			Rule rule = Rule.read(line, index);
			if (index.val < line.length && line[index.val].equals("tied-to")) {
				++index.val;
				addTie(rule, Rule.read(line, index));
			}
			if (index.val < line.length)
				throw new RuntimeException();
			addRule(rule);
		}
		in.close();
		addDefaultInit();
	}
	
	private void addDefaultInit() {
		Production[] prods = Config.getMRLGrammar().getProductions();
		for (int i = 0; i < prods.length; ++i)
			if (prods[i].isUnary() || prods[i].isWildcard()) {
				Symbol[] E = new Symbol[1];
				E[0] = (Symbol) prods[i].getRhs((short) 0).copy();
				E[0].setIndex((short) 1);
				addRule(new Rule(prods[i], E, new short[1], true));
			}
	}

	/**
	 * Adds rules to this grammar.  Rules are read from a file called <code>scfg-rules</code> in the 
	 * directory specified in the configuration file (via the key <code>Config.MODEL_DIR</code>).  If 
	 * this file contains something that is not a valid textual representation of a rule, then a 
	 * <code>RuntimeException</code> is thrown.
	 * 
	 * @throws IOException if an I/O error occurs.
	 * @throws RuntimeException if the file contains something that is not a valid textual representation
	 * of a rule.
	 */
	public void read() throws IOException {
		File file = new File(Config.getModelDir(), SCFG_RULES);
		TokenReader in = new TokenReader(new BufferedReader(new FileReader(file)));
		Rule.readInit = false;
		String[] line;
		int lineNum = 1;
		while ((line = in.readLine()) != null) {
			Int index = new Int(0);
			Rule rule = Rule.read(line, index);
			/*
			if (index.val < line.length && line[index.val].equals("tied-to")) {
				++index.val;
				addTie(rule, Rule.read(line, index));
			}
			*/
			if (index.val < line.length && line[index.val].equals("weight")) {
				rule.setWeight(Double.parseDouble(line[index.val+1]));
				index.val += 2;
			}
			if (index.val < line.length)
				throw new RuntimeException();
			System.out.println(lineNum++);
			System.out.println(rule);
			addRule(rule);
			
		}
		in.close();
	}
	
	/**
	 * Writes all active rules in this grammar to a file called <code>scfg-rules</code> in the directory
	 * specified in the configuration file (via the key <code>Config.MODEL_DIR</code>). 
	 * 
	 * @throws IOException if an I/O error occurs.
	 */
	public void write() throws IOException {
		File file = new File(Config.getModelDir(), SCFG_RULES);
		PrintWriter out = new PrintWriter(new BufferedWriter(FileWriter.createNew(file)));
		Rule[] rules = getRules();
		for (int i = 0; i < rules.length; ++i)
			if (rules[i].isActive()) {
				out.print(rules[i]);
				/*
				Rule tied = tied(rules[i]);
				if (tied != rules[i]) {
					out.print(" tied-to ");
					out.print(tied);
				}
				*/
				out.print(" weight ");
				out.print(rules[i].getWeight());
				out.println();
			}
		out.close();
	}
	
}
