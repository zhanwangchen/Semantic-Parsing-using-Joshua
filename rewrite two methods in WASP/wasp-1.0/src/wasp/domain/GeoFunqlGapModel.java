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
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;

import wasp.data.Terminal;
import wasp.main.Config;
import wasp.math.Math;
import wasp.nl.GapModel;
import wasp.util.Arrays;
import wasp.util.Double;
import wasp.util.TokenReader;

/**
 * A word-gap model for the Geoquery domain.
 * 
 * @author ywwong
 *
 */
public class GeoFunqlGapModel extends GapModel {

	private static final int NUM = 0;
	private static final int CITY_NAME = 1;
	private static final int COUNTRY_NAME = 2;
	private static final int PLACE_NAME = 3;
	private static final int RIVER_NAME = 4;
	private static final int STATE_ABBREV = 5;
	private static final int STATE_NAME = 6;
	private static final int NUM_WORD_CLASSES = 7;
	
	/** The word-class-specific weights. */
	private double[] wordClassWeights;
	
	private double[] wordClassOuters;
	
	private HashSet cityNames;
	private HashSet countryNames;
	private HashSet placeNames;
	private HashSet riverNames;
	private HashSet stateAbbrevs;
	private HashSet stateNames;
	
	public GeoFunqlGapModel() throws IOException {
		wordClassWeights = new double[NUM_WORD_CLASSES];
		wordClassOuters = new double[NUM_WORD_CLASSES];
		Arrays.fill(wordClassOuters, Double.NEGATIVE_INFINITY);
		readNames();
	}
	
	private void readNames() throws IOException {
		TokenReader in = new TokenReader(new BufferedReader(new FileReader(Config.get(Config.GEO_NAMES))));
		cityNames = new HashSet();
		countryNames = new HashSet();
		placeNames = new HashSet();
		riverNames = new HashSet();
		stateAbbrevs = new HashSet();
		stateNames = new HashSet();
		String[] line;
		while ((line = in.readLine()) != null) {
			HashSet names = null;
			if (line[0].equals("*n:CityName"))
				names = cityNames;
			else if (line[0].equals("*n:CountryName"))
				names = countryNames;
			else if (line[0].equals("*n:PlaceName"))
				names = placeNames;
			else if (line[0].equals("*n:RiverName"))
				names = riverNames;
			else if (line[0].equals("*n:StateAbbrev"))
				names = stateAbbrevs;
			else if (line[0].equals("*n:StateName"))
				names = stateNames;
			if (names != null)
				for (int i = 3; i < line.length-1; ++i)
					names.add(new Terminal(line[i], true));
		}
		in.close();
	}
	
	public double getWeight(Terminal word) {
		double weight = getBasicWeight(word);
		if (word.isNum())
			weight += wordClassWeights[NUM];
		if (cityNames.contains(word))
			weight += wordClassWeights[CITY_NAME];
		if (countryNames.contains(word))
			weight += wordClassWeights[COUNTRY_NAME];
		if (placeNames.contains(word))
			weight += wordClassWeights[PLACE_NAME];
		if (riverNames.contains(word))
			weight += wordClassWeights[RIVER_NAME];
		if (stateAbbrevs.contains(word))
			weight += wordClassWeights[STATE_ABBREV];
		if (stateNames.contains(word))
			weight += wordClassWeights[STATE_NAME];
		return weight;
	}

	public int countParams() {
		return countBasicParams()+NUM_WORD_CLASSES;
	}
	
	public double[] getWeightVector() {
		return Arrays.concat(getBasicWeightVector(), wordClassWeights);
	}
	
	public void setWeightVector(double[] weights) {
		setBasicWeightVector(Arrays.subarray(weights, 0, weights.length-NUM_WORD_CLASSES));
		wordClassWeights = Arrays.subarray(weights, weights.length-NUM_WORD_CLASSES, weights.length);
	}
	
