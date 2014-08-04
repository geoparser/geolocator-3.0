/**
 * 
 * Copyright (c) 2012 - 2014 Carnegie Mellon University
 * 
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
 * 
 * @author Wei Zhang,  Language Technology Institute, School of Computer Science, Carnegie-Mellon University.
 * email: wei.zhang@cs.cmu.edu
 *
 * 
 */
package edu.cmu.geolocator.nlp.ner.MinorThirdCRF;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.cmu.geolocator.io.ACEImporter;
import edu.cmu.geolocator.io.GetReader;
import edu.cmu.geolocator.model.Document;
import edu.cmu.geolocator.model.Sentence;
import edu.cmu.geolocator.model.Token;
import edu.cmu.geolocator.nlp.ner.FeatureExtractor.ACE_En_FeatureGenerator;
import edu.cmu.minorthird.classify.ClassLabel;
import edu.cmu.minorthird.classify.Example;
import edu.cmu.minorthird.classify.Feature;
import edu.cmu.minorthird.classify.MutableInstance;
import edu.cmu.minorthird.classify.sequential.CMM;
import edu.cmu.minorthird.classify.sequential.SequenceDataset;
import edu.cmu.minorthird.util.IOUtil;

public class ACLTester {

	public static void main(String argv[]) throws IOException {
		String learningalgo = "crf";

		CMM model = null;

		String name = "ACE-CRF.model120";
		if (learningalgo.equals("crf"))
			model = (CMM) IOUtil.loadSerialized(new java.io.File(name));
		if (learningalgo.equals("cpl"))
			model = (CMM) IOUtil.loadSerialized(new java.io.File("ACE-CPL.model"));

		System.out.println("Model loaded. Tagging test set and Evaluate:");

		// put your own docs here.
		ACEImporter importer = new ACEImporter(
				"E:\\chenxu\\cmu\\IEEE paper data\\parallel data\\LDC Data\\ACE 2005 Multilingual LDC2005E18\\ACE2005-TrainingData-V6.0\\English\\test");

		System.out.println("Start testing");
		HashMap<String, Document> testdocs = importer.getsDocs();

		ACE_En_FeatureGenerator fgen = new ACE_En_FeatureGenerator("res/");

		for (Document d : testdocs.values()) {
			System.out.println(d.getDid());
			int sentid = 0;
			for (Sentence sent : d.getP().get(0).getSentences()) {
				Example[] exp = new Example[sent.getTokens().length];
				List<ArrayList<Feature>> tweetfeatures = fgen.extractFeature(sent);
				for (int tokid = 0; tokid < sent.getTokens().length; tokid++) {
					ClassLabel lab = new ClassLabel(sent.getTokens()[tokid].getNE() == null ? ""
							: sent.getTokens()[tokid].getNE());
					MutableInstance inst = new MutableInstance("ACE-NER", d.getDid() + sentid + "-" + tokid);
					for (int j = 0; j < tweetfeatures.get(0).size(); j++) {
						inst.addBinary(tweetfeatures.get(tokid).get(j));
					}
					exp[tokid] = new Example(inst, lab);
				}
				ClassLabel[] labels = model.classification(exp);
				for (int tokid = 0; tokid < sent.getTokens().length; tokid++) {
					sent.getTokens()[tokid].setNEprediction(labels[tokid].bestClassName());
				}
				sentid++;
			}
		}
		System.out.println(name);
		printStat(new ArrayList<Document>(testdocs.values()));

	}

	private static void printStat(ArrayList<Document> testdocs) {
		int gold = 0, totalpred = 0, correct = 0;
		for (Document d : testdocs) {
			for (Sentence sent : d.getP().get(0).getSentences()) {
				for (Token t : sent.getTokens()) {
					if (t.getNEprediction().equals("O") == false)
						if((t.getNEprediction().equals("TYPE=\"GPE\"")||t.getNEprediction().equals("TYPE=\"LOC\"")))
						totalpred++;
					if (t.getNE() != null)
						if (t.getNE().equals("TYPE=\"GPE\"")||t.getNE().equals("TYPE=\"LOC\"")){
						gold++;
					if (t.getNE().equals(t.getNEprediction()))
						correct++;
						//System.out.println(t.getNE());
						}
				}
			}
		}
		double p = (double) correct / (double) totalpred;
		double r = (double) correct / (double) gold;
		double f1 = 2 * p * r / (p + r);
		System.out.println("Precision:" + p);
		System.out.println("Recall" + r);
		System.out.println(f1);
	}
}
