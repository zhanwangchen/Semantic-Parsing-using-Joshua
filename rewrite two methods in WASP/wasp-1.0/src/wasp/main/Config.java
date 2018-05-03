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

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Properties;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import wasp.mrl.MRLGrammar;
import wasp.util.Int;

/**
 * Settings that control the behavior of WASP programs.  Settings are stored in Java property lists.
 * This class provides methods that allow easy access to the most basic settings.  Other task-specific,
 * model-specific and domain-specific settings can be obtained through the <code>get</code> method.  Keys
 * for these settings are provided as publicly-accessible string constants.
 * 
 * @author ywwong
 *
 */
public class Config {

	// set up the loggers
	static {
		Logger root = Logger.getLogger("");
		Handler handler = new ConsoleHandler();
		handler.setLevel(Level.FINEST);
		handler.setFormatter(new Formatter() {
			public String format(LogRecord record) {
				return record.getMessage()+'\n';
			}
		});
		root.addHandler(handler);
		root.setLevel(Level.FINER);
	}
	
	/** The current settings. */
	private static Config config;
	
	/** The property list that stores all key-value pairs specified in the configuration file. */
	private Properties props;
	/** The MRL grammar. */
	private MRLGrammar mrlGram;
	
	private Config() {}

	private void init() throws IOException {
		config.mrlGram = MRLGrammar.createNew();
		config.mrlGram.read();
	}
	
	/**
	 * Searches for the specified property in the current property list.  This method returns 
	 * <code>null</code> if the property is not found.
	 * 
	 * @param key the property to look for.
	 * @return the value of the specified property in the current property list.
	 */
	public static String get(String key) {
		return config.props.getProperty(key);
	}
	
	/**
	 * Sets the value of the specified property in the current property list.
	 *  
	 * @param key the property to set.
	 * @param value the property value.
	 */
	public static void set(String key, String value) {
		config.props.setProperty(key, value);
	}
	
	/**
	 * Returns the identifier of the current natural language.  This is specified in the configuration
	 * file via the key <code>wasp.nl</code>.
	 * 
	 * @return the identifier of the current natural language.
	 */
	public static String getNL() {
		return get(NL);
	}
	
	/**
	 * Returns the identifier of the current meaning-representation language.  This is specified in the
	 * configuration file via the key <code>wasp.mrl</code>.
	 * 
	 * @return the identifier of the current meaning-representation language.
	 */
	public static String getMRL() {
		return get(MRL);
	}

	/**
	 * Returns the current MRL grammar.  The MRL grammar is read from the file specified in the
	 * configuration file via the key <code>wasp.mrl.grammar</code>.
	 * 
	 * @return the current MRL grammar.
	 */
	public static MRLGrammar getMRLGrammar() {
		return config.mrlGram;
	}

	/**
	 * Returns the maximum number of top-scoring parses to return during testing.  The actual number of 
	 * parses returned can be more if there are ties.  This number is specified in the configuration file
	 * via the key <code>wasp.kbest</code>.
	 * 
	 * @return the maximum number of top-scoring parses to return during testing.
	 */
	public static int getKBest() {
		return Int.parseInt(get(K_BEST));
	}
	
	/**
	 * Returns the type of the current NL language model.  This is specified in the configuration file
	 * file via the key <code>wasp.nl.model</code>.
	 * 
	 * @return the type of the current NL language model.
	 */
	public static String getNLModel() {
		return get(NL_MODEL);
	}
	
	/**
	 * Returns the type of the current translation model.  This is specified in the configuration file via
	 * the key <code>wasp.translation.model</code>.
	 * 
	 * @return the type of the current translation model.
	 */
	public static String getTranslationModel() {
		return get(TRANSLATION_MODEL);
	}
	
	/**
	 * Returns the name of the directory in which the current translation model and language model are
	 * stored.  This is specified in the configuration file via the key <code>wasp.model.dir</code>.
	 * 
	 * @return the name of the directory for the models.
	 */
	public static String getModelDir() {
		return get(MODEL_DIR);
	}
	
	/**
	 * Sets the name of the directory in which the current translation model and language model are
	 * stored.  This overrides any setting in the configuration file.
	 * 
	 * @param modelDir the name of the directory for the models.
	 */
	public static void setModelDir(String modelDir) {
		set(MODEL_DIR, modelDir);
	}

