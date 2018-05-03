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
package wasp.scfg.parse;

import java.util.HashSet;
import java.util.Iterator;
import java.util.logging.Logger;

import wasp.data.Example;
import wasp.data.Examples;
import wasp.data.Node;
import wasp.math.LBFGS;
import wasp.math.Math;
import wasp.math.Vectors;
import wasp.nl.GapModel;
import wasp.scfg.Rule;
import wasp.scfg.RuleSymbol;
import wasp.scfg.SCFG;
import wasp.util.Arrays;
import wasp.util.Double;

/**
 * Code for estimating the parameters of SCFG translation models based on the maximum-entropy principle.
 * 
 * @author ywwong
 *
 */
public class Maxent implements LBFGS.Objective {

	private static Logger logger = Logger.getLogger(Maxent.class.getName());
	
	private static final double PRIOR_VARIANCE = 100;
	private static final boolean DO_VITERBI_APPROX = true;
	private static final int VITERBI_APPROX_ITERATIONS = 10;
	private static final int VITERBI_APPROX_K = 1;
	
	private SCFG gram;
	private GapModel gm;

	private Examples examples;
	private double[] lastX;
	private double lastVal;
	private double[] lastGrad;
	
	public Maxent(SCFG gram, GapModel gm) {
		this.gram = gram;
		this.gm = gm;
	}
	
	/**
	 * Estimates the parameters of the SCFG translation model such that the conditional log-likelihood 
	 * of the specified training examples is maximized.
	 * 
	 * @param examples the training examples.
	 */
	public void estimate(Examples examples) {
		logger.info("Estimating the parameters of the SCFG translation model");
		this.examples = examples;
		reset();
		double[] weights = getInitWeightVector();
		new LBFGS().minimize(this, weights);
		setWeightVector(weights);
		reset();
		logger.info("Parameter estimation of the SCFG translation model is done");
	}

	private void reset() {
		lastX = null;
		lastVal = Double.NaN;
		lastGrad = null;
	}
	
	/**
	 * Returns the initial parameters of this translation model listed in a domain-specific order.
	 * 
	 * @return the initial parameters of this translation model.
	 */
	private double[] getInitWeightVector() {
		int nr = gram.countRules();
		int np = gm.countParams();
		return new double[nr+np];
	}
	
	public void getValueAndGradient(double[] X, Double val, double[] grad) {
		if (lastX != null && Arrays.equal(lastX, X)) {
			val.val = lastVal;
			Vectors.assign(grad, lastGrad);
			return;
		}
		double[] T_E = new double[X.length];
		double[] T_EF = new double[X.length];
		Arrays.fill(T_E, Double.NEGATIVE_INFINITY);
		Arrays.fill(T_EF, Double.NEGATIVE_INFINITY);
		val.val = 0;
		setWeightVector(X);
		SCFGParser parser = new SCFGParser(gram, gm);
		for (Iterator it = examples.iterator(); it.hasNext();) {
			Example ex = (Example) it.next();
			logger.finest("example "+ex.id);
			double z_E = Double.NEGATIVE_INFINITY;
			double z_EF = Double.NEGATIVE_INFINITY;
			for (Iterator jt = parser.parse(ex.E, ex.F); jt.hasNext();) {
				SCFGParse parse = (SCFGParse) jt.next();
				z_E = Math.logAdd(z_E, parse.score);
				if (!parse.item.m.isEmpty())
					z_EF = Math.logAdd(z_EF, parse.score);
			}
			if (z_EF > Double.NEGATIVE_INFINITY) {
				val.val += z_E - z_EF;
				parser.outside(false);
				addT(T_E, z_E);
				parser.outside(true);
				addT(T_EF, z_EF);
				logger.fine(ex.id+" "+(z_EF-z_E));
			} else
				logger.fine(ex.id+" X");
		}
		logger.fine("log Pr(F|E) = "+(-val.val));
		for (int i = 0; i < X.length; ++i) {
			val.val += X[i]*X[i]/(2*PRIOR_VARIANCE);
			grad[i] = Math.exp(T_E[i])-Math.exp(T_EF[i]);
			grad[i] += X[i]/PRIOR_VARIANCE;
		}
		logger.fine("obj func = "+val);
		logger.fine("norm(G) = "+Vectors.twoNorm(grad));
		lastX = (double[]) X.clone();
		lastVal = val.val;
		lastGrad = (double[]) grad.clone();
	}
	
