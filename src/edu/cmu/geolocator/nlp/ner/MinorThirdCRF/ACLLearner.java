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
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.cmu.geolocator.io.ACEImporter;
import edu.cmu.geolocator.io.GetReader;
import edu.cmu.geolocator.model.Document;
import edu.cmu.geolocator.model.Sentence;
import edu.cmu.geolocator.nlp.ner.FeatureExtractor.ACE_En_FeatureGenerator;
import edu.cmu.minorthird.classify.ClassLabel;
import edu.cmu.minorthird.classify.Example;
import edu.cmu.minorthird.classify.Feature;
import edu.cmu.minorthird.classify.MutableInstance;
import edu.cmu.minorthird.classify.sequential.CMM;
import edu.cmu.minorthird.classify.sequential.CRFLearner;
import edu.cmu.minorthird.classify.sequential.CollinsPerceptronLearner;
import edu.cmu.minorthird.classify.sequential.SequenceDataset;
import edu.cmu.minorthird.util.IOUtil;

public class ACLLearner {

	public static void main(String argv[]) throws IOException {

		String learner = "crf";

		ACEImporter importer = new ACEImporter(
				"E:\\chenxu\\cmu\\IEEE paper data\\parallel data\\LDC Data\\ACE 2005 Multilingual LDC2005E18\\ACE2005-TrainingData-V6.0\\English\\train");
		
		
		HashMap<String, Document> docs = importer.getsDocs();

		ACE_En_FeatureGenerator fgen = new ACE_En_FeatureGenerator("res/");

		int iter = 120;

		SequenceDataset dataset = new SequenceDataset();

		for (Document d : docs.values()) {
			int sentid = 0;
			for (Sentence sent : d.getP().get(0).getSentences()) {

				List<ArrayList<Feature>> tweetfeatures = fgen.extractFeature(sent);
				for (int tokid = 0; tokid < sent.getTokens().length; tokid++) {
					ClassLabel lab = new ClassLabel(sent.getTokens()[tokid].getNE()==null?"O":sent.getTokens()[tokid].getNE());
					MutableInstance inst = new MutableInstance("ACE-NER", d.getDid() + sentid + "-" + tokid);
					for (int j = 0; j < tweetfeatures.get(0).size(); j++)
						inst.addBinary(tweetfeatures.get(tokid).get(j));
					dataset.add(new Example(inst, lab));
				}
				sentid++;
			}
		}

		// initialize default properties.
		if (learner.equals("crf")) {
			CRFLearner CRF = new CRFLearner();
			CRF.setMaxIters(iter);
			CMM model = (CMM) CRF.batchTrain(dataset);
			IOUtil.saveSerialized(model, new File("ACE-CRF.model"+iter));

		} else if (learner.equals("cpl")) {
			CollinsPerceptronLearner cpl = new CollinsPerceptronLearner(3, iter);
			CMM model = (CMM) cpl.batchTrain(dataset);
			IOUtil.saveSerialized(model, new File("ACE-CPL.model"));
		} else
			System.out.println("Learner not defined.");
	}
}
