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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import wasp.data.Dictionary;
import wasp.data.Terminal;
import wasp.domain.GeoFunqlGapModel;
import wasp.domain.RoboCupCLangGapModel;
import wasp.main.Config;
import wasp.math.Math;
import wasp.util.Double;
import wasp.util.FileWriter;
import wasp.util.TokenReader;

/**
 * A special model for generating words from word gaps.  This model assigns weights to words generated
 * from word gaps.  The formulation of the word-gap model is domain-specific.  This class implements the
 * basic word-gap model, where the assigned weights take the following form:
 * <blockquote>
 * <p>
 * weight(<code>word</code>) = w<sub>0</sub> + w<sub><code>word</code></sub>
 * </blockquote>
 * <p>
 * where w<sub>0</sub> is the default weight and w<sub><code>word</code></sub> is the word-specific
 * weight.
 * 
 * @author ywwong
 *
 */
public class GapModel {

	/** The default weight. */
	protected double defWeight;
	/** The word-specific weights. */
	protected HashMap wordWeights;
	
	protected double defOuter;
	protected HashMap wordOuters;
	
	protected GapModel() {
		defWeight = 0;
		wordWeights = new HashMap();
		defOuter = Double.NEGATIVE_INFINITY;
		wordOuters = new HashMap();
	}
	
	/**
	 * Creates and returns a new word-gap model for the domain specified in the configuration
	 * file (via the key <code>Config.MRL</code>).  Currently, two domains are recognized: 
	 * <code>geo-funql</code> for the Geoquery domain, and <code>robocup-clang</code> for the RoboCup
	 * domain.  The basic word-gap model is returned if the given domain is not recognized.  
	 * 
	 * @return a new word-gap model for the domain specified in the configuration file.
	 */
	public static GapModel createNew() throws IOException {
		String mrl = Config.getMRL();
		if (mrl.equals("geo-funql"))
			return new GeoFunqlGapModel();
		else if (mrl.equals("robocup-clang"))
			return new RoboCupCLangGapModel();
		return new GapModel();
	}
	
	/**
	 * Returns the weight assigned to the given word.
	 * 
	 * @param word an NL word.
	 * @return the weight assigned to the <code>word</code> argument.
	 */
	public double getWeight(Terminal word) {
		return getBasicWeight(word);
	}
	
	/**
	 * Returns a basic component of the weight assigned to the given word, which is based on the
	 * default weight and the word-specific weights.
	 * 
	 * @param wordId a word ID.
	 * @return a basic component of the weight assigned to the specified word.
	 */
	protected double getBasicWeight(Terminal word) {
		return defWeight + wordWeight(word);
	}
	
	private double wordWeight(Terminal word) {
		Double wordWeight = (Double) wordWeights.get(word);
		return (wordWeight==null) ? 0 : wordWeight.val;
	}
	
	///
	/// Parameter estimation
	///

	/**
	 * Returns the number of parameters of this model.
	 */
	public int countParams() {
		return countBasicParams();
	}
	
	protected int countBasicParams() {
		return Dictionary.countWords()+1;
	}
	
	/**
	 * Returns the current parameters of this model listed in a domain-specific order.
	 * 
	 * @return the current parameters of this model.
	 */
	public double[] getWeightVector() {
		return getBasicWeightVector();
	}
	
	protected double[] getBasicWeightVector() {
		double[] weights = new double[Dictionary.countWords()+1];
		weights[0] = defWeight;
		for (int i = 0, j = 1; i < Dictionary.countTerms(); ++i)
			if (Dictionary.isWord(i)) {
				Double w = (Double) wordWeights.get(new Terminal(i));
				weights[j++] = (w==null) ? 0 : w.val;
			}
		return weights;
	}
	
	/**
	 * Sets the parameters of this model.
	 * 
	 * @param weights the model parameters, listed in a domain-specific order. 
	 */
	public void setWeightVector(double[] weights) {
		setBasicWeightVector(weights);
	}
	
