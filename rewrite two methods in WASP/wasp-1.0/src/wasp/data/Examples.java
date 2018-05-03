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
package wasp.data;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import wasp.main.Config;
import wasp.main.Parse;
import wasp.mrl.Production;
import wasp.nl.NLGrammar;
import wasp.util.Arrays;
import wasp.util.Double;
import wasp.util.FileWriter;
import wasp.util.Int;

/**
 * A set of examples for training and testing.
 * 
 * @author ywwong
 *
 */
public class Examples {

	private static Logger logger = Logger.getLogger(Examples.class.getName());
	
	private class ExampleIterator implements Iterator {
		private Iterator it;
		public ExampleIterator() {
			it = map.values().iterator();
		}
		public boolean hasNext() {
			return it.hasNext();
		}
		public Object next() {
			return it.next();
		}
		public void remove() {
			it.remove();
			ids = null;
		}
	}
	
	private TreeMap map;
	private Int[] ids;

	public Examples() {
		map = new TreeMap();
		ids = null;
	}
	
	public Iterator iterator() {
		return new ExampleIterator();
	}
	
	public Example get(int id) {
		return (Example) map.get(new Int(id));
	}
	
	public Example getNth(int n) {
		if (ids == null)
			ids = (Int[]) map.keySet().toArray(new Int[0]);
		return (Example) map.get(ids[n]);
	}
	
	public int size() {
		return map.size();
	}
	
	public void add(Example ex) {
		map.put(new Int(ex.id), ex);
		ids = null;
	}
	
	///
	/// File I/O
	///
	
	public void read(String filename)
	throws IOException, SAXException, ParserConfigurationException {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setValidating(false);
		factory.setNamespaceAware(false);
		SAXParser parser = factory.newSAXParser();
		parser.parse(new File(filename), new ExampleHandler());
	}

	private class ExampleHandler extends DefaultHandler {
		private Example ex;
		private String lang;
		private int nodeId;
		private double score;
		private StringBuffer buf;
		public void startElement(String uri, String localName, String qName, Attributes attributes) {
			if (qName.equalsIgnoreCase("example")) {
				ex = new Example();
				ex.id = Int.parseInt(attributes.getValue("id"));
				logger.finest("example "+ex.id);
			} else if (qName.equalsIgnoreCase("nl")) {
				lang = attributes.getValue("lang");
				buf = new StringBuffer();
				logger.finest("nl "+lang);
			} else if (qName.equalsIgnoreCase("syn")) {
				lang = attributes.getValue("lang");
				buf = new StringBuffer();
				logger.finest("syn "+lang);
			} else if (qName.equalsIgnoreCase("augsyn")) {
				lang = attributes.getValue("lang");
				buf = new StringBuffer();
				logger.finest("augsyn "+lang);
			} else if (qName.equalsIgnoreCase("mrl")) {
				lang = attributes.getValue("lang");
				buf = new StringBuffer();
				logger.finest("mrl "+lang);
			} else if (qName.equalsIgnoreCase("node")) {
				nodeId = Int.parseInt(attributes.getValue("id"));
				buf = new StringBuffer();
				logger.finest("mrl-parse node "+nodeId);
			} else if (qName.equalsIgnoreCase("parse")) {
				String attr = attributes.getValue("score");
				score = (attr==null) ? 0 : Double.parseDouble(attr);
				buf = new StringBuffer();
				logger.finest("parse");
			}
		}
		public void characters(char[] text, int start, int length) {
			if (buf != null)
				buf.append(text, start, length);
		}
		public void endElement(String uri, String localName, String qName) {
			if (qName.equalsIgnoreCase("nl")) {
				String nl = buf.toString().trim();
				ex.nlMap.put(lang, nl);
				if (lang.equals(Config.getNL()))
					ex.E = new NLGrammar().tokenize(nl);
			} else if (qName.equalsIgnoreCase("syn"))
				ex.synMap.put(lang, buf.toString().trim());
			else if (qName.equalsIgnoreCase("augsyn"))
				ex.augsynMap.put(lang, buf.toString().trim());
			else if (qName.equalsIgnoreCase("mrl")) {
				String mrl = buf.toString().trim();
				ex.mrlMap.put(lang, mrl);
				if (lang.equals(Config.getMRL())) {
					ex.F = new Meaning(mrl);
					if (ex.F.parse == null)
						logger.warning("example "+ex.id+": MR is not grammatical");
				}
			} else if (qName.equalsIgnoreCase("node")) {
				String[] line = Arrays.tokenize(buf.toString().trim());
				Int index = new Int(0);
				Production prod = Production.read(line, index);
				if (index.val == line.length)
					ex.Fparse.add(prod);
				else
					logger.warning("example "+ex.id+": node "+nodeId+" of MR parse is invalid");
			} else if (qName.equalsIgnoreCase("parse"))
				ex.parses.add(new Parse(buf.toString().trim(), score));
			else if (qName.equalsIgnoreCase("example")) {
				if (ex.F == null || ex.F.parse != null)
					add(ex);
			}
		}
	}

