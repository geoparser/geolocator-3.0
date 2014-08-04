/*Copyright 2014, Language Technologies Institute, Carnegie Mellon
University

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.

    You may obtain a copy of the License at
    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
implied. See the License for the specific language governing
permissions and limitations under the License.
*/
package cmu.arktweetnlp.impl;

import java.util.ArrayList;
import java.util.Arrays;

import edu.stanford.nlp.util.Pair;

/**
 * One sequence structure -- typically, for one sentence
 * This is the model's view of a sentence -- only deals with non-textual numberized versions of everything
 */
public class ModelSentence {
	public int T;

	/** Runtime inferred, Trainingtime observed.
	 * dim T
	 **/
	public int labels[];
	
	public double confidences[];
	
	/** Runtime observed, Trainingtime observed.
	 * This is an array-of-arrays of (featID, featValue) pairs.
	 * dim T, then variable nnz per t.
	 **/
	public ArrayList<ArrayList< Pair<Integer, Double>>> observationFeatures;

	/** Runtime observed, Trainingtime observed (for MEMM).
	 * dim T st: edgeFeatures[t] = ID of label@(t-1).
	 * values in 0..(N_labels-1), plus extra higher numbers for markers (see Model)
	 **/
	public int edgeFeatures[];

	public ModelSentence(int T) {
		this.T = T;
		labels = new int[T];
		edgeFeatures = new int[T];
		observationFeatures = new ArrayList<ArrayList< Pair<Integer, Double> >>();
		for (int t=0; t<T; t++) {
			observationFeatures.add( new ArrayList<Pair<Integer,Double>>() );
		}
		Arrays.fill(labels, -1);
		Arrays.fill(edgeFeatures, -1);
	}
}
