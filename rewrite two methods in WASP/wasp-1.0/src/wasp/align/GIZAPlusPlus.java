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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import wasp.data.Anaphor;
import wasp.data.Dictionary;
import wasp.data.Example;
import wasp.data.Examples;
import wasp.data.Meaning;
import wasp.data.Node;
import wasp.data.Symbol;
import wasp.data.Terminal;
import wasp.data.Tree;
import wasp.main.Config;
import wasp.mrl.Production;
import wasp.mrl.ProductionSymbol;
import wasp.scfg.Rule;
import wasp.scfg.SCFG;
import wasp.util.Arrays;
import wasp.util.InputStreamWriter;
import wasp.util.Int;
import wasp.util.Mask;
import wasp.util.Numberer;
import wasp.util.RadixMap;
import wasp.util.Double;
import wasp.util.Short;

/**
 * A wrapper for GIZA++, Franz Och's implementation of the IBM Models.
 * 
 * @author ywwong
 *
 */
public class GIZAPlusPlus extends WordAlignModel {

	private static final int NBEST = 10;
	private static final double MUST_LINK_THRESHOLD = 0.9;
	
	private static Logger logger = Logger.getLogger(GIZAPlusPlus.class.getName());
	
	private static class Settings {
	    public String[][] config = {
	            {"hmmiterations", "0"},
	            {"model1iterations", "5"},
	            {"model2iterations", "5"},
	            {"model3iterations", "3"},
	            {"model4iterations", "3"},
	            {"model5iterations", "3"},
	            {"pegging", "1"},
	            {"nbestalignments", Int.toString(NBEST)},
	            {"compactadtable", "0"}
	    };
	    public String[][] revConfig = {
	            {"hmmiterations", "0"},
	            {"model1iterations", "5"},
	            {"model2iterations", "5"},
	            {"model3iterations", "3"},
	            {"model4iterations", "0"},
	            {"model5iterations", "0"},
	            {"compactadtable", "0"}
	    };
	    public File execFile;
	    public File tmpDir;
	    public String prefix;
	    public File srcVocabFile;
	    public File tarVocabFile;
	    public File dictFile;
	    public File dictRevFile;
	    public File sentFile;
	    public File sentRevFile;
	    public File configFile;
	    public File tTableFile;
	    public File tActualTableFile;
	    public File tRevTableFile;
	    public File tiTableFile;
	    public File tiActualTableFile;
	    public File tiRevTableFile;
	    public File nTableFile;
	    public File nActualTableFile;
	    public File p0TableFile;
	    public File aTableFile;
	    public File aRevTableFile;
	    public File d3TableFile;
	    public File d4TableFile;
	    public File d5TableFile;
	    public File alignFile;
	    public File alignNBestFile;
	    public File alignRevFile;
	    public Settings() {
	        execFile = new File(Config.get(Config.GIZAPP_EXEC));
	        tmpDir = new File(System.getProperty("java.io.tmpdir"));
	        prefix = "giza++";
	        String suffix = ".final";
	        tTableFile = new File(tmpDir, prefix+".t3"+suffix);
	        tActualTableFile = new File(tmpDir, prefix+".actual.t3"+suffix);
	        tRevTableFile = new File(tmpDir, prefix+".t3"+suffix);
	        tiTableFile = new File(tmpDir, prefix+".ti"+suffix);
	        tiActualTableFile = new File(tmpDir, prefix+".actual.ti"+suffix);
	        tiRevTableFile = new File(tmpDir, prefix+".ti"+suffix);
	        nTableFile = new File(tmpDir, prefix+".n3"+suffix);
	        nActualTableFile = new File(tmpDir, prefix+".actual.n3"+suffix);
	        p0TableFile = new File(tmpDir, prefix+".p0_3"+suffix);
	        aTableFile = new File(tmpDir, prefix+".a3"+suffix);
	        aRevTableFile = new File(tmpDir, prefix+".a3"+suffix);
	        d3TableFile = new File(tmpDir, prefix+".d3"+suffix);
	        d4TableFile = new File(tmpDir, prefix+".d4"+suffix);
	        d5TableFile = new File(tmpDir, prefix+".d5"+suffix);
	        alignFile = new File(tmpDir, prefix+".A3"+suffix);
	        alignNBestFile = new File(tmpDir, prefix+".A3"+suffix+"NBEST");
	        alignRevFile = new File(tmpDir, prefix+".A3"+suffix);
	        tTableFile.deleteOnExit();
	        tActualTableFile.deleteOnExit();
	        tRevTableFile.deleteOnExit();
	        tiTableFile.deleteOnExit();
	        tiActualTableFile.deleteOnExit();
	        tiRevTableFile.deleteOnExit();
	        nTableFile.deleteOnExit();
	        nActualTableFile.deleteOnExit();
	        p0TableFile.deleteOnExit();
	        aTableFile.deleteOnExit();
	        aRevTableFile.deleteOnExit();
	        d3TableFile.deleteOnExit();
	        d4TableFile.deleteOnExit();
	        d5TableFile.deleteOnExit();
	        alignFile.deleteOnExit();
	        alignNBestFile.deleteOnExit();
	        alignRevFile.deleteOnExit();
	    }
	    public void createTempFiles() throws IOException {
	        srcVocabFile = File.createTempFile(prefix, ".vcb", tmpDir);
	        tarVocabFile = File.createTempFile(prefix, ".vcb", tmpDir);
	        dictFile = File.createTempFile(prefix, ".dct", tmpDir);
	        dictRevFile = File.createTempFile(prefix, ".reverse.dct", tmpDir);
	        sentFile = File.createTempFile(prefix, ".snt", tmpDir);
	        sentRevFile = File.createTempFile(prefix, ".reverse.snt", tmpDir);
	        configFile = File.createTempFile(prefix, ".cfg", tmpDir);
	        srcVocabFile.deleteOnExit();
	        tarVocabFile.deleteOnExit();
	        dictFile.deleteOnExit();
	        dictRevFile.deleteOnExit();
	        sentFile.deleteOnExit();
	        sentRevFile.deleteOnExit();
	        configFile.deleteOnExit();
	    }
	    public String getCmd() {
	        return execFile.getPath()+" "+configFile.getPath();
	    }
	}