	protected void setBasicWeightVector(double[] weights) {
		defWeight = weights[0];
		wordWeights = new HashMap();
		for (int i = 0, j = 1; i < Dictionary.countTerms(); ++i)
			if (Dictionary.isWord(i)) {
				double w = weights[j++];
				if (w != 0)
					wordWeights.put(new Terminal(i), new Double(w));
			}
	}
	
	public double[] getOuterScores() {
		return getBasicOuterScores();
	}
	
	protected double[] getBasicOuterScores() {
		double[] outers = new double[Dictionary.countWords()+1];
		outers[0] = defOuter;
		for (int i = 0, j = 1; i < Dictionary.countTerms(); ++i)
			if (Dictionary.isWord(i)) {
				Double o = (Double) wordOuters.get(new Terminal(i));
				outers[j++] = (o==null) ? Double.NEGATIVE_INFINITY : o.val;
			}
		return outers;
	}
	
	public void addOuterScores(Terminal word, double z) {
		addBasicOuterScores(word, z);
	}
	
	protected void addBasicOuterScores(Terminal word, double z) {
		defOuter = Math.logAdd(defOuter, z-defWeight);
		Double o = (Double) wordOuters.get(word);
		if (o == null) {
			o = new Double(Double.NEGATIVE_INFINITY);
			wordOuters.put(word, o);
		}
		o.val = Math.logAdd(o.val, z-wordWeight(word));
	}
	
	public void resetOuterScores() {
		resetBasicOuterScores();
	}
	
	protected void resetBasicOuterScores() {
		defOuter = Double.NEGATIVE_INFINITY;
		wordOuters = new HashMap();
	}
	
	///
	/// File I/O
	///
	
	private static final String GAP_MODEL = "gap-model";
	
	/**
	 * Reads the model parameters.  Model parameters are read from a file called <code>gap-model</code>
	 * in the directory specified in the configuration file (via the key <code>Config.MODEL_DIR</code>).
	 * 
	 * @throws IOException if an I/O error occurs.
	 */
	public void read() throws IOException {
		TokenReader in = getReader();
		readBasic(in);
		in.close();
	}
	
	protected static TokenReader getReader() throws IOException {
		File file = new File(Config.getModelDir(), GAP_MODEL);
		return new TokenReader(new BufferedReader(new FileReader(file)));
	}
	
	protected void readBasic(TokenReader in) throws IOException {
		String[] line = in.readLine();  // begin default-weight
		line = in.readLine();
		defWeight = Double.parseDouble(line[0]);
		line = in.readLine();  // end default-weight
		line = in.readLine();  // begin word-weights
		wordWeights = new HashMap();
		line = in.readLine();
		while (!(line[0].equals("end") && line[1].equals("word-weights"))) {
			wordWeights.put(new Terminal(line[0], true), new Double(Double.parseDouble(line[1])));
			line = in.readLine();
		}
	}
	
	/**
	 * Writes the model parameters to a file called <code>gap-model</code> in the directory specified
	 * in the configuration file (via the key <code>Config.MODEL_DIR</code>).
	 * 
	 * @throws IOException if an I/O error occurs.
	 */
	public void write() throws IOException {
		PrintWriter out = getWriter();
		writeBasic(out);
		out.close();
	}
	
	protected static PrintWriter getWriter() throws IOException {
		File file = new File(Config.getModelDir(), GAP_MODEL);
		return new PrintWriter(new BufferedWriter(FileWriter.createNew(file)));
	}
	
	protected void writeBasic(PrintWriter out) throws IOException {
		out.println("begin default-weight");
		out.println(defWeight);
		out.println("end default-weight");
		out.println("begin word-weights");
		for (Iterator it = wordWeights.entrySet().iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry) it.next();
			out.print(entry.getKey());
			out.print(' ');
			out.println(entry.getValue());
		}
		out.println("end word-weights");
	}
	
}
