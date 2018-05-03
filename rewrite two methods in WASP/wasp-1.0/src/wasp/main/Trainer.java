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
package wasp.main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import wasp.align.NTo1WordAlign;
import wasp.data.Example;
import wasp.data.ExampleMask;
import wasp.data.Examples;
import wasp.data.Node;

/**
 * Code for training translation models.
 * 
 * @author ywwong
 *
 */
public class Trainer {

	private Trainer() {}
	
	/**
	 * The main program for training translation models.  This program takes the following command-line
	 * arguments:
	 * <p>
	 * <blockquote><code><b>java wasp.main.Trainer</b> <u>config-file</u> <u>model-dir</u>
	 * <u>mask-file</u></code></blockquote>
	 * <p>
	 * <ul>
	 * <li><code><u>config-file</u></code> - the configuration file that contains the current 
	 * settings.</li>
	 * <li><code><u>model-dir</u></code> - the directory for storing the learned translation model.</li>
	 * <li><code><u>mask-file</u></code> - the example mask that specifies the training set.</li>
	 * </ul>
	 * <p>
	 * Log messages are sent to the standard error stream, which can be captured for detailed error
	 * analysis.
	 * 
	 * @param args the command-line arguments.
	 * @throws IOException if an I/O error occurs.
	 * @throws SAXException if the XML parser throws a <code>SAXException</code> while parsing.
	 * @throws ParserConfigurationException if an XML parser cannot be created which satisfies the 
	 * requested configuration.
	 */
	public static void main(String[] args) throws IOException, SAXException, ParserConfigurationException {
//		if (args.length != 3) {
//			System.err.println("Usage: java wasp.main.Trainer config-file model-dir mask-file");
//			System.err.println();
//			System.err.println("config-file - the configuration file that contains the current settings.");
//			System.err.println("model-dir - the directory for storing the learned translation model.");
//			System.err.println("mask-file - the example mask that specifies the training set.");
//			System.exit(1);
//		}
//		String configFilename = args[0];
//		String modelDir = args[1];
//		String maskFilename = args[2];
		String configFilename = "F:/Germany/lecture/NLP/final project/wasp-1.0/data/geo-funql/config";
		String modelDir = "F:/Germany/lecture/NLP/final project/wasp-1.0/data/geo-funql/model";
		String maskFilename = "F:/Germany/lecture/NLP/final project/wasp-1.0/data/geo-funql/split-250/run-1/fold-1/train-N10-2";
		
		Config.read(configFilename);
		Config.setModelDir(modelDir);
		Examples examples = new Examples();
		examples.read(Config.getCorpusFile());
//		ExampleMask mask = new ExampleMask();
//		mask.read(maskFilename);
//		examples = mask.apply(examples);
//		TranslationModel.createNew().train(examples);
//		Config.getMRLGrammar().writeMore();
		getCorpus(examples);
	}
	
	public static void getCorpus(Examples examples) throws IOException {
		Writer NLW = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("nl.txt"), "UTF-8"));
		Writer MRLW = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("MRL.txt"), "UTF-8"));
		Writer AlignsW = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("Aligns.txt"), "UTF-8"));
			
//		FileWriter NLW = new FileWriter(new File("nl.txt"));
//		FileWriter MRLW = new FileWriter(new File("MRL.txt"));
//		FileWriter AlignsW = new FileWriter(new File("Aligns.txt"));
		for (Iterator it = examples.iterator(); it.hasNext();) {
			Example ex = (Example) it.next();
			String NL = (String) ex.nlMap.get("en");
//			System.out.println();
			NL = NL.trim().substring(0,NL.trim().length()-1).trim();
			//System.out.println(NL);
			String Meanning = (String) ex.F.str;
//			Meanning = Meanning.replace("(", " ( ").replace(")", " ) ");
			Meanning = Meanning.replace("answer(", "");
			Meanning = Meanning.replace("(", "( ").replace(")", " ");
			String Aligns = (String) ex.getSortedAligns().toString();
			NLW.write(NL + "\n");
			MRLW.write(Meanning.trim() + "\n");
			
			String test = ex.F.str.trim();
			if(test.indexOf(")")<test.length()-1){
				if(test.charAt(test.indexOf(")")+1) !=')'){
					System.out.println(NL+"\n");
					System.out.println(test+"\n");
//					System.out.println("=====================================================================");
				}
			}
				
			
		}
		NLW.close();
		MRLW.close();
		
	}
	
}