	private static class TranslationTable {
	    private Vocabulary vocab1;
	    private Vocabulary vocab2;
	    private double[][] table;
	    public TranslationTable(Vocabulary vocab1, Vocabulary vocab2) {
	        this.vocab1 = vocab1;
	        this.vocab2 = vocab2;
	        table = new double[vocab1.getNextId()][vocab2.getNextId()];
	    }
	    public double getProb(String s1, String s2) {
	        int id1 = vocab1.getId(s1, false);
	        int id2 = vocab2.getId(s2, false);
	        if (id1 >= 0 && id2 >= 0)
	            return table[id1][id2];
	        else
	            return 0;
	    }
	    public void setProb(int id1, int id2, double prob) {
	        table[id1][id2] = prob;
	    }
	}

	private static class Vocabulary extends Numberer {
		private static final int FIRST_ID = 2;
		private RadixMap counts;
		public Vocabulary() {
			super(FIRST_ID);
			counts = new RadixMap();
		}
		public int getId(Object o, boolean add) {
			int id = super.getId(o, add);
			Int count = (Int) counts.get(id);
			if (count == null)
				counts.put(id, new Int(1));
			else
				++count.val;
			return id;
		}
		public void write(PrintWriter out) {
			for (int i = FIRST_ID; i < getNextId(); ++i) {
				out.print(i);
				out.print(' ');
				out.print(getObj(i));
				out.print(' ');
				out.println(counts.get(i));
			}
		}
	}
	
	private static class DistortionTable {
	    private static final int INC = 2;
	    private double[][][][] table;
	    public DistortionTable() {
	        table = null;
	    }
	    public double getProb(int j, int i, int l, int m) {
	        if (table != null && table.length > j
	            && table[j] != null && table[j].length > i
	            && table[j][i] != null && table[j][i].length > l
	            && table[j][i][l] != null && table[j][i][l].length > m)
	            return table[j][i][l][m];
	        else
	            return 0;
	    }
	    public void setProb(int j, int i, int l, int m, double prob) {
	        if (table == null)
	            table = new double[((j/INC)+1)*INC][][][];
	        else if (table.length <= j)
	            table = (double[][][][]) Arrays.resize(table, j+INC);
	        if (table[j] == null)
	            table[j] = new double[((i/INC)+1)*INC][][];
	        else if (table[j].length <= i)
	            table[j] = (double[][][]) Arrays.resize(table[j], i+INC);
	        if (table[j][i] == null)
	            table[j][i] = new double[((l/INC)+1)*INC][];
	        else if (table[j][i].length <= l)
	            table[j][i] = (double[][]) Arrays.resize(table[j][i], l+INC);
	        if (table[j][i][l] == null)
	            table[j][i][l] = new double[((m/INC)+1)*INC];
	        else if (table[j][i][l].length <= m)
	            table[j][i][l] = Arrays.resize(table[j][i][l], m+INC);
	        table[j][i][l][m] = prob;
	    }
	}

	private Settings s;
	private Vocabulary srcVocab;
	private Vocabulary tarVocab;
	private RadixMap Ecombs;
	private RadixMap Eshorters;
	private RadixMap initAligns;
	private RadixMap revAligns;
	private RadixMap aligns;

	public GIZAPlusPlus() throws IOException {
		s = new Settings();
		s.createTempFiles();
	}
		
	public void train(Examples examples) throws IOException {
		logger.info("Finding the best word alignments");
		init(examples);
		applyInitRules();
		writeGizaInput();
		//runGizaReverse();
		readGizaOutputReverse();
		runGiza();
		readGizaOutput();
		fixAligns();
		if (createAnaphora()) {
			init(examples);
			applyInitRules();
			writeGizaInput();
			runGizaReverse();
			readGizaOutputReverse();
			runGiza();
			readGizaOutput();
			fixAligns();
		}
		storeAligns();
		logger.info("Word alignments have been found");
	}
	
