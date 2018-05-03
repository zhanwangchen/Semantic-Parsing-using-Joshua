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
package wasp.align;

import java.io.IOException;
import java.util.Iterator;

import wasp.data.Example;
import wasp.data.Examples;
import wasp.data.Node;
import wasp.data.Terminal;
import wasp.main.Config;
import wasp.util.Mask;
import wasp.util.RadixMap;

/**
 * The abstract class for word alignment models.
 * 
 * @author ywwong
 *
 */
public abstract class WordAlignModel {

	/** The training examples. */
	protected Examples examples;
	/** Masks for removing words that are not to be aligned. */
	protected RadixMap Emasks;
	/** Masks for removing nodes that are not to be aligned. */
	protected RadixMap Fmasks;
	/** Sentences in the training set with certain words removed. */
	protected RadixMap Eshorts;
	/** Linearized MR parses in the training set with certain parse nodes removed. */
	protected RadixMap Fshorts;

	/**
	 * Creates and returns a new word alignment model as specified in the configuration file (via the key
	 * <code>Config.WORD_ALIGN_MODEL</code>).  Currently, two type of word alignment models are
	 * recognized: <code>giza++</code> for the GIZA++ implementation of IBM models, and 
	 * the <code>gold-standard</code> model which uses the word alignments given by the gold-standard
	 * augmented syntactic parse trees.  <code>null</code> is returned if the given model type is not 
	 * recognized.
	 * 
	 * @return a new word alignment model of the type specified in the configuration file.
	 */
	public static WordAlignModel createNew() throws IOException {
		String type = Config.get(Config.WORD_ALIGN_MODEL);
		if (type.equals("giza++"))
			return new GIZAPlusPlus();
		else if (type.equals("gold-standard"))
			return new GoldStandard();
		return null;
	}
	
	/**
	 * Trains the word alignment model.  The best word alignments for each training example are then
	 * obtained and put into the <code>aligns</code> field of the training example.
	 * 
	 * @param examples a set of training examples.
	 */
	public abstract void train(Examples examples) throws IOException;
	
	/**
	 * Initializes the word alignment model for training.
	 */
	protected void init(Examples examples) throws IOException {
		this.examples = examples;
		Emasks = new RadixMap();
		Fmasks = new RadixMap();
		Eshorts = new RadixMap();
		Fshorts = new RadixMap();
		for (Iterator it = examples.iterator(); it.hasNext();) {
			Example ex = (Example) it.next();
			
			// create mask for the NL sentence
			short Elen = (short) ex.E.length;
			Mask Emask = new Mask(Elen);
			for (short i = 0; i < Elen; ++i) {
				if (ex.E[i].isBoundary())
					continue;
				Emask.set(i);
			}
			Emasks.put(ex.id, Emask);
			
			// apply mask
			Terminal[] Eshort = (Terminal[]) Emask.apply(ex.E);
			Eshorts.put(ex.id, Eshort);
			
			// create mask for the linearized MR parse
			short Flen = (short) ex.F.linear.length;
			Mask Fmask = new Mask(Flen);
			for (short i = 0; i < Flen; ++i) {
				if (ex.F.lprods[i].isUnary())
					continue;
				if (Config.getMRLGrammar().isZeroFertility(ex.F.lprods[i]))
					continue;
				Fmask.set(i);
			}
			Fmasks.put(ex.id, Fmask);
			
			// apply mask
			Node[] Fshort = (Node[]) Fmask.apply(ex.F.linear);
			Fshorts.put(ex.id, Fshort);
		}
	}
	
}
