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
package wasp.align;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Logger;

import wasp.data.Dictionary;
import wasp.data.Example;
import wasp.data.Examples;
import wasp.data.Node;
import wasp.data.Terminal;
import wasp.main.Config;
import wasp.mrl.Production;
import wasp.mrl.ProductionSymbol;
import wasp.nl.NLGrammar;
import wasp.util.Int;
import wasp.util.Short;

/**
 * A word alignment "model" that simply outputs the gold-standard word alignments, if they are available
 * in the corpus.
 * 
 * @author ywwong
 *
 */
public class GoldStandard extends WordAlignModel {

	private static Logger logger = Logger.getLogger(GoldStandard.class.getName());
	
	private static class AugSymbol {
		public Terminal sym;
		public int nodeLhs;
		public short nodeId;
		public AugSymbol(Terminal sym, int nodeLhs, short nodeId) {
			this.sym = sym;
			this.nodeLhs = nodeLhs;
			this.nodeId = nodeId;
		}
		public AugSymbol(Terminal sym) {
			this.sym = sym;
			nodeLhs = -1;
			nodeId = -1;
		}
		public boolean isLinked() {
			return nodeLhs >= 0;
		}
	}
	
	public void train(Examples examples) throws IOException {
		logger.info("Retrieving gold-standard word alignments");
		for (Iterator it = examples.iterator(); it.hasNext();) {
			Example ex = (Example) it.next();
			String augsyn = (String) ex.augsynMap.get(Config.getNL());
			if (augsyn == null) {
				logger.warning("example "+ex.id+": no augmented NL syntactic parse");
				continue;
			}
			ArrayList augsyms = readAugsyn(augsyn);
			if (augsyms == null || augsyms.size() != ex.E.length-2) {
				logger.warning("example "+ex.id+": sentence and augmented parse do not match");
				continue;
			}
			Node parse = getMRLParse(ex);
			if (parse == null) {
				logger.warning("example "+ex.id+": MRL parse is invalid");
				continue;
			}
			ex.F.replace((short) 0, parse);
			NTo1WordAlign align = toWordAlign(augsyms, ex);
			if (align == null) {
				logger.warning("example "+ex.id+": word alignment is invalid");
				continue;
			}
			ex.EFalign = align;
			ex.aligns.clear();
			ex.aligns.add(align);
			NTo1WordAlign[] a = ex.getSortedAligns();
			System.out.println(a);
		}
		logger.info("Gold-standard word alignments have been retrieved");
	}

	private ArrayList readAugsyn(String str) {
		String[] line = new NLGrammar().tokenizeSyn(str);
		Int index = new Int(0);
		ArrayList augsyms = new ArrayList();
		if (readAugsyn(augsyms, line, index) && index.val == line.length)
			return augsyms;
		else
			return null;
	}
	
	private static boolean readAugsyn(ArrayList augsyms, String[] line, Int index) {
		int i = index.val;
		if (i >= line.length)
			return false;
		if (line[i].equals("(")) {
			++i;
			if (i == line.length || Terminal.read(line[i]) == null)
				return false;
			for (++i; i < line.length && !line[i].equals(")");) {
				Int idx = new Int(i);
				if (!readAugsyn(augsyms, line, idx))
					return false;
				i = idx.val;
			}
			if (i == line.length)
				return false;
			index.val = i+1;
			return true;
		} else {
			AugSymbol augsym;
			Terminal.readWords = true;
			int idx1 = line[i].indexOf("-[");
			int idx2 = line[i].indexOf(":", idx1);
			int idx3 = line[i].indexOf("]", idx2);
			if (0 < idx1 && idx1+2 < idx2 && idx2+1 < idx3 && idx3 == line[i].length()-1)
				augsym = new AugSymbol((Terminal) Terminal.read(line[i].substring(0, idx1)),
						Dictionary.nonterm(line[i].substring(idx1+2, idx2)),
						Short.parseShort(line[i].substring(idx2+1, idx3)));
			else
				augsym = new AugSymbol((Terminal) Terminal.read(line[i]));
			if (augsym.sym == null)
				return false;
			augsyms.add(augsym);
			index.val = i+1;
			return true;
		}
	}
	
	private Node getMRLParse(Example ex) {
		Short index = new Short(0);
		Node parse = getMRLParse(ex.Fparse, index);
		return (index.val==ex.Fparse.size()) ? parse : null;
	}

	private Node getMRLParse(ArrayList prods, Short index) {
		if (index.val >= prods.size())
			return null;
		Production prod = (Production) prods.get(index.val++);
		Node n = new Node(new ProductionSymbol(prod));
		for (short i = 0; i < prod.countArgs(); ++i) {
			Node child = getMRLParse(prods, index);
			if (child == null)
				return null;
			else
				n.addChild(child);
		}
		return n;
	}
	
	private NTo1WordAlign toWordAlign(ArrayList augsyms, Example ex) {
		NTo1WordAlign align = new NTo1WordAlign(ex.E, ex.F.linear, 1);
		for (short i = 0; i < augsyms.size(); ++i) {
			AugSymbol augsym = (AugSymbol) augsyms.get(i);
			if (augsym.isLinked()) {
				if (augsym.nodeId < 0 || augsym.nodeId >= ex.F.linear.length) {
					logger.warning("example "+ex.id+": "+augsym.nodeId+" is invalid node ID");
					return null;
				}
				if (augsym.nodeLhs != ex.F.lprods[augsym.nodeId].getLhs()) {
					logger.warning("example "+ex.id+": LHS mismatch for node ID "+augsym.nodeId);
					return null;
				}
				align.addLink((short) (i+1), augsym.nodeId);
			}
		}
		return align;
	}
	
}