	protected void init(Examples examples) throws IOException {
		super.init(examples);
		srcVocab = new Vocabulary();
		tarVocab = new Vocabulary();
		Ecombs = new RadixMap();
		Eshorters = new RadixMap();
		initAligns = new RadixMap();
		revAligns = new RadixMap();
		aligns = new RadixMap();
	}
	
	private void applyInitRules() throws IOException {
		// the initial rules are SCFG initial rules
		SCFG gram = new SCFG();
		gram.readInit();
		for (Iterator it = examples.iterator(); it.hasNext();) {
			Example ex = (Example) it.next();
			Terminal[] E = (Terminal[]) Eshorts.get(ex.id);
			NToNWordAlign initAlign = new NToNWordAlign(E, ex.F.linear);
			ArrayList Ecomb = new ArrayList();
			initAlign = applyInitRules(gram, initAlign, Ecomb);
			Ecombs.put(ex.id, Ecomb);
			Eshorters.put(ex.id, initAlign.getE());
			initAligns.put(ex.id, initAlign);
			logger.finest("example "+ex.id);
			logger.finest(initAlign.toString());
		}
	}
	
	private NToNWordAlign applyInitRules(SCFG gram, NToNWordAlign align, ArrayList Ecomb) {
		Rule[] rules = gram.getRules();
		// rules with longer patterns are applied first
		Arrays.sort(rules, new Comparator() {
			public int compare(Object e1, Object e2) {
				if (((Rule) e1).lengthE() > ((Rule) e2).lengthE())
					return -1;
				else if (((Rule) e1).lengthE() < ((Rule) e2).lengthE())
					return 1;
				else
					return 0;
			}
		});
		for (int i = 0; i < rules.length; ++i) {
			for (short j = 0; j < align.lengthE(); ++j) {
				Rule rule = rules[i];
				if (rule.hasArgs())
					continue;
				boolean matchE = true;
				for (short k = 0, l = j; k < rule.lengthE(); ++k, ++l)
					if (l >= align.lengthE() || !rule.getE(k).matches(align.getE(l))) {
						matchE = false;
						break;
					}
				if (!matchE)
					continue;
				boolean[] matchF = new boolean[align.lengthF()];
				if (rule.isWildcard())
					rule = new Rule(rule, (Terminal) align.getE(j));
				for (short k = 0; k < align.lengthF(); ++k) {
					Production p = ((ProductionSymbol) align.getF(k).getSymbol()).getProduction();
					matchF[k] = p.equals(rule.getProduction());
				}
				if (Arrays.count(matchF) == 0)
					continue;
				if (rule.lengthE() > 1)
					align = align.combineE(j, rule.lengthE(), Ecomb);
				for (short k = 0; k < align.lengthF(); ++k)
					if (matchF[k])
						align.addLink(j, k);
			}
		}
		return align;
	}
	
    private void writeGizaInput() throws IOException {
        PrintWriter srcVocabOut = new PrintWriter(new BufferedWriter(new FileWriter(s.srcVocabFile)));
        PrintWriter tarVocabOut = new PrintWriter(new BufferedWriter(new FileWriter(s.tarVocabFile)));
        PrintWriter sentOut = new PrintWriter(new BufferedWriter(new FileWriter(s.sentFile)));
        PrintWriter revSentOut = new PrintWriter(new BufferedWriter(new FileWriter(s.sentRevFile)));
        for (Iterator it = examples.iterator(); it.hasNext();) {
            Example ex = (Example) it.next();
            Symbol[] E = (Symbol[]) Eshorters.get(ex.id);
            Node[] F = (Node[]) Fshorts.get(ex.id);
            // source is F
            StringBuffer srcSb = new StringBuffer();
            for (short j = 0; j < F.length; ++j) {
                int sid = srcVocab.getId(F[j].getSymbol().toString(), true);
                srcSb.append(sid);
                srcSb.append(' ');
            }
            // target is E
            StringBuffer tarSb = new StringBuffer();
            for (short j = 0; j < E.length; ++j) {
                int tid = tarVocab.getId(Dictionary.term(E[j].getId()), true);
                tarSb.append(tid);
                tarSb.append(' ');
            }
            sentOut.println('1');
            sentOut.println(srcSb.toString());
            sentOut.println(tarSb.toString());
            revSentOut.println('1');
            revSentOut.println(tarSb.toString());
            revSentOut.println(srcSb.toString());
        }
        srcVocab.write(srcVocabOut);
        tarVocab.write(tarVocabOut);
        srcVocabOut.close();
        tarVocabOut.close();
        sentOut.close();
        revSentOut.close();
    }

