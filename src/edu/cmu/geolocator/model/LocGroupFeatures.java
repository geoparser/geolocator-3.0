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
package edu.cmu.geolocator.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import libsvm.svm_node;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexFileNames;

import edu.cmu.geolocator.common.TweetDisambUtil;
import edu.cmu.geolocator.parser.ParserFactory;
import edu.cmu.geolocator.resource.ResourceFactory;
import edu.cmu.geolocator.resource.gazindexing.CollaborativeIndex.CollaborativeIndex;
import edu.cmu.geolocator.resource.gazindexing.CollaborativeIndex.InfoFields;

public class LocGroupFeatures {

  ArrayList<ArrayList<CandidateAndFeature>> featureArrays;

  public ArrayList<ArrayList<CandidateAndFeature>> getFeatureArrays() {
    return featureArrays;
  }

  public static final String TRAINMODE = "train", DEBUGMODE = "debug",
          FILTERZEROPOP = "filterZeroPop", FILTERLESS1000POP = "filterLess1000Pop", NOFILTER = "nofilter";

  /**
   * create loc group features for :
   * 
   * 'train' or 'debug'
   * 
   * filter rules: 'noZeroPop', 'noLess1000Pop'
   * 
   * @param t
   * @param mode
   * @param filterRule
   * @throws Exception
   */
  public LocGroupFeatures(Tweet t, String mode, String filterRule) throws Exception {
    List<LocEntityAnnotation> topos = t.getToponyms();

    int _numberOfTopos = topos.size();

    // store feature vectors for all the loc entities.
    featureArrays = new ArrayList(_numberOfTopos);

    /**
     * Fill In The Candidate Arrays. for every candidate of every toponym in training, put them in
     * 2D array, and set label.
     */
    for (int topocount = 0; topocount < topos.size(); topocount++) {
      LocEntityAnnotation alocentity = topos.get(topocount);
      String name = alocentity.getTokenString();
      // name = name.replaceAll("[,.-]", " ").replaceAll("[ ]+", " ");
      ArrayList<Document> candidates = ResourceFactory.getClbIndex().getDocumentsByPhrase(name);
      if (candidates == null || candidates.size() == 0) {
        System.err.println("Tagging inconsistency found! The tag is not in the gazetteer!");
        continue;
      }
      ArrayList<CandidateAndFeature> tempFeatureArray = new ArrayList<CandidateAndFeature>();
      for (Document cand : candidates) {
        
        // filter 
        String a = cand.get(InfoFields.population);
        int inta = Integer.parseInt(a);
        if (filterRule.equals(LocGroupFeatures.FILTERZEROPOP))
          if (inta < 1)
            continue;
        if (filterRule.equals(LocGroupFeatures.FILTERZEROPOP))
          if (inta < 1000)
            continue;

        CandidateAndFeature aFeature = new CandidateAndFeature(alocentity.getTokenString(), cand,
                alocentity);
        // select if to fill out Y by the mode.
        if (mode.equals(LocGroupFeatures.TRAINMODE))
          if (alocentity.ids.contains(cand.get(InfoFields.id)))
            aFeature.setY(1);
          else
            aFeature.setY(0);

        tempFeatureArray.add(aFeature);
      }
      featureArrays.add(tempFeatureArray);
    }

    /**
     * Find Common Country Or State.
     */
    HashSet<String> commonCountries = LocGroupFeatures.getComCountries(featureArrays);
    HashSet<String> commonStates = LocGroupFeatures.getComStates(featureArrays);

    /**
     * Parse user location field. use TOPONYM PARSER.
     */
    List<LocEntityAnnotation> userLocs = ParserFactory.getEnToponymParser().parse(
            new Tweet(t.getUserLocation()));
    ArrayList<ArrayList<Document>> matrixDocs = new ArrayList<ArrayList<Document>>();
    if (userLocs != null && userLocs.size() != 0)
      for (LocEntityAnnotation userLoc : userLocs) {
        ArrayList<Document> docs = ResourceFactory.getClbIndex().getDocumentsByPhrase(
                userLoc.getTokenString());
        matrixDocs.add(docs);
      }

    /**
     * Fill In The Features. put the features into arrays.
     */
    for (ArrayList<CandidateAndFeature> aFeatureList : featureArrays) {
      int docListSize = aFeatureList.size();
      double totalPopulation = 0;

      for (int i = 0; i < docListSize; i++) {
        CandidateAndFeature aFeature = aFeatureList.get(i);

        /** f0: if it's a single location or not. */
        if (featureArrays.size() == 1) {
          aFeature.setF_single(true);
          // set common country and common state to empty.
          commonCountries = new HashSet<String>();
          commonStates = new HashSet<String>();
        }

        /**
         * f5: contains user location Use country or state overlap between userloc and topos.
         * Description info is not used.
         * */
        if (userLocs != null && userLocs.size() != 0) {
          aFeature.setF_containsUserLoc(true);
          /**
           * f17 user location overlap with toponym. (country and state)
           * */
          aFeature.setF_userLocOverlap(matrixDocs, userLocs);
        }

        // deprecated.
        /**
         * f6: set timezone existence. set contained in timezone. Examples for this.
         */
        // if (t.containsTimezone()) {
        // aFeature.setF_containsTimezone(true);
        // Document[] timezone = TweetDisambUtil.twitterTimezone2Country(aFeature.getTimezone());

        // deprecated
        /**
         * f7: if the location is in timezone or not. Examples for this.
         */
        // if (TweetDisambUtil.countryInTimezone(timezone, aFeature))
        // aFeature.setF_InTimezone(true);
        // }

        /** f11: set Tweet Coord. */
        if (t.containsTweetCoord()) {
          double latdiff = t.getLatitude() - aFeature.getLatitude();
          double londiff = t.getLongitude() - aFeature.getLongitude();
          double dist = Math.sqrt(latdiff * latdiff + londiff * londiff);
          /** f11_1: contain tweet coordinate feature */
          aFeature.setF_containsTweetCoordinates(true);
          /** distance to tweet coordinates, not feature yet. rank them out of loop. */
          aFeature.setF_DistanceToUserLoc(dist);
        }

        /** f12: feature code value */
        // if (aFeature.getFeature() == null || aFeature.getFeature().length() == 0)
        // aFeature.setOneHotFeatureCode(-1);
        // else {
        // aFeature.setOneHotFeatureCode(ResourceFactory.getFeatureCode2Map().getIndex(
        // aFeature.getFeature()));
        // }

        /** f13: abbreviation or not */
        if (aFeature.isAbbr())
          aFeature.setF_isAbbr(true);

        /** f14: Similarity of the candidate with the asciiName */
        if (aFeature.isAbbr() == false)
          aFeature.setF_strSim();
        else
          aFeature.setF_strSim4Abbr();

        /** f15: single candidate case. */
        if (docListSize == 1)
          aFeature.setF_singleCandidate(true);
        else
          aFeature.setF_singleCandidate(false);

        /**
         * f16: if it's a country or not THis function is added because when we use the user
         * location information, we should guaranttee that the countries won't be mis-selected.
         */
        // country has been set. Just use it in the feature extractor.

      } // end of looping aFeature.

      /** f1: set population ranking non-zero population greater than 1000 are set to 1. */
      // sort by population decreasing order.
      Collections.sort(aFeatureList);
      // the first most population one is 1 then 2, 3,... if smaller 1000 population, 0.
      // if the population is null, it's rank is 0. if not null, it's all 1.
      for (int i = 0; i < docListSize; i++) {
        // if (aFeatureList.get(i).getPopulation() == 0)
        // aFeatureList.get(i).setF_PopRank(docListSize+1);
        // else
        aFeatureList.get(i).setF_PopRank(i); // feature value is 1 if population >0.
      }

      // @deprecated
      /**
       * f2: set feature ranking use countrystate infered feature type. use the infered feature Sea
       * and Lake, put it in the paper.
       */
      // Collections.sort(aFeatureList, CandidateAndFeature.getCountryStateComparator());
      // for (int i = 0; i < docListSize; i++) {
      // aFeatureList.get(i).setF_FeatureRank(aFeatureList.get(i).getHierarcheyLevel());
      // }

      /** f3: set alternate names number ranking. */
      Collections.sort(aFeatureList, CandidateAndFeature.getAltNamesComparator());
      for (int i = 0; i < docListSize; i++) {
        int c = aFeatureList.get(i).getAltnameCount();
        // if (c == 0)
        // aFeatureList.get(i).setF_AltNamesRank(-1);
        // else
        aFeatureList.get(i).setF_AltNamesRank(i);
      }

      /**
       * f11: the distance to the user location rank.
       */
      Collections.sort(aFeatureList, CandidateAndFeature.getDistToUserLocComparator());
      for (int i = 0; i < docListSize; i++) {
        aFeatureList.get(i).setF_DistanceToUserLocRank(i);
      }

    }// end of LocEntityAnnotation loop
    /**
     * f9: set the most popular countries and popular states for multiple topos in the same
     * sentence.
     */
    for (int i = 0; i < featureArrays.size(); i++) {
      for (int j = 0; j < featureArrays.get(i).size(); j++) {
        CandidateAndFeature afeature = featureArrays.get(i).get(j);
        String cc = afeature.getCountryCode();
        String adm1 = afeature.getAdm1Code();
        if (commonCountries.contains(cc))
          afeature.setF_isCommonCountry(true);
        if (commonStates.contains(adm1))
          afeature.setF_isCommonState(true);
      }
    }

    // System.out.println("Common Countries are: \n" + commonCountries);
    // System.out.println("Common States are: \n" + commonStates);

  }

