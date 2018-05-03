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
package wasp.scfg;

import java.io.IOException;

import wasp.align.WordAlignModel;
import wasp.data.Examples;
import wasp.main.TranslationModel;
import wasp.nl.GapModel;
import wasp.scfg.parse.Maxent;

/**
 * Translation models based on synchronous context-free grammars.
 * 
 * @author ywwong
 *
 */
public class SCFGModel extends TranslationModel {

	public SCFG gram;
	public GapModel gm;
	
	public SCFGModel() throws IOException {
		gram = new SCFG();
		gm = GapModel.createNew();
	}
	
	public void train(Examples examples) throws IOException {
		gram.readInit();
		WordAlignModel.createNew().train(examples);
		new RuleExtractor().extract(gram, examples);
		Maxent maxent = new Maxent(gram, gm);
		maxent.estimate(examples);
		gram.write();
		gm.write();
	}
	
	public void read() throws IOException {
		gram.read();
		gm.read();
	}
	
}
