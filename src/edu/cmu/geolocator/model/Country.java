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

public class Country extends CandidateAndFeature implements Comparable<CandidateAndFeature>{

  
  public Country(){
    super();
  }
  public String getAbbr() {
    return abbr;
  }
  public Country setAbbr(String abbr) {
    this.abbr = abbr;
    return this;
  }
  public String getLang() {
    return lang;
  }
  public Country setLang(String lang) {
    this.lang = lang;
    return this;
  }
  public String getRace() {
    return race;
  }
  public Country setRace(String race) {
    this.race = race;
    return this;
  }

  String abbr,lang,race;
  @Override
  public int compareTo(CandidateAndFeature arg0) {
    // TODO Auto-generated method stub
    return arg0.getId().compareTo(this.getId());
  }
    
  }
