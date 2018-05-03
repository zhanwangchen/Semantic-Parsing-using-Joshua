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
package wasp.scfg.parse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.NoSuchElementException;

import wasp.data.Meaning;
import wasp.data.Node;
import wasp.data.Nonterminal;
import wasp.data.Symbol;
import wasp.data.Terminal;
import wasp.main.Config;
import wasp.main.Parser;
import wasp.math.Math;
import wasp.mrl.Production;
import wasp.mrl.ProductionSymbol;
import wasp.nl.GapModel;
import wasp.scfg.SCFG;
import wasp.scfg.Rule;
import wasp.scfg.SCFGModel;
import wasp.util.Arrays;
import wasp.util.BitSet;
import wasp.util.Heap;
import wasp.util.SortIterator;

/**
 * An Earley chart parser for synchronous context-free grammars.  The parser takes NL sentences as 
 * input and generates parse trees for both NL and MRL.  It is based on previous work by Stolcke (1995).
 * 
 * @author ywwong
 *
 */
public class SCFGParser extends Parser {

	private SCFG gram;
	private GapModel gm;
	private int kbest;
	private boolean ignoreEmpty;
	
	/** The chart currently in use.  Each call to the <code>parse</code> method creates a new chart
	 * based on the input sentence.  This chart is re-used by the outside algorithm during parameter
	 * estimation. */
	private Chart c;
	/** The input sentence currently being considered. */
	private Terminal[] E;
	private BitSet mEmpty;
	private BitSet mWhole;
	private HashMap mWilds;
	private HashMap mProds;
	
	/**
	 * Creates a parser based on the specified SCFG for parameter estimation.
	 * 
	 * @param gram an SCFG.
	 * @param gm a word-gap model.
	 */
	public SCFGParser(SCFG gram, GapModel gm) {
		this.gram = gram;
		this.gm = gm;
		kbest = 0;
		ignoreEmpty = false;
		c = null;
		E = null;
	}
	
	/**
	 * Creates a parser based on the specified SCFG for Viterbi approximation.
	 * 
	 * @param gram an SCFG.
	 * @param gm a word-gap model.
	 * @param kbest the maximum number of top-scoring theories to keep for each cell.
	 */
	public SCFGParser(SCFG gram, GapModel gm, int kbest) {
		this.gram = gram;
		this.gm = gm;
		this.kbest = kbest;
		ignoreEmpty = true;
		c = null;
		E = null;
	}
	
	/**
	 * Creates an SCFG parser based on the specified SCFG translation model.
	 */
	public SCFGParser(SCFGModel model) {
		gram = model.gram;
		gm = model.gm;
		kbest = Config.getKBest();
		ignoreEmpty = false;  // item.m is always null to begin with
		c = null;
		E = null;
	}
	
	public Iterator parse(Terminal[] E, Meaning F) {
		this.E = E;
		if (F != null)  // training
			initm(F);
		c = new Chart(gram, this.E, kbest, ignoreEmpty);
		Item item = new Item(new Rule(gram.getStart()), (short) 0);
		item.inner = 0;
		if (F != null)  // training
			item.m = mWhole;
		c.addItem(item);
		for (short i = 0; i <= c.maxPos; ++i) {
			if (i > 0)
				complete(this.E, F, c, i);
			if (i < c.maxPos)
				predictAndScan(this.E, F, c, i);
		}
		Iterator parseIt = new ParseIterator(gram, c);
		return (kbest==0) ? parseIt : new SortIterator(parseIt, kbest);
	}
	
	public Iterator parse(Terminal[] E) {
		return parse(E, null);
	}
	
