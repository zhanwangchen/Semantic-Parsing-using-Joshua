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

import wasp.data.Terminal;
import wasp.math.Math;
import wasp.scfg.Rule;
import wasp.util.Arrays;
import wasp.util.BitSet;
import wasp.util.Double;

/**
 * Chart items used in the Earley parser for synchronous context-free grammars.
 * 
 * @author ywwong
 *
 */
public class Item {

	private static final int INC = 8;
	
	public Rule rule;
	public short dot;
	/** Number of words that have been skipped due to the word gap on the left of the dot. */
	public short gap;
	public short start;
	public short current;
	/** During training, this bit vector shows the partial MR parse tree that has been generated so
	 * far.  The <i>i</i>-th bit of this vector is set if the partial parse corresponds to the 
	 * <i>i</i>-th node of the gold-standard MR parse tree.  If the partial parse is not part of the 
	 * gold standard, then this bit vector is empty. */
	public BitSet m;
	/** The inner score. */
	public double inner;
	/** The outer score. */
	public double outer;
	public int timestamp;
	private int nback;
	private Item[] back;
	private Item[] backComp;
	
	/**
	 * Creates an item for the prediction step.
	 * 
	 * @param rule an SCFG rule.
	 * @param start the start position.
	 */
	public Item(Rule rule, short start) {
		this.rule = rule;
		dot = 0;
		gap = 0;
		this.start = start;
		current = start;
		m = null;
		inner = Double.NEGATIVE_INFINITY;
		outer = Double.NEGATIVE_INFINITY;
		timestamp = 0;
		nback = 0;
		back = null;
		backComp = null;
	}

	/**
	 * Creates an item for the scanning step.
	 * 
	 * @param back the back-pointer item.
	 * @param word the terminal symbol that has been scanned.
	 */
	public Item(Item back, Terminal word) {
		if (back.rule.isWildcard())
			rule = new Rule(back.rule, word);
		else
			rule = back.rule;
		dot = (short) (back.dot+1);
		gap = 0;
		start = back.start;
		current = (short) (back.current+1);
		m = null;
		inner = Double.NEGATIVE_INFINITY;
		outer = Double.NEGATIVE_INFINITY;
		timestamp = 0;
		nback = 1;
		this.back = new Item[1];
		this.back[0] = back;
		backComp = null;
	}
	
	/**
	 * Creates an item for the completion step.
	 * 
	 * @param back the item to be completed.
	 * @param comp a complete item.
	 */
	public Item(Item back, Item comp) {
		rule = back.rule;
		dot = (short) (back.dot+1);
		gap = 0;
		start = back.start;
		current = comp.current;
		m = null;
		inner = Double.NEGATIVE_INFINITY;
		outer = Double.NEGATIVE_INFINITY;
		timestamp = 0;
		nback = 1;
		this.back = new Item[1];
		this.back[0] = back;
		this.backComp = new Item[1];
		this.backComp[0] = comp;
	}
	
	/**
	 * Creates an item for word skipping.
	 * 
	 * @param back the back-pointer item.
	 */
	public Item(Item back) {
		rule = back.rule;
		dot = back.dot;
		gap = (short) (back.gap+1);
		start = back.start;
		current = (short) (back.current+1);
		m = null;
		inner = Double.NEGATIVE_INFINITY;
		outer = Double.NEGATIVE_INFINITY;
		timestamp = 0;
		nback = 1;
		this.back = new Item[1];
		this.back[0] = back;
		backComp = null;
	}
	
	public boolean equals(Object o) {
		if (o instanceof Item) {
			Item i = (Item) o;
			return rule.equals(i.rule) && dot == i.dot && gap == i.gap && start == i.start 
			&& current == i.current && ((m==null) ? (i.m==null) : m.equals(i.m));
		}
		return false;
	}

	public int hashCode() {
		int hash = 1;
		hash = 31*hash + rule.hashCode();
		hash = 31*hash + dot;
		hash = 31*hash + gap;
		hash = 31*hash + start;
		hash = 31*hash + current;
		hash = 31*hash + ((m==null) ? 0 : m.hashCode());
		return hash;
	}
	
	/**
	 * Returns the number of back pointers from this item.
	 * 
	 * @return the number of back pointers from this item.
	 */
	public int countBack() {
		return nback;
	}
	
	/**
	 * Returns the back pointer from this item with the specified index.  If there is no such pointer,
	 * then <code>null</code> is returned.
	 * 
	 * @param i a non-negative index.
	 * @return the <i>i</i>-th back pointer from this item.
	 */
	public Item getBack(int i) {
		return (i<nback) ? back[i] : null;
	}
	
	/**
	 * Returns the complete item associated with the back pointer from this item with the specified 
	 * index.  If there is no such item, then <code>null</code> is returned.
	 * 
	 * @param i a non-negative index.
	 * @return the complete item associated with the <i>i</i>-th back pointer from this item.
	 */
	public Item getBackComplete(int i) {
		return (i<nback && backComp!=null) ? backComp[i] : null;
	}
	
	/**
	 * Combines the content of this item with that of the specified item.  The specified item must be
	 * <i>equal</i> to this item.  For efficiency reasons, this method does not check for equality of 
	 * items.  But keep in mind that combining items that are not equal will lead to <b>disastrous</b> 
	 * effects!
	 * 
	 * @param item an item that is equal to this item.
	 */
	public void combine(Item item) {
		inner = Math.logAdd(inner, item.inner);
		if (nback+item.nback > back.length) {
			back = (Item[]) Arrays.resize(back, ((nback+item.nback-1)/INC+1)*INC);
			if (backComp != null)
				backComp = (Item[]) Arrays.resize(backComp, ((nback+item.nback-1)/INC+1)*INC);
		}
		for (int i = 0; i < item.nback; ++i) {
			back[nback+i] = item.back[i];
			if (backComp != null)
				backComp[nback+i] = item.backComp[i];
		}
		nback += item.nback;
	}
	
	/**
	 * Replaces the content of this item with that of the specified item, such that this item will
	 * remain "the same" in data structures like heaps and hash maps.  The specified item must be
	 * <i>equal</i> to this item.  For efficiency reasons, this method does not check for equality of
	 * items.  But keep in mind that using items that are not equal will lead to <b>disastrous</b> 
	 * effects!
	 *   
	 * @param item an item that is equal to this item.
	 */
	public void replace(Item item) {
		inner = item.inner;
		nback = item.nback;
		back = item.back;
		backComp = item.backComp;
	}
	
	public boolean isPredict() {
		return nback == 0;
	}
	
	public boolean isScan() {
		return nback > 0 && backComp == null;
	}

	public boolean isCompleted() {
		return nback > 0 && backComp != null;
	}
	
}