    private void runGizaReverse() throws IOException {
        PrintWriter configOut = new PrintWriter(new BufferedWriter(new FileWriter(s.configFile)));
        for (int i = 0; i < s.revConfig.length; ++i) {
            configOut.print(s.revConfig[i][0]);
            configOut.print(' ');
            configOut.println(s.revConfig[i][1]);
        }
        configOut.print("s ");
        configOut.println(s.tarVocabFile.getPath());
        configOut.print("t ");
        configOut.println(s.srcVocabFile.getPath());
        configOut.print("c ");
        configOut.println(s.sentRevFile.getPath());
        configOut.print("o ");
        configOut.println(s.prefix);
        configOut.print("outputpath ");
        configOut.println(s.tmpDir.getPath());
        configOut.close();
        try {
        	logger.info("GIZA++ starts");
        	logger.info(s.getCmd());
            Process proc = Runtime.getRuntime().exec(s.getCmd());
            new InputStreamWriter(proc.getInputStream(), System.err).start();
            new InputStreamWriter(proc.getErrorStream(), System.err).start();
            int exitVal = proc.waitFor();
            if (exitVal != 0) {
            	logger.severe("GIZA++ terminates abnormally");
                throw new RuntimeException();
            }
            logger.info("GIZA++ ends");
        } catch (InterruptedException e) {}
    }
    
    private void readGizaOutputReverse() throws IOException {
        BufferedReader tTableIn = new BufferedReader(new FileReader(s.tRevTableFile));
        BufferedReader tiTableIn = new BufferedReader(new FileReader(s.tiRevTableFile));
        BufferedReader alignIn = new BufferedReader(new FileReader(s.alignRevFile));
        TranslationTable tTable = new TranslationTable(tarVocab, srcVocab);
        String s;
        while ((s = tTableIn.readLine()) != null) {
            StringTokenizer toks = new StringTokenizer(s);
            int tid = Int.parseInt(toks.nextToken());
            int sid = Int.parseInt(toks.nextToken());
            double prob = Double.parseDouble(toks.nextToken());
            tTable.setProb(tid, sid, prob);
        }
        TranslationTable tiTable = new TranslationTable(srcVocab, tarVocab);
        while ((s = tiTableIn.readLine()) != null) {
            StringTokenizer toks = new StringTokenizer(s);
            int sid = Int.parseInt(toks.nextToken());
            int tid = Int.parseInt(toks.nextToken());
            double prob = Double.parseDouble(toks.nextToken());
            tiTable.setProb(sid, tid, prob);
        }
        while ((s = alignIn.readLine()) != null) {
            Example ex = null;
            Mask Fmask = null;
            Symbol[] E = null;
            Node[] F = null;
            NToNWordAlign align = null;
            StringTokenizer toks = new StringTokenizer(s);
            while (toks.hasMoreTokens()) {
                String tok = toks.nextToken();
                if (tok.equals("pair")) {
                    tok = toks.nextToken();
                    tok = tok.substring(1, tok.length()-1);
                    ex = examples.getNth(Int.parseInt(tok)-1);
                    Fmask = (Mask) Fmasks.get(ex.id);
                    E = (Symbol[]) Eshorters.get(ex.id);
                    F = (Node[]) Fshorts.get(ex.id);
                } else if (tok.equals("score")) {
                    toks.nextToken();
                    tok = toks.nextToken(); // score
                    align = new NToNWordAlign(E, F);
                }
            }
            alignIn.readLine();
            s = alignIn.readLine();
            toks = new StringTokenizer(s);
            for (short i = 0; i <= E.length; ++i) {
                short e = -1;
                String word = null;
                if (i > 0) {
                	e = (short) (i-1);
                	word = Dictionary.term(E[e].getId());
                }
                String tok;
                toks.nextToken();
                toks.nextToken();  // ({
                while (!(tok = toks.nextToken()).equals("})"))
                    if (i > 0) {
                        short j = (short) Int.parseInt(tok);
                        short f = (short) (j-1);
                        String prod = F[f].getSymbol().toString();
                        double t = tTable.getProb(word, prod);
                        //double ti = tiTable.getProb(prod, word);
                        align.addLink(e, f, t);
                    }
            }
            logger.finest("example "+ex.id);
            logger.finest(align.toString());
            revAligns.put(ex.id, align.unmaskF(ex.F.linear, Fmask));
            logger.finest("unmask");
            logger.finest(revAligns.get(ex.id).toString());
        }
        tTableIn.close();
        tiTableIn.close();
        alignIn.close();
    }

    private void runGiza() throws IOException {
        PrintWriter configOut = new PrintWriter(new BufferedWriter(new FileWriter(s.configFile)));
        for (int i = 0; i < s.config.length; ++i) {
            configOut.print(s.config[i][0]);
            configOut.print(' ');
            configOut.println(s.config[i][1]);
        }
        configOut.print("s ");
        configOut.println(s.srcVocabFile.getPath());
        configOut.print("t ");
        configOut.println(s.tarVocabFile.getPath());
        configOut.print("c ");
        configOut.println(s.sentFile.getPath());
        configOut.print("o ");
        configOut.println(s.prefix);
        configOut.print("outputpath ");
        configOut.println(s.tmpDir.getPath());
        configOut.close();
        try {
        	logger.info("GIZA++ starts");
            Process proc = Runtime.getRuntime().exec(s.getCmd());
            new InputStreamWriter(proc.getInputStream(), System.err).start();
            new InputStreamWriter(proc.getErrorStream(), System.err).start();
            int exitVal = proc.waitFor();
            if (exitVal != 0) {
            	logger.severe("GIZA++ terminates abnormally");
                throw new RuntimeException();
            }
            logger.info("GIZA++ ends");
        } catch (InterruptedException e) {}
    }
    