	/**
	 * Sets the parameters of the SCFG translation model to the specified values.
	 * 
	 * @param weights the parameter values to use; parameters are listed in the same order as in the
	 * <code>getWeightVector</code> method.
	 */
	private void setWeightVector(double[] weights) {
		int nr = gram.countRules();
		for (int i = 0; i < nr; ++i) {
			Rule rule = gram.getRule(i);
			if (rule.isActive()) {
				int tied = gram.getId(gram.tied(rule));
				rule.setWeight(weights[tied]);
			}
		}
		gm.setWeightVector(Arrays.subarray(weights, nr, weights.length));
	}
	
	private void addT(double[] T, double z) {
		int nr = gram.countRules();
		for (int i = 0; i < nr; ++i) {
			Rule rule = gram.getRule(i);
			if (rule.isActive()) {
				int tied = gram.getId(gram.tied(rule));
				T[tied] = Math.logAdd(T[tied], rule.getWeight()+rule.getOuterScore()-z);
			}
		}
		double[] weights = gm.getWeightVector();
		double[] outers = gm.getOuterScores();
		for (int i = 0; i < weights.length; ++i)
			T[nr+i] = Math.logAdd(T[nr+i], weights[i]+outers[i]-z);
	}
	
	public void getX(double[] X) {
		Vectors.assign(X, getWeightVector());
	}
	
	/**
	 * Returns the current parameter vector of this translation model listed in a domain-specific order.
	 * 
	 * @return the current parameter vector of this translation model.
	 */
	private double[] getWeightVector() {
		int nr = gram.countRules();
		double[] weights = new double[nr];
		for (int i = 0; i < nr; ++i) {
			Rule rule = gram.getRule(i);
			if (rule.isActive() && rule == gram.tied(rule))
				weights[i] = rule.getWeight();
		}
		return Arrays.concat(weights, gm.getWeightVector());
	}
	
	/**
	 * Deactivates rules that are not used in any of the top-ranked parses of the current training
	 * examples.  This is a trick called <i>Viterbi approximation</i>.  Deactivation of rules changes
	 * the objective function being considered, so the underlying <code>LBFGS</code> object has to be
	 * reset.
	 */
	public void check(LBFGS lbfgs, int iter, boolean isLastIter) {
		if (DO_VITERBI_APPROX) {
			if (isLastIter || (iter+1) % VITERBI_APPROX_ITERATIONS == 0) {
				HashSet set = new HashSet();
				SCFGParser parser = new SCFGParser(gram, gm, VITERBI_APPROX_K);
				for (Iterator it = examples.iterator(); it.hasNext();) {
					Example ex = (Example) it.next();
					for (Iterator jt = parser.parse(ex.E, ex.F); jt.hasNext();) {
						SCFGParse parse = (SCFGParse) jt.next();
						markRules(set, parse.toTree());
					}
				}
				boolean reset = false;
				int nr = gram.countRules();
				for (int i = 0; i < nr; ++i) {
					Rule rule = gram.getRule(i);
					if (rule.isActive() && !rule.isInit() && !isRuleMarked(set, rule)) {
						rule.deactivate();
						logger.fine("deactivate "+rule);
						reset = true;
					}
				}
				if (reset)
					lbfgs.reset();
			}
		}
	}
	
	private void markRules(HashSet set, Node node) {
		Rule rule = ((RuleSymbol) node.getSymbol()).getRule();
		set.add(gram.tied(rule));
		short nc = node.countChildren();
		for (short i = 0; i < nc; ++i)
			markRules(set, node.getChild(i));
	}
	
	private boolean isRuleMarked(HashSet set, Rule rule) {
		return set.contains(gram.tied(rule));
	}
	
}