	private void initm(Meaning F) {
		short size = (short) F.linear.length;
		mEmpty = new BitSet(size);
		mWhole = new BitSet(size);
		mWhole.set((short) 0, true);
		mWilds = new HashMap();
		Symbol[] wilds = new Symbol[size];
		for (short i = 0; i < size; ++i)
			if (F.lprods[i].tied().isWildcard())
				wilds[i] = F.lprods[i].getRhs((short) 0);
		boolean[] checked = new boolean[size];
		for (short i = 0; i < size; ++i)
			if (!checked[i] && wilds[i] != null) {
				checked[i] = true;
				BitSet m = new BitSet(size);
				m.set(i, true);
				for (short j = (short) (i+1); j < size; ++j)
					if (wilds[j] != null && wilds[j].equals(wilds[i])) {
						checked[j] = true;
						m.set(j, true);
					}
				mWilds.put(wilds[i], m);
			}
		mProds = new HashMap();
		Production[] prods = Config.getMRLGrammar().getProductions();
		for (int i = 0; i < prods.length; ++i) {
			Node parse = prods[i].getParse();
			BitSet m = new BitSet(size);
			for (short j = 0; j < size; ++j)
				if (match(parse, F.linear[j]))
					m.set(j, true);
			if (!m.isEmpty())
				mProds.put(prods[i], m);
		}
	}
	
	private boolean match(Node parse, Node Fparse) {
		Production prod = ((ProductionSymbol) parse.getSymbol()).getProduction();
		Production Fprod = ((ProductionSymbol) Fparse.getSymbol()).getProduction();
		if (!prod.equals(Fprod) && !prod.equals(Fprod.tied()))
			return false;
		short nc = parse.countChildren();
		for (short i = 0; i < nc; ++i) {
			Node child = parse.getChild(i);
			if (child.getSymbol() instanceof ProductionSymbol && !match(child, Fparse.getChild(i)))
				return false;
		}
		return true;
	}
	
	private void complete(Terminal[] E, Meaning F, Chart c, short current) {
		while (!c.comps[current].isEmpty()) {
			Item comp = (Item) c.comps[current].extractMin();
			if (comp.rule.isDummy())
				continue;
			ArrayList items = c.toComps[comp.start][comp.rule.getLhs()];
			if (items == null)
				continue;
			for (Iterator it = items.iterator(); it.hasNext();) {
				Item item = (Item) it.next();
				Item next = new Item(item, comp);
				if (F != null)  // training
					next.m = m(F, item, comp);
				next.inner = item.inner+comp.inner;
				c.addItem(next);
				skipWords(E, c, next);
			}
		}
	}
	
	private BitSet m(Meaning F, Item item, Item comp) {
		if (item.rule.isDummy())
			return item.m.intersect(comp.m);
		else {
			short[] path = item.rule.getPath(item.rule.getE(item.dot).getIndex());
			BitSet m = (BitSet) item.m.copy();
			for (short i = 0; i < m.length(); ++i)
				if (m.get(i)) {
					Node n = F.linear[i];
					for (short j = 0; j < path.length; ++j)
						n = n.getChild(path[j]);
					if (!comp.m.get((short) Arrays.indexOf(F.linear, n)))
						m.set(i, false);
				}
			return (m.isEmpty()) ? mEmpty : m;
		}
	}
	
	private void skipWords(Terminal[] E, Chart c, Item item) {
		short gap = item.rule.getGap((short) (item.dot-1));
		for (short i = 0; i < gap && item.current < E.length; ++i) {
			Item next = new Item(item);
			next.m = item.m;
			next.inner = item.inner+gm.getWeight(E[item.current]);
			c.addItem(next);
			item = next;
		}
	}
	
	private void predictAndScan(Terminal[] E, Meaning F, Chart c, short current) {
		ArrayList set = c.sets[current];
		for (int i = 0; i < set.size(); ++i) {
			Item item = (Item) set.get(i);
			if (item.dot == item.rule.lengthE())
				continue;
			Symbol sym = item.rule.getE(item.dot);
			if (sym instanceof Nonterminal) {
				// predict
				for (int j = 0; j < gram.countNonterms(); ++j)
					if (gram.isLeftCornerForE(sym.getId(), j) && !c.isPredicted(current, j)) {
						c.predict(current, j);
						Rule[] rules = gram.getRules(j);
						for (int k = 0; k < rules.length; ++k)
							if (rules[k].isActive()) {
								Item next = new Item(rules[k], current);
								if (F != null)  // training
									next.m = m(rules[k]);
								next.inner = rules[k].getWeight();
								c.addItem(next);
							}
					}
			} else {
				// scan
				if (sym.matches(E[current])) {
					Item next = new Item(item, E[current]);
					if (F != null) // training
						next.m = m(item, E[current]);
					next.inner = item.inner;
					c.addItem(next);
					skipWords(E, c, next);
				}
			}
		}
	}
	
