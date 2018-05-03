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
package wasp.main.eval;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.TreeSet;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import wasp.data.Example;
import wasp.data.Examples;
import wasp.main.Config;
import wasp.main.Parse;
import wasp.util.Arrays;
import wasp.util.FileWriter;

/**
 * The program for evaluating the performance of parsers.  The collected statistics can be used to create
 * learning curves and precision-recall curves.  All evaluation metrics (precision, recall and F-measure)
 * are based on the definition of correctness of translations, which is domain-specific.  Precision is 
 * the percentage of translations that are correct.  Recall is the percentage of input sentences that are 
 * correctly translated.  F-measure is the harmonic mean of precision and recall.
 * 
 * Macroaveraging is used to obtain the average statistics across all trials.
 * 
 * @author ywwong
 *
 */
public abstract class ParserEvaluator {

	private static Logger logger = Logger.getLogger(ParserEvaluator.class.getName());
	
	/**
	 * Contains information about an input sentence and its MR translation.  This information is passed
	 * to the evaluators to generate statistics.
	 * 
	 * @author ywwong
	 *
	 */
	protected static class Entry implements Comparable {
		private static int nextId = 0;
		private int id;
		public boolean isTranslated;
		public double score;
		public boolean isCorrect;
		public Entry(double score, boolean isCorrect) {
			id = nextId++;
			isTranslated = true;
			this.score = score;
			this.isCorrect = isCorrect;
		}
		/**
		 * This is for sentences not having any translations.
		 */
		public Entry() {
			id = nextId++;
			isTranslated = false;
		}
		/**
		 * Sentences that have translations are ranked higher.  For sentences that have translations,
		 * those with higher-scoring translations are ranked higher.  The entry IDs ensure that the 
		 * natural ordering is consistent with equals.
		 */
		public int compareTo(Object o) {
			if (isTranslated && !((Entry) o).isTranslated)
				return -1;
			else if (!isTranslated && ((Entry) o).isTranslated)
				return 1;
			else if (score > ((Entry) o).score)
				return -1;
			else if (score < ((Entry) o).score)
				return 1;
			else if (id < ((Entry) o).id)
				return -1;
			else if (id > ((Entry) o).id)
				return 1;
			else
				return 0;
		}
	}
	
	private static TreeSet evaluate(PrintWriter out, Examples gold, String filename, int k)
	throws IOException, SAXException, ParserConfigurationException {
		Examples examples = new Examples();
		//examples.read(filename);
		readTestResFromText(examples,"testMRRes.txt");
		
		
		out.println("file "+filename);
		// send all parses to the domain-specific evaluator
		boolean[][] isCorrect = Config.getMRLGrammar().evaluate(examples, gold);
		// choose the top-scoring (correct) parse for each example, and create a new entry for it
		TreeSet entries = new TreeSet();
		int size = examples.size();
		for (int i = 0; i < size; ++i) {
			Example ex = examples.getNth(i);
			out.println("example "+ex.id);
			out.println("correct translation:");
			out.println(gold.get(ex.id).F.str);
			boolean isCorrectlyTranslated = false;
			Parse[] parses = ex.getSortedParses();
			if (parses.length == 0)
				entries.add(new Entry());
			else {
				for (int j = 0; j < parses.length; ++j) {
					out.println("parse "+j+": ");
					if (!isCorrect[i][j])
						out.print('*');
					out.println(parses[j].toStr());
					if (!isCorrectlyTranslated && isCorrect[i][j]) {
						entries.add(new Entry(parses[j].score, true));
						isCorrectlyTranslated = true;
					}
				}
				if (!isCorrectlyTranslated)
					entries.add(new Entry(parses[0].score, false));
			}
		}
		return entries;
	}
	
