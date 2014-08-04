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

package edu.cmu.geolocator.parser;

import edu.cmu.geolocator.parser.Universal.ACE_MTNERParser;
import edu.cmu.geolocator.parser.Universal.MTNERParser;
import edu.cmu.geolocator.parser.english.EnglishParser;
import edu.cmu.geolocator.parser.english.EnglishRuleSTBDParser;
import edu.cmu.geolocator.parser.english.EnglishRuleToponymParser;
import edu.cmu.geolocator.parser.spanish.SpanishParser;
import edu.cmu.geolocator.parser.spanish.SpanishRuleSTBDParser;
import edu.cmu.geolocator.parser.spanish.SpanishRuleToponymParser;

public class ParserFactory {
  private static EnglishParser enparser;
  private static SpanishParser esparser;
  public static EnglishParser getEnAggrParser(){
    if(enparser==null)
      enparser = new EnglishParser(false);
    return enparser;
  }
  public static SpanishParser getEsAggrParser(){
    if(esparser==null)
      esparser = new SpanishParser(false);
    return esparser;
  }
  public static MTNERParser getEnNERParser() {
      try {
        return MTNERParser.getInstance("en");
      } catch (Exception e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      return null;
  }
  
  public static MTNERParser getFineEnNERParser() {
    try {
      return MTNERParser.getInstance("fine-en");
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return null;
}


  public static EnglishRuleSTBDParser getEnSTBDParser() {
      return EnglishRuleSTBDParser.getInstance();
  }
  
  public static EnglishRuleToponymParser getEnToponymParser() {
      return EnglishRuleToponymParser.getInstance();
  }

  public static MTNERParser getEsNERParser() {
    try {
      return MTNERParser.getInstance("es");
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return null;
}

public static SpanishRuleSTBDParser getEsSTBDParser() {
    return SpanishRuleSTBDParser.getInstance();
}

public static SpanishRuleToponymParser getEsToponymParser() {
    return SpanishRuleToponymParser.getInstance();
}
public static ACE_MTNERParser getACENERParser() {
	// TODO Auto-generated method stub
	return ACE_MTNERParser.getInstance();
}

}
