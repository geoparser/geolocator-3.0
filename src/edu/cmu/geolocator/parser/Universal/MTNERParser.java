/**
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
 */
package edu.cmu.geolocator.parser.Universal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import edu.cmu.geolocator.GlobalParam;
import edu.cmu.geolocator.model.LocEntityAnnotation;
import edu.cmu.geolocator.model.Sentence;
import edu.cmu.geolocator.model.Token;
import edu.cmu.geolocator.model.Tweet;
import edu.cmu.geolocator.nlp.NERFeatureFactory;
import edu.cmu.geolocator.nlp.ner.FeatureExtractor.FeatureGenerator;
import edu.cmu.geolocator.nlp.ner.FeatureExtractor.FineFeatureGenerator;
import edu.cmu.geolocator.nlp.tokenizer.EuroLangTwokenizer;
import edu.cmu.geolocator.parser.NERTagger;
import edu.cmu.geolocator.parser.ParserFactory;
import edu.cmu.minorthird.classify.ClassLabel;
import edu.cmu.minorthird.classify.Example;
import edu.cmu.minorthird.classify.Feature;
import edu.cmu.minorthird.classify.MutableInstance;
import edu.cmu.minorthird.classify.sequential.SequenceClassifier;
import edu.cmu.minorthird.util.IOUtil;

public class MTNERParser implements NERTagger {

  SequenceClassifier model;

  FeatureGenerator fg;

  /**
   * Default protected constructor
   * 
   * @throws IOException
   * 
   */

  private static MTNERParser englishparser, spanishparser, fineenglishparser;

  public static MTNERParser getInstance(String lang) throws Exception {
    if (lang.equalsIgnoreCase("en")) {
      if (englishparser == null) {
        try {
          englishparser = new MTNERParser("res/en/enNER-crf-final.model",
                  NERFeatureFactory.getInstance("en"));
        } catch (Exception e) {
          System.err.println("English NER Model File not found");
          e.printStackTrace();
        }
      }
      return englishparser;
    }
    
    if (lang.equalsIgnoreCase("fine-en")) {
      if (fineenglishparser == null) {
        try {
          fineenglishparser = new MTNERParser("res/en-crf120-fner.model",
                  NERFeatureFactory.getInstance("fine-en"));
        } catch (Exception e) {
          System.err.println("Fine English NER Model File not found");
          e.printStackTrace();
        }
      }
      return fineenglishparser;
    }
    
    if (lang.equalsIgnoreCase("es")) {
      if (spanishparser == null) {
        try {
          englishparser = new MTNERParser("res/es/esNER-crf-final.model",
                  NERFeatureFactory.getInstance("es"));
        } catch (Exception e) {
          System.err.println("Spanish NER Model File not found");
          e.printStackTrace();
        }
      }
      return englishparser;
    }
    
    throw new Exception("Language for NER tagger is not defined.");
  }

  MTNERParser(String modelname, FeatureGenerator featureg) {
    try {
      model = (SequenceClassifier) IOUtil.loadSerialized(new java.io.File(modelname));
      this.fg = featureg;
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  // Identifying location names by NER
  Example[] examples;

  Sentence tweetSentence;

  /**
   * extract the entities, and put them into the tweet. return them also.
   */
  public List<LocEntityAnnotation> parse(Tweet tweet) {
    tweetSentence = tweet.getSentence();
    EuroLangTwokenizer.tokenize(tweetSentence);
    examples = new Example[tweet.getSentence().tokenLength()];

    List<Feature[]> feature_instances;
    if (fg instanceof FineFeatureGenerator )
    {
      feature_instances = ((FineFeatureGenerator)fg).extractFeature(tweetSentence);
      examples = new Example[feature_instances.size()];

    }
    else{
      
      feature_instances = fg.extractFeature(tweetSentence);

    }    
    if (feature_instances ==null || feature_instances.size()==0)
      return null;

    for (int i = 0; i < examples.length; i++) {

      ClassLabel label = new ClassLabel("NA");
      MutableInstance instance = new MutableInstance("0", Integer.toString(i));
      Feature[] features = feature_instances.get(i);

      for (int j = 0; j < features.length; j++) {
        instance.addBinary(features[j]);
      }
      examples[i] = new Example(instance, label);
      // System.out.println(examples[i].toString());
    }
    ClassLabel[] resultlabels = model.classification(examples);
    /*
     * for (int i = 0; i < resultlabels.length; i++) {
     * System.out.print(resultlabels[i].bestClassName() + " "); }
     */
    List<LocEntityAnnotation> locs = new ArrayList<LocEntityAnnotation>();

    /**
     * rewrite the loc-entity generation, to support positions.
     */
    int startpos = -1, endpos = -1;
    String current = "O", previous = "O";
    for (int k = 0; k < resultlabels.length; k++) {
      if (k > 0)
        previous = current;
      current = resultlabels[k].bestClassName();
      if (current.equals("O"))
        if (previous.equals("O"))
          continue;
        else {
          endpos = k - 1;
//          System.out.println(startpos + " " + endpos + " " + previous);
          Token[] t = new Token[endpos - startpos + 1];
          for (int i = startpos; i <= endpos; i++) {
            t[i - startpos] = tweet.getSentence().getTokens()[i].setNE(previous);
          }
          LocEntityAnnotation le = new LocEntityAnnotation(startpos, endpos, previous, t);
          
          // set the probability of the NE type
          // This may be changed later.
          le.setNETypeProb(0.95);
          
          locs.add(le);
        }
      else if (previous.equals("O"))
        startpos = k;
      else
        endpos = k;

    }
    return locs;
  }

  public static void main(String argv[]) throws IOException {
    GlobalParam.setGazIndex("/users/indri/GazIndex");
    GlobalParam.setGeoNames("GeoNames");
    String sss = "I live in Pittsburgh. I am going to new york.";
    Tweet t = new Tweet(sss);
    BufferedReader s = new BufferedReader(new InputStreamReader(System.in, "utf-8"));
    System.out.println(">");
    while (true) {
      String ss = s.readLine();
      if (ss.length() == 0)
        continue;
      t.setSentence(ss);
      double stime = System.currentTimeMillis();
      List<LocEntityAnnotation> matches = ParserFactory.getFineEnNERParser().parse(t);
      double etime = System.currentTimeMillis();
      System.out.println(matches);
      System.out.println(etime - stime + "\n>");
    }
  }
}