    private void readGizaOutput() throws IOException {
        BufferedReader tTableIn = new BufferedReader(new FileReader(s.tTableFile));
        BufferedReader tiTableIn = new BufferedReader(new FileReader(s.tiTableFile));
        BufferedReader d3TableIn = new BufferedReader(new FileReader(s.d3TableFile));
        BufferedReader alignNBestIn = new BufferedReader(new FileReader(s.alignNBestFile));
        TranslationTable tTable = new TranslationTable(srcVocab, tarVocab);
        String s;
        while ((s = tTableIn.readLine()) != null) {
            StringTokenizer toks = new StringTokenizer(s);
            int sid = Int.parseInt(toks.nextToken());
            int tid = Int.parseInt(toks.nextToken());
            double prob = Double.parseDouble(toks.nextToken());
            tTable.setProb(sid, tid, prob);
        }
        TranslationTable tiTable = new TranslationTable(tarVocab, srcVocab);
        while ((s = tiTableIn.readLine()) != null) {
            StringTokenizer toks = new StringTokenizer(s);
            int tid = Int.parseInt(toks.nextToken());
            int sid = Int.parseInt(toks.nextToken());
            double prob = Double.parseDouble(toks.nextToken());
            tiTable.setProb(tid, sid, prob);
        }
        DistortionTable dTable = new DistortionTable();
        Stack stack = new Stack();
        while ((s = d3TableIn.readLine()) != null)
            stack.push(s);
        while (!stack.empty()) {
            s = (String) stack.pop();
            StringTokenizer toks = new StringTokenizer(s);
            int j = Int.parseInt(toks.nextToken());
            int i = Int.parseInt(toks.nextToken());
            int l = Int.parseInt(toks.nextToken());
            int m = Int.parseInt(toks.nextToken());
            double prob = Double.parseDouble(toks.nextToken());
            dTable.setProb(j, i, l, m, prob);
        }
        while ((s = alignNBestIn.readLine()) != null) {
            Example ex = null;
            Mask Fmask = null;
            Symbol[] E = null;
            Node[] F = null;
            //int l = 0;
            //int m = 0;
            NTo1WordAlign align = null;
            StringTokenizer toks = new StringTokenizer(s);
            while (toks.hasMoreTokens()) {
                String tok = toks.nextToken();
                if (tok.equals("pair")) {
                    tok = toks.nextToken();
                    tok = tok.substring(1, tok.length()-1);
                    ex = examples.getNth(Int.parseInt(tok)-1);
                    Fmask = (Mask) Fmasks.get(ex.id);
                    E = (Symbol[]) Eshorters.get(ex.id);
                    F = (Node[]) Fshorts.get(ex.id);
                    //l = F.length;
                    //m = E.length;
                } else if (tok.equals("score")) {
                    toks.nextToken();
                    tok = toks.nextToken();
                    double score = Double.parseDouble(tok);
                    align = new NTo1WordAlign(E, F, score);
                }
            }
            alignNBestIn.readLine();
            s = alignNBestIn.readLine();
            toks = new StringTokenizer(s);
            for (short i = 0; i <= F.length; ++i) {
                short f = -1;
                String prod = null;
                if (i > 0) {
                    f = (short) (i-1);
                    prod = F[f].getSymbol().toString();
                }
                String tok;
                toks.nextToken();
                toks.nextToken();  // ({
                while (!(tok = toks.nextToken()).equals("})"))
                    if (i > 0) {
                        short j = (short) Int.parseInt(tok);
                        short e = (short) (j-1);
                        String word = Dictionary.term(E[e].getId());
                        double ti = tiTable.getProb(word, prod);
                        //double t = tTable.getProb(prod, word);
                        //double d = dTable.getProb(j, i, l, m);
                        align.addLink(e, f, ti);
                    }
            }
            ArrayList list = (ArrayList) aligns.get(ex.id);
            if (list == null) {
                list = new ArrayList();
                aligns.put(ex.id, list);
            }
            logger.finest("example "+ex.id);
            logger.finest(align.toString());
            list.add(align.unmaskF(ex.F.linear, Fmask));
            logger.finest("unmask");
            logger.finest(list.get(list.size()-1).toString());
        }
        tTableIn.close();
        tiTableIn.close();
        d3TableIn.close();
        alignNBestIn.close();
    }

