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

import java.io.IOException;

import wasp.data.Examples;
import wasp.scfg.SCFGModel;

/**
 * An abstract class for translation models.
 * 
 * @author ywwong
 *
 */
public abstract class TranslationModel {

	/**
	 * Creates and returns a new translation model of the type specified in the configuration file (via
	 * the key <code>Config.TRANSLATION_MODEL</code>).  Currently, one type of translation model is 
	 * recognized: <code>scfg</code> for synchronous context-free grammars.  <code>null</code> is returned 
	 * if the given model type is not recognized.
	 * 
	 * @return a new translation model of the specified type.
	 * @throws IOException if an I/O error occurs.
	 */
	public static TranslationModel createNew() throws IOException {
		String type = Config.getTranslationModel();
		if (type.equals("scfg"))
			return new SCFGModel();
		return null;
	}
	
	/**
	 * Trains a new translation model using the specified training examples.
	 * 
	 * @param examples a set of training examples.
	 * @throws IOException if an I/O error occurs.
	 */
	public abstract void train(Examples examples) throws IOException;

	/**
	 * Retrieves a previously learned translation model from the directory specified in the configuration
	 * file (via the key <code>Config.MODEL_DIR</code>).
	 * 
	 * @throws IOException if an I/O error occurs.
	 */
	public abstract void read() throws IOException;
	
}
