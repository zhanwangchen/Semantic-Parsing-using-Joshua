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
import java.util.Comparator;
//import java.util.logging.Logger;

import wasp.data.Nonterminal;
import wasp.data.Symbol;
import wasp.util.Bool;
import wasp.util.Heap;
import wasp.util.RadixMap;

/**
 * A simple Earley chart for parsing MRLs, which have unambiguous context-free grammars.
 * 
 * @author ywwong
 *
 */
public class Chart {

	//private static Logger logger = Logger.getLogger(Chart.class.getName());
	
	private static final Comparator LATER_FIRST = new Comparator() {
		public int compare(Object o1, Object o2) {
			Item i1 = (Item) o1;
			Item i2 = (Item) o2;
			if (i1.start > i2.start)
				return -1;
			else if (i1.start < i2.start)
				return 1;
			else if (i1.timestamp < i2.timestamp)
				return -1;
			else if (i1.timestamp > i2.timestamp)
				return 1;
			else
				return 0;
		}
	};
	private static final int INC = 64;
	
	public short maxPos;
	public ArrayList[] sets;
	public RadixMap[] toComps;
	public Heap[] comps;
	private RadixMap[] predicted;
	private int timestamp;
	
	public Chart(Symbol[] m) {
		maxPos = (short) m.length;
		sets = new ArrayList[maxPos+1];
		toComps = new RadixMap[maxPos+1];
		comps = new Heap[maxPos+1];
		predicted = new RadixMap[maxPos+1];
		for (short i = 0; i <= maxPos; ++i) {
			sets[i] = new ArrayList();
			toComps[i] = new RadixMap();
			comps[i] = new Heap(LATER_FIRST, INC);
			predicted[i] = new RadixMap();
		}
		timestamp = 0;
	}
	
	public void addItem(Item item) {
		//logger.finest(item.start+" "+item.current+" "+item.prod);
		item.timestamp = timestamp++;
		sets[item.current].add(item);
		if (item.dot == item.prod.length())
			// item is complete
			comps[item.current].add(item);
		else {
			Symbol sym = item.prod.getRhs(item.dot);
			if (sym instanceof Nonterminal && item.current < maxPos) {
				// item is to be completed
				int n = sym.getId();
				ArrayList list = (ArrayList) toComps[item.current].get(n);
				if (list == null) {
					list = new ArrayList();
					toComps[item.current].put(n, list);
				}
				list.add(item);
			}
		}
	}
	
	public boolean isPredicted(short start, int lhs) {
		return predicted[start].containsKey(lhs);
	}
	
	public void predict(short start, int lhs) {
		predicted[start].put(lhs, Bool.TRUE);
	}
	
}