  private static HashSet<String> getComCountries(
          ArrayList<ArrayList<CandidateAndFeature>> featureArrays2) {
    int length = featureArrays2.size();
    HashMap<String, Integer> com = new HashMap<String, Integer>();
    for (ArrayList<CandidateAndFeature> l1 : featureArrays2) {
      HashMap<String, Integer> countryCodes = new HashMap<String, Integer>();
      for (CandidateAndFeature l2 : l1)
        countryCodes.put(l2.getCountryCode(), 1);
      for (Entry<String, Integer> entry : countryCodes.entrySet()) {
        if (com.containsKey(entry.getKey()))
          com.put(entry.getKey(), 1 + com.get(entry.getKey()));
        else
          com.put(entry.getKey(), 1);
      }
    }
    HashSet<String> tailoredCom = new HashSet<String>();
    for (Entry<String, Integer> entry : com.entrySet())
      if (entry.getValue() == length)
        tailoredCom.add(entry.getKey());
    return tailoredCom;
  }

  private static HashSet<String> getComStates(
          ArrayList<ArrayList<CandidateAndFeature>> featureArrays2) {
    int length = featureArrays2.size();
    HashMap<String, Integer> com = new HashMap<String, Integer>();
    for (ArrayList<CandidateAndFeature> l1 : featureArrays2) {
      HashMap<String, Integer> countryCodes = new HashMap<String, Integer>();
      for (CandidateAndFeature l2 : l1)
        countryCodes.put(l2.getAdm1Code(), 1);
      for (Entry<String, Integer> entry : countryCodes.entrySet()) {
        if (com.containsKey(entry.getKey()))
          com.put(entry.getKey(), 1 + com.get(entry.getKey()));
        else
          com.put(entry.getKey(), 1);
      }
    }
    HashSet<String> tailoredCom = new HashSet<String>();
    for (Entry<String, Integer> entry : com.entrySet())
      if (entry.getValue() == length)
        tailoredCom.add(entry.getKey());
    return tailoredCom;
  }

