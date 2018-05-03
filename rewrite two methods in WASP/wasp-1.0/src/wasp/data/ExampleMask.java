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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

import wasp.util.Bool;
import wasp.util.Int;
import wasp.util.RadixMap;

/**
 * Masks for creating subsets of corpora for training and testing.
 * 
 * @author ywwong
 *
 */
public class ExampleMask {

	private RadixMap mask;
	
	public ExampleMask() {
		mask = new RadixMap();
	}
	
	public Examples apply(Examples examples) {
		Examples subset = new Examples();
		for (Iterator it = examples.iterator(); it.hasNext();) {
			Example ex = (Example) it.next();
			if (mask.containsKey(ex.id))
				subset.add(ex);
		}
		return subset;
	}
	
	public void read(String filename) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(filename));
		String line;
		while ((line = in.readLine()) != null)
			mask.put(Int.parseInt(line), Bool.TRUE);
		in.close();
	}
	
}
