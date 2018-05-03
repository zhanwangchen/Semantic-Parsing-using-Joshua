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
package wasp.nl;

import java.util.ArrayList;
import java.util.StringTokenizer;

import wasp.data.Node;
import wasp.data.Terminal;
import wasp.util.Int;

/**
 * Code for tokenizing NL sentences, reading syntactic parses of NL sentences, and doing other NL-related 
 * tasks.
 * 
 * @author ywwong
 *
 */
public class NLGrammar {

	public Terminal[] tokenize(String str) {
		Terminal.readWords = true;
		ArrayList list = new ArrayList();
		list.add(Terminal.boundary());
		StringTokenizer tokenizer = new StringTokenizer(str);
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			list.add(Terminal.read(token));
		}
		// remove punctuation at the end of a sentence
		String last = list.get(list.size()-1).toString();
		if (last.equals(".") || last.equals("?"))
			list.remove(list.size()-1);
		list.add(Terminal.boundary());
		return (Terminal[]) list.toArray(new Terminal[0]);
	}
	
	public String combine(Terminal[] syms) {
		StringBuffer sb = new StringBuffer();
		for (short i = 1; i < syms.length-1; ++i) {
			if (i > 1)
				sb.append(' ');
			sb.append(syms[i]);
		}
		return sb.toString();
	}
	
	public Node readSyn(String str) {
		String[] line = tokenizeSyn(str);
		Int index = new Int(0);
		Node.readSyn = true;
		Node syn = Node.read(line, index);
		return (index.val==line.length) ? syn : null;
	}
	
	public String[] tokenizeSyn(String str) {
		ArrayList list = new ArrayList();
		StringTokenizer tokenizer = new StringTokenizer(str, "() \t\n\r\f", true);
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			if (token.length() == 1 && Character.isWhitespace(token.charAt(0)))
				continue;
			list.add(token);
		}
		return (String[]) list.toArray(new String[0]);
	}
	
	public String writeSyn(Node syn) {
		return syn.toString().replaceAll("( ", "(").replaceAll(" )", ")");
	}
	
}
