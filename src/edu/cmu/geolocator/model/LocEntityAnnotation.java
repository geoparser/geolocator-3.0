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
import java.util.Arrays;
import java.util.Comparator;

import org.apache.lucene.document.Document;

/**
 * An entity that is extracted from text. After it's generated, geonamesID, latitude, longitude, and
 * population needs to be determine.
 * 
 * @author indri
 * 
 */
public class LocEntityAnnotation {

  /**
   * Those are filled out after generating the loc entity.
   */
  private int toksStart, toksEnd;

  private Token[] tokens;

  private String NEType;
  private double NETypeProb;

  public double getNETypeProb() {
    return NETypeProb;
  }

  public void setNETypeProb(double nETypeProb) {
    NETypeProb = nETypeProb;
  }

  /**
   * This is absolutely necessary.
   */
  private double latitude;

  private double longitude;



  public int getToksStart() {
    return toksStart;
  }

  public LocEntityAnnotation setToksStart(int toksStart) {
    this.toksStart = toksStart;
    return this;
  }

  public int getToksEnd() {
    return toksEnd;
  }

  public LocEntityAnnotation setToksEnd(int toksEnd) {
    this.toksEnd = toksEnd;
    return this;
  }

  public String getNEType() {
    return NEType;
  }

  public LocEntityAnnotation setNEType(String type) {
    this.NEType = type;

    for (int i = 0; i < tokens.length; i++) {
      tokens[i].setNE(type + "_" + (i == 0 ? "B" : "I"));
    }
    return this;
  }

  public Token[] getTokens() {
    return tokens;
  }

  public LocEntityAnnotation setTokens(Token[] token) {
    this.tokens = token;
    return this;
  }

  public double getLatitude() {
    return latitude;
  }

  public LocEntityAnnotation setLatitude(double latitude) {
    this.latitude = latitude;
    return this;
  }

  public double getLongitude() {
    return longitude;
  }

  public LocEntityAnnotation setLongitude(double longitude) {
    this.longitude = longitude;
    return this;
  }

  /**
   * tokStart, toke End, NEType, tokens are necessary. Geonames ID, lat and lon are not.
   * 
   * @param tokStart
   * @param tokEnd
   * @param NEType
   * @param tokens
   */
  public LocEntityAnnotation(int tokStart, int tokEnd, String NEType, Token[] tokens) {
    this.NEType = NEType;
    toksStart = tokStart;
    toksEnd = tokEnd;
    this.tokens = tokens;
    latitude = longitude = -999;
  }

  @Override
  public String toString() {
    String s = "[" + NEType + " : ";
    for (Token t : tokens)
      s += t.getToken() + " ";
    s += toksStart + "-" + toksEnd + " " + latitude + "," + longitude + "]";
    return s;
  }

  StringBuffer sb;

  public String getTokenString() {
    sb = new StringBuffer();
    for (Token t : tokens) {
      sb.append(t.getToken()).append(" ");
    }
    return sb.toString().trim();
  }

  @Override
  public boolean equals(Object lc) {
    if (lc instanceof LocEntityAnnotation) {
      LocEntityAnnotation le = (LocEntityAnnotation) lc;
      if (le.getTokenString().equals(this.getTokenString()) && le.toksStart == this.toksStart
              && le.toksEnd == this.toksEnd && le.NEType.equalsIgnoreCase(this.NEType))
        return true;
    }
    return false;
  }

  public static Comparator spanOrderComparator = new Comparator() {

    @Override
    public int compare(Object arg0, Object arg1) {
      LocEntityAnnotation span0 = (LocEntityAnnotation) arg0;
      LocEntityAnnotation span1 = (LocEntityAnnotation) arg1;
      if ((span0.toksStart - span0.toksEnd) > (span1.toksStart - span1.toksEnd))
        return -1;
      if ((span0.toksStart - span0.toksEnd) == (span1.toksStart - span1.toksEnd))
        return 0;
      else
        return 1;

    }

  };

  ArrayList<String> ids;
  
  public LocEntityAnnotation setGeoNamesId(ArrayList<String> ids){
    this.ids=ids;
    return this;
  }
  public LocEntityAnnotation setGeoNamesId(String id){
    if (this.ids==null)
    {
      ids = new ArrayList<String>();
      ids.add(id);
    }
    return this;
    
  }
  
  public ArrayList<String> getGeonamesIds() {
    // TODO Auto-generated method stub
    return ids;
  }
}
