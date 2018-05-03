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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import wasp.data.Dictionary;
import wasp.data.Example;
import wasp.data.Examples;
import wasp.data.Symbol;
import wasp.data.Terminal;
import wasp.main.Config;
import wasp.main.Parse;
import wasp.mrl.MRLGrammar;
import wasp.mrl.MRLParser;
import wasp.mrl.Production;
import wasp.util.Arrays;
import wasp.util.InputStreamWriter;
import wasp.util.Int;
import wasp.util.TokenReader;

/**
 * The formal grammar of the functional query language in the Geoquery domain.
 * 
 * @author ywwong
 *
 */
public class GeoFunqlGrammar extends MRLGrammar {

	private static Logger logger = Logger.getLogger(GeoFunqlGrammar.class.getName());
	
	private static final String[] NONTERMS = {
		"City",
		"CityName",
		"Country",
		"CountryName",
		"Num",
		"Place",
		"PlaceName",
		"Query",
		"River",
		"RiverName",
		"State",
		"StateAbbrev",
		"StateName",
		"X"
	};
	
	private static final String START = "Query";
	
	public int getStart() {
		return Dictionary.nonterm(START);
	}
	
	public int countNonterms() {
		return NONTERMS.length;
	}
	
	public Symbol[] tokenize(String str) {
		Terminal.readWords = false;
		ArrayList list = new ArrayList();
		StringTokenizer tokenizer = new StringTokenizer(str, "(),' \t\n\r\f", true);
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			if (token.length() == 1 && Character.isWhitespace(token.charAt(0)))
				continue;
			list.add(Symbol.read(token));
		}
		return (Symbol[]) list.toArray(new Symbol[0]);
	}
	
	public String combine(Symbol[] syms) {
		StringBuffer sb = new StringBuffer();
		boolean addSpace = false;
		for (short i = 0; i < syms.length; ++i) {
			String token = Dictionary.term(syms[i].getId());
			boolean sep = token.equals("(") || token.equals(")") || token.equals(",") || token.equals("'");
			if (sep)
				sb.append(token);
			else {
				if (addSpace)
					sb.append(' ');
				sb.append(token);
			}
			addSpace = !sep;
		}
		return sb.toString();
	}
	
	public boolean[][] evaluate(Examples examples, Examples gold) throws IOException {
		String evalDir = Config.get(Config.GEO_EVAL_DIR);
		String execFile = Config.get(Config.SICSTUS_EXEC);
		File geobaseFile = new File(evalDir, "geobase.pl");
		File geoqueryFile = new File(evalDir, "geoquery.pl");
		File evalFile = new File(evalDir, "eval.pl");
		File dataFile = File.createTempFile("eval", ".pl");
		File outputFile = File.createTempFile("eval", ".out");
		dataFile.deleteOnExit();
		outputFile.deleteOnExit();
		PrintWriter dataOut = new PrintWriter(new BufferedWriter(new FileWriter(dataFile)));
		dataOut.println(":-compile('"+geobaseFile.getPath()+"').");
		dataOut.println(":-compile('"+geoqueryFile.getPath()+"').");
		dataOut.println(":-compile('"+evalFile.getPath()+"').");
		dataOut.print(":-eval([");
		MRLParser parser = new MRLParser(this);
		boolean first = true;
		int size = examples.size();
		boolean[][] isCorrect = new boolean[size][];
		for (int i = 0; i < size; ++i) {
			Example ex = examples.getNth(i);
			Symbol[] correct = gold.get(ex.id).F.syms;
			Parse[] parses = ex.getSortedParses();
			isCorrect[i] = new boolean[parses.length];
			for (int j = 0; j < parses.length; ++j) {
				Symbol[] syms = tokenize(parses[j].toStr());
				isCorrect[i][j] = Arrays.equal(correct, syms);
				if (!isCorrect[i][j]) {
					// make sure that the translation is grammatical
					if (parser.parse(syms) == null)
						continue;
					if (first)
						first = false;
					else
						dataOut.print(',');
					dataOut.print(i);
					dataOut.print(',');
					dataOut.print(j);
					dataOut.print(',');
					dataOut.print(combine(correct));
					dataOut.print(',');
					dataOut.print(combine(syms));
				}
			}
		}
		dataOut.println("]).");
		dataOut.println(":-halt.");
		dataOut.close();
		
		// run Geoquery evaluation scripts on SICSTUS
		logger.info("Geoquery evaluation scripts start");
		try {
			String cmd = execFile+" -l "+dataFile.getPath();
			Process proc = Runtime.getRuntime().exec(cmd);
			PrintStream out = new PrintStream(new FileOutputStream(outputFile), true);
			Thread outThread = new InputStreamWriter(proc.getInputStream(), out);
			outThread.start();
			new InputStreamWriter(proc.getErrorStream(), System.err).start();
			int exitVal = proc.waitFor();
			if (exitVal != 0) {
				logger.warning("SICSTUS terminates abnormally");
				return isCorrect;
			}
			outThread.join();
			out.close();
		} catch (IOException e) {
			logger.warning(e.toString());
			return isCorrect;
		} catch (InterruptedException e) {
			logger.warning(e.toString());
			return isCorrect;
		}
		logger.info("Geoquery evaluation scripts end");
		
		// read output of the evaluation scripts
		TokenReader in = new TokenReader(new BufferedReader(new FileReader(outputFile)));
		String[] line;
		while ((line = in.readLine()) != null)
			if (line[2].equals("y"))
				isCorrect[Int.parseInt(line[0])][Int.parseInt(line[1])] = true;
		in.close();
		return isCorrect;
	}
	
	protected void readModifiers(Production prod, String[] line, Int index) {}
	
}