	public static boolean writeSyn = false;
	public static boolean writeAugsyn = false;
	public static boolean writeMRLParse = false;
	public static boolean writeMRL = false;
	public static boolean writeParses = true;

	public void write(String filename) throws IOException {
		PrintWriter out = new PrintWriter(new BufferedWriter(FileWriter.createNew(filename)));
		writeHeader(out);
		for (Iterator it = iterator(); it.hasNext();)
			write(out, (Example) it.next());
		writeFooter(out);
		out.close();
	}
	
	private static void writeHeader(PrintWriter out) throws IOException {
		out.println("<?xml version=\"1.0\"?>");
		out.println("<!DOCTYPE examples [");
		out.println("  <!ELEMENT examples (example*)>");
		out.println("  <!ELEMENT example (nl*,syn*,augsyn*,mrl*,mrl-parse?,parse*)>");
		out.println("  <!ELEMENT nl (#PCDATA)>");
		out.println("  <!ELEMENT syn (#PCDATA)>");
		out.println("  <!ELEMENT augsyn (#PCDATA)>");
		out.println("  <!ELEMENT mrl (#PCDATA)>");
		out.println("  <!ELEMENT mrl-parse (node*)>");
		out.println("  <!ELEMENT node (#PCDATA)>");
		out.println("  <!ELEMENT parse (#PCDATA)>");
		out.println("]>");
		out.println("<examples>");
		out.println();
	}
	
	private static void write(PrintWriter out, Example ex) throws IOException {
		out.println("<example id=\""+ex.id+"\">");
		for (Iterator it = ex.nlMap.entrySet().iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry) it.next();
			String lang = (String) entry.getKey();
			String nl = (String) entry.getValue();
			out.println("<nl lang=\""+lang+"\">");
			out.println(nl);
			out.println("</nl>");
		}
		if (writeSyn)
			for (Iterator it = ex.synMap.entrySet().iterator(); it.hasNext();) {
				Map.Entry entry = (Map.Entry) it.next();
				String lang = (String) entry.getKey();
				String nl = (String) entry.getValue();
				out.println("<syn lang=\""+lang+"\">");
				out.println(nl);
				out.println("</syn>");
			}
		if (writeAugsyn)
			for (Iterator it = ex.augsynMap.entrySet().iterator(); it.hasNext();) {
				Map.Entry entry = (Map.Entry) it.next();
				String lang = (String) entry.getKey();
				String nl = (String) entry.getValue();
				out.println("<augsyn lang=\""+lang+"\">");
				out.println(nl);
				out.println("</augsyn>");
			}
		if (writeMRL)
			for (Iterator it = ex.mrlMap.entrySet().iterator(); it.hasNext();) {
				Map.Entry entry = (Map.Entry) it.next();
				String lang = (String) entry.getKey();
				String mrl = (String) entry.getValue();
				out.println("<mrl lang=\""+lang+"\">");
				out.println(mrl);
				out.println("</mrl>");
			}
		if (writeMRLParse) {
			out.println("<mrl-parse>");
			for (short i = 0; i < ex.F.lprods.length; ++i) {
				out.print("<node id=\""+i+"\"> ");
				out.print(ex.F.lprods[i]);
				out.println(" </node>");
			}
			out.println("</mrl-parse>");
		}
		if (writeParses) {
			Parse[] parses = ex.getSortedParses();
			for (int j = 0; j < parses.length; ++j) {
				String str = parses[j].toStr();
				if (str == null) {
					logger.finer("example "+ex.id+" parse "+j+" is bad");
					continue;
				}
				logger.finer("example "+ex.id+" parse "+j);
				out.println("<parse rank=\""+j+"\" score=\""+parses[j].score+"\">");
				out.println(str);
				out.println("</parse>");
				Node tree = parses[j].toTree();
				if (tree != null) {
					out.println("<!--");
					out.println(tree.toPrettyString());
					out.println("-->");
				}
			}
		}
		out.println("</example>");
		out.println();
	}
	
	private static void writeFooter(PrintWriter out) throws IOException {
		out.println("</examples>");
	}
	
}
