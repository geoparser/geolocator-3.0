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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import edu.cmu.geolocator.common.StringUtil;
import edu.cmu.geolocator.model.CandidateAndFeature;
import edu.cmu.geolocator.model.LocEntityAnnotation;
import edu.cmu.geolocator.model.LocGroupFeatures;
import edu.cmu.geolocator.model.Sentence;
import edu.cmu.geolocator.model.Token;
import edu.cmu.geolocator.model.Tweet;

public class MinimalityGeoCoder implements GeoCoder {
  private static MinimalityGeoCoder agcoder;

  public static MinimalityGeoCoder getInstance() {
    if (agcoder == null) {
      agcoder = new MinimalityGeoCoder();
    }
    return agcoder;
  }

  public List<CandidateAndFeature> resolve(Tweet example, String mode, String filter)
          throws Exception {

//    System.err.println("[From Minimality Coder: Toponyms to resolve:]"
//            + example.getToponyms().size());
    if (example.getToponyms() == null)
      return null;
    if (example.getToponyms().size() < 2)
      return null;
    if (example.getToponyms().size() > 3)
      return null;

//    System.err.println("[From Minimality Coder: Are Adjacent words:]"
//            + isAdjacentWords(example.getToponyms(), example));

    if (!isAdjacentWords(example.getToponyms(), example))
      return null;

    ArrayList<CandidateAndFeature> decoded = new ArrayList<CandidateAndFeature>();

    // copy 2d feature array into feature lists
    LocGroupFeatures feature = new LocGroupFeatures(example, mode, filter).toFeatures();
    ArrayList<ArrayList<CandidateAndFeature>> farrays = feature.getFeatureArrays();

    ArrayList<CandidateAndFeature> maxPopCandidates = getAmanalous(farrays);

    int i = 0;
    for (CandidateAndFeature d : maxPopCandidates) {
      // System.out.println("maxPosCandidate " + (i++) + " is " + d.getOriginName());
      if (d.getPopulation() > 999)
        decoded.add(d);
    }

    if (decoded.size() == 0)
      return null;
    else {
      // added the probability output for each result.
      for (CandidateAndFeature de : decoded)
        de.setProb(0.80);
      return decoded;
    }
  }

  @SuppressWarnings("unchecked")
  private boolean isAdjacentWords(List<LocEntityAnnotation> toponyms, Tweet t) {
    // TODO Auto-generated method stub
    Token[] tokens = t.getSentence().getTokens();
    ArrayList<Token> locTokens = new ArrayList<Token>();
    for (LocEntityAnnotation tp : toponyms)
      for (Token token : tp.getTokens())
        locTokens.add(token);
    Collections.sort(locTokens, new Comparator() {
      @Override
      public int compare(Object o1, Object o2) {
        // TODO Auto-generated method stub
        Integer p1 = ((Token) o1).getPosition();
        Integer p2 = ((Token) o2).getPosition();
        return p1.compareTo(p2);
      }
    });
    int current = -1;
    for (Token tok : locTokens) {
      if (current == -1) {
        current = tok.getPosition();
      } else {
        if (tok.getPosition() == current + 1) {
          current = tok.getPosition();
          continue;
        }
        for (int i = current + 1; i < tok.getPosition(); i++)
          if (!StringUtil.isPunctuation(tokens[i].getToken()))
            return false;
        current = tok.getPosition();
      }
    }
    return true;
  }

  private ArrayList<CandidateAndFeature> getAmanalous(ArrayList<ArrayList<CandidateAndFeature>> fa) {
    ArrayList<CandidateAndFeature> maxPop = new ArrayList<CandidateAndFeature>();
    if (fa == null)
      return null;
    for (ArrayList<CandidateAndFeature> word : fa)
      for (CandidateAndFeature cand : word)
        if (cand.getF_PopRank() < 1 && cand.getF_PopRank() > -0.5)
          maxPop.add(cand);
    return maxPop;
  }

}