    private void fixAligns() {
    	for (Iterator it = examples.iterator(); it.hasNext();) {
    		Example ex = (Example) it.next();
    		NToNWordAlign initAlign = (NToNWordAlign) initAligns.get(ex.id);
    		NToNWordAlign revAlign = (NToNWordAlign) revAligns.get(ex.id);
    		ArrayList list = (ArrayList) aligns.get(ex.id);
    		for (Iterator jt = list.iterator(); jt.hasNext();) {
    			NTo1WordAlign align = (NTo1WordAlign) jt.next();
        		logger.finest("example "+ex.id);
        		logger.finest(align.toString());
    			Symbol[] E = align.getE();
    			Node[] F = align.getF();

    			// remove links that are not consistent with initial alignment
    			boolean doLog = false;
		        for (short k = 0; k < E.length; ++k) {
		            if (!initAlign.isLinkedFromE(k))
		                continue;
		            Link link = align.getFirstLinkFromE(k);
		            if (link != null) {
		                if (initAlign.isLinked(link.e, link.f))
		                    link.strength = 1;
		                else {
		                    align.removeLink(link);
		                    doLog = true;
		                }
		            }
		        }
		        for (short k = 0; k < F.length; ++k) {
		            if (!initAlign.isLinkedFromF(k))
		                continue;
		            Link link = align.getFirstLinkFromF(k);
		            for (; link != null; link = link.next) {
		                if (initAlign.isLinked(link.e, link.f))
		                    link.strength = 1;
		                else {
		                    align.removeLink(link);
		                    doLog = true;
		                }
		            }
		        }
		        if (doLog) {
		            logger.finest("remove links inconsistent with initial alignment");
		            logger.finest(align.toString());
		        }
		
		        // add links from initial alignment if necessary
		        doLog = false;
		        for (short k = (short) (F.length-1); k >= 0; --k) {
		            if (align.isLinkedFromF(k))
		                continue;
		            Link link = initAlign.getFirstLinkFromF(k);
		            if (link == null)
		            	continue;
		            short min = Short.MAX_VALUE;
		            Link addLink = null;
		            for (; link != null; link = link.next) {
		                if (align.isLinkedFromE(link.e))
		                    continue;
		                align.addLink(link);
		                short violate = totalViolate(align);
		                if (min > violate) {
		                    min = violate;
		                    addLink = link;
		                }
		                align.removeLink(link);
		            }
		            if (addLink != null) {
		                align.addLink(addLink);
		                doLog = true;
		            }
		        }
		        if (doLog) {
		            logger.finest("add links from initial alignment");
		            logger.finest(align.toString());
		        }
		
		        // remove links that violate compositionality
		        doLog = false;
		        while (true) {
		            short violate = totalViolate(align);
		            short maxDiff = 0;
		            Link removeLink = null;
		            for (short k = 0; k < E.length; ++k) {
		                Link link = align.getFirstLinkFromE(k);
		                if (link == null)
		                    continue;
		                if (link.strength >= MUST_LINK_THRESHOLD)
		                    continue;
		                if (align.countLinksFromF(link.f) == 1)
		                    continue;
		                align.removeLink(link);
		                short diff = (short) (violate-totalViolate(align));
		                if (diff > 0) {
		                    if (maxDiff < diff) {
		                        maxDiff = diff;
		                        removeLink = link;
		                    } else if (maxDiff == diff && removeLink.strength > link.strength)
		                        removeLink = link;
		                }
		                align.addLink(link);
		            }
		            if (removeLink == null)
		                break;
		            else {
		                align.removeLink(removeLink);
		                doLog = true;
		            }
		        }
		        if (doLog) {
		            logger.finest("remove links violating compositionality");
		            logger.finest(align.toString());
		        }
		
		        // add links from reverse alignment if not violating compositionality
		        doLog = false;
		        for (short k = (short) (F.length-1); k >= 0; --k) {
		            if (align.isLinkedFromF(k))
		                continue;
		            Link link = revAlign.getFirstLinkFromF(k);
		            if (link == null)
		            	continue;
		            if (align.isLinkedFromE(link.e))
		                continue;
		            short violate = totalViolate(align);
		            align.addLink(link);
		            if (totalViolate(align) == violate)
		                doLog = true;
		            else
		                align.removeLink(link);
		        }
		        if (doLog) {
		        	logger.finest("add links from reverse alignment");
		        	logger.finest(align.toString());
		        }
    		}
    	}
    }

    private short totalViolate(NTo1WordAlign align) {
    	Node[] F = align.getF();
        short[][] spans = getSpans(align);
        short total = 0;
        for (short i = 0; i < F.length; ++i)
            if (spans[i] != null)
                total += violate(align, i, spans[i]);
        return total;
    }

    private short[][] getSpans(NTo1WordAlign align) {
    	Node[] F = align.getF();
        short[][] spans = new short[F.length][];
        for (short i = (short) (F.length-1); i >= 0; --i) {
        	short[] span = new short[2];
            span[0] = Short.MAX_VALUE;
            span[1] = 0;
            for (Link link = align.getFirstLinkFromF(i); link != null; link = link.next) {
            	if (span[0] > link.e)
            		span[0] = link.e;
            	if (span[1] < (short) (link.e+1))
            		span[1] = (short) (link.e+1);
            }
            short nc = F[i].countChildren();
            for (short j = 0; j < nc; ++j) {
            	short k = (short) Arrays.indexOf(F, F[i].getChild(j));
            	if (spans[k] != null) {
            		if (span[0] > spans[k][0])
            			span[0] = spans[k][0];
            		if (span[1] < spans[k][1])
            			span[1] = spans[k][1];
            	}
            }
            if (span[1] > 0)
            	spans[i] = span;
        }
        return spans;
    }

