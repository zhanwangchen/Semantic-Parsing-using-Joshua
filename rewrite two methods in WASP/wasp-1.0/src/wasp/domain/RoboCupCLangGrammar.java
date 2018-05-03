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
package wasp.domain;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import wasp.data.Dictionary;
import wasp.data.Example;
import wasp.data.Examples;
import wasp.data.Node;
import wasp.data.Symbol;
import wasp.data.Terminal;
import wasp.main.Parse;
import wasp.mrl.MRLGrammar;
import wasp.mrl.MRLParser;
import wasp.mrl.Production;
import wasp.mrl.ProductionSymbol;
import wasp.util.Arrays;
import wasp.util.Int;
import wasp.util.Short;

/**
 * The formal grammar of CLang, the coach language in the RoboCup domain.
 * 
 * @author ywwong
 *
 */
public class RoboCupCLangGrammar extends MRLGrammar {

	private static final String[] NONTERMS = {
		"Action",
		"Condition",
		"Directive",
		"Ident",
		"Num",
		"Player",
		"Point",
		"Region",
		"Rule",
		"Statement",
		"Team",
		"Unum"
	};
	
	private static final String START = "Statement";

	private HashMap reorderArgs;
	
	public RoboCupCLangGrammar() {
		reorderArgs = new HashMap();
	}
	
	public int getStart() {
		return Dictionary.nonterm(START);
	}
	
	public int countNonterms() {
		return NONTERMS.length;
	}
	
	public Symbol[] tokenize(String str) {
		Terminal.readWords = false;
		ArrayList list = new ArrayList();
		StringTokenizer tokenizer = new StringTokenizer(str, "(){}\" \t\n\r\f", true);
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			if (token.length() == 1 && Character.isWhitespace(token.charAt(0)))
				continue;
			list.add(Symbol.read(token));
		}
		Symbol[] syms = (Symbol[]) list.toArray(new Symbol[0]);
		if (syms.length >= 5 && syms[1].stringEquals("definerule"))
			syms = (Symbol[]) Arrays.subarray(syms, 4, syms.length-1);
		return syms;
	}
	
	public String combine(Symbol[] syms) {
		StringBuffer sb = new StringBuffer();
		boolean insideQuotes = false;
		boolean addSpace = false;
		for (short i = 0; i < syms.length; ++i) {
			String token = Dictionary.term(syms[i].getId());
			if (token.equals(")") || token.equals("}"))
				sb.append(token);
			else if (token.equals("(") || token.equals("{")) {
				if (addSpace)
					sb.append(' ');
				sb.append(token);
			} else if (token.equals("\"")) {
				if (!insideQuotes && addSpace)
					sb.append(' ');
				sb.append(token);
				insideQuotes = !insideQuotes;
			} else {
				if (addSpace)
					sb.append(' ');
				sb.append(token);
			}
			addSpace = !token.equals("(") && !token.equals("{") && !(token.equals("\"") && insideQuotes);
		}
		if (syms.length >= 2 && !syms[1].stringEquals("definec") && !syms[1].stringEquals("definer")) {
			sb.insert(0, "(definerule RULE direc ");
			sb.append(')');
		}
		return sb.toString();
	}

	public boolean[][] evaluate(Examples examples, Examples gold) throws IOException {
		MRLParser parser = new MRLParser(this);
		int size = examples.size();
		boolean[][] isCorrect = new boolean[size][];
		for (int i = 0; i < size; ++i) {
			Example ex = examples.getNth(i);
			Node correct = gold.get(ex.id).F.parse;
			Parse[] parses = ex.getSortedParses();
			isCorrect[i] = new boolean[parses.length];
			for (int j = 0; j < parses.length; ++j) {
				Node parse = parser.parse(tokenize(parses[j].toStr()));
				if (parse != null)
					isCorrect[i][j] = match(correct, parse);
			}
		}
		return isCorrect;
	}
	
	private boolean match(Node x, Node y) {
		Production xp = ((ProductionSymbol) x.getSymbol()).getProduction();
		Production yp = ((ProductionSymbol) y.getSymbol()).getProduction();
		if (xp.equals(yp)) {
			short xn = x.countChildren();
			Node[] xc = x.getChildren();
			Node[] yc = y.getChildren();
			Short r = (Short) reorderArgs.get(xp);
			for (short i = 0; i < ((r==null) ? xn : r.val); ++i)
				if (!match(xc[i], yc[i]))
					return false;
			if (r != null) {
				short[] yidx = new short[xn-r.val];
				for (short i = r.val; i < xn; ++i)
					yidx[i-r.val] = i;
				PermuteState s = new PermuteState(yidx);
				while (yidx != null) {
					boolean match = true;
					for (short i = r.val; i < xn; ++i)
						if (!match(xc[i], yc[yidx[i]])) {
							match = false;
							break;
						}
					if (match)
						return true;
					yidx = nextPermute(yidx, s);
				}
				return false;
			}
			return true;
		}
		return false;
	}

	// http://www.geocities.com/permute_it/01example.html
	private static class PermuteState {
		public short[] p;
		public short i;
		public PermuteState(short[] array) {
			p = new short[array.length+1];
			for (short j = 0; j <= array.length; ++j)
				p[j] = j;
			i = 1;
		}
	}
	private short[] nextPermute(short[] array, PermuteState s) {
		while (s.i < array.length) {
			--s.p[s.i];
			short j = (short) ((s.i%2) * s.p[s.i]);
			short tmp = array[j];
			array[j] = array[s.i];
			array[s.i] = tmp;
			s.i = 1;
			while (s.p[s.i] == 0) {
				s.p[s.i] = s.i;
				++s.i;
			}
			return array;
		}
		return null;
	}
	
	protected void readModifiers(Production prod, String[] line, Int index) {
		if (index.val < line.length && line[index.val].equals("reorder-args")) {
			reorderArgs.put(prod, new Short(Short.parseShort(line[index.val+1])));
			index.val += 2;
		}
	}
	
}
