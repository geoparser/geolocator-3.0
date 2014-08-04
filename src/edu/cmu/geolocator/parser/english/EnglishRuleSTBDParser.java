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
package edu.cmu.geolocator.parser.english;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.cmu.geolocator.model.LocEntityAnnotation;
import edu.cmu.geolocator.model.Sentence;
import edu.cmu.geolocator.model.Token;
import edu.cmu.geolocator.model.Tweet;
import edu.cmu.geolocator.nlp.ner.FeatureExtractor.FeatureGenerator;
import edu.cmu.geolocator.nlp.tokenizer.EuroLangTwokenizer;
import edu.cmu.geolocator.parser.ParserFactory;
import edu.cmu.geolocator.parser.STBDParser;
import edu.cmu.geolocator.parser.utils.ParserUtils;
import edu.cmu.geolocator.resource.ResourceFactory;
import edu.cmu.geolocator.resource.gazindexing.CollaborativeIndex.CollaborativeIndex;
import edu.cmu.geolocator.resource.trie.IndexSupportedTrie;

public class EnglishRuleSTBDParser implements STBDParser {

  /**
   * Default protected constructor
   */
  static String streetpattern = "\\$+[AN\\^G]+[N\\^G]";

  static String streetpattern2 = "[D]*[AN\\^G][N\\^G]";

  static String streetpattern3 = "[D]*[AN\\^G][AN\\^G][N\\^G]";

  static String streetpattern4 = "[D]*[AN\\^G][AN\\^G][AN\\^G][N\\^G]";

  static String[] stpattern = { streetpattern, streetpattern2, streetpattern3, streetpattern4 };

  static String buildingpattern = "([D]*[AN\\^G]+[N\\^G])";

  static Pattern streetpospattern;

  static Pattern buildingpospattern = Pattern.compile(buildingpattern);

  FeatureGenerator fgen;

  public EnglishRuleSTBDParser(FeatureGenerator fgen) {
    this.fgen = fgen;
  }

  List<LocEntityAnnotation> les;

  /**
   * parse street and buildings by rules, and put them into the tweet.
   * return them as well.
   * 
   * @param tweetMatches
   * @param tweet
   */
  public List<LocEntityAnnotation> parse(Tweet tweet) {
    les = new ArrayList<LocEntityAnnotation>();
    Sentence tweetSent = tweet.getSentence();
    EuroLangTwokenizer.tokenize(tweetSent);
    fgen.getPostagger().tag(tweetSent);

    String posstr = "";
    for (int i = 0; i < tweetSent.tokenLength(); i++)
      posstr += tweetSent.getTokens()[i].getPOS();

    for (int j = 0; j < stpattern.length; j++) {
      streetpospattern = Pattern.compile(stpattern[j]);
      Matcher stmatcher = streetpospattern.matcher(posstr);
      while (stmatcher.find()) {
        if (ParserUtils.isStreetSuffix(tweetSent.getTokens()[stmatcher.end() - 1].getToken())) {
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
    Matcher bdmatcher = buildingpospattern.matcher(posstr);
    while (bdmatcher.find()) {
      if (ParserUtils.isBuildingSuffix(tweetSent.getTokens()[bdmatcher.end() - 1].getToken())) {
        int startpos = bdmatcher.start();
        int endpos = bdmatcher.end() - 1;

        Token[] t = new Token[endpos - startpos + 1];
        for (int i = startpos; i <= endpos; i++) {
          t[i - startpos] = tweet.getSentence().getTokens()[i].setNE("bd");
        }
        LocEntityAnnotation le = new LocEntityAnnotation(startpos, endpos, "bd", t);

        // set the probability of the NE type
        // This may be changed later.
        le.setNETypeProb(0.85);

        les.add(le);
      }
    }
    return ParserUtils.ResultReduce(les, true);
  }

  private static EnglishRuleSTBDParser estbdparser;

  public static EnglishRuleSTBDParser getInstance() {
    if (estbdparser == null)
      estbdparser = new EnglishRuleSTBDParser(new FeatureGenerator("en",
              ResourceFactory.getClbIndex(), "res/"));
    return estbdparser;

  }

  public static void main(String argv[]) {
    Tweet t = new Tweet();
    String s = "RT @JenellaHerring: #centraltxfires Big Dog Rescue is evac. THEY NEED TRAILERS, TRUCKS & CRATES!!! shelter is located 589 Cool Water Dri ...";
    s = "Neal St. Park on fire damn man we breathing ashes";
    s = "I'm at Harrah's Louisiana Downs (8000 E Texas St, at I-220, Bossier City) http://t.co/EPvhlnd";
    s = "I'm at Trigon Bus Stop (Texas A&M University, College Station) http://t.co/xLlvSH9";
    s = "RT @ksatweather: Mandatory evacuations ordered for The Abbey Apts & Canyon Ridge Estates in the Stone Oak area. #TxWildfire";
    // s =
    // "MT @roxxsfisher: law officers asking spectators to please leave canyon ridge elem school and stone oak park for their safety. #satxwildfires";
    // s =
    // "Am worried about the #centraltxfires in Cedar Creek bc the Capitol of Texas Zoo is there. Lots of rescues & endangered animals. #pawcircle";
    t.setSentence(new Sentence(s));
    EnglishRuleSTBDParser stparser;
    for (int i = 0; i < 100; i++)
    {  stparser = ParserFactory.getEnSTBDParser();
    List<LocEntityAnnotation> locs = stparser.parse(t);
    System.out.println(locs);
    }
    

  }
}