    private short violate(NTo1WordAlign align, short f, short[] span) {
    	Node[] F = align.getF();
        short violate = 0;
        for (short i = span[0]; i < span[1]; ++i)
        	for (Link link = align.getFirstLinkFromE(i); link != null; link = link.next)
                if (!F[link.f].isDescendOf(F[f])) {
                    ++violate;
                    break;
                }
        return violate;
    }
    
    /**
     * Replaces certain expressions in the gold-standard MRs with <code>Anaphor</code> symbols.  The
     * purpose of this is to allow certain words to have <code>Anaphor</code> symbols as their MR.
     * Most of these words would be pronouns or possessive pronouns.
     * <p>
     * The replacement of MR expressions is done in a completely unsupervised fashion.  Here is the
     * basic idea:
     * <ul>
     * <li>For each training example, (<i>E</i>,<i>F</i>):
     * <ul>
     * <li>For each of the <i>N</i> top-scoring word alignments between <i>E</i> and <i>F</i>:
     * <ul>
     * <li>Identify repeating MR expressions in <i>F</i>.
     * <li>If there are fewer links from these MR expressions <i>combined</i> than the size of
     * <i>each</i> of these expressions, then it is a strong indication that coreferences occur in
     * <i>E</i>.
     * <li>The MR expression that has the most links should be kept unchanged.  All other repetitions
     * should be replaced by <code>Anaphor</code> symbols.  Now the MR expression that has the most
     * links receives one vote.
     * </ul>
     * <li>The MR expression that receives the most votes are kept unchanged.  All other repetitions
     * are replaced by <code>Anaphor</code> symbols if there is a strong indication that coreferences
     * occur in <i>E</i>.
     * </ul>
     * </ul>
     * <p>
     * This method only examines MR expressions of type <i>X</i> if the production
     * <code>*n:X -&gt; ({ *^:X })</code> is in the MRL grammar.
     * <p>
     * This method returns <code>true</code> if some MR expressions have been replaced, in which case
     * the word alignment model will need to be re-trained.  
     * 
     * @see wasp.data.Anaphor
     * @return <code>true</code> if some MR expressions have been replaced by <code>Anaphor</code>
     * symbols; <code>false</code> otherwise.
     */
    private boolean createAnaphora() {
        boolean created = false;
        for (Iterator it = examples.iterator(); it.hasNext();) {
            Example ex = (Example) it.next();
            HashMap allChains = new HashMap();
            ArrayList list = (ArrayList) aligns.get(ex.id);
            for (Iterator jt = list.iterator(); jt.hasNext();) {
                NTo1WordAlign align = (NTo1WordAlign) jt.next();
                ArrayList chains = findCorefChains(ex, align);
                for (Iterator kt = chains.iterator(); kt.hasNext();) {
                	CorefChain chain = (CorefChain) kt.next();
                	CorefChain intern = (CorefChain) allChains.get(chain);
                	if (intern == null)
                		allChains.put(chain, chain);
                	else
                		intern.add(chain);
                }
            }
            if (allChains.isEmpty())
                continue;
            int[] toAnaphor = new int[ex.F.linear.length];
            Arrays.fill(toAnaphor, -1);
            for (Iterator jt = allChains.values().iterator(); jt.hasNext();) {
            	CorefChain chain = (CorefChain) jt.next();
            	short antecedant = chain.argmax();
            	for (short k = 0; k < ex.F.linear.length; ++k)
            		if (chain.contains(k) && k != antecedant)
            			toAnaphor[k] = ex.F.lprods[k].getLhs();
            }
            for (short j = (short) (ex.F.linear.length-1); j >= 0; --j)
            	if (toAnaphor[j] >= 0) {
            		Symbol[] rhs = new Symbol[1];
            		rhs[0] = new Anaphor(toAnaphor[j]);
            		Production prod = new Production(toAnaphor[j], rhs);
            		ex.F.replace(j, new Node(new ProductionSymbol(prod)));
                    created = true;
                }
        }
        return created;
    }
    