  public static void main(String argv[]) throws Exception {
    // sample program, not working.
    Tweet t = new Tweet();
    LocGroupFeatures cf = new LocGroupFeatures(t, LocGroupFeatures.TRAINMODE,
            LocGroupFeatures.FILTERZEROPOP);
    cf.toFeatures();
  }

  double f0, f1, f2, f3, f4, f5, f6, f7, f8, f9_1, f9_2, label, f11, f11_1;

  double f12[], f13, f14, f15, f16, f17, f17_1;

  int[] fvector;

  ArrayList<svm_node[]> featureVector;

  ArrayList<Double> labels;

  ArrayList<String> ids;

  public ArrayList<String> getIds() {
    return ids;
  }

  public LocGroupFeatures toFeatures() {

    // flaten the two dimension two one dimension.
    featureVector = new ArrayList<svm_node[]>(featureArrays.size());
    labels = new ArrayList<Double>(featureArrays.size());
    ids = new ArrayList<String>(featureArrays.size());
    for (int i = 0; i < featureArrays.size(); i++) {
      for (int j = 0; j < featureArrays.get(i).size(); j++) {
        CandidateAndFeature aFeature = featureArrays.get(i).get(j);
        f0 = aFeature.getF_single() ? 1 : 0;
        f1 = aFeature.getF_PopRank();
        // f2 = aFeature.getF_FeatureRank();
        f3 = aFeature.getF_AltNamesRank();
        // f4 = aFeature.getF_containsTweetCoordinates() ? 1 : 0;
        f5 = aFeature.getF_containsUserLoc() ? 1 : 0;
        // f6 = aFeature.getF_containsTimezone() ? 1 : 0;
        // f7 = aFeature.getF_inTimezone() ? 1 : 0;
        // f8 = aFeature.getF_userInfoOverlap();
        f9_1 = aFeature.getF_isCommonCountry() ? 1 : 0;
        f9_2 = aFeature.getF_isCommonState() ? 1 : 0;
        f11 = aFeature.getF_DistanceToUserLocRank();
        f11_1 = aFeature.getF_containsTweetCoordinates() ? 1 : 0;
        // f12 = aFeature.getOneHotFeatureVector();
        f13 = aFeature.getF_isAbbr() ? 1 : 0;
        f14 = aFeature.getF_strSim();
        f15 = aFeature.getF_singleCandidate() ? 1 : 0;
        // f16 = aFeature.getF_isCountry() ? 1 : 0;
        f17 = aFeature.getF_userLocCountryAgree() ? 1 : 0;
        f17_1 = aFeature.getF_userLocStateAgree() ? 1 : 0;
        label = aFeature.getY();
        // double[] part_of_vals = new double[] {
        double[] vals = new double[] {

            // single top
            f0,
            // poprank
            f1,
            // altnames rank
            f3

            // ,f4,// dep
            // ,f6,//dep
            // ,f7,//dep
            // ,f8,//dep

            // contextual, country and state
            , f9_1, f9_2

            // disatnce
            // , f11, f11_1

            // string sim
            , f13, f14, f15

            // ,f16// dep

            // user location.
            , f5, f17, f17_1

        };
        // double[] vals = new double[part_of_vals.length + f12.length];
        // System.arraycopy(part_of_vals, 0, vals, 0, part_of_vals.length);
        // System.arraycopy(f12, 0, vals, part_of_vals.length, f12.length);
        svm_node[] nodes = new svm_node[vals.length];
        for (int ik = 0; ik < vals.length; ik++) {
          nodes[ik] = new svm_node();
          nodes[ik].index = ik;
          nodes[ik].value = vals[ik];
        }
        /*
         * for (int ik = 0; ik < ResourceFactory.getFeatureCode2Map().size(); ik++) {
         * nodes[vals.length + ik] = new svm_node(); nodes[ik].index = vals.length + ik;
         * nodes[ik].value = fvector[ik]; }
         */
        featureVector.add(nodes);
        labels.add(label);
        ids.add(aFeature.getId());
      }
    }
    return this;
  }

  public ArrayList<svm_node[]> getFeatureVector() {
    return featureVector;
  }

  public ArrayList<Double> getLabels() {
    return labels;
  }

}
