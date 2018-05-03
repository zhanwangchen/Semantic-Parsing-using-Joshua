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

import java.util.Iterator;
import java.util.logging.Logger;

import wasp.align.Link;
import wasp.align.NTo1WordAlign;
import wasp.data.Anaphor;
import wasp.data.Example;
import wasp.data.Examples;
import wasp.data.Node;
import wasp.data.Nonterminal;
import wasp.data.Symbol;
import wasp.main.Config;
import wasp.mrl.Production;
import wasp.mrl.ProductionSymbol;
import wasp.util.Arrays;
import wasp.util.Matrices;

/**
 * Extracts SCFG rules from word alignments between training sentences and their corresponding linearized 
 * MR parses.
 * 
 * @author ywwong
 *
 */
public class RuleExtractor {

	private static Logger logger = Logger.getLogger(RuleExtractor.class.getName());
	
	/** The minimum number of words that an extracted pattern must have if the corresponding production
	 * has a single argument and is not unary, and if the word alignment from which the pattern is 
	 * extracted has a score lower than 1. */
	private static final short MIN_WORD_COUNT = 1;
	
	// for detecting infinite loops
	private boolean[][] dep;
	
	/**
	 * Extracts rules from the specified examples.  The extracted rules are added to the specified
	 * grammar.  Word alignments must be made available in the <code>aligns</code> field of the examples.  
	 * These word alignments can be obtained using a <code>WordAlignModel</code>.
	 *
	 * @param gram an SCFG.
	 * @param examples a set of training examples that have been through a word alignment model.
	 * @throws RuntimeException if rule extraction fails.
	 */
	public void extract(SCFG gram, Examples examples) {
		logger.info("Extracting SCFG rules from word alignments");
		initDep(gram);
		for (Iterator it = examples.iterator(); it.hasNext();) {
			Example ex = (Example) it.next();
			logger.fine("example "+ex.id);
			NTo1WordAlign[] aligns = ex.getSortedAligns();
			for (int j = 0; j < aligns.length; ++j) {
				// add links to sentence boundaries
				short top = 0;
				while (ex.F.lprods[top].isUnary())
					++top;
				aligns[j].addLink((short) 0, top);
				aligns[j].addLink((short) (ex.E.length-1), top);
				extract(gram, aligns[j]);
			}
		}
		logger.info("SCFG rules have been extracted");
	}

	private void initDep(SCFG gram) {
		int nlhs = gram.countNonterms();
		dep = new boolean[nlhs][nlhs];
		Rule[] rules = gram.getRules();
		for (int i = 0; i < rules.length; ++i)
			if (rules[i].lengthE() == 1) {
				Symbol sym = rules[i].getE((short) 0);
				if (sym instanceof Nonterminal)
					dep[rules[i].getLhs()][sym.getId()] = true;
			}
		// sanity check
		if (loopy()) {
			logger.severe("The initial rules would cause infinite loops during parsing");
			throw new RuntimeException();
		}
	}
	
	private boolean loopy() {
		boolean[][] depTrans = Matrices.transitive(dep);
		for (int i = 0; i < depTrans.length; ++i)
			if (depTrans[i][i])
				return true;
		return false;
	}
	
	private void extract(SCFG gram, NTo1WordAlign align) {
		logger.finer(align.toString());
		for (short i = (short) (align.lengthF()-1); i >= 0; --i) {
			Node node = align.getF(i);
			Production prod = ((ProductionSymbol) node.getSymbol()).getProduction();
			short nwords = align.countLinksFromF(i);
			short nargs = node.countChildren();
			if (nwords+nargs == 0) {
				logger.finest("merge "+node.getSymbol()+" with its parent");
				align = align.combineF(i);
				logger.finest(align.toString());
				continue;
			}
			if (align.getScore() < 1 && nwords < MIN_WORD_COUNT
					&& !prod.isUnary() && !Config.getMRLGrammar().isZeroFertility(prod)) {
				logger.finest("merge "+node.getSymbol()+" with its parent");
				align = align.combineF(i);
				logger.finest(align.toString());
				continue;
			}
			Symbol[] E = new Symbol[nwords+nargs];
			short[] gaps = new short[nwords+nargs];
			short from = align.lengthE();  // first symbol in the pattern
			if (align.isLinkedFromF(i))
				from = align.getFirstLinkFromF(i).e;
			for (short j = 0; j < from; ++j) {
				Link l = align.getFirstLinkFromE(j);
				if (l != null && align.getF(l.f).getParent() == node) {
					from = j;
					break;
				}
			}
			short to = from;  // last symbol in the pattern
			for (short k = 0; k < nwords+nargs; ++to) {
				Link l = align.getFirstLinkFromE(to);
				if (l == null) {
					++gaps[k-1];
					continue;
				}
				if (l.f == i) {
					E[k++] = (Symbol) align.getE(to).copy();
					continue;
				}
				short index = node.indexOf(align.getF(l.f));
				if (index >= 0) {
					E[k] = (Symbol) align.getE(to).copy();
					E[k].setIndex((short) (index+1));
					++k;
					continue;
				}
				E = null;
				break;
			}
			if (E == null) {
				logger.finest("merge "+node.getSymbol()+" with its parent");
				align = align.combineF(i);
				logger.finest(align.toString());
				continue;
			}
			if (nwords == 0 && nargs == 1) {
				int lhs = prod.getLhs();
				int rhs = prod.getArgs()[0];
				if (!dep[lhs][rhs]) {
					dep[lhs][rhs] = true;
					if (loopy()) {
						dep[lhs][rhs] = false;
						logger.finest("merge "+node.getSymbol()+" with its parent");
						align = align.combineF(i);
						logger.finest(align.toString());
						continue;
					}
				}
			}
			Rule rule = new Rule(prod, E, gaps, false);
			if (prod.isAnaphor() || !Arrays.contains(prod.getRhs(), Anaphor.class)) {
				if (!gram.containsRule(gram.tied(rule))) {
					Config.getMRLGrammar().addProduction(prod);
					gram.addRule(rule);
					logger.fine("add "+rule.toString());
				}
			}
			logger.finest(rule.toString());
			align = align.replaceE(from, to, new Nonterminal(rule.getLhs()));
			align.addLink(from, i);
			logger.finest(align.toString());
		}
	}
	
}