	public double[] getOuterScores() {
		return Arrays.concat(getBasicOuterScores(), wordClassOuters);
	}
	
	public void addOuterScores(Terminal word, double z) {
		addBasicOuterScores(word, z);
		if (word.isNum())
			wordClassOuters[NUM] = Math.logAdd(wordClassOuters[NUM], z-wordClassWeights[NUM]);
		if (cityNames.contains(word))
			wordClassOuters[CITY_NAME] =
				Math.logAdd(wordClassOuters[CITY_NAME], z-wordClassWeights[CITY_NAME]);
		if (countryNames.contains(word))
			wordClassOuters[COUNTRY_NAME] = 
				Math.logAdd(wordClassOuters[COUNTRY_NAME], z-wordClassWeights[COUNTRY_NAME]);
		if (placeNames.contains(word))
			wordClassOuters[PLACE_NAME] =
				Math.logAdd(wordClassOuters[PLACE_NAME], z-wordClassWeights[PLACE_NAME]);
		if (riverNames.contains(word))
			wordClassOuters[RIVER_NAME] =
				Math.logAdd(wordClassOuters[RIVER_NAME], z-wordClassWeights[RIVER_NAME]);
		if (stateAbbrevs.contains(word))
			wordClassOuters[STATE_ABBREV] =
				Math.logAdd(wordClassOuters[STATE_ABBREV], z-wordClassWeights[STATE_ABBREV]);
		if (stateNames.contains(word))
			wordClassOuters[STATE_NAME] =
				Math.logAdd(wordClassOuters[STATE_NAME], z-wordClassWeights[STATE_NAME]);
	}
	
	public void resetOuterScores() {
		resetBasicOuterScores();
		Arrays.fill(wordClassOuters, Double.NEGATIVE_INFINITY);
	}

	public void read() throws IOException {
		TokenReader in = getReader();
		readBasic(in);
		String[] line = in.readLine();  // begin word-class-weights
		wordClassWeights = new double[NUM_WORD_CLASSES];
		line = in.readLine();
		while (!(line[0].equals("end") && line[1].equals("word-class-weights"))) {
			if (line[0].equals("Num"))
				wordClassWeights[NUM] = Double.parseDouble(line[1]);
			else if (line[0].equals("CityName"))
				wordClassWeights[CITY_NAME] = Double.parseDouble(line[1]);
			else if (line[0].equals("CountryName"))
				wordClassWeights[COUNTRY_NAME] = Double.parseDouble(line[1]);
			else if (line[0].equals("PlaceName"))
				wordClassWeights[PLACE_NAME] = Double.parseDouble(line[1]);
			else if (line[0].equals("RiverName"))
				wordClassWeights[RIVER_NAME] = Double.parseDouble(line[1]);
			else if (line[0].equals("StateAbbrev"))
				wordClassWeights[STATE_ABBREV] = Double.parseDouble(line[1]);
			else if (line[0].equals("StateName"))
				wordClassWeights[STATE_NAME] = Double.parseDouble(line[1]);
			line = in.readLine();
		}
		in.close();
	}
	
	public void write() throws IOException {
		PrintWriter out = getWriter();
		writeBasic(out);
		out.println("begin word-class-weights");
		out.print("Num ");
		out.println(wordClassWeights[NUM]);
		out.print("CityName ");
		out.println(wordClassWeights[CITY_NAME]);
		out.print("CountryName ");
		out.println(wordClassWeights[COUNTRY_NAME]);
		out.print("PlaceName ");
		out.println(wordClassWeights[PLACE_NAME]);
		out.print("RiverName ");
		out.println(wordClassWeights[RIVER_NAME]);
		out.print("StateAbbrev ");
		out.println(wordClassWeights[STATE_ABBREV]);
		out.print("StateName ");
		out.println(wordClassWeights[STATE_NAME]);
		out.println("end word-class-weights");
		out.close();
	}

}
