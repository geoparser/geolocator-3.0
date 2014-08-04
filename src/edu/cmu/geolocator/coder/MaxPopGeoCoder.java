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
import java.util.List;

import edu.cmu.geolocator.model.CandidateAndFeature;
import edu.cmu.geolocator.model.LocGroupFeatures;
import edu.cmu.geolocator.model.Tweet;

/**
 * Pick the one with the largest population for the geocoding.
 * 
 * @author indri
 * 
 */
public class MaxPopGeoCoder {

  private static MaxPopGeoCoder rgcoder;

  public static MaxPopGeoCoder getInstance() {
    if (rgcoder == null) {
      rgcoder = new MaxPopGeoCoder();
    }
    return rgcoder;
  }

  public List<CandidateAndFeature> resolve(Tweet example, String mode,String filter) throws Exception {

    ArrayList<CandidateAndFeature> decoded = new ArrayList<CandidateAndFeature>();

    // copy 2d feature array into feature lists
//    System.out.println("Max Pop feature extration start");
    long time = System.currentTimeMillis();
    LocGroupFeatures feature = new LocGroupFeatures(example, mode,filter).toFeatures();
    if (feature==null)
      return null;
    long etime = System.currentTimeMillis();
//    System.out.println("MaxPop feature extration time: "+(etime-time));

//    System.out.println("GeoCoder get Feature Arrays start");
    time = System.currentTimeMillis();

    ArrayList<ArrayList<CandidateAndFeature>> farrays = feature.getFeatureArrays();
    
    etime = System.currentTimeMillis();
//    System.out.println("MaxPop get FeatureArray time: "+(etime-time));

    time = System.currentTimeMillis();
//    System.out.println("GeoCoder get max candidate start");

    ArrayList<CandidateAndFeature> maxPopCandidates = getMaxPopulation(farrays);

    etime = System.currentTimeMillis();
//    System.out.println("MaxPop get max candidate time: "+(etime-time));

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
      for(CandidateAndFeature de:decoded)
        de.setProb(0.75);
      
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
        if (cand.getF_PopRank() < 1 && cand.getF_PopRank() > -0.5)
          maxPop.add(cand);
    return maxPop;
  }

}