	/**
	 * Returns the name of the file that contains the entire corpus.  This corpus will be used for
	 * training and testing.  This is specified in the configuration file via the key
	 * <code>wasp.corpus.file</code>.
	 * 
	 * @return the name of the file that contains the entire corpus.
	 */
	public static String getCorpusFile() {
		return get(CORPUS_FILE);
	}
	
	///
	/// Key constants
	///
	
	/** The key to the current natural language.  Recognized languages are: <code>en</code> for English, 
	 * <code>es</code> for Spanish, <code>ja</code> for Japanese, and <code>tr</code> for Turkish. */
	public static final String NL = "wasp.nl";
	
	/** The key to the current meaning-representation langauge.  Recognized languages are: 
	 * <code>geo-funql</code> for the functional query language in the Geoquery domain, and
	 * <code>robocup-clang</code> for CLang, the coach language in the RoboCup domain. */
	public static final String MRL = "wasp.mrl";
	
	/** The key to the name of the file that contains the productions of the current MRL grammar. */
	public static final String MRL_GRAMMAR = "wasp.mrl.grammar";

	/** The key to the maximum number of top-scoring parses to return during testing.  The actual number 
	 * of parses returned can be more if there are ties. */
	public static final String K_BEST = "wasp.kbest";
	
	/** The key to the name of the directory in which the current translation model and language model
	 * are stored. */
	public static final String MODEL_DIR = "wasp.model.dir";
	
	/** The key to the name of the file that contains the entire corpus for both training and testing. */
	public static final String CORPUS_FILE = "wasp.corpus.file";
	
	/** The key to the current word alignment model.  Recognized models are: <code>giza++</code> for the 
	 * GIZA++ implementation of the IBM models, and <code>gold-standard</code> which uses the word
	 * alignments given by the gold-standard augmented syntactic parses. */
	public static final String WORD_ALIGN_MODEL = "wasp.align.model";
	
	/** The key to the absolute pathname of the GIZA++ executable file. */
	public static final String GIZAPP_EXEC = "wasp.align.giza++.exec";

	/** The key to the current NL language model. */
	public static final String NL_MODEL = "wasp.nl.model";
	
	/** The key to the current translation model.  The only recognized model is: <code>scfg</code> for 
	 * synchronous context-free grammars. */
	public static final String TRANSLATION_MODEL = "wasp.translation.model";
	
	/** The key to the name of the file that contains the initial rules for training an SCFG. */
	public static final String SCFG_INIT = "wasp.scfg.init";
	
	/** The key to the name of the file that contains the names of all geographical entities in the 
	 * Geoquery domain. */
	public static final String GEO_NAMES = "wasp.domain.geo.names";
	
	/** The key to the name of the directory that contains the evaluation scripts for the Geoquery
	 * domain. */
	public static final String GEO_EVAL_DIR = "wasp.domain.geo.eval.dir";
	
	/** The key to the absolute pathname of the SICSTUS executable file, necessary for the Geoquery
	 * evaluation scripts. */
	public static final String SICSTUS_EXEC = "wasp.sicstus.exec";
	
	///
	/// Configuration files
	///
	
	/**
	 * Reads the specified configuration file and updates the current settings of WASP.
	 * 
	 * @param filename the name of the configuration file.
	 * @throws IOException if an I/O error occurs.
	 */
	public static void read(String filename) throws IOException {
		BufferedInputStream in = new BufferedInputStream(new FileInputStream(filename));
		config = new Config();
		config.props = new Properties();
		config.props.load(in);
		config.print(System.err);
		config.init();
	}
	
	/**
	 * Prints the settings stored in this <code>Config</code> object to the specified output stream.
	 * 
	 * @param out the output stream to write to.
	 * @throws IOException if an I/O error occurs.
	 */
	public void print(PrintStream out) throws IOException {
		props.list(out);
	}
	
	/**
	 * Prints the settings stored in this <code>Config</code> object to the specified character stream.
	 * 
	 * @param out the character stream to write to.
	 * @throws IOException if an I/O error occurs.
	 */
	public void print(PrintWriter out) throws IOException {
		props.list(out);
	}
	
}
