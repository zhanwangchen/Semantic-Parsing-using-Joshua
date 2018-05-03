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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Iterator;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import wasp.data.Example;
import wasp.data.ExampleMask;
import wasp.data.Examples;
import wasp.data.Node;
import wasp.data.Terminal;
import wasp.scfg.SCFGModel;
import wasp.scfg.parse.SCFGParser;

/**
 * An abstract class for synchronous parsers for translating NL sentences into meaning representations.
 *  
 * @author ywwong
 *
 */
public abstract class Parser {

	private static Logger logger = Logger.getLogger(Parser.class.getName());
	
	/**
	 * Creates and returns a new synchronous parser based on the specified translation model.
	 * 
	 * @param model a translation model.
	 * @return a new parser based on the specified model.
	 */
	public static Parser createNew(TranslationModel model) {
		if (model instanceof SCFGModel)
			return new SCFGParser((SCFGModel) model);
		return null;
	}
	
	/**
	 * Finds the <i>K</i> top-scoring parses given an NL sentence.  The value of <i>K</i> is specified in
	 * the configuration file (via the key <code>Config.K_BEST</code>).  Use <i>K</i> = 1 for Viterbi
	 * decoding.
	 * 
	 * @param E the input NL sentence broken into words.
	 * @return the <i>K</i> top-scoring parses given <code>E</code>.
	 */
	public abstract Iterator parse(Terminal[] E);
	
	/**
	 * The main program for parsing (i.e. translation from NL into MRL).  This program takes the following
	 * command-line arguments:
	 * <p>
	 * <blockquote><code><b>java wasp.main.Parser</b> <u>config-file</u> <u>model-dir</u> <u>mask-file</u>
	 * <u>output-file</u></code></blockquote>
	 * <p>
	 * <ul>
	 * <li><code><u>config-file</u></code> - the configuration file that contains the current 
	 * settings.</li>
	 * <li><code><u>model-dir</u></code> - the directory that contains the learned translation model.</li>
	 * <li><code><u>mask-file</u></code> - the example mask that specifies the test set.</li>
	 * <li><code><u>output-file</u></code> - the output XML file for storing the automatically-generated
	 * parses. </li>
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
//		if (args.length != 4) {
//			System.err.println("Usage: java wasp.main.Parser config-file model-dir mask-file output-file");
//			System.err.println();
//			System.err.println("config-file - the configuration file that contains the current settings.");
//			System.err.println("model-dir - the directory that contains the learned translation model.");
//			System.err.println("mask-file - the example mask that specifies the test set.");
//			System.err.println("output-file - the output XML file for storing the automatically-generated parses.");
//			System.exit(1);
//		}
//		String configFilename = args[0];
//		String modelDir = args[1];
//		String maskFilename = args[2];
//		String outputFilename = args[3];
		
		String configFilename = "F:/Germany/lecture/NLP/final project/wasp-1.0/data/geo-funql/config";
		String modelDir = "F:/Germany/lecture/NLP/final project/wasp-1.0/data/geo-funql/model";
		String maskFilename = "F:/Germany/lecture/NLP/final project/wasp-1.0/data/geo-funql/split-250/run-1/fold-1/test";
		String outputFilename = "F:/Germany/lecture/NLP/final project/wasp-1.0/data/geo-funql/model/output-withou-anser-start.xml";
		
		Config.read(configFilename);
		Config.setModelDir(modelDir);
		Examples examples = new Examples();
		examples.read(Config.getCorpusFile());
		ExampleMask mask = new ExampleMask();
		mask.read(maskFilename);
		examples = mask.apply(examples);
		Config.getMRLGrammar().readMore();
		TranslationModel model = TranslationModel.createNew();
		model.read();
		Parser parser = Parser.createNew(model);
		logger.info("Parsing all input sentences");
		for (Iterator it = examples.iterator(); it.hasNext();) {
			Example ex = (Example) it.next();
			logger.fine("example "+ex.id);
			for (Iterator jt = parser.parse(ex.E); jt.hasNext();)
				ex.parses.add(jt.next());
		}
		logger.info("All input sentences have been parsed");
		examples.write(outputFilename);
		
		
//		/////============my parser==========================
//		
//		logger.info("Parsing all input sentences");
//		Writer testRes = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("testRes-withou-anser-start.txt"), "UTF-8"));
//		for (Iterator it = examples.iterator(); it.hasNext();) {
//			Example ex = (Example) it.next();
//			logger.fine("example "+ex.id);
//			String Meanning = (String) ex.F.str;
//			testRes.write("example "+ex.id+"========================begin============================\n");
//			testRes.write(Meanning + "\n");
//			
//			for (Iterator jt = parser.parse(ex.E); jt.hasNext();)
//				ex.parses.add(jt.next());
//			testRes.write("example parses num: "+ex.parses.size()+"\n");
//			
//			Parse[] parses = ex.getSortedParses();
//			boolean hasCorrectTrans = false;
//			for (int j = 0; j < parses.length; ++j) {
//				String str = parses[j].toStr();
//				if (str == null) {
//					logger.finer("example "+ex.id+" parse "+j+" is bad");
//					continue;
//				}
//				logger.finer("example "+ex.id+" parse "+j);
//				//testRes.println("<parse rank=\""+j+"\" score=\""+parses[j].score+"\">");
//				testRes.write(str+"\n");
//				
//				if ( Meanning.trim().replaceAll(" ", "").equals(str.trim().replaceAll(" ", ""))  ){
//					hasCorrectTrans = true;
//				}
//				
//				//testRes.println("</parse>");
////				Node tree = parses[j].toTree();
////				if (tree != null) {
////					out.println("<!--");
////					out.println(tree.toPrettyString());
////					out.println("-->");
////				}
//			}
//			
//			
//				
//				//testRes.write(jt.next() + "\n");
//			if(hasCorrectTrans){
//				testRes.write("Has correct answer!"+"\n");
//			}else{testRes.write("wrong answer!"+"\n");}
//			
//			testRes.write("example "+ex.id+"========================end============================\n");
//		}
//		testRes.close();
//		logger.info("All input sentences have been parsed");
//		examples.write(outputFilename);
//	/////======================================
	}
	
}
