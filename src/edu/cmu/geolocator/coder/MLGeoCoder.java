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

package edu.cmu.geolocator.coder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;
import libsvm.svm_problem;
import edu.cmu.geolocator.model.CandidateAndFeature;
import edu.cmu.geolocator.model.LocGroupFeatures;
import edu.cmu.geolocator.model.Tweet;

public class MLGeoCoder {

  private static MLGeoCoder tgcoder;

  private static final String ModelPath = "res/geocoder-test.mdl";

  svm_problem problem;

  ArrayList<svm_node[]> nodelist;

  ArrayList<Double> labels;

  double[] probability;

  ArrayList<CandidateAndFeature> fs;

  ArrayList<ArrayList<CandidateAndFeature>> farrays;

  svm_model model;

  public MLGeoCoder() {
    try {
      model = svm.svm_load_model(ModelPath);
    } catch (IOException e) {
      // TODO Auto-generated catch block
      System.err.println("GeoCoder model not found!");
      e.printStackTrace();
    }
  }

  /**
   * Parse a tweet example, and perform geo-coding. Note that the tweet example contains not only
   * locations, but also the metadata,contextual info.
   * 
   * @param example
   * @return
   * @throws Exception
   */
  public List<CandidateAndFeature> resolve(Tweet example, String mode,String filter) throws Exception {

    ArrayList<CandidateAndFeature> decoded = new ArrayList<CandidateAndFeature>();
    
    problem = new svm_problem();

    // copy 2d feature array into feature lists
    LocGroupFeatures feature = new LocGroupFeatures(example, mode,filter).toFeatures();
    if (feature==null)
      return null;
    farrays = feature.getFeatureArrays();

    ArrayList<CandidateAndFeature> maxPopCandidates = getMaxPopulation(farrays);

    fs = new ArrayList<CandidateAndFeature>();
    for (ArrayList<CandidateAndFeature> a : farrays)
      for (CandidateAndFeature b : a)
        fs.add(b);

    // get the feature
    nodelist = feature.getFeatureVector();

    // System.out.println(nodelist.size());
    problem.l = nodelist.size();
    problem.x = new svm_node[problem.l][];
    probability = new double[problem.l];

    for (int i = 0; i < problem.l; i++) {
      problem.x[i] = nodelist.get(i);
      int l = (int) svm.svm_predict(model, problem.x[i]);
      // System.out.println("The prediction is " + l);
      if (l == 1) {
        fs.get(i).setL(1);
        fs.get(i).setProb(probability[i]);
        decoded.add(fs.get(i));
      }
    }

    /*
     * add most populated city, if svm miss the place. If the population is smaller than 1000,
     * filter the place.
     */
    HashSet<String> SVMOutput = new HashSet<String>();

    for (CandidateAndFeature d : decoded)
      SVMOutput.add(d.getOriginName());
//    System.out.println("SVM output is :" + SVMOutput);

    int i = 0;
    for (CandidateAndFeature d : maxPopCandidates) {
//      System.out.println("maxPosCandidate " + (i++) + " is " + d.getOriginName());
      if (!SVMOutput.contains(d.getOriginName())
       && d.getPopulation() > 999
      )
        decoded.add(d);
    }

    if (decoded.size() == 0)
      return null;
    else{
      // added the probability output for each result.
      for(CandidateAndFeature de:decoded)
        de.setProb(0.85);
      
      return decoded;      
    }
  }

  private ArrayList<CandidateAndFeature> getMaxPopulation(
          ArrayList<ArrayList<CandidateAndFeature>> fa) {
    ArrayList<CandidateAndFeature> maxPop = new ArrayList<CandidateAndFeature>();
    if (fa == null)
      return null;
    for (ArrayList<CandidateAndFeature> word : fa)
      for (CandidateAndFeature cand : word)
         if (cand.getF_PopRank() <1 && cand.getF_PopRank() >-0.5 )
        maxPop.add(cand);
    return maxPop;
  }

  public static MLGeoCoder getInstance() {
    // TODO Auto-generated method stub
    if (tgcoder == null)
      tgcoder = new MLGeoCoder();
    return tgcoder;
  }
}
