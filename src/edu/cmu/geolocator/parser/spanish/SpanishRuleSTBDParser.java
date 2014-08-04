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

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.csvreader.CsvReader;

import edu.cmu.geolocator.model.LocEntityAnnotation;
import edu.cmu.geolocator.model.Sentence;
import edu.cmu.geolocator.model.Token;
import edu.cmu.geolocator.model.Tweet;
import edu.cmu.geolocator.nlp.NLPFactory;
import edu.cmu.geolocator.nlp.ner.FeatureExtractor.FeatureGenerator;
import edu.cmu.geolocator.nlp.tokenizer.EuroLangTwokenizer;
import edu.cmu.geolocator.parser.ParserFactory;
import edu.cmu.geolocator.parser.STBDParser;
import edu.cmu.geolocator.parser.english.EnglishRuleSTBDParser;
import edu.cmu.geolocator.parser.utils.ParserUtils;
import edu.cmu.geolocator.resource.gazindexing.CollaborativeIndex.CollaborativeIndex;

public class SpanishRuleSTBDParser implements STBDParser {

  /**
   * Default protected constructor
   */
  static String streetpattern = "\\$+[an]+n";

  static String streetpattern2 = "[d]*[an]n";

  static String streetpattern3 = "[d]*[an][an]n";

  static String streetpattern4 = "[d]*[an][an][an]n";

  static String highway = "nz";

  static String av_street = "afvz";

  static String st_adj = "na";

  static String st_only = "ndn";

  static String calle = "nn[z]*|na[z]*";

  static String esquina = "ns[an]{0,2}";

  static String[] stpattern = { streetpattern, streetpattern2, streetpattern3, streetpattern4,
      highway, av_street, st_adj, st_only, calle, esquina };

  static String buildingpattern = "[d]*[an]+n";

  static String buildingpattern2 = "ns[an]+[na]";

  static String el_pattern = "[n]+dn";

  static String de_pattern = "nsn";

  static String de_sup_pattern = "nsdn";

  static String recurNS_de_pattern = "nsnsn[a]?";

  static String de_dot_pattern = "nfsn";

  static String buildingpattern3 = "[an]+";

  static String bd_adj = "na";

  static String[] bdpattern = { buildingpattern, buildingpattern2, el_pattern, recurNS_de_pattern,
      de_dot_pattern, buildingpattern3, de_pattern, bd_adj, de_sup_pattern };

  static Pattern streetpospattern;

  static Pattern buildingpospattern;

  
  ArrayList<LocEntityAnnotation> les;

