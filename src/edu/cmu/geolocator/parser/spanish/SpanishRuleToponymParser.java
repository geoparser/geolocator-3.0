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
package edu.cmu.geolocator.parser.spanish;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;

import edu.cmu.geolocator.common.StringUtil;
import edu.cmu.geolocator.model.LocEntityAnnotation;
import edu.cmu.geolocator.model.Sentence;
import edu.cmu.geolocator.model.Token;
import edu.cmu.geolocator.model.Tweet;
import edu.cmu.geolocator.nlp.MisspellParser;
import edu.cmu.geolocator.nlp.NLPFactory;
import edu.cmu.geolocator.nlp.ner.FeatureExtractor.FeatureGenerator;
import edu.cmu.geolocator.nlp.tokenizer.EuroLangTwokenizer;
import edu.cmu.geolocator.parser.ParserFactory;
import edu.cmu.geolocator.parser.TPParser;
import edu.cmu.geolocator.parser.english.EnglishRuleToponymParser;
import edu.cmu.geolocator.parser.utils.ParserUtils;
import edu.cmu.geolocator.resource.ResourceFactory;
import edu.cmu.geolocator.resource.gazindexing.CollaborativeIndex.CollaborativeIndex;

public class SpanishRuleToponymParser implements TPParser {

  static final int GRAM = 5;

  static String[] ngram;

  static String[] posngram;

  static boolean allstopwords;

  static ArrayList<String> results;

  static String p1 = "d[an]";

  static String p2 = "[avn]";

  static String p3 = "[an][an]";

  static String p4 = "[an][an][an]";

  static String[] patterns = { p1, p2, p3, p4 };

  static Pattern gazpattern;

  /*
   * Gaz Matching. The parser only lookup the token array. This parser does not tokenize the input
   * string for efficiency reasons.
   */
  public SpanishRuleToponymParser() {
  }

  List<LocEntityAnnotation> les;

  public List<LocEntityAnnotation> parse(Tweet tweet) {
    // TODO Auto-generated method stub
    les = new ArrayList<LocEntityAnnotation>();

    if (tweet == null || tweet.getSentence() == null
            || tweet.getSentence().getSentenceString() == null
            || tweet.getSentence().getSentenceString().length() == 0)
      return null;

    Sentence tweetSent = tweet.getSentence();
    EuroLangTwokenizer.tokenize(tweetSent);
    NLPFactory.getEsPosTagger().tag(tweetSent);
    Token[] tokens = tweetSent.getTokens();

    System.out.println("Tokenization : \n" + tokens.toString());

    for (Token t : tokens) {
      if (t.getToken().startsWith("#"))
        t.setToken(t.getToken().substring(1));
    }

    String posstr = "";
    for (int i = 0; i < tweetSent.tokenLength(); i++)
      posstr += tweetSent.getTokens()[i].getPOS().charAt(0);

    System.out.println("POS : \n" + posstr);

    // convert Tokens to Strings
    String[] toks = new String[tokens.length];
    for (int i = 0; i < toks.length; i++)
      toks[i] = tokens[i].getToken();

    Token[] countryToks, topoToks;// store the token array variable inside.

    // match countries without considering the part of speech.
    for (int i = 1; i < 5; i++) {
      String[] igrams = StringUtil.constructgrams(toks, i, true);
      if (igrams == null)
        continue;

      // current ngram starting position is j.
      // length is i, cause it's i-gram.
      for (int j = 0; j < igrams.length; j++) {
        if (ParserUtils.isCountry(igrams[j])) {
          String[] str = igrams[j].split(" ");
          int min = i;// minimal dimension for the igram
          if (str.length != i) {
            System.out.println("dimension not agree when unwrapping ngram in enTopoParser.");
            System.out.println("Proceed anyway. Discard the rest part in ngram.");
            min = Math.min(i, str.length);// if demension not agree, choose smaller one.
          }
          countryToks = new Token[min];
          for (int k = 0; k < min; k++) {
            countryToks[k] = new Token(str[k], tweet.getId(), j);
          }

        LocEntityAnnotation le = new LocEntityAnnotation(j, j + min - 1, "tp", countryToks);
        le.setNETypeProb(0.65); 
        les.add(le);

          
        }
      }
    }

    for (int p = 0; p < patterns.length; p++) {
      gazpattern = Pattern.compile(patterns[p]);
      Matcher gazmatcher = gazpattern.matcher(posstr);
      while (gazmatcher.find()) {
        // System.out.println("found");
        int n = gazmatcher.end() - gazmatcher.start();
        String[] subtoks = new String[n];

        int _offset = gazmatcher.start();

        for (int i = gazmatcher.start(); i < gazmatcher.end(); i++) {
          subtoks[i - gazmatcher.start()] = tokens[i].getToken();
        }
        for (int i = 1; i < n + 1; i++) {
          String[] igrams = StringUtil.constructgrams(subtoks, i, true);
          for (int j = 0; j < igrams.length; j++) {

            // match gaz entry in the chunk.
            if (ResourceFactory.getClbIndex().inIndex(igrams[j].trim())) {

              if (ParserUtils.isEsFilterword(igrams[j].trim())
                      || igrams[j].trim().equalsIgnoreCase("el")
                      || igrams[j].trim().equalsIgnoreCase("ha")) {
                continue;
              }
              // if (ParserUtils.isEsFilterword(kgram)&&Character.isUpperCase(kgram.charAt(0))
              // &&
              // StringUtil.mostTokensUpperCased(tokens)
              // && tokens.size()>5
              // ) {
              // continue;
              // }
              // System.out.println("word to be added is: " + kgram);
              String[] str = igrams[j].split(" ");
              int min = i;
              if (str.length != i) {
                System.out.println("dimension not agree when unwrapping ngram in enTopoParser.");
                System.out.println("Proceed anyway. Discard the rest part in ngram.");
                Math.min(i, str.length);// if demension not agree, choose smaller one.
              }
              topoToks = new Token[min];
              for (int k = 0; k < min; k++) {
                topoToks[k] = new Token(str[k], tweet.getId(), _offset + j);
              }

              LocEntityAnnotation le = new LocEntityAnnotation(_offset + j, _offset + j + i - 1, "tp", topoToks);
              le.setNETypeProb(0.65);
              les.add(le);
            }
          }
        }
      }
    }
    return ParserUtils.ResultReduce(les, true);
  }

  private static SpanishRuleToponymParser etpparser;

  public static SpanishRuleToponymParser getInstance() {
    if (etpparser == null)
      try {
        etpparser = new SpanishRuleToponymParser();
      } catch (Exception e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    return etpparser;
  }

  public static void main(String argv[]) throws IOException {

    Tweet t = new Tweet();
    BufferedReader s = new BufferedReader(new InputStreamReader(System.in, "utf-8"));
    System.out.println(">");
    while (true) {
      String ss = s.readLine();
      if (ss.length() == 0)
        continue;
      t.setSentence(ss);
      double stime = System.currentTimeMillis();
      List<LocEntityAnnotation> matches = ParserFactory.getEsToponymParser().parse(t);
      double etime = System.currentTimeMillis();
      System.out.println(matches);
      System.out.println(etime - stime + "\n>");
    }
  }

}
