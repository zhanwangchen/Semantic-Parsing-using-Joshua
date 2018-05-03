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

import java.util.ArrayList;
import java.util.TreeMap;

import wasp.align.NTo1WordAlign;
import wasp.main.Parse;
import wasp.util.Arrays;

/**
 * Examples for training and testing.
 * 
 * @author ywwong
 *
 */
public class Example {

	/** The example ID. */
	public int id;
	/** The original sentences in various languages. */
	public TreeMap nlMap;
	/** Syntactically annotated sentences in various languages. */
	public TreeMap synMap;
	/** Syntactically and semantically annotated sentences in various languages. */
	public TreeMap augsynMap;
	/** The correct meaning representation in various MRLs. */
	public TreeMap mrlMap;
	
	/** The NL sentence in the default language, broken into tokens. */
	public Terminal[] E;
	/** The correct meaning representation. */
	public Meaning F;
	/** The correct linearized MR parse as specified by the <code>mrl-parse</code> section in an XML
	 * file. */
	public ArrayList Fparse;
	/** The gold-standard word alignment between the sentence and the correct linearized MR parse. */
	public NTo1WordAlign EFalign;

	/** Word alignment between the sentence and the linearized MR parse, sorted in descending order of 
	 * alignment scores. */
	public ArrayList aligns;
	/** Automatically-generated parses, sorted in descending order of parse scores. */
	public ArrayList parses;
	
	public Example() {
		nlMap = new TreeMap();
		synMap = new TreeMap();
		augsynMap = new TreeMap();
		mrlMap = new TreeMap();
		E = null;
		F = null;
		Fparse = new ArrayList();
		EFalign = null;
		aligns = new ArrayList();
		parses = new ArrayList();
	}
	
	public NTo1WordAlign[] getSortedAligns() {
		NTo1WordAlign[] array = (NTo1WordAlign[]) aligns.toArray(new NTo1WordAlign[0]);
		Arrays.sort(array);
		return array;
	}
	
	public Parse[] getSortedParses() {
		Parse[] array = (Parse[]) parses.toArray(new Parse[0]);
		Arrays.sort(array);
		return array;
	}
	
}