	private BitSet m(Rule rule) {
		BitSet m = (BitSet) mProds.get(rule.getProduction());
		return (m==null) ? mEmpty : m;
	}
	
	private BitSet m(Item item, Terminal word) {
		if (item.rule.isWildcard()) {
			BitSet m = (BitSet) mWilds.get(word);
			return (m==null) ? mEmpty : m.intersect(item.m);
		} else
			return item.m;
	}
	
	private static class ParseIterator implements Iterator {
		private SCFG gram;
		private Iterator it;
		private Item next;
		public ParseIterator(SCFG gram, Chart c) {
			this.gram = gram;
			it = c.sets[c.maxPos].iterator();
			findNext();
		}
		private void findNext() {
			next = null;
			int start = gram.getStart();
			while (it.hasNext()) {
				Item item = (Item) it.next();
				if (item.start == 0 && item.dot == item.rule.lengthE() && item.rule.getLhs() == start) {
					next = item;
					break;
				}
			}
		}
		public boolean hasNext() {
			return next != null;
		}
		public Object next() {
			if (this.next == null)
				throw new NoSuchElementException();
			Item next = this.next;
			findNext();
			return new SCFGParse(next, next.inner);
		}
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
	
	///
	/// Parameter estimation
	///
	
	/**
	 * The outside algorithm for calcaluting the outer scores of chart items.  The previous call to the
	 * <code>parse</code> method supplies the chart and input sentence required by this algorithm.  If
	 * no such call has been made, then a <code>NullPointerException</code> is thrown.
	 * 
	 * @param ignoreEmpty indicates if items with empty <code>m</code> field are ignored during the
	 * outside algorithm.
	 * @throws NullPointerException if the <code>parse</code> method has not been called.
	 */
	public void outside(boolean ignoreEmpty) {
		gram.resetOuterScores();
		gm.resetOuterScores();
		c.resetOuterScores();
		initOuterScores(c, ignoreEmpty);
		for (short i = c.maxPos; i > 0; --i) {
			reverseComplete(E, c, i);
			reverseScan(E, c, i);
		}
		for (short i = 0; i < c.maxPos; ++i)
			addOuterScores(c, i);
	}
	
	private void initOuterScores(Chart c, boolean ignoreEmpty) {
		for (Iterator it = new ParseIterator(gram, c); it.hasNext();) {
			SCFGParse parse = (SCFGParse) it.next();
			if (ignoreEmpty && parse.item.m.isEmpty())
				continue;
			parse.item.outer = 0;
		}
	}
	
	private void reverseComplete(Terminal[] E, Chart c, short current) {
		Heap heap = c.createReverseHeap();
		for (Iterator it = c.sets[current].iterator(); it.hasNext();) {
			Item item = (Item) it.next();
			if (item.dot == 0)
				continue;
			if (item.isCompleted())
				heap.add(item);
		}
		while (!heap.isEmpty()) {
			Item item = (Item) heap.extractMin();
			int nback = item.countBack();
			for (int i = 0; i < nback; ++i) {
				Item back = item.getBack(i);
				Item comp = item.getBackComplete(i);
				back.outer = Math.logAdd(back.outer, item.outer+comp.inner);
				comp.outer = Math.logAdd(comp.outer, item.outer+back.inner);
			}
		}
	}
	
	private void reverseScan(Terminal[] E, Chart c, short current) {
		for (Iterator it = c.sets[current].iterator(); it.hasNext();) {
			Item item = (Item) it.next();
			if (item.isScan()) {
				int nback = item.countBack();
				for (int j = 0; j < nback; ++j) {
					Item back = item.getBack(j);
					if (back.dot == item.dot) {
						// word gap
						double w = gm.getWeight(E[back.current]);
						back.outer = Math.logAdd(back.outer, item.outer+w);
						gm.addOuterScores(E[back.current], item.outer+back.inner+w);
					} else
						back.outer = Math.logAdd(back.outer, item.outer);
				}
			}
		}
	}
	
	private void addOuterScores(Chart c, short current) {
		for (Iterator it = c.sets[current].iterator(); it.hasNext();) {
			Item item = (Item) it.next();
			if (item.isPredict() && !item.rule.isDummy()) {
				gram.tied(item.rule).addOuterScore(item.outer);
			}
		}
	}
	
}
