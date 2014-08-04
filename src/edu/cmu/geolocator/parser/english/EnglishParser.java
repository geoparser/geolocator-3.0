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

package edu.cmu.geolocator.parser.english;

import java.util.ArrayList;
import java.util.List;

import edu.cmu.geolocator.model.LocEntityAnnotation;
import edu.cmu.geolocator.model.Token;
import edu.cmu.geolocator.model.Tweet;
import edu.cmu.geolocator.parser.NERTagger;
import edu.cmu.geolocator.parser.ParserFactory;
import edu.cmu.geolocator.parser.STBDParser;
import edu.cmu.geolocator.parser.TPParser;
import edu.cmu.geolocator.parser.Universal.ACE_MTNERParser;
import edu.cmu.geolocator.parser.utils.ParserUtils;
import edu.cmu.lti.weizh.fda.FDAGeoParser;
import edu.cmu.lti.weizh.models.NamedEntity;

/**
 * The aggregation of all the parsers for English.
 * 
 * @author indri
 * 
 */
public class EnglishParser {

  private NERTagger ner, finener;
  
  private ACE_MTNERParser ace;

  private STBDParser stbd;

  private TPParser tp;

  private FDAGeoParser fda;

  public EnglishParser(boolean misspell) {
	ace = ParserFactory.getACENERParser();
    ner = ParserFactory.getEnNERParser();
    finener = ParserFactory.getFineEnNERParser();
    stbd = ParserFactory.getEnSTBDParser();
    tp = ParserFactory.getEnToponymParser();
    //fda = ParserFactory.getEnFDAGeoParser();
  }

  public List<LocEntityAnnotation> parse(Tweet t) {
    ArrayList<LocEntityAnnotation> match = new ArrayList<LocEntityAnnotation>();
    long start = System.currentTimeMillis();
    /*List<LocEntityAnnotation> nerresult = ner.parse(t);
    long end = System.currentTimeMillis();
    /*System.out.println("NER parser parse time: " + (end - start));
    System.out.println("NER result is: " + nerresult);
    start = end;*/
    List<LocEntityAnnotation> stbdresult = stbd.parse(t);
    long end = System.currentTimeMillis();
    System.out.println("stbd parser time: " + (end - start));
    System.out.println("stbd result is: " + stbdresult);
    start = end;
    
    List<LocEntityAnnotation> acedresult = ace.parse(t);
    end = System.currentTimeMillis();
    System.out.println("ace parser time: " + (end - start));
    System.out.println("ace result is: " + stbdresult);
    start = end;
    
    List<LocEntityAnnotation> toporesult = tp.parse(t);
    end = System.currentTimeMillis();
    System.out.println("toponym parser time: " + (end - start));
    System.out.println("topo result is: " + toporesult);
    start = end;
    /*List<LocEntityAnnotation> finenerresult = finener.parse(t);
    end = System.currentTimeMillis();
    System.out.println("fine-ner parser time: " + (end - start));
    System.out.println("fine-ner result is: " + finenerresult);
    start = end;*/

   /* ArrayList<NamedEntity> fdaresult = fda.parse(t.getText());
    System.out.println("FDA result is " + fdaresult);
    List<LocEntityAnnotation> fdaAdapt = new ArrayList<LocEntityAnnotation>();
    for (NamedEntity res : fdaresult) {
      Token[] toks = new Token[res.getEnd() - res.getStart() + 1];
      for (int i = res.getStart(); i <= res.getEnd(); i++) {
        if (res.getSent().wordAt(i).getCorrected() == null)
          toks[i - res.getStart()] = new Token(res.getSent().wordAt(i).getWord(), null, i);
        else
          toks[i - res.getStart()] = new Token(res.getSent().wordAt(i).getCorrected(), null, i);

      }
      String entityType = res.getEntityType();
      if (entityType.equals("GPE"))
        entityType = "TP";
      else if (entityType.equals("NORP"))
        entityType = "TP";
      else if (entityType.equals("ORG"))
        entityType = "BD";
      else if (entityType.equals("LOC"))
        entityType = "TP";
      else if (entityType.equals("FAC"))
        entityType = "BD";
      fdaAdapt.add(new LocEntityAnnotation(res.getStart(), res.getEnd(), entityType, toks));
    }

    end = System.currentTimeMillis();
    System.out.println("FDA parser time: " + (end - start));*/

    //match.addAll(nerresult);
    match.addAll(acedresult);
    match.addAll(stbdresult);
    match.addAll(toporesult);
    //match.addAll(finenerresult);
    //match.addAll(fdaAdapt);

//    System.out.println(match);
/*    ArrayList<LocEntityAnnotation> newmatch = new ArrayList<LocEntityAnnotation>();
    for (LocEntityAnnotation nr : match) {
      boolean filter = true;
      if (!ParserUtils.isFilterword(nr.getTokenString().toLowerCase())) {
        filter = false;
      }
      if (filter == false) {
        newmatch.add(nr);
      }
    }*/
    // System.out.println(ParserUtils.ResultReduce(newmatch, true));
    return ParserUtils.ResultReduce(match, true);
  }
}