  /**
   * Find streets by street suffixes
   * 
   * @param tweetMatches
   * @param tweet
   */
  public List<LocEntityAnnotation> parse(Tweet tweet) {
    les = new ArrayList<LocEntityAnnotation>();
    Sentence tweetSent = tweet.getSentence();
    EuroLangTwokenizer.tokenize(tweetSent);
    NLPFactory.getEsPosTagger().tag(tweetSent);

    String posstr = "";
    for (int i = 0; i < tweetSent.tokenLength(); i++)
      posstr += tweetSent.getTokens()[i].getPOS();

    for (int j = 0; j < stpattern.length; j++) {
      streetpospattern = Pattern.compile(stpattern[j]);
      Matcher stmatcher = streetpospattern.matcher(posstr);
      if (j == 8) {

      }
      while (stmatcher.find()) {
        if (ParserUtils.isESStreetPrefix(tweetSent.getTokens()[stmatcher.start()].getToken())) {
          int startpos = stmatcher.start();
          int endpos = stmatcher.end() - 1;
          Token[] t = new Token[endpos - startpos + 1];
          for (int i = startpos; i <= endpos; i++) {
            t[i - startpos] = tweet.getSentence().getTokens()[i].setNE("st");
          }
          LocEntityAnnotation le = new LocEntityAnnotation(startpos, endpos, "st", t);
          le.setNETypeProb(0.85);
         
les.add(le);
        }
      }
    }
    for (int j = 0; j < bdpattern.length; j++) {
      buildingpospattern = Pattern.compile(bdpattern[j]);
      Matcher bdmatcher = buildingpospattern.matcher(posstr);
      // System.out.println(j);
      if (j == 2)// the building prefix is at the end, which is the
        // building suffix
        while (bdmatcher.find()) {
          if (ParserUtils.isESBuildingPrefix(tweetSent.getTokens()[bdmatcher.end() - 1].getToken())
                  || ParserUtils.isESBuildingPrefix(tweetSent.getTokens()[bdmatcher.start()]
                          .getToken())) {
            int startpos = bdmatcher.start();
            int endpos = bdmatcher.end() - 1;

            Token[] t = new Token[endpos - startpos + 1];
            for (int i = startpos; i <= endpos; i++) {
              t[i - startpos] = tweet.getSentence().getTokens()[i].setNE("bd");
            }
            LocEntityAnnotation le = new LocEntityAnnotation(startpos, endpos, "bd", t);

            les.add(le);
          }
        }
      else if (j == 3)
        while (bdmatcher.find()) {
          if (ParserUtils.isESBuildingPrefix(tweetSent.getTokens()[bdmatcher.start() + 2]
                  .getToken())) {
            int startpos = bdmatcher.start();
            int endpos = bdmatcher.end() - 1;

            Token[] t = new Token[endpos - startpos + 1];
            for (int i = startpos; i <= endpos; i++) {
              t[i - startpos] = tweet.getSentence().getTokens()[i].setNE("bd");
            }
            LocEntityAnnotation le = new LocEntityAnnotation(startpos, endpos, "bd", t);

            le.setNETypeProb(0.85);

            les.add(le);
          }
        }
      else
        while (bdmatcher.find()) {
          if (ParserUtils.isESBuildingPrefix(tweetSent.getTokens()[bdmatcher.start()].getToken())) {
            int startpos = bdmatcher.start();
            int endpos = bdmatcher.end() - 1;

            Token[] t = new Token[endpos - startpos + 1];
            for (int i = startpos; i <= endpos; i++) {
              t[i - startpos] = tweet.getSentence().getTokens()[i].setNE("bd");
            }
            LocEntityAnnotation le = new LocEntityAnnotation(startpos, endpos, "bd", t);
            
         // added confidence value for toponym parser.
            le.setNETypeProb(0.85);
            
            les.add(le);
          }
        }
    }
    ArrayList<LocEntityAnnotation> finalmatch = new ArrayList<LocEntityAnnotation>();
    for (LocEntityAnnotation match : les) {
      String words = match.getTokenString();
      if (words.split(" ").length == 1) {
        if (ParserUtils.isBuildingSuffix(words) || ParserUtils.isESBuildingPrefix(words)
                || ParserUtils.isESStreetPrefix(words) || ParserUtils.isStreetSuffix(words))
          continue;
      } else if (words.contains("..") || words.contains("=") || words.contains(",")
              || words.contains("@") || words.contains("?") || words.contains("!")
              || words.contains(" _") || words.contains(" rt}") || words.contains("{rt ")
              || words.contains(" rt ")) {
        continue;
      } else if (finalmatch.contains(match))
        continue;
      else
        finalmatch.add(match);
    }
    return ParserUtils.ResultReduce(finalmatch, true);
  }

  public static void main(String argv[]) throws IOException {
    String s = "He estudiado en la Universidad de Carnegie Mellon durante dos a�os.";
    Tweet t = new Tweet(s);
    CollaborativeIndex ci = new CollaborativeIndex().config("GazIndex/StringIndex",
            "GazIndex/InfoIndex", "mmap", "mmap").open();

    List<LocEntityAnnotation> lcs = ParserFactory.getEsSTBDParser().parse(t);
    System.out.println(lcs);
  }

  private static SpanishRuleSTBDParser esparser;

  public static SpanishRuleSTBDParser getInstance() {
    if (esparser == null)
      esparser = new SpanishRuleSTBDParser();
    return esparser;
  }
}