	private static Examples readTestResFromText(Examples examples , String filename){
		logger.info("readTestResFromTexts");
		try {
			File f = new File(filename);
			BufferedReader ResRd = new BufferedReader(new FileReader(filename));
			BufferedReader testRd = new BufferedReader(new FileReader("testMask.txt"));
			String line = testRd.readLine();
			String[] nums = line.trim().split(" ");
			for(int i=0;i<nums.length;i++){
				Example ex = new Example();
				ex.id = Integer.valueOf(nums[i]);
				ArrayList parses = new ArrayList();
				Parse p = new Parse(ResRd.readLine(),0);
				parses.add(p);
				ex.parses = parses;
				examples.add(ex);
			}
			return examples;
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	
	
	
	/**
	 * Summarizes the evaluation results using a particular evaluation metric.  The results are usually
	 * statistical figures which are written to the specified character stream.
	 * 
	 * @param out the character stream to write to.
	 * @param entries sets of entries that contain information about input sentences in each trial (e.g. 
	 * whether the sentences are correctly translated); there is one entry set for each trial.
	 */
	protected abstract void summarize(PrintWriter out, TreeSet[] entries);
	
	/**
	 * The main program for evaluating the performance of parsers.  This program takes the following 
	 * command-line arguments:
	 * <p>
	 * <blockquote><code><b>java wasp.main.eval.ParserEvaluator</b> <u>config-file</u> <u>output-file</u>
	 * <u>input-file</u> ...</code></blockquote>
	 * <p>
	 * <ul>
	 * <li><code><u>config-file</u></code> - the configuration file that contains the current 
	 * settings.</li>
	 * <li><code><u>output-file</u></code> - the output text file for storing the evaluation results.</li>
	 * <li><code><u>input-file</u></code> - the input XML file that contains parses to evaluate.</li>
	 * </ul>
	 * <p>
	 * If there are more than one input XML file, then each file represents a separate trial, and
	 * macroaveraging will be used to obtain the average statistics across all trials.  Log messages are
	 * sent to the standard error stream, which can be captured for detailed error analysis.
	 * 
	 * @param args the command-line arguments.
	 * @throws IOException if an I/O error occurs.
	 * @throws SAXException if the XML parser throws a <code>SAXException</code> while parsing.
	 * @throws ParserConfigurationException if an XML parser cannot be created which satisfies the 
	 * requested configuration.
	 */
	public static void main(String[] args) throws IOException, SAXException, ParserConfigurationException {
		if (args.length < 3) {
			System.err.println("Usage: java wasp.main.eval.ParserEvaluator config-file output-file input-file ...");
			System.err.println();
			System.err.println("config-file - the configuration file that contains the current settings.");
			System.err.println("output-file - the output text file for storing the evaluation results.");
			System.err.println("input-file - the input XML file that contains parses to evaluate.");
			System.exit(1);
		}
		String configFilename = args[0];
		String outputFilename = args[1];
		String[] inputFilenames = (String[]) Arrays.subarray(args, 2, args.length);
//		String configFilename = "F:/Germany/lecture/NLP/final project/wasp-1.0/data/geo-funql/config";
//		String outputFilename = "F:/Germany/lecture/NLP/final project/wasp-1.0/data/geo-funql/empty-30.output.txt";
//		String[] inputFilenames =new String[1];
//		inputFilenames[0]="a";
		
		Config.read(configFilename);
		logger.info("Evaluation starts");
		PrintWriter out = new PrintWriter(new BufferedWriter(FileWriter.createNew(outputFilename)));
		Examples gold = new Examples();
		gold.read(Config.getCorpusFile());
		TreeSet[] entries = new TreeSet[inputFilenames.length];
		for (int i = 0; i < inputFilenames.length; ++i) {
			logger.fine("evaluate "+inputFilenames[i]);
			entries[i] = evaluate(out, gold, inputFilenames[i], 1);
		}
		new Precision().summarize(out, entries);
		new Recall().summarize(out, entries);
		new FMeasure().summarize(out, entries);
		new PrecisionRecallCurve().summarize(out, entries);
		out.close();
		logger.info("Evaluation ends");
	}
	
}
