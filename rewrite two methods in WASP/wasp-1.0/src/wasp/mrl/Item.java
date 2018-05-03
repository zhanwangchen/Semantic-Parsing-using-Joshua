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

import wasp.data.Symbol;
import wasp.data.Terminal;

/**
 * Chart items used in the Earley parser for MRLs.
 * 
 * @author ywwong
 *
 */
public class Item {

	public Production prod;
	public short dot;
	public short start;
	public short current;
	public Item back;
	public Item backComp;
	public int timestamp;
	
	/**
	 * Creates an item for the prediction step.
	 * 
	 * @param prod an MRL production.
	 * @param start the start position.
	 */
	public Item(Production prod, short start) {
		this.prod = prod;
		dot = 0;
		this.start = start;
		current = start;
		back = null;
		backComp = null;
		timestamp = 0;
	}

	/**
	 * Creates an item for the scanning step.
	 * 
	 * @param back the item at the back pointer.
	 * @param sym the symbol that has been scanned.
	 */
	public Item(Item back, Symbol sym) {
		if (back.prod.isWildcard())
			prod = new Production(back.prod, (Terminal) sym);
		else
			prod = back.prod;
		dot = (short) (back.dot+1);
		start = back.start;
		current = (short) (back.current+1);
		this.back = back;
		backComp = null;
		timestamp = 0;
	}
	
	/**
	 * Creates an item for the completion step.
	 * 
	 * @param back the item to be completed.
	 * @param comp a complete item.
	 */
	public Item(Item back, Item comp) {
		prod = back.prod;
		dot = (short) (back.dot+1);
		start = back.start;
		current = comp.current;
		this.back = back;
		this.backComp = comp;
		timestamp = 0;
	}
	
	public boolean equals(Object o) {
		if (o instanceof Item) {
			Item i = (Item) o;
			return prod.equals(i.prod) && dot == i.dot && start == i.start && current == i.current;
		}
		return false;
	}

	public int hashCode() {
		int hash = 1;
		hash = 31*hash + prod.hashCode();
		hash = 31*hash + dot;
		hash = 31*hash + start;
		hash = 31*hash + current;
		return hash;
	}
	
}