    private static class CorefChain {
    	private boolean[] isMember;
    	private short[] nlinks;
    	private short[] histogram;
    	public CorefChain(Meaning F) {
    		isMember = new boolean[F.linear.length];
    		nlinks = new short[F.linear.length];
    		histogram = new short[F.linear.length];
    	}
    	public boolean equals(Object o) {
    		return o instanceof CorefChain && Arrays.equal(isMember, ((CorefChain) o).isMember);
    	}
    	public int hashCode() {
    		return Arrays.hashCode(isMember);
    	}
    	public void add(short i) {
    		++histogram[i];
    	}
    	public void add(CorefChain c) {
    		if (!equals(c))
    			throw new RuntimeException();
    		for (short i = 0; i < histogram.length; ++i)
    			histogram[i] += c.histogram[i];
    	}
    	public short argmax() {
    		return (short) Arrays.argmax(histogram);
    	}
    	public boolean contains(short i) {
    		return isMember[i];
    	}
    	public void set(short i, short links) {
    		isMember[i] = true;
    		nlinks[i] = links;
    	}
    	public short size() {
    		return (short) Arrays.count(isMember);
    	}
    	public short countLinks() {
    		return Arrays.sum(nlinks);
    	}
    }
    
    private ArrayList findCorefChains(Example ex, NTo1WordAlign align) {
    	Node[] Fshort = (Node[]) Fshorts.get(ex.id);
        // identify coreference chains
        ArrayList chains = new ArrayList();
        boolean[] checked = new boolean[ex.F.linear.length];
        for (short i = 0; i < ex.F.linear.length; ++i)
        	if (!checked[i]) {
	        	checked[i] = true;
	        	if (Config.getMRLGrammar().isAnaphorOK(ex.F.lprods[i].getLhs())) {
	        		CorefChain chain = new CorefChain(ex.F);
	        		chain.set(i, countLinks(align, i, ex.F.lastd[i]));
	        		for (short j = (short) (i+1); j < ex.F.linear.length; ++j)
	        			if (Tree.equal(ex.F.linear[i], ex.F.linear[j])) {
	        				checked[j] = true;
	        				chain.set(j, countLinks(align, j, ex.F.lastd[j]));
	        			}
	        		short treeSize = treeSize(ex.F.linear[i], Fshort);
		        	if (chain.size() > 1 && chain.countLinks() > 0 && chain.countLinks() <= treeSize)
		        		chains.add(chain);
	        	}
        	}
        if (chains.isEmpty())
        	return chains;
        // remove links from all coreference chains
        NTo1WordAlign rm = new NTo1WordAlign(ex.E, ex.F.linear);
        for (Iterator it = chains.iterator(); it.hasNext();) {
        	CorefChain chain = (CorefChain) it.next();
        	for (short j = 0; j < ex.F.linear.length; ++j)
        		if (chain.contains(j))
        			for (short k = j; k <= ex.F.lastd[j]; ++k) {
        				for (Link link = align.getFirstLinkFromF(k); link != null; link = link.next)
        					rm.addLink(link);
        				align.removeLinksFromF(k);
        			}
        }
        // identify the antecedant for each coreference chain
        for (Iterator it = chains.iterator(); it.hasNext();) {
        	CorefChain chain = (CorefChain) it.next();
            short maxLinks = 0;
            short minViolate = Short.MAX_VALUE;
            short cand = -1;
            for (short j = 0; j < ex.F.linear.length; ++j)
            	if (chain.contains(j)) {
            		for (short k = j; k <= ex.F.lastd[j]; ++k)
            			for (Link link = rm.getFirstLinkFromF(k); link != null; link = link.next)
            				align.addLink(link);
            		short links = chain.countLinks();
            		short violate = totalViolate(align);
            		if (maxLinks < links || (maxLinks < links && minViolate > violate)) {
            			maxLinks = links;
            			minViolate = violate;
            			cand = j;
            		}
            		for (short k = j; k <= ex.F.lastd[j]; ++k)
            			align.removeLinksFromF(k);
            	}
            chain.add(cand);
        }
        // restore links that have been removed
        for (short i = 0; i < ex.E.length; ++i)
        	for (Link link = rm.getFirstLinkFromE(i); link != null; link = link.next)
        		align.addLink(link);
        return chains;
    }

    private short countLinks(NTo1WordAlign align, short first, short last) {
    	short count = 0;
    	for (short i = first; i <= last; ++i)
    		count += align.countLinksFromF(i);
    	return count;
    }
    
    private short treeSize(Node n, Node[] array) {
    	short size = 0;
    	Node[] d = n.getDescends();
    	for (short i = 0; i < d.length; ++i)
    		if (Arrays.indexOf(array, d[i]) >= 0)
    			++size;
    	return size;
    }
    
    private void storeAligns() {
    	for (Iterator it = examples.iterator(); it.hasNext();) {
    		Example ex = (Example) it.next();
    		Mask Emask = (Mask) Emasks.get(ex.id);
    		Terminal[] Eshort = (Terminal[]) Eshorts.get(ex.id);
    		ArrayList Ecomb = (ArrayList) Ecombs.get(ex.id);
    		ex.aligns.clear();
    		ArrayList list = (ArrayList) aligns.get(ex.id);
    		if (list != null)
    			for (Iterator jt = list.iterator(); jt.hasNext();) {
    				NTo1WordAlign align = (NTo1WordAlign) jt.next();
    				align = align.separateE(Eshort, Ecomb);
    				align = align.unmaskE(ex.E, Emask);
    				ex.aligns.add(align);
    			}
    	}
    }
    
}
